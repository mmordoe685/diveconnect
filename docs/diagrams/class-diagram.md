# Diagrama de clases UML

Diagrama de las clases más relevantes del back-end. Renderiza directamente en GitHub gracias a Mermaid.

## Capa de entidades (modelo de dominio)

```mermaid
classDiagram
    class Usuario {
        +Long id
        +String username
        +String email
        +String password
        +String biografia
        +String fotoPerfil
        +String nivelCertificacion
        +Integer numeroInmersiones
        +TipoUsuario tipoUsuario
        +String nombreEmpresa
        +Boolean activo
        +LocalDateTime fechaRegistro
        +Set~Usuario~ seguidores
        +Set~Usuario~ siguiendo
    }

    class CentroBuceo {
        +Long id
        +Usuario usuario
        +String nombre
        +String descripcion
        +String ciudad
        +String certificaciones
        +Double valoracion
    }

    class Inmersion {
        +Long id
        +CentroBuceo centroBuceo
        +String titulo
        +LocalDateTime fechaInmersion
        +Integer duracion
        +Double profundidadMaxima
        +String nivelRequerido
        +Double precio
        +Integer plazasTotales
        +Integer plazasDisponibles
        +Double latitud
        +Double longitud
        +Boolean activa
    }

    class Reserva {
        +Long id
        +Usuario usuario
        +Inmersion inmersion
        +CentroBuceo centroBuceo
        +Integer numeroPersonas
        +Double precioTotal
        +EstadoReserva estado
        +String paymentStatus
        +String stripeSessionId
        +String paypalOrderId
        +LocalDateTime fechaReserva
    }

    class Publicacion {
        +Long id
        +Usuario usuario
        +String contenido
        +String imagenUrl
        +String videoUrl
        +String lugarInmersion
        +Double profundidadMaxima
        +Set~Usuario~ likes
        +LocalDateTime fechaPublicacion
    }

    class Comentario {
        +Long id
        +Publicacion publicacion
        +Usuario usuario
        +String contenido
        +LocalDateTime fechaComentario
    }

    class Historia {
        +Long id
        +Usuario usuario
        +String mediaUrl
        +TipoMedia mediaType
        +String texto
        +LocalDateTime fechaExpiracion
    }

    class Notificacion {
        +Long id
        +Usuario destinatario
        +Usuario emisor
        +TipoNotificacion tipo
        +String mensaje
        +Boolean leida
        +Boolean accionable
        +Boolean resuelta
        +Long entidadRelacionadaId
    }

    class SolicitudSeguimiento {
        +Long id
        +Usuario solicitante
        +Usuario destinatario
        +EstadoSolicitud estado
    }

    class PuntoMapa {
        +Long id
        +Usuario autor
        +Double latitud
        +Double longitud
        +String titulo
        +Double profundidadMetros
        +List~FotoPuntoMapa~ fotos
    }

    class FotoPuntoMapa {
        +Long id
        +PuntoMapa puntoMapa
        +String url
        +String especieAvistada
    }

    Usuario "1" -- "0..1" CentroBuceo : posee
    CentroBuceo "1" -- "*" Inmersion : ofrece
    Usuario "1" -- "*" Reserva : reserva
    Inmersion "1" -- "*" Reserva : tiene
    Usuario "1" -- "*" Publicacion : publica
    Publicacion "1" -- "*" Comentario : tiene
    Usuario "1" -- "*" Comentario : escribe
    Usuario "*" -- "*" Publicacion : likea
    Usuario "1" -- "*" Historia : crea
    Usuario "1" -- "*" Notificacion : recibe
    Usuario "1" -- "*" SolicitudSeguimiento : envía/recibe
    Usuario "*" -- "*" Usuario : sigue
    Usuario "1" -- "*" PuntoMapa : crea
    PuntoMapa "1" -- "*" FotoPuntoMapa : contiene
```

---

## Capa de servicios (lógica de negocio)

```mermaid
classDiagram
    class UsuarioService {
        +registrarUsuario(RegistroRequest)
        +obtenerPerfil(Long)
        +actualizarPerfil(Long, ActualizarPerfilRequest)
        +seguirUsuario(Long, Long)
        +dejarDeSeguir(Long, Long)
        +obtenerSeguidores(Long)
        +obtenerSiguiendo(Long)
    }

    class ReservaService {
        +crearReserva(Long, ReservaRequest)
        +obtenerReservasDeUsuario(Long)
        +marcarComoPagada(Long, String)
        +cancelarReserva(Long)
    }

    class PayPalService {
        -clientId String
        -clientSecret String
        -mode String
        +isConfigured() boolean
        +fetchAccessToken() String
        +createOrder(Reserva) JsonNode
        +captureOrder(String) JsonNode
    }

    class StripeService {
        -secretKey String
        +isEnabled() boolean
        +createCheckoutSession(Reserva) Session
        +retrieveSession(String) Session
    }

    class NotificacionService {
        +crear(Usuario, Usuario, TipoNotificacion, Long, String, boolean)
        +listar(Long)
        +marcarLeida(Long)
        +contarNoLeidas(Long)
    }

    class SeguimientoService {
        +solicitar(Long, Long)
        +aceptar(Long)
        +rechazar(Long)
        +dejarDeSeguir(Long, Long)
        +estado(Long, Long) EstadoResponse
    }

    class PublicacionService {
        +listar(Pageable)
        +crear(Long, PublicacionRequest)
        +like(Long, Long)
        +eliminar(Long, Long)
    }

    UsuarioService ..> NotificacionService : usa
    SeguimientoService ..> NotificacionService : usa
    ReservaService ..> NotificacionService : usa
    PayPalService ..> NotificacionService : usa
    StripeService ..> NotificacionService : usa
    PublicacionService ..> NotificacionService : usa
```

---

## Capa de seguridad

```mermaid
classDiagram
    class SecurityConfig {
        <<@Configuration>>
        +securityFilterChain(HttpSecurity) SecurityFilterChain
        +passwordEncoder() PasswordEncoder
        +authenticationManager() AuthenticationManager
    }

    class JwtAuthenticationFilter {
        <<@Component>>
        -jwtTokenProvider
        -userDetailsService
        +doFilterInternal(...)
    }

    class JwtTokenProvider {
        <<@Component>>
        -secretKey String
        -jwtExpiration long
        +generarToken(Authentication) String
        +validarToken(String) boolean
        +obtenerUsername(String) String
    }

    class CustomUserDetails {
        <<UserDetails>>
        -usuario Usuario
        +getAuthorities() Collection
        +getUsername() String
    }

    class CustomUserDetailsService {
        <<@Service>>
        +loadUserByUsername(String) UserDetails
    }

    class GoogleOAuth2SuccessHandler {
        <<@Component>>
        +onAuthenticationSuccess(...)
    }

    SecurityConfig --> JwtAuthenticationFilter : registra
    SecurityConfig --> GoogleOAuth2SuccessHandler : registra (si activo)
    JwtAuthenticationFilter --> JwtTokenProvider : valida
    JwtAuthenticationFilter --> CustomUserDetailsService : carga
    CustomUserDetailsService --> CustomUserDetails : devuelve
```

---

## Patrones aplicados

| Patrón | Dónde | Por qué |
|---|---|---|
| **MVC** | controllers/services/repositories | Separación clásica de Spring |
| **Repository** | `UsuarioRepository`, `ReservaRepository`, etc. | Abstrae el acceso a datos |
| **DTO** | `dto/request`, `dto/response` | Aísla la API del modelo de dominio |
| **Strategy** (implícito) | `PaymentController.verificar()` | Decide entre Stripe / demo según configuración |
| **Builder** | `Stripe SessionCreateParams.builder()` | Construcción fluida de objetos complejos |
| **Filter Chain** | `JwtAuthenticationFilter` | Composición de filtros de Spring Security |
| **Singleton** (Spring) | Todos los `@Service`, `@Component` | Por defecto en el contenedor de Spring |
| **Dependency Injection** | Constructor injection con `@RequiredArgsConstructor` | Clarísima la dependencia y facilita test |
