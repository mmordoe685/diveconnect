# Guía de estilo · DiveConnect

Documento de referencia para el sistema visual de DiveConnect. Cualquier nueva pantalla o componente debe respetar estos principios de paleta, tipografía, espaciado y micro-interacción.

---

## 1. Paleta cromática

La paleta combina un fondo cálido (cream warm) con un acento aqua que evoca el mar superficial. Se complementa con un azul navy profundo, un coral para acciones críticas y un dorado suave para avisos.

### Tokens base

| Token CSS | Valor | Uso |
|---|---|---|
| `--bg` | `#FFFBF5` | Fondo principal cream |
| `--bg-raise` | `#FFFFFF` | Cards, modales, paneles elevados |
| `--alt` | `#F5F0EA` | Hover sutil, áreas secundarias |
| `--navy` | `#0B1A2B` | Tipografía principal, fondo dark |
| `--navy-mid` | `#132F4C` | Tipografía secundaria, headings |
| `--seafoam` | `#00D4AA` | Color primario / acento (aqua) |
| `--coral` | `#FF6B6B` | Errores, acciones destructivas |
| `--gold` | `#F5A623` | Avisos, badges TFG/Demo |
| `--indigo` | `#5B5EA6` | Acentos secundarios fríos |
| `--sky` | `#3B82F6` | Estados informativos |
| `--t1` | `#1A1A2E` | Texto principal |
| `--t2` | `#4A5568` | Texto secundario |
| `--t3` | `#A0AEC0` | Texto muted, placeholders |
| `--line` | `#E8E3DC` | Bordes neutros |
| `--line-h` | `#D4CFC7` | Bordes hover |

### Capa "ocean theme" (overlay)

Sobre la paleta base, la temática submarina añade:

| Token | Valor | Uso |
|---|---|---|
| `--ocean-deep` | `#0B1A2B` | Fondo más profundo |
| `--ocean-shallow` | `#1B4965` | Azul de agua media |
| `--ocean-surface` | `#62B6CB` | Azul de superficie |
| `--ocean-light` | `#BEE9E8` | Spume, espuma |
| `--ocean-foam` | `#DCF3EE` | Aqua muy claro, fondo |
| `--ocean-glow` | `#00D4AA` | Acentos glow |
| `--glass-bg` | `rgba(255,255,255,.58)` | Glassmorphism cards |
| `--glass-border` | `rgba(255,255,255,.42)` | Borde glass |

### Combinaciones aprobadas

```
Primario · Botón "Pagar"     →  --seafoam sobre --bg-raise
Destructivo · Cancelar       →  --coral sobre --bg-raise
Aviso · Demo / Sandbox       →  --gold sobre --alt
Info · Notificaciones        →  --sky sobre --bg-raise
Heading · "Mis Reservas"     →  Gradient --navy → --navy-mid → --seafoam
```

Contraste mínimo: WCAG AA (4.5:1 para texto, 3:1 para componentes UI no-texto). Verificado para `--t1` sobre `--bg` (≈ 14:1) y `--seafoam` sobre `--bg` (≈ 3.6:1, sólo apto para iconos y elementos UI).

---

## 2. Tipografía

| Familia | Uso | Pesos disponibles |
|---|---|---|
| `'DM Serif Display'` | Headings hero (`h1`, `h2`), saludos, totales en serif | 400, 400 italic |
| `'Nunito Sans'` | Cuerpo, UI, navegación, formularios | 400, 500, 600, 700, 800 |
| `'Poppins'` | Stats numéricos grandes (mostrado en stats-bar) | 800 |
| `Courier` | Coordenadas geográficas, código, IDs UUID | 400 |

### Escala tipográfica (rem)

```
hero h1       2.5  – clamp(1.75, 3.5vw, 2.6)
section h2    1.4  – 1.6
card title    1.05
body          0.95
small         0.82
caption       0.72
mono          0.85 (siempre Courier)
```

### Reglas

- Las **headings** principales aplican gradient text con shimmer animado de 8 s (`heading-shimmer` keyframe).
- El **cuerpo** usa `Nunito Sans` con `line-height: 1.7` y `letter-spacing: 0`.
- Las **cifras** (totales, stats) usan `Poppins` o `DM Serif Display`, no Nunito.
- Los **chips** y **badges** son siempre Nunito 700 con `letter-spacing: .04em`.

---

## 3. Espaciado y radios

### Espaciado (basado en escala 4px / 0.25rem)

```
4    8    12   16   20   24   32   40   48   64   96
0.25 0.5  0.75 1    1.25 1.5  2    2.5  3    4    6    rem
```

Las superficies (cards, modales) usan `padding: 1.25rem` o `1.75rem` en desktop, `0.85-1rem` en móvil.

### Radios

| Token | Valor | Uso |
|---|---|---|
| `--r-xs` | 4px | Inputs pequeños, badges |
| `--r-sm` | 6px | Iconos cuadrados |
| `--r-md` | 12px | Botones, chips |
| `--r-lg` | 16px | Cards |
| `--r-xl` | 24px | Modales, secciones grandes |
| `--r-2xl` | 32px | Heroes, paneles especiales |
| `--r-full` | 9999px | Píldoras, avatares |

---

## 4. Sombras y elevación

| Token | Valor (resumido) | Cuándo |
|---|---|---|
| `--sh-card` | `0 2px 8px rgba(...,.04)` | Estado base cards |
| `--sh-card-hover` | `0 16px 40px rgba(...,.1)` | Hover, focus |
| `--sh-glow` | `0 4px 24px rgba(0,212,170,.14)` | Botón primario |
| `--sh-glow-strong` | `0 8px 36px rgba(0,212,170,.22)` | Botón primario activo |
| `--glass-shadow` | `0 8px 32px + inner glow` | Glassmorphism |

---

## 5. Componentes

### Botones

```
.btn-primary    → linear-gradient(135deg, #00D4AA, #2EC4B6, #1B9AAA)
                  + shimmer sweep + spring-scale on active
.btn-secondary  → Bg cream + border line
.btn-ghost      → Texto sólo, hover muestra fondo alt
.btn-danger     → Bg coral, hover oscurece
```

### Inputs

```
border: 1.5px solid --line
focus: border + 3-layer aqua glow
border-radius: --r-md
min-height: 44px (mobile-friendly)
font-size: 16px (evita zoom iOS)
```

### Cards

Glass por defecto en páginas con `ocean-theme.css`:
```css
background: var(--glass-bg);
backdrop-filter: blur(18px) saturate(160%);
border: 1px solid var(--glass-border);
box-shadow: var(--glass-shadow);
```

### Modales

- Desktop: centrados, max-width 440px (pago) / 600px (otros), `--r-xl`.
- Móvil (< 720px): bottom-sheet con borde superior `--r-xl`, animación spring desde abajo.

---

## 6. Micro-animaciones

Transiciones por defecto: `.2s var(--ease)` para interacciones rápidas, `.4s var(--ease-spring)` para feedback positivo (botones, cards).

| Animación | Duración | Easing | Uso |
|---|---|---|---|
| `heading-shimmer` | 8 s | ease-in-out | Heading con gradient |
| `ocean-rays` | 14 s | ease-in-out | Rayos de luz fondo |
| `ocean-caustic` | 22 s | ease-in-out | Caustics en fondo |
| `dock-pulse` | 2.4 s | ease-in-out | Halo del dock activo |
| `heart-pop` | .42 s | spring | Like en publicación |
| `ocean-modal-in` | .45 s | spring | Entrada modal |
| `ocean-sheet-up` | .42 s | spring | Bottom sheet móvil |
| `ocean-rise` | 12-26 s | linear | Burbujas ascendentes |

Easings:
- `--ease` = `cubic-bezier(.4, 0, .2, 1)` (Material default)
- `--ease-spring` = `cubic-bezier(.22, 1.2, .36, 1)` (overshoot suave)
- `--ease-out` = `cubic-bezier(0, .55, .45, 1)`

`prefers-reduced-motion` desactiva todas las animaciones largas y los efectos de fondo.

---

## 7. Iconografía

- **Sin emojis decorativos.** Decisión consciente: los emojis dependen de la fuente del sistema operativo y rompen el contraste profesional. Toda la iconografía es SVG inline.
- Trazo: `stroke-width: 2` para iconos UI (16-24 px), `stroke-width: 1.5` para iconos grandes (32+ px).
- Color: `currentColor` para que los iconos hereden el color del contenedor.
- Línea: `stroke-linecap: round` y `stroke-linejoin: round` siempre.

Conjuntos usados (todos en SVG inline, no librería externa):

```
Pin (ubicación)       · Profundidad (flecha hacia abajo)  · Termómetro
Ojo (visibilidad)     · Pez (especies)                     · Tarjeta de crédito
Candado (pago seguro) · Lápiz (editar)                     · Trash (eliminar)
Cámara (perfil)       · Galería (publicar)                 · Buceador
Sol/Luna/Nube/Lluvia  (clima en mapa)
```

---

## 8. Responsive y breakpoints

```
xs   :  0    px   (móvil portrait)
sm   :  380  px   (móvil grande)
md   :  720  px   (tablet portrait, breakpoint principal)
lg   :  960  px   (tablet landscape)
xl   :  1200 px   (desktop)
xxl  :  1440 px   (desktop ancho)
```

**Mobile First:** las hojas de estilo asumen móvil por defecto; los `@media (min-width)` añaden sobre el layout móvil.

Reglas específicas en `< 720px`:
- Modales como bottom sheets.
- Grids colapsan a 1 columna.
- Inputs altura 44px y font-size 16px.
- Filter tabs con scroll horizontal.
- `padding-bottom: 76px` en `<body>` para que el dock fixed no tape contenido.
- Safe-area-inset para iPhones con notch.

Reglas en `< 380px`:
- Reducción extra de fuente en stats y dock.

---

## 9. Accesibilidad (WCAG 2.1 AA)

Compromisos del proyecto:
- Cada `<button>` con `aria-label` cuando solo contiene un icono.
- Inputs con `<label>` o `aria-labelledby` asociado.
- Contraste mínimo 4.5:1 en texto.
- `<html lang="es">` en todas las páginas.
- Estructura semántica: `header > main > footer`, `nav`, `section`, `article`.
- Focus visible mediante `:focus-visible` con outline aqua de 3px.
- `prefers-reduced-motion` respetado.
- Tablas y formularios accesibles a lectores de pantalla.

---

## 10. Voz y tono

- **Cercano pero técnico.** El público es buceador: usa terminología real (Open Water, fondeo, posidonia) sin condescendencia.
- **Honesto.** Los avisos como "TFG · Demo" o "Sandbox" se muestran sin disfraces — el usuario debe saber siempre en qué entorno está pagando.
- **Conciso.** Los CTA son verbos cortos: "Pagar", "Reservar", "Confirmar". Los placeholders dan ejemplos concretos: "Islas Medas, Cabo de Creus".
- **Sin emojis decorativos en copy.**

---

## 11. Dark mode

Dark mode automático no se aplica. Razón: la paleta base es cream warm intencionada para evocar luz cálida sobre fondo marino. Un dark mode mecánico rompería el contraste con el ocean-theme. Si en el futuro se añadiese, sería como switch manual, no `prefers-color-scheme`, y mantendría la paleta aqua/teal cambiando solo `--bg`, `--bg-raise` y los tokens `--t*`.
