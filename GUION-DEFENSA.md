# Guion de defensa - DiveConnect

Duracion objetivo: 10-12 minutos + preguntas.

## 1. Apertura (1 min)

Buenos dias. Soy Marcos y presento DiveConnect, una plataforma web para buceadores y centros de buceo. El problema que resuelve es la dispersion actual: los centros publican inmersiones por redes o mensajeria, los usuarios reservan por canales manuales y la informacion tecnica queda repartida.

DiveConnect une comunidad, reservas y gestion en una unica aplicacion web.

## 2. Objetivos y alcance (1 min)

Objetivos principales:

- Red social vertical para buceadores.
- Marketplace de inmersiones con reservas y pagos.
- Panel de empresa y panel administrador.
- Mapa con puntos de interes y datos meteorologicos.
- Despliegue reproducible, pruebas y documentacion completa.

El MVP no intenta ser una app movil nativa ni un ERP completo de centros; se centra en demostrar un flujo web completo y mantenible.

## 3. Arquitectura tecnica (2 min)

Explicar con `docs/diagrams/architecture.md`:

- Frontend HTML/CSS/JS servido como estatico.
- Backend Spring Boot con controladores, servicios, repositorios, entidades y DTOs.
- Seguridad con JWT, BCrypt, filtros y roles.
- MySQL como base relacional.
- Integraciones: Stripe, PayPal, Google OAuth2 y OpenWeatherMap.
- Docker y CI con GitHub Actions.

Mensaje clave: la logica cliente y servidor esta separada, y las reglas de negocio viven en servicios transaccionales.

## 4. Base de datos (1 min)

Mostrar `docs/diagrams/er-diagram.md` y comentar:

- Usuarios, centros, inmersiones, reservas, publicaciones, comentarios, historias, mapa y notificaciones.
- Relaciones N:M para seguidores y likes.
- Vistas para reporting y paneles.
- Procedimientos para operaciones criticas.
- Indices para consultas frecuentes.

Justificar la desnormalizacion de `centro_buceo_id` en reservas: mejora la consulta de reservas recibidas por centro.

## 5. Demo funcional (4 min)

### Flujo usuario comun

1. Login con `sofia_buceo` / `admin`.
2. Feed: publicar, comentar o dar like.
3. Buscar inmersiones y aplicar filtro.
4. Reservar una inmersion.
5. Pagar con modo demo y comprobar reserva confirmada.

### Flujo centro

1. Login con `oceandive` / `admin`.
2. Abrir panel empresa.
3. Mostrar gestion de inmersiones y reservas recibidas.

### Flujo admin

1. Login con `admin` / `admin`.
2. Abrir dashboard admin.
3. Mostrar estadisticas y gestion de usuarios/centros/reservas.

Si falla internet o pasarelas externas, usar modo demo: esta preparado precisamente para que la defensa sea robusta.

## 6. Calidad, pruebas y despliegue (1 min)

Mostrar:

- `mvn test`: 31 tests, 0 fallos en la revision.
- `docs/test-plan.md`: plan automatico y manual.
- `Dockerfile`, `docker-compose.yml`, `render.yaml`: despliegue.
- `/actuator/health`: healthcheck.
- `COBERTURA-RUBRICA.md`: trazabilidad con los criterios.

## 7. Sostenibilidad y mejoras (1 min)

Puntos de sostenibilidad:

- Consultas con indices y paginacion.
- Limpieza programada de historias expiradas.
- Logs SQL desactivados por defecto.
- Imagen Docker multi-stage.
- Uploads fuera de Git.

Mejoras futuras:

- Analitica integrada en panel admin.
- Valoraciones por reserva.
- Capturas finales y Lighthouse real sobre produccion.
- Politicas de backup y restauracion.

## 8. Cierre

DiveConnect demuestra las competencias del ciclo completo: cliente, servidor, base de datos, despliegue, diseno, pruebas, seguridad, documentacion y defensa tecnica. El repositorio queda preparado para que cualquier miembro del tribunal pueda encontrar evidencias concretas en la memoria y en la matriz de cobertura.

## Preguntas esperables

| Pregunta | Respuesta breve |
|---|---|
| Por que sin framework frontend | Para demostrar dominio de HTML/CSS/JS y reducir peso del cliente en el MVP |
| Como proteges rutas privadas | JWT, filtro Spring Security, roles y reglas por endpoint |
| Como evitas overbooking | La reserva descuenta plazas en transaccion y la cancelacion las libera |
| Que pasa si Stripe/PayPal no estan configurados | La app usa modo demo para defensa y entornos sin credenciales |
| Como escalaria | Separar frontend, CDN para estaticos, MySQL gestionado, backups, replicas y storage externo para uploads |
| Donde estan las pruebas | `src/test/java/` y `docs/test-plan.md` |
