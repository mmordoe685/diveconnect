# Changelog

Todas las versiones del proyecto, con resumen de cambios. Sigue el formato [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

## [1.0.0] – 2026-05-22 (Defensa TFG)

### Added
- README exhaustivo con instalación local, Docker, despliegue Render, variables de entorno, estructura del repo, capturas y roadmap.
- Diagrama Entidad-Relación en Mermaid + DBML para dbdiagram.io.
- Diagrama de clases UML (entidades, servicios, seguridad).
- Diagrama de arquitectura por capas y secuencia de pago.
- Cronograma Gantt con sprints y hitos.
- Schema SQL exportado, vistas (`vw_reservas_resumen`, `vw_estadisticas_centro`, `vw_actividad_usuario`, `vw_inmersiones_disponibles`, `vw_notificaciones_no_leidas`).
- Procedimientos almacenados (`sp_marcar_reserva_pagada`, `sp_purgar_historias_expiradas`, `sp_estadisticas_globales`) y creación idempotente de índices.
- 31 tests JUnit unitarios sobre `ReservaService`, `PayPalService`, `StripeService`, `UploadController` y `EmojiStripMigration`.
- Swagger UI en `/swagger-ui.html` con OpenAPI 3.0 vía springdoc.
- `Dockerfile` multi-stage (build + runtime alpine), `docker-compose.yml` con MySQL 8 y `render.yaml` para deploy en Render.com.
- GitHub Actions de CI: build + tests con MySQL service + build de imagen Docker en master.
- Wireframes SVG de las 5 pantallas principales (login, feed, inmersiones, pago, mapa).
- Guía de estilo (paleta, tipografías, espaciados, breakpoints, accesibilidad).
- Capítulos de memoria nuevos: Digitalización, Sostenibilidad y ODS, Sistemas Informáticos.
- Plan de pruebas documentado en `docs/test-plan.md`.
- Script Python ETL para análisis de datos en `scripts/analytics/`.
- Meta tags SEO (description, og:type/title/image, twitter:card, theme-color, lang) en todas las páginas públicas.
- `INSTALL.md`, `LICENSE` MIT, `.env.example`, `.dockerignore`.
- `ISSUES.md` con backlog inicial para GitHub Issues.

### Changed
- Reorganización del repositorio: `/database`, `/docs/diagrams`, `/docs/wireframes`, `/docs/screenshots`, `/scripts/analytics`, `/.github/workflows`.
- Ampliación del PDF de memoria técnica con apéndices de diagramas y normalización 1FN/2FN/3FN/BCNF.

### Fixed
- (No nuevos bugs en esta release; ver [0.9.0] para los críticos resueltos.)

---

## [0.9.0] – 2026-05-04 (Pulido visual y subida desde galería)

### Added
- `UploadController` con multipart, validación de MIME y extensión, UUID en filename para evitar colisiones y path traversal.
- Servir `/uploads/**` como recurso estático (`WebConfig.addResourceHandlers`).
- File picker en publicaciones, historias y avatar con `accept="image/*,video/*"` y `capture="environment"`/`"user"`.
- `ocean-theme.css` overlay: glassmorphism, fondo subacuático animado, light rays, dock con halo, hover lift.
- `ocean-effects.js`: burbujas ascendentes (CSS keyframes), reveal-on-scroll con IntersectionObserver, tilt 3D suave en cards.
- Responsive móvil: bottom sheets, viewport-fit=cover, safe-area-inset, inputs 44px y font-size 16px.
- `EmojiStripMigration`: pasada idempotente que limpia emojis de los registros existentes en BD tras la decisión visual de quitarlos.

### Changed
- Limpieza completa de emojis en HTML, JS y datos seed. Sustitución por iconos SVG inline en chips, botones y modal de pago.
- `paymentStatus` consistente en seed (PAID para CONFIRMADA/COMPLETADA).
- Estilo del modal de pago: badge de modo (TFG/Sandbox/Live) más visible.

### Fixed
- **Race en `Inmersiones.confirmarReserva`**: `cerrarModal()` ponía `inmersionActual = null` antes de leer `.precio`. Capturado precio/título/id antes de cerrar.
- **Notificación de reserva confirmada en Stripe demo**: el flujo demo no notificaba al usuario ni al centro. Ahora `PaymentController.verificar` dispara las dos notificaciones en cualquier camino exitoso.
- **`paymentStatus = null` ocultaba botón "Pagar"**: tratado como UNPAID para compatibilidad con datos seed.

---

## [0.8.0] – 2026-04-29 (Pasarela de pago real)

### Added
- `PayPalService` con cliente HTTP `java.net.http.HttpClient` (sin SDK). Soporta sandbox y live.
- `PayPalController`: `/create-order/{reservaId}` y `/capture-order/{reservaId}`.
- `StripeService` con SDK oficial.
- `PaymentController`: `/checkout/{reservaId}`, `/verify/{reservaId}`, `/config`.
- Modal de pago multi-pasarela en `payment.js` con tres modos: Stripe, PayPal y Demo TFG. Detección automática del modo según configuración del backend.
- Idempotencia en `/verify`: si ya está pagada, no duplica notificaciones.
- Notificaciones automáticas tras pago confirmado: usuario + centro.

### Changed
- `Reserva` enriquecida con `paymentStatus`, `stripeSessionId`, `paypalOrderId`, `paypalCaptureId`.

---

## [0.7.0] – 2026-04-15 (Búsqueda universal y notificaciones)

### Added
- Endpoint `/api/search` con búsqueda por usuarios, centros e inmersiones.
- Fallback de proximidad (Haversine) cuando no hay coincidencias textuales y hay coordenadas del usuario.
- `NotificacionService` y página dedicada con badge en topbar (poll 30s).
- `SeguimientoService` con máquina de estados solicitar/aceptar/rechazar/dejar.
- Solicitudes de seguimiento con notificación accionable.

### Fixed
- **Lombok @Data + Set<Usuario> rompe equals/hashCode**: bypass con SQL nativo en `UsuarioRepository.existsSeguimiento`, `addSeguidor`, `removeSeguidor`.

---

## [0.6.0] – 2026-03-15 (Mapa y red social)

### Added
- Mapa interactivo con Leaflet 1.9 y tiles OpenStreetMap.
- `PuntoMapaController` y `FotoPuntoMapa` para puntos georreferenciados.
- Tiempo atmosférico vía OpenWeatherMap con fallback mock.
- Publicaciones con datos técnicos (profundidad, temperatura, visibilidad, especies).
- Comentarios y likes con notificaciones.
- Historias de 24h (efímeras).

---

## [0.5.0] – 2026-02-01 (Marketplace de centros)

### Added
- `CentroBuceo`, `Inmersion`, `Reserva` con flujo completo CRUD.
- Filtros del catálogo: nivel, precio, profundidad.
- Descuento automático de plazas al reservar y devolución al cancelar.

---

## [0.1.0] – 2025-10-23 (MVP backend)

### Added
- Spring Boot 3.2 + JPA + MySQL.
- Auth con JWT (jjwt 0.11) y BCrypt.
- Tres tipos de usuario: COMUN, EMPRESA, ADMINISTRADOR.
- DataInitializer con datos de demostración.
- Login social con Google OAuth2 (opcional, sólo si `GOOGLE_OAUTH_ENABLED=true`).
