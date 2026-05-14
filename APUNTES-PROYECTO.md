# Apuntes de proyecto - DiveConnect

## Decisiones tecnicas

| Decision | Motivo | Evidencia |
|---|---|---|
| Spring Boot + Java 17 | Stack estable, productivo y alineado con servidor DAW | `pom.xml`, `src/main/java/` |
| Frontend sin framework | Menor peso y demostracion directa de HTML/CSS/JS | `src/main/resources/static/` |
| JWT stateless | Encaja con API REST y cliente SPA-like | `SecurityConfig`, `JwtAuthenticationFilter` |
| MySQL 8 | Modelo relacional claro para reservas y relaciones sociales | `database/schema.sql` |
| Docker multi-stage | Build reproducible e imagen runtime mas ligera | `Dockerfile` |
| Modo demo de pagos | Robustez para defensa sin depender de credenciales externas | `PaymentController`, `payment.js` |
| CORS centralizado | Evita duplicidad en controladores y facilita produccion | `CorsConfig` |

## Deuda controlada

| Deuda | Impacto | Plan |
|---|---|---|
| Capturas finales no versionadas | Falta evidencia visual en README | Generar `docs/screenshots/` antes de exportar la memoria |
| Analitica no integrada en panel web | Es modulo optativo, no bloquea MVP | Crear `AnalyticsController` en futura version |
| Backups no automatizados | Riesgo productivo | Programar dumps MySQL diarios en proveedor externo |
| Valoraciones por reserva | Mejora funcional | Anadir campo en `Reserva` y recalculo de centro |

## Checklist previa a defensa

- Ejecutar `mvn test`.
- Arrancar app y verificar `/actuator/health`.
- Probar login con usuario comun, empresa y admin.
- Probar reserva + pago demo.
- Probar mapa y busqueda.
- Revisar `COBERTURA-RUBRICA.md`.
- Exportar `MEMORIA-DIVECONNECT.md` a PDF.
- Preparar diapositivas desde `GUION-DEFENSA.md`.
