# Plan de defensa orientado a la rubrica PIDAWE

Guia paso a paso para preparar y ejecutar la defensa apuntando a la maxima puntuacion en cada criterio de la rubrica. Este documento complementa `GUION-DEFENSA.md` (que es el guion narrativo) con un mapa de criterios y un plan de preparacion realista.

---

## 1. Preparacion antes del dia (cronograma)

### D-7 (una semana antes)

- Releer la rubrica completa (`docs/rubrica.pdf` o equivalente) y `COBERTURA-RUBRICA.md`.
- Releer `MEMORIA-DIVECONNECT.md` entera, marcando con boli las secciones que vas a citar en cada criterio.
- Verificar que el repositorio en GitHub se ve limpio: solo rama `master`, README como portada, sin nada raro.
- Hacer un `git pull` en limpio en otra carpeta y comprobar que arranca el proyecto desde cero solo con `INSTALL.md`. Si encuentras pasos confusos, reescribelos.

### D-3 a D-1

- **Ensayar el guion** (`GUION-DEFENSA.md`) en voz alta cronometrando: 10-12 min + 5-8 min de demo + Q&A.
- **Dia ensayo completo**: levantar MySQL, ejecutar `./mvnw clean test`, lanzar la app y ejecutar el flujo demo de principio a fin sin parar.
- **Lighthouse real**: abrir Chrome en `http://localhost:8080/pages/feed.html`, DevTools → Lighthouse → "Analyze page load" en modo mobile. Guardar el PDF resultante en `docs/lighthouse/`. Actualizar `docs/lighthouse-audit.md` con los numeros reales.
- **Ejecutar el ETL de Python** (`python scripts/analytics/analytics.py`) para generar los CSV y los PNG en `docs/screenshots/analytics/`. Abrir los PNG en el navegador o portatil para tenerlos listos.
- **Captures de pantalla**: hacer screenshots de las pantallas clave (feed, inmersiones, reserva, pago demo, panel empresa, panel admin) y guardarlos en `docs/screenshots/`.
- **Slides**: preparar 8-10 diapositivas en Google Slides o LibreOffice Impress con los puntos esenciales. No hace falta diseno premium; cualquier plantilla limpia sirve. Estructura sugerida:
  1. Portada (nombre, autor, fecha).
  2. Problema y propuesta.
  3. Stack y arquitectura.
  4. Modelo E/R.
  5. Capturas del flujo usuario.
  6. Capturas del panel empresa.
  7. Capturas del panel admin.
  8. Tests, CI, Docker, Render.
  9. Sostenibilidad y ODS.
  10. Mejoras futuras y cierre.

### D-day (mismo dia)

- Llegar 30 min antes. Conectar portatil, comprobar HDMI/USB-C, sonido, navegador.
- Tener abierto: terminal con `mvn test` ya ejecutado, navegador con feed.html ya cargado y otra pestana con `swagger-ui.html`.
- Tener el repositorio abierto en GitHub en otra pestana.
- Tener `MEMORIA-DIVECONNECT.md` impresa o en pdf para citar paginas concretas.
- Vaso de agua, modo "no molestar" del movil, respiracion.

---

## 2. Material de apoyo y URLs utiles

| Recurso | Donde |
|---|---|
| Repositorio GitHub | `https://github.com/mmordoe685/diveconnect` |
| Memoria tecnica | `MEMORIA-DIVECONNECT.md` |
| Cobertura rubrica | `COBERTURA-RUBRICA.md` |
| Guion narrativo | `GUION-DEFENSA.md` |
| Wireframes | `docs/wireframes/` |
| Diagramas | `docs/diagrams/` |
| Plan de pruebas | `docs/test-plan.md` |
| Auditoria Lighthouse | `docs/lighthouse-audit.md` |
| Swagger UI local | `http://localhost:8080/swagger-ui.html` |
| Healthcheck local | `http://localhost:8080/actuator/health` |

Usuarios demo (ya inicializados al arrancar):

| Username | Password | Rol |
|---|---|---|
| `admin` | `admin` | ADMINISTRADOR |
| `oceandive` | `admin` | USUARIO_EMPRESA (centro de buceo) |
| `sofia_buceo` | `admin` | USUARIO_COMUN |

---

## 3. Estructura de la defensa (15-18 min total)

| Bloque | Tiempo | Contenido | Material |
|---|---|---|---|
| Apertura | 1 min | Quien soy + nombre proyecto + problema que resuelve | Slide 1-2 |
| Objetivos y alcance | 1 min | Objetivo general + 4-5 especificos + lo que no entra | Slide 2-3 |
| Arquitectura | 2 min | Diagrama capas + stack + decisiones clave | Slide 3 + `architecture.md` |
| Base de datos | 1 min | E/R, normalizacion 3FN, trigger, vistas, indices | Slide 4 + `er-diagram.md` |
| Demo en vivo | 6-7 min | Flujo usuario + empresa + admin + mapa + pago demo | Navegador |
| Calidad y despliegue | 1.5 min | Tests, CI, Docker, render.yaml, Swagger | Terminal + Slide 8 |
| Sostenibilidad y mejoras | 1 min | Green Code, ODS, trabajo futuro | Slide 9-10 |
| Cierre | 30 s | Recap + invitar a preguntas | Slide 10 |

---

## 4. Mapa criterio por criterio (lo que valora la rubrica)

Para cada modulo de la rubrica indicamos: **que decir** + **que mostrar en pantalla** + **donde esta en el repo**.

### 4.1 Bases de Datos (1o DAW)

- **Que decir**: "El modelo esta normalizado hasta 3FN. Tenemos 13 tablas relacionales, 5 vistas para reporting, 4 procedimientos almacenados y un trigger temporal. Las relaciones N:M se modelan con tablas puente como `seguidores` y `likes_publicacion`."
- **Que mostrar**: abrir `docs/diagrams/er-diagram.md` (preferiblemente renderizado), luego abrir `database/schema.sql` y mostrar el trigger `trg_reservas_ultima_modificacion` y un par de indices compuestos.
- **Repo**: `database/schema.sql`, `database/views.sql`, `database/procedures.sql`, `docs/diagrams/er-diagram.md`.

### 4.2 Digitalizacion aplicada al sector (1o DAW)

- **Que decir**: "El sector de buceo de costa funciona casi en exclusiva con telefono, WhatsApp y agenda en papel. DiveConnect digitaliza el ciclo completo: descubrir, reservar, pagar y compartir. Modelo de negocio simulado: comision por reserva mas plan premium para centros."
- **Que mostrar**: la pagina `centros.html` con datos reales y el flujo de reserva en vivo. Mencionar slide del modelo de negocio.
- **Repo**: `MEMORIA-DIVECONNECT.md` seccion 4 (modelo de negocio).

### 4.3 Sostenibilidad / Green Code (1o DAW)

- **Que decir**: "Aplicamos consultas paginadas y con indices, limpieza programada de historias caducadas con `@Scheduled`, imagen Docker multi-stage < 250 MB y uploads fuera del control de versiones. A nivel de ODS contribuimos a 9, 11, 12 y 14: industria/innovacion, ciudades sostenibles, consumo responsable (bitacora digital sustituye papel) y vida submarina (sensibilizacion)."
- **Que mostrar**: el bloque `@Scheduled` en `HistoriaService` o `EmojiStripMigration`, la seccion §12 de la memoria.
- **Repo**: `MEMORIA-DIVECONNECT.md` §12.

### 4.4 Entornos de Desarrollo (1o DAW)

- **Que decir**: "Git con flujo simplificado GitHub Flow, Conventional Commits, CI con GitHub Actions y MySQL service container, suite JUnit con Mockito (31 tests verdes), Maven Wrapper, `.editorconfig` y `.gitattributes` para evitar problemas entre Windows y Linux."
- **Que mostrar**: ejecutar `./mvnw test` en directo (o tener el output reciente preparado), abrir `.github/workflows/ci.yml`, abrir el log de Actions en GitHub si esta visible.
- **Repo**: `src/test/java/`, `.github/workflows/ci.yml`, `docs/test-plan.md`.

### 4.5 Lenguajes de Marcas y SGI (1o DAW)

- **Que decir**: "HTML5 semantico con `header/nav/main/section/article/footer`, `lang='es'`, viewport, meta description, Open Graph y theme-color. JSON como formato de intercambio en todos los endpoints REST."
- **Que mostrar**: vista del codigo de `feed.html` o `Inmersiones.html` desplazando para ver la estructura semantica; respuesta JSON de Swagger.
- **Repo**: `src/main/resources/static/pages/`, `robots.txt`, `sitemap.xml`.

### 4.6 Programacion (1o DAW)

- **Que decir**: "POO clasico con entidades, servicios y DTOs. Algoritmica destacable: formula de Haversine en SQL nativo para buscar inmersiones por proximidad y endpoint `/verify` idempotente para evitar duplicados en pagos. Excepciones de dominio capturadas en `GlobalExceptionHandler`."
- **Que mostrar**: `InmersionRepository.findMasCercanas` (query nativa con Haversine), `PaymentController.verificar` (idempotencia), `GlobalExceptionHandler`.
- **Repo**: `src/main/java/com/diveconnect/repository/`, `controller/`, `exception/`.

### 4.7 Sistemas Informaticos (1o DAW)

- **Que decir**: "Infraestructura simple y reproducible: contenedor Docker con la app, MySQL externo, puerto 8080 expuesto. Usuario no-root dentro del contenedor, secretos por variables de entorno, healthcheck con Actuator. En produccion, Render.com gestiona el balanceador y SSL Let's Encrypt."
- **Que mostrar**: `Dockerfile`, `docker-compose.yml`, `render.yaml`, output de `curl http://localhost:8080/actuator/health`.
- **Repo**: raiz del repositorio + `INSTALL.md`.

### 4.8 Desarrollo Web Cliente (2o DAW)

- **Que decir**: "Cliente vanilla con JS modular sin framework. `api.js` centraliza llamadas con JWT, `auth.js` maneja sesion, `payment.js` el modal de pago y `publicaciones.js` el feed. Async/await en todas las peticiones. Estado en `localStorage` y memoria."
- **Que mostrar**: abrir `static/js/api.js` para mostrar las funciones `fetchAPI` y manejo de errores; abrir DevTools Network durante una operacion CRUD en directo.
- **Repo**: `src/main/resources/static/js/`.

### 4.9 Despliegue de Aplicaciones Web (2o DAW)

- **Que decir**: "Tres modos de ejecucion documentados: local con Maven, Docker Compose y Render.com. SSL/HTTPS automatico por Let's Encrypt en Render. CI/CD con GitHub Actions: en cada push a master se ejecutan tests, build y verificacion de la imagen Docker. Render hace auto-deploy desde master."
- **Que mostrar**: `Dockerfile` (multi-stage), `render.yaml`, log de Actions en GitHub.
- **Repo**: `Dockerfile`, `docker-compose.yml`, `render.yaml`, `.github/workflows/ci.yml`, `INSTALL.md`.

### 4.10 Desarrollo Web Servidor (2o DAW)

- **Que decir**: "Arquitectura MVC clasica de Spring Boot: controller/service/repository/entity/dto. Autenticacion JWT stateless con BCrypt para contrasenas. API REST documentada en Swagger UI. Roles ADMINISTRADOR, USUARIO_EMPRESA y USUARIO_COMUN con reglas por endpoint en `SecurityConfig`. Variables sensibles via `.env`."
- **Que mostrar**: `SecurityConfig.java`, `JwtAuthenticationFilter.java`, Swagger UI con un endpoint protegido, `.env.example`.
- **Repo**: `src/main/java/com/diveconnect/security/`, `config/`, `controller/`, `service/`.

### 4.11 PIDAWE (2o DAW)

- **Que decir**: "Ciclo de vida iterativo-incremental con 10 sprints planificados y trazados en `docs/diagrams/gantt.md`. El historial de Git refleja la progresion: backend, marketplace, pasarela, red social, mapa, UX, calidad, despliegue, documentacion. Las incidencias relevantes estan en `ISSUES.md` y los cambios en `CHANGELOG.md`. README como mapa del proyecto."
- **Que mostrar**: `docs/diagrams/gantt.md` (renderizado Mermaid), `ISSUES.md`, `CHANGELOG.md`, `README.md`, `git log --oneline`.
- **Repo**: raiz + `docs/diagrams/gantt.md`.

### 4.12 Diseno de Interfaces Web (2o DAW)

- **Que decir**: "Guia de estilo en `docs/style-guide.md` con paleta submarina, tipografia y tokens. Wireframes SVG en `docs/wireframes/`. Mobile-first con breakpoints en 480, 768 y 1024 px. Accesibilidad WCAG nivel AA con foco visible, `aria-label`, contraste y enlace 'Saltar al contenido'. Auditoria recogida en `docs/lighthouse-audit.md`."
- **Que mostrar**: redimensionar la ventana del navegador en `feed.html` para mostrar el dock movil; abrir wireframes; abrir `style-guide.md`.
- **Repo**: `docs/wireframes/`, `docs/style-guide.md`, `docs/lighthouse-audit.md`, `style.css`, `ocean-theme.css`.

### 4.13 Programacion en Python y Analisis de Datos (2o DAW)

- **Que decir**: "Script ETL en `scripts/analytics/analytics.py`. Extract con PyMySQL desde la misma BD, transform con pandas (agregaciones por mes, estado, top centros, especies mencionadas) y load a CSV y graficas PNG con matplotlib. Las graficas se guardan en `docs/screenshots/analytics/` para incluirse como evidencia visual."
- **Que mostrar**: ejecutar `python scripts/analytics/analytics.py` en directo si hay tiempo, o abrir los PNG ya generados antes de la defensa.
- **Repo**: `scripts/analytics/analytics.py`, `requirements.txt`, `README.md`.

---

## 5. Como manejar lo que no esta al 100 %

Hay zonas donde el proyecto puede recibir preguntas dificiles. La estrategia no es mentir sino redirigir a la evidencia preparada.

### 5.1 Despliegue en produccion real

- **Realidad**: el `render.yaml` esta listo y la imagen Docker esta probada localmente. No hay una URL publica en vivo permanente.
- **Como manejarlo**: "El blueprint de Render esta preparado y el `Dockerfile` se verifica en CI. El proximo paso es asociar un dominio y activar el plan Starter; para la defensa lo enseno funcionando localmente porque garantiza estabilidad y latencia 0." Mostrar `render.yaml` y `Dockerfile`.
- **Si insisten**: si hay wifi estable, se puede levantar un deploy en Render Free 24h antes y compartir la URL.

### 5.2 Pasarela de pago real con Stripe/PayPal

- **Realidad**: la integracion esta hecha y soporta sandbox; las claves reales requieren cuenta empresarial verificada (KYC).
- **Como manejarlo**: "La integracion completa Stripe Checkout y PayPal Orders v2 esta implementada y testada con sandbox. Para la defensa uso modo demo, que reproduce exactamente el flujo de confirmacion y notificacion. Para produccion solo falta verificar la cuenta empresarial; es una gestion administrativa, no tecnica." Mostrar `StripeService.java`, `PayPalService.java`.

### 5.3 GitHub Issues vs `ISSUES.md`

- **Realidad**: el control de incidencias esta en `ISSUES.md`, no en la pestana Issues de GitHub.
- **Como manejarlo**: "Trabajando solo, opte por un fichero versionado `ISSUES.md` que viaja con el codigo y es mas auditable que un tablero externo. Cada incidencia tiene tipo, impacto, solucion y estado. En equipo trasladaria ese contenido a GitHub Issues o un Trello."

### 5.4 Lighthouse automatico

- **Realidad**: la auditoria existe en `docs/lighthouse-audit.md` pero es manual; los numeros oficiales se generan el dia anterior.
- **Como manejarlo**: si para el dia de la defensa ya has corrido Lighthouse en Chrome, ensenas el reporte PDF. Si no, "La auditoria manual con criterios Lighthouse esta en `lighthouse-audit.md` con tabla de paginas; un audit automatico se puede ejecutar en directo si el tribunal lo pide".

### 5.5 Cobertura de tests > 80 %

- **Realidad**: 31 tests, cubren servicios criticos y validacion de archivos, no es cobertura por linea > 80 %.
- **Como manejarlo**: "Los tests cubren la logica de negocio mas critica: reservas, pagos, subidas, migracion de datos. Para un MVP individual prioricé tests de logica de dominio frente a cobertura por linea; en un equipo de mantenimiento anadiria JaCoCo y subiria a 80 %."

### 5.6 App movil nativa

- **Realidad**: solo web responsive, no app nativa.
- **Como manejarlo**: "La API REST esta lista y documentada en Swagger. Una app movil iOS/Android consumiria los mismos endpoints. Lo dejo como trabajo futuro porque el MVP web responsive cubre el uso real desde movil."

---

## 6. Preguntas tipicas con respuesta directa

| Pregunta probable | Respuesta corta |
|---|---|
| ¿Por que sin framework frontend? | "Para demostrar dominio de HTML/CSS/JS puros y mantener el cliente ligero. En produccion migrar a React o Svelte es un refactor controlado." |
| ¿Como evitas que dos usuarios reserven la ultima plaza? | "La reserva es transaccional (`@Transactional`), descontamos la plaza en la misma transaccion. Si fallara la pasarela, se libera." |
| ¿Que pasa si el cliente recarga la pagina de pago? | "El endpoint `/verify` es idempotente: comprueba el estado antes de confirmar, asi no se duplica." |
| ¿Por que MySQL y no PostgreSQL? | "El driver, soporte y conocimiento en el ciclo lo justifican. Spring Data JPA es portable, migrar es cambiar dependencias y dialecto." |
| ¿Como manejas archivos grandes? | "Validamos MIME, extension y tamano. UUID para evitar colisiones. En produccion se movera a almacenamiento externo (S3/R2) en lugar de disco." |
| ¿Como autenticas a un usuario? | "Login devuelve JWT firmado HS256. El frontend lo envia en `Authorization: Bearer`. `JwtAuthenticationFilter` valida y rellena `SecurityContext`." |
| ¿Que hay del GDPR? | "Datos minimos, contrasenas con BCrypt, JWT en cliente, RGPD recogido en la memoria seccion de seguridad. Los uploads no se versionan." |
| ¿Sostenibilidad en software? | "Paginacion, indices, limpieza programada, Docker multi-stage, logs SQL off. ODS 9, 11, 12, 14." |
| ¿Tests e2e? | "Tests JUnit + Mockito cubren lo critico. E2e con Cypress o Playwright queda como mejora; lo dejo en backlog." |
| ¿Internacionalizacion? | "Estructura preparada con `messages.properties` espanol; falta traduccion. Es un refactor cosmetico, no arquitectonico." |

---

## 7. Plan B si algo falla en directo

| Fallo | Plan B |
|---|---|
| No arranca la app | `./mvnw clean package -DskipTests && java -jar target/diveconnect-*.jar` con la consola del tribunal viendo el log de arranque |
| MySQL no conecta | Tener un dump local listo y comando `mysql -u root < dump.sql` preparado |
| Stripe/PayPal sandbox da error | Usar modo demo, esta documentado en la memoria como decision intencional |
| WiFi caida | Levantar todo en `localhost`. El proyecto no depende de internet salvo Stripe/PayPal real y OpenWeather |
| Tests fallan en directo | Mostrar reporte anterior verde en `target/surefire-reports/` y explicar el motivo del fallo si es trivial |
| Lighthouse no carga | Ensenar la auditoria manual en `lighthouse-audit.md` |

Regla general: **no se entra en panico, se redirige a la evidencia documentada**.

---

## 8. Checklist final el dia anterior

- [ ] `git status` limpio en master.
- [ ] `./mvnw clean test` verde (31/0/0).
- [ ] `./mvnw spring-boot:run` arranca y `curl http://localhost:8080/actuator/health` responde 200.
- [ ] Login con `admin/admin` funciona y se ve el panel admin.
- [ ] Reserva + pago demo completados sin error.
- [ ] Swagger UI accesible en `/swagger-ui.html`.
- [ ] `python scripts/analytics/analytics.py` ejecutado y graficas guardadas.
- [ ] Lighthouse manual ejecutado en Chrome y notas anadidas.
- [ ] Screenshots de los flujos en `docs/screenshots/`.
- [ ] Slides 8-10 diapositivas exportadas a PDF.
- [ ] Memoria impresa o en pantalla en formato PDF.
- [ ] Portatil cargado, cargador en mochila, adaptador HDMI.
- [ ] Usuarios demo en una nota fisica o en pantalla bloqueada.

---

## 9. Mentalidad

- La rubrica valora la **trazabilidad**: que cada cosa que digas tenga su evidencia en el repositorio. Tu mejor activo es `COBERTURA-RUBRICA.md`.
- El tribunal valora **profesionalidad y dominio del lenguaje tecnico** mas que un proyecto inmenso. Habla con calma, usa palabras tecnicas pero defendibles.
- Si una pregunta no tiene respuesta, **no inventes**. Reconoce el limite y propon como se resolveria.
- El proyecto es solido. La defensa es solo ponerlo en contexto. Confianza.
