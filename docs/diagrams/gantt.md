# Cronograma del proyecto (Gantt)

Cronograma real seguido durante el desarrollo de DiveConnect, con sprints semanales y fechas aproximadas en función del histórico de commits del repositorio.

```mermaid
gantt
    title DiveConnect — Cronograma de desarrollo
    dateFormat  YYYY-MM-DD
    axisFormat  %d %b
    excludes    weekends

    section Sprint 0 · Análisis
    Estudio de requisitos       :done, s0a, 2025-10-13, 4d
    Modelado de datos (E/R)     :done, s0b, after s0a, 3d
    Boceto de arquitectura      :done, s0c, after s0b, 2d
    Configuración del repo      :done, s0d, after s0c, 1d

    section Sprint 1 · Backend núcleo
    Entidades JPA               :done, s1a, 2025-10-23, 3d
    Auth JWT + login            :done, s1b, after s1a, 4d
    CRUD usuarios + perfiles    :done, s1c, after s1b, 3d
    Tests iniciales             :done, s1d, after s1c, 2d

    section Sprint 2 · Marketplace
    Centros de buceo            :done, s2a, 2025-11-10, 3d
    Inmersiones + filtros       :done, s2b, after s2a, 4d
    Reservas + plazas           :done, s2c, after s2b, 4d
    Notificaciones              :done, s2d, after s2c, 2d

    section Sprint 3 · Pasarela de pago
    Stripe Checkout             :done, s3a, 2025-12-01, 4d
    PayPal REST v2              :done, s3b, after s3a, 4d
    Modal frontend pago         :done, s3c, after s3b, 3d
    Demo TFG fallback           :done, s3d, after s3c, 2d

    section Sprint 4 · Red social
    Publicaciones               :done, s4a, 2026-01-12, 3d
    Comentarios + likes         :done, s4b, after s4a, 2d
    Sistema de seguimiento      :done, s4c, after s4b, 4d
    Historias 24h               :done, s4d, after s4c, 2d

    section Sprint 5 · Mapa y búsqueda
    Leaflet + puntos            :done, s5a, 2026-02-09, 3d
    OpenWeatherMap              :done, s5b, after s5a, 2d
    Búsqueda universal          :done, s5c, after s5b, 4d
    Proximidad Haversine        :done, s5d, after s5c, 2d

    section Sprint 6 · UX y diseño
    Tema submarino              :done, s6a, 2026-03-09, 4d
    Responsive móvil            :done, s6b, after s6a, 3d
    Bottom sheets + glass       :done, s6c, after s6b, 3d
    Wireframes + guía estilo    :done, s6d, after s6c, 3d

    section Sprint 7 · Subida archivos
    UploadController            :done, s7a, 2026-04-06, 2d
    File picker + camera        :done, s7b, after s7a, 2d
    Avatar de perfil            :done, s7c, after s7b, 1d

    section Sprint 8 · Calidad
    Tests JUnit + Mockito       :done, s8a, 2026-04-20, 5d
    Swagger / OpenAPI           :done, s8b, after s8a, 2d
    Lighthouse audit            :done, s8c, after s8b, 2d
    Refactor y limpieza         :active, s8d, after s8c, 3d

    section Sprint 9 · Despliegue
    Dockerfile + compose        :done, s9a, 2026-05-04, 2d
    GitHub Actions CI           :done, s9b, after s9a, 1d
    Deploy en Render            :done, s9c, after s9b, 1d
    Monitorización              :done, s9d, after s9c, 2d

    section Sprint 10 · Documentación final
    Memoria técnica             :active, s10a, 2026-05-11, 4d
    Slides defensa              :s10b, after s10a, 2d
    Ensayo defensa              :milestone, s10c, after s10b, 0d

    section Hitos
    MVP Backend                 :milestone, m1, 2025-11-21, 0d
    Pago funcional              :milestone, m2, 2025-12-19, 0d
    MVP Completo                :milestone, m3, 2026-03-27, 0d
    Defensa Tribunal            :milestone, m4, 2026-05-22, 0d
```

## Sprints en detalle

| Sprint | Foco | Entregables clave | Estado |
|---|---|---|---|
| 0 | Análisis y arranque | E/R, repo Git, README inicial | ✓ |
| 1 | Backend núcleo | Auth, perfiles, base JPA | ✓ |
| 2 | Marketplace | Centros, inmersiones, reservas | ✓ |
| 3 | Pasarela | Stripe + PayPal + demo | ✓ |
| 4 | Red social | Publicaciones, likes, seguimiento | ✓ |
| 5 | Mapa | Leaflet, weather, búsqueda | ✓ |
| 6 | UX | Tema submarino, responsive | ✓ |
| 7 | Subida | UploadController, file picker | ✓ |
| 8 | Calidad | Tests, Swagger, Lighthouse | en curso |
| 9 | Despliegue | Docker, Render, CI/CD | ✓ |
| 10 | Defensa | Memoria final, slides | en curso |

## Hitos cumplidos

- **MVP Backend** (21 nov 2025): API REST completa con auth y CRUD básico.
- **Pago funcional** (19 dic 2025): pasarela end-to-end con Stripe + PayPal + demo.
- **MVP Completo** (27 mar 2026): todas las funcionalidades del scope final.
- **Defensa Tribunal** (22 may 2026): presentación ante tribunal docente.
