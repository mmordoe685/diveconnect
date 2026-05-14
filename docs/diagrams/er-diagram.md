## 1. Diagrama Entidad-Relación (Mermaid)

erDiagram
    USUARIO ||--o{ PUBLICACION : "publica"
    USUARIO ||--o{ COMENTARIO : "comenta"
    USUARIO ||--o{ HISTORIA : "crea"
    USUARIO ||--o{ RESERVA : "reserva"
    USUARIO ||--o| CENTRO_BUCEO : "es propietario"
    USUARIO ||--o{ NOTIFICACION : "recibe"
    USUARIO ||--o{ SOLICITUD_SEGUIMIENTO : "solicita"
    USUARIO ||--o{ PUNTO_MAPA : "crea"
    USUARIO }o--o{ USUARIO : "sigue / es seguido"

    CENTRO_BUCEO ||--o{ INMERSION : "ofrece"
    INMERSION ||--o{ RESERVA : "es reservada"

    PUBLICACION ||--o{ COMENTARIO : "tiene"
    PUBLICACION }o--o{ USUARIO : "likes (NxM)"

    PUNTO_MAPA ||--o{ FOTO_PUNTO_MAPA : "contiene"

    USUARIO {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password "BCrypt hash"
        text biografia
        varchar foto_perfil
        varchar nivel_certificacion
        int numero_inmersiones
        enum tipo_usuario "COMUN | EMPRESA | ADMIN"
        varchar nombre_empresa
        text descripcion_empresa
        varchar direccion
        varchar telefono
        varchar sitio_web
        boolean activo
        datetime fecha_registro
    }

    CENTRO_BUCEO {
        bigint id PK
        bigint usuario_id FK,UK
        varchar nombre
        text descripcion
        varchar direccion
        varchar ciudad
        varchar provincia
        varchar pais
        varchar telefono
        varchar email
        varchar sitio_web
        varchar certificaciones
        varchar imagen_url
        double valoracion
        boolean activo
    }

    INMERSION {
        bigint id PK
        bigint centro_buceo_id FK
        varchar titulo
        text descripcion
        datetime fecha_inmersion
        int duracion "minutos"
        double profundidad_maxima
        varchar nivel_requerido
        double precio
        int plazas_totales
        int plazas_disponibles
        varchar ubicacion
        double latitud
        double longitud
        text equipo_incluido
        varchar imagen_url
        boolean activa
        datetime fecha_creacion
    }

    RESERVA {
        bigint id PK
        bigint usuario_id FK
        bigint inmersion_id FK
        bigint centro_buceo_id FK
        int numero_personas
        double precio_total
        enum estado "PENDIENTE | CONFIRMADA | CANCELADA | COMPLETADA"
        varchar payment_status "UNPAID | PAID | FAILED"
        varchar stripe_session_id
        varchar stripe_payment_intent_id
        varchar paypal_order_id
        varchar paypal_capture_id
        text observaciones
        datetime fecha_reserva
        datetime ultima_modificacion
    }

    PUBLICACION {
        bigint id PK
        bigint usuario_id FK
        text contenido
        varchar imagen_url
        varchar video_url
        varchar lugar_inmersion
        double profundidad_maxima
        double temperatura_agua
        double visibilidad
        varchar especies_vistas
        datetime fecha_publicacion
    }

    COMENTARIO {
        bigint id PK
        bigint publicacion_id FK
        bigint usuario_id FK
        text contenido
        datetime fecha_comentario
    }

    HISTORIA {
        bigint id PK
        bigint usuario_id FK
        varchar media_url
        enum media_type "FOTO | VIDEO"
        varchar texto
        datetime fecha_creacion
        datetime fecha_expiracion "+24h"
    }

    NOTIFICACION {
        bigint id PK
        bigint destinatario_id FK
        bigint emisor_id FK "nullable"
        enum tipo
        varchar mensaje
        boolean leida
        boolean accionable
        boolean resuelta
        bigint entidad_relacionada_id
        datetime fecha_creacion
    }

    SOLICITUD_SEGUIMIENTO {
        bigint id PK
        bigint solicitante_id FK
        bigint destinatario_id FK
        enum estado "PENDIENTE | ACEPTADA | RECHAZADA"
        datetime fecha_creacion
        datetime fecha_respuesta
    }

    PUNTO_MAPA {
        bigint id PK
        bigint autor_id FK
        double latitud
        double longitud
        varchar titulo
        text descripcion
        double profundidad_metros
        double temperatura_agua
        double presion_bar
        double visibilidad_metros
        varchar corriente
        varchar especies_vistas
        datetime fecha_observacion
    }

    FOTO_PUNTO_MAPA {
        bigint id PK
        bigint punto_mapa_id FK
        varchar url
        varchar especie_avistada
        text descripcion
    }


## 3. Normalización aplicada

El esquema cumple las tres primeras formas normales:

### 1ª Forma Normal (1FN)
Todos los atributos son atómicos. No hay listas embebidas en celdas. Las "especies vistas" se almacenan como cadena separada por comas pero a efectos del modelo se trata como un valor único de texto libre — no es una colección con consultas individualizadas.

### 2ª Forma Normal (2FN)
No hay claves primarias compuestas en tablas con dependencias parciales. Las únicas claves compuestas son las tablas puente (`seguidores`, `publicacion_likes`) cuyos atributos se reducen a la propia clave.

### 3ª Forma Normal (3FN)
No hay dependencias transitivas. Decisión consciente:

- En `reservas` se duplica `centro_buceo_id` aunque sea derivable desde `inmersion_id → inmersion.centro_buceo_id`. Es una **desnormalización deliberada** para acelerar la consulta "reservas recibidas por un centro" sin tener que hacer JOIN. Documentado en la memoria, capítulo 6.
- `usuarios.nombre_empresa` aparece y se duplica en `centros_buceo.nombre`. La razón es histórica: el registro inicial guarda el nombre en `usuarios`, pero el centro tiene su entidad propia con datos extra. Las dos tablas pueden divergir y se asume que `centros_buceo.nombre` es la fuente de verdad cuando existe.

### Boyce-Codd (BCNF)
Tablas no en BCNF estricta: `reservas` (por la desnormalización mencionada). El resto sí.
