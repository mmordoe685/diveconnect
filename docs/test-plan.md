# Plan de pruebas · DiveConnect

Este documento describe la estrategia de testing del proyecto, los casos cubiertos por tests automatizados (JUnit) y los casos que se validan manualmente cada release.

---

## 1. Estrategia general

| Nivel | Herramienta | Cobertura objetivo |
|---|---|---|
| Unitarios | JUnit 5 + Mockito + AssertJ | Servicios críticos, utilidades, validaciones |
| Integración (futuro) | Spring Boot Test + Testcontainers | Flujo completo controller → BD |
| End-to-end (futuro) | Playwright | Flujos clave en navegador real |
| Manual | Walkthrough cronometrado | Cada release, ver §3 |

Cobertura actual: **31 tests automáticos** sobre 5 clases (`ReservaService`, `PayPalService`, `StripeService`, `UploadController`, `EmojiStripMigration`). Suficiente para los caminos felices y los errores controlados; los tests de integración con BD real quedan como deuda en `ISSUES.md #9`.

---

## 2. Tests automatizados (JUnit)

### 2.1. ReservaServiceTest (5 tests)
| Caso | Verifica |
|---|---|
| `crearReserva_caminoFeliz` | Creación correcta + descuento de plazas + estado PENDIENTE/UNPAID |
| `crearReserva_sinPlazas` | Lanza `BadRequestException` y NO toca BD |
| `crearReserva_inmersionNoExiste` | Lanza `ResourceNotFoundException` |
| `crearReserva_usuarioNoExiste` | Lanza `ResourceNotFoundException` antes de cargar inmersión |
| `marcarComoPagada_actualizaCorrectamente` | Cambia estado a CONFIRMADA + payment_status PAID |

### 2.2. PayPalServiceTest (8 tests)
| Caso | Verifica |
|---|---|
| `noConfigurado_clientIdVacio` | `isConfigured() = false` con clientId vacío |
| `noConfigurado_secretVacio` | `isConfigured() = false` con secret vacío |
| `configurado_cuandoAmbosDefinidos` | `isConfigured() = true` |
| `fetchAccessToken_sinConfigurar_lanzaExcepcion` | Lanza `PayPalException` informativa |
| `getBaseUrl_sandbox` | URL correcta en modo sandbox |
| `getBaseUrl_live` | URL correcta en modo live |
| `getBaseUrl_modoDesconocido_usaSandbox` | Fallback a sandbox |
| `getCurrency_devuelveLaConfigurada` | Currency configurable |

### 2.3. StripeServiceTest (5 tests)
| Caso | Verifica |
|---|---|
| `isEnabled_secretVacio` | `false` con secret vacío |
| `isEnabled_secretNull` | `false` con null (defensa adicional) |
| `isEnabled_secretDefinida` | `true` con secret válida |
| `getPublishableKey_nuncaNull` | Devuelve `""` en vez de null |
| `createCheckoutSession_sinConfigurar_lanza` | `IllegalStateException` informativa |

### 2.4. UploadControllerTest (5 tests)
| Caso | Verifica |
|---|---|
| `subidaCorrecta_pngTrivial` | PNG válido se sube, devuelve URL `/uploads/<uuid>.png`, archivo existe en disco |
| `subidaVideo_correctaDevuelveTipoVideo` | MP4 → tipo `VIDEO` |
| `sinFichero_lanzaBadRequest` | Multipart vacío rechazado |
| `mimeNoAceptado_rechazado` | `text/plain` rechazado |
| `extensionFueraListaBlanca_rechazada` | `.exe` con MIME `image/png` rechazado por la lista blanca |

### 2.5. EmojiStripMigrationTest (8 tests)
| Caso | Verifica |
|---|---|
| `textoSinEmojis_quedaIntacto` | Sin emojis no toca el texto |
| `emojiPictograph_seQuita` | Quita 🐠 (rango U+1F300-1FAFF) |
| `emojiMiscSymbols_seQuita` | Quita ☀ (rango U+2600-26FF) |
| `variationSelectorYDingbats_seQuitan` | Quita ✔️ (U+2700 + U+FE0F) |
| `dobleEspacio_seCompacta` | Compacta 2+ espacios resultantes |
| `espacioAntesPuntuacion_seQuita` | Limpia espacios huérfanos antes de `,.;:!?` |
| `multiplesEmojis_todosSeQuitan` | Funciona con varios emojis seguidos |
| `saltoDeLinea_preservaSinTrailing` | No rompe `\n`, sí limpia trailing whitespace |

---

## 3. Walkthrough manual (pre-defensa)

Lista de pasos a ejecutar antes de cada presentación o entrega final. Tiempo estimado: 8-10 minutos.

### 3.1. Login & sesión
1. Cargar `/pages/login.html` → ✅ se ve sin errores en consola.
2. Login con `sofia_buceo` / `admin` → ✅ redirección a `/pages/feed.html`.
3. Logout → ✅ vuelta a `/pages/login.html` y localStorage vacío.

### 3.2. Feed
4. Tras login, el feed muestra publicaciones existentes con fotos, chips de datos técnicos y avatares.
5. Click en corazón → ✅ animación + contador sube en 1.
6. Click otra vez → ✅ contador baja (unlike).
7. Botón "Crear" → ✅ modal "Nueva publicación" aparece como bottom sheet en móvil.
8. "Elegir de galería o cámara" → ✅ abre el selector nativo del dispositivo.
9. Subir una foto → ✅ preview inmediato + URL `/uploads/...` en el campo hidden.
10. Rellenar descripción + lugar + profundidad → "Publicar" → ✅ aparece en feed con datos.

### 3.3. Inmersiones + reserva + pago
11. Dock → "Buscar" → ✅ catálogo se carga.
12. Click en "Reservar" en cualquier card → ✅ modal de reserva con número de personas.
13. Confirmar reserva → ✅ modal de pago abre con concepto + total + badge "TFG · DEMO".
14. Tarjeta `4242 4242 4242 4242` + `12/30` + `123` + nombre → "Pagar" → ✅ tick verde.
15. Cerrar modal → reservas → ✅ aparece en estado CONFIRMADA + PAID.
16. Notificaciones → ✅ "Tu reserva ha sido confirmada y pagada con Demo".

### 3.4. Mapa
17. Dock → "Mapa" → ✅ Leaflet carga con 7+ marcadores.
18. Click en marcador → ✅ popup con título, profundidad, temperatura.
19. Click en "Ver detalles" → ✅ modal con datos completos + tiempo atmosférico.

### 3.5. Buscar y proximidad
20. Dock → "Explorar" → ✅ aparecen usuarios, centros e inmersiones.
21. Tab "Inmersiones" → escribir "tabarca" → ✅ filtra por texto.
22. Limpiar búsqueda + click "Usar mi ubicación" → ✅ ordena por proximidad.

### 3.6. Perfil + foto
23. Dock → "Perfil" → ✅ datos de sofia_buceo (bio, certificación, número de inmersiones).
24. "Editar perfil" → "Cambiar foto desde galería o cámara" → subir foto → ✅ preview circular.
25. Cambiar bio + Guardar → ✅ alerta de éxito + datos persistidos al recargar.

### 3.7. Notificaciones y seguimiento
26. Login con `pablo_oc` / `admin`.
27. Buscar `sofia_buceo` → "Seguir" → ✅ se queda en "Solicitud enviada".
28. Logout, login con `sofia_buceo` → notificaciones → ✅ "@pablo_oc quiere seguirte" con botones.
29. "Aceptar" → ✅ alerta + notificación marcada como resuelta.

### 3.8. Responsive móvil
30. DevTools → Device toolbar → iPhone X.
31. Verificar:
    - ✅ Topbar compacta.
    - ✅ Dock fixed-bottom con safe-area-inset.
    - ✅ Modales como bottom sheets.
    - ✅ Filter tabs con scroll horizontal.
    - ✅ Inputs con altura 44px.

### 3.9. Swagger
32. Abrir `http://localhost:8080/swagger-ui.html` → ✅ UI se carga.
33. Probar `POST /api/auth/login` desde Swagger → ✅ devuelve JWT.
34. Pulsar "Authorize" → pegar `Bearer <token>` → ejecutar `GET /api/usuarios/perfil` → ✅ JSON del perfil.

### 3.10. Tests automatizados
35. `./mvnw test` → ✅ `Tests run: 31, Failures: 0, Errors: 0`.
36. (Opcional) `docker compose up --build` → ✅ app + MySQL en contenedores, healthy.

---

## 4. Compatibilidad de navegadores

Probado y validado en:

| Navegador | Versión | Estado |
|---|---|---|
| Chrome / Edge | 130+ | ✅ Plenamente funcional |
| Firefox | 130+ | ✅ Plenamente funcional |
| Safari (macOS) | 17+ | ✅ Plenamente funcional |
| Safari iOS | 17+ | ✅ Plenamente funcional incluyendo bottom sheets |
| Chrome Android | 130+ | ✅ Plenamente funcional con cámara nativa |
| Internet Explorer | — | ❌ No soportado (CSS3 moderno + ES2020) |

---

## 5. Errores conocidos

Ver `ISSUES.md` para el backlog completo. Los críticos están todos resueltos en la versión 1.0.0.

---

## 6. Métricas de calidad

| Métrica | Valor objetivo | Valor actual |
|---|---|---|
| Tests unitarios | ≥ 30 | **31** ✓ |
| Compilación limpia | sin warnings | ✓ (sólo Lombok deprecation warnings) |
| Lighthouse Accessibility | ≥ 85 | _pendiente medición final_ |
| Lighthouse Performance (móvil) | ≥ 70 | _pendiente medición final_ |
| Tiempo de arranque local (sin Docker) | ≤ 30 s | 12-15 s |
| Tiempo de build Docker | ≤ 5 min | 3-4 min en frío |
