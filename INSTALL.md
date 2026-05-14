# Manual de instalación · DiveConnect

Tres caminos posibles, en orden de menor a mayor nivel de "producción":

1. **Local con Docker Compose** (recomendado): un solo comando levanta MySQL + app.
2. **Local sin Docker**: si prefieres tu MySQL ya instalado.
3. **Producción en Render.com**: con SSL, dominio público y deploy automático en cada push.

---

## 1. Local con Docker Compose

### Requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) o Docker Engine ≥ 20.
- 2 GB de RAM libres.
- Puertos `8080` y `3307` libres.

### Pasos

```bash
git clone https://github.com/mmordoe685/diveconnect.git
cd diveconnect

# (Opcional) Configurar variables de entorno
cp .env.example .env
# Editar .env y rellenar STRIPE_*, PAYPAL_*, etc. si quieres pasarela real

# Levantar
docker compose up --build

# La app queda en http://localhost:8080
# La BD queda en localhost:3307 (no 3306 para no chocar con un MySQL local)
```

Para detener: `Ctrl+C` y luego `docker compose down`. Los datos de MySQL se conservan en el volumen `mysql_data` y se reutilizan en el siguiente arranque. Para empezar de cero: `docker compose down -v`.

---

## 2. Local sin Docker

### Requisitos
- Java 17 o superior (`java -version`).
- MySQL 8 corriendo en `localhost:3306`.
- Maven NO necesario (el proyecto incluye `mvnw` / `mvnw.cmd`).

### Pasos

#### 2.1. Crear la base de datos
Ejecuta como `root` en MySQL:

```sql
CREATE DATABASE diveconnect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'diveconnect_user'@'localhost' IDENTIFIED BY 'DiveConnect2025!';
GRANT ALL ON diveconnect_db.* TO 'diveconnect_user'@'localhost';
FLUSH PRIVILEGES;
```

(Opcional) aplicar vistas y procedimientos almacenados — Hibernate ya crea las tablas automáticamente, así que esto solo añade los extras:

```bash
mysql -u diveconnect_user -p diveconnect_db < database/views.sql
mysql -u diveconnect_user -p diveconnect_db < database/procedures.sql
```

#### 2.2. Variables de entorno
Crea un fichero `.env` en la raíz (NO se commitea, está en `.gitignore`):

```bash
cp .env.example .env
```

Edita `.env` para meter tus claves de Stripe / PayPal / Google si las tienes. Si no las tienes, déjalas vacías y la app funcionará en modo demo TFG.

#### 2.3. Arrancar la aplicación

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

**Windows (CMD o PowerShell):**
```bat
mvnw.cmd spring-boot:run
```

La primera vez tarda 1-2 minutos descargando dependencias. Cuando veas en consola:
```
Started DiveconnectApplication in X seconds
```
Abre `http://localhost:8080` en tu navegador.

#### 2.4. Acceder desde el móvil (misma Wi-Fi)
1. En tu PC: `ipconfig` → busca tu IP LAN del adaptador Wi-Fi (algo tipo `192.168.0.111`).
2. En el móvil, mismo Wi-Fi: `http://192.168.0.111:8080/pages/login.html`.
3. Si no carga, abre el puerto 8080 en Windows Firewall (PowerShell admin):
   ```powershell
   New-NetFirewallRule -DisplayName "DiveConnect 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
   ```

---

## 3. Despliegue en Render.com

Este es el camino más cercano a producción real, con SSL gratuito y dominio público.

### Pasos

1. Forkea o sube este repositorio a tu cuenta de GitHub.
2. Crea cuenta gratuita en [render.com](https://render.com).
3. Dashboard → "New +" → "Blueprint" → conectar el repositorio.
4. Render detecta el `render.yaml` y crea el **servicio web "diveconnect"**.
5. Conecta una base MySQL externa y define en el dashboard de Render:
   `SPRING_DATASOURCE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.
6. Define las variables opcionales:
   `STRIPE_SECRET_KEY`, `PAYPAL_CLIENT_ID`, `GOOGLE_CLIENT_ID`, `OPENWEATHER_API_KEY`, etc.
7. Render construye la imagen Docker y despliega. Tarda 5-8 min la primera vez.
8. La app queda en `https://diveconnect.onrender.com` con SSL automático.

### Plan gratuito de Render
- 0,5 CPU / 512 MB RAM.
- El servicio "duerme" tras 15 min sin tráfico (cold start ~30 s la siguiente petición).
- Suficiente para una demo de TFG; en producción real se subiría al plan Starter ($7/mes).

### Auto-deploy
Cada `git push` a `master` dispara una build nueva en Render automáticamente.

---

## Usuarios de prueba (seed)

| Usuario | Contraseña | Tipo |
|---|---|---|
| `admin` | `admin` | ADMINISTRADOR |
| `superadmin` | `admin` | ADMINISTRADOR |
| `oceandive` | `admin` | EMPRESA |
| `blueworld` | `admin` | EMPRESA |
| `islanddive` | `admin` | EMPRESA |
| `sofia_buceo` | `admin` | COMUN |
| `pablo_oc` | `admin` | COMUN |
| `marinalopez` | `admin` | COMUN |
| `javier_sub` | `admin` | COMUN |
| `lucia_dive` | `admin` | COMUN |
| `buceador` | `admin` | COMUN |

`DataInitializer` crea estos usuarios sólo si no existen ya. Para regenerarlos en local: borrar la base de datos y reiniciar la app.

---

## Endpoints útiles tras arrancar

| URL | Qué es |
|---|---|
| `http://localhost:8080/` | Landing pública |
| `http://localhost:8080/pages/login.html` | Login |
| `http://localhost:8080/swagger-ui.html` | API REST interactiva (Swagger) |
| `http://localhost:8080/v3/api-docs` | Especificación OpenAPI 3.0 (JSON) |
| `http://localhost:8080/actuator/health` | Health-check público de Actuator |

---

## Resolución de problemas

### "Address already in use: bind"
Otro proceso está usando el puerto 8080. Cambia el puerto con variable de entorno o en `application.properties`:
```bash
PORT=8081
```

### "Access denied for user 'diveconnect_user'@'localhost'"
La contraseña no coincide. Verifica que la has puesto bien en MySQL y en `.env`.

### "Communication link failure"
MySQL no está corriendo. En Linux/macOS: `sudo systemctl start mysql`. En Windows: abre Servicios y arranca "MySQL80".

### El módulo de Lombok da errores en IntelliJ
Instala el plugin Lombok desde el marketplace y activa "Enable annotation processing" en Settings → Build → Compiler → Annotation Processors.

### "java: package lombok does not exist" al compilar
Maven no descargó Lombok. Borra `~/.m2/repository/org/projectlombok` y ejecuta `./mvnw clean compile`.

### Las imágenes subidas no se ven tras cerrar el servidor
Si usas Docker, el volumen `app_uploads` persiste. Si NO usas Docker, los uploads están en `./uploads/` relativo al directorio donde arrancaste la app — asegúrate de no mover esa carpeta.
