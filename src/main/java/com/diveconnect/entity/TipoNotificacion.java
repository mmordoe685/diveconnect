package com.diveconnect.entity;

public enum TipoNotificacion {

    /** Alguien ha solicitado seguirte. Accionable: aceptar / rechazar. */
    SOLICITUD_SEGUIMIENTO,

    /** Tu solicitud de seguimiento ha sido aceptada. */
    SEGUIMIENTO_ACEPTADO,

    /** Tu solicitud de seguimiento ha sido rechazada. */
    SEGUIMIENTO_RECHAZADO,

    /** Alguien empezó a seguirte (sin necesidad de solicitud, p.ej. empresa). */
    NUEVO_SEGUIDOR,

    /** Alguien ha dado like a tu publicación. */
    LIKE_PUBLICACION,

    /** Alguien ha comentado tu publicación. */
    COMENTARIO_PUBLICACION,

    /** Tu reserva ha sido confirmada / pagada. */
    RESERVA_CONFIRMADA,

    /** (Empresa) Has recibido una nueva reserva. */
    RESERVA_RECIBIDA,

    /** Alguien te ha mencionado en una historia o publicación. */
    MENCION
}
