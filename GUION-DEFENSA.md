# Guion de defensa · DiveConnect

Guion completo sincronizado con `docs/DiveConnect-Defensa.pptx` (30 slides). Pensado para una defensa de **35-40 minutos + 5-15 minutos de preguntas**.

## Cómo usar este guion

- **Imprime este documento** y tenlo a mano (papel) durante la defensa, junto con el handout para el tribunal.
- **Cada slide** tiene cuatro columnas: tiempo, qué dices, qué enseñas y notas.
- **Lo que dices**: está en *cursiva*. Es texto literal preparado para sonar natural; no lo leas tal cual al tribunal, mejor estúdialo y dilo con tus palabras.
- **Lo que enseñas**: además de la slide, qué pestaña del navegador, qué archivo en VS Code, qué terminal abrir.
- **Antes de empezar la defensa, abre estas pestañas en Chrome** (en este orden):
  1. La URL pública de Render: `https://diveconnect-XXXX.onrender.com`
  2. La misma URL + `/pages/login.html`
  3. La misma URL + `/swagger-ui.html`
  4. https://github.com/mmordoe685/diveconnect
  5. https://github.com/mmordoe685/diveconnect/issues
  6. https://github.com/mmordoe685/diveconnect/actions
  7. https://github.com/mmordoe685/diveconnect/blob/master/docs/diagrams/er-diagram.md
  8. http://localhost:8080 (la app en local, como plan B si Render se cae)

- **VS Code abierto** con la rama `claude/peaceful-hellman` y estos archivos en pestañas:
  - `database/views.sql`
  - `database/procedures.sql`
  - `src/main/java/com/diveconnect/controller/PaymentController.java`
  - `src/main/java/com/diveconnect/repository/InmersionRepository.java`
  - `src/main/java/com/diveconnect/config/SecurityConfig.java`
  - `Dockerfile`
  - `render.yaml`
  - `.github/workflows/ci.yml`

- **Terminal abierto** (PowerShell) en la raíz del proyecto, listo para ejecutar `./mvnw test`.

- **El .pptx** abierto en modo presentación (`F5` en PowerPoint).

## Tabla de tiempos resumida

| Bloque | Slides | Min | Acumulado |
|---|---|---|---|
| Apertura | 1-2 | 2:00 | 2:00 |
| Problema y solución | 3-5 | 4:00 | 6:00 |
| Visual y demo | 6-7 | 5:00 | 11:00 |
| Profundidad técnica | 8-11 | 6:00 | 17:00 |
| Stack | 12-13 | 2:00 | 19:00 |
| Pasarela de pago | 14-15 | 3:00 | 22:00 |
| Seguridad | 16-17 | 3:00 | 25:00 |
| Frontend | 18-19 | 2:00 | 27:00 |
| Subida + Tests + CI | 20-22 | 3:00 | 30:00 |
| Despliegue + Python | 23-25 | 3:00 | 33:00 |
| Sostenibilidad + decisiones | 26-27 | 2:30 | 35:30 |
| Métricas + roadmap + cierre | 28-30 | 2:30 | 38:00 |
| **Preguntas** | — | 7-15 | 45:00-53:00 |

---

# Bloque 1 — Apertura · 2 min

## Slide 1 (portada) · 0:30

**Lo que enseñas**: portada del .pptx. Mírales a los ojos al saludar.

**Lo que dices**:

> *"Buenos días. Soy Marcos Mordoñez Estévez, estudiante de 2º curso de DAW, y vengo a defender mi proyecto intermodular: DiveConnect. Una red social y plataforma de reservas para la comunidad submarinista."*

**Acción**: pulsar para avanzar a la slide 2.

## Slide 2 (sobre mí + estructura) · 1:30

**Lo que enseñas**: la slide. Llama la atención sobre los 4 bloques con el dedo.

**Lo que dices**:

> *"He cursado los 13 módulos del ciclo. Mi optativa es Programación en Python y Análisis de Datos. La presentación dura 35-40 minutos y la he organizado en cuatro bloques claros:*
>
> *Primero el contexto del proyecto y una demo en vivo del flujo principal. Luego entro en la profundidad técnica: arquitectura, modelo de datos, pasarela de pago y seguridad. Después calidad y operación: tests, despliegue y análisis de datos. Y cierro con decisiones técnicas notables, métricas reales y roadmap.*
>
> *Antes de empezar he repartido al tribunal una hoja resumen con los datos del proyecto y links útiles, por si quieren consultar algo en paralelo."*

**Notas**: si te ofrecen mover el papel para verlo mejor, deja unos segundos. Avanza con confianza.

---

# Bloque 2 — Problema y solución · 4 min

## Slide 3 (el problema) · 1:30

**Lo que dices**:

> *"DiveConnect nace de un problema real. El sector del buceo recreativo en España está fragmentado en tres frentes desconectados.*
>
> *Primero, las reservas siguen siendo analógicas: la mayoría de centros gestionan sus plazas por correo, WhatsApp y llamada telefónica. El cobro va por transferencia. Sin trazabilidad y con fricción para el usuario.*
>
> *Segundo, la comunidad está dispersa: los buceadores hablan en grupos privados de Facebook, foros antiguos e Instagram, sin filtros por nivel, profundidad o ubicación que aprovechen los datos técnicos del buceo.*
>
> *Y tercero, el diario personal del buceador sigue siendo un cuaderno físico o un export aislado del ordenador de buceo. Sin sincronización social ni descubrimiento."*

## Slide 4 (datos del sector) · 1:00

**Lo que dices**:

> *"Algunos datos para dimensionar el mercado: el sector mueve unos 200 millones de euros anuales según la Federación Española de Actividades Subacuáticas. Hay alrededor de 2.400 centros registrados, pero el 70 % no tiene plataforma de reservas propia. Y solo un 35 % de los buceadores reserva online — el resto sigue por canales analógicos.*
>
> *Hay un hueco real para una plataforma vertical que conecte buceadores con centros y digitalice las reservas, manteniendo la actividad social. Mi modelo de negocio es freemium: comisión del 3-5 % por reserva al centro, gratis para el buceador."*

## Slide 5 (3 ejes) · 1:30

**Lo que dices**:

> *"Mi propuesta es DiveConnect, que une tres productos en una sola plataforma.*
>
> *El primer eje es marketplace: catálogo de inmersiones con filtros, reservas con descuento automático de plazas, y pasarela de pago real en tres modos: demo, sandbox y producción.*
>
> *El segundo eje es red social vertical: feed con publicaciones que llevan datos técnicos como profundidad, temperatura y especies vistas. Historias 24h. Sistema de seguimiento. Mapa interactivo con puntos georreferenciados.*
>
> *Y el tercer eje es diario digital: cada publicación con datos técnicos sirve también de log book personal del buceador.*
>
> *Hay tres tipos de usuario: el buceador común, el centro de buceo como empresa, y el administrador."*

---

# Bloque 3 — Visual y demo · 5 min

## Slide 6 (cómo se ve) · 1:00

**Lo que dices**:

> *"Antes de la demo, una palabra rápida sobre el diseño visual. He desarrollado un tema submarino propio sobre el sistema de diseño base.*
>
> *La paleta combina un fondo cálido cream con un acento aqua que evoca el mar superficial, complementado con coral para acciones destructivas y dorado para avisos. Glassmorphism en las tarjetas, fondo subacuático animado con caustics y rayos de luz, dock con halo pulsante. Todo respeta `prefers-reduced-motion` y cumple WCAG 2.1 AA verificado en una auditoría manual.*
>
> *Sin emojis decorativos: uso iconos SVG inline para garantizar consistencia entre Windows, macOS, Android e iOS, donde las fuentes de emoji varían."*

## Slide 7 (puente a demo) · 0:15

**Lo que dices**:

> *"Voy a hacer una demo en vivo del flujo completo. Tarda unos 5 minutos."*

**Acción**: alt+tab a Chrome con la URL de Render.

## Demo en vivo · 4 min

**Pasos cronometrados** (tienes 4 minutos, no te entretengas):

### Paso 1 — Login (30 s)

- Abre `https://diveconnect-XXXX.onrender.com/pages/login.html`.
- Login con `sofia_buceo` / `admin`.

> *"Login con email y contraseña. La autenticación se valida en backend con BCrypt y devuelve un JWT firmado con HS256 que el cliente guarda en localStorage. También hay login con Google OAuth2 si se configura."*

### Paso 2 — Feed (40 s)

- Scroll por el feed.
- Da like a una publicación.

> *"El feed es cronológico, estilo Instagram. Cada publicación lleva datos técnicos: lugar de inmersión, profundidad máxima, temperatura del agua, visibilidad y especies vistas. Estos datos sirven al filtrado de la búsqueda y al log book personal del buceador."*

### Paso 3 — Inmersiones y reserva (60 s)

- Dock → "Buscar" o navega a `/pages/Inmersiones.html`.
- Mira los filtros, los menciona.
- Click en "Reservar" en una inmersión.
- En el modal, selecciona 1 persona.
- Click en "Confirmar reserva".

> *"Aquí está el catálogo de inmersiones. Filtros por nivel de certificación, profundidad máxima y precio. Selecciono cualquiera para reservar. Indico número de personas — el sistema verifica plazas disponibles antes de confirmar y las descuenta atómicamente en una transacción."*

### Paso 4 — Pago (60 s)

- Aparece el modal de pago. Señala el badge "TFG · DEMO".
- Rellena tarjeta `4242 4242 4242 4242` / `12/30` / `123` / `Sofia Test`.
- Click "Pagar".

> *"Aparece el modal de pago. La aplicación detecta automáticamente la configuración del backend mediante /api/paypal/config y /api/payments/config. Como aquí no hay credenciales reales, está en modo demo TFG, claramente marcado con el badge amarillo. Si tuviera Stripe configurado, redirigiría al Checkout oficial. Si tuviera PayPal, cargaría el SDK.*
>
> *La validación del número de tarjeta es local con el algoritmo de Luhn. Pulso pagar."*

### Paso 5 — Notificaciones (30 s)

- Tras el tick verde, ir a `/pages/notificaciones.html`.

> *"Tick verde, reserva confirmada. Y aquí están las dos notificaciones automáticas: una para mí como buceadora confirmando que la reserva está pagada, y otra para el centro avisándole que ha recibido una nueva reserva. Todo en una transacción atómica."*

### Paso 6 — Mapa (30 s)

- Dock → "Mapa".
- Click en cualquier marcador para abrir su popup.
- Click en "Ver detalles".

> *"El mapa interactivo usa Leaflet con tiles de OpenStreetMap. Cada marcador es un punto de buceo georreferenciado por un centro o un administrador. Al abrir el detalle veo profundidad, temperatura, visibilidad, especies y el tiempo atmosférico actual obtenido de OpenWeatherMap."*

### Paso 7 — Perfil (20 s)

- Cerrar modal del mapa.
- Dock → "Perfil".

> *"Y por último el perfil. Aquí puedo cambiar mi foto desde la galería del móvil — subida real con multipart, validada por MIME y extensión, y guardada con UUID. La biografía y el nivel de certificación son editables."*

**Acción**: alt+tab de vuelta al .pptx, slide 8.

---

# Bloque 4 — Profundidad técnica · 6 min

## Slide 8 (arquitectura) · 1:30

**Lo que dices**:

> *"Vamos al backend. El proyecto sigue una arquitectura clásica de tres capas dentro de un único proceso Spring Boot.*
>
> *El cliente envía cada petición autenticada con un Authorization Bearer JWT. El JwtAuthenticationFilter intercepta la petición antes que cualquier otro filtro, valida la firma y la expiración del token, y monta el SecurityContext de Spring.*
>
> *De ahí pasa al controller correspondiente — tengo 17, uno por dominio. El controller delega en un service, que aplica la lógica de negocio en transacciones explícitas. El service usa los repositories de Spring Data JPA para hablar con MySQL.*
>
> *Como servicios externos, integro Stripe Checkout, PayPal REST v2, Google OAuth2 y OpenWeatherMap. Todos opcionales: si no hay credenciales, la app sigue funcionando."*

## Slide 9 (secuencia del pago) · 1:30

**Lo que dices**:

> *"Como ejemplo de flujo, esta es la secuencia técnica del pago en modo demo. El usuario pulsa pagar en el frontend. El frontend consulta /api/paypal/config para saber el modo. El backend responde con la config. El frontend llama a /verify con la id de la reserva. El backend hace UPDATE de la reserva e INSERT de las dos notificaciones, todo en transacción.*
>
> *Aquí hay un detalle importante: idempotencia. Si la reserva ya está pagada, el endpoint devuelve {alreadyPaid: true} sin volver a notificar. Esto previene el problema de doble click clásico en formularios de pago."*

## Slide 10 (modelo de datos) · 1:30

**Lo que dices**:

> *"El modelo de datos tiene 13 tablas, 25+ relaciones, 5 vistas SQL y 4 procedimientos almacenados. Está normalizado a tercera forma normal con dos desnormalizaciones deliberadas que ahora explico.*
>
> *Las entidades centrales son Usuario, CentroBuceo, Inmersion, Reserva, Publicacion, Comentario, Historia, Notificacion, SolicitudSeguimiento y PuntoMapa.*
>
> *Las relaciones N a M las modelo con tablas puente: seguidores y publicacion_likes. El diagrama Entidad-Relación completo está en docs/diagrams/er-diagram.md, en formato Mermaid que GitHub renderiza automáticamente, y también en formato DBML para dbdiagram.io."*

**Acción** (opcional, si quieres reforzar): alt+tab a la pestaña de GitHub con el er-diagram.md → vuelve al .pptx.

## Slide 11 (normalización) · 1:30

**Lo que dices**:

> *"Sobre la normalización, cumplo las tres primeras formas normales y BCNF en 14 de las 15 tablas.*
>
> *La excepción es la tabla reservas, donde duplico la FK a centros_buceo aunque sería derivable desde inmersion_id. Esta desnormalización es deliberada para acelerar la query 'todas las reservas recibidas por un centro', que es la consulta más caliente del dashboard de empresa. Está documentada como decisión técnica en la memoria.*
>
> *La segunda desnormalización es entre usuarios.nombre_empresa y centros_buceo.nombre. Pueden coincidir o divergir; la convención es que centros_buceo.nombre es la fuente de verdad cuando existe el centro."*

---

# Bloque 5 — Stack tecnológico · 2 min

## Slide 12 (stack backend) · 1:00

**Lo que dices**:

> *"Ahora el stack. En backend uso Java 17 con Spring Boot 3.2.3, que da el contenedor de inyección, el servidor Tomcat embebido y la autoconfiguración. Spring Web MVC para los controllers, Spring Data JPA con Hibernate para la persistencia, Spring Security 6 con OAuth2 client.*
>
> *La autenticación va con jjwt 0.11 y BCrypt. Las pasarelas: Stripe Java SDK 24 oficial, PayPal con cliente HTTP nativo de Java sin SDK añadido. Swagger lo monto con springdoc-openapi.*
>
> *Para tests: JUnit 5 + Mockito + AssertJ. Lombok para reducir boilerplate."*

## Slide 13 (stack frontend e infra) · 1:00

**Lo que dices**:

> *"En frontend, decisión consciente: HTML, CSS y JavaScript ES2020, sin framework. Sin bundler, sin transpiler. La razón es demostrar dominio del DOM, eventos y CSS modernos sin abstracciones, y mantener el bundle por debajo de 50 KB.*
>
> *Leaflet 1.9 para el mapa, fuentes Google Fonts para tipografía. Mi capa de tema submarino es CSS puro: ocean-theme.css y ocean-effects.js.*
>
> *Para infraestructura: Dockerfile multi-stage que produce una imagen de 120 MB, docker-compose para local, render.yaml para producción en Render.com. CI con GitHub Actions.*
>
> *Y el módulo optativo de Python lo cubro con un script ETL real que ahora explicaré."*

---

# Bloque 6 — Pasarela de pago · 3 min

## Slide 14 (3 modos) · 1:30

**Lo que dices**:

> *"La pasarela de pago soporta tres modos en un único flujo de código. Esta es una de las decisiones más interesantes del proyecto.*
>
> *En modo demo TFG, sin credenciales configuradas, la pestaña Tarjeta del modal valida con el algoritmo de Luhn local y marca la reserva como pagada directamente. Es el modo que veis ahora en la demo.*
>
> *En modo sandbox, con PAYPAL_CLIENT_ID o STRIPE_SECRET_KEY de test, la app llama a los entornos sandbox reales. Tarjetas de prueba contra api-m.sandbox.paypal.com.*
>
> *En modo live, con credenciales reales en variables de entorno, la app procesa cobros reales. Cero cambios de código entre los tres modos.*
>
> *El frontend detecta el modo automáticamente consultando los endpoints de configuración y muestra un badge visible al usuario para que sepa siempre dónde está."*

## Slide 15 (PaymentController detalle) · 1:30

**Lo que dices**:

> *"El detalle técnico del controller. Tres caminos en el método verificar.*
>
> *Camino 1: si la reserva ya está pagada, devuelvo el estado actual con la flag alreadyPaid en true. Es la idempotencia que mencioné antes — protege contra doble click y reintentos.*
>
> *Camino 2: si Stripe está activo y la reserva tiene un sessionId, hago una llamada real a Stripe para verificar el pago. Si Stripe lo confirma, llamo al método privado confirmarPago.*
>
> *Camino 3: para todos los demás casos — modo demo o Stripe en modo demo —, llamo directamente a confirmarPago sin verificación externa.*
>
> *El método confirmarPago es uno solo: hace UPDATE de la reserva, y dispara dos INSERT a notificaciones, una para el usuario y otra para el centro. Esto garantiza que el comportamiento es idéntico en los tres modos."*

---

# Bloque 7 — Seguridad · 3 min

## Slide 16 (JWT) · 1:30

**Lo que dices**:

> *"La seguridad va con JWT stateless. No hay sesiones en servidor, no hay cookies — todo se hace con tokens firmados.*
>
> *Cuando un usuario hace login, el backend valida sus credenciales con BCrypt cost factor 10. Si son correctas, JwtUtil genera un token firmado con HS256 usando un secret de 64+ caracteres que vive en variable de entorno. El token incluye el username como subject y caduca en 24 horas. El cliente lo guarda en localStorage.*
>
> *En cada petición protegida, el cliente añade el header Authorization Bearer. Mi JwtAuthenticationFilter, que extiende OncePerRequestFilter, intercepta la petición antes del UsernamePasswordAuthenticationFilter de Spring. Valida la firma, valida la expiración, extrae el username y monta el Authentication en el SecurityContext.*
>
> *A partir de ahí, Spring Security comprueba las reglas de autorización declaradas en SecurityConfig. Catálogo público, todo lo demás autenticado, /api/admin solo para el rol ADMINISTRADOR."*

## Slide 17 (OAuth2 + uploads + otros) · 1:30

**Lo que dices**:

> *"Más capas de seguridad. Las contraseñas con BCrypt, salt incluido, nunca se loguean ni se serializan al cliente.*
>
> *OAuth2 con Google es opcional, activable con un flag. El flujo es estándar: redirect a accounts.google.com, callback, mi GoogleOAuth2SuccessHandler busca el email en BD, lo crea si no existe, y emite un JWT idéntico al del login normal.*
>
> *Para los uploads, defensa en tres capas: el MIME debe empezar por image/ o video/, la extensión debe estar en una lista blanca de 11 extensiones, y el nombre del archivo se genera con UUID en el servidor. El cliente nunca controla el filename, lo que previene path traversal.*
>
> *Headers X-Content-Type-Options y X-Frame-Options activos por defecto Spring. CSRF deshabilitado porque es API JSON con JWT. CORS restrictivo en producción al dominio del frontend.*
>
> *Y un GlobalExceptionHandler que traduce mis tres excepciones custom — BadRequest, ResourceNotFound, Unauthorized — a JSON con status, message y timestamp."*

---

# Bloque 8 — Frontend · 2 min

## Slide 18 (sin framework) · 1:00

**Lo que dices**:

> *"Sobre el frontend, ya he comentado que es JavaScript vanilla. Aquí están las razones y los trade-offs aceptados.*
>
> *Por qué sin framework: demuestra dominio real, bundle pequeño, auditoría fácil para cualquier evaluador, feedback loop instantáneo, cero dependencias salvo Leaflet.*
>
> *Trade-offs: más markup repetido entre páginas, sin TypeScript, sin gestión de estado declarativa. La API REST está completamente desacoplada — si en el futuro creciera, migrar a React sería trivial sin tocar backend."*

## Slide 19 (tema submarino) · 1:00

**Lo que dices**:

> *"El tema submarino es una capa overlay sobre el sistema de diseño base. Glassmorphism con backdrop-filter blur en las tarjetas, fondo subacuático con caustics radiales animadas y rayos de luz oblicuos, burbujas que ascienden con CSS keyframes para no consumir CPU del JavaScript.*
>
> *Responsive móvil con bottom sheets — los modales surgen desde abajo en pantallas estrechas con animación spring. Inputs de 44 píxeles para target táctil. Font-size 16 píxeles para evitar el zoom automático de iOS al hacer focus.*
>
> *Todo respeta prefers-reduced-motion: si el usuario lo ha activado en su sistema, las burbujas no se inyectan y las animaciones largas se desactivan."*

---

# Bloque 9 — Subida + tests + CI · 3 min

## Slide 20 (uploads) · 1:00

**Lo que dices**:

> *"La subida de archivos sigue este pipeline. El móvil usa el atributo capture environment para abrir directamente la cámara o la galería. El frontend manda multipart al endpoint /api/uploads. El UploadController valida MIME, valida extensión contra la lista blanca, genera un nombre con UUID y guarda en disco. Devuelve la URL pública /uploads/abc.png que sirve el ResourceHandler con caché HTTP de 1 hora.*
>
> *Tres usos del mismo endpoint: publicación con foto, historia 24h con foto o vídeo, y foto de perfil con cámara frontal usando capture user. Cubierto con 5 tests unitarios."*

## Slide 21 (tests) · 1:00

**Lo que dices**:

> *"Tests automatizados: 31 tests JUnit con cero fallos. Cubren los servicios y controllers críticos.*
>
> *ReservaService con 5 tests: caminos felices y errores controlados. PayPalService con 8: configuración, modos sandbox y live, fetch de token. StripeService con 5. UploadController con 5: MIME, extensión, UUID, vídeo, MIME no aceptado. Y EmojiStripMigration con 8: rangos Unicode y edge cases.*
>
> *Si quieren puedo ejecutarlos en vivo ahora."*

**Acción**: si te lo piden, alt+tab a terminal y ejecuta `./mvnw test`. La última línea sale `Tests run: 31, Failures: 0, Errors: 0`.

## Slide 22 (CI/CD) · 1:00

**Lo que dices**:

> *"Integración continua con GitHub Actions. El workflow .github/workflows/ci.yml define este pipeline en cada push: checkout, setup JDK 17 con cache de Maven, compilar, tests con MySQL como service container, empaquetar JAR, build de imagen Docker en master, subir artefactos JAR y reportes Surefire.*
>
> *Render redeploya automáticamente en cada push a master, así que el flujo completo de desarrollo es: editar localmente, push, GitHub Actions valida, Render redespliega, app actualizada en producción en menos de 10 minutos."*

**Acción**: si te interesa enseñar las builds verdes, alt+tab a https://github.com/mmordoe685/diveconnect/actions.

---

# Bloque 10 — Despliegue + Python · 3 min

## Slide 23 (despliegue 3 caminos) · 1:00

**Lo que dices**:

> *"He preparado tres caminos de despliegue para distintos escenarios.*
>
> *Local sin Docker para desarrollo: ./mvnw spring-boot:run levanta la app en 12-15 segundos.*
>
> *Docker Compose para entorno aislado reproducible: docker compose up --build levanta MySQL y la app en contenedores separados con volúmenes y healthchecks.*
>
> *Y Render.com para producción real: el archivo render.yaml es un Blueprint declarativo que define el web service y la base de datos MySQL gestionada. Push a master, Render hace build de la imagen Docker y deploya con SSL automático. La URL pública es la que estoy usando ahora en la demo."*

## Slide 24 (por qué Render) · 1:00

**Lo que dices**:

> *"La justificación de elegir Render sobre VPS clásico, AWS o Heroku.*
>
> *Plan free real con 0,5 CPU y 512 MB RAM permanentes — AWS Free Tier expira a los 12 meses. SSL Let's Encrypt automático sin certbot. Blueprint declarativo en un fichero versionable en el repo. Auto-deploy sin GitHub Actions custom para esa parte. BD MySQL gestionada en red interna sin exposición pública. Dominio gratis cambiable a uno propio con DNS CNAME.*
>
> *Para un TFG con tracción cero al principio, el plan free es perfecto. Si el proyecto creciera, el Starter cuesta 7 dólares al mes."*

## Slide 25 (Python) · 1:00

**Lo que dices**:

> *"El módulo optativo de Python y Análisis de Datos lo cubro con un pipeline ETL real, no un script de juguete.*
>
> *Extracción: PyMySQL conecta a la base de datos del proyecto principal y saca cinco datasets — publicaciones por mes, reservas por estado, top inmersiones, especies más mencionadas, métricas globales.*
>
> *Transformación: pandas con groupby, sort, aggregate. Filtros por fecha y por estado.*
>
> *Visualización: matplotlib con la paleta corporativa del frontend para mantener coherencia visual. Tres gráficas en PNG: barras de publicaciones por mes, pie de distribución de reservas, barras horizontales del top de inmersiones.*
>
> *Output: las tres PNG más un dashboard.csv consolidado con todas las métricas. Idempotente: ejecutar dos veces produce los mismos archivos."*

---

# Bloque 11 — Sostenibilidad y decisiones · 2:30

## Slide 26 (sostenibilidad) · 1:30

**Lo que dices**:

> *"Sostenibilidad y digitalización. He alineado el proyecto con cuatro Objetivos de Desarrollo Sostenible.*
>
> *ODS 14, vida submarina: las publicaciones con datos técnicos generan un dataset social sobre el estado de los ecosistemas marinos. Cualquier biólogo o gestor de áreas protegidas puede agregar tendencias por zona.*
>
> *ODS 9, industria e innovación: digitaliza un sector tradicionalmente analógico.*
>
> *ODS 8, trabajo decente: comisión del 3-5 % muy por debajo del 15-25 % de plataformas de hostelería.*
>
> *ODS 13, acción por el clima: servidor consume mínimo, frontend sin framework reduce huella en cliente.*
>
> *En Green Code: 10 decisiones técnicas concretas — Haversine en SQL en lugar de cargar todo en Java, paginación obligatoria, vistas SQL precomputadas, índices secundarios, caché HTTP de 1 hora, CSS keyframes en lugar de JS RAF, gzip por defecto, imagen Alpine de 120 MB, JVM con G1GC.*
>
> *Huella estimada según metodología Sustainable Web Design: 0,75 gramos de CO2 al mes con 1.000 visitantes. Despreciable."*

## Slide 27 (decisión Lombok) · 1:00

**Lo que dices**:

> *"Una decisión técnica que merece una mención específica. Es la historia más rica del desarrollo.*
>
> *Síntoma: tras aceptar una solicitud de seguimiento entre dos usuarios, el endpoint que consultaba si A sigue a B seguía devolviendo NO_SIGUE aunque la fila estaba en la BD. El comportamiento era no determinista, y a veces caía en StackOverflowError.*
>
> *Causa: Lombok @Data genera equals y hashCode cubriendo TODOS los campos de la clase, incluidas las colecciones autorreferenciadas seguidores y siguiendo. Como Set.contains usa hashCode, y el hashCode necesita el hash de las colecciones, y las colecciones contienen la propia entidad… recursión.*
>
> *Solución: bypass del Set para las consultas críticas con SQL nativo en UsuarioRepository. Tres queries: addSeguidor con INSERT IGNORE, removeSeguidor con DELETE, countSeguimiento con SELECT COUNT. Y un default method existsSeguimiento sobre countSeguimiento.*
>
> *La alternativa ortodoxa era anotar las 15 entidades con @EqualsAndHashCode of id. Riesgo de regresiones. Mi solución es local al subsistema de seguimiento, está documentada y cubierta con tests."*

---

# Bloque 12 — Métricas, roadmap, cierre · 2:30

## Slide 28 (métricas) · 1:00

**Lo que dices**:

> *"Algunos números del proyecto: 95 archivos Java, 21 páginas HTML, 7 módulos JavaScript, 17 controllers REST, 15 services, 11 repositories, 13 tablas en MySQL, 5 vistas SQL, 4 procedimientos almacenados, 31 tests JUnit con cero fallos, 87 páginas de memoria técnica en PDF, 30 slides de defensa.*
>
> *Cada uno de estos números es verificable directamente en el repositorio público de GitHub. Está en la hoja resumen que tienen ustedes."*

## Slide 29 (roadmap) · 1:00

**Lo que dices**:

> *"El roadmap. La versión 1.0 que defiendo hoy es el MVP completo: red social, marketplace, pasarela en 3 modos, mapa, subida desde galería, despliegue con SSL.*
>
> *La versión 1.1 sería calidad operativa: migrar uploads a S3 para escalado horizontal, skip-to-content link para accesibilidad, tests E2E con Playwright, job programado de purga de historias.*
>
> *La versión 2.0 sería escalado: refresh tokens para sesiones largas, WebSockets para notificaciones en tiempo real reemplazando el poll, internacionalización a inglés, portugués y francés, y un recomendador básico de inmersiones aprovechando el módulo de Python.*
>
> *La lista completa con detalle técnico está en GitHub Issues."*

## Slide 30 (cierre) · 0:30

**Lo que dices**:

> *"Eso es todo. El código fuente está en GitHub público en mmordoe685/diveconnect, la memoria completa son 87 páginas con 20 capítulos, los tests, el CI, el blueprint de Render, los wireframes, el análisis Python y todos los diagramas están commiteados en el repositorio.*
>
> *Muchas gracias por su tiempo. Quedo a su disposición para cualquier pregunta."*

**Acción**: deja la slide 30 en pantalla durante el turno de preguntas.

---

# Anexo · Cómo manejar las preguntas del tribunal

## Reglas básicas para responder

1. **Escucha la pregunta entera antes de empezar a responder**. No te adelantes. Si te pierdes, di "¿podría repetirla?".

2. **Empieza con una frase de orientación** ("Sí, eso lo cubrí en...") antes de meterte en el detalle. Da tiempo a tu cerebro y al del que escucha.

3. **Si no sabes algo, dilo**. "Esa parte concreta no la profundicé. Mi enfoque fue X. Si lo hubiera abordado, habría hecho Y." Suena mucho mejor que inventar.

4. **Apóyate en lo que tienes en pantalla**. Si te preguntan por seguridad, abre `SecurityConfig.java` mientras hablas. Si te preguntan por la BD, abre `database/views.sql`.

5. **Reconoce los límites del MVP** sin disculparte. "En la versión 1.0 no incluí X porque queda fuera de alcance. Está en el roadmap como issue #N."

## 12 preguntas frecuentes con respuesta preparada

### "¿Por qué no usaste un framework JS como React?"

> *"Decisión consciente. El TFG quería demostrar dominio del DOM, eventos y CSS modernos sin abstracciones. El bundle final son menos de 50 KB combinados, sin transpilación. Auditar el código no requiere conocer un framework. Si en el futuro el proyecto creciera, migrar a React sería trivial porque la API REST está completamente desacoplada."*

### "¿Cómo proteges las contraseñas?"

> *"BCryptPasswordEncoder con cost factor 10. Cada hash incluye salt embebido. Las contraseñas en plano nunca se loguean ni se serializan. El bean está declarado en SecurityConfig.java."*

### "Enséñame una transacción compleja en BD"

> *"El procedimiento almacenado sp_marcar_reserva_pagada en database/procedures.sql. Hace UPDATE de la reserva, INSERT de la notificación al usuario, INSERT de la notificación al centro, todo en una transacción START/COMMIT con manejo de excepciones EXIT HANDLER."* — abre el archivo y enseña el procedure.

### "¿Cómo escalarías esto si tuvieras 100.000 usuarios?"

> *"Cuatro pasos en orden de prioridad. Primero, migrar uploads a almacenamiento de objetos como S3 o R2 para que múltiples instancias compartan los archivos — está en el roadmap como issue #6. Segundo, balanceador delante de varias instancias del JAR; la arquitectura ya es stateless gracias a JWT. Tercero, refresh tokens para no obligar a relogin cada 24h. Cuarto, sustituir el poll de notificaciones por WebSockets STOMP. El resto del stack ya escala: Render y Cloud SQL gestionan la BD, MySQL aguanta sin problema 100k usuarios con los índices que tengo."*

### "¿Cómo testean las cosas?"

> *"./mvnw test"* — abre terminal y lo ejecuta. *"31 tests cubren los 5 servicios y controllers más críticos. Tests unitarios con Mockito y AssertJ, sin BD ni Spring context para que sean rápidos. Hay tests de integración con MySQL en GitHub Actions service container."*

### "¿Cómo manejas un error?"

> *"Tres excepciones custom — BadRequestException 400, ResourceNotFoundException 404, UnauthorizedException 401 — y un GlobalExceptionHandler con @RestControllerAdvice que las traduce a JSON con status, message y timestamp. El test ReservaServiceTest.crearReserva_sinPlazas verifica que se lanza BadRequestException cuando no hay plazas."*

### "Enséñame una consulta SQL avanzada"

> *"La fórmula de Haversine en InmersionRepository.findMasCercanas. Calcula la distancia entre dos puntos sobre la superficie de la Tierra usando latitud y longitud. SQL nativo porque no es traducible a JPQL puro. Devuelve una projection de Spring Data — la interfaz InmersionConDistancia — que evita cargar entidades completas."* — abre el archivo y enseña la query.

### "¿Cómo hiciste responsive?"

> *"@media (max-width: 720px) en ocean-theme.css. Bottom sheets para los modales en móvil, inputs de 44 píxeles para target táctil, font-size 16 píxeles para evitar zoom de iOS. Lo demuestro."* — abre DevTools, Toggle Device Toolbar, iPhone 12 Pro, y muéstrale cómo el modal de pago surge desde abajo.

### "¿Tienes documentación API?"

> *"Swagger UI en /swagger-ui.html con OpenAPI 3.0 vía springdoc."* — abre la URL pública + /swagger-ui.html. *"Pulsando Authorize y pegando un JWT puedo ejecutar cualquier endpoint protegido en vivo desde aquí."*

### "¿Cómo subes archivos?"

> *"UploadController con multipart, validación de MIME que debe empezar por image/ o video/, lista blanca de 11 extensiones, y nombre con UUID generado en el servidor — el cliente nunca controla el filename, lo que previene path traversal. 5 tests cubren los caminos."* — abre `UploadController.java` y enseña.

### "Enséñame un patrón de diseño que uses"

> *"Strategy implícito en PaymentController.verificar: tres caminos según el modo — demo, sandbox, live — pero la misma firma pública. Repository en cada *Repository. Filter Chain en Spring Security. Builder en SessionCreateParams.builder de Stripe. Y dependency injection por constructor en todos mis services."*

### "¿Por qué dos ramas en Git?"

> *"master para producción y código estable, claude/peaceful-hellman para desarrollo activo. Hago pull request cuando tengo cambios estables. El pipeline de CI corre en ambas ramas, así que cualquier rotura se ve antes de mergear."*

## Si te quedas en blanco

1. **No te disculpes**. Toma aire.

2. **Repite la pregunta para tu propio beneficio**: *"Sobre la pregunta de cómo X..."*. Te da 2 segundos.

3. **Si no sabes la respuesta concreta**, ofrece lo más cercano que sí sabes: *"Concretamente eso no lo profundicé, pero lo más relacionado en el proyecto es Y, que funciona así..."*.

4. **Si te bloqueas en una pantalla**, vuelve a la slide 30 y dí: *"Si me da un segundo, voy a abrir el archivo correspondiente"*. La autoridad ante un tribunal viene de la calma, no de la velocidad.

---

# Anexo · Plan B si Render se cae durante la defensa

1. **Antes de empezar la defensa, comprueba que la URL pública responde**. Abre la URL en una pestaña 5 minutos antes para "despertarla" del sleep del free tier.

2. **Si durante la demo Render no responde** (ruleta de carga eterna o 502), no entres en pánico. Dí:

> *"El servicio gratuito de Render entra en sleep tras 15 minutos sin tráfico. Voy a usar mi instancia local que tengo arrancada de respaldo."*

3. **Cambia a la pestaña http://localhost:8080**. La app local tiene el mismo código y la misma BD seed. La demo es idéntica.

4. **No menciones que es un fallback** después del primer aviso. Sigue como si nada.

5. **Si te preguntan luego por qué pasó**, contesta:

> *"Render free pone los servicios web a dormir tras 15 minutos sin tráfico para ahorrar recursos. La primera petición tarda 30 segundos en despertarlo. Es una limitación documentada del plan gratuito que aceptamos para la demo. En producción real, el plan Starter de 7 dólares al mes lo evita."*

---

# Anexo · Lo que llevas encima el día de la defensa

## En papel impreso

1. **Este guion** (para ti) — letra grande para leer de un vistazo si te pierdes.
2. **El handout para el tribunal** — 2 páginas A4 a color, una copia por miembro (3-4 copias, mejor llevar 5 por si acaso).
3. **Un bloc de notas en blanco** — para apuntar preguntas mientras te las hacen y poder responderlas en orden.
4. **Tu DNI o documento identificativo** — por si te lo piden.

## En el portátil

1. **Pestañas abiertas en Chrome** (lista al inicio de este guion).
2. **VS Code abierto** con los archivos clave en pestañas.
3. **PowerPoint abierto** con el .pptx en modo presentación lista para empezar (F5).
4. **Terminal abierto** en la raíz del proyecto.
5. **MySQL Workbench abierto** y conectado a la BD local — por si te piden enseñar el procedure en vivo.

## En el bolsillo

1. **Cargador del portátil**.
2. **Adaptador HDMI / USB-C** según el conector del proyector.
3. **Tarjeta SD o pendrive** con copia de seguridad del PDF de memoria, slides y handout — por si el portátil falla al conectarse al proyector.
4. **Botella de agua**.

---

# Anexo · Cronómetro mental durante la defensa

Para no pasarte ni quedarte corto:

- **A los 5 minutos** deberías estar en la slide 5 (3 ejes).
- **A los 10 minutos** ya en plena demo.
- **A los 15 minutos** terminas la demo y vuelves a las slides en la 8.
- **A los 25 minutos** estás cerca de la 22 (CI/CD).
- **A los 35 minutos** estás en la 28 (métricas) o 29 (roadmap).
- **A los 38 minutos** has terminado.

Si vas adelantado, **alarga la demo** mostrando alguna interacción extra (ejemplo: cancelar la reserva tras pagarla y enseñar que las plazas vuelven a sumarse). Si vas atrasado, **acorta el bloque 11** (sostenibilidad puede recortarse en 30 segundos).

¡Suerte!
