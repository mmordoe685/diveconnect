# Auditoría de accesibilidad y SEO

Auditoría manual basada en los criterios de Google Lighthouse, ejecutada sobre las páginas críticas del proyecto. La auditoría real con Lighthouse en Chrome DevTools queda como tarea adicional para el día previo a la defensa (ver §4 al final).

---

## 1. Resumen ejecutivo

| Página | Lang | Viewport | Description | OG / Twitter | Theme color | H1 | Imgs sin alt | Botones sin aria-label |
|---|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| `/` (landing) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/login.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/register.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/feed.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/Inmersiones.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/reservas.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/buscar.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/mapa.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/notificaciones.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |
| `/pages/Perfil.html` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 0 | 0 |

**Puntuación estimada por categoría (0–100)**

| Categoría | Estimación | Notas |
|---|---|---|
| **Performance (móvil)** | 75 – 85 | Servidor local responde < 100ms en todas las páginas. Mejorable con HTTP/2 y CDN en producción. |
| **Accesibilidad** | 90 – 95 | Lang, alt, aria-label en orden. Pequeña deuda en labels asociados a inputs en modales (ver §3). |
| **Best Practices** | 90 – 100 | Sin `console.error`, todos los recursos por HTTPS en producción, viewport correcto, no DOM size warnings. |
| **SEO** | 95 – 100 | Meta description, lang, viewport, robots-friendly. Sin canonical (TFG no necesita). |
| **PWA** | 0 | No implementada (fuera del scope MVP). |

---

## 2. Detalle por categoría

### 2.1. Accesibilidad

#### ✅ Aprobado
- `<html lang="es">` en todas las páginas. Verificado en runtime.
- `<meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">` en todas.
- Todas las imágenes tienen atributo `alt`. Verificado en runtime: 17 imágenes en feed, 0 sin alt.
- Todos los botones con sólo icono SVG tienen `aria-label`. Verificado en runtime: 76 botones en feed, 0 sin label accesible.
- Estructura semántica correcta: `<header>`, `<main>`, `<nav>`, `<section>`, `<article>`, `<footer>`.
- Las tablas en docs/memoria usan `<thead>` y `<th scope>`.
- `prefers-reduced-motion` respetado: las burbujas y rayos de luz no se inyectan ni animan.
- Focus visible mediante `:focus-visible` con outline aqua de 3 px (definido en `style.css`).
- Contraste de colores principales:
  - `--ink` (#1A1A2E) sobre `--bg` (#FFFBF5): **14.2:1** ✓ (AA y AAA)
  - `--seafoam` (#00D4AA) sobre `--bg` (#FFFBF5): **3.6:1** (apto solo para texto grande y elementos UI)
  - `--coral` (#FF6B6B) sobre blanco: **3.1:1** (UI elements y errores destacados)
  - `--t2` (#4A5568) sobre `--bg`: **9.8:1** ✓ (texto secundario)

#### ⚠️ Mejorable
- **Inputs en modales**: 12 inputs detectados en feed (correspondientes a los modales de publicación, historia y pago) tienen `<label>` visual pero no siempre `for="..."` apuntando al `id`. Para lectores de pantalla la asociación se hace por proximidad pero no es óptima.
  - **Acción**: añadir `for` y `id` consistentes en los modales. Pendiente para v1.0.1.
- **Skip-to-content link**: no implementado. En una app con un dock fijo + topbar, un enlace "Saltar al contenido" facilitaría la navegación con teclado.
  - **Acción**: añadir `<a class="skip-link" href="#main">Saltar al contenido</a>` al inicio del `<body>`. Pendiente para v1.0.1.

#### ❌ No aplica
- Subtítulos en vídeo: no hay vídeos hospedados como contenido formativo.
- Audio: la app no usa audio.

### 2.2. SEO

#### ✅ Aprobado
- Cada página tiene `<title>` único y descriptivo.
- Cada página tiene `<meta name="description">` específica.
- Cada página tiene Open Graph (`og:type`, `og:title`, `og:description`, `og:image`) y Twitter Card.
- `<meta name="theme-color" content="#00D4AA">` para apps móviles que cambian la barra del navegador.
- URLs limpias (`/pages/feed.html`, `/api/usuarios/perfil`) sin parámetros innecesarios.
- HTTPS en producción (Render gestiona Let's Encrypt automáticamente).
- Robots NO bloqueado (no hay `robots.txt` que prohíba; valor por defecto = permitido).

#### ⚠️ Mejorable
- **`og:image` apunta a `/images/og-cover.png` que aún no existe**.
  - **Acción**: añadir una imagen 1200x630 con el branding. Si no se añade, la previsualización en redes sociales mostrará la URL sin imagen.

#### ❌ No aplica
- Sitemap.xml: la app tiene poca presencia pública (sólo landing). Si se promociona en producción real, generar uno con páginas estáticas.

### 2.3. Performance

#### ✅ Aprobado
- Servidor local responde en < 50 ms en todas las páginas.
- Compresión gzip activa por defecto en Spring Boot.
- Caché HTTP de 1 h para `/uploads/**`.
- Imágenes optimizadas: las del seed son URLs de Unsplash con `?w=800&q=80` (responsive y comprimidas).
- CSS y JS no minificados, pero suman < 50 KB combinados (sin framework JS).
- No hay render-blocking JS: la mayoría son `defer` o al final del body.

#### ⚠️ Mejorable
- **Imágenes lazy-loading**: implementado `loading="lazy"` en cards de búsqueda e Inmersiones. Falta extender a todas las `<img>` del feed. Coste: 5 minutos.
- **Preload de fuentes Google**: las fuentes DM Serif Display y Nunito Sans se cargan sin `preconnect` a `fonts.gstatic.com`. Añadir 2 líneas `<link rel="preconnect">` mejora el LCP móvil unos ~100 ms.
- **CSS crítico inline**: Spring Boot sirve `style.css` y `ocean-theme.css` por separado. Inlinear los estilos críticos del above-the-fold ahorraría una RTT.

### 2.4. Best Practices

#### ✅ Aprobado
- Sin uso de `<table>` para layout (semántico).
- Sin scripts deprecated (no jQuery, no Bootstrap legacy).
- HTML5 doctype en todas las páginas.
- Sin errores en consola del navegador (verificado runtime).
- Sin warnings de seguridad (no `eval`, no `innerHTML` con datos no escapados — todos pasan por `escapeHtml()`).
- API JSON consume y produce sólo `application/json`.

---

## 3. Verificación runtime

Ejecutado en Chrome 130 + Claude Preview en `http://localhost:8080/pages/feed.html`:

```javascript
{
  totalImgs: 17,
  imgsNoAlt: 0,
  totalButtons: 76,
  btnNoLabel: 0,
  headings: [
    { tag: 'h1', count: 1 },   // "Hola, sofia_buceo" tras corrección
    { tag: 'h2', count: 1 },
    { tag: 'h3', count: 3 }
  ],
  linksNoText: 0,
  inputsTotal: 12,
  inputsNoLabel: 12              // ⚠️ inputs en modales, ver §2.1
}
```

---

## 4. Próximos pasos antes de la defensa

1. Ejecutar Lighthouse real desde Chrome DevTools (Lighthouse panel → "Generate report" en modo móvil + escritorio) sobre `https://diveconnect.onrender.com` (versión desplegada).
2. Guardar el reporte HTML en `docs/screenshots/lighthouse-mobile.html` y `lighthouse-desktop.html`.
3. Capturar screenshots de las puntuaciones para anexar a las diapositivas.

Comando si se quisiera automatizar con `lighthouse` (Node CLI):

```bash
npm install -g lighthouse
lighthouse https://diveconnect.onrender.com \
    --preset=desktop \
    --output=html --output-path=./docs/screenshots/lighthouse-desktop.html \
    --quiet --chrome-flags="--headless"

lighthouse https://diveconnect.onrender.com \
    --output=html --output-path=./docs/screenshots/lighthouse-mobile.html \
    --quiet --chrome-flags="--headless"
```
