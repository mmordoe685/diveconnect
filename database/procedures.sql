-- ============================================================
-- Procedimientos almacenados — DiveConnect
-- ============================================================
-- Encapsulan operaciones que tocan varias tablas en un único
-- punto, garantizando atomicidad y reduciendo round-trips.
-- ============================================================

USE diveconnect_db;

DELIMITER //

-- ------------------------------------------------------------
-- sp_marcar_reserva_pagada(reservaId, paymentRef)
-- ------------------------------------------------------------
-- Operación crítica del flujo de pago.
-- 1. Marca la reserva como PAID + CONFIRMADA.
-- 2. Inserta dos notificaciones: usuario + centro.
-- Todo en una transacción explícita para garantizar consistencia
-- aunque el cliente JPA falle en mitad del flujo.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_marcar_reserva_pagada//
CREATE PROCEDURE sp_marcar_reserva_pagada(
    IN p_reserva_id BIGINT,
    IN p_payment_ref VARCHAR(255),
    IN p_pasarela VARCHAR(20)
)
BEGIN
    DECLARE v_usuario_id BIGINT;
    DECLARE v_centro_usuario_id BIGINT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Capturar IDs antes de modificar
    SELECT r.usuario_id, c.usuario_id
      INTO v_usuario_id, v_centro_usuario_id
      FROM reservas r
      JOIN centros_buceo c ON c.id = r.centro_buceo_id
     WHERE r.id = p_reserva_id;

    IF v_usuario_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Reserva no encontrada';
    END IF;

    -- Marcar pagada y confirmada
    UPDATE reservas
       SET payment_status = 'PAID',
           estado = 'CONFIRMADA',
           stripe_payment_intent_id = CASE WHEN p_pasarela = 'Stripe'
                                            THEN p_payment_ref
                                            ELSE stripe_payment_intent_id END,
           paypal_capture_id = CASE WHEN p_pasarela = 'PayPal'
                                            THEN p_payment_ref
                                            ELSE paypal_capture_id END,
           ultima_modificacion = NOW()
     WHERE id = p_reserva_id;

    -- Notificación al usuario
    INSERT INTO notificaciones
        (destinatario_id, emisor_id, tipo, mensaje, leida, accionable, resuelta,
         entidad_relacionada_id, fecha_creacion)
    VALUES
        (v_usuario_id, NULL, 'RESERVA_CONFIRMADA',
         CONCAT('Tu reserva ha sido confirmada y pagada con ', p_pasarela),
         FALSE, FALSE, FALSE, p_reserva_id, NOW());

    -- Notificación al centro (sólo si tiene usuario propietario)
    IF v_centro_usuario_id IS NOT NULL THEN
        INSERT INTO notificaciones
            (destinatario_id, emisor_id, tipo, mensaje, leida, accionable, resuelta,
             entidad_relacionada_id, fecha_creacion)
        VALUES
            (v_centro_usuario_id, v_usuario_id, 'RESERVA_RECIBIDA',
             'Has recibido una nueva reserva confirmada',
             FALSE, FALSE, FALSE, p_reserva_id, NOW());
    END IF;

    COMMIT;
END//

-- ------------------------------------------------------------
-- sp_purgar_historias_expiradas()
-- ------------------------------------------------------------
-- Limpia historias 24h ya caducadas. Se puede programar como
-- evento MySQL o llamar desde un cron.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_purgar_historias_expiradas//
CREATE PROCEDURE sp_purgar_historias_expiradas()
BEGIN
    DELETE FROM historias
     WHERE expira_en < NOW();

    SELECT ROW_COUNT() AS historias_purgadas;
END//

-- ------------------------------------------------------------
-- sp_estadisticas_globales()
-- ------------------------------------------------------------
-- Devuelve un único registro con las métricas globales del
-- sistema: usuarios, publicaciones, reservas pagadas e ingresos.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_estadisticas_globales//
CREATE PROCEDURE sp_estadisticas_globales()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM usuarios WHERE activo = TRUE)
                                                AS usuarios_activos,
        (SELECT COUNT(*) FROM usuarios WHERE tipo_usuario = 'USUARIO_EMPRESA')
                                                AS centros_registrados,
        (SELECT COUNT(*) FROM inmersiones WHERE activa = TRUE)
                                                AS inmersiones_activas,
        (SELECT COUNT(*) FROM publicaciones)    AS total_publicaciones,
        (SELECT COUNT(*) FROM reservas)         AS total_reservas,
        (SELECT COUNT(*) FROM reservas WHERE payment_status = 'PAID')
                                                AS reservas_pagadas,
        (SELECT COALESCE(SUM(precio_total),0) FROM reservas WHERE payment_status = 'PAID')
                                                AS ingresos_acumulados;
END//

-- ------------------------------------------------------------
-- TRIGGER: actualiza valoracion_promedio del centro al insertar
-- una reserva COMPLETADA con una valoración (extensión futura).
-- Lo dejamos preparado aunque el campo de valoración por reserva
-- aún no se exponga en la API.
-- ------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_recalcular_valoracion_centro//

DELIMITER ;

-- ------------------------------------------------------------
-- ÍNDICES adicionales — wrap en procedimiento porque MySQL no
-- soporta CREATE INDEX IF NOT EXISTS de forma directa.
-- Hibernate crea automáticamente índices para PKs y FKs;
-- estos son índices secundarios para las queries más calientes.
-- ------------------------------------------------------------
DELIMITER //

DROP PROCEDURE IF EXISTS sp_crear_indices_si_faltan//
CREATE PROCEDURE sp_crear_indices_si_faltan()
BEGIN
    DECLARE n_idx INT;

    SELECT COUNT(*) INTO n_idx FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND index_name = 'idx_reservas_usuario_estado';
    IF n_idx = 0 THEN
        ALTER TABLE reservas ADD INDEX idx_reservas_usuario_estado (usuario_id, estado);
    END IF;

    SELECT COUNT(*) INTO n_idx FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND index_name = 'idx_inmersiones_activa_fecha';
    IF n_idx = 0 THEN
        ALTER TABLE inmersiones ADD INDEX idx_inmersiones_activa_fecha (activa, fecha_inmersion);
    END IF;

    SELECT COUNT(*) INTO n_idx FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND index_name = 'idx_publicaciones_fecha';
    IF n_idx = 0 THEN
        ALTER TABLE publicaciones ADD INDEX idx_publicaciones_fecha (fecha_publicacion);
    END IF;

    SELECT COUNT(*) INTO n_idx FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND index_name = 'idx_notif_destinatario_leida';
    IF n_idx = 0 THEN
        ALTER TABLE notificaciones ADD INDEX idx_notif_destinatario_leida (destinatario_id, leida);
    END IF;

    SELECT COUNT(*) INTO n_idx FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND index_name = 'idx_historias_expiracion';
    IF n_idx = 0 THEN
        ALTER TABLE historias ADD INDEX idx_historias_expiracion (expira_en);
    END IF;
END//

DELIMITER ;

CALL sp_crear_indices_si_faltan();
