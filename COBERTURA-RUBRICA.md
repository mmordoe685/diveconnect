# Cobertura de la rubrica PIDAWE

Este documento relaciona los criterios de la rubrica PIDAWE 2o DAW (curso 2025/26) con evidencias concretas del repositorio. Sirve como mapa para la memoria, la defensa oral y la revision del tribunal.

## Resumen de estado

| Bloque rubrica | Estado | Evidencias principales |
|---|---|---|
| Planificacion y seguimiento | Cubierto | `docs/diagrams/gantt.md`, `CHANGELOG.md`, `ISSUES.md` |
| Calidad tecnica y viabilidad | Cubierto | `src/main/java/`, `database/`, `pom.xml`, `Dockerfile` |
| Documentacion completa | Cubierto | `README.md`, `MEMORIA-DIVECONNECT.md`, `INSTALL.md`, `docs/` |
| Defensa y demo | Cubierto | `GUION-DEFENSA.md`, usuarios seed, flujos CRUD/pago/mapa |
| Tests y robustez | Cubierto | `src/test/java/`, `docs/test-plan.md`, GitHub Actions CI |

## Matriz por modulo (Memoria tecnica)

### 1o DAW

| Modulo | Criterio | Evidencia |
|---|---|---|
| Bases de datos | Modelado E/R y normalizacion 3FN | `docs/diagrams/er-diagram.md`, `MEMORIA-DIVECONNECT.md` §7.1 |
| Bases de datos | DDL, DML, vistas y procedimientos | `database/schema.sql`, `database/views.sql`, `database/procedures.sql` |
| Bases de datos | PKs, FKs, triggers e indices | DDL + trigger `trg_reservas_ultima_modificacion`, indices compuestos en `procedures.sql` |
| Digitalizacion | Transformacion del sector | `MEMORIA-DIVECONNECT.md` §4 (modelo de negocio) |
| Digitalizacion | Tecnologias habilitadoras | ETL Python, OpenWeatherMap, pagos online, mapa interactivo |
| Digitalizacion | Modelo de negocio digital | `MEMORIA-DIVECONNECT.md` §4: comisiones, plan premium, freemium |
| Sostenibilidad | Green Code | Paginacion, indices, `@Scheduled` limpieza, logs reducidos, imagen Docker < 250 MB |
| Sostenibilidad | Impacto social y ODS | `MEMORIA-DIVECONNECT.md` §12.2 (ODS 9, 11, 12, 14) |
| Sostenibilidad | Residuos digitales y mantenimiento | Limpieza programada de historias, purga documentada de notificaciones |
| Entornos de desarrollo | Control de versiones (Gitflow) | `MEMORIA-DIVECONNECT.md` §15.2, historial Git + Conventional Commits |
| Entornos de desarrollo | Plan de pruebas | `docs/test-plan.md`, suite JUnit + Mockito, `mvn test` 31/0/0 |
| Entornos de desarrollo | Optimizacion del entorno | Maven Wrapper, GitHub Actions, Lighthouse, `.editorconfig` |
| Lenguajes de marcas SGI | HTML5 semantico y validado | Etiquetas `header/nav/main/section/article/footer`, `lang="es"` |
| Lenguajes de marcas SGI | Estrategia SEO | `<meta description>`, `og:` tags, `<title>` descriptivo, `og-cover.png` |
| Lenguajes de marcas SGI | XML/JSON intercambio | API REST con JSON via Jackson, paginacion estructurada |
| Programacion | Logica y algoritmia | Haversine en `InmersionRepository.findMasCercanas`, idempotencia en `/verify` |
| Programacion | POO y diagrama de clases | `docs/diagrams/class-diagram.md`, entidades, servicios, DTOs |
| Programacion | Tratamiento de excepciones | `GlobalExceptionHandler`, excepciones especificas de dominio |
| Sistemas informaticos | Infraestructura | `Dockerfile`, `docker-compose.yml`, `render.yaml`, esquema en memoria §11 |
| Sistemas informaticos | Configuracion de red | Puerto 8080, MySQL 3306, variables `.env`, CORS por entorno |
| Sistemas informaticos | Seguridad del sistema | Usuario no-root Docker, JWT + BCrypt, variables seguras, healthcheck |

### 2o DAW

| Modulo | Criterio | Evidencia |
|---|---|---|
| Desarrollo Web Cliente | Interactividad DOM y CRUD | `static/js/`, paginas feed/reservas/perfil/admin/empresa |
| Desarrollo Web Cliente | Consumo asincrono | `api.js` con `async/await` y manejo de errores |
| Desarrollo Web Cliente | Gestion de estado | JWT en `localStorage`, estado efimero en memoria, debounce en busqueda |
| Despliegue | Hosting y servidor | Render.com + Docker, justificacion en `MEMORIA-DIVECONNECT.md` §11.1 |
| Despliegue | SSL/HTTPS | Let's Encrypt automatico en Render, redireccion HTTP→HTTPS |
| Despliegue | CI/CD | `.github/workflows/ci.yml`, auto-deploy Render desde master |
| Desarrollo Web Servidor | Arquitectura MVC | Capas `controller/service/repository/entity/dto` en `src/main/java/` |
| Desarrollo Web Servidor | Seguridad y autenticacion | `SecurityConfig`, `JwtAuthenticationFilter`, BCrypt, roles |
| Desarrollo Web Servidor | API REST documentada | Swagger UI en `/swagger-ui.html`, anotaciones springdoc |
| PIDAWE | Gestion ciclo de vida (Gantt/Sprints) | `docs/diagrams/gantt.md`, hitos cumplidos, historial Git cronologico |
| PIDAWE | Estructura del repositorio | `src/`, `docs/`, `database/`, `scripts/`, `uploads/`, raiz limpia |
| PIDAWE | README como mapa | `README.md` con secciones, comandos y rutas a memoria/codigo |
| PIDAWE | Gestion incidencias y robustez | `ISSUES.md`, `CHANGELOG.md`, decisiones documentadas |
| PIDAWE | Manual de despliegue y escalabilidad | `INSTALL.md`, reflexion en memoria §11 y §17 |
| Diseno de Interfaces | UX/UI y guia de estilo | `docs/style-guide.md`, `docs/wireframes/`, paleta submarina |
| Diseno de Interfaces | Responsive Mobile First | `style.css`, `ocean-theme.css`, breakpoints 480/768/1024 |
| Diseno de Interfaces | Accesibilidad WCAG | `docs/lighthouse-audit.md`, `aria-label`, foco visible, contraste |
| Python | Procesamiento de datos (ETL) | `scripts/analytics/analytics.py`, extract→transform→load |
| Python | Visualizacion e informe | Graficas PNG generadas, CSV exportados, referenciadas en memoria |
| Python | Integracion con la web | Conexion via `PyMySQL` a la misma BD; CSV/PNG consumidos como evidencia |

## Matriz por modulo (Defensa oral)

| Modulo | Criterio | Como se cubre en defensa |
|---|---|---|
| Bases de datos | Defender estructura BD | Mostrar diagrama E/R, vistas y consulta Haversine |
| Digitalizacion | Impacto en sector | Slide de propuesta de valor y madurez digital |
| Sostenibilidad | Green Code y huella | Hablar de paginacion, indices, `@Scheduled`, ODS |
| Entornos | Git y pruebas | Mostrar historial, ramas, ejecucion de tests en directo |
| Lenguajes | HTML5 responsive | Demo redimensionando ventana y mostrando dock movil |
| Programacion | Resolucion problemas | Caso de bug Lombok, race condition pago, ahora resuelto |
| Sistemas Inf | Infraestructura | Esquema Render + Docker + MySQL externa |
| Cliente | CRUD en vivo | Demo: registro, publicacion, reserva, comentario, like |
| Servidor | Login y .env | Mostrar `application.properties`, `.env.example`, JWT |
| Despliegue | Hosting + SSL + CI/CD | Mostrar Render dashboard, GitHub Actions verde |
| PIDAWE | Gantt y demo robusta | Cronograma + flujo completo sin errores ante el tribunal |
| Diseno | Estetica y responsive | Tema submarino + redimension + lectura accesible |
| Python | ETL y graficas | Ejecutar script en vivo y abrir graficas PNG |

## Riesgos residuales y plan

| Riesgo | Estado | Plan |
|---|---|---|
| Capturas finales para `docs/screenshots/` | Pendiente | Generar el dia de la demo con la app desplegada |
| Claves reales Stripe/PayPal/Google/OWM | No versionadas | `.env.example`, `INSTALL.md` y `render.yaml` lo documentan |
| Memoria en PDF para entrega formal | A generar | Exportar `MEMORIA-DIVECONNECT.md` el dia de la entrega |
| Cobertura de pruebas e2e | Limitada | Tests JUnit cubren servicios criticos; e2e queda como mejora |
