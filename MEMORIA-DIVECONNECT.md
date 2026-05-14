# Memoria tecnica - DiveConnect

> Trabajo Fin de Grado · CFGS Desarrollo de Aplicaciones Web · 2o curso
> Autor: Marcos Mordonez Estevez · Optativa: Programacion en Python y Analisis de Datos
> Curso 2025/2026

## 1. Resumen

DiveConnect es una aplicacion web full-stack para la comunidad submarinista. Combina una red social vertical, un marketplace de inmersiones y herramientas de gestion para centros de buceo. El objetivo es digitalizar procesos hoy dispersos entre redes sociales, llamadas, hojas de calculo y pasarelas externas: descubrir puntos de buceo, publicar experiencias, seguir a otros usuarios, reservar inmersiones y pagar online.

Cubre los resultados de aprendizaje del CFGS DAW: cliente, servidor, base de datos, despliegue, pruebas, documentacion, accesibilidad, sostenibilidad, planificacion y analisis de datos con Python.

## 2. Objetivos

### 2.1 Objetivo general

Crear una plataforma web mantenible y desplegable que conecte buceadores y centros, permitiendo publicar contenido, consultar inmersiones, reservar plazas, pagar y gestionar actividad desde perfiles diferenciados.

### 2.2 Objetivos especificos

- Autenticacion stateless con JWT y tres roles: `USUARIO_COMUN`, `USUARIO_EMPRESA` y `ADMINISTRADOR`.
- CRUD completo para usuarios, centros, inmersiones, reservas, publicaciones, comentarios, historias, puntos de mapa y notificaciones.
- Interfaz responsive y accesible con HTML5, CSS3 y JavaScript ES2020 sin framework.
- Base de datos relacional normalizada con MySQL 8, vistas, procedimientos, indices y trigger temporal.
- Integracion opcional con Stripe Checkout, PayPal REST v2, Google OAuth2 y OpenWeatherMap; fallback demo para defensa.
- Despliegue reproducible con Docker multi-stage, Docker Compose, Render y pipeline GitHub Actions.
- Documentacion alineada con la rubrica PIDAWE: planificacion, viabilidad, riesgos y trazabilidad.

## 3. Alcance funcional

| Area | Funcionalidades |
|---|---|
| Red social | Feed, publicaciones con foto/video, comentarios, likes, historias 24h, seguimiento, notificaciones |
| Marketplace | Centros de buceo, inmersiones, filtros, reservas, control de plazas y pago Stripe/PayPal/demo |
| Mapa | Puntos geolocalizados, fotos asociadas, datos tecnicos y clima de OpenWeatherMap |
| Empresa | Panel de centro, gestion de inmersiones publicadas y reservas recibidas |
| Administracion | Panel admin para usuarios, centros, inmersiones y reservas con metricas agregadas |
| Analitica | Script Python ETL, CSV y graficas para metricas del proyecto |

Fuera del MVP: chat en tiempo real, app movil nativa, valoraciones avanzadas por reserva y dashboard analitico embebido en web.

## 4. Modelo de negocio y propuesta de valor

DiveConnect digitaliza un sector tradicionalmente analogico (centros de buceo de costa) donde las reservas se gestionan por telefono, correo o WhatsApp. El modelo de negocio simulado contempla:

- **Comision por reserva** confirmada: 5-8 % sobre el precio de la inmersion.
- **Plan premium** para centros (10-25 €/mes): destacado en busquedas, mas plazas simultaneas, exportacion de informes.
- **Plan freemium** para buceadores: bitacora y red social gratis; almacenamiento y filtros avanzados de pago.

La madurez digital se mide en tres niveles: presencia (web informativa), transaccional (reservar y pagar online) y comunidad (red social y bitacora). DiveConnect aporta los tres niveles en un unico producto.

## 5. Stack tecnologico

| Capa | Tecnologia | Justificacion |
|---|---|---|
| Backend | Java 17, Spring Boot 3.2 | Ecosistema robusto para API REST, seguridad y JPA |
| Seguridad | Spring Security 6, JWT (jjwt 0.11), BCrypt | Autenticacion stateless y hash seguro de contrasenas |
| Datos | MySQL 8 InnoDB, Spring Data JPA, Hibernate | Modelo relacional adecuado para reservas y relaciones sociales |
| Frontend | HTML5, CSS3, JavaScript ES2020 | Control total de UI y peso minimo del cliente |
| Mapa | Leaflet 1.9 + OpenStreetMap | Tiles libres respetuosos con RGPD |
| Pagos | Stripe Java SDK 24, PayPal REST v2 (HttpClient nativo) | Pasarelas reales con sandbox y fallback demo |
| Documentacion API | springdoc-openapi 2.3 | Swagger UI automatico desde anotaciones |
| Despliegue | Docker multi-stage, Render, GitHub Actions | Reproducibilidad, CI verde y deploy automatizable |
| Analitica | Python 3.10+, pandas, matplotlib, PyMySQL | ETL sencillo y graficas exportables |

## 6. Arquitectura

Arquitectura por capas con separacion estricta cliente/servidor:

- `controller/`: endpoints REST, validacion de entrada y codigos HTTP.
- `service/`: reglas de negocio, transacciones y conversion a DTOs.
- `repository/`: consultas Spring Data JPA, queries nativas y projections.
- `entity/`: modelo JPA y relaciones.
- `dto/`: objetos request/response para no exponer entidades.
- `security/`: filtro JWT, utilidades de token y OAuth2.
- `static/`: cliente HTML/CSS/JS servido por Spring Boot.

El frontend consume JSON mediante `fetchAPI` y conserva el JWT en `localStorage`. El backend mantiene el estado persistente en MySQL y usa transacciones para operaciones criticas como reservas y pagos.

Diagramas: `docs/diagrams/architecture.md`, `docs/diagrams/class-diagram.md`, `docs/diagrams/er-diagram.md`.

## 7. Base de datos

### 7.1 Modelo y normalizacion

El modelo principal incluye usuarios, centros, inmersiones, reservas, publicaciones, comentarios, historias, notificaciones, solicitudes de seguimiento y puntos de mapa. Se aplica normalizacion hasta 3FN; las tablas puente `seguidores` y `likes_publicacion` modelan relaciones N:M.

### 7.2 Evidencias

- Modelo E/R y paso a tablas: `docs/diagrams/er-diagram.md`.
- DDL completo: `database/schema.sql`.
- Vistas para consulta agregada: `database/views.sql`.
- Procedimientos almacenados e indices: `database/procedures.sql`.

### 7.3 Integridad y optimizacion

- Claves primarias `AUTO_INCREMENT` y foraneas declaradas con `ON DELETE` segun caso de uso.
- Trigger `trg_reservas_ultima_modificacion` para mantener la marca temporal si una reserva se actualiza fuera de Hibernate.
- Indices compuestos para consultas calientes: reservas por usuario/estado, inmersiones activas por fecha, publicaciones por fecha, notificaciones no leidas e historias expiradas.
- Desnormalizacion controlada en `reservas.centro_buceo_id` para evitar JOIN adicional al consultar reservas recibidas por centro.

## 8. Backend

API REST organizada por recursos:

- `/api/auth`: registro, login y usuario autenticado.
- `/api/usuarios`: perfil, busqueda y usuarios empresa.
- `/api/centros-buceo`: gestion y consulta de centros.
- `/api/inmersiones`: catalogo, filtros, gestion y proximidad Haversine.
- `/api/reservas`: reserva, cancelacion, confirmacion y paneles.
- `/api/payments` y `/api/paypal`: pagos Stripe, PayPal y demo idempotente.
- `/api/publicaciones`, `/api/comentarios`, `/api/historias`: red social.
- `/api/puntos-mapa` y `/api/weather`: mapa y meteorologia.
- `/api/admin`: datos agregados y panel administrativo.

Seguridad: JWT con HS256, BCrypt para contrasenas, reglas por ruta y rol en `SecurityConfig`, CORS centralizado en `CorsConfig` y configurable por `CORS_ALLOWED_ORIGINS`. Las excepciones de dominio se mapean en `GlobalExceptionHandler`; las genericas no devuelven detalles internos al cliente.

Swagger UI disponible en `/swagger-ui.html` durante desarrollo.

### 8.1 Programacion orientada a objetos

- Entidades JPA con relaciones bidireccionales controladas para evitar recursion (excluyendo colecciones de `equals`/`hashCode`).
- Capa de servicio `@Transactional` con metodos cohesivos y reglas de negocio.
- DTOs request/response separados de la entidad para no exponer estructura interna.
- Patron Repository de Spring Data; queries derivadas + queries nativas con `Pageable` cuando se necesita SQL especifico.
- Manejo de excepciones jerarquico: excepciones de dominio especificas (`RecursoNoEncontradoException`, `OperacionInvalidaException`) capturadas en `GlobalExceptionHandler`.

## 9. Frontend

Cliente con HTML5, CSS3 y JavaScript modular sin framework:

- `api.js`: capa comun de llamadas HTTP y manejo de JWT.
- `auth.js`: login, registro y sesion.
- `nav.js`: topbar, dock por rol, notificaciones y enlace "Saltar al contenido".
- `payment.js`: modal de pago y comunicacion con Stripe/PayPal/demo.
- `publicaciones.js`: feed, publicaciones, comentarios, likes e historias.
- `style.css` y `ocean-theme.css`: sistema visual responsive con tokens CSS.

### 9.1 Interactividad DOM y gestion de estado

- Manipulacion DOM directa con `document.querySelector`, `addEventListener`, `appendChild`.
- Async/await sobre `fetch` para todas las llamadas a la API.
- Estado de sesion en `localStorage` (`jwt`, `usuario`); estado efimero en memoria.
- Renderizado incremental para feed e historias; debounce en busquedas.

### 9.2 SEO y semantica HTML5

Las paginas publicas incluyen:

- `<!DOCTYPE html>`, `lang="es"`, `<meta charset>` y `<meta viewport>`.
- `<meta name="description">`, `<title>` descriptivo y Open Graph (`og:title`, `og:image`).
- Estructura semantica con `<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<footer>`.
- Atributos `alt` en imagenes, `aria-label` en botones de icono y enlace "Saltar al contenido" para teclado.
- Foco visible mediante `:focus-visible` y contraste validado en `docs/lighthouse-audit.md`.

### 9.3 Sindicacion y formatos de intercambio

La API utiliza JSON como formato de intercambio en todos los endpoints REST. Los DTOs serializan con Jackson; las respuestas estructuradas incluyen paginacion (`page`, `size`, `totalElements`) cuando aplica.

## 10. Pruebas

### 10.1 Suite automatizada

Cobertura sobre servicios y controladores criticos:

| Clase de test | Cubre |
|---|---|
| `ReservaServiceTest` | Plazas, estados, reglas de reserva y excepciones |
| `StripeServiceTest` | Configuracion, sesiones, errores controlados |
| `PayPalServiceTest` | Configuracion, fallos de red y respuestas no validas |
| `UploadControllerTest` | Validacion MIME, extensiones y path traversal |
| `EmojiStripMigrationTest` | Limpieza de caracteres incompatibles con utf8mb3 |

```bash
mvn test
```

Resultado de referencia: **31 tests ejecutados, 0 fallos, 0 errores**.

### 10.2 Plan de pruebas

Documentado en `docs/test-plan.md`: incluye casos manuales para los flujos criticos (registro, login, reserva con pago, publicacion con archivo, busqueda y mapa) en navegadores Chrome, Firefox, Edge y Safari iOS.

### 10.3 Optimizacion del entorno

- Maven Wrapper (`mvnw`) para no depender de instalacion global.
- GitHub Actions con MySQL service container ejecuta `mvn test` y build de Docker en cada push.
- Lighthouse manual con `docs/lighthouse-audit.md`.
- `.editorconfig` y `.gitattributes` para consistencia entre Windows y Linux.

## 11. Despliegue e infraestructura

### 11.1 Modos de ejecucion

- Local con Maven (`./mvnw spring-boot:run`) y MySQL 8.
- Local reproducible con Docker Compose (`docker compose up -d`).
- Produccion con imagen Docker en Render.com + base MySQL externa.

### 11.2 Archivos clave

- `Dockerfile`: build multi-stage, usuario no-root, volumen de uploads, healthcheck con Actuator.
- `docker-compose.yml`: app + MySQL 8 + volumenes.
- `render.yaml`: blueprint del servicio web con variables seguras.
- `.env.example`: plantilla de configuracion sin secretos.
- `.github/workflows/ci.yml`: build, tests, empaquetado y Docker build.

### 11.3 Seguridad en produccion

- HTTPS/SSL via Render (Let's Encrypt) con redireccion automatica HTTP→HTTPS.
- Variables sensibles (`JWT_SECRET`, claves Stripe/PayPal, OAuth) inyectadas como variables de entorno; nunca commiteadas.
- Usuario no-root dentro del contenedor; volumenes acotados.
- CORS restringido por dominio en produccion.

### 11.4 CI/CD

GitHub Actions ejecuta en cada push:
1. Checkout + setup JDK 17 + cache Maven.
2. `mvn -B test` con MySQL 8 service container.
3. Empaquetado `mvn -B package -DskipTests`.
4. Build de imagen Docker (sin push) para verificar Dockerfile.

Render hace auto-deploy a partir de `master` cuando CI esta verde.

### 11.5 Manual de puesta en marcha

`INSTALL.md` documenta el procedimiento completo: prerequisitos, clonado, variables de entorno, creacion de BD, compilacion, arranque, verificacion y comandos Docker.

## 12. Sostenibilidad y Green Code

### 12.1 Eficiencia tecnica

- Consultas paginadas con `Pageable` en repositorios para evitar volcados completos.
- Indices SQL en columnas filtradas con frecuencia.
- `@Scheduled` para limpieza horaria de historias expiradas en lugar de eliminar bajo demanda.
- Logs SQL desactivados por defecto para reducir IO y ruido.
- Docker multi-stage reduce la imagen final a < 250 MB.
- Uploads fuera del control de versiones (`uploads/.gitkeep`) para no inflar el repositorio.

### 12.2 Impacto en Objetivos de Desarrollo Sostenible (ODS)

| ODS | Aporte de DiveConnect |
|---|---|
| ODS 9 - Industria, innovacion e infraestructura | Digitaliza un sector PYME tradicionalmente analogico |
| ODS 11 - Ciudades y comunidades sostenibles | Reduce desplazamientos y llamadas: reservas confirmadas online sin gestion manual |
| ODS 12 - Produccion y consumo responsables | Bitacora digital sustituye al cuaderno fisico; reduce papel y plastico |
| ODS 14 - Vida submarina | Sensibilizacion: el feed promueve buenas practicas y conservacion marina |

### 12.3 Mantenimiento a largo plazo

- Stack maduro (Spring Boot LTS, MySQL 8 LTS) con soporte oficial extendido.
- Dependencias monitorizadas para evitar acumular deuda tecnica.
- Documentacion exhaustiva para facilitar relevos.
- Gestion de residuos digitales: `@Scheduled` purga historias caducadas; las notificaciones antiguas se pueden purgar con un procedimiento documentado.

## 13. Diseno de interfaces

### 13.1 Guia de estilo

Documentada en `docs/style-guide.md`: paleta submarina (azul profundo, turquesa, blanco arena), tipografia, espaciado, sombras, radius y estados de foco.

### 13.2 Estrategia responsive

- Enfoque mobile-first con breakpoints en 480 px, 768 px y 1024 px.
- `meta viewport` correcto en todas las paginas.
- Tipografia fluida con `clamp()` y unidades relativas (`rem`).
- Imagenes adaptables con `max-width: 100%`.
- Dock inferior en movil y topbar en escritorio (ver `nav.js`).

### 13.3 Wireframes y accesibilidad

- Wireframes SVG en `docs/wireframes/`.
- Cumplimiento WCAG 2.1 nivel AA verificado con Lighthouse y revision manual.
- Contraste de texto ≥ 4.5:1.
- Foco visible y orden de tabulacion logico.
- `aria-label`, `role` y `alt` donde corresponde.

## 14. Analitica Python

Modulo `scripts/analytics/` con flujo ETL completo:

1. **Extract**: conexion a MySQL via `PyMySQL` con credenciales del `.env`.
2. **Transform**: limpieza y agregacion con `pandas` (publicaciones por mes, estado de reservas, top de inmersiones).
3. **Load**: exporta CSV en `scripts/analytics/output/` y graficas PNG con `matplotlib`.

Las graficas obtenidas (PNG) se referencian en la memoria y se incluyen como evidencia en la defensa. La integracion con la web queda como mejora futura via `AnalyticsController` o panel admin embebido.

## 15. Gestion del ciclo de vida

### 15.1 Metodologia

Iterativa-incremental con sprints semanales. Cronograma completo en `docs/diagrams/gantt.md` (10 sprints + hitos clave + diagrama Mermaid Gantt).

### 15.2 Estrategia de ramas

Flujo simplificado tipo GitHub Flow:

- `master`: rama estable, recibe el codigo terminado y dispara el auto-deploy a Render.
- `feature/*`: ramas cortas para cambios puntuales que se mergean a master via Pull Request.

Convencion de commits: estilo Conventional Commits (`feat`, `fix`, `refactor`, `docs`, `test`, `chore`).

### 15.3 Hitos cumplidos

- MVP Backend (21 nov 2025)
- Pasarela de pago funcional (19 dic 2025)
- MVP completo (27 mar 2026)
- Defensa tribunal (22 may 2026)

## 16. Incidencias, decisiones y trazabilidad

### 16.1 Incidencias documentadas

`ISSUES.md` recoge problemas detectados durante el desarrollo y su resolucion. Casos destacados:

- Bug de recursion `equals/hashCode` con Lombok `@Data` + `Set` en relaciones bidireccionales → resuelto con `@EqualsAndHashCode.Exclude` en colecciones.
- Race condition en confirmacion de pago (modal cerrado antes de leer precio) → resuelta capturando datos antes de cerrar el modal.
- Stripe en modo demo no creaba notificaciones → refactor de `PaymentController.verificar` con metodo privado comun a las tres ramas.
- `LIMIT :limite` marcado como error por linter de VS Code → migrado a `Pageable` (idiomatico Spring Data).

### 16.2 Decisiones de diseno

- HTML/CSS/JS sin framework para mantener bajo el peso del cliente y demostrar dominio del stack base.
- Fallback demo en pagos para que la defensa funcione sin credenciales reales.
- OAuth2 Google desactivado por defecto hasta configurar credenciales reales.
- CORS configurable por entorno: abierto en local, restringido en produccion.

### 16.3 Trazabilidad con la rubrica

`COBERTURA-RUBRICA.md` mapea cada criterio de la rubrica PIDAWE con su evidencia concreta en el repositorio. Sirve como checklist para la defensa.

## 17. Conclusiones y trabajo futuro

DiveConnect cumple los criterios principales del PIDAWE: aplica bases de datos, cliente, servidor, despliegue, diseno, pruebas, documentacion, planificacion y analisis de datos. La arquitectura es mantenible, la demo es defendible y el repositorio queda navegable para el tribunal.

Lineas de evolucion:

- Apps moviles nativas iOS/Android consumiendo la API REST existente.
- WebSockets para chat 1-a-1 y notificaciones en tiempo real.
- Dashboard analitico embebido con metricas en vivo (Plausible/Matomo o integracion propia).
- Cache distribuida (Redis) y CDN (Cloudflare) para soportar mayor concurrencia.
- Verificacion KYC empresarial en Stripe/PayPal para pagos en produccion real.
- Programa piloto con 5-10 centros de buceo de la Costa Tropical.
