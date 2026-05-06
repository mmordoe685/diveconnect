-- ============================================================
-- Vistas SQL — DiveConnect
-- ============================================================
-- Las vistas materializan consultas frecuentes de la API en una
-- estructura legible y reutilizable. Están pensadas para:
--   1. Acelerar la lectura desde reports y paneles de admin.
--   2. Evitar repetir JOINs complejos en JPQL.
--   3. Permitir granting de permisos finos sin exponer las
--      tablas base completas.
-- ============================================================

USE diveconnect_db;

-- ------------------------------------------------------------
-- vw_reservas_resumen
-- ------------------------------------------------------------
-- Combina reserva + inmersión + centro + usuario para listar
-- reservas con todo el contexto en una única query.
-- Usada por: panel de admin, exportaciones, dashboard de centro.
-- ------------------------------------------------------------
DROP VIEW IF EXISTS vw_reservas_resumen;
CREATE VIEW vw_reservas_resumen AS
SELECT
    r.id                            AS reserva_id,
    r.fecha_reserva                 AS fecha_reserva,
    r.estado                        AS estado_reserva,
    r.payment_status                AS estado_pago,
    r.numero_personas               AS personas,
    r.precio_total                  AS importe_total,
    u.id                            AS usuario_id,
    u.username                      AS usuario_username,
    u.email                         AS usuario_email,
    u.nivel_certificacion           AS usuario_nivel,
    i.id                            AS inmersion_id,
    i.titulo                        AS inmersion_titulo,
    i.fecha_inmersion               AS inmersion_fecha,
    i.profundidad_maxima            AS inmersion_profundidad,
    i.nivel_requerido               AS inmersion_nivel,
    c.id                            AS centro_id,
    c.nombre                        AS centro_nombre,
    c.ciudad                        AS centro_ciudad,
    CASE
        WHEN r.payment_status = 'PAID' THEN 'Pagada'
        WHEN r.payment_status = 'UNPAID' THEN 'Pendiente'
        ELSE 'Estado desconocido'
    END                             AS estado_pago_humano
FROM reservas r
JOIN usuarios u       ON u.id = r.usuario_id
JOIN inmersiones i    ON i.id = r.inmersion_id
JOIN centros_buceo c  ON c.id = r.centro_buceo_id;

-- ------------------------------------------------------------
-- vw_estadisticas_centro
-- ------------------------------------------------------------
-- Agregación por centro: nº de inmersiones, plazas vendidas,
-- ingresos confirmados, valoración media. Sirve al dashboard
-- del USUARIO_EMPRESA y al panel de admin.
-- ------------------------------------------------------------
DROP VIEW IF EXISTS vw_estadisticas_centro;
CREATE VIEW vw_estadisticas_centro AS
SELECT
    c.id                                                AS centro_id,
    c.nombre                                            AS centro,
    c.ciudad                                            AS ciudad,
    COUNT(DISTINCT i.id)                                AS num_inmersiones,
    COUNT(DISTINCT r.id)                                AS num_reservas,
    COALESCE(SUM(CASE WHEN r.estado IN ('CONFIRMADA','COMPLETADA')
                       THEN r.numero_personas ELSE 0 END), 0)
                                                        AS plazas_vendidas,
    COALESCE(SUM(CASE WHEN r.payment_status = 'PAID'
                       THEN r.precio_total ELSE 0 END), 0)
                                                        AS ingresos_pagados,
    c.valoracion_promedio                               AS valoracion
FROM centros_buceo c
LEFT JOIN inmersiones i ON i.centro_buceo_id = c.id AND i.activa = TRUE
LEFT JOIN reservas r    ON r.centro_buceo_id = c.id
GROUP BY c.id, c.nombre, c.ciudad, c.valoracion_promedio;

-- ------------------------------------------------------------
-- vw_actividad_usuario
-- ------------------------------------------------------------
-- Resumen por usuario: nº de publicaciones, comentarios, likes,
-- reservas, inmersiones declaradas. Útil para el perfil público
-- y para filtrar usuarios activos vs inactivos.
-- ------------------------------------------------------------
DROP VIEW IF EXISTS vw_actividad_usuario;
CREATE VIEW vw_actividad_usuario AS
SELECT
    u.id                                AS usuario_id,
    u.username                          AS username,
    u.email                             AS email,
    u.tipo_usuario                      AS tipo,
    u.nivel_certificacion               AS nivel,
    u.numero_inmersiones                AS inmersiones_declaradas,
    (SELECT COUNT(*) FROM publicaciones p WHERE p.usuario_id = u.id)
                                        AS num_publicaciones,
    (SELECT COUNT(*) FROM comentarios cm WHERE cm.usuario_id = u.id)
                                        AS num_comentarios,
    (SELECT COUNT(*) FROM reservas r WHERE r.usuario_id = u.id)
                                        AS num_reservas,
    (SELECT COUNT(*) FROM seguidores s WHERE s.seguido_id = u.id)
                                        AS num_seguidores,
    (SELECT COUNT(*) FROM seguidores s WHERE s.seguidor_id = u.id)
                                        AS num_siguiendo
FROM usuarios u
WHERE u.activo = TRUE;

-- ------------------------------------------------------------
-- vw_inmersiones_disponibles
-- ------------------------------------------------------------
-- Catálogo público filtrando inmersiones activas con plazas.
-- ------------------------------------------------------------
DROP VIEW IF EXISTS vw_inmersiones_disponibles;
CREATE VIEW vw_inmersiones_disponibles AS
SELECT
    i.id,
    i.titulo,
    i.descripcion,
    i.fecha_inmersion,
    i.profundidad_maxima,
    i.nivel_requerido,
    i.precio,
    i.plazas_disponibles,
    i.plazas_totales,
    i.ubicacion,
    i.latitud,
    i.longitud,
    c.id           AS centro_id,
    c.nombre       AS centro_nombre,
    c.ciudad       AS centro_ciudad,
    c.valoracion_promedio AS centro_valoracion
FROM inmersiones i
JOIN centros_buceo c ON c.id = i.centro_buceo_id
WHERE i.activa = TRUE
  AND i.plazas_disponibles > 0
  AND i.fecha_inmersion >= NOW();

-- ------------------------------------------------------------
-- vw_notificaciones_no_leidas
-- ------------------------------------------------------------
-- Acelera el contador del badge de la topbar evitando contar
-- en cada poll el WHERE leida = FALSE.
-- ------------------------------------------------------------
DROP VIEW IF EXISTS vw_notificaciones_no_leidas;
CREATE VIEW vw_notificaciones_no_leidas AS
SELECT
    destinatario_id   AS usuario_id,
    COUNT(*)          AS num_no_leidas
FROM notificaciones
WHERE leida = FALSE
GROUP BY destinatario_id;
