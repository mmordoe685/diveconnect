# Anexos de memoria

## A. Trazabilidad rapida de requisitos

| Requisito | Implementacion |
|---|---|
| Usuarios por rol | `TipoUsuario`, `SecurityConfig`, paneles por rol |
| Publicaciones sociales | `PublicacionController`, `PublicacionService`, `publicaciones.js` |
| Historias 24h | `HistoriaService`, limpieza programada con `@Scheduled` |
| Reservas | `ReservaService`, `ReservaController` |
| Pagos | `PaymentController`, `PayPalController`, `StripeService`, `PayPalService` |
| Mapa | `PuntoMapaController`, Leaflet en `mapa.html` |
| Meteorologia | `WeatherService`, OpenWeatherMap |
| Administracion | `AdminController`, paginas `/pages/admin/` |

## B. Escalabilidad

El MVP funciona como monolito Spring Boot porque reduce complejidad de despliegue y facilita la defensa. Si creciera, la evolucion natural seria:

- Servir estaticos desde CDN.
- Mover uploads a almacenamiento tipo S3.
- Usar MySQL gestionado con backups y replicas.
- Separar jobs programados de la API principal.
- Anadir observabilidad: logs estructurados, metricas y alertas.

## C. Seguridad

Medidas actuales:

- BCrypt para contrasenas.
- JWT firmado con secreto externo.
- Usuario no-root en Docker.
- CORS configurable por entorno.
- Variables sensibles fuera del repositorio.
- Mensajes de error genericos para excepciones no controladas.

Medidas futuras:

- Rotacion de secretos.
- Rate limiting en login y upload.
- Antivirus o escaneo de archivos subidos.
- Cabeceras CSP estrictas.

## D. Sostenibilidad

La aplicacion reduce trabajo manual en reservas y centraliza informacion tecnica. A nivel software se han aplicado indices, paginacion, limpieza de historias expiradas, logs controlados y Docker multi-stage.
