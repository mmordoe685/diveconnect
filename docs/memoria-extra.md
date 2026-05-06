# Memoria técnica · Capítulos complementarios

Este documento amplía la [memoria principal en PDF](DiveConnect-Documentacion-Tecnica.pdf) con tres capítulos requeridos por la rúbrica de PIDAWE 2º DAW (módulos de 1º DAW que también puntúan): Digitalización aplicada, Sostenibilidad aplicada, Sistemas Informáticos. Se completa con un cuarto capítulo sobre la normalización del modelo de datos.

---

## Capítulo A · Digitalización aplicada al sector productivo

### A.1. Sector elegido y problema actual

El sector del **buceo recreativo y formación profesional** en España mueve aproximadamente 200 M€ anuales (datos FEDAS), pero su digitalización es heterogénea:

- La mayoría de los **centros de buceo** siguen gestionando reservas por correo electrónico, WhatsApp y llamada telefónica. Sólo los grandes operadores tienen plataformas de reservas propias.
- La **comunidad** se fragmenta entre Facebook (grupos cerrados), foros (Forobuceo) e Instagram (sin filtrado por especies, ubicación o profundidad).
- El **buceador** lleva su diario de inmersiones en cuadernos físicos o ordenadores de buceo cerrados (Suunto, Garmin) sin sincronización social.

DiveConnect interviene en estos tres frentes con un modelo unificado:

1. **Marketplace para centros**: gestión completa de catálogo, plazas, pagos y notificaciones sin necesidad de software propio.
2. **Red social vertical**: filtrado por datos técnicos (profundidad, especies, ubicación) y mapa interactivo con puntos georreferenciados.
3. **Diario digital integrado**: cada publicación con sus datos técnicos puede servir como log book del buceador.

### A.2. Tecnologías habilitadoras empleadas

La rúbrica menciona "IA, Big Data, IoT" como ejemplos, pero la lista oficial de tecnologías habilitadoras del Digitalization Initiative de la UE es más amplia. DiveConnect emplea las siguientes:

| Tecnología | Aplicación en el proyecto | Valor añadido |
|---|---|---|
| **APIs públicas en tiempo real** | OpenWeatherMap para condiciones del mar | Datos atmosféricos actualizados en cada visualización del mapa |
| **OAuth 2.0** | Login con Google, OAuth client de Stripe y PayPal | Reduce fricción del registro y delega autenticación a proveedores certificados |
| **Geolocalización + cómputo de proximidad** (Haversine en SQL) | Búsqueda con fallback geográfico cuando no hay coincidencias textuales | Personalización por ubicación sin necesidad de servicios externos de pago |
| **Cloud-native (Docker + Render)** | Despliegue containerizado con SSL automático | Escalabilidad horizontal trivial, despliegue 1-click |
| **JWT stateless** | Autenticación sin estado servidor | Permite escalar a múltiples instancias detrás de balanceador |
| **Pasarelas de pago externas** | Stripe Checkout + PayPal REST API v2 | Cobro internacional sin gestionar PCI-DSS internamente |
| **Generación de PDFs en cliente** | (Pendiente) Recibos descargables por reserva | Trazabilidad sin servidor de reportes |

Sobre **IA específica**: DiveConnect no integra modelos de IA en su versión 1.0. La hoja de ruta (`ISSUES.md #12`) prevé un recomendador básico basado en similitud entre inmersiones, calculado por el script Python del módulo de Análisis de Datos. Es una vía realista para añadir IA a un TFG sin caer en hype injustificado.

Sobre **IoT**: el ecosistema natural sería conectar **ordenadores de buceo** (Suunto Bluetooth, Garmin Descent) que exporten el log de inmersiones automáticamente al perfil del usuario. Es el siguiente paso natural pero queda fuera del MVP por dependencia de SDKs propietarios y hardware específico.

### A.3. Madurez digital y escalabilidad

| Eje | Estado actual | Plan de escalado |
|---|---|---|
| **Datos** | MySQL único, ~100 MB previstos en 1er año | Migrar a MySQL gestionado (Render → AWS RDS) cuando supere 5 GB |
| **Tráfico** | 1 instancia Spring Boot (Tomcat embebido) | Horizontal scaling con múltiples instancias detrás de load balancer (uploads ya migrados a S3) |
| **Almacenamiento de archivos** | Local en `uploads/` | Migración a S3/R2 documentada en `ISSUES.md #6` |
| **Latencia internacional** | Servidor en Frankfurt (Render) | CDN para estáticos + uploads (Cloudflare) si se internacionaliza |
| **Búsqueda** | Queries SQL con LIKE | OpenSearch o Elastic en cuanto se superen 100k publicaciones |
| **Notificaciones** | Poll cada 30 s | WebSockets STOMP (`ISSUES.md #8`) |

### A.4. Modelo de negocio

DiveConnect se concibe como **plataforma freemium** con dos vías de ingreso:

1. **Comisión por reserva**: 3-5 % sobre el importe de cada reserva confirmada al centro. El alumno como prestador del servicio actúa como **intermediario neutral**: el centro mantiene su precio público y la plataforma factura al final del mes.
2. **Suscripción opcional para centros** ("DiveConnect Pro"): listing destacado en el catálogo, analítica de reservas y branding personalizado. Aproximadamente 19 €/mes.

Para usuarios buceadores, el uso es 100 % gratuito. Sin publicidad. La privacidad se monetiza con suscripciones de centros, no con datos de usuarios.

---

## Capítulo B · Sostenibilidad aplicada al sistema productivo

### B.1. Green Code: eficiencia algorítmica

Decisiones técnicas que reducen el consumo de CPU/RAM y, por extensión, el coste energético del sistema:

| Decisión | Impacto |
|---|---|
| **Búsqueda por proximidad en SQL nativo** (Haversine) en vez de cargar todas las filas y calcular en Java | Reduce transferencia de datos cliente↔BD y memoria heap del servidor |
| **Paginación obligatoria** en `/api/publicaciones` (`Pageable` con tamaño máximo 50) | Evita queries que escanean toda la tabla |
| **Vistas SQL** (`vw_estadisticas_centro`, `vw_actividad_usuario`) precomputan agregaciones costosas | El motor las puede materializar y el JOIN no se reejecuta cada vez |
| **Índices secundarios** en `reservas(usuario_id, estado)`, `publicaciones(fecha_publicacion)`, `notificaciones(destinatario_id, leida)` | Lookup en O(log n) en queries de listado más calientes |
| **Caché HTTP de 1 h** en `/uploads/**` | Reduce ancho de banda en visitantes recurrentes |
| **CSS keyframes para burbujas** en lugar de `requestAnimationFrame` | El navegador delega la animación a la GPU sin tick JS |
| **`prefers-reduced-motion` respetado** | Si el usuario tiene "movimiento reducido" activado, las burbujas no se inyectan, ahorrando CPU |
| **Compresión gzip** en respuestas (Spring Boot por defecto) | Reduce 60-80 % el peso de respuestas JSON |
| **Imagen Docker `eclipse-temurin:17-jre-alpine`** | Imagen final ~120 MB → menos descarga, menos almacenamiento |
| **JVM con `-Xmx450m -XX:+UseG1GC`** | Limita el heap; G1GC pausa menos en pico de carga |

### B.2. Impacto socioambiental

DiveConnect contribuye directa o indirectamente a varios **Objetivos de Desarrollo Sostenible (ODS)** de la Agenda 2030 de Naciones Unidas:

| ODS | Cómo lo aborda DiveConnect |
|---|---|
| **ODS 14 · Vida submarina** | El feed con datos técnicos (especies, profundidad, visibilidad) genera un **dataset social** sobre el estado de los ecosistemas marinos. Cualquier biólogo marino o gestor de áreas protegidas puede agregar tendencias por zona. La sección de avistamientos con etiquetas de especies fomenta la observación informada y el respeto por la fauna marina. |
| **ODS 9 · Industria, innovación e infraestructura** | Digitaliza un sector tradicionalmente analógico. Permite que centros pequeños accedan a una infraestructura de e-commerce sin invertir en software propio. |
| **ODS 8 · Trabajo decente y crecimiento económico** | Reduce la barrera de entrada para nuevos centros y autónomos del buceo. La comisión por reserva (3-5 %) está muy por debajo de las plataformas equivalentes en hostelería (15-25 %). |
| **ODS 4 · Educación de calidad** | Centros que ofrecen cursos PADI/SSI tienen visibilidad equiparable a los grandes operadores. El sistema de niveles del catálogo informa a los principiantes sobre qué inmersiones son aptas para ellos. |
| **ODS 11 · Ciudades y comunidades sostenibles** | El mapa con puntos georreferenciados puede usarse por administraciones costeras para identificar zonas con mayor presión turística y planificar áreas de protección. |
| **ODS 13 · Acción por el clima** | El servidor consume mínimo (ver sección B.1). El frontend sin framework JS reduce el tamaño de los bundles y, con ello, el consumo energético del cliente. |

### B.3. Huella de carbono digital

Estimación según la metodología del [Sustainable Web Design](https://sustainablewebdesign.org/calculating-digital-emissions/):

- **Página HTML típica del proyecto**: ~30 KB transferidos (HTML + CSS + JS), ~5 KB de imágenes promedio.
- **Una sesión típica**: ~10 navegaciones + 3 imágenes subidas (~500 KB cada una).
- **Cálculo**: ~1.5 MB / sesión. Con 1000 visitantes/mes, 1.5 GB de transferencia mensual.
- Aplicando los **0.5 g CO2 / GB transferido** del Greenhouse Gas Protocol del web hosting promedio: **~ 0.75 g CO2 / mes** del lado de red.

Comparativa de referencia: un solo búsqueda en Google emite ~0.2 g CO2. Un email con adjunto de 1 MB emite ~50 g CO2. **DiveConnect a la escala de un TFG-piloto tiene huella prácticamente despreciable.**

### B.4. Mantenimiento y residuos digitales

Estrategias para que el sistema no acumule "basura" con los años:

- **Procedimiento `sp_purgar_historias_expiradas`**: borra historias 24h ya caducadas. Programable como job (`ISSUES.md #11`).
- **Soft-delete en usuarios** (`activo = FALSE`) en lugar de borrado: preserva integridad referencial sin retener datos personales innecesarios. Cumple LOPD: tras 2 años inactivo, anonimizar nombre y email.
- **Uploads huérfanos**: actualmente sin política. Plan: cron mensual que liste archivos en `uploads/` no referenciados desde ninguna entidad y los archive en bucket frío o borre.
- **Logs**: se rotan por SLF4J/Logback con tamaño máximo (no commit a repo, en `.gitignore`).
- **Imágenes Docker**: tagged con SHA del commit; las antiguas se borran automáticamente en Render tras 7 días.

---

## Capítulo C · Sistemas Informáticos: infraestructura, red y seguridad operativa

### C.1. Arquitectura física en producción (Render.com)

```
                  ┌─────────────────────────────┐
                  │   Cloudflare (CDN + DDoS)   │  ← (opcional, plan futuro)
                  └─────────┬───────────────────┘
                            │ HTTPS 443
                  ┌─────────▼───────────────────┐
                  │    Render Edge (Frankfurt)  │  ← TLS termination
                  └─────────┬───────────────────┘
                            │ HTTP 8080 (red interna)
       ┌────────────────────┼────────────────────┐
       │                    │                    │
       ▼                    ▼                    ▼
┌─────────────┐    ┌──────────────┐    ┌──────────────┐
│  Container  │    │  Container   │    │   Volumen    │
│  Spring Boot│    │  MySQL 8     │    │  uploads/    │
│  0.5 vCPU   │    │  managed     │    │  persistent  │
│  512 MB RAM │    │  256 MB RAM  │    │  10 GB max   │
└──────┬──────┘    └──────────────┘    └──────────────┘
       │
       │ HTTPS salida
       ├──→ api-m.sandbox.paypal.com  (PayPal)
       ├──→ api.stripe.com             (Stripe)
       ├──→ accounts.google.com        (OAuth2)
       └──→ api.openweathermap.org     (Clima)
```

### C.2. Configuración de red

| Capa | Detalle |
|---|---|
| **Puerto público** | 443 HTTPS (Render gestiona certificado Let's Encrypt) |
| **Puerto interno app** | 8080 (mapeado por Render) |
| **Puerto MySQL** | 3306 sólo en red interna de Render (no expuesto) |
| **CORS** | Sólo el dominio del frontend (`https://diveconnect.onrender.com`) |
| **Redirecciones** | HTTP → HTTPS automática en Render |
| **Outbound permitido** | 443 a Stripe, PayPal, Google, OpenWeather |

### C.3. Plataforma local de desarrollo

Hardware mínimo para correr el proyecto en local:

| Componente | Mínimo | Recomendado |
|---|---|---|
| CPU | 2 cores x86_64 (Intel/AMD) | 4 cores |
| RAM | 4 GB libres | 8 GB libres |
| Disco | 2 GB libres | 5 GB libres |
| OS | Windows 10+, macOS 11+, Linux moderno | Cualquiera |
| Java | OpenJDK 17 | OpenJDK 21 |
| MySQL | 8.0 (o Docker) | Docker Compose |

Probado en:
- **Windows 11 + IntelliJ IDEA 2025.3 + MySQL 8.0** (entorno principal de desarrollo).
- **WSL2 Ubuntu 22.04** (compatibilidad Linux).
- **macOS 14 + Homebrew MySQL** (testing en Mac).

### C.4. Compatibilidad de navegadores cliente

| Navegador | Versión mínima | Notas |
|---|---|---|
| Chrome / Edge | 120+ | Funcionalidad completa, mejor rendimiento |
| Firefox | 120+ | Funcionalidad completa |
| Safari | 16+ | Funcionalidad completa, incluido `backdrop-filter` |
| Safari iOS | 16+ | Bottom sheets, file picker con cámara, safe-area-inset |
| Chrome Android | 120+ | Cámara nativa via `capture="environment"` |
| Internet Explorer | — | NO soportado |

### C.5. Seguridad del sistema

#### C.5.1. Autenticación
- **JWT HS256** con secret de 64+ caracteres, configurable por env (`JWT_SECRET`).
- **BCrypt cost factor 10** para hash de contraseñas (~80 ms / hash).
- Expiración de tokens: **24 h**. Sin refresh tokens en v1.0.
- OAuth2 Google opcional, configurable por flag `GOOGLE_OAUTH_ENABLED`.

#### C.5.2. Autorización
Cada endpoint declara explícitamente su nivel de acceso en `SecurityConfig`. Política de menor privilegio:

- **Público**: catálogo de inmersiones, login, registro, recursos estáticos, weather, Swagger UI.
- **Cualquier autenticado**: API de usuarios, reservas, publicaciones, notificaciones.
- **Solo USUARIO_EMPRESA**: CRUD del propio centro, gestión de inmersiones propias.
- **Solo ADMINISTRADOR**: `/api/admin/**` (gestión global).

#### C.5.3. Protección de datos
- **Contraseñas**: nunca se loguean ni se serializan al cliente.
- **Tokens**: enviados sólo por header `Authorization`, nunca por URL.
- **Subidas**: validación de MIME y extensión + nombre UUID para evitar path traversal.
- **CORS**: restrictivo en producción (solo dominio frontend).
- **Headers**: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` (default Spring Security).

#### C.5.4. Backups y recuperación

| Recurso | Frecuencia | Retención | Método |
|---|---|---|---|
| Base de datos | Diaria automática (Render) | 7 días en plan Free, 30+ en Starter | Snapshot completo |
| Uploads (volumen) | Manual / mensual | A discreción | `tar.gz` del volumen |
| Repositorio Git | Continuo | Eterno | GitHub remote |

En entorno local, recomendación de backup:
```bash
mysqldump -u diveconnect_user -p diveconnect_db > backup-$(date +%F).sql
```

---

## Capítulo D · Normalización del modelo de datos

### D.1. Primera Forma Normal (1FN)

Una tabla cumple 1FN si todos sus atributos son **atómicos** (un solo valor por celda) y no hay grupos repetidos.

**DiveConnect cumple 1FN** en todas sus tablas:

- Las relaciones N:M (seguidores, publicacion_likes) se modelan con tablas puente independientes, no con listas embebidas.
- Las **fotos de un punto de mapa** son una entidad propia (`fotos_punto_mapa`) con FK al punto, no un campo `JSON` o cadena separada por comas en `puntos_mapa`.
- El campo `especies_vistas` en `publicaciones` es texto libre (limitado a 500 chars). Se trata como un **valor único** desde el punto de vista del modelo. Si en el futuro se quisiera consultar publicaciones por especie individual, habría que extraer este campo a una tabla `publicacion_especies (publicacion_id, especie_id)`. Es deuda asumida en el TFG actual.

### D.2. Segunda Forma Normal (2FN)

Cumple 1FN + no hay **dependencias parciales** sobre claves compuestas.

**DiveConnect cumple 2FN.** Las únicas claves compuestas en el esquema son las de tablas puente (`seguidores`, `publicacion_likes`), donde por definición no hay atributos no-clave que puedan depender parcialmente de un fragmento de la PK.

### D.3. Tercera Forma Normal (3FN)

Cumple 2FN + no hay **dependencias transitivas** entre atributos no-clave.

**DiveConnect cumple 3FN con dos desnormalizaciones deliberadas y documentadas:**

#### D.3.1. `reservas.centro_buceo_id`
Esta columna es derivable: dada `reservas.inmersion_id`, podríamos hacer `JOIN inmersiones ON ... → centro_buceo_id`. La conservamos como **FK directa** en `reservas` por dos razones:
- Acelera la query "todas las reservas recibidas por un centro" (la consulta más caliente del dashboard de empresa).
- Evita un JOIN adicional en cada listado de reservas.

Trade-off: si se cambia `inmersion.centro_buceo_id` (escenario raro), habría que actualizar también las reservas asociadas. Solución actual: `inmersion.centro_buceo_id` es de hecho inmutable (no hay endpoint que lo cambie).

#### D.3.2. `usuarios.nombre_empresa` y `centros_buceo.nombre`
Son dos campos que pueden coincidir o divergir. La razón es que durante el registro inicial del USUARIO_EMPRESA se guarda el nombre directamente en `usuarios`, pero después se crea la entidad `CentroBuceo` con su propio nombre.

Convención asumida (documentada en código y en este capítulo): **`centros_buceo.nombre` es la fuente de verdad** cuando existe el centro; `usuarios.nombre_empresa` solo se consulta antes de que el centro se haya creado.

### D.4. Forma Normal de Boyce-Codd (BCNF)

Variante más estricta de 3FN: para cada dependencia funcional X → Y, X debe ser superllave.

**DiveConnect cumple BCNF en 14 de sus 15 tablas.** La excepción es `reservas` por la desnormalización mencionada en D.3.1: `inmersion_id → centro_buceo_id` es una dependencia funcional donde `inmersion_id` no es superllave de `reservas`.

### D.5. Cuarta Forma Normal (4FN) y posteriores

No aplican: el modelo no contiene atributos multi-valor independientes que generen dependencias multivaluadas.

---

> Estos cuatro capítulos se incorporan a la memoria PDF en una segunda revisión y se entregan también en formato markdown para auditoría directa en el repositorio.
