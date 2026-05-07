# Guía manual — Pasos pendientes de Marcos

Este documento es tu lista única y exhaustiva. Está ordenada por importancia y dependencias. Sigue los pasos en orden, no saltes ninguno.

Cada bloque tiene:

- **Tiempo aproximado**.
- **Para qué sirve** dentro de la rúbrica.
- **Pasos detallados** con todo lo que tienes que copiar y pegar.
- **Cómo verificar** que ha quedado bien.

---

## Bloque 0 · Antes de empezar (5 min)

Necesitas tener instalado en el portátil:

- Java 17 o superior. Compruébalo con `java -version` en CMD/PowerShell.
- MySQL 8 corriendo en `localhost:3306`. Compruébalo con `mysql -u root -p`.
- Git. Compruébalo con `git --version`.
- Una cuenta de GitHub con acceso al repo `mmordoe685/diveconnect`.

Antes de seguir, abre PowerShell **como administrador** en la raíz del proyecto y ejecuta:

```bash
cd C:\Proyecto\diveconnect
git pull origin master
```

Luego entra al worktree donde he estado trabajando:

```bash
cd C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman
git status
```

Tienes que ver "On branch claude/peaceful-hellman, nothing to commit, working tree clean". Si ves cambios pendientes, dímelo antes de seguir.

---

## Bloque 1 · Despliegue en Render.com (30 min) — IMPORTANTE

Esto cubre el módulo **Despliegue de Aplicaciones Web (4.03 %)** de la rúbrica. Sin esto, ese módulo te puntúa 0.

### 1.1. Crear cuenta en Render

1. Abre https://render.com.
2. "Get Started" — sign up con tu cuenta de GitHub (la misma de mmordoe685).
3. Confirma el email si te lo pide.
4. Cuando pregunte el plan, elige **Free** ($0/mes).

### 1.2. Conectar el repositorio

1. Dashboard de Render → "New +" arriba a la derecha → "Blueprint".
2. Conecta tu cuenta de GitHub (autoriza la app de Render).
3. Selecciona el repositorio `mmordoe685/diveconnect`.
4. Render detecta automáticamente el archivo `render.yaml` que está en la raíz y te muestra dos servicios:
   - `diveconnect` (web service)
   - `diveconnect-db` (base de datos MySQL)
5. Pulsa **"Apply"**.

### 1.3. Definir variables de entorno opcionales

Render ya rellena las variables `SPRING_DATASOURCE_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` automáticamente. Tú tienes que rellenar el resto solo si tienes credenciales reales:

1. Entra al servicio `diveconnect` en el dashboard.
2. Tab "Environment".
3. Pulsa "Add Environment Variable" para cada una:

| Key | Value | Cómo conseguirla |
|---|---|---|
| `STRIPE_SECRET_KEY` | `sk_test_...` | https://dashboard.stripe.com/test/apikeys |
| `STRIPE_PUBLISHABLE_KEY` | `pk_test_...` | mismo sitio |
| `PAYPAL_CLIENT_ID` | tu client id sandbox | https://developer.paypal.com/dashboard/applications/sandbox |
| `PAYPAL_CLIENT_SECRET` | tu client secret sandbox | mismo sitio |
| `PAYPAL_MODE` | `sandbox` | (literal) |
| `OPENWEATHER_API_KEY` | tu API key gratuita | https://openweathermap.org/api |

Si no quieres registrarte en esos servicios, **déjalas vacías**. La aplicación funcionará en modo demo TFG y todo seguirá viéndose bien.

### 1.4. Esperar al primer despliegue

1. Tab "Events" del servicio. Verás "Deploy started for ...".
2. La primera build tarda **5-8 minutos** (descarga Maven + dependencies + build de imagen Docker).
3. Cuando termine, en la parte superior aparecerá la URL pública: `https://diveconnect-XXXX.onrender.com`.
4. Apunta esa URL — la vas a usar en los siguientes bloques.

### 1.5. Verificar que funciona

Abre la URL en tu navegador. Tienes que ver la landing pública con el logo. Pruebas mínimas:

1. `https://diveconnect-XXXX.onrender.com` — landing.
2. `https://diveconnect-XXXX.onrender.com/pages/login.html` — login.
3. Hacer login con `sofia_buceo` / `admin`.
4. Ir al feed.
5. Abrir `https://diveconnect-XXXX.onrender.com/swagger-ui.html` para confirmar que la API funciona.

> Nota: el plan free de Render duerme tras 15 min sin tráfico. La primera petición tras el sueño tarda 30 segundos. Para la defensa, abre la URL 1 minuto antes para "despertarla".

### 1.6. Cambiar el FRONTEND_URL

Una vez sepas la URL pública:

1. Tab "Environment" del servicio `diveconnect`.
2. Edita `FRONTEND_URL` y pon `https://diveconnect-XXXX.onrender.com` (con tu URL real).
3. "Save Changes". Render redeploya automáticamente.

Esto sirve para que cuando Stripe haga el redirect después de pagar, vuelva al dominio público y no a `localhost:8080`.

---

## Bloque 2 · GitHub Issues + Project (15 min)

Esto cubre el criterio **"Gestión de Incidencias y Robustez"** del módulo PIDAWE y **"Herramientas de Gestión y Desarrollo"** (uso de GitHub Projects).

### 2.1. Activar Issues en el repositorio

1. https://github.com/mmordoe685/diveconnect/settings.
2. Sección "Features".
3. Marca la casilla **"Issues"** si no está marcada.

### 2.2. Crear las 12 issues retrospectivas

Abre el archivo `ISSUES.md` del repositorio en otra pestaña del navegador. Cada bloque numerado (`## #1`, `## #2`, etc.) es una issue.

Para cada una:

1. https://github.com/mmordoe685/diveconnect/issues → "New issue".
2. **Title**: la línea inmediatamente después de `##`. Por ejemplo `closed: Lombok @Data + Set<Usuario> rompe equals/hashCode en JPA`.
3. **Comment / Body**: el contenido completo desde "Etiquetas:" hasta antes del siguiente `## #`.
4. **Labels** (panel derecho): crea las que aparecen tras "Etiquetas:" — `bug`, `backend`, `crítico`, `enhancement`, etc. Para crear una nueva etiqueta: "Labels" → "New label".
5. Pulsa "Submit new issue".
6. Para las que tienen `closed:` en el título: una vez creada la issue, pulsa "Close issue" arriba a la derecha y selecciona "Close as completed".

Total a crear: **12 issues** (5 cerradas con causa "completed", 7 abiertas).

### 2.3. Crear un GitHub Project

1. https://github.com/mmordoe685?tab=projects → "New project".
2. Plantilla: "Board".
3. Nombre: `DiveConnect Roadmap`.
4. "Create project".
5. En el board verás tres columnas (Todo / In Progress / Done).
6. "Add item" en cada columna y enlaza las issues que has creado:
   - **Done**: las 5 cerradas (#1, #2, #3, #4, #5).
   - **Todo**: las 7 abiertas (#6 a #12).
7. Renombra "Todo" a "Backlog" y "In Progress" a "Sprint actual" si quieres.

### 2.4. Verificación

- En `https://github.com/mmordoe685/diveconnect/issues` deben aparecer 12 issues.
- 5 cerradas con tag `closed`. 7 abiertas con tag `open`.
- En `https://github.com/mmordoe685?tab=projects` debe aparecer el board con todas las issues.

---

## Bloque 3 · Capturas de pantalla (20 min)

El README hace referencia a capturas en `docs/screenshots/`. Si no existen, las tablas del README muestran imágenes rotas.

### 3.1. Levantar la app en local

```bash
cd C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman
mvnw.cmd spring-boot:run
```

Espera a "Started DiveconnectApplication". Abre Chrome.

### 3.2. Hacer las 6 capturas

Para cada una: presiona `Ctrl + Shift + I` (DevTools) → toggle device toolbar (`Ctrl + Shift + M`) → selecciona "iPhone 12 Pro" en el desplegable. Luego `Ctrl + Shift + P` → escribe `screenshot` → "Capture screenshot".

| Archivo de salida | Cómo hacerla |
|---|---|
| `docs/screenshots/login.png` | URL: `/pages/login.html`. Sin estar logueado. |
| `docs/screenshots/feed-mobile.png` | Login con sofia_buceo / admin → te lleva a `/pages/feed.html`. |
| `docs/screenshots/pay-modal.png` | `/pages/Inmersiones.html` → "Reservar" en cualquier card → "Confirmar" → screenshot del modal de pago. |
| `docs/screenshots/inmersiones.png` | `/pages/Inmersiones.html` con catálogo cargado. |
| `docs/screenshots/mapa.png` | `/pages/mapa.html` con marcadores cargados. |
| `docs/screenshots/perfil.png` | `/pages/Perfil.html` mostrando datos de sofia. |

Mueve los PNG descargados a `C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman\docs\screenshots\`.

### 3.3. Verificar

Tras las 6 capturas, ejecuta:

```bash
ls docs/screenshots/
```

Debes ver los 6 archivos `.png`.

### 3.4. Commit de las capturas

```bash
cd C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman
git add docs/screenshots/
git commit -m "Capturas de pantalla para el README"
git push origin claude/peaceful-hellman
```

---

## Bloque 4 · Auditoría Lighthouse real (10 min)

Esto cubre los criterios de **Accesibilidad** y **SEO** del módulo DIW. Lo importante es la **captura de la puntuación**, no automatizarlo.

### 4.1. Abrir tu app desplegada en Chrome

URL desde el bloque 1.4: `https://diveconnect-XXXX.onrender.com`.

### 4.2. Generar el report móvil

1. F12 (DevTools).
2. Tab "Lighthouse".
3. Marca: Performance, Accessibility, Best Practices, SEO. Desmarca PWA.
4. Device: **Mobile**.
5. "Analyze page load".
6. Espera ~30 segundos.
7. Cuando aparezcan las puntuaciones, descarga el reporte: icono de los tres puntos arriba a la derecha del panel Lighthouse → "Save as HTML".
8. Guarda como `docs/screenshots/lighthouse-mobile.html`.

### 4.3. Generar el report desktop

Repite igual pero seleccionando **Desktop** en lugar de Mobile. Guarda como `docs/screenshots/lighthouse-desktop.html`.

### 4.4. Captura de las puntuaciones

Cuando tengas las dos puntuaciones, haz screenshot del panel Lighthouse (los 4 círculos con números). Guárdala como:

- `docs/screenshots/lighthouse-mobile.png`
- `docs/screenshots/lighthouse-desktop.png`

### 4.5. Commit

```bash
cd C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman
git add docs/screenshots/lighthouse-*
git commit -m "Reportes Lighthouse de la web desplegada"
git push origin claude/peaceful-hellman
```

---

## Bloque 5 · Ejecutar el script Python (15 min)

Esto cubre el módulo **Programación en Python y Análisis de Datos (6.03 %)**. Sin esto, ese módulo te puntúa 0.

### 5.1. Instalar Python si no lo tienes

1. https://www.python.org/downloads/ → "Download Python 3.12".
2. Ejecuta el instalador. **Marca "Add python.exe to PATH"** antes de instalar.
3. "Install Now".
4. En PowerShell nuevo: `python --version`. Debe responder "Python 3.12.x".

### 5.2. Crear entorno virtual e instalar dependencias

```bash
cd C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman\scripts\analytics
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

Esto tarda 1-2 minutos descargando pandas y matplotlib. Cuando termine, deberías ver `(.venv)` al principio de tu prompt.

### 5.3. Ejecutar el análisis

```bash
python analytics.py
```

Si MySQL está corriendo en local con los datos seed, verás algo como:

```
[+] Conectando a localhost:3306/diveconnect_db
[+] Métricas globales
    usuarios_activos        = 11
    centros_registrados     = 3
    inmersiones_activas     = 12
    publicaciones           = 14
    total_reservas          = 27
    reservas_pagadas        = 5
    ingresos_pagados        = 290.0
    profundidad_media       = 15.4
[+] Publicaciones por mes: N filas
[+] Reservas por estado: 4 estados
[+] Top inmersiones: 5 filas
[+] Especies top: N filas
[OK] Gráficas guardadas en docs/screenshots/analytics
[OK] CSV consolidado en scripts/analytics/dashboard.csv
```

### 5.4. Verificar las gráficas

Abre estas tres imágenes con tu visor de imágenes:

```bash
docs\screenshots\analytics\01-publicaciones-mes.png
docs\screenshots\analytics\02-reservas-estado.png
docs\screenshots\analytics\03-top-inmersiones.png
```

Tienes que ver gráficas de barras, un pie chart y barras horizontales con la paleta corporativa (turquoise, coral, gold). Si las ves, perfecto.

### 5.5. Commit de los outputs

```bash
cd C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman
git add docs/screenshots/analytics scripts/analytics/*.csv
git commit -m "Output del análisis Python: gráficas y CSV"
git push origin claude/peaceful-hellman
```

---

## Bloque 6 · Imagen Open Graph (10 min, opcional)

Esto mejora la presentación cuando se comparte el link en redes sociales. **No es crítico** pero suma puntos en SEO.

1. https://canva.com → "Crear diseño" → tamaño 1200x630.
2. Diseño rápido:
   - Fondo: gradiente de `#0B1A2B` (navy) a `#00D4AA` (teal).
   - Texto principal: "DiveConnect", 90pt, blanco, en serif (Georgia o Playfair).
   - Subtítulo: "Red social y reservas de buceo", 28pt, color `#BEE9E8`.
3. Descarga como PNG.
4. Guárdalo como `C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman\src\main\resources\static\images\og-cover.png`.

```bash
git add src/main/resources/static/images/og-cover.png
git commit -m "Imagen Open Graph para previsualización en redes"
git push origin claude/peaceful-hellman
```

---

## Bloque 7 · Antes de la defensa (la noche anterior)

### 7.1. Despertar el servicio en Render

1. Abre tu URL de Render una vez para que el servidor se despierte.
2. Verifica que la página se carga.
3. Login con `sofia_buceo` / `admin`. Verifica que entras al feed.
4. Abre Inmersiones → "Reservar" → confirma → "Pagar". Que llegue al tick verde.
5. Abre `/swagger-ui.html`. Verifica que carga.

### 7.2. Tener el portátil preparado

Ten estas pestañas abiertas en Chrome:

1. https://github.com/mmordoe685/diveconnect (repo).
2. https://github.com/mmordoe685/diveconnect/issues (Issues).
3. https://github.com/mmordoe685?tab=projects (Project board).
4. https://render.com/dashboard (Render — para enseñar el despliegue).
5. Tu URL pública de Render (la app desplegada).
6. La app local en http://localhost:8080 (también arrancada con `mvnw.cmd spring-boot:run` por si Render falla).
7. Swagger UI local en http://localhost:8080/swagger-ui.html.

Y el `DiveConnect-Defensa.pptx` abierto en PowerPoint o Keynote.

### 7.3. Tener listo el plan B

Si Render se cae durante la defensa, ten un terminal abierto con:

```bash
cd C:\Proyecto\diveconnect\.claude\worktrees\peaceful-hellman
mvnw.cmd spring-boot:run
```

Si te preguntan cómo desplegar y Render no responde, abre el `render.yaml` en VS Code y enséñalo, demostrando que el código está listo aunque el servicio gratuito esté caído.

### 7.4. Repaso del proyecto

Lee de cabo a rabo:

1. El **README.md**.
2. El archivo **APUNTES-PROYECTO.md** que está en la raíz del repo.
3. Las **5 issues cerradas** en GitHub para acordarte de los bugs y soluciones.
4. La **memoria PDF** (`docs/DiveConnect-Documentacion-Tecnica.pdf`) — al menos los capítulos 17 y 18 (decisiones técnicas).

---

## Bloque 8 · Presentación final ante tribunal (40 min)

El día de la defensa, sigue el guion del archivo `APUNTES-PROYECTO.md` apartado **3. Guion para el tribunal**.

Estructura recomendada:

1. **2 min** — Presentación + qué es DiveConnect.
2. **3 min** — Demo en vivo: login → feed → reservar → pagar → notificación.
3. **3 min** — Mapa, búsqueda con proximidad, perfil con foto subida.
4. **3 min** — Arquitectura por capas (enseñar el diagrama del PDF).
5. **3 min** — Modelo de datos (E/R + normalización).
6. **3 min** — Pasarela de pago (los 3 modos: demo / sandbox / live).
7. **2 min** — Seguridad (JWT + OAuth2 + BCrypt).
8. **2 min** — Tests + CI/CD (enseñar GitHub Actions).
9. **2 min** — Despliegue (Render + Docker + render.yaml).
10. **2 min** — Análisis Python (enseñar las gráficas generadas).
11. **3 min** — Decisiones técnicas notables (Lombok @Data, race en Inmersiones, modos de pago).
12. **2 min** — Roadmap.
13. **10 min** — Preguntas del tribunal.

---

## Bloque 9 · Tras la defensa

Independientemente del resultado:

1. Cuando te confirmen la nota, anótala en el `CHANGELOG.md` (commit + push).
2. Si te aprueban con buena nota, etiqueta el commit con `git tag v1.0.0 && git push --tags`.
3. Cambia el repositorio a privado si no quieres que esté público (no lo recomiendo, mejor que esté de portfolio).

---

## Resumen del trabajo manual a hacer

| Bloque | Tiempo | Crítico |
|---|---|---|
| 0. Comprobaciones previas | 5 min | Sí |
| 1. Despliegue Render | 30 min | **Crítico — módulo Despliegue 4 %** |
| 2. GitHub Issues + Project | 15 min | **Crítico — módulo PIDAWE** |
| 3. Capturas de pantalla | 20 min | Sí (README) |
| 4. Lighthouse | 10 min | Sí (DIW + LMSGI) |
| 5. Python ETL | 15 min | **Crítico — módulo Python 6 %** |
| 6. OG image | 10 min | No |
| 7. Noche anterior | 30 min | Sí |
| 8. Defensa | 40 min | (la defensa misma) |

Total trabajo manual: **~2 horas**, repartidas en los días previos.
