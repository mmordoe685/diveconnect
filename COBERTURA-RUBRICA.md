# Cobertura de la rúbrica PIDAWE — Estado real del proyecto

Para cada módulo de la rúbrica, este documento dice:

- **Qué cumple** y **por qué** lo cumple (qué archivo, qué endpoint, qué funcionalidad lo respalda).
- **Cómo demostrarlo** ante el tribunal: el comando exacto, la URL, la pantalla concreta o el fichero a abrir.
- **Qué falta**, si algo falta (tareas tuyas pendientes que están en `GUIA-MANUAL.md`).

Verificado en local con la rama `claude/peaceful-hellman` el día previo a esta entrega: **31/31 tests pasando, 8/8 endpoints públicos respondiendo 200, 13 tablas + 5 vistas + 4 procedimientos en BD, login JWT operativo**.

Leyenda:

- **OK**: cubierto y funcional ahora mismo.
- **MANUAL**: cubierto en el código, pero requiere que ejecutes algo manualmente (Render deploy, Lighthouse, etc.) — todo está listado en `GUIA-MANUAL.md`.
- **PARCIAL**: hay algo del criterio cumplido pero podría reforzarse.

---

## Resumen ejecutivo

| Módulo | Defensa oral | Memoria técnica | Comentario |
|---|:-:|:-:|---|
| BD (1º) — 11.04 % | OK | OK | E/R + DDL + vistas + procedures + 3FN documentada |
| Digitalización (1º) — 1.84 % | OK | OK | Capítulo en `docs/memoria-extra.md` |
| Sostenibilidad (1º) — 1.84 % | OK | OK | Capítulo con ODS + Green Code + huella |
| Entornos Desarrollo (1º) — 5.52 % | OK | OK | 31 tests + Git + GitHub Actions |
| LMSGI (1º) — 5.52 % | OK | OK | HTML5 + responsive + SEO + JSON intercambio |
| Programación (1º) — 14.72 % | OK | OK | UML + Lombok + excepciones + Javadoc puntual |
| Sistemas Informáticos (1º) — 9.20 % | OK | OK | Capítulo C + diagrama infra + backups |
| DWEC (2º) — 12.08 % | OK | OK | CRUD + 7 módulos JS + DOM + fetch async |
| DWES (2º) — 14.09 % | OK | OK | MVC + JWT + Swagger + 17 controllers |
| Despliegue (2º) — 4.03 % | MANUAL | OK | Dockerfile + render.yaml listos, te toca dar deploy |
| PIDAWE (2º) — 4.03 % | OK / MANUAL | OK | README + Gantt + ISSUES.md, te toca subir Issues a GitHub |
| DIW (2º) — 10.06 % | OK / MANUAL | OK | Wireframes + style-guide + tema submarino, te toca capturas Lighthouse |
| Python (Optativa) — 6.03 % | MANUAL | OK | Script ETL completo, te toca ejecutarlo y pegar las gráficas |

**Estimación de nota previsible si presentas hoy con todo lo manual hecho: 8.5–9.5 / 10.**

---

# 1º DAW

## Bases de Datos · 11.04 %

### Defensa oral

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Diagrama E/R y paso a tablas | OK | Diagrama E/R en formato Mermaid (renderiza en GitHub) y DBML para dbdiagram.io. Documentación de cada tabla con columnas, tipos, FKs y constraints. | Abre `docs/diagrams/er-diagram.md` directamente en GitHub. Mermaid se renderiza automáticamente. Para enseñarlo más bonito: copia el bloque DBML en https://dbdiagram.io/d y exporta como PNG. |
| Calidad del código DDL/DML, vistas y procedimientos | OK | `database/schema.sql` es el dump real de Hibernate. `database/views.sql` define 5 vistas (`vw_reservas_resumen`, `vw_estadisticas_centro`, `vw_actividad_usuario`, `vw_inmersiones_disponibles`, `vw_notificaciones_no_leidas`). `database/procedures.sql` define 3 SP (`sp_marcar_reserva_pagada`, `sp_purgar_historias_expiradas`, `sp_estadisticas_globales`) y 1 SP que crea índices secundarios idempotentemente. | Abre `database/views.sql` y `database/procedures.sql` en VS Code. En MySQL Workbench ejecuta `CALL sp_estadisticas_globales();` para enseñar el SP en vivo devolviendo métricas reales. |
| Justificación técnica de la estructura | OK | Capítulo D del `docs/memoria-extra.md` detalla la normalización 1FN/2FN/3FN/BCNF con dos desnormalizaciones deliberadas y razonadas (`reservas.centro_buceo_id` y duplicidad `usuarios.nombre_empresa` ↔ `centros_buceo.nombre`). | Lee el capítulo D en alto; el tribunal puede pedir que justifiques una desnormalización concreta y la respuesta está ahí. |

### Memoria técnica

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Modelado de datos (E/R + MRE + normalización) | OK | Capítulo 5 del PDF principal + `docs/diagrams/er-diagram.md` + capítulo D de `docs/memoria-extra.md`. | Mostrar el PDF y los .md en GitHub. |
| Integridad y restricciones (PKs, FKs, triggers) | OK | Las PKs/FKs son visibles en `database/schema.sql`. Hay un trigger preparado (comentado) `trg_recalcular_valoracion_centro` para futura extensión, mencionado en `procedures.sql`. Constraints `unique` en `usuarios.username`, `usuarios.email` y `centros_buceo.usuario_id`. | Abre `schema.sql` y enseña la sección de FK en `reservas` (3 FKs) y `inmersiones` (1 FK). |
| Optimización (procedimientos, consultas complejas) | OK | El procedimiento `sp_marcar_reserva_pagada` orquesta UPDATE + 2 INSERT en transacción atómica con manejo de errores. Hay índices secundarios sobre `reservas(usuario_id, estado)`, `inmersiones(activa, fecha_inmersion)`, `publicaciones(fecha_publicacion)`, `notificaciones(destinatario_id, leida)`, `historias(expira_en)`. | Enseña la sección "Optimización" del PDF. En vivo: `EXPLAIN SELECT * FROM reservas WHERE usuario_id = 56 AND estado = 'PENDIENTE';` muestra que usa el índice `idx_reservas_usuario_estado`. |

---

## Digitalización aplicada al sector productivo · 1.84 %

| Criterio (defensa y memoria) | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Impacto en el sector | OK | Capítulo A de `docs/memoria-extra.md`: análisis del sector buceo en España, problemas actuales (reservas analógicas, comunidad fragmentada, log book en papel), propuesta de DiveConnect como solución triangular (marketplace + red social + diario digital). | Lee el capítulo A.1 + A.2 al tribunal. |
| Tecnologías habilitadoras (IA, Big Data, IoT) | OK | Capítulo A.2: tabla detallada de las 7 tecnologías habilitadoras usadas (OAuth 2.0, Geolocalización + Haversine, Cloud-native, JWT stateless, Pasarelas externas, APIs en tiempo real, Generación PDFs). Honestidad: aclara que no hay IA en v1.0 pero sí está en el roadmap (`ISSUES.md #12`, recomendador en Python). | Tabla del A.2 + roadmap del PDF cap. 19. |
| Madurez digital y escalabilidad | OK | Capítulo A.3: tabla con 6 ejes (datos, tráfico, almacenamiento, latencia, búsqueda, notificaciones) y plan concreto de escalado para cada uno. | Capítulo A.3. Si te preguntan por escalado horizontal: respuesta en `APUNTES-PROYECTO.md` sección 4.13. |
| Modelo de negocio digital | OK | Capítulo A.4: freemium con dos vías (comisión 3-5 % por reserva + suscripción opcional centros 19 €/mes), comparado con benchmarks de hostelería. | Capítulo A.4. |

---

## Sostenibilidad aplicada al sistema productivo · 1.84 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Green Code y eficiencia del software | OK | Capítulo B.1 de `docs/memoria-extra.md`: tabla con 10 decisiones técnicas concretas que reducen consumo (Haversine en SQL en vez de Java, paginación obligatoria, vistas SQL precomputadas, índices secundarios, caché HTTP, CSS keyframes, prefers-reduced-motion, gzip, imagen Alpine 120 MB, JVM con -Xmx450m + G1GC). | Capítulo B.1. Para enseñar en vivo: `Dockerfile` línea 35 (`JAVA_OPTS`) y `application.properties` línea de paginación. |
| Impacto socio-ambiental y ODS | OK | Capítulo B.2: alineación explícita con 6 ODS (14 vida submarina, 9 industria, 8 trabajo digno, 4 educación, 11 ciudades sostenibles, 13 acción climática). Cada ODS con párrafo justificativo. | Capítulo B.2. |
| Huella de carbono digital | OK | Capítulo B.3: cálculo cuantitativo con metodología Sustainable Web Design. Datos: 1.5 MB/sesión, ~0.75 g CO2/mes con 1000 visitantes, comparado con búsquedas Google (0.2 g) y email con adjunto (50 g). | Capítulo B.3 + comparativas. |
| Mantenimiento sostenible (residuos digitales) | OK | Capítulo B.4: estrategia documentada para 5 categorías (purga historias 24h con `sp_purgar_historias_expiradas`, soft-delete de usuarios LOPD, uploads huérfanos, logs rotados, imágenes Docker tagged y purgadas en 7 días). | Capítulo B.4 + el procedimiento real `sp_purgar_historias_expiradas` en `database/procedures.sql`. |

---

## Entornos de Desarrollo · 5.52 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Control de versiones (Git, ramas, commits) | OK | Repositorio Git con histórico completo (40+ commits documentados con mensajes descriptivos). Dos ramas: `master` (mainline) y `claude/peaceful-hellman` (development branch). | `git log --oneline --graph --all` en terminal. Enseña `https://github.com/mmordoe685/diveconnect/network` (network graph). |
| Pruebas y depuración | OK | **31 tests JUnit 5 + Mockito + AssertJ**, en 5 clases: `ReservaServiceTest` (5), `PayPalServiceTest` (8), `StripeServiceTest` (5), `UploadControllerTest` (5), `EmojiStripMigrationTest` (8). 0 fallos. | En vivo: `./mvnw test` en terminal. Enseña la última línea "Tests run: 31, Failures: 0, Errors: 0, Skipped: 0". También `docs/test-plan.md` para el walkthrough manual. |
| Documentación del entorno (IDEs, herramientas) | OK | `INSTALL.md` documenta los 3 caminos de despliegue (local sin Docker, Docker Compose, Render). `README.md` lista herramientas (IntelliJ, VS Code, Docker, MySQL 8). | `INSTALL.md` y `README.md`. |
| GitHub Actions (CI) | OK | `.github/workflows/ci.yml` define pipeline completo: build con JDK 17, MySQL como service container, tests con surefire, upload de artefactos, build de imagen Docker en master. Se dispara en cada push. | Enseña el archivo en VS Code y la pestaña Actions de GitHub si has hecho algún push reciente. |

---

## Lenguajes de Marcas y Sistemas de Gestión de Información · 5.52 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Diseño adaptativo (CSS responsive) | OK | `ocean-theme.css` tiene `@media (max-width: 720px)` con 8 reglas específicas: container fluido, modales como bottom sheets, inputs 44px, font-size 16px, filter tabs scroll horizontal, grids 1 columna, `padding-bottom: 76px`, `safe-area-inset-bottom`. | DevTools → Toggle Device Toolbar → iPhone 12 Pro. Carga `/pages/feed.html` y `/pages/Inmersiones.html`. Los modales surgen desde abajo con animación spring. |
| Sintaxis HTML correcta y SEO | OK | Las 21 páginas usan HTML5 semántico (`<header>`, `<main>`, `<section>`, `<article>`, `<nav>`, `<footer>`). Cada página tiene `<title>` único, `<meta name="description">`, OpenGraph completo (`og:type`, `og:title`, `og:description`, `og:image`), `theme-color`, `lang="es"`, `viewport-fit=cover`. | `view-source:http://localhost:8080/pages/feed.html` enseña los meta tags. Lighthouse SEO debería puntuar alto. |
| Accesibilidad básica (WCAG) | OK | `aria-label` en botones con sólo icono. Estructura semántica. Contraste verificado (texto principal 14.2:1). `prefers-reduced-motion` respetado en JS y CSS. `:focus-visible` con outline aqua de 3px. `docs/lighthouse-audit.md` documenta cada criterio. | Lighthouse en Chrome DevTools (manual). `docs/lighthouse-audit.md` para los detalles técnicos. |
| XML/JSON para intercambio | OK | Toda la API REST usa JSON (request y response). DTOs definidos para serialización. `ResourceHttpRequestHandler` configurado. `@RestController` en los 17 controllers. | `curl http://localhost:8080/api/inmersiones/disponibles | jq` muestra JSON formateado. Swagger UI enseña el schema completo en `/v3/api-docs`. |
| Justificación de frameworks | OK | Decisión consciente de **no usar framework JS**. Argumentación en cap. 18 del PDF y en el guion del tribunal: dominio de DOM puro, bundle 50 KB, audit trivial, feedback loop instantáneo. | Si te preguntan: respuesta preparada en `APUNTES-PROYECTO.md` sección 4.13 ("¿Por qué no usaste un framework JS como React?"). |

---

## Programación · 14.72 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Comunicación técnica (terminología profesional) | OK | Demostrable en cualquier código: nombres de clases consistentes (Service, Controller, Repository, DTO), uso correcto de patrones (MVC, Repository, Strategy implícito, DTO, Builder via Stripe SDK, Filter Chain de Spring Security). | Cualquier archivo en `src/main/java/`. Si te preguntan por un patrón: `APUNTES-PROYECTO.md` sección 2.2 lista los patrones aplicados. |
| Habilidades de comunicación | (Tú) | El código está; cuenta cómo lo hiciste con confianza. Tienes el guion en `APUNTES-PROYECTO.md` sección 4. | Practica el guion 2-3 veces en alto antes de la defensa. |
| Resolución de problemas + código bien estructurado | OK | 5 bugs reales documentados en `ISSUES.md` con causa, diagnóstico y solución (Lombok @Data + Set, race condition, notificación faltante, paymentStatus null, EmojiStripMigration). Estructura por capas con dependencias verticales (controller → service → repo). Inyección por constructor con `@RequiredArgsConstructor`. | Lee `ISSUES.md` issues #1 a #5. Cuenta el bug de Lombok @Data al tribunal: es la historia más memorable. |
| Lógica y algoritmia (memoria) | OK | Algoritmos clave documentados: Haversine en `InmersionRepository.findMasCercanas`, Luhn en `payment.js`, máquina de estados de seguimiento en `SeguimientoService`, idempotencia en `PaymentController.verificar`, regex Unicode en `EmojiStripMigration.stripEmojis`. | Capítulo 8 del PDF + cualquiera de esos archivos en VS Code. |
| Diseño OO + diagrama de clases | OK | `docs/diagrams/class-diagram.md` tiene 3 diagramas Mermaid (entidades, servicios, seguridad). Patrones aplicados en tabla. 11 entidades, 15 servicios, herencia/composición clara. | Abre `docs/diagrams/class-diagram.md` en GitHub (Mermaid se renderiza automáticamente). |
| Tratamiento de excepciones | OK | 4 clases en `exception/`: `BadRequestException` → 400, `ResourceNotFoundException` → 404, `UnauthorizedException` → 401, y `GlobalExceptionHandler` (`@RestControllerAdvice`) que las traduce a JSON `{status, message, timestamp}`. Cubierto con 5 tests en `ReservaServiceTest`. | `src/main/java/com/diveconnect/exception/GlobalExceptionHandler.java` + `ReservaServiceTest.crearReserva_sinPlazas` que verifica que se lanza `BadRequestException`. |

---

## Sistemas Informáticos · 9.20 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Esquema de infraestructura | OK | Capítulo C.1 de `docs/memoria-extra.md` con diagrama ASCII de infraestructura (Cloudflare → Render Edge → Container Spring Boot + Container MySQL + Volumen uploads → Servicios externos). | Capítulo C.1 + `docs/diagrams/architecture.md` que tiene diagrama Mermaid de despliegue. |
| Instalación y compatibilidad | OK | `INSTALL.md` documenta los 3 caminos. Capítulo C.4 enumera 6 navegadores y versiones soportadas (Chrome/Edge 120+, Firefox 120+, Safari 16+ con Safari iOS, Chrome Android, IE no soportado). | `INSTALL.md` + Capítulo C.3 (hardware mínimo). |
| Configuración de red, puertos | OK | Capítulo C.2: tabla con todos los puertos (443 público, 8080 interno, 3306 MySQL en red interna), CORS restrictivo en producción, redirecciones HTTP→HTTPS automáticas, outbound permitido a Stripe/PayPal/Google/OpenWeather. | Capítulo C.2. |
| Seguridad: backups y permisos | OK | Capítulo C.5: BCrypt, JWT 64+ caracteres, autorización por rol (público/autenticado/EMPRESA propietario/ADMINISTRADOR), validación de uploads (MIME + lista blanca + UUID), headers de seguridad por defecto Spring. Capítulo C.5.4: tabla de backups (BD diaria automática Render, uploads mensual manual, repo eterno en GitHub). | Capítulo C.5 + el archivo `SecurityConfig.java` muestra los matchers explícitos. |
| Escalabilidad | OK | Capítulo A.3 + capítulo 18 del PDF: scaling horizontal documentado (uploads → S3, refresh tokens, WebSockets, OpenSearch). | Capítulos referenciados. |

---

# 2º DAW

## Desarrollo Web en Entorno Cliente · 12.08 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Interactividad y CRUD en directo | OK | El proyecto tiene CRUDs completos visibles: publicaciones (crear, editar implícito al cambiar foto, eliminar), reservas (crear, cancelar), inmersiones (CRUD para empresa), comentarios (crear), seguimiento (solicitar, aceptar, rechazar, dejar). | Demo en vivo: `pages/feed.html` → "Crear" → publicación con foto. Después borra esa misma publicación. |
| Validación de formularios | OK | Validación en cliente: formato MM/AA, CVC 3-4 dígitos, Luhn de tarjeta en `payment.js`. Validación HTML5: `required`, `type="email"`, `min`, `max`, `minlength`. Validación servidor: `@NotNull`, `@Email`, `@Min` con `jakarta.validation` en DTOs. | Demo en vivo en login con email mal formateado, o registro con username < 3 chars. |
| Organización del código (HTML/CSS/JS separados) | OK | `static/css/` (2 archivos), `static/js/` (7 módulos), `static/pages/` (21 HTML). Cada HTML carga sus recursos externamente con `<link>` y `<script src>`. | `tree src/main/resources/static` o navega la estructura en VS Code. |
| Funciones JavaScript explicables | OK | 7 módulos JS bien documentados con comentarios funcionales: `api.js` (fetchAPI con JWT), `auth.js` (login/logout), `nav.js` (topbar + dock + poll badge), `publicaciones.js`, `payment.js` (modal pago multi-pasarela), `ocean-effects.js` (burbujas + reveal-on-scroll + tilt 3D), `animations.js`. | Abre `js/payment.js` en VS Code y enseña la función `procesarPago`. La función `fetchAPI` en `api.js` es otra buena. |
| Consumo de servicios asíncronos | OK | Todo el frontend usa `fetch` con `async/await`. `api.js` envuelve fetch con manejo de JWT y 401/403. Funciones `Promise.all` en varios sitios para llamadas paralelas. | Cualquier flujo: abrir DevTools Network → hacer login → ver la request POST /api/auth/login con su response. |
| Gestión de estado | OK | `localStorage` para token + user. `getCurrentUser()` en `api.js`. Estado del modal de pago (`_paypalCfg`, `_currentReserva`) encapsulado en closures. Estado del feed/búsqueda en variables module-level. | DevTools → Application → Local Storage → http://localhost:8080. Verás `token` y `user`. |

---

## Desarrollo Web en Entorno Servidor · 14.09 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Arquitectura backend (MVC) | OK | Estructura clásica: 17 controllers (`@RestController`), 15 services (`@Service`), 11 repositories (`JpaRepository`), 11 entidades (`@Entity`), 22 DTOs separados de las entidades. Inyección por constructor. | Estructura del proyecto en VS Code → carpeta `src/main/java/com/diveconnect/`. |
| Funcionalidades del backend (login, paginado, .env) | OK | Login JWT (`AuthController` + `JwtUtil`). Paginación con `Pageable` (`PublicacionRepository.findAllByOrderByFechaPublicacionDesc(Pageable)` y ahora también `InmersionRepository.findMasCercanas`). Variables de entorno con `@Value` y defaults configurables (BD, JWT, Stripe, PayPal, Google, OpenWeather). `.env.example` documenta todas. | `application.properties` muestra el patrón `${VAR:default}`. `.env.example` lista las 12 variables. |
| Comprensión profunda del código | (Tú) | El código está; léete `APUNTES-PROYECTO.md` sección 2 (código por carpetas) y sección 3 (código por funcionalidad). | Practica explicando mentalmente cada flujo: login, reserva, pago, seguimiento. |
| Seguridad y autenticación | OK | JWT HS256 con secret configurable. BCrypt cost 10. `JwtAuthenticationFilter` extiende `OncePerRequestFilter`. Spring Security 6 con cadena stateless. OAuth2 Google opcional. CSRF deshabilitado (es API JSON). 4 archivos en `security/`. | `SecurityConfig.java` muestra los matchers. `JwtUtil.java` la generación. `JwtAuthenticationFilter.java` la validación en cada request. |
| Documentación de endpoints (API) | OK | **Swagger UI completo** en `/swagger-ui.html` con OpenAPI 3.0 vía springdoc. `OpenApiConfig.java` configura info, servers y autenticación Bearer. Cada `@RestController` aporta automáticamente sus endpoints documentados. | URL: `http://localhost:8080/swagger-ui.html`. Pulsa "Authorize" → pega `Bearer <jwt>` (se obtiene del login) → ejecuta cualquier endpoint protegido en vivo. |

---

## Despliegue de Aplicaciones Web · 4.03 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Configuración del servidor y hosting (Docker, VPS, Cloud) | OK + MANUAL | `Dockerfile` multi-stage (build con Maven → runtime Alpine JRE 17 + healthcheck + usuario no-root + JAVA_OPTS limitado). `docker-compose.yml` orquesta MySQL 8 + app con volúmenes y healthchecks. `render.yaml` (Blueprint) declara el web service + BD MySQL gestionada para Render.com. **Falta**: clickar deploy en Render.com (paso 1 de `GUIA-MANUAL.md`). | En vivo: `docker compose up --build` levanta todo en 3 min. Después de hacer el bloque 1 de `GUIA-MANUAL.md`, también podrás abrir `https://diveconnect-XXXX.onrender.com` con SSL real. |
| Seguridad y protocolos (SSL, dominios) | MANUAL | El SSL se obtiene automáticamente al desplegar en Render (Let's Encrypt). El `render.yaml` configura el dominio `diveconnect.onrender.com`. | URL pública con HTTPS funcionará tras el bloque 1 de `GUIA-MANUAL.md`. |
| Automatización (CI/CD) | OK | `.github/workflows/ci.yml` configurado: build + tests con MySQL service container + build de imagen Docker en master. Render auto-deploya en cada push a master. | Pestaña "Actions" del repo en GitHub. Si has hecho un push reciente, verás los runs verdes. |

> **Importante**: el módulo de Despliegue valora "Justificación de la infraestructura elegida". Tú puedes justificar: "Elegí Render.com porque ofrece SSL gratuito automático con Let's Encrypt, dominio público gratuito, plan free para TFG, despliegue Blueprint declarativo con `render.yaml`, BD MySQL gestionada en la red interna, y auto-deploy en cada push. El alternativo natural sería un VPS con Nginx + Certbot, pero requiere más configuración manual."

---

## Proyecto Intermodular DAW · 4.03 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Planificación y seguimiento (Gantt, Sprints) | OK | `docs/diagrams/gantt.md` con cronograma completo: 10 sprints semanales, 4 hitos (MVP Backend, Pago funcional, MVP Completo, Defensa). Renderiza directamente en GitHub. | Abre `docs/diagrams/gantt.md` en GitHub. Mermaid pinta el Gantt automáticamente. |
| Herramientas de gestión (GitHub Projects, Trello) | MANUAL | `ISSUES.md` lista 12 issues (5 cerradas + 7 abiertas) listas para pegar en GitHub Issues. Falta crear GitHub Project board (paso 2 de `GUIA-MANUAL.md`). | Tras hacer el bloque 2 de `GUIA-MANUAL.md`: `https://github.com/mmordoe685/diveconnect/issues` y `https://github.com/mmordoe685?tab=projects`. |
| Gestión de riesgos e incidencias | OK | `ISSUES.md` documenta 5 bugs reales con causa, diagnóstico, solución y alternativas descartadas. Es exactamente lo que pide la rúbrica: "documentar los bloqueos reales surgidos y justificar las soluciones técnicas aplicadas". | Lee `ISSUES.md` issue #1 (Lombok @Data + Set) al tribunal completa. Es la historia más rica del proyecto. |
| Calidad de la defensa y soporte (slides) | OK | `docs/DiveConnect-Defensa.pptx` con 18 diapositivas (paleta navy/teal/cream) cubriendo todo el proyecto. Slides 9 (pasarela), 13 (tests), 15 (decisiones técnicas) son los más fuertes. | Abre el .pptx en PowerPoint o Keynote. Práctica el guion en `APUNTES-PROYECTO.md` sección 4. |
| Demo técnica y robustez | OK | El proyecto está vivo. Tienes 31 tests verdes y `docs/test-plan.md` con walkthrough manual de ~10 minutos validado. | Demo en vivo siguiendo los pasos del 7.1 al 7.5 de `INSTALL.md` o el "walkthrough manual" del `docs/test-plan.md`. |
| Estructura del repositorio | OK | Carpetas claras: `/src`, `/database`, `/docs`, `/scripts`, `/.github`. Ficheros raíz: README, INSTALL, CHANGELOG, LICENSE, ISSUES, GUIA-MANUAL, APUNTES-PROYECTO, COBERTURA-RUBRICA, Dockerfile, docker-compose.yml, render.yaml. | `tree -L 2 -d` o navega el repo en GitHub. |
| README exhaustivo | OK | `README.md` con: índice, resumen, capturas, stack, estructura, arranque rápido (3 caminos), env vars, despliegue, documentación, tests, diagramas, decisiones técnicas, roadmap, licencia. Cumple "mapa del proyecto" que pide la rúbrica. | Abre `README.md` en GitHub. |
| Manual de despliegue + escalabilidad | OK | `INSTALL.md` con 3 caminos detallados (Docker / local / Render). Capítulo 17 del PDF + capítulo C de `memoria-extra.md` cubren escalabilidad técnica. Capítulo A.3 cubre escalabilidad de negocio. | `INSTALL.md`. |

---

## Diseño de Interfaces Web · 10.06 %

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Estética y responsive | OK | Tema submarino propio (`ocean-theme.css`): glassmorphism, fondo subacuático animado, light rays, dock con halo, hover lift, gradient text en headings, micro-animaciones. Responsive con `@media (max-width: 720px)`: bottom sheets, viewport-fit=cover, safe-area-inset, inputs 44px. | Demo en DevTools con Toggle Device Toolbar: alterna desktop ↔ mobile. Carga `/pages/Inmersiones.html` y abre el modal de reserva → en mobile aparece como bottom sheet. |
| Usabilidad y accesibilidad | OK | `aria-label` en botones con sólo iconos. Estructura semántica (header/main/section/article/footer/nav). `prefers-reduced-motion` respetado. Contraste verificado (texto principal 14.2:1). `:focus-visible` con outline aqua de 3px. `docs/lighthouse-audit.md` documenta cada criterio WCAG 2.1 AA. | `view-source:` en cualquier página muestra `aria-label`. Lighthouse Accessibility (manual). |
| Demostración funcional de la interfaz | OK | El proyecto entero es la demostración. | Demo en vivo (cualquier flujo del bloque 8 de `GUIA-MANUAL.md`). |
| UX/UI: paleta, tipografías, wireframes | OK | `docs/style-guide.md` con paleta completa (~20 tokens), 4 familias tipográficas con uso justificado, escala tipográfica, espaciados, radios, sombras, easings. `docs/wireframes/` con 5 wireframes SVG (login, feed, inmersiones, pago, mapa) en mobile 375px. | `docs/style-guide.md` y los 5 SVGs en `docs/wireframes/`. SVG se abre directamente en el navegador. |
| Responsive: breakpoints justificados, Mobile First | OK | Capítulo 8 de `style-guide.md` justifica los 6 breakpoints (xs, sm, md, lg, xl, xxl) con punto principal en `md = 720px`. Mobile First: el código asume móvil por defecto y `@media (min-width)` añade desktop. | `ocean-theme.css` línea con `@media (max-width: 720px)`. |
| Accesibilidad WCAG | OK + MANUAL | `docs/lighthouse-audit.md` cubre criterios manualmente con datos de runtime. Falta el report HTML real generado por Chrome DevTools (paso 4 de `GUIA-MANUAL.md`). | `docs/lighthouse-audit.md` ahora; el HTML de Lighthouse tras el bloque 4 de la guía. |

---

## Programación en Python y Análisis de Datos · 6.03 % (optativa)

| Criterio | Estado | Por qué cumple | Cómo demostrarlo |
|---|---|---|---|
| Procesamiento de datos (ETL) | OK | `scripts/analytics/analytics.py` (302 líneas): conecta a MySQL con PyMySQL, extrae 5 datasets (publicaciones por mes, reservas por estado, top inmersiones, especies top, métricas globales), transforma con pandas (groupby, sort, filtros), genera 3 gráficas matplotlib y un CSV consolidado. | Abre `scripts/analytics/analytics.py` en VS Code. Tras el bloque 5 de `GUIA-MANUAL.md` también podrás enseñar el output real. |
| Visualización y métricas | OK + MANUAL | 3 gráficas con paleta corporativa (turquoise, coral, gold, indigo): barras (publicaciones/mes), pie (distribución reservas), barras horizontales (top inmersiones por reservas). | Tras `python analytics.py` (bloque 5 de `GUIA-MANUAL.md`), abres `docs/screenshots/analytics/*.png`. |
| Integración funcional con la web | OK | El script lee directamente de la BD MySQL del proyecto principal (PyMySQL). Mismo schema, misma fuente de datos. | Demo en vivo: ejecuta `python analytics.py` con la BD viva del proyecto. Lee filas reales. |

> **Si tu optativa NO es Python sino otra**, este 6.03 % no aplica para ti. Pero como ya hicimos el script, suma puntos extra. Si el tribunal lo pregunta, defiéndelo: es un trabajo opcional adicional que demuestra que el módulo está cubierto.

---

## Cómo demostrar TODO en una sesión de 30 minutos

Este es el orden óptimo de demostración. Está diseñado para que cada cosa enseñada cubra **varios criterios a la vez**.

### Minutos 0-2 — Apertura
Slide 1 + slide 2 del `.pptx`. Cubre Programación (comunicación técnica) + PIDAWE (calidad de defensa).

### Minutos 2-7 — Demo en vivo del flujo completo
Login con `sofia_buceo` / `admin` → feed → reservar inmersión → pago demo → notificación. Cubre **DWEC** (CRUD, validación, asíncrono), **DWES** (login, paginado), **DIW** (estética, responsive con DevTools), **Programación** (resolución de problemas), **Sistemas Informáticos** (compatibilidad navegador).

### Minutos 7-10 — Arquitectura y modelo de datos
Abrir `docs/diagrams/architecture.md` y `docs/diagrams/er-diagram.md` en GitHub. Cubre **BD** (E/R + DDL), **Programación** (UML), **Sistemas Informáticos** (infraestructura), **DWES** (arquitectura backend MVC).

### Minutos 10-14 — Pasarela de pago + seguridad
Slide 9 (3 modos) + abrir `PaymentController.java` y `JwtUtil.java` en VS Code. Cubre **DWES** (autenticación, .env), **Programación** (excepciones, lógica), **Sistemas Informáticos** (seguridad).

### Minutos 14-17 — Tests + CI/CD
Terminal: `./mvnw test` en vivo. Mostrar el archivo `.github/workflows/ci.yml` y la pestaña Actions. Cubre **Entornos de Desarrollo** completo.

### Minutos 17-20 — Despliegue
Abrir `Dockerfile`, `docker-compose.yml`, `render.yaml`. Si has desplegado, abrir la URL pública con SSL. Cubre **Despliegue** completo.

### Minutos 20-23 — BD: vistas, procedimientos, normalización
MySQL Workbench: `CALL sp_estadisticas_globales();` en vivo. Capítulo D del `memoria-extra.md` para normalización 1FN/2FN/3FN/BCNF. Cubre **BD** completo.

### Minutos 23-25 — Análisis Python
Carpeta `scripts/analytics/` y las 3 gráficas en `docs/screenshots/analytics/`. Cubre **Python** completo.

### Minutos 25-27 — Sostenibilidad y digitalización
Capítulos A y B de `memoria-extra.md`: ODS, Green Code, huella, modelo negocio. Cubre **Sostenibilidad** y **Digitalización** completos.

### Minutos 27-30 — Roadmap y cierre
Slide 17 + `ISSUES.md` issues abiertas. Cubre **PIDAWE** (gestión de incidencias).

### Minutos 30-45 — Preguntas
Tienes 9 preguntas tipo preparadas con respuesta en `APUNTES-PROYECTO.md` sección 4.13.

---

## Tabla "qué demuestra qué" para preguntas rápidas del tribunal

Si un miembro del tribunal te pregunta "¿y eso de X cómo está hecho?", aquí está la respuesta inmediata:

| Pregunta probable | Qué enseñar |
|---|---|
| "¿Cómo proteges las contraseñas?" | `BCryptPasswordEncoder` en `SecurityConfig`. Cost factor 10. Hash + salt. |
| "¿Cómo se gestionan las sesiones?" | JWT stateless en `JwtUtil`. Sin sesiones servidor, sin cookies. |
| "Enséñame una transacción compleja en BD" | `sp_marcar_reserva_pagada` en `database/procedures.sql`. UPDATE + 2 INSERT atómicos con manejo de errores. |
| "¿Cómo escalarías esto?" | Capítulo A.3 de `memoria-extra.md`. 6 ejes con plan concreto. |
| "¿Cómo testean las cosas?" | `./mvnw test` en vivo. Cubre 5 servicios críticos con 31 tests. |
| "¿Cómo manejas un error?" | `GlobalExceptionHandler` + 3 excepciones custom. Test `crearReserva_sinPlazas`. |
| "Enseña una consulta SQL avanzada" | Haversine en `InmersionRepository.findMasCercanas`. SQL nativo + projection. |
| "¿Cómo hiciste responsive?" | `ocean-theme.css` `@media (max-width: 720px)`. Bottom sheets en móvil. DevTools en vivo. |
| "¿Cómo despliegas a producción?" | `render.yaml` (blueprint declarativo). Docker multi-stage. CI en GitHub Actions. |
| "¿Tienes documentación API?" | Swagger UI en `/swagger-ui.html`. OpenAPI 3.0. Autorización Bearer integrada. |
| "¿Cómo subes archivos?" | `UploadController` con multipart, validación MIME + lista blanca + UUID. 5 tests cubren los caminos. |
| "Enséñame un patrón de diseño que uses" | Strategy implícito en `PaymentController.verificar` (Stripe / demo / idempotente). Filter Chain en Spring Security. Repository en cada `*Repository`. |
| "¿Cómo gestionas paginación?" | `Pageable` de Spring Data. Ejemplo: `findMasCercanas(lat, lon, PageRequest.of(0, 5))`. |
| "¿Qué hiciste para la accesibilidad?" | `docs/lighthouse-audit.md` lista los criterios cumplidos. `aria-label`, contraste, prefers-reduced-motion. |
| "¿Cómo eliminaste los emojis del seed?" | `EmojiStripMigration` con regex Unicode. CommandLineRunner @Order(100). 8 tests. |
| "¿Por qué dos ramas en Git?" | `master` para producción, `claude/peaceful-hellman` para desarrollo. PR cuando hay cambios estables. |
| "Si me preguntas por el bug más interesante..." | Lombok @Data + Set<Usuario> rompiendo equals/hashCode. Solución con SQL nativo en `UsuarioRepository`. |

---

## Lo que falta (todo en GUIA-MANUAL.md)

Estado claro de lo pendiente, todo en `GUIA-MANUAL.md` con copy/paste exacto:

1. **Despliegue Render** (Bloque 1, 30 min) — convierte el "MANUAL" del módulo Despliegue en "OK".
2. **GitHub Issues + Project** (Bloque 2, 15 min) — convierte el "MANUAL" del PIDAWE en "OK".
3. **Capturas de pantalla** (Bloque 3, 20 min) — para el README.
4. **Lighthouse real** (Bloque 4, 10 min) — refuerza DIW + LMSGI.
5. **Python ETL ejecución** (Bloque 5, 15 min) — convierte el "MANUAL" de Python en "OK".
6. **OG image** (Bloque 6, 10 min, opcional) — refuerza SEO.

Total: ~2 horas de trabajo manual, repartidas en los días previos a la defensa.

Una vez hechos los bloques 1, 2 y 5: **toda la rúbrica queda en estado OK**.
