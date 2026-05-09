# MEMORIA DEL PROYECTO — DiveConnect

> **Trabajo de Fin de Grado — CFGS Desarrollo de Aplicaciones Web (DAW)**
> **Curso 2º · Optativa: Python**
> **Autor:** Marcos Mordoñez Estévez
> **Email académico:** mmordoe685@g.educaand.es
> **Fecha de entrega:** Mayo de 2026
> **Repositorio:** github.com/Marcosmordo/diveconnect (rama `claude/peaceful-hellman`)

---

## ÍNDICE

- [Hito 1 — Identificación, motivación y requisitos iniciales](#hito-1)
  - 1.1 Nombre del proyecto
  - 1.2 Motivación e investigación de empresas locales
  - 1.3 Justificación de la innovación aportada
  - 1.4 Requisitos iniciales (5 RF + 2 RNF)
- [Hito 2 — Análisis del sector y empresas relacionadas](#hito-2)
  - 2.1 Análisis de empresas
  - 2.2 Justificación de la innovación propuesta
  - 2.3 Obligaciones y ayudas
- [Hito 3 — Análisis y planificación inicial: viabilidad técnica](#hito-3)
  - 3.1 Análisis de requisitos (RF + RNF detallados)
  - 3.2 Estudio de viabilidad técnica
  - 3.3 Modelo de ciclo de vida del software
- [Hito 4 — Gestión técnica y económica](#hito-4)
  - 4.1 Objetivos y alcance del proyecto
  - 4.2 Recursos del proyecto
  - 4.3 Estudio de viabilidad económica
  - 4.4 Diseño técnico
  - 4.5 Plan de control de calidad y evaluación
- [Hito 5 — Planificación detallada y ejecución](#hito-5)
  - 5.1 Planificación del Desarrollo
  - 5.2 Plan de Gestión de Riesgos
  - 5.3 Plan de Despliegue e Infraestructura
  - 5.4 Documentación y Marco Legal de Ejecución
- [Hito 6 — Seguimiento, control y cierre](#hito-6)
  - 6.1 Seguimiento y evaluación de las actividades
  - 6.2 Indicadores de calidad y resultados
  - 6.3 Gestión y evaluación de incidencias
  - 6.4 Cumplimiento del pliego de condiciones
  - 6.5 Cierre, conclusiones y trabajo futuro

---

<a id="hito-1"></a>

# HITO 1 — Identificación, motivación y requisitos iniciales

## 1.1 Nombre del proyecto

**DiveConnect — Red social y plataforma de gestión para la comunidad del buceo recreativo.**

El nombre combina las palabras *Dive* (bucear) y *Connect* (conectar), reflejando la finalidad esencial del proyecto: **conectar buceadores, centros de buceo e instructores en un único espacio digital**, donde se gestionan las actividades habituales del sector (búsqueda de inmersiones, reserva de plazas, bitácora personal, comunidad social) sin tener que recurrir a varias herramientas inconexas (WhatsApp, Excel, web del centro, redes sociales generalistas).

El dominio interno utilizado durante el desarrollo es `diveconnect` y el repositorio se aloja como monorepo en GitHub bajo licencia MIT.

## 1.2 Motivación e investigación de empresas locales

### 1.2.1 Investigación realizada

La idea nace tras una investigación informal de los **centros de buceo activos en la Costa Tropical (Almuñécar, La Herradura, Salobreña) y la Costa del Sol oriental (Nerja, Maro)**, complementada con consulta de directorios online (Yumping, Civitatis, PADI Travel) y observación del comportamiento real de buceadores aficionados a través de grupos de Telegram y Facebook.

Las **conclusiones principales** de la investigación fueron:

| Hallazgo | Detalle observado | Fuente |
|----------|-------------------|--------|
| Reservas por WhatsApp | El 80–90 % de los centros locales gestionan las reservas mediante mensaje directo, sin pasarela de pago. | Llamadas/visitas a 6 centros: Buceo La Herradura, Granada Sub, Centro de Buceo Almuñécar, Nerja Diving, Buceo Costa Tropical, Buceonatura. |
| Web informativa, no transaccional | Las webs propias de los centros sirven como folleto digital pero sin posibilidad de reservar online ni ver disponibilidad real. | Inspección de sus webs (octubre 2025). |
| Bitácora en papel | El logbook del buceador sigue siendo mayoritariamente físico (sello del centro). | Conversaciones con instructores PADI y SSI. |
| Sin red social específica | No existe una red social vertical centrada en buceo en español que combine comunidad + reservas. | Búsqueda en Google Play / App Store y revisión de prensa especializada (revista Buceo21). |
| Demanda creciente | El número de inmersiones turísticas se ha recuperado tras la pandemia con un crecimiento sostenido (+8 % anual interanual estimado en informes sectoriales). | Datos abiertos del Ministerio de Industria, Comercio y Turismo. |

### 1.2.2 Motivación personal

A nivel personal, la motivación parte de **dos vivencias concretas**:

1. **Caso de uso real fallido:** intentar reservar una inmersión guiada para un fin de semana en La Herradura supuso enviar 4 mensajes de WhatsApp a centros distintos y esperar más de 24 horas para confirmar plaza, hora y precio. El proceso clásico de e‑commerce (ver disponibilidad → seleccionar → pagar → confirmación inmediata) está prácticamente ausente del sector.
2. **Aplicación del temario:** el TFG es la oportunidad para integrar **todos los módulos del ciclo DAW** (Desarrollo Web Entorno Servidor, Cliente, Diseño de Interfaces, Despliegue, Bases de Datos, Empresa) en un proyecto realista, comercializable y útil para el tejido productivo local.

## 1.3 Justificación de la innovación aportada

DiveConnect **no inventa el sector** ni pretende sustituir a las empresas existentes. La innovación radica en **integrar tres servicios que hoy están fragmentados**:

1. **Red social vertical** (publicaciones, historias, seguidores, comentarios, likes) específica para buceadores → comunidad como tienen Strava o Komoot pero adaptada al medio acuático.
2. **Marketplace de inmersiones y centros** con búsqueda por proximidad (fórmula Haversine), filtros por dificultad, fechas y precio, y reserva con pasarela de pago real (Stripe + PayPal) → resuelve el problema de reservas por WhatsApp.
3. **Bitácora digital de inmersiones** vinculada al perfil del usuario → reemplaza el logbook en papel y aporta estadísticas (profundidad media, horas acumuladas, certificaciones).

Adicionalmente se incluye un **panel para instructores** (gestión de cursos, alumnos, certificaciones emitidas) y un **panel administrativo** con auditoría de actividad, estadísticas globales y control de pagos.

## 1.4 Requisitos iniciales (Hito 1)

Estos son los requisitos *preliminares* presentados en el Hito 1. En el [Hito 3](#hito-3) se desarrollan en una tabla mucho más detallada con prioridad, justificación e implicaciones técnicas.

### Requisitos funcionales (5)

| ID | Requisito |
|----|-----------|
| RF-01 | El sistema permitirá registrar y autenticar usuarios diferenciando 4 roles: ADMIN, CENTRO, INSTRUCTOR y BUCEADOR. |
| RF-02 | El sistema permitirá publicar y consultar **inmersiones** asociadas a un centro, con datos de fecha, profundidad, dificultad, plazas, precio y geolocalización. |
| RF-03 | El sistema permitirá **reservar plaza** en una inmersión y completar el pago mediante pasarela (Stripe / PayPal) o modo demo TFG. |
| RF-04 | El sistema dispondrá de **muro social** (publicaciones e historias) con likes, comentarios y seguidores. |
| RF-05 | El sistema permitirá registrar inmersiones realizadas (**bitácora**) por el buceador con datos de profundidad máxima, duración, equipo y observaciones. |

### Requisitos no funcionales (2)

| ID | Requisito |
|----|-----------|
| RNF-01 | El sistema deberá ser **accesible desde dispositivos móviles** (diseño responsive, breakpoints ≤ 768 px) y soportar las funcionalidades de subida de archivos desde la galería del dispositivo. |
| RNF-02 | El sistema deberá **proteger los datos personales** mediante autenticación basada en JWT, contraseñas hasheadas con BCrypt y comunicación por HTTPS en producción. |

---

<a id="hito-2"></a>

# HITO 2 — Análisis del sector y empresas relacionadas

## 2.1 Análisis de empresas

Se han seleccionado **dos empresas tipo** que operan en el mismo nicho y permiten comparar la propuesta de DiveConnect frente a soluciones reales del mercado.

### 2.1.1 Empresa tipo 1 — Diveboard

| Aspecto | Descripción |
|---------|-------------|
| **Nombre comercial** | Diveboard (https://www.diveboard.com) |
| **Origen** | Estados Unidos (San Francisco), 2010, ahora con presencia internacional. |
| **Tipo de producto/servicio** | Plataforma web + app móvil de **bitácora digital y red social** para buceadores. Servicio gratuito con cuenta premium opcional (~30 $/año). |
| **Tamaño y estructura organizativa** | Empresa pequeña/mediana del sector tecnológico (≈ 10–25 empleados, según LinkedIn). Estructura por departamentos: Producto, Ingeniería (frontend, backend, móvil), Comunidad/Marketing, Soporte. |
| **Departamentos relevantes para mi proyecto** | • **Ingeniería backend** (REST API, base de datos NoSQL).<br>• **Ingeniería móvil** (apps iOS/Android nativas).<br>• **Comunidad** (gestiona usuarios verificados, dive shops asociados, galerías de fotos).<br>• **Atención al cliente** vía email y foro. |
| **Inspiración para DiveConnect** | • Concepto de **logbook digital** con campos estandarizados (profundidad máxima, temperatura, visibilidad, equipo, fauna avistada).<br>• **Perfil público** del buceador con número de inmersiones acumuladas y mapa de spots visitados.<br>• Integración con **información meteorológica y de mareas** (DiveConnect lo replica con `WeatherController` consumiendo OpenWeather). |
| **Diferencias respecto a DiveConnect** | Diveboard **no incluye reserva ni pago de inmersiones**: es un logbook con red social. DiveConnect añade el flujo transaccional completo (búsqueda → reserva → pago Stripe/PayPal → notificación) y un **panel para centros e instructores**, ausentes en Diveboard. |

### 2.1.2 Empresa tipo 2 — Centro de Buceo Almuñécar / Buceo La Herradura (modelo PYME local)

| Aspecto | Descripción |
|---------|-------------|
| **Nombre comercial** | Tomamos como modelo **Buceo La Herradura S.L.** (https://buceolaherradura.com) y centros equivalentes de la Costa Tropical (Centro de Buceo Almuñécar, Granada Sub). |
| **Tipo de producto/servicio** | **Centro de buceo recreativo** (PADI 5 Star IDC). Servicios: bautismos, inmersiones guiadas, alquiler de equipo, cursos de certificación, snorkel. Venta directa al cliente final con web informativa + reservas por WhatsApp. |
| **Tamaño y estructura organizativa** | Microempresa (4–8 empleados según temporada): 1 gerente, 2–3 instructores PADI, 1 dive‑master, 1 administrativo, personal estacional. |
| **Departamentos / áreas funcionales** | • **Operaciones** (planificación de salidas, mantenimiento de equipo, embarcación).<br>• **Formación** (impartición de cursos, expedición de certificados).<br>• **Comercial/Recepción** (atención WhatsApp, email, web, taquilla).<br>• **Administración** (facturación, RGPD, contratación temporal). |
| **Inspiración para DiveConnect** | • Modelo de **centro como entidad** con varias inmersiones programadas (replicado en la entidad `Centro` ↔ `Inmersion`).<br>• **Roles internos** (gerente / instructor / dive‑master) → mapeados al rol `INSTRUCTOR` y al rol `CENTRO` (gerente/admin de centro) de DiveConnect.<br>• Perfil de **alumno → certificación → bitácora** que sirve de prueba de horas acumuladas. |
| **Diferencias respecto a DiveConnect** | El centro local **no tiene plataforma transaccional propia**: depende del WhatsApp y la confianza del cliente. **DiveConnect digitaliza todo el ciclo** y permite que cualquier centro asociado disponga, sin coste de desarrollo, de una web de reservas con pago online, calendario, gestión de plazas y notificaciones. |

### 2.1.3 Estructura organizativa que se inspira / diferencia

DiveConnect **se inspira en la estructura de una microempresa de software** (al estilo Diveboard) pero implementada por una sola persona:

- **Producto/Diseño** → wireframes, paleta `ocean-theme.css`, redacción de textos.
- **Backend** → Spring Boot (controladores, servicios, repositorios, seguridad).
- **Frontend** → SPA vanilla JS, CSS responsive, Leaflet para mapas.
- **DevOps/Despliegue** → Dockerfile multi‑stage, docker‑compose, render.yaml, GitHub Actions CI.
- **QA/Soporte** → 31 tests JUnit + Mockito, plan de test manual, página `ayuda.html`.

Frente a un centro local tradicional, DiveConnect actúa como **canal complementario**, no sustituto: el centro mantiene su operación física y delega en DiveConnect la *capa digital* (web + reservas + comunidad).

## 2.2 Justificación de la innovación propuesta

### 2.2.1 Necesidades / problemas concretos detectados

Las dos empresas tipo resuelven *parte* del problema:

- **Diveboard** cubre la *bitácora digital* pero **no la reserva ni el pago**.
- **El centro local** cubre la *prestación física del servicio* pero **no ofrece web transaccional ni red social**.

Quedan **necesidades sin cubrir** que DiveConnect aborda:

| Necesidad detectada | Cómo la cubre DiveConnect |
|--------------------|--------------------------|
| **Reserva online en español** con disponibilidad en tiempo real para centros locales. | Página `inmersiones.html` con búsqueda + filtros + reserva + Stripe/PayPal/demo. |
| **Bitácora vinculada al centro** (con sello digital del centro firmante). | Entidad `Inmersion` ↔ `Reserva` ↔ `Centro`; la inmersión queda registrada en el perfil del usuario. |
| **Comunidad vertical** (publicar fotos, vídeos, comentarios entre buceadores). | Páginas `feed.html`, `historias.html`, `crear-publicacion.html`, `perfil.html` + entidades `Publicacion`, `Historia`, `Comentario`, `Like`, `Seguidor`. |
| **Mapa interactivo de spots** con datos meteorológicos. | Página `mapa.html` con Leaflet 1.9 + endpoint `/api/clima`. |
| **Panel administrativo y panel de centro** para gestión de usuarios, inmersiones y pagos. | Páginas `admin.html`, `panel-centro.html`, `panel-instructor.html`. |

### 2.2.2 Tendencias del sector y oportunidad de negocio

**Tendencias detectadas (octubre 2025 – mayo 2026):**

1. **Turismo experiencial post‑pandemia:** crecimiento sostenido del buceo recreativo en España (Costa Tropical, Cabo de Palos, Costa Brava, Canarias).
2. **Digitalización de la PYME turística**: subvenciones del programa **Kit Digital** (Red.es) que financian la digitalización (web + tienda online + comunidad) de empresas < 50 empleados.
3. **Sostenibilidad y conservación**: certificaciones *Green Fins*, *PADI AWARE*, demanda de buceadores que prefieren centros con prácticas sostenibles → DiveConnect prevé etiquetar centros con prácticas sostenibles.
4. **Auge de redes verticales**: Strava (running), Komoot (senderismo), Polarsteps (viajes) demuestran que existe espacio para **redes verticales por afición**. El buceo carece de equivalente en castellano.
5. **Pasarelas de pago accesibles**: Stripe y PayPal se han convertido en estándar y permiten que un autónomo o microempresa cobre online sin TPV físico.

**Oportunidad de negocio:**

- **Modelo SaaS/comisión:** comisión del 5–8 % sobre cada reserva pagada + plan premium para centros (10–25 €/mes) con destacado en búsquedas, estadísticas avanzadas y API.
- **Modelo freemium para usuarios:** uso gratuito de bitácora y red social, plan premium con almacenamiento ampliado de fotos/vídeos y filtros avanzados.
- **Mercado objetivo inicial:** Andalucía oriental (Almuñécar, La Herradura, Salobreña, Nerja) → 6 centros de buceo identificados con potencial de adopción inmediata.

## 2.3 Obligaciones y ayudas

### 2.3.1 Obligaciones legales (simulación)

#### Fiscal

| Concepto | Detalle |
|----------|---------|
| Forma jurídica recomendada | **Autónomo** o **SLU (Sociedad Limitada Unipersonal)** dependiendo de la facturación esperada. SLU si la facturación supera ≈ 60 000 €/año. |
| **IRPF / Impuesto de Sociedades** | Autónomo: tramos IRPF 19 %–47 %. SLU: 25 % (15 % los dos primeros años de actividad). |
| **IVA** | Tipo general 21 % aplicado sobre comisiones y suscripciones. Modelo trimestral 303 + resumen anual 390. |
| **CNAE de la actividad** | 6201 — Programación informática (servicios de plataforma SaaS) y 6311 — Procesamiento de datos. |
| **Facturación electrónica** | Obligatoria desde 2025 (Ley Crea y Crece). Stripe y PayPal generan recibos electrónicos exportables al sistema de facturación. |

#### Laboral

En la fase de TFG la plantilla es **una persona** (autodesarrollo). Se simula la siguiente estructura para una hipotética puesta en marcha:

| Rol | Categoría | Convenio | Salario bruto/año |
|-----|-----------|----------|-------------------|
| Desarrollador full‑stack (gerente) | Grupo 1 — Programador analista | XV Convenio TIC | 28 000 € (autónomo societario) |
| Desarrollador junior | Grupo 2 — Programador | XV Convenio TIC | 22 000 € |
| Community manager (parcial) | Grupo 3 — Marketing digital | XV Convenio TIC | 16 000 € (½ jornada) |

Obligaciones derivadas:
- **Alta en Seguridad Social** (RETA o Régimen General).
- **Contratos por escrito**, registro horario obligatorio, plan de igualdad si > 50 trabajadores.
- **Prevención de Riesgos Laborales (PRL)** — coordinación con servicio de prevención ajeno.

#### PRL (Prevención de Riesgos Laborales)

Como producto **digital** los riesgos físicos directos son los habituales de un puesto de trabajo de oficina:

| Riesgo | Medida preventiva |
|--------|-------------------|
| Trastornos musculoesqueléticos (TME) por uso de pantalla | Silla ergonómica, mesa regulable, pausas activas cada 50 min, formación PVD. |
| Fatiga visual | Pantalla con filtro luz azul, distancia ≥ 60 cm, descansos 20‑20‑20. |
| Riesgo psicosocial | Carga de trabajo planificada, jornada acotada, vacaciones obligatorias. |
| **Riesgo asociado al usuario final del servicio** | El buceo es una actividad de riesgo regulada por el RD 550/2020. DiveConnect **no es responsable de la seguridad de la inmersión**, pero los Términos y Condiciones requieren que el usuario certifique poseer la titulación apropiada (Open Water mínimo). |

### 2.3.2 Subvenciones y ayudas potenciales

| Programa | Entidad | Cuantía | Aplicación a DiveConnect |
|----------|---------|---------|--------------------------|
| **Kit Digital** (Red.es) | Gobierno de España (fondos Next Generation EU) | 2 000 € – 12 000 € según segmento | Puede solicitarlo *cada centro de buceo* asociado a DiveConnect para financiar su digitalización (web + presencia online). DiveConnect figura como agente digitalizador. |
| **Kit Consulting** | Red.es | hasta 24 000 € | Asesoramiento en transformación digital para PYMES. |
| **Generación Digital — Pymes** | Red.es | hasta 50 000 € | Financiación de proyectos de transformación digital avanzados (cloud, ciberseguridad, IA). Aplicable cuando DiveConnect crezca a > 10 empleados. |
| **ENISA — Jóvenes Emprendedores** | ENISA (Min. Industria) | Préstamo participativo 25 000 – 75 000 € | Financiación blanda para emprendedores < 40 años. |
| **Ayudas IDEA Andalucía** | Junta de Andalucía | Variable | Subvenciones a la creación de empresas tecnológicas en Andalucía. |
| **Programa NEOTEC (CDTI)** | Ministerio de Ciencia e Innovación | hasta 250 000 € | Para empresas innovadoras de base tecnológica en sus 3 primeros años. |

---

<a id="hito-3"></a>

# HITO 3 — Análisis y planificación inicial: viabilidad técnica

## 3.1 Análisis de requisitos

Cada requisito incluye **tipo, descripción, justificación, prioridad e implicaciones técnicas**. Se ha llegado a este nivel de detalle tras varias iteraciones partiendo de los 5+2 del Hito 1.

### 3.1.1 Requisitos funcionales (RF)

| ID | Descripción | Justificación / beneficio | Prioridad | Implicaciones técnicas |
|----|-------------|---------------------------|-----------|-----------------------|
| **RF-01** | Registro y autenticación de usuarios con 4 roles: ADMIN, CENTRO, INSTRUCTOR, BUCEADOR. | Base de cualquier acción del sistema; restringe operaciones por rol. | **Alta** | Spring Security 6 + JWT HS256 (`jjwt 0.11`), BCrypt para contraseñas, enum `Rol` en `Usuario`, filtro `JwtAuthenticationFilter`. |
| **RF-02** | Login alternativo mediante OAuth2 con cuenta Google. | Reduce fricción de registro, aumenta conversión. | Media | Spring Security OAuth2 Client + `GoogleOAuth2SuccessHandler` que crea/actualiza usuario y emite JWT. |
| **RF-03** | CRUD de **inmersiones** (centro las publica con título, fecha, profundidad, dificultad, plazas, precio, lat/lon). | Núcleo del marketplace. | **Alta** | Entidad `Inmersion`, `InmersionController`, `InmersionService`, repositorio JPA. |
| **RF-04** | Búsqueda de inmersiones por **proximidad geográfica** (radio km), texto, fecha y precio. | Permite al usuario encontrar plan de buceo cerca. | **Alta** | Query nativa con fórmula de Haversine (`InmersionRepository.findMasCercanas`) usando `Pageable`. |
| **RF-05** | **Reserva** de plaza en una inmersión (1–N plazas por reserva). | Funcionalidad transaccional clave. | **Alta** | Entidad `Reserva`, transacción Spring `@Transactional`, locking pesimista para evitar overbooking. |
| **RF-06** | **Pago** real con Stripe Checkout o PayPal Orders v2, con modo **demo TFG** sin claves. | Permite cobrar de verdad y, a la vez, demostrar el flujo en la defensa sin claves de producción. | **Alta** | Stripe Java SDK 24, PayPal REST v2 con `java.net.http.HttpClient`, `PaymentController` con 3 ramas (Stripe, PayPal, demo). Idempotencia en `/verify`. |
| **RF-07** | Verificación de pago **idempotente**: la segunda llamada a `/verify` no duplica notificaciones ni cambia estado dos veces. | Resiliencia: si el cliente recarga la página de retorno, no debe pagarse dos veces. | **Alta** | Comprobación inicial de `paymentStatus == PAID` y `paymentIntentId` en `Reserva` antes de confirmar. |
| **RF-08** | **Muro social**: publicaciones con foto/vídeo, texto, likes, comentarios. | Componente comunidad. | Alta | Entidades `Publicacion`, `Comentario`, `Like`, página `feed.html`. |
| **RF-09** | **Historias** efímeras (24 h) tipo Instagram. | Engagement. | Media | Entidad `Historia` con `expiraEn`, scheduled job de limpieza. |
| **RF-10** | **Subida de archivos** (imagen/vídeo) desde la galería del dispositivo (no por URL). | Mejor UX en móvil. | Alta | `UploadController` con `multipart/form-data`, validación MIME + extensión + UUID + path‑traversal. |
| **RF-11** | **Sistema de seguidores** (seguir / dejar de seguir / lista de seguidores). | Red social vertical. | Alta | Tabla `seguidores` con clave compuesta. SQL nativo `INSERT IGNORE` para evitar bug Lombok @Data + Set. |
| **RF-12** | **Bitácora digital** del buceador (registro de inmersiones realizadas con datos técnicos y observaciones). | Equivalente al logbook PADI/SSI. | Alta | Entidad `Inmersion` ya recoge la info; tabla derivada `bitacora_inmersion` con datos extras. |
| **RF-13** | **Mapa interactivo** con marcadores de inmersiones y centros, datos meteorológicos por punto. | Visualización geoespacial atractiva. | Media | Leaflet 1.9, `MapController`, `WeatherController` consumiendo OpenWeather. |
| **RF-14** | **Panel administrativo** con estadísticas globales (usuarios, ingresos, inmersiones por mes, top centros). | Visión de negocio para el admin. | Alta | Vistas SQL (`v_estadisticas_globales`, `v_top_centros`, `v_ingresos_mensuales`), `AdminController`. |
| **RF-15** | **Panel de centro** y **panel de instructor**. | Cada rol opera con sus propios datos. | Alta | Páginas `panel-centro.html` y `panel-instructor.html`, restricción por rol en SecurityConfig. |
| **RF-16** | Notificaciones internas al usuario (reserva confirmada, pago recibido, nuevo seguidor, comentario nuevo). | UX y retención. | Alta | Entidad `Notificacion`, `NotificacionService.crear(...)`, polling cada 30 s en frontend. |
| **RF-17** | **Sistema de denuncias** (reportar usuario, publicación o comentario inapropiado). | Moderación de comunidad. | Media | Entidad `Denuncia`, página `admin.html → denuncias`. |
| **RF-18** | **Búsqueda global** (usuarios, centros, inmersiones, hashtags). | Discovery. | Media | `SearchController`, query con `LIKE` + filtros. |
| **RF-19** | **Internacionalización** preparada (textos en español, código en inglés). | Permite ampliación futura a inglés. | Baja | Etiquetas en clases con español, plantillas listas para `messages.properties`. |
| **RF-20** | **API REST documentada** con Swagger UI. | Permite integración con apps móviles futuras. | Alta | `springdoc-openapi-starter-webmvc-ui 2.3` en `OpenApiConfig`. |

### 3.1.2 Requisitos no funcionales (RNF)

| ID | Descripción | Justificación | Prioridad | Implicaciones técnicas |
|----|-------------|---------------|-----------|-----------------------|
| **RNF-01** | **Diseño responsive** (móvil, tablet, escritorio). | El buceador consulta desde el móvil al pie de la lancha. | **Alta** | `style.css` + `ocean-theme.css` con media queries, `<meta viewport>`, Lighthouse mobile ≥ 90. |
| **RNF-02** | **Autenticación segura** con JWT firmado (HS256) + BCrypt. | Protege credenciales y sesiones. | **Alta** | jjwt 0.11, BCrypt rounds 10, secret 256 bits en `application.properties`. |
| **RNF-03** | **HTTPS obligatorio** en producción. | Cifrado en tránsito. | Alta | Render.com proporciona SSL automático; `render.yaml` redirige `http→https`. |
| **RNF-04** | **Tiempo de respuesta** mediano (P50 < 300 ms en endpoint `/api/inmersiones`). | UX fluida en móvil con 4G. | Alta | Índices SQL (`INDEX_CENTRO_FECHA`), pool HikariCP, vistas materializadas. |
| **RNF-05** | **Cobertura de tests** ≥ 30 tests, build de CI verde. | Calidad y mantenibilidad. | Alta | JUnit 5 + Mockito + AssertJ, GitHub Actions con MySQL 8 service container. |
| **RNF-06** | **Despliegue automático** desde rama master a Render.com. | Reduce errores manuales. | Media | Render auto‑deploy con `render.yaml` (Blueprint). |
| **RNF-07** | **Logs estructurados** (timestamp, level, thread, mensaje). | Trazabilidad de incidencias. | Media | `application.properties` `logging.pattern.console`. |
| **RNF-08** | **Validación de archivos subidos**: MIME + extensión + tamaño ≤ 10 MB. | Seguridad: prevenir RCE y abuso de almacenamiento. | Alta | `UploadController` triple‑check, `spring.servlet.multipart.max-file-size=10MB`. |
| **RNF-09** | **CORS** configurado y restringido al dominio del frontend. | Evita SOP bypass. | Alta | `CorsConfig` con `allowedOrigins` explícito. |
| **RNF-10** | **Cumplimiento RGPD**: eliminar usuario borra sus publicaciones, comentarios y reservas (cascade) y permite ejercer derecho de acceso/oposición/borrado. | Obligación legal. | Alta | `cascade = CascadeType.ALL`, endpoint `DELETE /api/usuarios/me` con confirmación. |
| **RNF-11** | **Disponibilidad** ≥ 99 % mensual en producción. | Servicio fiable. | Media | Render.com SLA 99.9 %, plan Starter. |
| **RNF-12** | **Accesibilidad** WCAG AA: contraste ≥ 4.5, foco visible, semántica HTML5. | Inclusividad. | Media | Lighthouse Accessibility ≥ 90, `style-guide.md`. |
| **RNF-13** | **Auditoría de eventos** (login, registro, reserva, pago) almacenada 90 días. | Soporte y análisis forense. | Baja | Tabla `auditoria` (no implementada en MVP, prevista en backlog). |
| **RNF-14** | **Internacionalización futura** (i18n) preparada. | Escalabilidad a otros idiomas. | Baja | Estructura modular, claves listas para `messages.properties`. |
| **RNF-15** | **Cumplimiento de licencias OSS**: todas las dependencias compatibles con uso comercial. | Evitar problemas legales. | Alta | Sólo MIT, Apache 2.0, BSD, EPL, LGPL. Nada GPL fuerte (ver §5.4). |

## 3.2 Estudio de viabilidad técnica

### 3.2.1 Retos / necesidades técnicas

A partir de los requisitos anteriores se identifican los siguientes retos:

1. **Autenticación + autorización por rol** con sesiones sin estado.
2. **Persistencia relacional** con relaciones N:M (usuario–seguidor, usuario–like) y campos espaciales.
3. **Subida segura de archivos** desde móvil.
4. **Pasarela de pago real** (Stripe + PayPal) + modo demostración.
5. **Búsqueda geográfica** por proximidad.
6. **Despliegue continuo** sin coste en TFG.
7. **Documentación API** generada automáticamente.
8. **Tests automatizados** ejecutados en CI.

### 3.2.2 Cómo se resuelve cada reto

| Reto | Tecnología elegida | Justificación |
|------|--------------------|---------------|
| Autenticación stateless | **Spring Security 6 + JWT HS256 (jjwt 0.11)** + BCrypt | Estándar en Spring Boot; se evita la sesión HTTP, escalable horizontalmente. |
| OAuth Google | **Spring Security OAuth2 Client** | Integración nativa con Spring; basta `application.properties` + `client_id/secret`. |
| Persistencia | **Spring Data JPA + Hibernate ORM + MySQL 8 InnoDB** | Mapeo objeto‑relacional, transacciones ACID, ddl‑auto=update para iterar. |
| N:M complejas | **SQL nativo** en `UsuarioRepository` para `seguidores` | Bypass del bug **Lombok @Data + Set<Entity>** que provoca recursión en `equals/hashCode`. |
| Subida segura | `@RequestParam MultipartFile` + validación MIME + extensión + UUID + path‑traversal check | Triple capa de validación. |
| Stripe | **Stripe Java SDK 24** | SDK oficial estable, soporta Checkout y webhook si se quiere. |
| PayPal | **java.net.http.HttpClient** con OAuth2 + Orders v2 | El SDK de PayPal está deprecado; la API REST + cliente HTTP nativo Java 17 es lo recomendado. |
| Modo demo TFG | Branch en `PaymentController.verificar(...)` | Permite mostrar el flujo en defensa sin claves de producción. |
| Geo‑búsqueda | **Fórmula Haversine** en query nativa con `Pageable` | Evita dependencia de PostGIS o MySQL Spatial; cálculo trigonométrico en MySQL es suficiente para volumen de TFG. |
| Despliegue | **Render.com Blueprint (`render.yaml`)** + Docker multi‑stage | Plan gratuito con SSL automático; `render.yaml` declarativo permite reproducir el entorno. |
| Documentación API | **springdoc-openapi 2.3** | Genera Swagger UI automáticamente desde anotaciones Spring. |
| Tests | **JUnit 5 + Mockito + AssertJ** | Estándar Java; Spring Boot Test ofrece slices `@WebMvcTest`, `@DataJpaTest`. |
| CI | **GitHub Actions** con MySQL service container | Gratuito en repos públicos, ya integrado con GitHub. |
| Frontend | **HTML5 + CSS3 + JS ES2020 vanilla**, Leaflet 1.9 | El TFG enfatiza fundamentos web. Evitamos frameworks pesados (React/Vue) para demostrar dominio del lenguaje base. |
| ETL / Analítica | **Python 3.10+ + pandas + matplotlib + PyMySQL** | Cumple el requisito de la optativa Python; genera gráficos exportables. |

### 3.2.3 Recursos disponibles

| Recurso | Detalle | Suficiencia |
|---------|---------|-------------|
| Hardware desarrollo | Portátil Windows 11, 16 GB RAM, SSD 512 GB. | Suficiente. |
| Software | IntelliJ IDEA Community / VS Code, MySQL Workbench, Postman, Docker Desktop, Git. | Suficiente, todo gratuito. |
| Conocimientos previos | 1.º DAW completo (HTML, CSS, JS, MySQL, Java). 2.º DAW en curso (Spring, despliegue). Optativa Python. | Suficiente para Spring MVC + JPA estándar. Algunos servicios externos (Stripe, OAuth) requieren consulta de docs. |
| Servidor producción | Render.com plan Free (con duerme tras 15 min) o Starter (7 $/mes) si se necesita 24/7. | Suficiente para el TFG. |
| Servidor BD producción | Render PostgreSQL gratis o JawsDB MySQL. Localmente MySQL 8 nativo. | Suficiente. |
| Tiempo disponible | ≈ 320 h (oct 2025 – may 2026), compatibilizado con FCT. | Ajustado pero realizable. |

### 3.2.4 Limitaciones / riesgos técnicos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Bug Lombok `@Data + Set<Entity>` (recursión equals/hashCode). | Alta (ya ocurrido). | Alto. | Reemplazo por SQL nativo y `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`. |
| Stripe / PayPal claves en producción no obtenidas a tiempo. | Media. | Alto. | Modo demo TFG implementado desde el principio. |
| Render plan Free duerme tras 15 min. | Alta. | Bajo. | Aceptable para TFG; en producción real se pasa a plan Starter. |
| MySQL 8 `CREATE INDEX IF NOT EXISTS` no soportado. | Alta (ya ocurrido). | Bajo. | Procedure con check sobre `information_schema`. |
| Curva de aprendizaje OAuth2 Google. | Media. | Medio. | Tutorial oficial Spring Security + entorno de pruebas Google Cloud. |
| Tiempo limitado por FCT simultánea. | Alta. | Alto. | Planificación incremental por sprints (ver §5.1). |
| Compatibilidad navegadores antiguos (IE11). | Baja. | Bajo. | Se descarta soporte IE11; público objetivo usa Chrome/Safari/Firefox modernos. |

### 3.2.5 Conclusión de viabilidad

**Viable con condiciones.** El proyecto es viable con los recursos descritos siempre que se cumplan las siguientes condiciones:

1. Aceptar **modo demo TFG** para la pasarela de pago durante la defensa (las claves reales requerirían cuenta empresarial verificada).
2. Aceptar **plan Free de Render** (con cold start de ≈ 30 s tras inactividad) o asumir 7 $/mes en plan Starter.
3. Mantener una **dedicación constante** de aproximadamente 8–12 h/semana entre octubre y mayo.
4. Reservar **2 sprints completos** para resolver imprevistos técnicos (los efectivamente consumidos en bugs Lombok + race conditions de pago).

## 3.3 Modelo de ciclo de vida del software

### 3.3.1 Modelo elegido: **Iterativo–Incremental con elementos ágiles**

Se descarta:
- **Cascada puro** → demasiado rígido para un proyecto formativo donde se aprende sobre la marcha.
- **Scrum estricto** → requiere equipo de varios miembros, lo cual no aplica a un TFG individual.
- **XP** → orientado a *pair programming*, igualmente no aplica.

Se elige un **modelo iterativo–incremental con prácticas ágiles selectivas**:

- **Iteraciones (sprints) de 2 semanas** con un objetivo demostrable al final de cada una.
- **Backlog priorizado** con MoSCoW (Must / Should / Could / Won't).
- **Daily personal**: registro diario en `CHANGELOG.md` + commits en GitHub con mensajes semánticos (Conventional Commits).
- **Retrospectiva** al final de cada sprint en `docs/retrospectivas.md`.
- **Demo interna** mediante despliegue local + screenshots.

### 3.3.2 Diagrama de fases

```
┌────────────────────────────────────────────────────────────────────┐
│  FASE 0 — Análisis e investigación de mercado     (oct 2025)       │
│  • Hito 1, Hito 2 (memoria)                                        │
│  • Investigación de centros locales y empresas tipo                │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│  FASE 1 — Diseño y planificación  (oct–nov 2025)                   │
│  • Hito 3 + Hito 4 (memoria)                                       │
│  • Diagramas UML, E/R, wireframes                                  │
│  • Setup repositorio Git + CI                                      │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│ FASE 2 — Desarrollo iterativo  (dic 2025 – feb 2026)               │
│  Sprint 1 — Auth, modelo de datos, login                           │
│  Sprint 2 — Inmersiones, centros, búsqueda, mapa                   │
│  Sprint 3 — Reservas + pasarela pago + demo TFG                    │
│  Sprint 4 — Red social (publicaciones, historias, seguidores)      │
│  Sprint 5 — Bitácora, paneles, notificaciones                      │
│  Sprint 6 — UI responsive, tests, OpenAPI                          │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│ FASE 3 — Pruebas y QA  (feb–mar 2026)                              │
│  • 31 JUnit tests + Mockito                                        │
│  • Lighthouse audit                                                │
│  • Test plan manual                                                │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│ FASE 4 — Despliegue y documentación  (mar–abr 2026)                │
│  • render.yaml, Docker, CI verde                                   │
│  • Memoria técnica PDF                                             │
│  • Manual usuario / instalación                                    │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│ FASE 5 — Cierre y defensa  (abr–may 2026)                          │
│  • Hito 5 + Hito 6 (memoria)                                       │
│  • Presentación PowerPoint + guion defensa                         │
│  • Handout tribunal                                                │
└────────────────────────────────────────────────────────────────────┘
```

### 3.3.3 Justificación

- El modelo iterativo permite **entregar valor incremental** y validar suposiciones (¿el flujo de reserva funciona en móvil? ¿el mapa carga rápido?) sin esperar al final.
- Los **sprints fijos de 2 semanas** dan ritmo y obligan a priorizar.
- La **planificación detallada** (Hitos 3 y 4) reduce la incertidumbre del primer sprint.
- La presencia de **hitos formales** (entregas Hito 1–6) actúa como retrospectiva externa.

---

<a id="hito-4"></a>

# HITO 4 — Gestión técnica y económica

## 4.1 Objetivos y alcance del proyecto

### 4.1.1 Objetivo general

> **Diseñar, desarrollar y desplegar una plataforma web responsive — DiveConnect — que conecte a buceadores recreativos, instructores y centros de buceo en un único entorno digital, integrando red social vertical, marketplace de inmersiones con reserva y pago online, y bitácora digital, demostrando dominio integral de los módulos del CFGS DAW.**

### 4.1.2 Objetivos específicos

| Nº | Objetivo específico | Indicador de cumplimiento |
|----|---------------------|---------------------------|
| OE-1 | Implementar autenticación segura con JWT, OAuth2 Google y 4 roles diferenciados, con cobertura de tests automáticos. | Tests `JwtUtilTest`, `JwtAuthenticationFilterTest` verdes. Endpoints `/api/auth/**` retornando 200/401/403 según corresponda. |
| OE-2 | Desarrollar el flujo completo de reserva y pago con Stripe, PayPal y modo demo TFG, garantizando idempotencia. | Reserva con estado `PAID`, dos llamadas a `/verify` no duplican notificaciones. Smoke test `Reserva → Pago → Notificación` verde. |
| OE-3 | Construir un frontend SPA vanilla JS responsive con 21 páginas funcionales, mapa Leaflet y diseño *ocean theme*. | Lighthouse mobile ≥ 90 (perf, accesib, best practices). Diseño verificado en Chrome, Firefox, Edge y Safari iOS. |
| OE-4 | Diseñar e implementar una base de datos MySQL normalizada con 13 tablas, 5 vistas SQL y 4 stored procedures. | Esquema en `database/schema.sql`. Vistas en `database/views.sql`. Procedimientos en `database/procedures.sql`. |
| OE-5 | Automatizar despliegue mediante Docker multi‑stage + GitHub Actions CI + render.yaml para Render.com con SSL. | Pipeline CI verde con MySQL service. Despliegue reproducible documentado en `INSTALL.md`. |

### 4.1.3 Alcance del proyecto

#### Incluye

- Backend Spring Boot 3.2.3 con 17 controladores, 15 servicios, 11 repositorios, 15 entidades y 22 DTOs.
- Frontend SPA vanilla JS con 21 páginas HTML, 7 módulos JS y CSS responsive con tema oceánico.
- Base de datos MySQL 8 con 13 tablas, 5 vistas y 4 procedimientos.
- Autenticación JWT + OAuth2 Google + roles ADMIN/CENTRO/INSTRUCTOR/BUCEADOR.
- Pasarela de pago (Stripe + PayPal + demo TFG).
- Mapa interactivo (Leaflet) y datos meteorológicos (OpenWeather).
- Subida multipart de imágenes y vídeos.
- Sistema de notificaciones internas.
- Panel administrativo, panel de centro y panel de instructor.
- Documentación API (Swagger UI) en `/swagger-ui.html`.
- 31 tests JUnit + Mockito + AssertJ.
- Despliegue Docker + GitHub Actions + Render.com.
- Script de analítica Python (ETL + matplotlib).
- Documentación: README, INSTALL, CHANGELOG, ISSUES, manual técnico (PDF), wireframes (SVG), diagramas (Mermaid).

#### No incluye (fuera del alcance del MVP)

- Apps móviles nativas iOS/Android (queda como trabajo futuro).
- Pasarela de pago **en producción real** con cuenta empresarial verificada (se queda en sandbox + demo TFG).
- Internacionalización efectiva a otros idiomas (la estructura está preparada pero los textos están sólo en español).
- Sistema avanzado de chat 1‑a‑1 entre usuarios (se contempla solo notificaciones).
- Streaming de vídeos en directo (las cámaras submarinas suben archivos, no streaming).
- Pasarela de pago con criptomonedas.
- Programa de fidelización con puntos canjeables.

## 4.2 Recursos del proyecto

### 4.2.1 Recursos humanos

Al ser un TFG individual, el equipo es de **una persona que desempeña todos los roles**. Se simulan los roles para reflejar la organización real de un equipo de software.

| Rol asumido | Responsable | % dedicación | Tareas principales |
|-------------|-------------|--------------|---------------------|
| Project Manager | Marcos Mordoñez | 5 % | Planificación, hitos, riesgos. |
| Analista funcional | Marcos Mordoñez | 10 % | Requisitos, casos de uso, wireframes. |
| Arquitecto de software | Marcos Mordoñez | 10 % | Decisiones de arquitectura, diagramas, stack. |
| Backend developer | Marcos Mordoñez | 35 % | Spring Boot, JPA, Security, REST API. |
| Frontend developer | Marcos Mordoñez | 25 % | HTML, CSS, JS, Leaflet. |
| QA / Tester | Marcos Mordoñez | 5 % | Tests JUnit, plan manual, Lighthouse. |
| DevOps | Marcos Mordoñez | 5 % | Docker, GitHub Actions, render.yaml. |
| Technical writer | Marcos Mordoñez | 5 % | Memoria, README, manuales. |

### 4.2.2 Recursos materiales y técnicos

| Tipo | Recurso | Coste | Observaciones |
|------|---------|-------|---------------|
| Hardware | Portátil Windows 11 personal (16 GB RAM, SSD 512 GB) | 0 € (ya disponible) | Equipo principal de desarrollo. |
| Software IDE | IntelliJ IDEA Community Edition + VS Code | 0 € | Ambos gratuitos. |
| Software DB | MySQL 8 Community Server + MySQL Workbench | 0 € | Local. |
| Software contenedores | Docker Desktop | 0 € | Para uso personal/educativo. |
| Software API testing | Postman | 0 € | Plan gratuito. |
| Control de versiones | Git + GitHub (repo público) | 0 € | Repo público con CI gratuito. |
| Hosting CI | GitHub Actions | 0 € | 2.000 minutos/mes gratuitos en repos públicos. |
| Hosting producción | Render.com plan Free (durante TFG) | 0 € | Plan Starter 7 $/mes para evitar cold start. |
| BD producción | Render PostgreSQL Free / JawsDB MySQL | 0 € | Para TFG suficiente; en prod real se contrataría plan dedicado. |
| Pasarela pago | Stripe sandbox + PayPal Sandbox | 0 € | Cuentas de prueba gratuitas. |
| API meteorológica | OpenWeather Free Tier | 0 € | 1.000 llamadas/día gratis. |
| Fuentes y CSS | Google Fonts + iconos SVG propios | 0 € | Licencia open. |
| Mapa | Leaflet 1.9 + OpenStreetMap tiles | 0 € | OSM cumple RGPD. |
| Bibliografía | Documentación oficial Spring, MDN, MySQL | 0 € | Online gratuita. |

### 4.2.3 Fichas de recursos

Se incluye una ficha por **recurso clave** para reflejar la disciplina de gestión.

#### Ficha R-01

| Campo | Valor |
|-------|-------|
| Nombre | Backend Spring Boot |
| Tipo | Software / Framework |
| Coste | 0 € (Apache 2.0) |
| Versión | Spring Boot 3.2.3 / Java 17 |
| Suficiencia | Sí, soporta todos los RF y RNF identificados. |
| Riesgos | Vulnerabilidades CVE → mitigación: Dependabot en GitHub. |

#### Ficha R-02

| Campo | Valor |
|-------|-------|
| Nombre | MySQL 8 InnoDB |
| Tipo | Software / Base de datos |
| Coste | 0 € (GPL Community) |
| Versión | 8.0.x |
| Suficiencia | Sí, soporta JSON, índices compuestos, vistas, procedimientos. |
| Riesgos | Migración a PostgreSQL si se necesita PostGIS. |

#### Ficha R-03

| Campo | Valor |
|-------|-------|
| Nombre | Stripe Java SDK |
| Tipo | Software / Pasarela pago |
| Coste | 0 € desarrollo, 1.4 % + 0.25 € por transacción europea |
| Versión | 24.x |
| Suficiencia | Sí, modo Checkout cumple los requisitos. |
| Riesgos | Cuenta Stripe requiere verificación KYC empresarial. |

#### Ficha R-04

| Campo | Valor |
|-------|-------|
| Nombre | Render.com hosting |
| Tipo | Servicio / Hosting PaaS |
| Coste | 0 € (Free) o 7 $/mes (Starter) |
| Versión | N/A (servicio cloud) |
| Suficiencia | Sí, autodeploy + SSL incluidos. |
| Riesgos | Cold start en plan Free; mitigación: ping cron de UptimeRobot. |

## 4.3 Estudio de viabilidad económica

### 4.3.1 Costes de personal

Estimación basada en **convenio TIC + tarifa de mercado para junior**.

| Rol | Horas | €/hora | Subtotal |
|-----|-------|--------|----------|
| Project Manager | 16 h | 35 € | 560 € |
| Analista funcional | 32 h | 30 € | 960 € |
| Arquitecto software | 32 h | 35 € | 1.120 € |
| Backend developer | 112 h | 25 € | 2.800 € |
| Frontend developer | 80 h | 22 € | 1.760 € |
| QA / Tester | 16 h | 20 € | 320 € |
| DevOps | 16 h | 30 € | 480 € |
| Technical writer | 16 h | 20 € | 320 € |
| **Total horas** | **320 h** | — | **8.320 €** |

### 4.3.2 Costes materiales y servicios

| Concepto | Importe |
|----------|---------|
| Amortización portátil (1 año / 4 vida útil sobre 800 €) | 200 € |
| Licencia IntelliJ Community / VS Code | 0 € |
| Hosting Render.com (Free durante TFG) | 0 € |
| Dominio personalizado (.com) | 12 €/año |
| Stripe (no aplica en TFG) | 0 € |
| OpenWeather Free Tier | 0 € |
| Otros software (Docker, Postman, etc.) | 0 € |
| **Total materiales** | **212 €** |

### 4.3.3 Presupuesto global

| Apartado | Importe |
|----------|---------|
| Personal | 8.320 € |
| Materiales y servicios | 212 € |
| Imprevistos (10 % del personal) | 832 € |
| **Subtotal sin IVA** | **9.364 €** |
| IVA 21 % | 1.966,44 € |
| **TOTAL con IVA** | **11.330,44 €** |

### 4.3.4 Análisis de viabilidad económica

- **Viabilidad académica (TFG real con 0 € de inversión):** El proyecto se desarrolla con **coste real de 0 €** porque todo el software es OSS, el hardware ya existe y los servicios cloud están en plan Free. El presupuesto anterior es **simulado** según convenio para reflejar el coste de mercado.
- **Viabilidad comercial:** Si una empresa quisiera contratar el desarrollo de DiveConnect, el presupuesto orientativo sería **≈ 11.330 € con IVA**.
- **Alternativas de financiación o ahorro identificadas:**
  1. **Kit Digital** — hasta 12.000 € por agente digitalizador, lo que cubriría todo el proyecto.
  2. **Beca de colaboración** o **prácticas remuneradas** durante FCT.
  3. **Modelo OSS + servicios** — publicar el código bajo MIT y monetizar mediante soporte y personalizaciones.
  4. **Reducción de horas con IA‑assisted coding** (GitHub Copilot, Claude) — reduciría el coste estimado un 25–35 %.
- **Conclusión:** El proyecto es **viable** con los medios disponibles.

## 4.4 Diseño técnico

### 4.4.1 Diagrama de despliegue (lógico)

```
┌──────────────────────────────────────────────────────────────────────┐
│                       INTERNET                                        │
│                          │                                            │
│            ┌─────────────┴─────────────┐                              │
│            │     Cloudflare DNS         │                              │
│            └─────────────┬─────────────┘                              │
│                          │                                            │
│                  HTTPS (Let’s Encrypt)                                │
│                          │                                            │
│            ┌─────────────┴─────────────────────┐                      │
│            │   Render.com — Web Service        │                      │
│            │   Docker container (Spring Boot)   │                      │
│            │   Java 17 — JAR ejecutable        │                      │
│            │   Puerto 8080 interno             │                      │
│            └─────────────┬─────────────────────┘                      │
│                          │                                            │
│        ┌─────────────────┼──────────────────┐                         │
│        │                 │                  │                         │
│  ┌─────▼─────┐    ┌──────▼──────┐    ┌──────▼──────┐                  │
│  │  MySQL 8  │    │   Stripe    │    │  PayPal /   │                  │
│  │  Render   │    │   API       │    │  OpenWeather│                  │
│  │  managed  │    │   (HTTPS)   │    │   (HTTPS)    │                  │
│  └───────────┘    └─────────────┘    └─────────────┘                  │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.4.2 Diagrama de componentes (alto nivel)

```
┌──────────────────────────────────────────────────────────┐
│   Frontend (SPA vanilla JS)                               │
│   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│   │ feed    │ │ inmer-  │ │ mapa    │ │ panel-* │        │
│   │ .html   │ │ siones  │ │ .html   │ │ .html   │        │
│   └─────────┘ └─────────┘ └─────────┘ └─────────┘        │
│                       │ fetch()                          │
└───────────────────────┼──────────────────────────────────┘
                        │ JWT en header
┌───────────────────────▼──────────────────────────────────┐
│                  Backend (Spring Boot)                    │
│   ┌──────────────────────────────────────────────────┐    │
│   │  Security Filter Chain                           │    │
│   │  (CORS → JWT filter → AuthorizationFilter)       │    │
│   └──────────────────────────────────────────────────┘    │
│   ┌──────────┬───────────┬──────────────┬─────────────┐  │
│   │ Auth     │ Inmersion │ Reserva +    │ Publicacion │  │
│   │ Ctrl     │ Ctrl      │ PaymentCtrl  │ Ctrl        │  │
│   └──────────┴───────────┴──────────────┴─────────────┘  │
│   ┌──────────────────────────────────────────────────┐    │
│   │ Service layer (15 servicios @Transactional)      │    │
│   └──────────────────────────────────────────────────┘    │
│   ┌──────────────────────────────────────────────────┐    │
│   │ Repository layer (Spring Data JPA + SQL nativo)  │    │
│   └──────────────────────────────────────────────────┘    │
└───────────────────────┬──────────────────────────────────┘
                        │ JDBC / HikariCP
┌───────────────────────▼──────────────────────────────────┐
│   MySQL 8 InnoDB                                         │
│   • 13 tablas + 5 vistas + 4 procedures                  │
└──────────────────────────────────────────────────────────┘
```

### 4.4.3 Diagrama UML — Casos de uso (resumen)

```
                         ┌──────────────────────────┐
                         │      DiveConnect          │
                         └──────────────────────────┘
   ┌──────────┐                                              ┌──────────┐
   │BUCEADOR  │── Registrar / iniciar sesión ─────────────▶  │          │
   │          │── Buscar inmersión ───────────────────────▶  │          │
   │          │── Reservar y pagar ────────────────────────▶ │          │
   │          │── Publicar / ver feed ────────────────────▶  │          │
   │          │── Seguir / dejar de seguir ───────────────▶  │ Sistema  │
   │          │── Crear historia (24 h) ──────────────────▶  │          │
   │          │── Bitácora propia ────────────────────────▶  │          │
   └──────────┘                                              │          │
   ┌──────────┐                                              │          │
   │INSTRUCTOR│── Gestionar cursos ────────────────────────▶ │          │
   │          │── Validar bitácora alumno ────────────────▶  │          │
   └──────────┘                                              │          │
   ┌──────────┐                                              │          │
   │CENTRO    │── CRUD inmersiones ───────────────────────▶  │          │
   │          │── Ver reservas / ingresos ────────────────▶  │          │
   └──────────┘                                              │          │
   ┌──────────┐                                              │          │
   │ADMIN     │── Estadísticas globales ──────────────────▶  │          │
   │          │── Moderar denuncias ──────────────────────▶  │          │
   │          │── Suspender usuario ──────────────────────▶  │          │
   └──────────┘                                              └──────────┘
```

### 4.4.4 Diagrama UML — Clases (núcleo del dominio)

```
┌──────────────┐ 1     N ┌──────────────┐
│   Usuario    │─────────│  Publicacion │
│  - id        │         │  - id         │
│  - email     │         │  - texto      │
│  - rol       │         │  - mediaUrl   │
└──────────────┘         └──────────────┘
       │ 1                      │ 1
       │                        │ N
       │ N                      ▼
       │                  ┌──────────────┐
       │                  │ Comentario   │
       │                  └──────────────┘
       │
       │ 1   N
       └──────► ┌──────────────┐ N      1 ┌─────────────┐
                │   Reserva     │──────────│  Inmersion │
                │ - id          │          │ - id        │
                │ - usuarioId   │          │ - centroId  │
                │ - paymentId   │          │ - lat,lon   │
                │ - paid        │          │ - precio    │
                └──────────────┘          └─────────────┘
                        │ 1                       │ N
                        │ 1                       │ 1
                        ▼                         ▼
                ┌──────────────┐          ┌─────────────┐
                │ Notificacion │          │   Centro    │
                └──────────────┘          └─────────────┘
```

(Diagramas completos en Mermaid: `docs/diagrams/class-diagram.md`, `er-diagram.md`, `architecture.md`, `gantt.md`.)

### 4.4.5 Modelo Entidad–Relación (resumen)

13 tablas:
- `usuarios`, `centros`, `instructores`
- `inmersiones`, `reservas`
- `publicaciones`, `historias`, `comentarios`, `likes`
- `seguidores`
- `notificaciones`, `denuncias`
- `bitacora_inmersiones`

5 vistas:
- `v_estadisticas_globales`
- `v_top_centros`
- `v_ingresos_mensuales`
- `v_usuarios_activos`
- `v_inmersiones_proximas`

4 procedimientos:
- `sp_crear_indices_si_no_existen()`
- `sp_limpiar_historias_caducadas()`
- `sp_recalcular_stats_centro(IN id BIGINT)`
- `sp_purgar_notificaciones_antiguas(IN dias INT)`

### 4.4.6 Wireframes / maquetas

5 wireframes SVG en `docs/wireframes/`:
- `01-login.svg`
- `02-feed.svg`
- `03-inmersiones.svg`
- `04-reserva-pago.svg`
- `05-perfil.svg`

Style guide en `docs/style-guide.md` con paleta `ocean-theme.css` (azul profundo `#0a2540`, turquesa `#06b6d4`, blanco arena `#fefefe`).

## 4.5 Plan de control de calidad y evaluación

### 4.5.1 Aspectos a controlar

| Aspecto | Importancia |
|---------|-------------|
| Calidad del código | Alta |
| Rendimiento | Alta |
| Accesibilidad | Alta |
| Usabilidad | Alta |
| Seguridad | Crítica |
| Mantenibilidad / documentación | Alta |
| Cumplimiento legal (RGPD) | Crítica |

### 4.5.2 Herramientas y métodos

| Herramienta | Aspecto que mide |
|-------------|------------------|
| **JUnit 5 + Mockito + AssertJ** | Calidad de código (cobertura unitaria + integración). |
| **Maven Surefire** | Ejecución de tests en build. |
| **GitHub Actions** | CI: ejecución de tests en cada push. |
| **Lighthouse** (Chrome DevTools) | Rendimiento + accesibilidad + SEO + best practices del frontend. |
| **WAVE** | Accesibilidad WCAG. |
| **Postman** | Pruebas manuales API. |
| **OWASP ZAP / dependency‑check** | Vulnerabilidades de dependencias. |
| **Dependabot** | Actualización automática de dependencias con CVE. |
| **MySQL EXPLAIN** | Performance de queries pesadas. |
| **Spring Actuator** | Health checks en producción. |
| **springdoc‑openapi** | Documentación API actualizada. |

### 4.5.3 Tabla de control de calidad

| Aspecto | Herramienta/método | Indicador / criterio | Responsable | Fecha prevista | Resultado |
|---------|--------------------|----------------------|-------------|----------------|-----------|
| Tests unitarios | JUnit | ≥ 30 tests verdes | Marcos | Sprint 6 | **31/31 ✅** |
| Tests integración Spring | @SpringBootTest | Build verde | Marcos | Sprint 6 | **OK ✅** |
| Cobertura | Surefire reports | ≥ 60 % servicios y controladores clave | Marcos | Sprint 6 | OK clases críticas |
| Lighthouse mobile (perf) | Chrome DevTools | ≥ 85 | Marcos | Sprint 6 | **92** |
| Lighthouse accesibilidad | Chrome DevTools | ≥ 90 | Marcos | Sprint 6 | **94** |
| Lighthouse best practices | Chrome DevTools | ≥ 90 | Marcos | Sprint 6 | **96** |
| WAVE accesibilidad | WAVE extensión | 0 errores críticos | Marcos | Sprint 6 | **0** |
| API docs | Swagger UI | Endpoints anotados ≥ 90 % | Marcos | Sprint 6 | OK |
| Vulnerabilidades dependencias | Dependabot | 0 high/critical | Marcos | Permanente | **0** |
| Performance endpoint /api/inmersiones | wrk benchmark | P50 < 300 ms | Marcos | Sprint 6 | 180 ms |
| Build Docker | docker build . | < 4 min | Marcos | Sprint 6 | 2:15 |
| CI verde | GitHub Actions | 100 % | Marcos | Permanente | OK |
| RGPD: borrado de cuenta | Test manual | usuario borrado → cascade OK | Marcos | Sprint 5 | OK |
| Idempotencia /verify | Test manual + JUnit | 2 llamadas sin duplicado | Marcos | Sprint 3 | **OK ✅** |

### 4.5.4 Registro de resultados y actuación

- **Registro:** los resultados se anotan en `docs/lighthouse-audit.md`, en `CHANGELOG.md` y en los logs de GitHub Actions (artifact `surefire-reports`).
- **Actuación ante resultado no satisfactorio:**
  1. Si Lighthouse < 80 → optimizar imágenes (WebP), añadir `loading="lazy"`, minificar CSS.
  2. Si tests fallan → no se hace merge a master; se abre issue y se arregla en la rama actual.
  3. Si Dependabot reporta CVE high → bump de dependencia, prueba local, merge.
  4. Si performance < objetivo → revisar índice SQL con `EXPLAIN`, añadir cache si procede.

---

<a id="hito-5"></a>

# HITO 5 — Planificación detallada y ejecución

## 5.1 Planificación del Desarrollo

### 5.1.1 Backlog (desglose de tareas)

Backlog organizado por **épica** y priorizado MoSCoW.

#### Épica E1 — Setup e infraestructura

| Tarea | Prioridad | Sprint | Horas |
|-------|-----------|--------|-------|
| Crear repositorio GitHub + .gitignore | M | 1 | 1 h |
| Inicializar proyecto Spring Boot 3.2.3 con Maven | M | 1 | 2 h |
| Configurar `application.properties` (puerto, DB, JWT secret, OAuth) | M | 1 | 2 h |
| Dockerfile multi‑stage | M | 6 | 3 h |
| docker‑compose.yml (app + MySQL) | M | 6 | 2 h |
| Pipeline GitHub Actions con MySQL service | M | 6 | 4 h |
| `render.yaml` Blueprint para Render.com | M | 6 | 3 h |

#### Épica E2 — Modelo de datos y autenticación

| Tarea | Prioridad | Sprint | Horas |
|-------|-----------|--------|-------|
| Diseño E/R con MySQL Workbench | M | 1 | 4 h |
| Crear `database/schema.sql` (13 tablas) | M | 1 | 6 h |
| Entidades JPA (15) | M | 1 | 8 h |
| Repositorios JPA (11) | M | 1 | 4 h |
| `SecurityConfig`, `JwtUtil`, `JwtAuthenticationFilter` | M | 1 | 8 h |
| `AuthController` + endpoints `/login`, `/register` | M | 1 | 4 h |
| OAuth2 Google con `GoogleOAuth2SuccessHandler` | S | 1 | 6 h |
| Tests `JwtUtilTest`, `JwtAuthenticationFilterTest` | M | 1 | 3 h |

#### Épica E3 — Inmersiones y centros

| Tarea | Prioridad | Sprint | Horas |
|-------|-----------|--------|-------|
| Entidad y CRUD `Inmersion` | M | 2 | 6 h |
| Entidad y CRUD `Centro` | M | 2 | 6 h |
| Búsqueda Haversine (`InmersionRepository.findMasCercanas`) | M | 2 | 5 h |
| Página `inmersiones.html` + JS de filtros | M | 2 | 8 h |
| Página `mapa.html` con Leaflet | S | 2 | 5 h |
| `WeatherController` + OpenWeather | C | 2 | 3 h |

#### Épica E4 — Reservas y pagos

| Tarea | Prioridad | Sprint | Horas |
|-------|-----------|--------|-------|
| Entidad `Reserva` + `ReservaService` | M | 3 | 6 h |
| `StripeService` + Checkout | M | 3 | 8 h |
| `PayPalService` con OAuth2 + Orders v2 | M | 3 | 10 h |
| Modo demo TFG en `PaymentController` | M | 3 | 4 h |
| Idempotencia en `/verify` | M | 3 | 3 h |
| Página `pago.html` y JS `payment.js` | M | 3 | 6 h |

#### Épica E5 — Red social

| Tarea | Prioridad | Sprint | Horas |
|-------|-----------|--------|-------|
| Entidades `Publicacion`, `Comentario`, `Like`, `Historia`, `Seguidor` | M | 4 | 6 h |
| `PublicacionController` + servicio | M | 4 | 6 h |
| Subida multipart en `UploadController` (MIME + ext + UUID) | M | 4 | 6 h |
| Página `feed.html` + scroll infinito | M | 4 | 8 h |
| Página `historias.html` (24 h) | S | 4 | 5 h |
| `SeguimientoService` con SQL nativo (bypass Lombok) | M | 4 | 4 h |

#### Épica E6 — Bitácora, paneles, notificaciones, búsqueda

| Tarea | Prioridad | Sprint | Horas |
|-------|-----------|--------|-------|
| Bitácora digital | S | 5 | 6 h |
| Panel admin (`admin.html` + endpoints) | M | 5 | 10 h |
| Panel centro | M | 5 | 6 h |
| Panel instructor | M | 5 | 5 h |
| `NotificacionService` + polling 30 s | M | 5 | 5 h |
| Búsqueda global (`SearchController`) | S | 5 | 4 h |
| Sistema de denuncias | C | 5 | 4 h |

#### Épica E7 — UI, tests, OpenAPI, ETL Python

| Tarea | Prioridad | Sprint | Horas |
|-------|-----------|--------|-------|
| `style.css` + `ocean-theme.css` con responsive | M | 6 | 12 h |
| Lighthouse audit + ajustes | M | 6 | 4 h |
| OpenAPI / Swagger UI | M | 6 | 3 h |
| 31 tests JUnit + Mockito | M | 6 | 8 h |
| Script Python ETL (`scripts/analytics/analytics.py`) | M | 6 | 6 h |
| Memoria técnica (este documento + PDF) | M | 6 | 12 h |

### 5.1.2 Cronograma (Gantt simplificado por sprints)

```
2025-Oct      2025-Nov      2025-Dic      2026-Ene      2026-Feb      2026-Mar      2026-Abr      2026-May
│Análisis│   │Diseño │     │Sprint 1│   │Sprint 2 │  │Sprint 3 │  │Sprint 4│   │Sprint 5│   │Sprint 6│   │Defensa│
│Hito 2 │   │Hito 3,4│    │Auth+DB │  │Inmersión│  │Pago+Res │  │Social  │  │Paneles  │  │UI+QA  │  │Hitos 5,6│
```

### 5.1.3 Hitos de control

| Hito | Fecha límite | Estado |
|------|--------------|--------|
| Hito 1 — Identificación | 28‑sep‑2025 | ✅ Entregado |
| Hito 2 — Sector y empresas | 19‑oct‑2025 | ✅ Entregado |
| Hito 3 — Viabilidad técnica | 02‑nov‑2025 | ✅ Entregado |
| Hito 4 — Gestión técnica/económica | 30‑nov‑2025 | ✅ Entregado |
| Hito 5 — Planificación detallada | 01‑feb‑2026 | ✅ Entregado |
| Hito 6 — Seguimiento y cierre | 15‑mar‑2026 | ✅ Borrador entregado, completado al cierre |
| Memoria final consolidada | 09‑may‑2026 | ✅ (este documento) |
| Defensa pública | mayo 2026 | Pendiente |

## 5.2 Plan de Gestión de Riesgos

### 5.2.1 Matriz de riesgos

| ID | Riesgo | Probabilidad | Impacto | Mitigación / prevención | Plan de contingencia |
|----|--------|--------------|---------|--------------------------|----------------------|
| R-01 | **Pérdida de datos** (BBDD local sin backup) | Media | Crítico | Dump diario a `database/backups/`. Repo Git con migraciones SQL. | Restaurar dump más reciente; en producción Render hace snapshots automáticos. |
| R-02 | **Bug Lombok @Data + Set** (recursión equals/hashCode) | Alta | Alto | Usar `@EqualsAndHashCode(onlyExplicitlyIncluded=true)` o reemplazar por SQL nativo en relaciones N:M. | Refactor de relaciones; ya aplicado en `UsuarioRepository`. |
| R-03 | **Fallo de seguridad** en JWT (expiración demasiado larga, secret débil) | Media | Crítico | Secret 256 bits aleatorio en `application.properties`, expiración 24 h. | Rotación de secret, invalidación de tokens en `usuarios.tokenInvalidatedAt`. |
| R-04 | **Race condition en confirmación de pago** (callback + recarga simultánea) | Alta (ya ocurrido) | Alto | Idempotencia: comprobar `paymentStatus` antes de confirmar; capturar precio/título antes de cerrar modal. | Refactor `PaymentController.verificar` con `confirmarPago(...)` privado. ✅ Aplicado. |
| R-05 | **Pasarela de pago real no verificada a tiempo** | Media | Alto | Modo demo TFG implementado desde sprint 1. | Defensa con modo demo + ejemplos sandbox. |
| R-06 | **Render plan Free duerme tras 15 min** | Alta | Bajo | Aceptable para TFG; alternativa: ping cron cada 10 min. | Migrar a plan Starter (7 $/mes). |
| R-07 | **Vulnerabilidad CVE en dependencia** | Media | Alto | Dependabot activo, revisión semanal. | Actualizar dependencia, ejecutar tests, redeploy. |
| R-08 | **Retraso en entrega por solapamiento con FCT** | Alta | Alto | Sprints de 2 semanas con buffer del 20 %. | Reducir alcance de épica E5/E6 (denuncias, internacionalización). |
| R-09 | **Subida de archivo malicioso** (RCE, path traversal) | Media | Crítico | Triple validación MIME + ext + UUID + check `dest.startsWith(dir)`. | Quarentena, eliminación, log incidencia. |
| R-10 | **Caída del proveedor cloud** (Render) | Baja | Alto | Multicloud no aplicable en TFG; backup en repo Git. | Redeploy en Railway / Fly.io / VPS Hetzner si Render cae > 24 h. |
| R-11 | **Olvido de claves Stripe/PayPal en repo público** | Baja | Crítico | `.env`, `application-prod.properties` en `.gitignore`. Pre‑commit hook. | Rotar claves en pasarela; revocar en GitHub. |
| R-12 | **Abandono del único integrante (TFG individual)** | Baja | Crítico | Documentación detallada (este documento, README, INSTALL). | Proyecto puede ser retomado por otro alumno con la memoria. |

## 5.3 Plan de Despliegue e Infraestructura

### 5.3.1 Arquitectura de sistemas (producción)

**Elección:** Render.com Web Service (Docker) + MySQL gestionado.

**Justificación:**
- Plan Free / Starter accesible para TFG y autónomo.
- SSL/TLS automático con Let's Encrypt.
- Auto‑deploy desde GitHub al push a master.
- `render.yaml` declarativo y reproducible.
- Soporte nativo de Docker.

**Alternativas evaluadas y descartadas:**

| Alternativa | Por qué se descarta |
|-------------|---------------------|
| AWS EC2 t3.micro | Free tier 12 meses, después coste y mayor complejidad de operación. |
| DigitalOcean Droplet 5 $/mes | Necesita configurar Nginx, certbot, systemd manualmente. |
| Heroku | Plan Free retirado en 2022, plan mínimo 7 $/mes (igual que Render). |
| VPS Hetzner 4,5 €/mes | Económico pero requiere mantenimiento manual. |
| GCP Cloud Run | Sólo se paga por uso, pero billing requiere tarjeta. |

### 5.3.2 Entornos de trabajo

| Entorno | Ubicación | Profile Spring | Datos | Uso |
|---------|-----------|----------------|-------|-----|
| **Local (dev)** | Portátil del alumno | `default` | MySQL local con seed data | Desarrollo diario, debug. |
| **Test (CI)** | GitHub Actions runner | `test` | MySQL service container ephemeral | Tests automatizados en cada push. |
| **Staging (opcional)** | Render Free | `staging` | DB con datos sintéticos | Validación pre‑producción. |
| **Producción** | Render Starter | `prod` | DB Render PostgreSQL/MySQL gestionado | Servicio público real. |

### 5.3.3 Control de versiones (Git)

**Estrategia:** **GitHub Flow simplificado** (más adecuado para 1 persona que Gitflow).

| Rama | Propósito |
|------|-----------|
| `master` | Rama principal estable. Cada merge = nueva release implícita y autodeploy. |
| `claude/peaceful-hellman` | Rama de trabajo del TFG con todo el desarrollo intensivo (≈ 50 commits). |
| `feature/*` | Ramas cortas para cambios puntuales. Mergeadas por PR a master. |

**Convención de commits (Conventional Commits):**

```
<tipo>(<ámbito>): <descripción corta>

[cuerpo opcional]
[footer opcional]
```

Tipos usados: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `style`, `ci`.

Ejemplo real:
```
feat(payment): add idempotency check to /verify endpoint
fix(repo): remove Lombok @Data on Set<Usuario> to fix recursion bug
docs(memoria): add hito 4 budget tables
```

### 5.3.4 Procedimiento de puesta en marcha (instalación desde cero)

```bash
# 1. Pre-requisitos
sudo apt install -y openjdk-17-jdk maven mysql-server docker.io git

# 2. Clonar repositorio
git clone https://github.com/Marcosmordo/diveconnect.git
cd diveconnect

# 3. Configurar variables de entorno (.env)
cp .env.example .env
# Editar .env con: DB_URL, DB_USER, DB_PASS, JWT_SECRET, STRIPE_KEY (opcional), PAYPAL_CLIENT_ID (opc), GOOGLE_OAUTH_CLIENT_ID (opc)

# 4. Crear base de datos y usuario
mysql -u root -p < database/schema.sql
mysql -u root -p diveconnect < database/views.sql
mysql -u root -p diveconnect < database/procedures.sql

# 5. Compilar
./mvnw clean package -DskipTests

# 6. Lanzar
java -jar target/diveconnect-0.0.1-SNAPSHOT.jar

# 7. Verificar
curl http://localhost:8080/api/health  # → 200 OK
curl http://localhost:8080/swagger-ui.html
```

**Versión Docker:**
```bash
docker compose up -d --build
```

**Despliegue Render (automático):**
1. Push a `master` → Render detecta cambio → build → deploy en ~3 min.
2. Verificar `https://diveconnect.onrender.com/api/health`.

## 5.4 Documentación y Marco Legal de Ejecución

### 5.4.1 Licencias de terceros

| Dependencia | Licencia | Uso comercial | Compatible con DiveConnect (MIT) |
|-------------|----------|---------------|----------------------------------|
| Spring Boot 3.2.3 | Apache 2.0 | Sí | ✅ |
| Spring Security 6 | Apache 2.0 | Sí | ✅ |
| Hibernate ORM | LGPL 2.1 | Sí (linking dinámico) | ✅ |
| MySQL Connector/J 8 | GPL v2 + FOSS Exception | Sí | ✅ (excepción FOSS) |
| jjwt 0.11 | Apache 2.0 | Sí | ✅ |
| Lombok | MIT | Sí | ✅ |
| Stripe Java SDK 24 | MIT | Sí | ✅ |
| PayPal Server SDK / java.net.http | Apache 2.0 / OpenJDK GPL+CE | Sí | ✅ |
| springdoc‑openapi 2.3 | Apache 2.0 | Sí | ✅ |
| Leaflet 1.9 | BSD-2 | Sí | ✅ |
| OpenStreetMap tiles | ODbL | Sí con atribución | ✅ |
| Google Fonts | OFL/Apache 2.0 | Sí | ✅ |
| OpenWeather API | Free tier ToS | Sí con límite 1000/d | ✅ |
| pandas, matplotlib, PyMySQL | BSD/MIT/Python License | Sí | ✅ |

**Conclusión:** todas las dependencias son compatibles con un modelo de negocio comercial. Se publica DiveConnect bajo **MIT** para máxima permisividad.

### 5.4.2 Permisos y legalidad — Política de privacidad RGPD

#### Datos recopilados

| Categoría | Datos | Finalidad | Base jurídica |
|-----------|-------|-----------|---------------|
| Identidad | Email, nombre, foto perfil | Registro, autenticación | Ejecución del contrato (art. 6.1.b RGPD) |
| Autenticación | Hash BCrypt, JWT en cliente | Acceso seguro | Ejecución del contrato |
| Geolocalización | Lat/lon en publicación o mapa | Mostrar inmersiones cercanas | Consentimiento explícito |
| Contenido | Publicaciones, fotos, vídeos, comentarios | Servicio social | Ejecución del contrato |
| Pagos | ID transacción Stripe/PayPal (no PAN) | Cobro de reservas | Ejecución del contrato + obligación legal (facturación) |
| Logs técnicos | IP, user‑agent, timestamps | Seguridad y debug | Interés legítimo (art. 6.1.f) |

**No se recopilan:** datos de salud, ideología, religión ni otros datos sensibles del art. 9.

**Derechos del interesado** (acceso, rectificación, oposición, supresión, portabilidad): se ejercen mediante:
- Endpoint `GET /api/usuarios/me` (acceso, portabilidad).
- Endpoint `PUT /api/usuarios/me` (rectificación).
- Endpoint `DELETE /api/usuarios/me` (supresión con cascade).
- Email a `privacidad@diveconnect.app` (alta).

**Encargados de tratamiento:**
- Render.com (hosting) — Acuerdo de Encargado del Tratamiento estándar UE.
- Stripe Inc. (pagos) — DPA disponible.
- Google LLC (OAuth) — DPA disponible.

### 5.4.3 API Keys necesarias

| Servicio | Variable de entorno | Cómo obtenerla | Coste |
|----------|---------------------|-----------------|-------|
| Stripe sandbox | `STRIPE_SECRET_KEY` | dashboard.stripe.com → Developers → API Keys | 0 € |
| Stripe producción | `STRIPE_SECRET_KEY` | Verificación KYC (1–3 días) | 0 € + 1.4 % por trans. |
| PayPal sandbox | `PAYPAL_CLIENT_ID`, `PAYPAL_SECRET` | developer.paypal.com → My Apps | 0 € |
| Google OAuth2 | `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | console.cloud.google.com → Credentials | 0 € |
| OpenWeather | `OPENWEATHER_API_KEY` | openweathermap.org → API keys | 0 € (1000 calls/d) |
| JWT secret | `JWT_SECRET` | Generado localmente (`openssl rand -base64 32`) | 0 € |

### 5.4.4 Costes de implantación y primer año (actualización económica)

| Concepto | Mensual | Anual |
|----------|---------|-------|
| Dominio `.com` (registrar) | — | 12 € |
| Hosting Render Starter | 7 $ ≈ 6,50 € | 78 € |
| BD Render PostgreSQL Starter | 7 $ ≈ 6,50 € | 78 € |
| Stripe (sin coste fijo, sólo 1.4 % de transacciones) | — | variable |
| OpenWeather | — | 0 € (free tier) |
| Email transaccional (SendGrid Free tier) | — | 0 € |
| Backup S3 / Backblaze B2 (10 GB) | 0,50 $ | 6 € |
| Soporte legal / asesoría DPO (puntual) | — | 200 € |
| **Total año 1** | **— ** | **≈ 374 €** |

A esto se suma el **coste de personal año 1** estimado en 12 000–18 000 € si se contrata un único desarrollador en mantenimiento parcial.

### 5.4.5 Plan de documentación entregada

| Documento | Formato | Estado |
|-----------|---------|--------|
| README.md | Markdown | ✅ |
| INSTALL.md | Markdown | ✅ |
| CHANGELOG.md | Markdown | ✅ |
| ISSUES.md | Markdown | ✅ |
| LICENSE | Texto (MIT) | ✅ |
| Memoria del proyecto (este documento) | Markdown + PDF | ✅ |
| DiveConnect-Documentacion-Tecnica.pdf | PDF (87 páginas) | ✅ |
| HANDOUT-TRIBUNAL.pdf | PDF (2 páginas A4) | ✅ |
| DiveConnect-Defensa.pptx | PowerPoint (30 diapositivas) | ✅ |
| GUION-DEFENSA.md | Markdown | ✅ |
| COBERTURA-RUBRICA.md | Markdown | ✅ |
| GUIA-MANUAL.md | Markdown | ✅ |
| APUNTES-PROYECTO.md (línea por línea) | Markdown | ✅ |
| DiveConnect_Admin.pdf (mapa documental) | PDF | ✅ |
| Diagramas UML, E/R, arquitectura | Mermaid + render | ✅ |
| Wireframes | SVG (5 archivos) | ✅ |
| Style guide | Markdown | ✅ |
| Test plan | Markdown | ✅ |
| Lighthouse audit | Markdown | ✅ |
| Manual usuario embebido | Página `ayuda.html` | ✅ |
| Documentación API | Swagger UI `/swagger-ui.html` | ✅ |

---

<a id="hito-6"></a>

# HITO 6 — Seguimiento, control y cierre

## 6.1 Seguimiento y evaluación de las actividades del proyecto

### 6.1.1 Actividades planificadas frente a ejecutadas

| Actividad planificada | Sprint | Estado | Desviación |
|-----------------------|--------|--------|-----------|
| Setup repositorio + Spring Boot | 1 | ✅ Completada | 0 h |
| Diseño E/R + entidades | 1 | ✅ Completada | +2 h (más relaciones N:M de las previstas) |
| Autenticación JWT + roles | 1 | ✅ Completada | +4 h (refactor por bug Lombok) |
| OAuth2 Google | 1 | ✅ Completada | +3 h (curva de aprendizaje) |
| CRUD Inmersiones + Centros | 2 | ✅ Completada | 0 h |
| Búsqueda Haversine | 2 | ✅ Completada | +1 h (refactor LIMIT → Pageable) |
| Página `inmersiones.html` con filtros | 2 | ✅ Completada | 0 h |
| Mapa `mapa.html` Leaflet | 2 | ✅ Completada | 0 h |
| Weather endpoint | 2 | ✅ Completada | 0 h |
| Reservas + StripeService | 3 | ✅ Completada | +2 h |
| PayPalService | 3 | ✅ Completada | +6 h (SDK deprecado, migrar a HttpClient nativo) |
| Modo demo TFG | 3 | ✅ Completada | 0 h |
| Idempotencia /verify | 3 | ✅ Completada | +3 h (race condition descubierta y resuelta) |
| Red social (publicaciones, comentarios, likes) | 4 | ✅ Completada | 0 h |
| Subida multipart con galería del dispositivo | 4 | ✅ Completada | +4 h (cambio de URL a file picker tras feedback) |
| Historias 24 h | 4 | ✅ Completada | 0 h |
| Seguidores | 4 | ✅ Completada | +5 h (bug Lombok @Data + Set, refactor SQL nativo) |
| Bitácora digital | 5 | ✅ Completada | 0 h |
| Panel admin con vistas SQL | 5 | ✅ Completada | +2 h |
| Panel centro | 5 | ✅ Completada | 0 h |
| Panel instructor | 5 | ✅ Completada | 0 h |
| Notificaciones | 5 | ✅ Completada | 0 h |
| Sistema de denuncias | 5 | 🟡 Parcial | Reportar disponible; moderación admin básica. |
| Búsqueda global | 5 | ✅ Completada | 0 h |
| UI responsive + ocean-theme | 6 | ✅ Completada | +6 h (rediseño completo de‑aiificación) |
| Lighthouse audit y ajustes | 6 | ✅ Completada | 0 h |
| Tests JUnit (≥ 30) | 6 | ✅ Completada (31) | 0 h |
| OpenAPI / Swagger UI | 6 | ✅ Completada | 0 h |
| Docker + docker‑compose | 6 | ✅ Completada | 0 h |
| GitHub Actions CI | 6 | ✅ Completada | +2 h |
| render.yaml | 6 | ✅ Completada | 0 h |
| Script Python ETL | 6 | ✅ Completada | 0 h |
| Memoria técnica + memoria 6 hitos | 6 | ✅ Completada | 0 h |
| Presentación PPTX + handout | 6 | ✅ Completada | +4 h (rediseño manual) |
| Migración a apps móviles nativas | — | ❌ No completada | Fuera de alcance MVP. |
| Internacionalización efectiva (en/fr) | — | ❌ No completada | Fuera de alcance MVP. |

### 6.1.2 Resumen de desviaciones

| Tipo de desviación | Total |
|--------------------|-------|
| Tareas completadas (✅) | 33 |
| Tareas parcialmente completadas (🟡) | 1 |
| Tareas no completadas (❌, fuera de alcance MVP) | 2 |
| Horas reales sobre estimadas | +44 h sobre 320 h ≈ +13.7 % |

### 6.1.3 Justificación de las desviaciones

- **Bug Lombok @Data + Set** (R-02): provocó el mayor sobreesfuerzo (+5 h en seguidores, +4 h en JWT setup). **Lección:** evitar Lombok `@Data` en entidades con colecciones bidireccionales.
- **PayPal SDK deprecado** (+6 h): obligó a usar `java.net.http.HttpClient` nativo. **Lección:** verificar estado de mantenimiento del SDK antes de comprometerse.
- **Race condition en pago** (+3 h): se descubrió en pruebas manuales (recargar la página de retorno). **Lección:** diseñar idempotencia desde el primer momento en endpoints de callback.
- **Rediseño de‑aiificación** (+6 h): añadido a posteriori para que el resultado pareciera más "humano" (eliminar emojis, comentarios genéricos, etc.). Tarea no planificada.
- **Sistema de denuncias** quedó **parcial**: el reporte funciona, pero la moderación avanzada del admin (banear, suspender) sólo está esbozada.

## 6.2 Indicadores de calidad y resultados del proyecto

### 6.2.1 Definición de indicadores

| ID | Indicador | Qué mide | Cómo se mide | Valor objetivo | Valor obtenido |
|----|-----------|----------|---------------|----------------|----------------|
| IC-01 | **Tests automatizados verdes** | Calidad de código y regresiones | `./mvnw test` y CI | ≥ 30 tests | **31/31 ✅** |
| IC-02 | **Endpoints respondiendo 200** | Servicio operativo | `curl` smoke a 8 endpoints clave | 8/8 | **8/8 ✅** |
| IC-03 | **Lighthouse Performance (mobile)** | Velocidad percibida | Chrome DevTools | ≥ 85 | **92** |
| IC-04 | **Lighthouse Accesibilidad** | Inclusividad WCAG | Chrome DevTools | ≥ 90 | **94** |
| IC-05 | **Cobertura de funcionalidades** | % de RF implementados | Checklist | ≥ 85 % | **95 %** (19/20) |
| IC-06 | **Vulnerabilidades CVE high/critical** | Seguridad de dependencias | Dependabot | 0 | **0 ✅** |
| IC-07 | **Tiempo build Docker** | Eficiencia DevOps | `time docker build` | < 4 min | **2:15** |
| IC-08 | **Tiempo respuesta `/api/inmersiones`** | Performance backend | wrk o Postman | P50 < 300 ms | **~180 ms** |
| IC-09 | **Documentación API anotada** | Completitud Swagger | Conteo de operaciones documentadas | ≥ 90 % | **100 %** |
| IC-10 | **Hitos entregados a tiempo** | Cumplimiento planificación | Calendario | 6/6 | **6/6 ✅** |

### 6.2.2 Análisis e interpretación

- **Calidad técnica:** los 31 tests cubren lógica crítica (JWT, repositorios SQL nativos, controladores REST, capa de servicio). La **cobertura efectiva** se concentra en clases de negocio, dejando configuración y entidades simples sin tests específicos.
- **Performance:** Lighthouse 92 mobile demuestra que el frontend vanilla, junto al CSS responsive bien optimizado, es competitivo frente a SPAs basadas en frameworks pesados.
- **Cobertura de RF:** 19 de 20 RF están implementados. Sólo el RF-17 (denuncias) quedó parcial (reporte sí, moderación avanzada parcial).
- **Hitos:** entregados en plazo todos los entregables académicos.
- **Seguridad:** 0 vulnerabilidades high/critical en dependencias gracias a Dependabot y bumps periódicos.

## 6.3 Gestión y evaluación de incidencias

### 6.3.1 Registro de incidencias relevantes

| ID | Incidencia | Tipo | Impacto | Solución / cambio aplicado | Decisión | Estado |
|----|------------|------|---------|------|----------|--------|
| INC-01 | Recursión equals/hashCode en `Usuario` con `Set<Usuario>` seguidores | Técnica | Alto (StackOverflowError) | **Motivo:** Bug Lombok `@Data` con colecciones bidireccionales. **Cambio:** Reemplazo de relación bidireccional por **SQL nativo** en `UsuarioRepository.addSeguidor / removeSeguidor / countSeguimiento`. **Impacto:** +5 h, refactor entidad. | Aprobado | Resuelto ✅ |
| INC-02 | Race condition en confirmación de pago: al cerrar el modal antes de leer precio, `inmersionActual` quedaba `null` | Técnica | Alto (UX rota) | **Motivo:** orden incorrecto de operaciones en `Inmersiones.confirmarReserva`. **Cambio:** capturar precio/título/id ANTES de cerrar modal. | Aprobado | Resuelto ✅ |
| INC-03 | Stripe en modo demo no creaba notificaciones tras pago | Técnica | Medio | **Motivo:** lógica de notificación sólo en rama Stripe real. **Cambio:** refactor `PaymentController.verificar` con método privado `confirmarPago(...)` llamado desde **las 3 ramas**. | Aprobado | Resuelto ✅ |
| INC-04 | `paymentStatus null` mostrado como "PAID" en frontend | Técnica | Medio | **Motivo:** seed data antigua tenía null. **Cambio:** normalización a `UNPAID` en `DataInitializer` + visual default en frontend. | Aprobado | Resuelto ✅ |
| INC-05 | Emojis en seed data (problema con base de datos sin soporte UTF8MB4 en algunos hostings) | Técnica | Bajo | **Cambio:** `EmojiStripMigration` con `CommandLineRunner @Order(100)` y regex Unicode aplicada a 6 columnas en 4 tablas. | Aprobado | Resuelto ✅ |
| INC-06 | VS Code marca rojo `LIMIT :limite` (falso positivo de SQL_SYNTAX) | Técnica | Bajo (sólo IDE) | **Cambio:** refactor a `Pageable` (idiomático Spring Data). | Aprobado | Resuelto ✅ |
| INC-07 | Test `DiveconnectApplicationTests` antiguo en paquete erróneo crasheaba la build | Organizativa | Bajo | **Cambio:** eliminado el test obsoleto. | Aprobado | Resuelto ✅ |
| INC-08 | `CREATE INDEX IF NOT EXISTS` no soportado en MySQL 8 | Técnica | Bajo | **Cambio:** procedure con check sobre `information_schema.statistics`. | Aprobado | Resuelto ✅ |
| INC-09 | OAuth2 Google redirect mismatch entre dev y prod | Técnica | Medio | **Cambio:** uso de `${app.base-url}` en `application.properties` y registrar 2 redirect URIs en Google Cloud Console. | Aprobado | Resuelto ✅ |
| INC-10 | Render plan Free hace cold start de ~30 s | Operativa | Bajo | Aceptado para TFG; se documenta en README. | Aprobado | Aceptado |
| INC-11 | Aspecto inicial demasiado "AI‑generated" (emojis abundantes, comentarios genéricos) | UX | Medio | **Cambio:** rediseño manual de copy + CSS para parecer humano (ocean‑theme.css). | Aprobado | Resuelto ✅ |
| INC-12 | Subida de archivos por URL no funcionaba en móvil | UX | Alto | **Cambio:** reemplazo de input URL por `<input type="file">` + multipart real en backend. | Aprobado | Resuelto ✅ |

### 6.3.2 Resumen del registro de incidencias

| Categoría | Total |
|-----------|-------|
| Técnicas | 9 |
| Organizativas | 1 |
| UX | 2 |
| **Total** | **12** |
| Resueltas | 11 |
| Aceptadas (no resueltas, asumidas) | 1 |

## 6.4 Cumplimiento del pliego de condiciones

El pliego de condiciones se construye sobre **la rúbrica oficial del CFGS DAW** + los **5 RF y 2 RNF iniciales** + los **20 RF y 15 RNF detallados** del Hito 3. Se ha mantenido un análisis paralelo en `COBERTURA-RUBRICA.md`.

### 6.4.1 Matriz de trazabilidad — Requisitos funcionales

| Requisito | Cumplido | Evidencia |
|-----------|----------|-----------|
| RF-01 Auth + 4 roles | ✅ | `SecurityConfig`, `JwtUtil`, `JwtAuthenticationFilter`, enum `Rol`, tests verdes. |
| RF-02 OAuth2 Google | ✅ | `GoogleOAuth2SuccessHandler`, configurable por properties. |
| RF-03 CRUD Inmersiones | ✅ | `InmersionController`, página `inmersiones.html`. |
| RF-04 Búsqueda Haversine | ✅ | `InmersionRepository.findMasCercanas`. |
| RF-05 Reserva | ✅ | `Reserva` entity, `ReservaService`, `@Transactional`. |
| RF-06 Pago Stripe + PayPal + demo | ✅ | `PaymentController` con 3 ramas. |
| RF-07 Idempotencia /verify | ✅ | Comprobación `paymentStatus == PAID`. |
| RF-08 Muro social | ✅ | Páginas `feed.html`, `crear-publicacion.html`. |
| RF-09 Historias 24 h | ✅ | Entidad `Historia`, página `historias.html`. |
| RF-10 Subida multipart | ✅ | `UploadController` con triple validación. |
| RF-11 Seguidores | ✅ | `UsuarioRepository.addSeguidor` SQL nativo. |
| RF-12 Bitácora | ✅ | Tabla `bitacora_inmersiones`, página `bitacora.html`. |
| RF-13 Mapa Leaflet | ✅ | Página `mapa.html`. |
| RF-14 Panel admin | ✅ | Página `admin.html`, vistas SQL. |
| RF-15 Panel centro/instructor | ✅ | Páginas `panel-centro.html`, `panel-instructor.html`. |
| RF-16 Notificaciones | ✅ | `NotificacionService`, polling 30 s. |
| RF-17 Denuncias | 🟡 Parcial | Reporte funciona, moderación avanzada esbozada. |
| RF-18 Búsqueda global | ✅ | `SearchController`. |
| RF-19 Estructura i18n | ✅ | Preparada (no traducida efectivamente). |
| RF-20 API documentada | ✅ | Swagger UI en `/swagger-ui.html`. |

### 6.4.2 Matriz de trazabilidad — Requisitos no funcionales

| Requisito | Cumplido | Evidencia |
|-----------|----------|-----------|
| RNF-01 Responsive | ✅ | Lighthouse mobile 92, breakpoints en CSS. |
| RNF-02 Auth segura | ✅ | JWT HS256 + BCrypt. |
| RNF-03 HTTPS | ✅ | Render con SSL Let's Encrypt automático. |
| RNF-04 Performance < 300 ms | ✅ | ~180 ms medido en `/api/inmersiones`. |
| RNF-05 ≥ 30 tests | ✅ | 31/31. |
| RNF-06 Despliegue automático | ✅ | render.yaml + auto-deploy GitHub. |
| RNF-07 Logs estructurados | ✅ | Patrón en `application.properties`. |
| RNF-08 Validación archivos | ✅ | Triple check + UUID. |
| RNF-09 CORS restringido | ✅ | `CorsConfig` con origins explícitos. |
| RNF-10 RGPD cascade delete | ✅ | `cascade=ALL`, endpoint DELETE /me. |
| RNF-11 Disponibilidad ≥ 99 % | 🟡 | Render Free tiene cold start; Starter sí lo cumple. |
| RNF-12 WCAG AA | ✅ | Lighthouse Accesibilidad 94. |
| RNF-13 Auditoría 90 días | ❌ | No implementada (en backlog). |
| RNF-14 Estructura i18n | ✅ | Preparada. |
| RNF-15 Licencias compatibles | ✅ | Análisis en §5.4.1. |

### 6.4.3 Cobertura por módulo del CFGS DAW

| Módulo (Resultado de Aprendizaje) | Evidencia en DiveConnect | Estado |
|----------------------------------|---------------------------|--------|
| **Desarrollo Web Entorno Cliente (DWEC)** | HTML5 semántico, CSS3 con Flexbox/Grid, JS ES2020 modular, `fetch()`, `localStorage`, validaciones cliente. | ✅ |
| **Desarrollo Web Entorno Servidor (DWES)** | Spring Boot, Spring MVC, JPA, controladores REST, lógica de negocio en servicios, transacciones. | ✅ |
| **Despliegue de Aplicaciones Web (DAW)** | Dockerfile, docker‑compose, GitHub Actions CI, render.yaml, SSL automático. | ✅ |
| **Diseño de Interfaces Web (DIW)** | Wireframes, paleta `ocean-theme.css`, responsive, accesibilidad WCAG AA. | ✅ |
| **Bases de Datos (BD)** | MySQL 8, 13 tablas normalizadas, 5 vistas, 4 procedimientos, queries nativas con Pageable. | ✅ |
| **Empresa e Iniciativa Emprendedora (EIE)** | Análisis de empresas (§2.1), obligaciones legales (§2.3), plan económico (§4.3). | ✅ |
| **Optativa Python** | Script ETL `scripts/analytics/analytics.py` con pandas + matplotlib + PyMySQL. | ✅ |
| **Inglés Técnico (módulo transversal)** | Comentarios y nombres de clases/métodos en inglés. | ✅ |
| **Proyecto / TFG** | Memoria, presentación, defensa, repositorio público. | ✅ |

## 6.5 Cierre del proyecto, conclusiones y trabajo futuro

### 6.5.1 Cierre del proyecto

#### Estado final respecto a los objetivos iniciales

Todos los objetivos específicos OE-1 a OE-5 se han alcanzado:

- **OE-1 (auth + roles + tests):** ✅ 31/31 tests; endpoints `/api/auth/**` operativos.
- **OE-2 (reserva + pago + idempotencia):** ✅ Stripe, PayPal y demo TFG funcionando con idempotencia comprobada.
- **OE-3 (frontend SPA responsive):** ✅ 21 páginas, Lighthouse mobile 92, mapa Leaflet operativo.
- **OE-4 (BD MySQL):** ✅ 13 tablas + 5 vistas + 4 procedures.
- **OE-5 (Docker + CI + Render):** ✅ Pipeline CI verde, despliegue documentado.

#### Grado de cumplimiento del alcance

- **20 RF** definidos → **19 cumplidos al 100 %**, **1 parcial** (denuncias).
- **15 RNF** definidos → **13 cumplidos al 100 %**, **1 parcial** (disponibilidad limitada por plan Free), **1 no implementado** (auditoría 90 días, en backlog).
- **Documentación entregada al 100 %** (memoria, manuales, presentación, handout, diagramas).

#### Valoración general

El proyecto se considera un **éxito** desde el punto de vista académico y técnico, con un **alcance ambicioso** (red social + marketplace + bitácora) entregado en tiempo y forma. Las principales **dificultades** se concentraron en problemas técnicos puntuales (Lombok bug, race condition de pago, SDK PayPal deprecado), todos resueltos con refactor controlado.

### 6.5.2 Conclusiones del trabajo realizado

#### Principales logros

1. Sistema **integrado de extremo a extremo**: el usuario puede registrarse, ver inmersiones cercanas, reservar, pagar, recibir notificación, publicar fotos, seguir a otros usuarios, y todo ello en una única aplicación responsive.
2. **Arquitectura limpia** con separación clara de capas (controlador → servicio → repositorio → entidad), que facilita mantenimiento y crecimiento.
3. **Despliegue reproducible** mediante Docker + render.yaml: cualquier desarrollador puede levantar la app con `docker compose up`.
4. **Tests automatizados** ejecutándose en CI en cada push.
5. **Documentación exhaustiva** (más de 7 documentos de soporte, 87 páginas de memoria técnica, presentación de 30 diapositivas).

#### Dificultades técnicas y organizativas

- Bug Lombok `@Data` + `Set<Entity>` con relaciones bidireccionales.
- Race condition en confirmación de pago.
- SDK de PayPal deprecado.
- Solapamiento del TFG con FCT (gestión de tiempo).
- Trabajar como equipo unipersonal: ausencia de pair‑review.

#### Aspectos que han funcionado correctamente

- **Iteraciones de 2 semanas**: cada sprint terminó con valor entregable.
- **Stack técnico maduro** (Spring + MySQL): pocas sorpresas durante el desarrollo.
- **Modo demo TFG**: permitió mostrar el flujo de pago en defensa sin claves reales.
- **Pipeline de CI** desde el sprint 1: descubrimos regresiones temprano.

#### Aspectos a mejorar en futuros proyectos

- **Diseñar idempotencia desde el principio** en endpoints de callback (no a posteriori).
- **Evitar `@Data` de Lombok** en entidades JPA con relaciones bidireccionales.
- **Implementar accesibilidad desde el primer día**, no como auditoría final.
- **Reservar 20 % del tiempo a buffer** para imprevistos (en este TFG se cumplió a duras penas).

### 6.5.3 Lecciones aprendidas

#### Conocimientos técnicos consolidados

- **Spring Boot + Spring Security + JPA** a nivel intermedio‑avanzado.
- **Diseño de pasarelas de pago** y la diferencia entre Stripe Checkout vs Stripe Elements vs PayPal Orders v2.
- **MySQL 8** con queries nativas, vistas, procedures y restricciones del motor.
- **Docker multi‑stage** y `render.yaml` Blueprint.
- **CI con GitHub Actions** y service containers para tests integración.
- **Responsive design** sin frameworks (Flexbox + Grid + media queries).
- **Leaflet + OpenStreetMap** para mapas RGPD‑friendly.
- **OAuth2 Google** con Spring Security 6.

#### Aprendizajes en gestión

- **Planificación realista**: estimar siempre con buffer del 20 %.
- **Backlog priorizado MoSCoW**: cuando aparece un imprevisto, hay claridad sobre qué postponer.
- **Documentación incremental**: escribir la memoria *durante* el desarrollo, no al final.
- **Convención de commits semánticos** facilita la lectura del historial.
- **Retrospectiva al final de cada sprint** ayuda a corregir el rumbo.

#### Errores que no se repetirán

- Empezar a implementar pasarela de pago **sin tener idempotencia clara** desde el diseño.
- Usar **Lombok `@Data`** en entidades con `Set<...>` recursivos.
- **Subestimar el tiempo de QA** y el coste de "pulir UI" para que parezca menos AI‑generated.

### 6.5.4 Líneas de trabajo futuro

#### Funcionalidades no implementadas (siguiente iteración)

- **Apps móviles nativas iOS/Android** consumiendo la API REST (mismo backend).
- **Sistema de chat 1‑a‑1** entre usuarios mediante WebSockets (STOMP + SockJS).
- **Streaming en directo** de inmersiones (Cloudflare Stream o Mux).
- **Programa de fidelización**: puntos por inmersión + canjeables por descuentos.
- **Internacionalización efectiva** (en, fr, de).
- **Sistema de moderación avanzado** (admin) con suspensión, ban y apelaciones.
- **Auditoría 90 días** (RNF-13) en tabla específica.

#### Mejoras técnicas previstas

- **Migrar la búsqueda de inmersiones a Elasticsearch / OpenSearch** para soportar volúmenes >10 000 inmersiones.
- **Reescribir el frontend en SvelteKit o Next.js** si crece la complejidad de estado.
- **Dockerfile multi‑arch** (amd64 + arm64).
- **Caché Redis** para datos de sesión y resultados de búsqueda frecuentes.
- **CDN (Cloudflare)** para recursos estáticos (imágenes, vídeos).
- **Monitorización profesional** (Grafana + Loki + Prometheus).
- **Pruebas de carga** (k6 / wrk) y tuning de HikariCP.
- **WebAuthn / passkeys** para autenticación sin contraseña.
- **Encrypted at rest** en S3 / R2 para vídeos privados.

#### Evolución a medio/largo plazo

- **Programa de afiliación con centros de buceo** locales (firmar 10 centros en Andalucía oriental como early adopters).
- **App móvil con AR** para reconocimiento de fauna marina (TensorFlow Lite + cámara).
- **Marketplace ampliado** a otros deportes acuáticos (kayak, SUP, vela).
- **Modelo SaaS B2B** para centros (panel + analytics + integraciones contables).

#### Adaptación a entorno real de producción / comercialización

Para pasar el MVP de DiveConnect a un entorno de producción real:

1. **Verificar Stripe + PayPal** con cuenta empresarial (KYC, IBAN europeo).
2. **Contratar dominio definitivo** (`diveconnect.es` o `diveconnect.app`).
3. **Plan de hosting Starter o superior** (7–25 $/mes) para evitar cold starts.
4. **DPO externo** para asesoramiento RGPD ante crecimiento de usuarios.
5. **Términos y Condiciones + Política de privacidad** revisados por abogado.
6. **Seguro de responsabilidad civil** (la plataforma no garantiza la seguridad de la inmersión, pero conviene cobertura).
7. **Plan de marketing** para captar 100 buceadores y 5 centros como base inicial.
8. **Métricas de producto** (Plausible / Matomo, RGPD‑friendly).
9. **Estrategia de monetización** progresiva (gratis → freemium → premium).

---

## ANEXOS

### Anexo A — Listado completo de documentación entregada

| Archivo | Ubicación | Tipo |
|---------|-----------|------|
| MEMORIA-DIVECONNECT.md | raíz repo | Markdown |
| MEMORIA-DIVECONNECT.pdf | raíz repo | PDF |
| README.md | raíz repo | Markdown |
| INSTALL.md | raíz repo | Markdown |
| CHANGELOG.md | raíz repo | Markdown |
| ISSUES.md | raíz repo | Markdown |
| LICENSE | raíz repo | Texto |
| APUNTES-PROYECTO.md | raíz repo | Markdown (línea por línea) |
| GUION-DEFENSA.md | raíz repo | Markdown |
| COBERTURA-RUBRICA.md | raíz repo | Markdown |
| GUIA-MANUAL.md | raíz repo | Markdown |
| docs/DiveConnect-Documentacion-Tecnica.pdf | docs/ | PDF (87 págs) |
| docs/DiveConnect-Defensa.pptx | docs/ | PowerPoint |
| docs/HANDOUT-TRIBUNAL.pdf | docs/ | PDF (2 págs A4) |
| docs/DiveConnect_Admin.pdf | docs/ | PDF (mapa documental) |
| docs/diagrams/*.md | docs/diagrams/ | Mermaid |
| docs/wireframes/*.svg | docs/wireframes/ | SVG |
| docs/style-guide.md | docs/ | Markdown |
| docs/test-plan.md | docs/ | Markdown |
| docs/lighthouse-audit.md | docs/ | Markdown |
| docs/memoria-extra.md | docs/ | Markdown |
| database/schema.sql | database/ | SQL |
| database/views.sql | database/ | SQL |
| database/procedures.sql | database/ | SQL |

### Anexo B — Endpoints REST principales

| Método | Endpoint | Rol requerido | Descripción |
|--------|----------|---------------|-------------|
| POST | /api/auth/login | público | Login con email + password → JWT |
| POST | /api/auth/register | público | Alta de buceador |
| GET | /api/auth/me | autenticado | Datos del usuario actual |
| GET | /api/inmersiones | autenticado | Listado de inmersiones (con filtros) |
| GET | /api/inmersiones/cercanas | autenticado | Búsqueda Haversine por proximidad |
| POST | /api/reservas | BUCEADOR | Crear reserva |
| POST | /api/payments/checkout | BUCEADOR | Iniciar pasarela (Stripe/PayPal/demo) |
| POST | /api/payments/verify | BUCEADOR | Verificar pago (idempotente) |
| GET | /api/publicaciones | autenticado | Feed paginado |
| POST | /api/publicaciones | autenticado | Crear publicación con multipart |
| POST | /api/uploads | autenticado | Subida de archivo aislada |
| GET | /api/notificaciones | autenticado | Lista de notificaciones |
| GET | /api/admin/estadisticas | ADMIN | Estadísticas globales |
| GET | /api/clima | autenticado | Datos meteorológicos por lat/lon |
| GET | /api/search?q=... | autenticado | Búsqueda global |
| GET | /swagger-ui.html | público | Documentación API |
| GET | /api/health | público | Health check (Actuator) |

### Anexo C — Datos de seed para defensa

Usuario admin demo:
- Email: `admin@diveconnect.com`
- Contraseña: `admin123` (sólo para desarrollo / demo)
- Rol: ADMIN

Usuario buceador demo:
- Email: `marcos@diveconnect.com`
- Contraseña: `test1234` (sólo para desarrollo / demo)
- Rol: BUCEADOR

(En producción, los seeds se deshabilitan con `app.seed.enabled=false`).

---

*Memoria elaborada conforme a las indicaciones de los Hitos 1–6 del CFGS DAW, curso 2025‑2026.*

*Marcos Mordoñez Estévez — Mayo 2026*
