package diveconnect.entity;

public enum EstadoReserva {
    PENDIENTE,    // Reserva creada, esperando confirmación
    CONFIRMADA,   // Reserva confirmada por el centro
    CANCELADA,    // Reserva cancelada
    COMPLETADA    // Inmersión realizada
}