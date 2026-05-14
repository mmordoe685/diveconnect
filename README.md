# DiveConnect

Red social y plataforma de reservas para la comunidad submarinista. Une el descubrimiento social (publicaciones, historias 24 h, seguir buceadores) con un marketplace de inmersiones donde los centros publican y los buceadores reservan y pagan en línea.

> Trabajo de Fin de Grado · CFGS Desarrollo de Aplicaciones Web (DAW) · Curso 2025/26

[![Build](https://github.com/mmordoe685/diveconnect/actions/workflows/ci.yml/badge.svg)](https://github.com/mmordoe685/diveconnect/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## Índice

- [Resumen](#resumen)
- [Evidencias visuales](#evidencias-visuales)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Arranque rápido (local)](#arranque-rápido-local)
- [Variables de entorno](#variables-de-entorno)
- [Despliegue](#despliegue)
- [Documentación](#documentación)
- [Tests](#tests)
- [Diagramas](#diagramas)
- [Decisiones técnicas](#decisiones-técnicas)
- [Roadmap](#roadmap)
- [Licencia](#licencia)

---

## Resumen

DiveConnect es una aplicación web full-stack que combina dos productos:

1. **Red social vertical para buceadores**: publicaciones con datos técnicos (profundidad, temperatura, visibilidad, especies vistas), historias efímeras de 24 h, comentarios, likes, sistema de seguimiento con solicitudes y notificaciones en tiempo real.
2. **Marketplace de centros de buceo**: catálogo de inmersiones filtrable por nivel, precio y profundidad, reservas con descuento automático de plazas, y pago en línea a través de Stripe Checkout o PayPal (sandbox y live).

### Tipos de usuario

| Rol | Capacidades |
|---|---|
| `USUARIO_COMUN` | Publica, sigue, reserva, paga, lleva su diario de inmersiones. |
| `USUARIO_EMPRESA` | Crea su centro de buceo, gestiona inmersiones, recibe reservas. |
| `ADMINISTRADOR` | Modera contenido y gestiona usuarios desde `/pages/admin`. |

### Funcionalidades destacadas

- Login con email + contraseña o Google OAuth2.
- Feed cronológico estilo Instagram + historias 24 h.
- Subida de imágenes y vídeos desde galería o cámara del móvil.
- Mapa interactivo con Leaflet + tiempo atmosférico actual (OpenWeatherMap).
- Búsqueda universal con fallback a proximidad geográfica (Haversine).
- Pasarela de pago en tres modos: demo TFG, sandbox Stripe/PayPal, live.
- Diseño responsive con bottom-sheets, glassmorphism y fondo subacuático animado.
- Notificaciones con badge en topbar y página dedicada.
- Sistema de seguimiento con solicitudes pendientes y aceptación.

---

## Evidencias visuales

| Evidencia | Ubicación |
|---|---|
| Wireframes principales | [`docs/wireframes/`](docs/wireframes/) |
| Guía visual | [`docs/style-guide.md`](docs/style-guide.md) |
| Auditoría accesibilidad/SEO | [`docs/lighthouse-audit.md`](docs/lighthouse-audit.md) |
| Imagen Open Graph | [`src/main/resources/static/images/og-cover.png`](src/main/resources/static/images/og-cover.png) |

Las capturas finales de la defensa deben generarse en `docs/screenshots/` cuando la app esté arrancada o desplegada.

---

## Stack tecnológico

### Back-end
- **Java 17+** (compatible con 21 y 25)
- **Spring Boot 3.2.3** — autoconfiguración, Tomcat embebido, MVC.
- **Spring Security 6** — filtros stateless, JWT (HS256), OAuth2 client (Google).
- **Spring Data JPA** + **Hibernate** — repositorios derivados y queries nativas.
- **MySQL 8** (InnoDB) con `ddl-auto=update`.
- **Lombok 1.18** — reducción de boilerplate.
- **jjwt 0.11.5** — generación y validación de tokens.
- **BCryptPasswordEncoder** — hash de contraseñas.
- **Stripe Java SDK** + **PayPal REST v2** (cliente HTTP nativo, sin SDK añadido).
- **springdoc-openapi** — Swagger UI en `/swagger-ui.html`.

### Front-end
- HTML5 + CSS3 + JavaScript ES2020, **sin framework**.
- Leaflet 1.9 para el mapa.
- DM Serif Display + Nunito Sans (Google Fonts).
- Capa de tema submarino propia (`ocean-theme.css` + `ocean-effects.js`): glassmorphism, fondo animado, burbujas, light rays, micro-animaciones.

### Datos / análisis (módulo Python optativo)
- **Python 3.10+** con `pandas` + `matplotlib` para ETL y gráficas.
- Script en [`scripts/analytics/`](scripts/analytics/) genera dashboard estático de métricas.

### Infraestructura y herramientas
- Maven 3.9 + wrapper (`mvnw` / `mvnw.cmd`).
- Docker + docker-compose para entorno local reproducible.
- Render.com (PaaS gratuito con SSL) para producción.
- GitHub Actions para CI (build + test).
- JUnit 5 + Mockito para tests.
- Lighthouse (Chrome DevTools) para auditoría de accesibilidad y SEO.

---

## Estructura del repositorio

```
diveconnect/
├── .github/workflows/         # CI: GitHub Actions
├── database/                  # Schema SQL, vistas, procedimientos
│   ├── schema.sql
│   ├── views.sql
│   └── procedures.sql
├── docs/                      # Diagramas, pruebas, estilo y anexos
│   ├── test-plan.md
│   ├── memoria-extra.md
│   ├── diagrams/
│   │   ├── er-diagram.md      # Diagrama Entidad-Relación (Mermaid)
│   │   ├── class-diagram.md   # Diagrama de clases UML
│   │   ├── gantt.md           # Cronograma del proyecto
│   │   └── architecture.md    # Diagrama de arquitectura
│   ├── wireframes/            # Wireframes SVG
│   ├── style-guide.md         # Paleta, tipografías, componentes
│   └── screenshots/           # Capturas finales de la defensa
├── scripts/
│   └── analytics/             # ETL Python + dashboard
├── src/
│   ├── main/
│   │   ├── java/com/diveconnect/
│   │   │   ├── DiveconnectApplication.java
│   │   │   ├── config/        # SecurityConfig, WebConfig, DataInitializer, ...
│   │   │   ├── controller/    # REST endpoints
│   │   │   ├── service/       # Lógica de negocio
│   │   │   ├── repository/    # Spring Data JPA
│   │   │   ├── entity/        # Modelo de dominio
│   │   │   ├── dto/           # Request / Response
│   │   │   ├── security/      # JwtAuthFilter, handlers OAuth2
│   │   │   └── exception/     # Excepciones + ControllerAdvice
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/        # Frontend completo
│   │           ├── pages/     # HTML
│   │           ├── css/       # style.css + ocean-theme.css
│   │           ├── js/        # api, auth, nav, payment, ocean-effects
│   │           └── images/
│   └── test/                  # Tests JUnit + Mockito
├── uploads/                   # Archivos subidos por usuarios (gitignored)
├── Dockerfile
├── docker-compose.yml
├── render.yaml                # Configuración de despliegue Render
├── pom.xml
├── README.md                  # Este fichero
├── MEMORIA-DIVECONNECT.md     # Memoria tecnica
├── COBERTURA-RUBRICA.md       # Matriz de criterios PIDAWE
├── GUIA-MANUAL.md             # Manual de uso y despliegue
├── GUION-DEFENSA.md           # Guion para la exposicion
├── CHANGELOG.md
├── INSTALL.md                 # Manual paso a paso
├── ISSUES.md                  # Backlog para GitHub Issues
└── LICENSE
```

---

## Arranque rápido (local)

### Prerequisitos
- Java 17 o superior
- MySQL 8 corriendo en `localhost:3306`
- Maven NO necesario (wrapper incluido)
- Docker (opcional, para entorno aislado)

### Opción A — con Docker (recomendado)

```bash
git clone https://github.com/mmordoe685/diveconnect.git
cd diveconnect
docker-compose up --build
```

La app queda disponible en `http://localhost:8080`. MySQL se levanta en un contenedor con la base creada automáticamente.

### Opción B — sin Docker

1. Crear la base de datos:
   ```sql
   CREATE DATABASE diveconnect_db CHARACTER SET utf8mb4;
   CREATE USER 'diveconnect_user'@'localhost' IDENTIFIED BY 'DiveConnect2025!';
   GRANT ALL ON diveconnect_db.* TO 'diveconnect_user'@'localhost';
   ```

2. Aplicar el schema (opcional — Hibernate lo crea solo, pero si prefieres SQL explícito):
   ```bash
   mysql -u diveconnect_user -p diveconnect_db < database/schema.sql
   mysql -u diveconnect_user -p diveconnect_db < database/views.sql
   mysql -u diveconnect_user -p diveconnect_db < database/procedures.sql
   ```

3. Arrancar la app:
   ```bash
   ./mvnw spring-boot:run        # macOS / Linux
   mvnw.cmd spring-boot:run      # Windows
   ```

4. Abrir `http://localhost:8080`.

### Usuarios de prueba (seed)

| Usuario | Contraseña | Tipo |
|---|---|---|
| `admin` | `admin` | ADMINISTRADOR |
| `oceandive` | `admin` | EMPRESA |
| `blueworld` | `admin` | EMPRESA |
| `sofia_buceo` | `admin` | COMUN |
| `pablo_oc` | `admin` | COMUN |
| `marinalopez` | `admin` | COMUN |

---

## Variables de entorno

Todas son **opcionales** salvo `DB_USERNAME` / `DB_PASSWORD`. Sin las de Stripe/PayPal/Google la app funciona en modo demo.

| Variable | Default | Uso |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/diveconnect_db...` | URL JDBC de MySQL |
| `DB_USERNAME` | `diveconnect_user` | Usuario MySQL |
| `DB_PASSWORD` | `DiveConnect2025!` | Contraseña MySQL |
| `JWT_SECRET` | (32+ caracteres por defecto) | Firma de tokens — cambiar en producción |
| `STRIPE_SECRET_KEY` | (vacío) | `sk_test_...` o `sk_live_...` |
| `STRIPE_PUBLISHABLE_KEY` | (vacío) | `pk_test_...` o `pk_live_...` |
| `PAYPAL_CLIENT_ID` | (vacío) | Sandbox o live |
| `PAYPAL_CLIENT_SECRET` | (vacío) | Sandbox o live |
| `PAYPAL_MODE` | `sandbox` | `sandbox` o `live` |
| `GOOGLE_CLIENT_ID` | (placeholder) | OAuth2 |
| `GOOGLE_CLIENT_SECRET` | (placeholder) | OAuth2 |
| `GOOGLE_OAUTH_ENABLED` | `false` | `true` para activar el botón Google |
| `OPENWEATHER_API_KEY` | (vacío) | Si falta, `/api/weather` devuelve mock |
| `FRONTEND_URL` | `http://localhost:8080` | Para los `success_url` de Stripe |
| `CORS_ALLOWED_ORIGINS` | `*` | Orígenes permitidos para `/api/**` |
| `JPA_DDL_AUTO` | `update` | Estrategia Hibernate (`update`, `validate`, etc.) |
| `JPA_SHOW_SQL` | `false` | Mostrar SQL en logs |
| `APP_LOG_LEVEL` | `INFO` | Nivel de logs de `com.diveconnect` |
| `HIBERNATE_SQL_LOG_LEVEL` | `WARN` | Nivel de logs SQL de Hibernate |

Plantilla en [`.env.example`](.env.example).

---

## Despliegue

### Render.com (gratuito con SSL)
1. Forkear este repo.
2. En [render.com](https://render.com) → "New +" → "Blueprint" → conectar repositorio.
3. Render detecta `render.yaml` y crea el servicio web Docker.
4. Definir las variables de entorno del paso anterior en el dashboard de Render, incluida una base MySQL externa (`SPRING_DATASOURCE_URL`, `DB_USERNAME`, `DB_PASSWORD`).
5. La app queda en `https://<tu-servicio>.onrender.com` con SSL activo.

### Docker (cualquier proveedor)
```bash
docker build -t diveconnect:latest .
docker run -p 8080:8080 \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/diveconnect_db \
  diveconnect:latest
```

Detalles paso a paso en [`INSTALL.md`](INSTALL.md).

---

## Documentación

| Documento | Contenido |
|---|---|
| [`MEMORIA-DIVECONNECT.md`](MEMORIA-DIVECONNECT.md) | Memoria técnica integral del proyecto |
| [`COBERTURA-RUBRICA.md`](COBERTURA-RUBRICA.md) | Trazabilidad directa con los criterios PIDAWE |
| [`GUIA-MANUAL.md`](GUIA-MANUAL.md) | Manual de instalación, uso y despliegue |
| [`GUION-DEFENSA.md`](GUION-DEFENSA.md) | Guion de exposición y demo ante tribunal |
| [`docs/test-plan.md`](docs/test-plan.md) | Plan de pruebas automático y manual |
| [`docs/diagrams/er-diagram.md`](docs/diagrams/er-diagram.md) | Diagrama E/R en Mermaid + DSL para dbdiagram.io |
| [`docs/diagrams/class-diagram.md`](docs/diagrams/class-diagram.md) | Diagrama de clases UML |
| [`docs/diagrams/architecture.md`](docs/diagrams/architecture.md) | Vista de arquitectura por capas |
| [`docs/diagrams/gantt.md`](docs/diagrams/gantt.md) | Cronograma con sprints y hitos |
| [`docs/style-guide.md`](docs/style-guide.md) | Paleta, tipografías, componentes UI |
| [`docs/wireframes/`](docs/wireframes/) | Wireframes SVG de las pantallas principales |
| [`CHANGELOG.md`](CHANGELOG.md) | Histórico de versiones |
| [Swagger UI](http://localhost:8080/swagger-ui.html) | API REST interactiva (en local tras arrancar) |

---

## Tests

```bash
./mvnw test                    # ejecutar todos
./mvnw test -Dtest=ReservaServiceTest   # un test concreto
```

Cobertura objetivo: 60% en servicios críticos. Ver [`docs/test-plan.md`](docs/test-plan.md) para casos de prueba documentados.

---

## Diagramas

Los siguientes diagramas se renderizan automáticamente en GitHub gracias a Mermaid:

- [Modelo Entidad-Relación](docs/diagrams/er-diagram.md)
- [Diagrama de clases](docs/diagrams/class-diagram.md)
- [Arquitectura por capas](docs/diagrams/architecture.md)
- [Cronograma Gantt](docs/diagrams/gantt.md)

---

## Decisiones técnicas

Las más relevantes están razonadas en [`MEMORIA-DIVECONNECT.md`](MEMORIA-DIVECONNECT.md), [`APUNTES-PROYECTO.md`](APUNTES-PROYECTO.md) y [`COBERTURA-RUBRICA.md`](COBERTURA-RUBRICA.md). En resumen:

- **Lombok `@Data` + `Set<Entity>` en JPA**: rompe `equals/hashCode`. Solución: queries nativas en `UsuarioRepository.existsSeguimiento`.
- **Pasarela demo + sandbox + live en un solo flujo**: el frontend detecta automáticamente la configuración del backend.
- **Frontend sin framework**: decisión consciente para demostrar dominio de DOM/CSS sin abstracciones.
- **Bottom sheets en móvil**: patrón nativo iOS/Android, una sola `@media` query lo activa.
- **SQL nativo + Haversine**: la fórmula no es traducible a JPQL puro y es self-contained.
- **Subida de archivos local**: `uploads/` en disco. En producción real se migraría a S3.

---

## Roadmap

Funcionalidades MVP completadas. Próximas iteraciones:

- [ ] Migración de uploads a almacenamiento de objetos (S3 / R2).
- [ ] Refresh tokens para sesiones más largas con seguridad.
- [ ] WebSockets para notificaciones en tiempo real (en lugar de poll).
- [ ] Internacionalización (i18n): inglés y portugués.
- [ ] Tests E2E con Playwright.
- [ ] Recomendador de inmersiones basado en historial del usuario.

---

## Licencia

[MIT](LICENSE) © 2026 Marcos Mordoñez Estévez

Las imágenes ilustrativas son de Unsplash bajo su licencia gratuita. Los iconos SVG son originales del proyecto. Las marcas Stripe, PayPal y Google son propiedad de sus respectivos titulares.

---

> Hecho con cariño desde Alicante, escuchando ruido de bombonas y sonido de mar.
