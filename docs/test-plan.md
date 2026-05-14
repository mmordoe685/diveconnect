# Plan de pruebas - DiveConnect

## Objetivo

Validar que los flujos criticos del MVP funcionan antes de la defensa: autenticacion, reservas, pagos, subida de archivos, mapa, notificaciones y paneles.

## Pruebas automatizadas

Comando:

```bash
mvn test
```

Resultado de referencia en esta revision:

| Metrica | Valor |
|---|---:|
| Tests ejecutados | 31 |
| Fallos | 0 |
| Errores | 0 |
| Omitidos | 0 |

Suites:

| Suite | Cobertura |
|---|---|
| `ReservaServiceTest` | Creacion de reservas, plazas, estados y cancelacion |
| `StripeServiceTest` | Configuracion, modo deshabilitado y sesiones |
| `PayPalServiceTest` | Configuracion, errores y respuestas externas |
| `UploadControllerTest` | Tipos permitidos, extensiones, nombres seguros y URLs |
| `EmojiStripMigrationTest` | Limpieza de caracteres no compatibles con MySQL |

## Pruebas manuales de aceptacion

| Flujo | Pasos | Resultado esperado |
|---|---|---|
| Login usuario | Entrar como `sofia_buceo` | Redireccion a feed y JWT guardado |
| Feed | Crear publicacion y comentar | Publicacion visible, contador actualizado |
| Historias | Crear historia | Historia agrupada por usuario y expira en 24h |
| Busqueda | Buscar por texto y filtros | Resultados de usuarios, empresas e inmersiones |
| Mapa | Abrir `/pages/mapa.html` | Puntos visibles y clima si hay API key |
| Reserva | Reservar inmersion con plazas | Reserva pendiente y plazas descontadas |
| Pago demo | Confirmar pago sin credenciales | Reserva `PAID` y `CONFIRMADA` |
| Empresa | Login `oceandive` | Panel empresa y reservas recibidas |
| Admin | Login `admin` | Dashboard con usuarios, centros, inmersiones y reservas |
| Upload | Subir imagen PNG/JPG | URL `/uploads/...` y archivo guardado |
| Accesibilidad | Tabular por topbar/dock | Foco visible y skip-link funcional |

## Pruebas de API

Endpoints publicos:

- `GET /api/inmersiones/disponibles`
- `GET /api/centros-buceo`
- `GET /api/search`
- `GET /api/weather`
- `GET /api/paypal/config`
- `GET /api/payments/config`
- `GET /actuator/health`

Endpoints autenticados:

- `GET /api/usuarios/perfil`
- `POST /api/reservas`
- `POST /api/uploads`
- `GET /api/notificaciones`
- `POST /api/payments/verify/{reservaId}`

## Pruebas de despliegue

| Entorno | Comando | Validacion |
|---|---|---|
| Maven local | `mvn spring-boot:run` | `http://localhost:8080` |
| Docker Compose | `docker compose up --build` | App en 8080, MySQL en 3307 |
| Docker imagen | `docker build -t diveconnect .` | Build multi-stage correcto |
| Render | Blueprint `render.yaml` | `/actuator/health` devuelve UP |

## Riesgos y mitigacion

| Riesgo | Mitigacion |
|---|---|
| Credenciales externas no configuradas | Modo demo de pagos y placeholders seguros |
| MySQL no disponible | Docker Compose levanta una instancia local |
| Uploads no persistentes en produccion | Volumen `/app/uploads` o storage externo |
| Fallo de red en la demo | Probar antes con datos seed y modo demo |
