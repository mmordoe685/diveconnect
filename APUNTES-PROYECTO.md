# Apuntes integrales de DiveConnect

Documento de referencia técnica del proyecto. Pensado para que repases todo el código antes de la defensa y para que respondas con seguridad cualquier pregunta del tribunal sobre cualquier parte del proyecto.

Está dividido en cuatro grandes secciones:

1. **¿Qué es DiveConnect?** — visión general, decisiones de diseño, problemas reales que aparecieron y cómo se resolvieron.
2. **Código por carpetas** — explicación funcional, archivo por archivo, siguiendo la estructura del proyecto.
3. **Código por funcionalidad** — qué archivos participan en cada flujo (login, reserva, pago, etc.) para responder rápido si te preguntan por una zona concreta de la web.
4. **Guion para el tribunal** — qué decir, en qué orden y qué enseñar en pantalla.

---

# 1. ¿Qué es DiveConnect?

## 1.1. Resumen del producto

DiveConnect es una aplicación web full-stack que combina dos productos en uno:

- **Red social vertical para buceadores**: feed cronológico, historias 24h, sistema de seguimiento, notificaciones, mapa interactivo con puntos georreferenciados, búsqueda universal con proximidad.
- **Marketplace para centros de buceo**: catálogo filtrable de inmersiones, reservas con descuento automático de plazas, pasarela de pago real (Stripe + PayPal + modo demo TFG).

Tres roles de usuario:

- **USUARIO_COMUN** (buceador): publica, sigue, reserva, paga.
- **USUARIO_EMPRESA** (centro): gestiona su catálogo y sus reservas.
- **ADMINISTRADOR**: modera contenido, gestiona usuarios.

## 1.2. Cómo se ha hecho

Arquitectura clásica de tres capas dentro de un único proceso Spring Boot:

```
Cliente HTML/JS  ──fetch + JWT──▶  Controllers REST
                                        │
                                        ▼
                                    Services (lógica + transacciones)
                                        │
                                        ▼
                                    Repositories JPA
                                        │
                                        ▼
                                    MySQL 8 InnoDB
```

Stack:

- **Backend**: Java 17 + Spring Boot 3.2.3 + Spring Security 6 + Spring Data JPA + Hibernate.
- **BD**: MySQL 8 con `ddl-auto=update`, 15 tablas, 5 vistas, 3 procedimientos, índices secundarios.
- **Auth**: JWT HS256 (jjwt) + BCrypt + OAuth2 Google opcional.
- **Pasarela**: SDK oficial de Stripe + cliente HTTP propio para PayPal v2 (sin SDK).
- **Frontend**: HTML5 + CSS3 + JavaScript ES2020 sin framework. Leaflet para el mapa.
- **Análisis**: Python 3.10+ con pandas + matplotlib + PyMySQL.
- **Infra**: Docker multi-stage + docker-compose + render.yaml para Render.com + GitHub Actions CI.

## 1.3. Problemas reales que aparecieron y cómo se resolvieron

Estos son los bugs reales del desarrollo. Saberlos es importante porque son las preguntas más probables del tribunal.

### Problema 1 — Lombok @Data + Set<Entity> rompe equals/hashCode

**Síntoma**: tras aceptar una solicitud de seguimiento, el endpoint `/api/seguimiento/estado/{id}` seguía devolviendo `NO_SIGUE` aunque la fila estaba en BD. `Set.contains(otroUsuario)` daba comportamiento no determinista, y a veces caía en `StackOverflowError`.

**Causa**: Lombok `@Data` genera `equals/hashCode` cubriendo TODOS los campos de la clase, incluidas las colecciones `seguidores` y `siguiendo`. Como `Set.contains()` usa `hashCode()`, y el `hashCode` necesita el hash de las colecciones, y las colecciones contienen la propia entidad… recursión.

**Solución**: bypass del `Set` para las consultas críticas. En `UsuarioRepository`:

```java
@Query(value = "INSERT IGNORE INTO seguidores (seguidor_id, seguido_id) VALUES (:s, :d)", nativeQuery = true)
void addSeguidor(@Param("s") Long s, @Param("d") Long d);

@Query(value = "DELETE FROM seguidores WHERE seguidor_id = :s AND seguido_id = :d", nativeQuery = true)
void removeSeguidor(@Param("s") Long s, @Param("d") Long d);

default boolean existsSeguimiento(Long s, Long d) { return countSeguimiento(s, d) > 0; }
```

Las consultas atacan la tabla puente directamente con SQL nativo, sin pasar por Hibernate. `SeguimientoService` usa estos métodos en lugar de `usuario.getSiguiendo().contains(...)`.

### Problema 2 — Race condition en confirmar reserva

**Síntoma**: al pulsar "Confirmar reserva" en `Inmersiones.html`, error en consola "Cannot read properties of null (reading 'precio')" y el modal de pago no abría.

**Causa**: la función JS llamaba a `cerrarModal()` (que pone `inmersionActual = null`) antes de leer `inmersionActual.precio` para pasarlo al modal de pago.

**Solución**: capturar precio, título e id en variables locales **antes** de cerrar el modal. Cambio en `Inmersiones.html`:

```javascript
const precioInmersion = inmersionActual.precio || 0;
const tituloInmersion = inmersionActual.titulo || 'Inmersión DiveConnect';
const idInmersion     = inmersionActual.id;
cerrarModal();  // ahora sí, sin depender del estado
```

### Problema 3 — Notificación de reserva confirmada faltaba en flujo Stripe demo

**Síntoma**: al pagar en modo demo, la reserva pasaba a CONFIRMADA pero no llegaba notificación al usuario ni al centro.

**Causa**: `PaymentController.verificar()` solo notificaba en el camino feliz de Stripe real. El camino demo y el "ya pagada" no disparaban `NotificacionService.crear`.

**Solución**: refactor de `PaymentController` con un método privado `confirmarPago(reserva, paymentRef, pasarela)` que se llama desde los tres caminos, y que dispara siempre las dos notificaciones (al usuario y al centro). También se añadió idempotencia: si ya está pagada, devuelve `{status: PAID, alreadyPaid: true}` sin notificar otra vez.

### Problema 4 — paymentStatus null ocultaba el botón "Pagar"

**Síntoma**: las reservas creadas por `DataInitializer` (datos seed) no mostraban el botón "Pagar ahora" en `reservas.html`.

**Causa**: la condición JS era `r.paymentStatus === 'UNPAID'`, pero el seed dejaba `paymentStatus = null`.

**Solución**: dos arreglos:
- Frontend: tratar `null` como UNPAID (red de seguridad para datos legacy).
- Backend: `DataInitializer` ahora establece `paymentStatus` consistente: `PAID` para CONFIRMADA/COMPLETADA, `UNPAID` para PENDIENTE/CANCELADA.

### Problema 5 — Limpieza de emojis en datos persistidos

**Síntoma**: tras decidir quitar emojis del UI (porque dependen de fuentes del SO y rompen el aspecto profesional), las publicaciones, biografías, comentarios e historias del seed seguían teniéndolos en BD.

**Solución**: clase `EmojiStripMigration` que extiende `CommandLineRunner` con `@Order(100)`. Se ejecuta tras `DataInitializer` y aplica un regex Unicode (rangos U+1F300–1FAFF, U+2600–26FF, U+2700–27BF, U+1F000–1F2FF, U+FE0F) a 6 columnas de 4 tablas. Es **idempotente**: la segunda ejecución no encuentra nada que cambiar.

Cubierta con 8 tests unitarios.

---

# 2. Código por carpetas

Sigue la estructura física del proyecto. Para cada archivo: qué hace + bloques significativos + decisiones de diseño relevantes.

## 2.1. Raíz del proyecto

| Archivo | Para qué |
|---|---|
| `pom.xml` | Maven build. Spring Boot 3.2.3, dependencias clave: Web, Security, Data JPA, Validation, Lombok, jjwt 0.11, Stripe SDK 24, springdoc-openapi 2.3, OAuth2 Client, MySQL connector. |
| `mvnw` / `mvnw.cmd` | Maven wrapper. Permite ejecutar `mvnw clean install` sin tener Maven instalado globalmente. |
| `Dockerfile` | Multi-stage. Stage 1: Maven JDK 17 compila el JAR. Stage 2: JRE Alpine ejecuta el JAR. Usuario no-root, healthcheck contra `/api/paypal/config`, JAVA_OPTS para limitar heap a 450 MB. |
| `docker-compose.yml` | Orquesta MySQL 8 + app. Volúmenes para BD persistente y uploads. Healthcheck en MySQL para que la app no arranque antes de tiempo. |
| `render.yaml` | Blueprint de Render.com. Define el web service + la BD MySQL gestionada. Render genera JWT_SECRET aleatorio. Variables sensibles marcadas con `sync: false`. |
| `.dockerignore` | Lista de archivos que NO entran en la imagen Docker (target/, uploads/, .git/, docs/). |
| `.env.example` | Plantilla de variables de entorno. Se copia a `.env` (que está en gitignore). |
| `.github/workflows/ci.yml` | Pipeline GitHub Actions: build con JDK 17 + tests con MySQL service container + build de imagen Docker en master. |

## 2.2. `src/main/java/com/diveconnect/` (95 archivos Java)

### `DiveconnectApplication.java`

Entry point estándar de Spring Boot:

```java
@SpringBootApplication
public class DiveconnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiveconnectApplication.class, args);
    }
}
```

`@SpringBootApplication` combina `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`. Spring escanea desde el paquete base `com.diveconnect`.

### `config/` (6 archivos)

#### `CorsConfig.java`
Bean de `CorsConfigurationSource` que permite cualquier origen en desarrollo. En producción real se restringiría al dominio del frontend.

#### `DataInitializer.java`
Clase `@Component` que implementa `CommandLineRunner`. Se ejecuta al arrancar y crea los usuarios seed, centros, inmersiones, publicaciones, reservas, historias, notificaciones, puntos de mapa.

Lógica clave:
- Comprueba si los usuarios `admin`, `buceador`, `oceandive` existen.
- Si existen pero faltan otros datos (reservas, puntos, etc.), borra todo y recrea.
- Si todo existe, sale sin tocar nada (idempotente).

Genera 11 usuarios, 3 centros, 12 inmersiones, ~14 publicaciones, ~27 reservas, varias historias y notificaciones.

#### `EmojiStripMigration.java`
`CommandLineRunner` con `@Order(100)`. Limpia emojis de 6 columnas de 4 tablas tras el arranque. Patrón regex Unicode + helper estático `stripEmojis(text)` que también compacta dobles espacios y limpia trailing whitespace.

#### `OpenApiConfig.java`
Configuración de Swagger / OpenAPI. Define info del API, contacto, license, servers y un esquema de seguridad Bearer JWT.

Disponible en runtime en:
- `/swagger-ui.html` — UI interactiva.
- `/v3/api-docs` — JSON OpenAPI 3.0.

Para autenticarse en Swagger: pulsar "Authorize" → pegar `Bearer <jwt>` → ya puedes ejecutar endpoints protegidos.

#### `SecurityConfig.java`
Cadena de filtros stateless. Reglas:

- CSRF desactivado (es API REST con JWT, no formularios).
- Sesiones en `STATELESS`.
- Recursos estáticos públicos (`/`, `/css/**`, `/js/**`, `/pages/**`, `/images/**`, `/uploads/**`).
- Swagger público (`/swagger-ui/**`, `/v3/api-docs/**`).
- `/api/auth/**`, `/oauth2/**`, `/login/oauth2/**` públicos.
- `/api/admin/**` solo `ADMINISTRADOR`.
- Catálogo público (GET inmersiones, centros, mapa, weather, paypal/config, payments/config, search).
- POST `/api/uploads` autenticado.
- Resto: autenticado.

OAuth2 Login solo si `app.google-oauth-enabled=true`.

#### `WebConfig.java`
- ViewController: `/` redirige a `/index.html`.
- ResourceHandler: sirve `/uploads/**` desde el directorio `file.upload-dir` con caché de 1 hora.

### `controller/` (17 archivos)

| Controller | Endpoints clave |
|---|---|
| `AdminController` | `/api/admin/usuarios` GET/POST/DELETE, gestión de roles. Solo ADMINISTRADOR. |
| `AuthController` | `/api/auth/login`, `/api/auth/registro`, `/api/auth/config`. Públicos. |
| `CentroBuceoController` | `/api/centros-buceo` GET listar, `/{id}` GET detalle, POST/PUT/DELETE para empresa propietaria. |
| `ComentarioController` | `/api/publicaciones/{id}/comentarios` GET y POST. |
| `HistoriaController` | `/api/historias/feed` lista activas (NOW < expira_en), POST crear, DELETE. |
| `InmersionController` | `/api/inmersiones/disponibles` (público), `/{id}`, `/mis-inmersiones` (empresa), CRUD para empresa propietaria. |
| `NotificacionController` | `/api/notificaciones`, `/no-leidas`, `/{id}/leer`, `/leer-todas`. |
| `PayPalController` | `/api/paypal/config`, `/create-order/{reservaId}`, `/capture-order/{reservaId}?orderId=`. |
| `PaymentController` | `/api/payments/config`, `/checkout/{reservaId}` (Stripe), `/verify/{reservaId}` (verificación + idempotencia). |
| `PublicacionController` | Feed paginado, like, unlike, eliminar, crear. |
| `PuntoMapaController` | CRUD de puntos georreferenciados. GET público, POST/DELETE autenticado. |
| `ReservaController` | POST crear, GET mis-reservas / centro, DELETE cancelar (libera plazas). |
| `SearchController` | `/api/search?q=&tipo=&lat=&lon=&profMin=...`. Búsqueda universal con fallback de proximidad. |
| `SeguimientoController` | `/solicitar/{id}`, `/aceptar/{id}`, `/rechazar/{id}`, `/dejar/{id}`, `/estado/{id}`. |
| `UploadController` | POST `/api/uploads` multipart. Valida MIME + extensión + UUID nombre. |
| `UsuarioController` | `/perfil` GET/PUT, `/{id}` GET, `/buscar`, `/empresas`. |
| `WeatherController` | `/api/weather?lat=&lon=`. Proxy a OpenWeatherMap con fallback mock si no hay API key. |

### `dto/request/` (12 archivos) y `dto/response/` (10 archivos)

Cada DTO es un POJO con `@Data` de Lombok (getters/setters automáticos). Se usan para:

- **Request**: lo que llega del cliente. Anotaciones `@NotNull`, `@Email`, `@Min` para validación con `jakarta.validation`. Ejemplos: `LoginRequest`, `RegistroRequest`, `ReservaRequest`, `ActualizarPerfilRequest`, `CrearInmersionRequest`, `PublicacionRequest`, `ComentarioRequest`, `CrearCentroRequest`, `CrearPuntoMapaRequest`.

- **Response**: lo que devuelve el servidor. Sin información sensible (jamás incluyen contraseña). Ejemplos: `LoginResponse` (token + tipo + usuario), `UsuarioResponse`, `InmersionResponse`, `ReservaResponse`, `PublicacionResponse`, `ComentarioResponse`, `CentroBuceoResponse`, `HistoriaResponse`, `NotificacionResponse`, `PuntoMapaResponse`.

### `entity/` (15 archivos: 11 entidades + 4 enums)

**Entidades** anotadas con `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@OneToMany`, `@ManyToMany` según corresponda. Todas usan `@Data` de Lombok salvo en relaciones donde se sobreescribe `equals/hashCode` (caso documentado).

| Entidad | Campos relevantes |
|---|---|
| `Usuario` | id, username UK, email UK, password (BCrypt), tipoUsuario (enum), datos de empresa nullable, biografia, fotoPerfil, nivelCertificacion, numeroInmersiones, activo, fechaRegistro. ManyToMany autorreferencial para seguidores/siguiendo. |
| `CentroBuceo` | id, OneToOne con Usuario propietario (UK), nombre, direccion, ciudad, telefono, certificaciones, imagenUrl, valoracionPromedio. |
| `Inmersion` | id, FK a CentroBuceo, titulo, fechaInmersion, profundidadMaxima, nivelRequerido, precio, plazasTotales, plazasDisponibles, latitud, longitud, equipoIncluido, imagenUrl, activa. |
| `Reserva` | id, FK Usuario + Inmersion + CentroBuceo, numeroPersonas, precioTotal, estado (enum PENDIENTE/CONFIRMADA/CANCELADA/COMPLETADA), paymentStatus (UNPAID/PAID/FAILED), stripeSessionId, stripePaymentIntentId, paypalOrderId, paypalCaptureId, observaciones, fechaReserva, ultimaModificacion. |
| `Publicacion` | id, FK Usuario, contenido, imagenUrl, videoUrl, datos técnicos (lugar, profundidad, temperatura, visibilidad, especies), fechaPublicacion. ManyToMany para likes. |
| `Comentario` | id, FK Publicacion + Usuario, contenido, fechaComentario. |
| `Historia` | id, FK Usuario, mediaUrl, mediaType (FOTO/VIDEO), texto, fechaPublicacion, expiraEn (NOW + 24h). |
| `Notificacion` | id, FK destinatario + emisor (nullable), tipo (enum), mensaje, leida, accionable, resuelta, entidadRelacionadaId, fechaCreacion. |
| `SolicitudSeguimiento` | id, FK solicitante + destinatario, estado (PENDIENTE/ACEPTADA/RECHAZADA), fechaCreacion, fechaRespuesta. |
| `PuntoMapa` | id, FK autor, latitud, longitud, titulo, profundidadMetros, temperaturaAgua, presionBar, visibilidadMetros, corriente, especiesVistas, fechaObservacion. OneToMany con FotoPuntoMapa. |
| `FotoPuntoMapa` | id, FK PuntoMapa, url, especieAvistada. |

**Enums**:

- `TipoUsuario`: USUARIO_COMUN, USUARIO_EMPRESA, ADMINISTRADOR.
- `EstadoReserva`: PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA.
- `EstadoSolicitud`: PENDIENTE, ACEPTADA, RECHAZADA.
- `TipoNotificacion`: SOLICITUD_SEGUIMIENTO, SEGUIMIENTO_ACEPTADO, SEGUIMIENTO_RECHAZADO, NUEVO_SEGUIDOR, LIKE_PUBLICACION, COMENTARIO_PUBLICACION, RESERVA_CONFIRMADA, RESERVA_RECIBIDA, MENCION.

### `exception/` (4 archivos)

- `BadRequestException` extends `RuntimeException` → mapeada a HTTP 400.
- `ResourceNotFoundException` → HTTP 404.
- `UnauthorizedException` → HTTP 401.
- `GlobalExceptionHandler` (`@RestControllerAdvice`): traduce las excepciones a JSON `{status, message, timestamp}`.

### `repository/` (11 archivos)

Todos extienden `JpaRepository<Entity, Long>`. La mayoría usan métodos derivados (`findByUsername`, `existsByEmail`). Notables:

- **`UsuarioRepository`**: además del CRUD estándar, tiene los métodos nativos `addSeguidor`, `removeSeguidor`, `countSeguimiento` y el helper `existsSeguimiento`. Necesarios por el bug de Lombok @Data + Set.
- **`InmersionRepository`**: tiene `findCercanas(lat, lon, limite)` con fórmula Haversine en SQL nativo, devolviendo una projection `InmersionConDistancia`. Usado para el fallback de proximidad en la búsqueda.
- **`PublicacionRepository`**: feed paginado con `Pageable`. Like / unlike también con SQL nativo (mismo bug @Data + Set).
- **`HistoriaRepository`**: `findActivas` filtra `expira_en > NOW()`.
- **`NotificacionRepository`**: `findByDestinatarioOrderByFechaCreacionDesc`, `countByDestinatarioAndLeidaFalse`.
- **`ReservaRepository`**: por usuario, por centro, ordenadas por fecha desc.
- **`SolicitudSeguimientoRepository`**: `findPendienteEntre(solicitante, destinatario)`, listado por destinatario.

### `security/` (4 archivos)

- **`JwtUtil`**: genera y valida tokens. Algoritmo HS256, secret de 64+ caracteres (env `JWT_SECRET`), expiración 24 h. Métodos: `generateToken(Authentication)`, `extractUsername(token)`, `validateToken(token)`.
- **`JwtAuthenticationFilter`**: extiende `OncePerRequestFilter`. Lee el header `Authorization: Bearer <jwt>`, valida, monta el `Authentication` en el `SecurityContext`. Se inserta antes de `UsernamePasswordAuthenticationFilter`.
- **`UserDetailsServiceImpl`**: implementa `UserDetailsService` de Spring Security. Carga el usuario por username y devuelve un `CustomUserDetails`. La authority se asigna según `tipoUsuario` (`ROLE_USUARIO_COMUN`, `ROLE_USUARIO_EMPRESA`, `ROLE_ADMINISTRADOR`).
- **`GoogleOAuth2SuccessHandler`**: tras un login OAuth2 exitoso, busca el email en BD. Si existe el usuario, emite JWT. Si no, lo crea con tipo `USUARIO_COMUN`. Redirige a `/pages/oauth-callback.html?token=...&user=...`.

### `service/` (15 archivos)

Capa de lógica de negocio. Todos con `@Service` + `@RequiredArgsConstructor`. Las escrituras en `@Transactional`, las lecturas en `@Transactional(readOnly = true)`.

**Servicios principales**:

- **`UsuarioService`**: registro, perfil, edición, búsqueda, listado de empresas, follower/following.
- **`CentroBuceoService`**: CRUD para empresa propietaria, listado público.
- **`InmersionService`**: CRUD con verificación de propiedad, listado de disponibles, búsqueda cercana.
- **`ReservaService`**: crear (descuenta plazas), marcar como pagada, cancelar (devuelve plazas). Todo en `@Transactional` para garantizar consistencia.
- **`PublicacionService`**: feed paginado, crear, eliminar, like/unlike (con notificación al autor).
- **`ComentarioService`**: crear comentario con notificación al autor de la publicación.
- **`HistoriaService`**: crear (calcula `expira_en = ahora + 24h`), listar activas.
- **`NotificacionService`**: punto único para crear notificaciones. `crear(destinatario, emisor, tipo, entidadId, mensaje, accionable)`.
- **`SeguimientoService`**: máquina de estados completa. Solicitar (crea SolicitudSeguimiento + notificación). Aceptar (cambia estado + inserta en seguidores + notificación). Rechazar. Dejar de seguir. Estado entre dos usuarios.
- **`SearchService`**: orquesta la búsqueda universal. Combina resultados de usuarios, centros e inmersiones. Si no hay coincidencias y hay coordenadas, llama a `InmersionRepository.findCercanas`.

**Servicios de pasarela**:

- **`PayPalService`**: cliente HTTP propio con `java.net.http.HttpClient`. Métodos: `isConfigured()`, `fetchAccessToken()` (OAuth2 client_credentials), `createOrder(reserva)`, `captureOrder(orderId)`. Lanza `PayPalException` en errores. Soporta sandbox y live según `PAYPAL_MODE`.
- **`StripeService`**: usa el SDK oficial. `isEnabled()`, `createCheckoutSession(reserva)` (devuelve `Session` con URL de redirección), `retrieveSession(id)`. Currency, frontend URL inyectados desde props.

**Otros**:

- **`UsuarioMapper`**: convierte `Usuario` → `UsuarioResponse` ocultando información sensible.
- **`WeatherService`**: cliente WebFlux a OpenWeatherMap. Si falla o no hay API key, devuelve datos mock con flag `source=mock`.

## 2.3. `src/main/resources/`

### `application.properties`

Configuración Spring Boot. Variables clave (todas con default y override por env):

```properties
server.port=${PORT:8080}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/diveconnect_db}
spring.datasource.username=${DB_USERNAME:diveconnect_user}
spring.datasource.password=${DB_PASSWORD:DiveConnect2025!}
spring.jpa.hibernate.ddl-auto=update
jwt.secret=${JWT_SECRET:DiveConnect2025...}
jwt.expiration=86400000

stripe.secret-key=${STRIPE_SECRET_KEY:}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY:}
paypal.client-id=${PAYPAL_CLIENT_ID:}
paypal.client-secret=${PAYPAL_CLIENT_SECRET:}
paypal.mode=${PAYPAL_MODE:sandbox}
openweather.api-key=${OPENWEATHER_API_KEY:}

file.upload-dir=${UPLOAD_DIR:uploads}
spring.servlet.multipart.max-file-size=10MB
app.frontend-url=${FRONTEND_URL:http://localhost:8080}
app.google-oauth-enabled=${GOOGLE_OAUTH_ENABLED:false}
```

### `static/` (frontend completo)

#### `static/index.html`
Landing pública. Hero, ventajas, testimonios, links a login/register. Sin auth.

#### `static/pages/` (21 HTML)

**Páginas públicas**: `login.html`, `register.html`, `oauth-callback.html`, `centros.html`.

**Páginas usuario**: `feed.html`, `Inmersiones.html`, `reservas.html`, `Perfil.html`, `buscar.html`, `mapa.html`, `notificaciones.html`.

**Páginas empresa** (`pages/empresa/`): `dashboard.html`, `mi-centro.html`, `mis-inmersiones.html`, `gestionar-reservas.html`.

**Páginas admin** (`pages/admin/`): `dashboard.html`, `usuarios.html`, `centros.html`, `inmersiones.html`, `reservas.html`.

Cada `.html`:
- `<head>`: charset UTF-8, viewport con `viewport-fit=cover`, meta description + OG tags + theme-color, lang="es", title específico.
- Carga `style.css` y `ocean-theme.css`.
- Bloque `<style>` propio para layout específico.
- `<body>` con `<header class="topbar" id="mainNavbar">` que se renderiza con `nav.js`.
- Scripts: `api.js`, `auth.js`, `nav.js`, módulos específicos, `ocean-effects.js` con `defer`.

#### `static/css/`

- **`style.css`**: design system base. Tokens de color (cream, navy, seafoam, coral, gold), radios, sombras, easings. Reset, layout de páginas, componentes (botones, cards, modales, feed, modal de pago).
- **`ocean-theme.css`**: capa overlay. Glassmorphism en cards, fondo subacuático animado (gradient + caustics + light rays), dock con halo, hover lift, gradient text en headings, micro-animaciones. Respeta `prefers-reduced-motion`. Bottom sheets en móvil con `@media (max-width: 720px)`.

#### `static/js/`

- **`api.js`**: `fetchAPI(endpoint, options)` añade JWT, gestiona 401/403 redirigiendo a login, parsea JSON. Helpers globales: `showAlert(msg, type)`, `escapeHtml(text)`, `formatFecha(date)`.
- **`auth.js`**: `login(email, password)`, `registrarUsuario(data)`, `logout()`, `getCurrentUser()`, `requireAuth()`, `redirectIfAuthenticated()`. Almacena token y usuario en `localStorage`.
- **`nav.js`**: `initNav()` pinta la topbar y el dock inferior según el rol del usuario. Refresca el badge de notificaciones cada 30 s con poll.
- **`publicaciones.js`**: `createPublicacion`, `likePublicacion`, `unlikePublicacion`, `getComentarios`, `createComentario`. Render de publicaciones modo IG.
- **`payment.js`**: modal de pago multi-pasarela. Detecta automáticamente el modo (Stripe / PayPal / demo TFG). En modo Stripe redirige a Checkout. En modo demo valida con Luhn local y llama a `/api/payments/verify/{id}`. Idempotencia, reintento, badge de modo. Reservas con total 0 € omiten el modal.
- **`ocean-effects.js`**: efectos visuales del tema. Inyecta burbujas con CSS keyframes. IntersectionObserver para reveal-on-scroll. Tilt 3D suave en cards (mousemove). Skip si `prefers-reduced-motion`.
- **`animations.js`**: animaciones de entrada en scroll para landing y secciones internas.

## 2.4. `src/test/java/com/diveconnect/`

5 clases de tests unitarios, **31 tests en total**, todos pasando.

| Test | Casos cubiertos |
|---|---|
| `service/ReservaServiceTest` | crearReserva camino feliz, sin plazas, inmersión inexistente, usuario inexistente, marcarComoPagada (5 tests). |
| `service/PayPalServiceTest` | isConfigured con varios estados, fetchAccessToken sin configurar, getBaseUrl sandbox/live (8 tests). |
| `service/StripeServiceTest` | isEnabled con secret vacío/null/válida, getPublishableKey, createCheckoutSession sin configurar (5 tests). |
| `controller/UploadControllerTest` | subida correcta de PNG, video MP4, sin fichero, MIME no aceptado, extensión peligrosa (5 tests). |
| `config/EmojiStripMigrationTest` | texto sin emojis, pictograph, miscellaneous symbols, variation selector + dingbats, doble espacio, espacio antes de puntuación, multiples emojis, salto de línea (8 tests). |

Mockito para mocks. AssertJ para asserts fluidos. JUnit 5 para framework. `@ExtendWith(MockitoExtension.class)` en los servicios.

## 2.5. `database/`

- **`schema.sql`**: dump del esquema generado por Hibernate (`mysqldump --no-data`). Sirve para arrancar una BD vacía con la estructura correcta.
- **`views.sql`**: 5 vistas SQL (`vw_reservas_resumen`, `vw_estadisticas_centro`, `vw_actividad_usuario`, `vw_inmersiones_disponibles`, `vw_notificaciones_no_leidas`). Materializan agregaciones costosas.
- **`procedures.sql`**: 3 procedimientos almacenados (`sp_marcar_reserva_pagada`, `sp_purgar_historias_expiradas`, `sp_estadisticas_globales`) + creación idempotente de índices secundarios.

## 2.6. `docs/`

| Archivo | Contenido |
|---|---|
| `DiveConnect-Documentacion-Tecnica.pdf` | Memoria técnica integral en PDF, 87 páginas con 20 capítulos. |
| `DiveConnect-Defensa.pptx` | Slides para la defensa (18 diapositivas, paleta navy/teal/cream). |
| `diagrams/er-diagram.md` | Diagrama E/R en Mermaid + DBML para dbdiagram.io + análisis de normalización 1FN/2FN/3FN/BCNF. |
| `diagrams/class-diagram.md` | Diagrama de clases UML (entidades, servicios, seguridad). |
| `diagrams/architecture.md` | Capas + secuencia del flujo de pago + secuencia de despliegue. |
| `diagrams/gantt.md` | Cronograma con 10 sprints y 4 hitos. |
| `wireframes/01-login.svg` a `05-mapa.svg` | Wireframes de las 5 pantallas principales en mobile 375px. |
| `style-guide.md` | Paleta, tipografías, espaciados, iconografía, breakpoints, accesibilidad. |
| `test-plan.md` | Plan de pruebas con casos automatizados + walkthrough manual de ~10 min. |
| `lighthouse-audit.md` | Auditoría manual de Lighthouse con criterios de Accesibilidad y SEO. |
| `memoria-extra.md` | Capítulos complementarios para la rúbrica: Digitalización, Sostenibilidad/ODS, Sistemas Informáticos, Normalización detallada. |

## 2.7. `scripts/analytics/`

- **`analytics.py`**: pipeline ETL. Conecta a MySQL via PyMySQL, extrae datos, transforma con pandas, genera 3 gráficas con matplotlib y un CSV consolidado. Idempotente.
- **`requirements.txt`**: pandas, matplotlib, PyMySQL.
- **`README.md`**: instrucciones de uso, virtualenv, ejecución, output esperado.

---

# 3. Código por funcionalidad

Esta sección responde a la pregunta "y eso que estás enseñando, ¿qué archivos lo hacen?". Ordenado por flujos típicos del usuario.

## 3.1. Login con email + contraseña

```
Cliente  →  POST /api/auth/login {usernameOrEmail, password}
            ↓
            AuthController.login()
            ↓
            UserDetailsServiceImpl.loadUserByUsername()
              ↓ usuarioRepository.findByUsername()
            ↓
            BCryptPasswordEncoder.matches()
            ↓
            JwtUtil.generateToken(authentication)
            ↓
Cliente  ←  { token, tipo: "Bearer", usuario: {...} }
            ↓
            localStorage.setItem('token', ...)
            ↓
            redirect /pages/feed.html
```

**Archivos involucrados**:
- Frontend: `pages/login.html`, `js/auth.js`, `js/api.js`.
- Backend: `controller/AuthController.java`, `dto/request/LoginRequest.java`, `dto/response/LoginResponse.java`, `security/UserDetailsServiceImpl.java`, `security/JwtUtil.java`, `service/UsuarioService.java`, `repository/UsuarioRepository.java`, `entity/Usuario.java`.

## 3.2. Login con Google OAuth2

```
Cliente  →  click en "Continuar con Google"
            redirect a /oauth2/authorization/google
            ↓
            Google login + consent screen
            ↓
            redirect a /login/oauth2/code/google
            ↓
            GoogleOAuth2SuccessHandler.onAuthenticationSuccess()
              ↓ buscar email en BD
              ↓ si no existe, crear Usuario
              ↓ JwtUtil.generateToken()
            ↓
Cliente  ←  redirect /pages/oauth-callback.html?token=...&user=...
            ↓
            localStorage.setItem + redirect feed
```

**Archivos**:
- Frontend: `pages/login.html`, `pages/oauth-callback.html`, `js/auth.js`.
- Backend: `security/GoogleOAuth2SuccessHandler.java`, `config/SecurityConfig.java`, `service/UsuarioService.java`.

## 3.3. Crear publicación con foto subida desde galería

```
Usuario  →  click en "Crear" (dock) → "Nueva publicación"
            modal con file picker
            ↓
            usuario elige foto del móvil
            ↓
            FormData con file → POST /api/uploads
            ↓
            UploadController.subirArchivo()
              ↓ valida MIME, extensión, UUID
              ↓ guarda en uploads/<uuid>.png
            ↓
Frontend ←  { url: "/uploads/abc123.png", tipo: "FOTO" }
            ↓
            preview local + URL en input hidden
            ↓
            usuario rellena descripción, lugar, profundidad...
            ↓
            POST /api/publicaciones {contenido, imagenUrl, ...}
            ↓
            PublicacionController.crear()
              ↓ PublicacionService.crear()
              ↓ publicacionRepository.save()
            ↓
            cargarFeed() refresca el feed
```

**Archivos**:
- Frontend: `pages/feed.html`, `js/payment.js` (no, perdón, este flujo no usa payment), `js/api.js`, `js/publicaciones.js`.
- Backend: `controller/UploadController.java`, `controller/PublicacionController.java`, `service/PublicacionService.java`, `repository/PublicacionRepository.java`, `entity/Publicacion.java`, `config/WebConfig.java` (resource handler para `/uploads/**`).

## 3.4. Reservar inmersión y pagar

```
Usuario  →  /pages/Inmersiones.html → "Reservar" en una card
            ↓
            modal con número de personas
            ↓
            "Confirmar reserva" → POST /api/reservas
            ↓
            ReservaController.crearReserva()
              ↓ ReservaService.crearReserva()
                ↓ verificar plazas
                ↓ calcular precioTotal
                ↓ crear Reserva con estado PENDIENTE / UNPAID
                ↓ decrementar plazasDisponibles
                ↓ guardar
            ↓
            modal de pago (payment.js)
              ↓ detecta config (Stripe / PayPal / demo)
              ↓ usuario rellena tarjeta o usa SDK PayPal
            ↓
            POST /api/payments/verify/{reservaId} (modo demo / Stripe)
            POST /api/paypal/capture-order/{reservaId}?orderId= (PayPal)
            ↓
            PaymentController.verificar() o PayPalController.capturarOrden()
              ↓ si idempotente y ya PAID, salir
              ↓ marcar PAID + CONFIRMADA
              ↓ NotificacionService.crear() x2 (usuario + centro)
            ↓
            tick verde + redirect a /pages/reservas.html
```

**Archivos**:
- Frontend: `pages/Inmersiones.html`, `js/payment.js`, `js/api.js`.
- Backend: `controller/ReservaController.java`, `controller/PaymentController.java`, `controller/PayPalController.java`, `service/ReservaService.java`, `service/PayPalService.java`, `service/StripeService.java`, `service/NotificacionService.java`, `repository/ReservaRepository.java`, `repository/InmersionRepository.java`, `entity/Reserva.java`, `entity/Inmersion.java`, `entity/EstadoReserva.java`.

## 3.5. Sistema de seguimiento

```
Usuario A  →  ver perfil de Usuario B → "Seguir"
              ↓
              POST /api/seguimiento/solicitar/{B.id}
              ↓
              SeguimientoController → SeguimientoService.solicitar()
                ↓ if A.tipo == COMUN && B.tipo == COMUN: crear SolicitudSeguimiento
                ↓                                         + notificacion accionable a B
                ↓ else: directo, addSeguidor + notificacion NUEVO_SEGUIDOR

Usuario B  →  /pages/notificaciones.html → ve la solicitud
              "Aceptar" → POST /api/seguimiento/aceptar/{solicitudId}
              ↓
              SeguimientoService.aceptar()
                ↓ cambiar estado a ACEPTADA
                ↓ usuarioRepository.addSeguidor(A.id, B.id)
                ↓ NotificacionService.crear() SEGUIMIENTO_ACEPTADO a A
```

**Archivos**:
- Frontend: `pages/notificaciones.html`, `pages/Perfil.html`, `pages/buscar.html`, `js/api.js`.
- Backend: `controller/SeguimientoController.java`, `service/SeguimientoService.java`, `repository/UsuarioRepository.java` (queries nativas de seguidores), `repository/SolicitudSeguimientoRepository.java`, `service/NotificacionService.java`.

## 3.6. Búsqueda con fallback de proximidad

```
Usuario  →  /pages/buscar.html
            input "tabarca" + click "Usar mi ubicación"
            ↓
            navigator.geolocation.getCurrentPosition() → lat/lon
            ↓
            GET /api/search?q=tabarca&lat=...&lon=...
            ↓
            SearchController → SearchService.buscar()
              ↓ usuarios LIKE q
              ↓ centros LIKE q
              ↓ inmersiones LIKE q
              ↓ si todos 0 y hay lat/lon: InmersionRepository.findCercanas() (Haversine)
            ↓
Frontend ←  { usuarios, empresas, inmersiones, inmersionesPorProximidad: bool }
```

**Archivos**:
- Frontend: `pages/buscar.html` (toda la lógica está embebida en el `<script>` interno + uso de `js/api.js`).
- Backend: `controller/SearchController.java`, `service/SearchService.java`, `repository/InmersionRepository.java` (con `findCercanas` Haversine).

## 3.7. Mapa interactivo + tiempo

```
Usuario  →  /pages/mapa.html
            ↓
            Leaflet 1.9 + tiles OpenStreetMap
            ↓
            GET /api/puntos-mapa
            ↓
            PuntoMapaController.listarTodos()
            ↓
            renderizar marcadores en el mapa
            ↓
            click en marcador → detalle modal
            ↓
            GET /api/weather?lat=&lon=
            ↓
            WeatherController → WeatherService
              ↓ si OPENWEATHER_API_KEY: WebClient real
              ↓ si no: mock con flag source=mock
```

**Archivos**:
- Frontend: `pages/mapa.html` (todo el JS embebido), `js/api.js`.
- Backend: `controller/PuntoMapaController.java`, `controller/WeatherController.java`, `service/PuntoMapaService.java` (si existe — si no, está embebido en el controller), `service/WeatherService.java`.

## 3.8. Notificaciones

```
Cliente  →  cualquier página → poll cada 30s en nav.js
            GET /api/notificaciones/no-leidas
            ↓
            badge en topbar con el número
            ↓
            click en campana → /pages/notificaciones.html
            ↓
            GET /api/notificaciones
            ↓
            render lista
            ↓
            usuario click → POST /api/notificaciones/{id}/leer
```

**Archivos**:
- Frontend: `pages/notificaciones.html`, `js/nav.js`, `js/api.js`.
- Backend: `controller/NotificacionController.java`, `service/NotificacionService.java`, `repository/NotificacionRepository.java`, `entity/Notificacion.java`.

## 3.9. Dashboard de empresa (USUARIO_EMPRESA)

```
Usuario empresa  →  /pages/empresa/dashboard.html
                    ↓
                    GET /api/centros-buceo/mio
                    GET /api/inmersiones/mis-inmersiones
                    GET /api/reservas/centro/{id}
                    ↓
                    enseñar stats + listas
```

**Archivos**:
- Frontend: `pages/empresa/dashboard.html`, `pages/empresa/mi-centro.html`, `pages/empresa/mis-inmersiones.html`, `pages/empresa/gestionar-reservas.html`.
- Backend: `controller/CentroBuceoController.java`, `controller/InmersionController.java`, `controller/ReservaController.java`.

## 3.10. Panel de administración

```
ADMINISTRADOR  →  /pages/admin/dashboard.html
                  ↓
                  AdminController endpoints (todos requieren ROLE_ADMINISTRADOR)
```

**Archivos**:
- Frontend: `pages/admin/*.html`.
- Backend: `controller/AdminController.java`.

---

# 4. Guion para el tribunal

Tiempo total recomendado: **30 minutos de exposición + 10-15 minutos de preguntas**. Sigue este orden y los puntos clave.

## 4.1. Apertura (2 min)

> "Buenos días. Soy Marcos Mordoñez Estévez, estudiante de 2º DAW, y voy a presentar mi proyecto intermodular: **DiveConnect**, una red social y plataforma de reservas para la comunidad submarinista.
>
> El proyecto nace de un sector real fragmentado: los buceadores se comunican por Facebook, foros e Instagram sin filtros técnicos; los centros gestionan reservas por WhatsApp, email y transferencia; y el log book de cada buceador sigue siendo un cuaderno físico. DiveConnect unifica los tres frentes.
>
> Lo he construido con Java 17 + Spring Boot 3.2.3 en backend, MySQL 8 en persistencia, JavaScript vanilla en frontend, pasarela de pago real con Stripe y PayPal, y despliegue en Render con Docker."

**En pantalla**: la slide 1 (portada) o la slide 2 (contexto).

## 4.2. Demo en vivo (5-7 min)

> "Voy a empezar con una demo del flujo principal. Login con `sofia_buceo` / `admin`."

**Acción**: abrir la URL de Render en una pestaña nueva, hacer login.

> "Esto es el feed: stories 24h, publicaciones con datos técnicos —profundidad, temperatura, especies vistas—, sistema de likes y comentarios."

**Acción**: scroll por el feed, dar like a una publicación.

> "Vamos a Inmersiones. Aquí está el catálogo con filtros por nivel, profundidad y precio."

**Acción**: ir a Inmersiones, mostrar filtros.

> "Voy a reservar una. Selecciono número de personas → Confirmar reserva."

**Acción**: clickar en Reservar → Confirmar.

> "Aparece el modal de pago. La aplicación detecta automáticamente la configuración del backend. Como aquí no hay credenciales reales, está en modo demo TFG, marcado claramente con el badge amarillo. Si tuviera Stripe o PayPal configurados, redirigiría a su Checkout."

**Acción**: rellenar tarjeta `4242 4242 4242 4242` / `12/30` / `123` → Pagar.

> "Tick verde. La reserva queda CONFIRMADA y se generan dos notificaciones: una al usuario y otra al centro de buceo."

**Acción**: ir a Notificaciones, mostrar las dos notificaciones recién creadas.

> "Volvamos al perfil. Aquí puedo cambiar mi foto desde la galería —subida real con multipart, validada y guardada con UUID— y editar la biografía."

**Acción**: ir a Perfil, mostrar Editar.

> "Por último el mapa: marcadores georreferenciados con datos técnicos y el tiempo atmosférico actual."

**Acción**: ir a Mapa, click en un marcador.

## 4.3. Arquitectura (3 min)

**En pantalla**: slide 7 (arquitectura por capas) o el archivo `docs/diagrams/architecture.md` renderizado en GitHub.

> "El proyecto sigue una arquitectura de tres capas dentro de un único proceso Spring Boot. El cliente envía peticiones autenticadas con JWT al header Authorization. El JwtAuthenticationFilter valida el token antes de llegar al controller. Los controllers delegan en services, los services en repositories, los repositories en MySQL.
>
> Como servicios externos: Stripe Checkout, PayPal REST v2, Google OAuth2 y OpenWeatherMap. Todos opcionales: si las credenciales no están configuradas, la app sigue funcionando en modo demo."

## 4.4. Modelo de datos (3 min)

**En pantalla**: archivo `docs/diagrams/er-diagram.md` o slide 8.

> "15 tablas, 25 relaciones, 5 vistas SQL y 3 procedimientos almacenados. La estructura cumple 3FN con dos desnormalizaciones documentadas: `reservas.centro_buceo_id` está duplicado para acelerar la consulta más caliente del dashboard de empresa.
>
> Las relaciones N:M se modelan con tablas puente: `seguidores`, `publicacion_likes`. Las tablas puente son siempre BCNF.
>
> Las vistas materializan agregaciones costosas: estadísticas de centro, actividad por usuario, resumen de reservas. Los procedimientos encapsulan operaciones multi-tabla, como `sp_marcar_reserva_pagada` que actualiza el estado y crea las dos notificaciones en una transacción atómica."

## 4.5. Pasarela de pago (3 min)

**En pantalla**: slide 9.

> "La pasarela soporta tres modos en el mismo flujo de código:
>
> 1. **Demo TFG**: sin credenciales. Validación local con Luhn, marca PAID directamente. Ideal para presentaciones.
> 2. **Sandbox**: con `PAYPAL_CLIENT_ID` y/o `STRIPE_SECRET_KEY` de test. Se usan tarjetas de prueba contra los entornos sandbox reales.
> 3. **Live**: con credenciales reales. Cero cambios de código.
>
> El modal de pago detecta el modo automáticamente consultando `/api/paypal/config` y `/api/payments/config`, y muestra un badge visible al usuario para que sepa siempre dónde está. La idempotencia está garantizada en `/verify`: si la reserva ya está PAID, devuelve el estado sin notificar de nuevo."

## 4.6. Seguridad (2 min)

> "JWT stateless con HS256, secret de 64 caracteres, expiración 24h. Las contraseñas se hashean con BCrypt cost factor 10. Spring Security 6 con cadena de filtros stateless, CSRF desactivado por ser API JSON, OAuth2 Google opcional via flag.
>
> Cada endpoint declara su nivel de acceso explícitamente. Catálogo público, todo lo demás autenticado, /api/admin/** solo ROLE_ADMINISTRADOR. La validación de uploads es exhaustiva: MIME, lista blanca de extensiones, UUID en filenames para evitar path traversal."

## 4.7. Tests + CI/CD (2 min)

**En pantalla**: ejecutar `./mvnw test` en terminal, o enseñar la pestaña Actions de GitHub.

> "31 tests unitarios JUnit 5 + Mockito + AssertJ. Cubren los caminos críticos: creación de reserva con sus errores, configuración de PayPal y Stripe en distintos estados, validación de uploads, regex de eliminación de emojis. 0 fallos.
>
> GitHub Actions ejecuta el pipeline completo en cada push: build con JDK 17 + tests con MySQL como service container + build de imagen Docker en master."

## 4.8. Despliegue (2 min)

**En pantalla**: enseñar el dashboard de Render + el archivo `render.yaml`.

> "Tres caminos de despliegue:
>
> - Local sin Docker para desarrollo: `./mvnw spring-boot:run`.
> - Docker Compose para entorno aislado reproducible: `docker compose up`.
> - Render.com para producción: blueprint declarativo en `render.yaml`. Cada push a master redeploya automáticamente. SSL con Let's Encrypt, base de datos gestionada en la red interna de Render.
>
> La imagen Docker es multi-stage, ~120 MB. Healthcheck contra `/api/paypal/config`."

## 4.9. Análisis con Python (2 min)

**En pantalla**: enseñar la carpeta `scripts/analytics/` y las gráficas generadas.

> "El módulo optativo de Análisis de Datos lo cubro con un pipeline ETL en Python: PyMySQL para extracción, pandas para transformación, matplotlib para visualización. Genera tres gráficas y un CSV consolidado con métricas globales: usuarios activos, ingresos confirmados, top de inmersiones, especies más mencionadas. La paleta cromática coincide con la del frontend para mantener coherencia visual."

## 4.10. Decisiones técnicas notables (3 min)

**En pantalla**: slide 15 o el archivo `ISSUES.md`.

> "Tres decisiones merecen mención específica:
>
> **Primera**: Lombok @Data sobre entidades JPA con Set<Entity> rompe equals/hashCode. Los Set se vuelven impredecibles, e incluso pueden caer en recursión infinita. Lo descubrí al ver que `seguir y dejar de seguir` no funcionaban consistentemente. Lo resolví bypassando el Set para las consultas críticas mediante SQL nativo en `UsuarioRepository`. La alternativa ortodoxa habría sido refactorizar 15 entidades con riesgo de regresiones.
>
> **Segunda**: el modal de pago detecta automáticamente la configuración del backend. Esto permite que el TFG funcione fuera de la caja, que con env vars pase a sandbox real, y que con credenciales live procese cobros reales. Cero código duplicado.
>
> **Tercera**: la búsqueda por proximidad usa la fórmula de Haversine en SQL nativo. No es traducible a JPQL puro, y permite paginar en BD devolviendo una projection sin cargar entidades completas."

## 4.11. Roadmap (1 min)

**En pantalla**: slide 17 o `ISSUES.md` (issues abiertas).

> "El MVP está completo. Las próximas iteraciones documentadas en GitHub Issues son: migración de uploads a S3 para escalado horizontal, refresh tokens para sesiones largas, WebSockets para notificaciones en tiempo real, internacionalización a inglés y portugués, y un recomendador básico de inmersiones aprovechando el módulo de Python."

## 4.12. Cierre (1 min)

**En pantalla**: slide 18 (cierre).

> "El código está en GitHub público en `mmordoe685/diveconnect`. La memoria técnica completa son 87 páginas con 20 capítulos en `docs/DiveConnect-Documentacion-Tecnica.pdf`. Los tests, el CI, el blueprint de Render, los wireframes y el análisis Python están todos commiteados en el repo.
>
> Muchas gracias. ¿Preguntas?"

---

## 4.13. Preguntas frecuentes y respuestas preparadas

### "¿Por qué no usaste un framework JS como React?"

> "Decisión consciente. El TFG quería demostrar dominio de DOM, eventos y CSS modernos sin abstracciones intermedias. El bundle final son menos de 50 KB de CSS y JS combinados, sin transpilación ni bundler. El feedback loop es inmediato: editar y recargar. El coste es repetir markup entre páginas, pero la cantidad total de código es manejable. Si en una iteración futura el proyecto creciera, migrar a React o Vue sería trivial porque la API REST está completamente desacoplada."

### "¿Por qué Spring Boot y no Node.js?"

> "El módulo de Desarrollo Web en Entorno Servidor del ciclo se centra en Java + Spring Boot. Aprovechar ese conocimiento profundo era razonable. Spring Security 6 además simplifica enormemente el flujo OAuth2, y Spring Data JPA reduce el boilerplate de los repositorios."

### "¿Cómo manejas la seguridad de los uploads?"

> "Tres capas: validación de MIME (debe empezar por `image/` o `video/`), validación de extensión contra una lista blanca (jpg, jpeg, png, gif, webp, heic, heif, mp4, webm, mov, m4v), y nombre UUID generado por el servidor. Esto previene path traversal porque el cliente no controla el filename. Está cubierto con 5 tests unitarios."

### "¿Y si el servidor de Render se cae durante la defensa?"

> "Tengo un plan B local. Mi portátil está corriendo la app en `localhost:8080` con la misma versión del código. Puedo hacer la demo igual."

### "Lombok @Data tiene problemas con JPA. ¿Por qué lo seguiste usando?"

> "El problema es solo con colecciones autorreferenciales en `equals/hashCode`, no con `@Data` en general. Las entidades planas funcionan perfectamente con `@Data`. Para los casos críticos —`Usuario` con seguidores/siguiendo, `Publicacion` con likes— bypaso el Set con SQL nativo. La alternativa ortodoxa sería usar `@Getter @Setter @ToString(exclude = ...)` o `@EqualsAndHashCode(of = "id")`. Documentado en el ISSUES.md como decisión #1."

### "¿La pasarela de pago procesa dinero real?"

> "En modo demo TFG no, claramente marcado. Si configuras Stripe en modo test, tampoco hay dinero real. Si configuras Stripe en modo live con credenciales reales, sí —el código no cambia, solo cambian las env vars. Para esta defensa la app está en modo demo para no usar dinero real."

### "Si tuviera 100 mil usuarios, ¿qué cambiarías?"

> "Migrar uploads a S3 para que múltiples instancias puedan compartir el almacenamiento (issue #6). Refresh tokens para no obligar a relogin cada 24h (#7). WebSockets para notificaciones en lugar de poll (#8). OpenSearch para búsqueda full-text en cuanto las queries SQL se vuelvan lentas. Y un load balancer delante de varias instancias del JAR. La arquitectura actual ya es stateless, así que el escalado horizontal es trivial."

### "¿Qué fue lo más difícil?"

> "El bug de Lombok @Data con Set<Entity>. Llevaba dos sesiones intentando entender por qué las solicitudes de seguimiento no funcionaban consistentemente, hasta que vi el `StackOverflowError` en logs y até cabos. Encontrar el problema fue mucho más difícil que solucionarlo."

### "¿Por qué un módulo de Python si el resto es Java?"

> "Es mi optativa del ciclo: Programación en Python y Análisis de Datos. La rúbrica del TFG la incluye. La separación es coherente: Java para el sistema transaccional, Python para el análisis offline. La integración futura sería un controller en Spring Boot que sirva los CSV o llame a un endpoint Flask con el modelo entrenado."

---

## 4.14. Material para tener a mano durante la defensa

- Tu portátil con la app corriendo en local + Chrome con todas las pestañas abiertas (ver Bloque 7 de la GUIA-MANUAL.md).
- El PDF de memoria abierto en otra pestaña.
- El PowerPoint abierto en presentación.
- Conexión a internet estable.
- Los wireframes SVG abiertos en otra pestaña por si te preguntan por UX.
- El `dashboard.csv` generado por el Python script.

¡Suerte!
