# Apuntes integrales de DiveConnect

Documento técnico de referencia del proyecto. Pensado para que repases el código completo antes de la defensa y respondas con seguridad cualquier pregunta del tribunal sobre cualquier archivo o cualquier línea.

Cuatro grandes secciones:

1. **¿Qué es DiveConnect?** — visión general, decisiones de diseño, problemas reales que aparecieron y cómo se resolvieron.
2. **Código por carpetas** — explicación archivo por archivo, línea por línea para los archivos clave.
3. **Código por funcionalidad** — qué archivos participan en cada flujo (login, reserva, pago, etc.).
4. **Guion para el tribunal** — qué decir, en qué orden y qué enseñar (también en `GUION-DEFENSA.md`).

---

# 1. ¿Qué es DiveConnect?

## 1.1. Resumen del producto

DiveConnect es una aplicación web full-stack que combina dos productos:

- **Red social vertical para buceadores**: feed cronológico, historias 24h, sistema de seguimiento, notificaciones, mapa interactivo con puntos georreferenciados, búsqueda universal con proximidad geográfica.
- **Marketplace para centros de buceo**: catálogo filtrable de inmersiones, reservas con descuento automático de plazas, pasarela de pago real (Stripe + PayPal + modo demo TFG).

Tres roles:
- **USUARIO_COMUN** (buceador): publica, sigue, reserva, paga.
- **USUARIO_EMPRESA** (centro): gestiona su catálogo y sus reservas.
- **ADMINISTRADOR**: modera contenido y gestiona usuarios.

## 1.2. Stack técnico

- **Backend**: Java 17 + Spring Boot 3.2.3 + Spring Security 6 + Spring Data JPA + Hibernate.
- **BD**: MySQL 8 con `ddl-auto=update`. 13 tablas, 5 vistas, 4 procedimientos almacenados, índices secundarios.
- **Auth**: JWT HS256 (jjwt 0.11) + BCrypt + OAuth2 Google opcional.
- **Pasarela**: SDK oficial de Stripe + cliente HTTP propio para PayPal v2 (sin SDK).
- **Frontend**: HTML5 + CSS3 + JavaScript ES2020 sin framework. Leaflet para el mapa.
- **Análisis**: Python 3.10+ con pandas + matplotlib + PyMySQL.
- **Infra**: Docker multi-stage + docker-compose + render.yaml + GitHub Actions.

## 1.3. Cinco problemas reales del desarrollo

Estos son los bugs que aparecieron y cómo se resolvieron. Saberlos es importante porque son las preguntas más probables del tribunal.

### Problema 1 — Lombok @Data + Set<Entity> rompe equals/hashCode

**Síntoma**: tras aceptar una solicitud de seguimiento, `/api/seguimiento/estado/{id}` seguía devolviendo NO_SIGUE aunque la fila existía. `Set.contains()` era no determinista, a veces caía en `StackOverflowError`.

**Causa**: Lombok `@Data` genera `equals/hashCode` cubriendo TODOS los campos, incluidas las colecciones `seguidores` y `siguiendo`. `Set.contains()` necesita `hashCode()` → necesita el hash del Set → recursión.

**Solución**: bypass del Set en `UsuarioRepository` con SQL nativo:
```java
@Query(value = "INSERT IGNORE INTO seguidores (seguidor_id, seguido_id) VALUES (:s, :d)", nativeQuery = true)
void addSeguidor(@Param("s") Long s, @Param("d") Long d);

@Query(value = "DELETE FROM seguidores WHERE seguidor_id = :s AND seguido_id = :d", nativeQuery = true)
void removeSeguidor(@Param("s") Long s, @Param("d") Long d);

default boolean existsSeguimiento(Long s, Long d) { return countSeguimiento(s, d) > 0; }
```

### Problema 2 — Race condition en confirmar reserva

**Síntoma**: clic en "Confirmar reserva" lanzaba `Cannot read properties of null (reading 'precio')` y el modal de pago no abría.

**Causa**: la función JS llamaba `cerrarModal()` (que pone `inmersionActual = null`) antes de leer `inmersionActual.precio`.

**Solución**: capturar precio, título e id en variables locales **antes** de cerrar.

### Problema 3 — Notificación faltante en flujo Stripe demo

**Síntoma**: al pagar en modo demo, la reserva pasaba a CONFIRMADA pero no llegaba notificación al usuario ni al centro.

**Causa**: solo el camino Stripe real notificaba. El demo y "ya pagada" no.

**Solución**: refactor de `PaymentController` con método privado `confirmarPago(reserva, paymentRef, pasarela)` llamado desde los tres caminos. También se añadió idempotencia.

### Problema 4 — paymentStatus null ocultaba el botón "Pagar"

**Síntoma**: las reservas seed no mostraban "Pagar ahora" en `reservas.html`.

**Causa**: la condición JS era `r.paymentStatus === 'UNPAID'`, pero el seed dejaba `null`.

**Solución**: tratar `null` como UNPAID en frontend + normalizar el seed con `paymentStatus` consistente.

### Problema 5 — Limpieza de emojis en datos persistidos

**Síntoma**: tras decidir quitar emojis del UI, las publicaciones del seed seguían con emojis en BD.

**Solución**: clase `EmojiStripMigration` (`CommandLineRunner` con `@Order(100)`) que aplica regex Unicode a 6 columnas de 4 tablas. Idempotente. 8 tests cubren los rangos.

---

# 2. Código por carpetas

> **Cómo leer esta sección**: para los archivos clave (controllers, services, JwtUtil, payment.js, ocean-theme.css) está la explicación detallada. Para archivos similares entre sí (DTOs, entidades, repos triviales) hay un patrón explicado y una lista de variantes.

## 2.0. Estructura general

```
src/main/java/com/diveconnect/
├── DiveconnectApplication.java     (1 archivo · entry point)
├── config/                         (6 archivos · configuración Spring)
├── controller/                     (17 archivos · endpoints REST)
├── dto/request/                    (12 archivos · DTOs de entrada)
├── dto/response/                   (10 archivos · DTOs de salida)
├── entity/                         (15 archivos · 11 entidades + 4 enums)
├── exception/                      (4 archivos · 3 excepciones + handler)
├── repository/                     (11 archivos · Spring Data JPA)
├── security/                       (4 archivos · JWT + OAuth2)
└── service/                        (15 archivos · lógica de negocio)

src/main/resources/
├── application.properties          (config principal)
└── static/
    ├── pages/                      (21 HTML)
    ├── js/                         (7 módulos)
    ├── css/                        (style.css + ocean-theme.css)
    └── images/                     (logos, placeholders)

src/test/java/com/diveconnect/      (5 tests · 31 casos)
database/                           (3 archivos SQL)
docs/                               (PDF memoria + .pptx + diagramas + wireframes)
scripts/analytics/                  (Python ETL)
.github/workflows/                  (CI con GitHub Actions)
```

---

## 2.1. Entry point: `DiveconnectApplication.java`

```java
package com.diveconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiveconnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiveconnectApplication.class, args);
    }
}
```

**Línea por línea:**

- `package com.diveconnect`: paquete raíz. Spring escanea desde aquí hacia abajo en busca de `@Component`, `@Service`, `@Controller`, `@Repository`, `@Configuration`.
- `@SpringBootApplication`: meta-anotación que combina tres:
  - `@Configuration`: la clase puede definir beans.
  - `@EnableAutoConfiguration`: Spring Boot configura automáticamente Tomcat, JPA, Security, etc. en función de las dependencias del classpath.
  - `@ComponentScan`: escanea el paquete actual y subpaquetes en busca de componentes.
- `SpringApplication.run(...)`: arranca el contenedor Spring, levanta Tomcat embebido en el puerto configurado (8080 por defecto), inicializa el datasource, ejecuta los `CommandLineRunner` (DataInitializer + EmojiStripMigration) y queda en escucha.

---

## 2.2. `config/` — Configuración Spring (6 archivos)

### `CorsConfig.java`

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
```

- `@Configuration`: clase de configuración leída por Spring al arrancar.
- `implements WebMvcConfigurer`: contrato de Spring MVC para añadir customizaciones (CORS, view controllers, resource handlers, etc.).
- `addCorsMappings`: solo aplica a `/api/**` (no a `/pages/**`).
- `allowedOriginPatterns("*")`: en desarrollo permite cualquier origen. En producción se restringe al dominio del frontend.
- `allowedMethods`: lista explícita de verbos. OPTIONS es necesario para preflight CORS.
- `exposedHeaders("Authorization")`: el cliente puede leer el header `Authorization` desde JS (si no lo expones, los navegadores lo ocultan a `fetch`).
- `maxAge(3600)`: cachea preflight 1 hora para reducir round-trips.

### `WebConfig.java`

Hace dos cosas:

1. Redirige raíz `/` → `/index.html`:
```java
registry.addViewController("/").setViewName("forward:/index.html");
```
2. Sirve `/uploads/**` desde el directorio `file.upload-dir` (por defecto `uploads/`):
```java
@Value("${file.upload-dir:uploads}")
private String uploadDir;

Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
String absPath = uploadPath.toString().replace('\\', '/');
if (!absPath.endsWith("/")) absPath = absPath + "/";
String location = "file:" + absPath;
registry.addResourceHandler("/uploads/**")
        .addResourceLocations(location)
        .setCachePeriod(3600);
```

- `Paths.get(...).toAbsolutePath().normalize()`: resuelve la ruta a absoluta y elimina `..`.
- `.replace('\\', '/')`: en Windows las rutas tienen backslash; Spring necesita slash.
- `"file:" + absPath`: prefijo obligatorio para que Spring trate la ubicación como sistema de ficheros (en lugar de classpath).
- `setCachePeriod(3600)`: cabecera HTTP `Cache-Control: max-age=3600`.

### `SecurityConfig.java` (resumen línea por línea)

Define la cadena de filtros stateless con CSRF deshabilitado y matchers explícitos:

- `.csrf(csrf -> csrf.disable())`: API JSON con JWT, no formularios server-side.
- `.sessionManagement(s -> s.sessionCreationPolicy(STATELESS))`: cero sesión servidor.
- `.exceptionHandling(...)`: 401 (sin auth) y 403 (sin permiso) devuelven JSON, no HTML.
- `.requestMatchers("/", "/index.html", "/css/**", "/js/**", "/pages/**", "/images/**", "/uploads/**", "/favicon.ico").permitAll()`: estáticos públicos.
- `.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()`: Swagger sin auth.
- `.requestMatchers("/api/auth/**").permitAll()`: login/registro/config públicos.
- `.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()`: callback Google.
- `.requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")`: solo ROLE_ADMINISTRADOR.
- `.requestMatchers(GET, "/api/centros-buceo/**").permitAll()` + GET `/api/inmersiones/**` + GET `/api/puntos-mapa/**` + GET `/api/weather` + GET `/api/payments/config` + GET `/api/paypal/config` + GET `/api/search/**`: catálogo público.
- `.requestMatchers(POST, "/api/uploads").authenticated()`: upload requiere sesión.
- `.requestMatchers("/api/notificaciones/**", "/api/seguimiento/**").authenticated()`.
- `.anyRequest().authenticated()`: cualquier otra ruta requiere JWT.
- `if (googleOAuthEnabled) http.oauth2Login(...)`: solo activa OAuth2 si el flag está en true.
- `http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`: el filtro JWT corre **antes** del filtro estándar de username/password de Spring.
- `@Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }`: cost factor 10 por defecto.
- `@Bean AuthenticationManager(...)`: bean expuesto para `AuthController.login()` (que llama `authenticate()` para validar credenciales).

### `OpenApiConfig.java`

Define la configuración de Swagger/OpenAPI:

```java
@Bean
public OpenAPI diveConnectOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("DiveConnect REST API")
            .description("API REST de DiveConnect...")
            .version("1.0.0")
            .contact(new Contact().name("Marcos Mordoñez")...)
            .license(new License().name("MIT")...))
        .servers(List.of(
            new Server().url("http://localhost:8080").description("Local"),
            new Server().url("https://diveconnect.onrender.com").description("Producción")
        ))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
}
```

- `Info`: bloque que aparece arriba en la UI con título, versión, contacto, licencia.
- `Server`: la dropdown que permite cambiar entre entornos en la UI.
- `SecurityScheme.Type.HTTP` + `scheme("bearer")` + `bearerFormat("JWT")`: registra autenticación Bearer JWT.
- `addSecurityItem(...)`: aplica el esquema globalmente, así Swagger UI tiene el botón "Authorize" para pegar el token.

### `DataInitializer.java`

`CommandLineRunner` que crea los datos seed al primer arranque:

- Comprueba si existen `admin`, `buceador`, `oceandive`. Si los tres existen Y hay reservas, puntos, historias y notificaciones, **sale sin tocar nada** (idempotente).
- Si los usuarios existen pero falta algún tipo de dato, borra todo y recrea.
- Crea: 11 usuarios (2 admin + 3 empresas + 6 comunes), 3 centros, 12 inmersiones, ~14 publicaciones con likes y comentarios, ~27 reservas con varios estados, varios puntos de mapa con fotos, historias 24h, notificaciones de varios tipos, solicitudes de seguimiento.
- Para cada Reserva, según el estado asigna `paymentStatus`: PAID si CONFIRMADA/COMPLETADA, UNPAID si PENDIENTE/CANCELADA.

### `EmojiStripMigration.java`

`CommandLineRunner` con `@Order(100)`. Se ejecuta **después** de DataInitializer:

```java
private static final Pattern EMOJI = Pattern.compile(
    "[\\x{1F300}-\\x{1FAFF}" +      // pictographs (corazones, animales, etc.)
    "\\x{2600}-\\x{26FF}"  +        // miscellaneous symbols (sol, luna)
    "\\x{2700}-\\x{27BF}"  +        // dingbats (✓, ✗)
    "\\x{1F000}-\\x{1F2FF}" +       // mahjong / playing cards
    "\\x{FE0F}]");                  // variation selector

private record Target(String table, String idColumn, String column) {}

private static final List<Target> TARGETS = List.of(
    new Target("publicaciones", "id", "contenido"),
    new Target("comentarios",   "id", "contenido"),
    new Target("historias",     "id", "texto"),
    new Target("usuarios",      "id", "biografia"),
    new Target("usuarios",      "id", "descripcion_empresa"),
    new Target("centros_buceo", "id", "descripcion")
);
```

- `Pattern.compile`: regex unicode pre-compilada para eficiencia.
- `record Target`: tipo inmutable Java 16+ para representar pares tabla/columna.
- `TARGETS`: lista de las 6 columnas a limpiar. Hardcoded para no procesar BD entera.

El método `run()` itera, lee cada fila con `JdbcTemplate.queryForList`, aplica `stripEmojis` y `UPDATE` solo si el texto cambió. Si una tabla no existe (DDL en evolución), captura la excepción y sigue.

`stripEmojis(text)` (estático, testeable):
1. `replaceAll(EMOJI, "")`: elimina los emojis.
2. `replaceAll(" ([,.;:!?])", "$1")`: limpia espacios huérfanos antes de signos de puntuación.
3. `replaceAll(" {2,}", " ")`: compacta dobles espacios.
4. `replaceAll("(?m)[ \\t]+$", "")`: elimina trailing whitespace por línea.
5. `.trim()`: elimina espacios al principio/final.

---

## 2.3. `exception/` — Excepciones (4 archivos)

### `BadRequestException.java`, `ResourceNotFoundException.java`, `UnauthorizedException.java`

Las tres son `extends RuntimeException` con dos constructores: uno con `String message` y otro con `String message, Throwable cause`. Diferencia entre ellas: el handler las traduce a HTTP 400, 404 y 401 respectivamente.

### `GlobalExceptionHandler.java`

`@RestControllerAdvice` que centraliza la traducción de excepciones a JSON:

- `@ExceptionHandler(ResourceNotFoundException.class)` → 404 con body `{status, message, timestamp}`.
- `@ExceptionHandler(BadRequestException.class)` → 400.
- `@ExceptionHandler(UnauthorizedException.class)` → 401.
- `@ExceptionHandler(BadCredentialsException.class)` → 401 con mensaje "Usuario o contraseña incorrectos" (estandariza el mensaje de Spring Security).
- `@ExceptionHandler(UsernameNotFoundException.class)` → 404 con "Usuario no encontrado".
- `@ExceptionHandler(MethodArgumentNotValidException.class)` → 400 con un mapa `{field: errorMessage}` agregando todos los errores de validación de los DTOs.
- `@ExceptionHandler(Exception.class)` → 500 con "Error interno del servidor: " + mensaje (catch-all).

`ErrorResponse` es una clase interna estática con `int status`, `String message`, `LocalDateTime timestamp`, getters y setters.

---

## 2.4. `security/` — Autenticación y JWT (4 archivos)

### `JwtUtil.java`

Genera y valida JWTs. Línea por línea:

```java
@Value("${jwt.secret}") private String secret;
@Value("${jwt.expiration}") private Long expiration;
```
- `secret` viene de `application.properties` (`JWT_SECRET` env var). Mínimo 32 bytes para HS256.
- `expiration` en milisegundos. 86400000 = 24 horas.

```java
private Key getSigningKey() {
    byte[] keyBytes = secret.getBytes();
    return Keys.hmacShaKeyFor(keyBytes);
}
```
- Convierte el secret string a bytes y crea una `Key` para HMAC-SHA.

```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}
```
- Devuelve el `subject` del JWT, que es el username.

```java
public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
}
```
- Genérico: cualquier claim del JWT extraíble con una función. Reutilizable.

```java
private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
}
```
- Parsea el JWT, valida la firma con el secret. Si la firma es inválida o el token está corrupto, lanza `JwtException`.

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername());
}

private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + expiration);
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}
```
- Crea un JWT firmado con HS256, subject = username, expiración = ahora + 24h.

```java
public Boolean validateToken(String token, UserDetails userDetails) {
    try {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}
```
- Valida que el username del token coincide con el UserDetails y que no ha expirado. Si la firma es inválida o malformada, captura y devuelve false.

### `JwtAuthenticationFilter.java`

`@Component` que extiende `OncePerRequestFilter` (garantía de ejecutarse una sola vez por request). Línea por línea (lo crítico):

```java
String header = request.getHeader("Authorization");
if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
    String token = header.substring(7);
    String username = jwtUtil.extractUsername(token);
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (jwtUtil.validateToken(token, userDetails)) {
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}
filterChain.doFilter(request, response);
```

Pasos:
1. Lee el header `Authorization`.
2. Si empieza por `Bearer `, extrae el token (después de los 7 caracteres).
3. Extrae el username del JWT.
4. Si hay username y aún no hay Authentication en el contexto, carga el `UserDetails` por username.
5. Valida el token con `JwtUtil.validateToken`.
6. Si todo OK, monta un `UsernamePasswordAuthenticationToken` con las authorities del usuario y lo pone en el `SecurityContextHolder`.
7. **Siempre** continúa la cadena con `filterChain.doFilter`. Si el token es inválido, no se monta auth y los matchers de `SecurityConfig` rechazarán la request con 401/403.

Está registrado en `SecurityConfig` con `addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`.

### `UserDetailsServiceImpl.java`

Implementa `UserDetailsService` de Spring Security. Su único método:

```java
@Override
public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
            .orElseGet(() -> usuarioRepository.findByEmail(usernameOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + usernameOrEmail)));
    String role = "ROLE_" + usuario.getTipoUsuario().name();  // ROLE_USUARIO_COMUN, etc.
    return new User(
        usuario.getUsername(),
        usuario.getPassword(),
        usuario.getActivo(),  // enabled
        true, true, true,     // accountNonExpired, credentialsNonExpired, accountNonLocked
        Collections.singleton(new SimpleGrantedAuthority(role))
    );
}
```

- Busca primero por username, si no encuentra busca por email (para soportar login con email).
- Construye un `org.springframework.security.core.userdetails.User` con la authority correspondiente al rol.
- Si `usuario.activo == false`, Spring rechaza el login automáticamente.

### `GoogleOAuth2SuccessHandler.java`

Solo se invoca si el flag `app.google-oauth-enabled=true`. Tras login OAuth2 exitoso:

1. Obtiene email y nombre del `OAuth2User`.
2. Busca el usuario en BD por email.
3. Si no existe, lo crea como `USUARIO_COMUN` con un username generado.
4. Genera JWT con `JwtUtil.generateToken`.
5. Redirige a `/pages/oauth-callback.html?token=...&user=...&isNew=...` para que el frontend guarde el token en localStorage.

---

## 2.5. `entity/` — Modelo de dominio (15 archivos)

### Patrón general de las entidades

Todas las entidades JPA usan:
- `@Entity` + `@Table(name = "tabla")`.
- `@Id @GeneratedValue(strategy = IDENTITY)` para la PK.
- `@Data` (Lombok) para getters/setters automáticos.
- `@CreationTimestamp` y `@UpdateTimestamp` (Hibernate) para timestamps automáticos.
- Validación con `@NotBlank`, `@Email`, `@Size`, etc. (jakarta.validation).

### `Usuario.java`

Atributos:
- `id Long PK AUTO_INCREMENT`.
- `username String unique not null` (3-50 chars). Validado.
- `email String unique not null`. Validado con `@Email`.
- `password String not null` (almacenado como hash BCrypt).
- `biografia text` nullable.
- `fotoPerfil String(500)`.
- `nivelCertificacion String(50)`: "Open Water", "Advanced", "Rescue Diver", "Divemaster", etc.
- `numeroInmersiones Integer` con default 0.
- `tipoUsuario` enum: `USUARIO_COMUN`, `USUARIO_EMPRESA`, `ADMINISTRADOR`.
- Datos de empresa nullable: `nombreEmpresa`, `descripcionEmpresa`, `direccion`, `telefono`, `sitioWeb`.
- `activo Boolean` con default true. Si `false`, Spring Security rechaza login.
- `fechaRegistro LocalDateTime` con `@CreationTimestamp`.
- **Crítico**: dos `Set<Usuario>` autorreferenciales:
  ```java
  @ManyToMany
  @JoinTable(name = "seguidores",
             joinColumns        = @JoinColumn(name = "seguido_id"),
             inverseJoinColumns = @JoinColumn(name = "seguidor_id"))
  private Set<Usuario> seguidores = new HashSet<>();

  @ManyToMany(mappedBy = "seguidores")
  private Set<Usuario> siguiendo = new HashSet<>();
  ```
  Esto crea la tabla puente `seguidores(seguidor_id, seguido_id)`. **Aviso**: por el bug de Lombok @Data, NO se usa `usuario.getSiguiendo().contains(otro)` — todas las consultas relacionadas pasan por `UsuarioRepository.existsSeguimiento` con SQL nativo.

### `CentroBuceo.java`

- `id Long PK`.
- `usuario Usuario` con `@OneToOne` y `@JoinColumn(unique = true)`: cada centro pertenece a un único usuario empresa, y un usuario empresa tiene 0 o 1 centros.
- `nombre String not null`.
- `descripcion text`.
- `direccion`, `ciudad`, `provincia`, `pais String`.
- `latitud Double`, `longitud Double` (para el mapa).
- `telefono`, `email`, `sitioWeb String`.
- `certificaciones String(500)`: "PADI 5 Estrellas, SSI Gold Palm".
- `imagenUrl String`.
- `valoracionPromedio Double` (preparado para futura funcionalidad).
- `activo Boolean`.

### `Inmersion.java`

- `id Long PK`.
- `centroBuceo CentroBuceo` con `@ManyToOne not null`.
- `titulo String not null`.
- `descripcion text`.
- `fechaInmersion LocalDateTime`.
- `duracion Integer` (minutos).
- `profundidadMaxima Double` (metros).
- `nivelRequerido String`: "Sin experiencia", "Open Water", etc.
- `precio Double`.
- `plazasTotales Integer` con default 12.
- `plazasDisponibles Integer` con default 10.
- `ubicacion String` (texto humano: "Playa del Postiguet, Alicante").
- `latitud Double`, `longitud Double` (para Haversine).
- `equipoIncluido text`.
- `imagenUrl String`.
- `activo Boolean` con default true.
- `fechaCreacion LocalDateTime`.

### `Reserva.java`

La entidad más enriquecida. Atributos:
- `id Long PK`.
- `usuario Usuario @ManyToOne`.
- `inmersion Inmersion @ManyToOne`.
- `centroBuceo CentroBuceo @ManyToOne`: redundante (derivable de inmersion.centroBuceo). **Desnormalización deliberada** para acelerar la query "todas las reservas recibidas por un centro".
- `numeroPersonas Integer not null`.
- `precioTotal Double not null` (= inmersion.precio × numeroPersonas).
- `estado EstadoReserva` enum: `PENDIENTE | CONFIRMADA | CANCELADA | COMPLETADA`.
- `paymentStatus String(20)`: `UNPAID | PAID | FAILED`.
- `stripeSessionId String(255)`: tras crear Stripe Checkout Session.
- `stripePaymentIntentId String(255)`: tras pago confirmado.
- `paypalOrderId String(255)`: tras create-order.
- `paypalCaptureId String(255)`: tras capture-order.
- `observaciones text`.
- `fechaReserva LocalDateTime` con `@CreationTimestamp`.
- `ultimaModificacion LocalDateTime` con `@UpdateTimestamp`.

### `Publicacion.java`

- `id Long PK`.
- `usuario Usuario @ManyToOne not null`.
- `contenido text`.
- `imagenUrl String(500)`, `videoUrl String(500)`.
- Datos técnicos del buceo (nullable): `lugarInmersion`, `profundidadMaxima`, `temperaturaAgua`, `visibilidad`, `especiesVistas`.
- `fechaPublicacion LocalDateTime`.
- `Set<Usuario> likes` con `@ManyToMany` sobre tabla puente `publicacion_likes`. **Mismo bug Lombok**: para consultas se usa SQL nativo en `PublicacionRepository`.

### `Comentario.java`

- `id Long PK`.
- `publicacion Publicacion @ManyToOne not null`.
- `usuario Usuario @ManyToOne not null`.
- `contenido text not null`.
- `fechaComentario LocalDateTime`.

### `Historia.java`

- `id Long PK`.
- `usuario Usuario @ManyToOne`.
- `mediaUrl String(1500)`.
- `mediaType` enum: `FOTO | VIDEO`.
- `texto String(500)` nullable.
- `fechaPublicacion LocalDateTime`.
- `expiraEn LocalDateTime not null`: calculado al crear como `fechaPublicacion + 24h`. Las historias caducadas se filtran en query.

### `Notificacion.java`

- `id Long PK`.
- `destinatario Usuario @ManyToOne not null`: quien recibe.
- `emisor Usuario @ManyToOne` nullable: quien causó la notificación.
- `tipo TipoNotificacion` enum (9 valores).
- `mensaje String(500)`.
- `leida Boolean` con default false.
- `accionable Boolean`: true si requiere acción (ej. SOLICITUD_SEGUIMIENTO con botones aceptar/rechazar).
- `resuelta Boolean`: true tras aceptar/rechazar.
- `entidadRelacionadaId Long`: id de la entidad referenciada (ej. id de la SolicitudSeguimiento, id de la Reserva...).
- `fechaCreacion LocalDateTime`.

### `SolicitudSeguimiento.java`

- `id Long PK`.
- `solicitante Usuario @ManyToOne`.
- `destinatario Usuario @ManyToOne`.
- `estado EstadoSolicitud` enum: `PENDIENTE | ACEPTADA | RECHAZADA`.
- `fechaCreacion`, `fechaRespuesta LocalDateTime`.

### `PuntoMapa.java`

- `id Long PK`.
- `autor Usuario @ManyToOne` (USUARIO_EMPRESA o ADMINISTRADOR).
- `latitud Double`, `longitud Double` not null.
- `titulo String`.
- `descripcion text`.
- Datos técnicos: `profundidadMetros`, `temperaturaAgua`, `presionBar`, `visibilidadMetros`, `corriente`, `especiesVistas`, `fechaObservacion`.
- `Set<FotoPuntoMapa> fotos` con `@OneToMany(mappedBy = "puntoMapa", cascade = ALL)`.

### `FotoPuntoMapa.java`

- `id Long PK`.
- `puntoMapa PuntoMapa @ManyToOne not null`.
- `url String not null`.
- `especieAvistada String`.
- `descripcion text`.
- `fechaHora LocalDateTime`.
- `fechaSubida LocalDateTime`.

### Enums (4)

- `TipoUsuario`: `USUARIO_COMUN`, `USUARIO_EMPRESA`, `ADMINISTRADOR`.
- `EstadoReserva`: `PENDIENTE`, `CONFIRMADA`, `CANCELADA`, `COMPLETADA`.
- `EstadoSolicitud`: `PENDIENTE`, `ACEPTADA`, `RECHAZADA`.
- `TipoNotificacion`: `SOLICITUD_SEGUIMIENTO`, `SEGUIMIENTO_ACEPTADO`, `SEGUIMIENTO_RECHAZADO`, `NUEVO_SEGUIDOR`, `LIKE_PUBLICACION`, `COMENTARIO_PUBLICACION`, `RESERVA_CONFIRMADA`, `RESERVA_RECIBIDA`, `MENCION`.

---

## 2.6. `repository/` — Spring Data JPA (11 archivos)

### Patrón general

Todos extienden `JpaRepository<Entity, Long>`, lo que da gratis: `save`, `findById`, `findAll`, `count`, `delete`, etc. Spring Data implementa los métodos derivados al arrancar.

### `UsuarioRepository.java` (el más complejo)

```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.nombreEmpresa) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Usuario> buscarPorNombre(@Param("keyword") String keyword);

    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario = 'USUARIO_EMPRESA' AND u.activo = true")
    List<Usuario> findEmpresasActivas();

    // SQL NATIVO para evitar el bug de @Data + Set<Usuario>
    @Modifying @Transactional
    @Query(value = "INSERT IGNORE INTO seguidores (seguidor_id, seguido_id) VALUES (:seguidorId, :seguidoId)",
           nativeQuery = true)
    void addSeguidor(@Param("seguidorId") Long seguidorId, @Param("seguidoId") Long seguidoId);

    @Modifying @Transactional
    @Query(value = "DELETE FROM seguidores WHERE seguidor_id = :seguidorId AND seguido_id = :seguidoId",
           nativeQuery = true)
    void removeSeguidor(@Param("seguidorId") Long seguidorId, @Param("seguidoId") Long seguidoId);

    @Query(value = "SELECT COUNT(*) FROM seguidores WHERE seguidor_id = :seguidorId AND seguido_id = :seguidoId",
           nativeQuery = true)
    long countSeguimiento(@Param("seguidorId") Long seguidorId, @Param("seguidoId") Long seguidoId);

    default boolean existsSeguimiento(Long seguidorId, Long seguidoId) {
        return countSeguimiento(seguidorId, seguidoId) > 0;
    }

    @Modifying @Transactional
    @Query(value = "DELETE FROM seguidores", nativeQuery = true)
    void clearSeguidores();
}
```

- `Optional<Usuario>`: para evitar NPE; `findByUsername("foo").orElseThrow(...)`.
- `@Query JPQL` con `LOWER` y `CONCAT` para búsqueda case-insensitive.
- `@Modifying`: obligatorio en JPQL/SQL que modifica datos (INSERT/UPDATE/DELETE).
- `@Transactional`: cada modificación va en su propia transacción (a nivel repo es excepción a la regla de "transacciones en service").
- `INSERT IGNORE`: si la fila ya existe (PK violación), MySQL la ignora silenciosamente. Útil para idempotencia.
- `default boolean existsSeguimiento`: método default Java 8+ que delega en `countSeguimiento`. Conveniencia.

### `InmersionRepository.java`

Métodos derivados estándar más una query crítica:

```java
@Query(value = "SELECT i.id AS id, " +
               "  (6371 * ACOS( " +
               "     COS(RADIANS(:lat)) * COS(RADIANS(i.latitud)) * " +
               "     COS(RADIANS(i.longitud) - RADIANS(:lon)) + " +
               "     SIN(RADIANS(:lat)) * SIN(RADIANS(i.latitud)) " +
               "  )) AS distanciaKm " +
               "FROM inmersiones i " +
               "WHERE i.activo = true AND i.latitud IS NOT NULL AND i.longitud IS NOT NULL " +
               "ORDER BY distanciaKm ASC",
       nativeQuery = true)
List<InmersionConDistancia> findMasCercanas(@Param("lat") double lat,
                                            @Param("lon") double lon,
                                            Pageable pageable);

interface InmersionConDistancia {
    Long   getId();
    Double getDistanciaKm();
}
```

- Fórmula de **Haversine**: distancia entre dos puntos sobre la superficie de una esfera. 6371 = radio de la Tierra en km.
- `RADIANS`, `COS`, `SIN`, `ACOS`: funciones MySQL que toman/devuelven radianes y aplican trigonometría.
- `Pageable`: límite y offset gestionados por Spring Data. En la llamada se pasa `PageRequest.of(0, 5)` para obtener las 5 más cercanas.
- `interface InmersionConDistancia`: **projection** de Spring Data. En lugar de cargar entidades completas, devuelve solo id y distancia. Hibernate genera el código de mapeo automáticamente.

Otra query interesante: `buscarAvanzado` con keyword + filtros opcionales (todos pueden ser null):

```java
@Query("SELECT i FROM Inmersion i WHERE i.activo = true " +
       " AND (:keyword IS NULL OR :keyword = '' OR LOWER(i.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) ...) " +
       " AND (:profMin IS NULL OR i.profundidadMaxima >= :profMin) " +
       " AND (:profMax IS NULL OR i.profundidadMaxima <= :profMax) " +
       " AND (:precioMax IS NULL OR i.precio <= :precioMax) " +
       " AND (:nivel IS NULL OR :nivel = '' OR LOWER(i.nivelRequerido) LIKE ...)" +
       " ORDER BY i.fechaInmersion ASC")
```

El truco `(:param IS NULL OR i.campo OP :param)` permite que cada filtro sea opcional sin construir SQL dinámico.

### Resto de repositories

- `CentroBuceoRepository`: métodos derivados estándar.
- `ComentarioRepository`: `findByPublicacionOrderByFechaComentarioAsc(Publicacion p)`.
- `PublicacionRepository`: feed paginado con `Pageable`, métodos nativos para likes.
- `ReservaRepository`: `findByUsuarioOrderByFechaReservaDesc`, `findByCentroBuceoOrderByFechaReservaDesc`.
- `HistoriaRepository`: `findByExpiraEnAfterOrderByFechaPublicacionDesc(LocalDateTime now)` para listar las activas.
- `NotificacionRepository`: `findByDestinatarioOrderByFechaCreacionDesc`, `countByDestinatarioAndLeidaFalse`.
- `SolicitudSeguimientoRepository`: `findBySolicitanteAndDestinatarioAndEstado`, listado por destinatario.
- `PuntoMapaRepository`, `FotoPuntoMapaRepository`: CRUD básico.

---

## 2.7. `controller/` — Endpoints REST (17 archivos)

### Patrón general

Todos los controllers usan:
- `@RestController` (= `@Controller` + `@ResponseBody`).
- `@RequestMapping("/api/...")`.
- `@RequiredArgsConstructor` (Lombok) para inyección por constructor.
- `@CrossOrigin(origins = "*")` (redundante con CorsConfig pero explícito).
- `@Slf4j` para logging cuando hace falta.

### `AuthController.java` — `/api/auth`

3 endpoints:

**POST `/api/auth/login`**:
```java
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String token = jwtUtil.generateToken(userDetails);
    UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(userDetails.getUsername());
    return ResponseEntity.ok(new AuthResponse(token, "Bearer", usuario));
}
```
- `@Valid`: dispara la validación de jakarta.validation (`@NotBlank`).
- `authenticationManager.authenticate(...)`: delega en Spring Security; si las credenciales son inválidas, lanza `BadCredentialsException` (capturada por GlobalExceptionHandler → 401).
- `UserDetailsServiceImpl.loadUserByUsername` carga el usuario; `BCryptPasswordEncoder` valida el hash de la contraseña.
- `JwtUtil.generateToken` firma el JWT.
- Devuelve `{token, tipo: "Bearer", usuario: {...}}`.

**POST `/api/auth/registro`**: crea usuario nuevo. Llama `usuarioService.registrarUsuario` que valida unicidad de username/email, hashea la contraseña y guarda.

**GET `/api/auth/config`**: devuelve `{googleEnabled: true|false}` para que el frontend decida si pintar el botón Google.

### `UsuarioController.java` — `/api/usuarios`

5+ endpoints:

- `GET /perfil` (autenticado): perfil del usuario actual. Extrae el username del SecurityContext.
- `PUT /perfil` (autenticado): actualiza biografía, fotoPerfil, nivelCertificacion, numeroInmersiones.
- `GET /{id}`: perfil público.
- `GET /buscar?q=...` (autenticado): búsqueda por nombre/empresa.
- `GET /empresas` (autenticado): lista de USUARIO_EMPRESA activos.

### `CentroBuceoController.java` — `/api/centros-buceo`

- `GET /` (público): lista todos los centros.
- `GET /{id}` (público): detalle.
- `POST /` (USUARIO_EMPRESA): crea su centro.
- `PUT /{id}` (USUARIO_EMPRESA propietario o ADMINISTRADOR): actualiza.
- `DELETE /{id}` (ADMINISTRADOR): borra.

### `InmersionController.java` — `/api/inmersiones`

- `GET /disponibles` (público): catálogo activo con plazas > 0.
- `GET /{id}` (público): detalle.
- `GET /mis-inmersiones` (USUARIO_EMPRESA): inmersiones del centro del usuario actual.
- `POST /` (USUARIO_EMPRESA): crea (verifica que el usuario tenga centro).
- `PUT /{id}` (USUARIO_EMPRESA propietario): actualiza.
- `DELETE /{id}` (USUARIO_EMPRESA propietario): borra (con check de propietario en el service).

### `ReservaController.java` — `/api/reservas`

- `POST /` (autenticado): crea reserva. Body: `{inmersionId, numeroPersonas, observaciones}`.
- `GET /mis-reservas` (autenticado): reservas del usuario.
- `GET /centro/{centroId}` (USUARIO_EMPRESA propietario): reservas recibidas.
- `DELETE /{id}` (autenticado): cancela y devuelve plazas.

### `PaymentController.java` — `/api/payments` (Stripe + demo)

```java
@PostMapping("/verify/{reservaId}")
public ResponseEntity<Map<String, Object>> verificar(@PathVariable Long reservaId) {
    Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

    // 1. Idempotencia: si ya está pagada, no notificar de nuevo
    if ("PAID".equalsIgnoreCase(reserva.getPaymentStatus())) {
        return ResponseEntity.ok(Map.of(
            "status", "PAID",
            "estado", reserva.getEstado().name(),
            "alreadyPaid", true
        ));
    }

    // 2. Stripe activado y con sessionId real → consultar Stripe
    if (stripeService.isEnabled() && reserva.getStripeSessionId() != null) {
        try {
            Session session = stripeService.retrieveSession(reserva.getStripeSessionId());
            String payStatus = session.getPaymentStatus(); // "paid" | "unpaid" | "no_payment_required"
            if ("paid".equalsIgnoreCase(payStatus)) {
                confirmarPago(reserva, session.getPaymentIntent(), "Stripe");
            } else {
                reserva.setPaymentStatus("UNPAID");
                reservaRepository.save(reserva);
            }
            return ResponseEntity.ok(Map.of(
                "status", reserva.getPaymentStatus(),
                "estado", reserva.getEstado().name(),
                "demo", false
            ));
        } catch (StripeException e) {
            log.error("Stripe retrieveSession falló", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // 3. Camino demo TFG (sin Stripe configurado)
    confirmarPago(reserva, null, "Demo");
    return ResponseEntity.ok(Map.of(
        "status", "PAID",
        "estado", reserva.getEstado().name(),
        "demo", true
    ));
}

private void confirmarPago(Reserva reserva, String paymentIntentId, String pasarela) {
    reserva.setPaymentStatus("PAID");
    if (paymentIntentId != null) reserva.setStripePaymentIntentId(paymentIntentId);
    reserva.setEstado(EstadoReserva.CONFIRMADA);
    reservaRepository.save(reserva);
    if (reserva.getUsuario() != null) {
        notificacionService.crear(reserva.getUsuario(), null,
            TipoNotificacion.RESERVA_CONFIRMADA, reserva.getId(),
            "Tu reserva ha sido confirmada y pagada con " + pasarela, false);
    }
    if (reserva.getCentroBuceo() != null && reserva.getCentroBuceo().getUsuario() != null) {
        notificacionService.crear(reserva.getCentroBuceo().getUsuario(), reserva.getUsuario(),
            TipoNotificacion.RESERVA_RECIBIDA, reserva.getId(),
            "Has recibido una nueva reserva confirmada", false);
    }
}
```

Tres caminos, una sola función privada que garantiza el mismo comportamiento (UPDATE + 2 notificaciones) en todos.

Otros métodos del PaymentController:
- `GET /config` (público): `{enabled: stripeEnabled, publishableKey: ...}`.
- `POST /checkout/{reservaId}`: si Stripe activo, crea una Session de Stripe Checkout y devuelve la URL de redirección.

### `PayPalController.java` — `/api/paypal`

- `GET /config` (público): `{enabled, clientId, mode, currency}`.
- `POST /create-order/{reservaId}`: invoca `PayPalService.createOrder(reserva)`. Devuelve `{orderId, status}`.
- `POST /capture-order/{reservaId}?orderId=...`: invoca `PayPalService.captureOrder(orderId)`. Si status=COMPLETED, marca reserva PAID + notifica a usuario y centro.

### `PublicacionController.java` — `/api/publicaciones`

- `GET /?page=&size=` (autenticado): feed paginado.
- `GET /feed`: feed personalizado de los usuarios seguidos.
- `POST /`: crear publicación.
- `DELETE /{id}`: eliminar (autor o admin).
- `POST /{id}/like` y `DELETE /{id}/like`: like/unlike.
- `GET /{id}/comentarios`: lista de comentarios.
- `POST /{id}/comentarios`: añadir comentario.

### `NotificacionController.java`

- `GET /api/notificaciones` (autenticado): lista del usuario actual.
- `GET /api/notificaciones/no-leidas`: count para el badge.
- `POST /{id}/leer`: marca leída.
- `POST /leer-todas`: marca todas leídas.
- `DELETE /{id}`: borra una notificación.

### `SeguimientoController.java`

- `POST /api/seguimiento/solicitar/{usuarioId}`: si destinatario es COMUN, crea SolicitudSeguimiento PENDIENTE + notificación accionable. Si es EMPRESA o si el solicitante es ADMIN, sigue directo.
- `POST /aceptar/{solicitudId}`: cambia estado a ACEPTADA + `addSeguidor` + notifica.
- `POST /rechazar/{solicitudId}`: estado RECHAZADA + notifica.
- `DELETE /dejar/{usuarioId}`: `removeSeguidor`.
- `GET /estado/{usuarioId}`: `{estado: SIGUIENDO | NO_SIGUE | SOLICITUD_PENDIENTE}`.

### `UploadController.java`

```java
@PostMapping(consumes = "multipart/form-data")
public ResponseEntity<Map<String, Object>> subirArchivo(@RequestParam("file") MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
        throw new BadRequestException("No se ha enviado ningún archivo");
    }
    String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
    if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
        throw new BadRequestException("Sólo se aceptan imágenes o videos");
    }
    String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
    String ext = "";
    int dot = original.lastIndexOf('.');
    if (dot >= 0 && dot < original.length() - 1) {
        ext = original.substring(dot + 1).toLowerCase();
    }
    if (!ALLOWED_EXT.contains(ext)) {
        throw new BadRequestException("Extensión no permitida: " + ext);
    }
    Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
    Files.createDirectories(dir);
    String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
    Path dest = dir.resolve(filename);
    if (!dest.startsWith(dir)) {
        throw new BadRequestException("Ruta de destino no válida");
    }
    try (var in = file.getInputStream()) {
        Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
    }
    String url = "/uploads/" + filename;
    String tipo = contentType.startsWith("video/") ? "VIDEO" : "FOTO";
    return ResponseEntity.ok(Map.of("url", url, "tipo", tipo, "filename", filename, "sizeBytes", file.getSize()));
}
```

Tres capas de validación:
1. **MIME**: debe empezar por `image/` o `video/`.
2. **Extensión**: lista blanca `["jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "mp4", "webm", "mov", "m4v"]`.
3. **Path traversal**: `dest.startsWith(dir)` verifica que el path resuelto está dentro del directorio. Como el filename se genera con UUID, es defensa en profundidad.

`Files.copy(in, dest, REPLACE_EXISTING)` con try-with-resources cierra el InputStream automáticamente.

### `SearchController.java` — `/api/search`

```java
@GetMapping
public SearchResponse search(@RequestParam(required = false) String q,
                             @RequestParam(required = false) String tipo,
                             @RequestParam(required = false) Double lat,
                             @RequestParam(required = false) Double lon,
                             @RequestParam(required = false) Double profMin,
                             @RequestParam(required = false) Double profMax,
                             @RequestParam(required = false) Double precioMax,
                             @RequestParam(required = false) String nivel) {
    return searchService.buscar(q, tipo, lat, lon, profMin, profMax, precioMax, nivel);
}
```

Delega en `SearchService` que combina resultados de usuarios, centros e inmersiones, y aplica fallback de proximidad si no hay coincidencias textuales.

### Otros controllers

- `HistoriaController`: `/api/historias/feed` lista activas, `POST /` crea, `DELETE /{id}`.
- `PuntoMapaController`: GET listar, GET detalle, POST crear (EMPRESA/ADMIN), DELETE (autor o ADMIN).
- `WeatherController`: `/api/weather?lat=&lon=` proxy a OpenWeatherMap con fallback mock.
- `ComentarioController`: gestión de comentarios.
- `AdminController`: `/api/admin/usuarios` GET, POST, DELETE para gestión global.

---

## 2.8. `service/` — Lógica de negocio (15 archivos)

### Patrón general

Todos con `@Service` + `@RequiredArgsConstructor`. Las escrituras en `@Transactional`, las lecturas en `@Transactional(readOnly = true)`.

### `ReservaService.java` (clave)

```java
@Transactional
public ReservaResponse crearReserva(Long usuarioId, ReservaRequest request) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    Inmersion inmersion = inmersionRepository.findById(request.getInmersionId())
            .orElseThrow(() -> new ResourceNotFoundException("Inmersión no encontrada"));

    if (inmersion.getPlazasDisponibles() < request.getNumeroPersonas()) {
        throw new BadRequestException("No hay suficientes plazas disponibles");
    }

    Double precioTotal = inmersion.getPrecio() * request.getNumeroPersonas();

    Reserva reserva = new Reserva();
    reserva.setUsuario(usuario);
    reserva.setInmersion(inmersion);
    reserva.setCentroBuceo(inmersion.getCentroBuceo());
    reserva.setNumeroPersonas(request.getNumeroPersonas());
    reserva.setPrecioTotal(precioTotal);
    reserva.setEstado(EstadoReserva.PENDIENTE);
    reserva.setPaymentStatus("UNPAID");
    Reserva saved = reservaRepository.save(reserva);

    inmersion.setPlazasDisponibles(inmersion.getPlazasDisponibles() - request.getNumeroPersonas());
    inmersionRepository.save(inmersion);

    return convertirAResponse(saved);
}
```

Pasos:
1. Carga usuario e inmersión (lanza 404 si no existen).
2. Verifica plazas suficientes (lanza 400 si no).
3. Calcula precio total = `inmersion.precio × numeroPersonas`.
4. Crea Reserva con estado PENDIENTE/UNPAID + FK redundante a CentroBuceo.
5. Guarda reserva.
6. Decrementa plazas en la inmersión.
7. Guarda inmersión.

Todo en `@Transactional`, así que si cualquier paso falla se hace rollback (la reserva no se queda creada con plazas no descontadas o viceversa).

`marcarComoPagada(Long reservaId, String paymentIntentId)`: cambia estado a CONFIRMADA + paymentStatus PAID.

`cancelarReserva(Long id)`: pone estado CANCELADA y devuelve las plazas a la inmersión.

### `PayPalService.java`

Cliente HTTP nativo (sin SDK):

```java
public boolean isConfigured() {
    return clientId != null && !clientId.isBlank()
        && clientSecret != null && !clientSecret.isBlank();
}

public String getBaseUrl() {
    return "live".equalsIgnoreCase(mode) ? baseUrlLive : baseUrlSandbox;
}

public String fetchAccessToken() throws PayPalException {
    if (!isConfigured()) throw new PayPalException("PayPal no está configurado");
    String creds = clientId + ":" + clientSecret;
    String basic = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(getBaseUrl() + "/v1/oauth2/token"))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization", "Basic " + basic)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();
    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) throw new PayPalException("PayPal token error " + resp.statusCode());
    JsonNode node = mapper.readTree(resp.body());
    return node.path("access_token").asText();
}
```

OAuth2 client credentials con HTTP Basic Auth (clientId:clientSecret en base64).

`createOrder(reserva)`:
```java
String body = """
        {
          "intent": "CAPTURE",
          "purchase_units": [{
            "reference_id": "reserva-%d",
            "description": %s,
            "amount": { "currency_code": "%s", "value": "%s" }
          }]
        }
        """.formatted(reserva.getId(), mapper.valueToTree(descripcion).toString(), currency, amount);
```

`Locale.US` en el `String.format("%.2f", precio)` para que el separador decimal sea punto (no coma) — PayPal exige punto.

`captureOrder(orderId)`: POST a `/v2/checkout/orders/{id}/capture` con `Bearer <accessToken>`.

### `StripeService.java`

```java
@PostConstruct
void init() {
    if (secretKey != null && !secretKey.isBlank()) {
        Stripe.apiKey = secretKey;  // setea la API key globalmente
    }
}

public Session createCheckoutSession(Reserva reserva) throws StripeException {
    if (!isEnabled()) throw new IllegalStateException("Stripe no está configurado");
    long amountCents = Math.round(reserva.getPrecioTotal() * 100);  // céntimos
    SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
            .setQuantity(1L)
            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(currency)
                    .setUnitAmount(amountCents)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(tituloInmersion)
                            .setDescription("DiveConnect - " + reserva.getNumeroPersonas() + " pax")
                            .build())
                    .build())
            .build();
    SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(frontendUrl + "/pages/reservas.html?payment=success&reserva=" + reserva.getId())
            .setCancelUrl(frontendUrl + "/pages/reservas.html?payment=cancelled&reserva=" + reserva.getId())
            .addLineItem(lineItem)
            .putMetadata("reservaId", String.valueOf(reserva.getId()))
            .build();
    return Session.create(params);
}
```

Stripe SDK con builder pattern. `setUnitAmount(amountCents)` porque Stripe trabaja siempre en céntimos. `successUrl` y `cancelUrl` redirigen a `reservas.html` con query params para que el frontend gestione la vuelta.

### `NotificacionService.java`

`crear(destinatario, emisor, tipo, entidadId, mensaje, accionable)`: punto único para crear notificaciones. Centraliza:
- `accionable = true` para SOLICITUD_SEGUIMIENTO (botones aceptar/rechazar).
- `entidadRelacionadaId`: id de la SolicitudSeguimiento (no del usuario emisor) para que el frontend pueda actuar directamente.

`marcarLeida(id)`, `marcarTodasLeidas(usuario)`, `contarNoLeidas(usuario)`, `listar(usuario)`.

### `SeguimientoService.java`

```java
@Transactional
public SeguimientoResponse solicitar(Long solicitanteId, Long destinatarioId) {
    if (solicitanteId.equals(destinatarioId)) throw new BadRequestException("No puedes seguirte");
    Usuario solicitante = usuarioRepository.findById(solicitanteId).orElseThrow(...);
    Usuario destinatario = usuarioRepository.findById(destinatarioId).orElseThrow(...);

    if (usuarioRepository.existsSeguimiento(solicitanteId, destinatarioId)) {
        throw new BadRequestException("Ya sigues a este usuario");
    }

    if (destinatario.getTipoUsuario() == TipoUsuario.USUARIO_COMUN
            && solicitante.getTipoUsuario() == TipoUsuario.USUARIO_COMUN) {
        // Crear solicitud pendiente
        SolicitudSeguimiento sol = solicitudRepo.findBySolicitanteAndDestinatarioAndEstado(...)
                .orElseGet(() -> {
                    SolicitudSeguimiento s = new SolicitudSeguimiento();
                    s.setSolicitante(solicitante);
                    s.setDestinatario(destinatario);
                    s.setEstado(EstadoSolicitud.PENDIENTE);
                    return solicitudRepo.save(s);
                });
        notificacionService.crear(destinatario, solicitante,
            TipoNotificacion.SOLICITUD_SEGUIMIENTO, sol.getId(),
            "@" + solicitante.getUsername() + " quiere seguirte", true);
        return new SeguimientoResponse("SOLICITUD_PENDIENTE");
    } else {
        // Sigue directo
        usuarioRepository.addSeguidor(solicitanteId, destinatarioId);
        notificacionService.crear(destinatario, solicitante,
            TipoNotificacion.NUEVO_SEGUIDOR, solicitante.getId(),
            "@" + solicitante.getUsername() + " ha comenzado a seguirte", false);
        return new SeguimientoResponse("SIGUIENDO");
    }
}
```

`aceptar(solicitudId)`:
```java
SolicitudSeguimiento sol = solicitudRepo.findById(solicitudId).orElseThrow(...);
sol.setEstado(EstadoSolicitud.ACEPTADA);
sol.setFechaRespuesta(LocalDateTime.now());
solicitudRepo.save(sol);
usuarioRepository.addSeguidor(sol.getSolicitante().getId(), sol.getDestinatario().getId());
notificacionService.crear(sol.getSolicitante(), sol.getDestinatario(),
    TipoNotificacion.SEGUIMIENTO_ACEPTADO, ...);
```

### `SearchService.java`

Combina resultados de tres dominios:

```java
public SearchResponse buscar(String q, String tipo, Double lat, Double lon, Double profMin, Double profMax, Double precioMax, String nivel) {
    List<UsuarioResponse> usuarios = ("usuarios".equals(tipo) || "todo".equals(tipo) || tipo == null)
        ? usuarioService.buscarUsuarios(q) : List.of();
    List<UsuarioResponse> empresas = (...) ? usuarioService.obtenerEmpresas(q) : List.of();
    List<InmersionResponse> inmersiones = (...) ? inmersionService.buscarAvanzado(q, profMin, profMax, precioMax, nivel) : List.of();

    boolean inmersionesPorProximidad = false;
    if (inmersiones.isEmpty() && lat != null && lon != null) {
        List<InmersionRepository.InmersionConDistancia> cercanas =
            inmersionRepository.findMasCercanas(lat, lon, PageRequest.of(0, PROXIMIDAD_N));
        if (!cercanas.isEmpty()) {
            Map<Long, Double> distancias = cercanas.stream().collect(Collectors.toMap(
                InmersionRepository.InmersionConDistancia::getId,
                InmersionRepository.InmersionConDistancia::getDistanciaKm));
            List<Inmersion> entities = inmersionRepository.findAllById(distancias.keySet());
            inmersiones = entities.stream()
                .map(i -> {
                    InmersionResponse r = inmersionService.toResponse(i);
                    r.setDistanciaKm(distancias.get(i.getId()));
                    return r;
                })
                .sorted(Comparator.comparing(InmersionResponse::getDistanciaKm))
                .collect(Collectors.toList());
            inmersionesPorProximidad = true;
        }
    }
    return new SearchResponse(q, usuarios, empresas, inmersiones, inmersionesPorProximidad);
}
```

Si la búsqueda textual no devuelve inmersiones y hay coordenadas, hace fallback de proximidad con Haversine. La projection de Spring Data se mapea a `Map<Long, Double>` y luego se cargan las entidades completas con `findAllById`.

### Otros services

- `UsuarioService`: registro (con BCrypt), actualizar perfil, búsqueda, listado de empresas.
- `CentroBuceoService`: CRUD de centros.
- `InmersionService`: CRUD con verificación de propiedad. `toResponse(inmersion)` para mapear.
- `PublicacionService`: feed paginado, like/unlike (con notificación al autor).
- `ComentarioService`: crear comentario con notificación.
- `HistoriaService`: crear (calcula `expiraEn = now + 24h`), listar activas.
- `WeatherService`: WebClient a OpenWeatherMap, fallback mock.
- `UsuarioMapper`: convierte Usuario → UsuarioResponse ocultando campos sensibles (no devuelve password).

---

## 2.9. `dto/` — Transferencia de datos (22 archivos)

### Patrón

Todos son POJOs con:
- `@Data` (Lombok).
- Para requests, validación con `@NotBlank`, `@Email`, `@Size`, `@Min`.
- Sin lógica de negocio.

### Requests más relevantes

`LoginRequest`:
```java
@Data
public class LoginRequest {
    @NotBlank(message = "El username o email es obligatorio")
    private String usernameOrEmail;
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
```

`RegistroRequest`: username (3-50 chars), email, password (mín 6), tipoUsuario, datos de empresa nullable.

`ReservaRequest`:
```java
@NotNull private Long inmersionId;
@NotNull @Min(1) private Integer numeroPersonas;
private String observaciones;
```

`ActualizarPerfilRequest`: biografia, fotoPerfil, nivelCertificacion, numeroInmersiones, datos de empresa.

`PublicacionRequest`: contenido, imagenUrl, videoUrl, datos técnicos opcionales.

`ComentarioRequest`: contenido (max 1000).

`InmersionRequest`, `CentroBuceoRequest`, `PuntoMapaRequest`, `HistoriaRequest`: campos correspondientes a sus entidades.

### Responses más relevantes

`AuthResponse`:
```java
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo;  // "Bearer"
    private UsuarioResponse usuario;
}
```

`UsuarioResponse`: todos los campos públicos del usuario, **sin password**, con número de seguidores, siguiendo y publicaciones (calculado).

`ReservaResponse`: campos de Reserva + nombres derivados (usuarioUsername, inmersionTitulo, centroBuceoNombre, inmersionFecha) para evitar al frontend tener que cruzar múltiples responses.

`PublicacionResponse`: campos + numeroLikes, numeroComentarios, likedByCurrentUser.

`InmersionResponse`: campos + centroBuceoNombre, distanciaKm (nullable, solo en proximidad).

`SearchResponse`: `{query, usuarios, empresas, inmersiones, inmersionesPorProximidad: boolean}`.

`NotificacionResponse`, `ComentarioResponse`, `HistoriaResponse`, `PuntoMapaResponse`, `CentroBuceoResponse`: responses correspondientes.

---

## 2.10. `static/js/` — Módulos JavaScript (7 archivos)

### `api.js` — Wrapper de fetch + helpers globales

```javascript
const API_BASE_URL = '/api';

async function fetchAPI(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const config = { ...options, headers: { ...headers, ...(options.headers || {}) } };

    let response;
    try {
        response = await fetch(API_BASE_URL + endpoint, config);
    } catch (networkError) {
        throw new Error('No se pudo conectar con el servidor');
    }

    if (response.status === 401 || response.status === 403) {
        _limpiarSesionYRedirigir();
        return null;
    }
    if (response.status === 204) return null;

    const contentType = response.headers.get('content-type') || '';
    let data = null;
    if (contentType.includes('application/json')) {
        try { data = await response.json(); }
        catch { /* body vacío o malformado */ }
    } else if (!response.ok) {
        throw new Error('Error ' + response.status + ' del servidor');
    }
    if (!response.ok) throw new Error((data && data.message) || 'Error ' + response.status);
    return data;
}
```

- Añade JWT del localStorage automáticamente.
- 401/403 → limpia sesión + redirige a login.
- 204 → null (sin contenido).
- Parseo JSON tolerante a errores.
- Lanza Error con mensaje del backend si lo hay.

Helpers globales: `showAlert(msg, type)` (toast), `escapeHtml(text)`, `formatFecha(date)`, `getCurrentUser()`, `isAuthenticated()`.

### `auth.js`

```javascript
async function login(usernameOrEmail, password) {
    const data = await fetchAPI('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ usernameOrEmail, password })
    });
    if (data) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.usuario));
    }
    return data;
}

async function registrarUsuario(userData) {
    return await fetchAPI('/auth/registro', {
        method: 'POST',
        body: JSON.stringify(userData)
    });
}

function logout() {
    localStorage.clear();
    window.location.href = '/pages/login.html';
}
```

### `nav.js`

`initNav()` pinta la topbar y el dock inferior según el rol del usuario. `refreshNotificationsBadge()` hace poll cada 30 s a `/api/notificaciones/no-leidas` y actualiza el badge numérico.

### `payment.js` (552 líneas, módulo más complejo)

Modal de pago multi-pasarela. Estructura:

```javascript
(function () {
    const STYLES = `...CSS embebido del modal...`;
    let _modalEl, _paypalCfg, _stripeCfg, _paypalSdkLoaded, _paypalRendered, _currentReserva;

    async function fetchPaypalConfig() {
        try { return (await fetch('/api/paypal/config')).json(); }
        catch { return { enabled: false }; }
    }
    async function fetchStripeConfig() {
        try { return (await fetch('/api/payments/config')).json(); }
        catch { return { enabled: false }; }
    }

    function modeBadge() {
        const stripe = _stripeCfg && _stripeCfg.enabled;
        const paypal = _paypalCfg && _paypalCfg.enabled;
        if (!stripe && !paypal) return '<span class="pay-mode-badge demo">TFG · Demo</span>';
        const ppMode = paypal ? (_paypalCfg.mode || 'sandbox').toLowerCase() : null;
        return ppMode === 'live'
            ? '<span class="pay-mode-badge live">Live</span>'
            : '<span class="pay-mode-badge">Sandbox</span>';
    }

    function renderForm({ total, concepto }) {
        // genera HTML con dos pestañas (Tarjeta + PayPal)
        // si Stripe está activo: botón "Pagar X € con Stripe" → redirección
        // si no: formulario con Luhn local
    }

    function luhn(num) {
        const digits = (num || '').replace(/\D/g, '');
        if (digits.length < 12) return false;
        let sum = 0, alt = false;
        for (let i = digits.length - 1; i >= 0; i--) {
            let n = parseInt(digits.charAt(i), 10);
            if (alt) { n *= 2; if (n > 9) n -= 9; }
            sum += n; alt = !alt;
        }
        return sum % 10 === 0;
    }

    async function procesarPagoDemo(total) {
        // valida Luhn + caducidad + CVC, luego POST /api/payments/verify/{id}
    }

    async function irAStripeCheckout(btn) {
        // POST /api/payments/checkout/{id}, redirige a session.url
    }

    async function renderPaypalButtons(total) {
        // carga el SDK de PayPal e instancia los botones oficiales
        // createOrder → POST /api/paypal/create-order/{id}
        // onApprove → POST /api/paypal/capture-order/{id}?orderId=...
    }

    window.abrirModalPago = async function ({ reservaId, total, concepto, onSuccess }) {
        if (Number(total || 0) <= 0) return autoConfirmarGratis(reservaId, onSuccess);
        if (!_paypalCfg || !_stripeCfg) {
            const [pp, st] = await Promise.all([fetchPaypalConfig(), fetchStripeConfig()]);
            _paypalCfg = pp; _stripeCfg = st;
        }
        // renderizar y abrir
    };
    window.cerrarModalPago = function() { ... };
})();
```

Puntos clave:
- IIFE para encapsular estado.
- `Promise.all` para fetch paralelo de las dos configs.
- Detección automática del modo según respuestas del backend.
- Luhn para validar tarjeta sin enviar al servidor en modo demo.
- Integración con SDK PayPal cargado dinámicamente.

### `ocean-effects.js`

Tres responsabilidades:

1. **Burbujas**: `injectBubbles()` crea 14-28 spans con CSS keyframes.
2. **Reveal-on-scroll**: `IntersectionObserver` añade `.in-view` cuando un `[data-reveal]` entra en viewport.
3. **Tilt 3D**: en `mousemove` sobre `.dive-card`, rota ±4° con `transform: rotateX/rotateY`.

Skip en `prefers-reduced-motion: reduce`.

### Otros JS

- `publicaciones.js`: `createPublicacion`, `likePublicacion`, comentarios, render de feed estilo IG.
- `animations.js`: animaciones de entrada en scroll para landing.

---

## 2.11. `static/css/` — Estilos (2 archivos)

### `style.css` (1184 líneas)

Sistema de diseño base. Contiene:

- **Tokens raíz** (`:root`): paleta (cream, navy, seafoam, coral, gold, indigo, sky), tipografía (DM Serif Display, Nunito Sans), radios (`--r-xs` a `--r-2xl`), sombras (`--sh-sm` a `--sh-xl`), easings (`--ease`, `--ease-spring`).
- **Reset**: `*,*::before,*::after { box-sizing: border-box; margin: 0; padding: 0 }`.
- **Componentes**: botones (`.btn`, `.btn-primary`, `.btn-secondary`, `.btn-danger`), inputs (`.form-input`, `.form-label`), cards, modales (`.dv-modal-backdrop`, `.modal-overlay`).
- **Layout**: `.container`, `.navbar`, `.dock`, `.feed-wrap`.

### `ocean-theme.css` (572 líneas)

Capa overlay sobre `style.css`. Lo crítico:

```css
:root {
    --ocean-deep:     #0B1A2B;
    --ocean-shallow:  #1B4965;
    --ocean-surface:  #62B6CB;
    --ocean-foam:     #DCF3EE;
    --ocean-glow:     #00D4AA;
    --glass-bg:       rgba(255, 255, 255, .58);
    --glass-shadow:   0 8px 32px rgba(11, 26, 43, .08), inset 0 1px 0 rgba(255, 255, 255, .65);
}

body {
    background: radial-gradient(ellipse 140% 80% at 50% -20%, rgba(98, 182, 203, .55), transparent 55%),
                radial-gradient(ellipse 100% 50% at 80% 110%, rgba(0, 212, 170, .22), transparent 60%),
                linear-gradient(180deg, var(--ocean-cream) 0%, var(--ocean-foam) 50%, #62B6CB 100%);
    background-attachment: fixed;
}

body::before { /* caustics animadas */
    animation: ocean-caustic 22s ease-in-out infinite alternate;
}

body::after { /* light rays */
    mix-blend-mode: screen;
    animation: ocean-rays 14s ease-in-out infinite alternate;
}

.ocean-bubble {
    position: absolute;
    border-radius: 50%;
    background: radial-gradient(circle at 35% 30%, rgba(255, 255, 255, .85), rgba(190, 233, 232, .35) 55%);
    box-shadow: inset -2px -3px 6px rgba(98, 182, 203, .25), inset 2px 2px 4px rgba(255, 255, 255, .6);
    animation: ocean-rise infinite;
}

@keyframes ocean-rise {
    0%   { transform: translate(0, 0); opacity: 0; }
    10%  { opacity: var(--bubble-opacity, .7); }
    50%  { transform: translate(var(--bubble-drift, 18px), -55vh); }
    100% { transform: translate(calc(var(--bubble-drift, 18px) * -.6), -110vh); opacity: 0; }
}

.post-card, .reserva-card, .notif-item, .inmersion-card, .dv-modal {
    background: var(--glass-bg) !important;
    backdrop-filter: blur(18px) saturate(160%);
    border: 1px solid var(--glass-border) !important;
    box-shadow: var(--glass-shadow) !important;
}

@media (max-width: 720px) {
    .dv-modal-backdrop { align-items: flex-end !important; padding: 0 !important; }
    .dv-modal { max-width: 100% !important; border-radius: 24px 24px 0 0 !important; }
    .form-input, .dv-input { min-height: 44px; font-size: 16px !important; }
}

@media (prefers-reduced-motion: reduce) {
    body::before, body::after, .ocean-bubble { animation: none !important; }
}
```

---

## 2.12. `static/pages/` — HTML (21 archivos)

### Patrón

Cada `.html`:
- `<!DOCTYPE html>` + `<html lang="es">`.
- `<head>` con meta charset, viewport `viewport-fit=cover`, meta description, OpenGraph, theme-color, title específico.
- Carga `style.css` y `ocean-theme.css`.
- Bloque `<style>` con estilos específicos de la página.
- `<body>` con `<header class="topbar" id="mainNavbar">` que se rellena con `nav.js`.
- Scripts al final: `api.js`, `auth.js`, `nav.js`, módulos específicos, `ocean-effects.js` con `defer`.

### Páginas principales (USUARIO_COMUN)

- `index.html`: landing pública con hero, ventajas, testimonios.
- `login.html`: formulario de login con `loginForm.addEventListener('submit', ...)` que llama `login()` y redirige a `/pages/feed.html`.
- `register.html`: formulario de registro con elección de tipoUsuario (radio buttons COMUN/EMPRESA).
- `oauth-callback.html`: lee `?token=...&user=...` de la URL, los guarda en localStorage y redirige a feed.
- `feed.html`: feed con stories + publicaciones + modal crear publicación + modal crear historia + visor de historias. Toda la lógica embebida en el `<script>` interno.
- `Inmersiones.html`: catálogo con filtros (nivel, precio min/max, profundidad min/max, búsqueda por texto). Modal de reserva.
- `reservas.html`: stats bar (4 stats) + filter tabs por estado + cards con botón "Pagar ahora" o "Cancelar".
- `Perfil.html`: hero con avatar y stats + form editar perfil + lista publicaciones + reservas recientes.
- `buscar.html`: búsqueda universal con tabs y geolocalización.
- `mapa.html`: Leaflet 1.9 con marcadores y popups + sidebar con lista de puntos + modal detalle con weather.
- `notificaciones.html`: lista cronológica con botones de acción para SOLICITUD_SEGUIMIENTO.

### Páginas empresa (`pages/empresa/`)

- `dashboard.html`, `mi-centro.html`, `mis-inmersiones.html`, `gestionar-reservas.html`.

### Páginas admin (`pages/admin/`)

- `dashboard.html`, `usuarios.html`, `centros.html`, `inmersiones.html`, `reservas.html`.

---

## 2.13. `src/test/java/` — Tests JUnit (5 archivos · 31 tests)

### `ReservaServiceTest.java` (5 tests)

Usa Mockito puro (`@ExtendWith(MockitoExtension.class)`), sin Spring context, así que arranca en milisegundos.

```java
@Mock private ReservaRepository  reservaRepository;
@Mock private InmersionRepository inmersionRepository;
@Mock private UsuarioRepository  usuarioRepository;
@InjectMocks private ReservaService reservaService;
```

`@Mock` crea mocks; `@InjectMocks` inyecta los mocks por constructor (Lombok genera el constructor con `@RequiredArgsConstructor`).

Casos:
1. `crearReserva_caminoFeliz`: verifica creación, descuento de plazas, return correcto.
2. `crearReserva_sinPlazas`: lanza `BadRequestException` y NO toca BD (`verify(reservaRepository, never()).save(any())`).
3. `crearReserva_inmersionNoExiste`: `ResourceNotFoundException`.
4. `crearReserva_usuarioNoExiste`: `ResourceNotFoundException`.
5. `marcarComoPagada_actualizaCorrectamente`: cambia estado a CONFIRMADA + paymentStatus PAID.

### `PayPalServiceTest.java` (8 tests)

Sin Spring. `ReflectionTestUtils.setField` para inyectar valores en campos `@Value` privados.

Casos: `isConfigured` con varios estados de clientId/secret, `fetchAccessToken` sin configurar lanza excepción, `getBaseUrl` sandbox/live/desconocido, `getCurrency`.

### `StripeServiceTest.java` (5 tests)

Igual patrón. `isEnabled` con secret vacía/null/válida, `getPublishableKey` nunca null, `createCheckoutSession` sin configurar lanza `IllegalStateException`.

### `UploadControllerTest.java` (5 tests)

Usa `MockMultipartFile` y un directorio temporal real:

```java
@BeforeEach void setUp() throws IOException {
    controller = new UploadController();
    tempDir = Files.createTempDirectory("dc-upload-test-");
    ReflectionTestUtils.setField(controller, "uploadDir", tempDir.toString());
}

@AfterEach void tearDown() throws IOException {
    if (tempDir != null && Files.exists(tempDir)) {
        Files.walk(tempDir).sorted(Comparator.reverseOrder())
             .map(Path::toFile).forEach(java.io.File::delete);
    }
}
```

Casos: subida correcta de PNG (verifica que el archivo existe en disco), subida video MP4, sin fichero, MIME no aceptado, extensión peligrosa.

### `EmojiStripMigrationTest.java` (8 tests)

Tests del helper estático `stripEmojis(text)`:

1. Texto sin emojis: queda intacto.
2. Pictograph (🐠): se quita.
3. Misc symbol (☀): se quita.
4. Variation selector + dingbat (✔️): se quitan.
5. Doble espacio: se compacta.
6. Espacio antes de puntuación: se quita.
7. Múltiples emojis seguidos: todos.
8. Salto de línea: preserva pero limpia trailing.

---

## 2.14. `database/` — SQL (3 archivos)

### `schema.sql`

Dump generado con `mysqldump --no-data`. Contiene la estructura de las 13 tablas con sus PKs, FKs, índices, charsets y collations. Hibernate genera lo mismo automáticamente con `ddl-auto=update`, así que este archivo es **referencia histórica** y permite recrear la BD desde cero sin la app.

### `views.sql`

5 vistas:

```sql
CREATE VIEW vw_reservas_resumen AS
SELECT r.id AS reserva_id, r.fecha_reserva, r.estado AS estado_reserva,
       r.payment_status AS estado_pago, r.numero_personas AS personas,
       r.precio_total AS importe_total,
       u.id AS usuario_id, u.username, u.email, u.nivel_certificacion,
       i.id AS inmersion_id, i.titulo AS inmersion_titulo, i.fecha_inmersion,
       i.profundidad_maxima AS inmersion_profundidad, i.nivel_requerido AS inmersion_nivel,
       c.id AS centro_id, c.nombre AS centro_nombre, c.ciudad AS centro_ciudad,
       CASE WHEN r.payment_status = 'PAID' THEN 'Pagada'
            WHEN r.payment_status = 'UNPAID' THEN 'Pendiente'
            ELSE 'Estado desconocido' END AS estado_pago_humano
FROM reservas r
JOIN usuarios u       ON u.id = r.usuario_id
JOIN inmersiones i    ON i.id = r.inmersion_id
JOIN centros_buceo c  ON c.id = r.centro_buceo_id;

CREATE VIEW vw_estadisticas_centro AS
SELECT c.id, c.nombre AS centro, c.ciudad,
       COUNT(DISTINCT i.id) AS num_inmersiones,
       COUNT(DISTINCT r.id) AS num_reservas,
       COALESCE(SUM(CASE WHEN r.estado IN ('CONFIRMADA','COMPLETADA') THEN r.numero_personas ELSE 0 END), 0) AS plazas_vendidas,
       COALESCE(SUM(CASE WHEN r.payment_status = 'PAID' THEN r.precio_total ELSE 0 END), 0) AS ingresos_pagados,
       c.valoracion_promedio AS valoracion
FROM centros_buceo c
LEFT JOIN inmersiones i ON i.centro_buceo_id = c.id AND i.activa = TRUE
LEFT JOIN reservas r    ON r.centro_buceo_id = c.id
GROUP BY c.id, c.nombre, c.ciudad, c.valoracion_promedio;

CREATE VIEW vw_actividad_usuario AS ...   -- subquery per usuario para stats
CREATE VIEW vw_inmersiones_disponibles AS ...
CREATE VIEW vw_notificaciones_no_leidas AS ...
```

### `procedures.sql`

3 procedures + 1 que crea índices:

```sql
DELIMITER //

CREATE PROCEDURE sp_marcar_reserva_pagada(IN p_reserva_id BIGINT, IN p_payment_ref VARCHAR(255), IN p_pasarela VARCHAR(20))
BEGIN
    DECLARE v_usuario_id BIGINT;
    DECLARE v_centro_usuario_id BIGINT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT r.usuario_id, c.usuario_id INTO v_usuario_id, v_centro_usuario_id
        FROM reservas r JOIN centros_buceo c ON c.id = r.centro_buceo_id
        WHERE r.id = p_reserva_id;
    IF v_usuario_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Reserva no encontrada';
    END IF;
    UPDATE reservas SET payment_status = 'PAID', estado = 'CONFIRMADA', ...
        WHERE id = p_reserva_id;
    INSERT INTO notificaciones(...) VALUES(v_usuario_id, ..., 'RESERVA_CONFIRMADA', ...);
    IF v_centro_usuario_id IS NOT NULL THEN
        INSERT INTO notificaciones(...) VALUES(v_centro_usuario_id, ..., 'RESERVA_RECIBIDA', ...);
    END IF;
    COMMIT;
END//

CREATE PROCEDURE sp_purgar_historias_expiradas()
BEGIN
    DELETE FROM historias WHERE expira_en < NOW();
    SELECT ROW_COUNT() AS historias_purgadas;
END//

CREATE PROCEDURE sp_estadisticas_globales()
BEGIN
    SELECT (SELECT COUNT(*) FROM usuarios WHERE activo = TRUE) AS usuarios_activos,
           (SELECT COUNT(*) FROM usuarios WHERE tipo_usuario = 'USUARIO_EMPRESA') AS centros_registrados,
           (SELECT COUNT(*) FROM inmersiones WHERE activa = TRUE) AS inmersiones_activas,
           (SELECT COUNT(*) FROM publicaciones) AS total_publicaciones,
           (SELECT COUNT(*) FROM reservas) AS total_reservas,
           (SELECT COUNT(*) FROM reservas WHERE payment_status = 'PAID') AS reservas_pagadas,
           (SELECT COALESCE(SUM(precio_total),0) FROM reservas WHERE payment_status = 'PAID') AS ingresos_acumulados;
END//

CREATE PROCEDURE sp_crear_indices_si_faltan()
BEGIN
    -- Comprueba con information_schema y crea con ALTER TABLE ADD INDEX si no existe
END//

DELIMITER ;
```

`DELIMITER //` cambia el separador a `//` para que el `;` interno no termine el procedure. `EXIT HANDLER FOR SQLEXCEPTION` es el equivalente a try/catch en SQL.

---

## 2.15. `scripts/analytics/` — Python ETL

`analytics.py`:

```python
import pymysql
import pandas as pd
import matplotlib.pyplot as plt

PALETTE = {
    "navy": "#0B1A2B", "navy_mid": "#1B4965",
    "seafoam": "#00D4AA", "indigo": "#5B5EA6",
    "coral": "#FF6B6B", "gold": "#F5A623", ...
}

def connect(args):
    return pymysql.connect(host=args.db_host, port=args.db_port,
                           user=args.db_user, password=args.db_pass,
                           database=args.db_name, charset="utf8mb4",
                           cursorclass=pymysql.cursors.DictCursor)

def fetch_df(conn, sql):
    with conn.cursor() as cur:
        cur.execute(sql)
        return pd.DataFrame(cur.fetchall())

def publicaciones_por_mes(conn):
    return fetch_df(conn, """
        SELECT DATE_FORMAT(fecha_publicacion, '%Y-%m') AS mes,
               COUNT(*) AS publicaciones
        FROM publicaciones
        GROUP BY mes
        ORDER BY mes ASC
    """)

def grafica_publicaciones_por_mes(df, out):
    fig, ax = plt.subplots()
    ax.bar(df["mes"], df["publicaciones"], color=PALETTE["seafoam"])
    ax.set_title("Publicaciones por mes")
    fig.savefig(out)
    plt.close(fig)
```

5 datasets, 3 gráficas PNG, 1 CSV consolidado. Idempotente.

---

## 2.16. Configuración del proyecto

### `pom.xml`

Maven build con parent `spring-boot-starter-parent:3.2.3`. Dependencias clave:

- `spring-boot-starter-web` (Tomcat embebido + Spring MVC).
- `spring-boot-starter-security` (Spring Security 6).
- `spring-boot-starter-data-jpa` (Hibernate + Spring Data).
- `spring-boot-starter-validation` (jakarta.validation).
- `spring-boot-starter-oauth2-client` (Google OAuth2 opcional).
- `spring-boot-starter-webflux` (WebClient para OpenWeatherMap).
- `mysql-connector-j`, scope runtime.
- `lombok`, scope provided (no va al JAR).
- `jjwt-api/impl/jackson` 0.11.5.
- `stripe-java` 24.16.0.
- `springdoc-openapi-starter-webmvc-ui` 2.3.0.
- `spring-boot-starter-test` (JUnit 5 + Mockito + AssertJ).

### `application.properties`

```properties
server.port=${PORT:8080}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/diveconnect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
spring.datasource.username=${DB_USERNAME:diveconnect_user}
spring.datasource.password=${DB_PASSWORD:DiveConnect2025!}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

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

Patrón `${VAR:default}`: si la env var existe la usa, si no usa el default.

### `Dockerfile`

Multi-stage:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S diveconnect && adduser -S -G diveconnect diveconnect
RUN mkdir -p /app/uploads && chown diveconnect:diveconnect /app/uploads
VOLUME ["/app/uploads"]
COPY --from=build /app/target/diveconnect-*.jar /app/app.jar
RUN chown diveconnect:diveconnect /app/app.jar
USER diveconnect
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/api/paypal/config || exit 1
ENV JAVA_OPTS="-Xms256m -Xmx450m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

- Stage 1: Maven con JDK 17, descarga dependencias, empaqueta JAR (sin tests).
- Stage 2: imagen Alpine con solo JRE (más pequeña). Usuario no-root. Volumen para uploads. Healthcheck. Heap limitado a 450 MB para Render free.

### `docker-compose.yml`

Orquesta MySQL 8 + app con dependencias y healthchecks. Volúmenes `mysql_data` y `app_uploads` para persistencia.

### `render.yaml`

Blueprint declarativo de Render.com:

```yaml
services:
  - type: web
    name: diveconnect
    runtime: docker
    plan: free
    region: frankfurt
    branch: master
    dockerfilePath: ./Dockerfile
    healthCheckPath: /api/paypal/config
    autoDeploy: true
    envVars:
      - key: SPRING_DATASOURCE_URL
        fromDatabase: { name: diveconnect-db, property: connectionString }
      - key: DB_USERNAME
        fromDatabase: { name: diveconnect-db, property: user }
      - key: DB_PASSWORD
        fromDatabase: { name: diveconnect-db, property: password }
      - key: JWT_SECRET
        generateValue: true
      - key: STRIPE_SECRET_KEY
        sync: false
      ... (resto de env vars)

databases:
  - name: diveconnect-db
    plan: free
    databaseName: diveconnect_db
    region: frankfurt
```

`fromDatabase`: Render inyecta automáticamente la URL de conexión, usuario y contraseña de la BD gestionada. `generateValue: true`: Render genera un secret aleatorio. `sync: false`: tienes que definirla manualmente en el dashboard.

### `.github/workflows/ci.yml`

GitHub Actions:

```yaml
name: CI
on:
  push: { branches: [master, claude/peaceful-hellman] }
  pull_request: { branches: [master] }

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env: { MYSQL_ROOT_PASSWORD: rootpass, MYSQL_DATABASE: diveconnect_db, ... }
        ports: ["3306:3306"]
        options: >-
          --health-cmd="mysqladmin ping --silent" --health-interval=10s ...
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17', cache: maven }
      - run: ./mvnw -B -q compile
      - run: ./mvnw -B test
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/diveconnect_db?...
          ...
      - uses: actions/upload-artifact@v4
        with: { name: surefire-reports, path: target/surefire-reports/ }
      - run: ./mvnw -B -q -DskipTests package
      - uses: actions/upload-artifact@v4
        with: { name: diveconnect-jar, path: target/diveconnect-*.jar }

  docker-build:
    needs: build-and-test
    if: github.ref == 'refs/heads/master'
    runs-on: ubuntu-latest
    steps:
      - uses: docker/setup-buildx-action@v3
      - uses: docker/build-push-action@v5
        with: { context: ., file: ./Dockerfile, push: false, ... }
```

`services.mysql`: GitHub levanta un contenedor MySQL para los tests. La app conecta con `localhost:3306`.

---

# 3. Código por funcionalidad

Esta sección responde a "y eso que estás enseñando, ¿qué archivos lo hacen?". Para cada flujo, lista los archivos involucrados.

## 3.1. Login con email + contraseña

```
Cliente → POST /api/auth/login {usernameOrEmail, password}
       ↓
       AuthController.login()
       ↓
       authenticationManager.authenticate(...) → UserDetailsServiceImpl.loadUserByUsername()
       ↓                                          ↓ usuarioRepository.findByUsername()
       BCryptPasswordEncoder.matches()
       ↓
       JwtUtil.generateToken()
       ↓
Cliente ← {token, tipo: "Bearer", usuario}
```

**Archivos**:
- Frontend: `pages/login.html`, `js/auth.js`, `js/api.js`.
- Backend: `controller/AuthController.java`, `dto/request/LoginRequest.java`, `dto/response/AuthResponse.java`, `security/UserDetailsServiceImpl.java`, `security/JwtUtil.java`, `service/UsuarioService.java`, `repository/UsuarioRepository.java`, `entity/Usuario.java`.

## 3.2. Login con Google OAuth2

**Archivos**:
- Frontend: `pages/login.html` (botón), `pages/oauth-callback.html` (recibe el token), `js/auth.js`.
- Backend: `security/GoogleOAuth2SuccessHandler.java`, `config/SecurityConfig.java` (registro condicional), `service/UsuarioService.java`.

## 3.3. Crear publicación con foto subida desde galería

```
Modal "Crear publicación" → file picker
                          ↓
                          POST /api/uploads (FormData multipart)
                          ↓
                          UploadController.subirArchivo()
                          ↓ valida MIME + extensión + UUID
                          ↓ guarda en uploads/<uuid>.png
                          ↓
Frontend ← {url: "/uploads/abc.png", tipo: "FOTO"}
                          ↓ rellena URL en hidden input
                          ↓ usuario rellena descripción + datos técnicos
                          ↓
                          POST /api/publicaciones {contenido, imagenUrl, ...}
                          ↓
                          PublicacionController + Service + Repository → MySQL
```

**Archivos**:
- Frontend: `pages/feed.html`, `js/api.js`, `js/publicaciones.js`.
- Backend: `controller/UploadController.java`, `controller/PublicacionController.java`, `service/PublicacionService.java`, `repository/PublicacionRepository.java`, `entity/Publicacion.java`, `config/WebConfig.java` (resource handler).

## 3.4. Reservar inmersión y pagar

```
Click "Reservar" → modal de reserva → POST /api/reservas
                                    ↓
                                    ReservaService.crearReserva (TX: descuenta plazas, crea Reserva PENDIENTE/UNPAID)
                                    ↓
Modal de pago abre → payment.js detecta config (Stripe/PayPal/demo)
                  ↓
                  Demo: POST /api/payments/verify/{id} con Luhn local
                  Stripe: POST /api/payments/checkout/{id} → redirect Stripe
                  PayPal: SDK + create-order + capture-order
                  ↓
                  PaymentController.confirmarPago():
                    UPDATE reserva PAID + CONFIRMADA
                    INSERT notificación a usuario
                    INSERT notificación a centro
                  ↓
                  Tick verde + redirect /pages/reservas.html
```

**Archivos**:
- Frontend: `pages/Inmersiones.html`, `js/payment.js`, `js/api.js`.
- Backend: `controller/ReservaController.java`, `controller/PaymentController.java`, `controller/PayPalController.java`, `service/ReservaService.java`, `service/PayPalService.java`, `service/StripeService.java`, `service/NotificacionService.java`, `repository/ReservaRepository.java`, `repository/InmersionRepository.java`, `entity/Reserva.java`, `entity/Inmersion.java`, `entity/EstadoReserva.java`.

## 3.5. Sistema de seguimiento

**Archivos**:
- Frontend: `pages/notificaciones.html`, `pages/Perfil.html`, `pages/buscar.html`, `js/api.js`.
- Backend: `controller/SeguimientoController.java`, `service/SeguimientoService.java`, `repository/UsuarioRepository.java` (queries nativas), `repository/SolicitudSeguimientoRepository.java`, `service/NotificacionService.java`, `entity/SolicitudSeguimiento.java`, `entity/EstadoSolicitud.java`.

## 3.6. Búsqueda con fallback de proximidad

**Archivos**:
- Frontend: `pages/buscar.html` (lógica embebida + `js/api.js`).
- Backend: `controller/SearchController.java`, `service/SearchService.java`, `repository/InmersionRepository.java` (Haversine), `dto/response/SearchResponse.java`.

## 3.7. Mapa interactivo + tiempo

**Archivos**:
- Frontend: `pages/mapa.html` (Leaflet + JS embebido), `js/api.js`.
- Backend: `controller/PuntoMapaController.java`, `controller/WeatherController.java`, `service/WeatherService.java`.

## 3.8. Notificaciones (poll badge)

**Archivos**:
- Frontend: `pages/notificaciones.html`, `js/nav.js` (badge con poll cada 30s), `js/api.js`.
- Backend: `controller/NotificacionController.java`, `service/NotificacionService.java`, `repository/NotificacionRepository.java`.

## 3.9. Dashboard de empresa

**Archivos**:
- Frontend: `pages/empresa/dashboard.html`, `pages/empresa/mi-centro.html`, `pages/empresa/mis-inmersiones.html`, `pages/empresa/gestionar-reservas.html`.
- Backend: `controller/CentroBuceoController.java`, `controller/InmersionController.java`, `controller/ReservaController.java` (con verificación de propiedad).

## 3.10. Panel de administración

**Archivos**:
- Frontend: `pages/admin/*.html`.
- Backend: `controller/AdminController.java` (todos los endpoints requieren ROLE_ADMINISTRADOR vía SecurityConfig).

---

# 4. Guion para el tribunal

Está separado en `GUION-DEFENSA.md` (40 páginas, sincronizado minuto a minuto con las 30 slides). Incluye también:

- 12 preguntas frecuentes con respuesta literal preparada.
- Plan B si Render se cae durante la defensa.
- Cronómetro mental cada 5 minutos.
- Lista de pestañas, archivos y herramientas a tener abiertas.
- Qué llevar en papel y en el portátil el día.

Los apuntes finales para preguntas rápidas están en `COBERTURA-RUBRICA.md` con la tabla "Qué demuestra qué" para responder al tribunal en menos de 30 segundos para cada pregunta probable.
