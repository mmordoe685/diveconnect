# Backlog de Issues para GitHub

Este fichero contiene **issues retrospectivos** del proyecto: bugs reales encontrados durante el desarrollo, mejoras técnicas y deuda. Su función es triple:

1. Demostrar al tribunal que existe un **registro escrito** del trabajo de calidad: identificación de problemas, diagnóstico, solución y verificación.
2. Servir como **changelog técnico** complementario al `CHANGELOG.md`.
3. Permitirme reabrir cualquiera de ellos en GitHub Issues con un copy-paste si en el futuro alguno reaparece.

> **Cómo usarlo:** copia cada bloque "Title + Body" en GitHub → Issues → New issue. Pegar y guardar. Las issues marcadas `closed:` ya están resueltas y se pueden cerrar inmediatamente.

---

## #1 — closed: Lombok @Data + Set<Usuario> rompe equals/hashCode en JPA

**Etiquetas:** `bug`, `backend`, `crítico`, `wontfix-deeper`

**Descripción:**
La entidad `Usuario` está anotada con `@Data` (Lombok) y tiene dos `Set<Usuario>` autorreferenciados (`seguidores`, `siguiendo`). Lombok genera `equals/hashCode` cubriendo TODAS las propiedades, incluidas las colecciones. Esto provoca recursión infinita al hacer `set.contains(otroUsuario)` y comportamiento no determinista en `Set.contains/Set.remove`.

**Síntomas observados:**
- Tras aceptar una solicitud de seguimiento, el endpoint `/api/seguimiento/estado/{usuarioId}` seguía devolviendo `NO_SIGUE` aunque el `JOIN` de la BD confirmaba que la fila estaba en `seguidores`.
- Imposible borrar un seguimiento desde código JPA (`siguiendo.remove(otro)` no surtía efecto).

**Diagnóstico:**
El stack trace al hacer `set.contains(...)` mostraba decenas de invocaciones a `Usuario.hashCode()` antes de un `StackOverflowError` en cargas con muchos usuarios.

**Solución aplicada:**
Bypassar el `Set` para las consultas críticas y operar directamente contra la tabla puente con SQL nativo en `UsuarioRepository`:
```java
@Query(value = "INSERT IGNORE INTO seguidores (seguidor_id, seguido_id) VALUES (:s, :d)", nativeQuery = true)
void addSeguidor(@Param("s") Long s, @Param("d") Long d);

@Query(value = "DELETE FROM seguidores WHERE seguidor_id = :s AND seguido_id = :d", nativeQuery = true)
void removeSeguidor(@Param("s") Long s, @Param("d") Long d);

@Query(value = "SELECT COUNT(*) FROM seguidores WHERE seguidor_id = :s AND seguido_id = :d", nativeQuery = true)
long countSeguimiento(@Param("s") Long s, @Param("d") Long d);

default boolean existsSeguimiento(Long s, Long d) {
    return countSeguimiento(s, d) > 0;
}
```

**Alternativa descartada:** anotar las entidades con `@EqualsAndHashCode(of = "id")` o reemplazar `@Data` por `@Getter @Setter` selectivos. Implicaba tocar las 15 entidades con riesgo de regresiones. La solución actual es local al subsistema de seguimiento.

---

## #2 — closed: Race en `Inmersiones.confirmarReserva` lee precio de objeto null

**Etiquetas:** `bug`, `frontend`, `crítico`

**Descripción:**
La función JS `confirmarReserva()` cierra el modal de reserva ANTES de leer `inmersionActual.precio`. `cerrarModal()` pone `inmersionActual = null`, así que la siguiente línea lanza `Cannot read properties of null (reading 'precio')` y la reserva nunca llega al modal de pago.

**Reproducción:**
1. Login como cualquier `USUARIO_COMUN`.
2. Inmersiones → cualquier card → "Reservar".
3. Modal de reserva se abre → "Confirmar reserva".
4. Error en consola y modal de pago no aparece.

**Solución aplicada:**
Capturar `precio`, `título` e `id` ANTES de llamar a `cerrarModal()`:
```javascript
const precioInmersion = inmersionActual.precio || 0;
const tituloInmersion = inmersionActual.titulo || 'Inmersión DiveConnect';
const idInmersion     = inmersionActual.id;
// ... ahora sí cerrarModal() y abrir modal de pago con los valores capturados
```

---

## #3 — closed: Notificación de reserva confirmada falta en flujo Stripe demo

**Etiquetas:** `bug`, `backend`, `media`

**Descripción:**
Tras confirmar pago vía `/api/payments/verify/{id}` (modo demo o Stripe sandbox sin sesión), no se generaba notificación al usuario ni al centro. Sólo el flujo PayPal disparaba notificaciones.

**Solución aplicada:**
`PaymentController.verificar()` añade idempotencia y dispara dos notificaciones tras cualquier paso a `PAID`:
- `RESERVA_CONFIRMADA` al usuario.
- `RESERVA_RECIBIDA` al centro.

Si la reserva ya estaba pagada antes de la llamada, `verificar()` devuelve `{status: PAID, alreadyPaid: true}` sin notificar de nuevo.

---

## #4 — closed: `paymentStatus = null` ocultaba el botón "Pagar"

**Etiquetas:** `bug`, `frontend`, `baja`

**Descripción:**
Las reservas seed creadas por `DataInitializer` no tenían `paymentStatus` definido. La condición JS `r.paymentStatus === 'UNPAID'` no se cumplía y la página `reservas.html` no mostraba el botón "Pagar" en reservas pendientes legacy.

**Solución aplicada:**
1. Tratar `null` como UNPAID en el frontend (red de seguridad).
2. Normalizar el seed para que cualquier `Reserva` tenga `paymentStatus` consistente desde el inicio (PAID en CONFIRMADA/COMPLETADA, UNPAID en PENDIENTE/CANCELADA).

---

## #5 — closed: Migración EmojiStripMigration para limpieza de seed

**Etiquetas:** `enhancement`, `backend`, `migración`

**Descripción:**
Tras decidir quitar emojis del UI por consistencia visual (no dependen de la fuente del SO), había que limpiar los registros existentes en BD que ya tenían emojis (publicaciones, biografías, comentarios, historias, descripciones).

**Solución aplicada:**
`EmojiStripMigration extends CommandLineRunner` con `@Order(100)`: se ejecuta tras `DataInitializer` y recorre las tablas `publicaciones`, `comentarios`, `historias`, `usuarios` (biografía + descripción_empresa), `centros_buceo` (descripción). Aplica regex Unicode (rangos U+1F300-1FAFF, U+2600-26FF, U+2700-27BF, U+1F000-1F2FF, U+FE0F) y compacta espacios sobrantes. **Idempotente**: la segunda pasada no encuentra nada que cambiar.

Tests unitarios cubren los principales rangos de emoji + edge cases.

---

## #6 — open: Migrar uploads a S3 / R2

**Etiquetas:** `enhancement`, `infra`, `low-priority`

**Descripción:**
Actualmente las imágenes y vídeos subidos por usuarios se guardan en disco local (`uploads/`). En producción real con varias instancias del backend tras un load balancer, este enfoque rompe (cada nodo tiene su propio sistema de ficheros).

**Plan:**
- Sustituir el `Files.copy(...)` de `UploadController` por SDK de AWS S3 o Cloudflare R2.
- Servir los archivos desde el bucket directamente (con CDN si hay presupuesto) en vez de `/uploads/**`.
- Mantener `WebConfig.addResourceHandlers` como fallback local sólo si `cloud.bucket.name` está vacío.

---

## #7 — open: Refresh tokens para sesiones largas

**Etiquetas:** `enhancement`, `seguridad`, `backend`

**Descripción:**
JWT actual expira en 24 h. Tras eso, el usuario tiene que volver a hacer login. En una app de producción con uso diario, sería mejor dual-token: access token corto (15 min) + refresh token largo (30 días) almacenado en cookie `httpOnly`.

**Plan:**
- Nueva entidad `RefreshToken` con `userId`, `tokenHash`, `expiresAt`, `revoked`.
- Endpoint `POST /api/auth/refresh` que valida el cookie y devuelve un nuevo access token.
- Logout revoca el refresh token (no solo borra del cliente).

---

## #8 — open: WebSockets para notificaciones en tiempo real

**Etiquetas:** `enhancement`, `frontend`, `backend`

**Descripción:**
El badge de notificaciones se actualiza por poll cada 30 s. Funciona, pero gasta llamadas innecesarias y tiene retardo perceptible. Mejor: WebSocket con Spring's `STOMP` y suscripción por usuario.

**Plan:**
- Añadir `spring-boot-starter-websocket`.
- Endpoint STOMP `/ws/notificaciones`.
- Tras crear notificación en `NotificacionService.crear`, publicar al canal del destinatario.
- Frontend usa SockJS + STOMP para conectarse y desactiva el poll.

---

## #9 — open: Tests E2E con Playwright

**Etiquetas:** `enhancement`, `testing`

**Descripción:**
Hay tests unitarios JUnit, pero no E2E que validen el flujo completo en un navegador real (login → reservar → pagar → ver notificación).

**Plan:**
- Añadir Playwright (Node) en `tests/e2e/`.
- Tres scenarios mínimos:
  1. Registro + login con cuenta nueva.
  2. Reserva de inmersión con pago demo.
  3. Aceptar solicitud de seguimiento.
- Workflow GitHub Actions que arranque docker-compose y corra los tests.

---

## #10 — open: i18n (inglés y portugués)

**Etiquetas:** `enhancement`, `frontend`, `roadmap`

**Descripción:**
Toda la UI está hardcodeada en español. Para abrirse a buceadores extranjeros (Portugal, Francia, Italia, UK), hace falta i18n.

**Plan:**
- Extraer las strings de las páginas HTML a un fichero JSON central por idioma.
- Pequeña librería JS que sustituye los textos por `data-i18n="key"`.
- Cookie `lang` o param `?lang=en` para cambiar idioma.

---

## #11 — open: Job programado para purgar historias 24h

**Etiquetas:** `enhancement`, `backend`, `clean-up`

**Descripción:**
La tabla `historias` tiene `expira_en`. La query de listado las filtra dinámicamente, pero las filas caducadas siguen ocupando espacio. Hay un procedimiento `sp_purgar_historias_expiradas` listo, pero no se ejecuta automáticamente.

**Plan:**
- Añadir `@Scheduled(cron = "0 0 4 * * *")` en `HistoriaService.purgarExpiradas()` que llame al procedimiento o ejecute `DELETE FROM historias WHERE expira_en < NOW()`.
- Habilitar `@EnableScheduling` en la clase principal.

---

## #12 — open: Recomendador de inmersiones

**Etiquetas:** `enhancement`, `python`, `data`

**Descripción:**
Aprovechar el módulo de Python (Análisis de Datos) para construir un recomendador básico que muestre al usuario inmersiones similares a las que ha reservado o publicado.

**Plan:**
- Script Python que lee la BD, calcula similitud entre inmersiones (nivel, profundidad, ubicación, especies).
- Guarda los top-5 por usuario en una tabla `recomendaciones_cache`.
- Endpoint `GET /api/recomendaciones/mis` que lee la caché.
