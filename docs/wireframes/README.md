# Wireframes · DiveConnect

Wireframes de las cinco pantallas principales en vista móvil (375 px). Hechos en SVG para que sean inspeccionables, escalables y diff-ables como cualquier fichero del repo.

| # | Pantalla | Fichero |
|---|---|---|
| 01 | Login + Google OAuth | [`01-login.svg`](01-login.svg) |
| 02 | Feed cronológico con stories | [`02-feed.svg`](02-feed.svg) |
| 03 | Catálogo de inmersiones con filtros | [`03-inmersiones.svg`](03-inmersiones.svg) |
| 04 | Modal de pago (bottom sheet) | [`04-pago.svg`](04-pago.svg) |
| 05 | Mapa interactivo con sidebar | [`05-mapa.svg`](05-mapa.svg) |

Estos wireframes definen la **estructura espacial** y la **jerarquía de información**. La paleta cromática y la tipografía finales están en [`../style-guide.md`](../style-guide.md).

## Decisiones reflejadas en estos wireframes

1. **Mobile First.** Todos los wireframes se diseñan en 375 px (iPhone X) y se adaptan a desktop con breakpoint principal `min-width: 720px`.
2. **Bottom dock fijo** con cinco destinos (Inicio · Buscar · Crear · Mapa · Perfil). Crear es el botón central con énfasis visual.
3. **Modales como bottom sheets** en móvil (ver wireframe 04). Surgen desde abajo con animación spring.
4. **Stories en banda horizontal** con scroll oculto (wireframe 02), inspirado en Instagram.
5. **Mapa con sidebar debajo en móvil** (wireframe 05): los puntos georreferenciados ocupan los primeros 60 vh; la lista textual va a continuación. En desktop (≥ 960 px), el mapa y la sidebar se reorganizan en columnas (1fr 380px).
6. **Hero secciones** con gradient text y eyebrow uppercase (wireframe 03).
