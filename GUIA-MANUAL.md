# Guia y manual de uso - DiveConnect

## 1. Arranque rapido

### Opcion A: Docker Compose

```bash
docker compose up --build
```

Servicios:

- Aplicacion: `http://localhost:8080`
- MySQL: `localhost:3307`
- Base de datos: `diveconnect_db`

### Opcion B: Maven + MySQL local

```bash
mvn spring-boot:run
```

Variables minimas:

```bash
DB_USERNAME=diveconnect_user
DB_PASSWORD=DiveConnect2025!
JWT_SECRET=DiveConnect2025SecretKeyForJWTTokenGenerationMustBeLongEnoughToBeSecure
```

La plantilla completa esta en `.env.example`.

## 2. Usuarios de prueba

| Usuario | Contrasena | Rol | Uso recomendado en demo |
|---|---|---|---|
| `admin` | `admin` | ADMINISTRADOR | Panel admin |
| `oceandive` | `admin` | USUARIO_EMPRESA | Gestion de centro |
| `blueworld` | `admin` | USUARIO_EMPRESA | Segundo centro |
| `sofia_buceo` | `admin` | USUARIO_COMUN | Feed, reservas, mapa |
| `pablo_oc` | `admin` | USUARIO_COMUN | Seguimiento y comentarios |
| `marinalopez` | `admin` | USUARIO_COMUN | Perfil alternativo |

## 3. Flujos principales

### Registro y login

1. Abrir `/pages/register.html`.
2. Crear usuario comun o empresa.
3. Iniciar sesion desde `/pages/login.html`.
4. El frontend guarda `token` y `user` en `localStorage`.

### Red social

1. Entrar en `/pages/feed.html`.
2. Crear una publicacion con texto, imagen o video.
3. Comentar o dar like.
4. Crear una historia desde el dock inferior.
5. Ver notificaciones desde la campana superior.

### Reserva de inmersion

1. Entrar en `/pages/Inmersiones.html`.
2. Filtrar por texto, nivel, precio o profundidad.
3. Abrir una inmersion y reservar plazas.
4. Pagar con modo demo, Stripe o PayPal segun configuracion.
5. Revisar la reserva en `/pages/reservas.html`.

### Centro de buceo

1. Entrar como `oceandive`.
2. Abrir `/pages/empresa/dashboard.html`.
3. Crear o editar inmersiones.
4. Consultar reservas recibidas.

### Administracion

1. Entrar como `admin`.
2. Abrir `/pages/admin/dashboard.html`.
3. Revisar usuarios, centros, inmersiones y reservas.

## 4. Configuracion de pagos

Si no se definen credenciales, la app mantiene modo demo para la defensa.

Variables Stripe:

```bash
STRIPE_SECRET_KEY=
STRIPE_PUBLISHABLE_KEY=
```

Variables PayPal:

```bash
PAYPAL_CLIENT_ID=
PAYPAL_CLIENT_SECRET=
PAYPAL_MODE=sandbox
```

## 5. Despliegue

### Docker

```bash
docker build -t diveconnect .
docker run --rm -p 8080:8080 --env-file .env diveconnect
```

### Render

1. Crear una instancia MySQL externa.
2. Configurar en Render las variables:
   `SPRING_DATASOURCE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.
3. Conectar el repositorio con el blueprint `render.yaml`.
4. Validar `/actuator/health`.

Ejemplo de URL JDBC:

```text
jdbc:mysql://HOST:3306/diveconnect_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

## 6. Pruebas

```bash
mvn test
```

Para empaquetar:

```bash
mvn -DskipTests package
```

## 7. Problemas frecuentes

| Sintoma | Causa probable | Solucion |
|---|---|---|
| 401 en endpoints privados | JWT ausente o caducado | Volver a iniciar sesion |
| PayPal/Stripe no aparece real | Faltan credenciales | Usar modo demo o completar `.env` |
| Error de MySQL en arranque | URL/usuario incorrectos | Revisar `SPRING_DATASOURCE_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| Upload no visible | Directorio no persistente | Montar volumen `/app/uploads` en Docker |
| OAuth Google redirige con error | Credenciales placeholder | Definir `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_OAUTH_ENABLED=true` |

## 8. Entrega al tribunal

Antes de la defensa:

1. Ejecutar `mvn test`.
2. Arrancar la app y comprobar login, feed, reserva, pago demo, mapa y admin.
3. Exportar `MEMORIA-DIVECONNECT.md` a PDF.
4. Revisar `COBERTURA-RUBRICA.md`.
5. Preparar capturas finales en `docs/screenshots/`.
