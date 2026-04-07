package com.diveconnect.config;

import com.diveconnect.entity.CentroBuceo;
import com.diveconnect.entity.Inmersion;
import com.diveconnect.entity.Publicacion;
import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;
import com.diveconnect.repository.CentroBuceoRepository;
import com.diveconnect.repository.ComentarioRepository;
import com.diveconnect.repository.InmersionRepository;
import com.diveconnect.repository.PublicacionRepository;
import com.diveconnect.repository.ReservaRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Inicializa la base de datos con 3 usuarios de prueba (uno por cada rol).
 * Contraseña para todos: admin
 * Elimina todos los datos previos al arrancar.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository      usuarioRepository;
    private final PublicacionRepository  publicacionRepository;
    private final ComentarioRepository   comentarioRepository;
    private final ReservaRepository      reservaRepository;
    private final InmersionRepository    inmersionRepository;
    private final CentroBuceoRepository  centroBuceoRepository;
    private final PasswordEncoder        passwordEncoder;

    @Override
    public void run(String... args) {
        // Si los 3 usuarios de prueba están todos presentes, no reinicializar
        boolean tieneAdmin    = usuarioRepository.existsByUsername("admin");
        boolean tieneBuceador = usuarioRepository.existsByUsername("buceador");
        boolean tieneOcean    = usuarioRepository.existsByUsername("oceandive");
        if (tieneAdmin && tieneBuceador && tieneOcean) {
            log.info("=== DataInitializer: usuarios ya existen, saltando ===");
            return;
        }

        log.info("=== DataInitializer: limpiando y recreando datos de prueba ===");

        // ── 1. Borrar en orden (cada deleteAll auto-commit en su propia tx) ─
        comentarioRepository.deleteAll();
        reservaRepository.deleteAll();
        publicacionRepository.deleteAll();
        inmersionRepository.deleteAll();
        centroBuceoRepository.deleteAll();
        usuarioRepository.clearSeguidores();
        usuarioRepository.deleteAll();

        String pass = passwordEncoder.encode("admin");

        // ── 2. USUARIO_COMUN ──────────────────────────────────────────────
        Usuario comun = new Usuario();
        comun.setUsername("buceador");
        comun.setEmail("buceador@diveconnect.com");
        comun.setPassword(pass);
        comun.setTipoUsuario(TipoUsuario.USUARIO_COMUN);
        comun.setBiografia("Apasionado del buceo desde los 18 años. Certificación Open Water PADI. He buceado en el Mediterráneo, Mar Rojo y Caribe.");
        comun.setNivelCertificacion("Advanced Open Water");
        comun.setNumeroInmersiones(87);
        comun.setActivo(true);
        comun = usuarioRepository.save(comun);

        // ── 3. USUARIO_EMPRESA ────────────────────────────────────────────
        Usuario empresa = new Usuario();
        empresa.setUsername("oceandive");
        empresa.setEmail("empresa@diveconnect.com");
        empresa.setPassword(pass);
        empresa.setTipoUsuario(TipoUsuario.USUARIO_EMPRESA);
        empresa.setNombreEmpresa("Ocean Dive Center");
        empresa.setDescripcionEmpresa("Centro de buceo profesional en la Costa Brava. Instructores certificados PADI con más de 20 años de experiencia.");
        empresa.setDireccion("Passeig del Mar, 45, Palamós");
        empresa.setTelefono("+34 972 000 111");
        empresa.setSitioWeb("https://oceandive.example.com");
        empresa.setActivo(true);
        empresa = usuarioRepository.save(empresa);

        // ── 4. Centro de buceo para la empresa ───────────────────────────
        CentroBuceo centro = new CentroBuceo();
        centro.setNombre("Ocean Dive Center");
        centro.setDescripcion("Centro de buceo profesional en la Costa Brava con más de 20 años de experiencia. Alquiler de equipo, cursos PADI y excursiones a las Islas Medas.");
        centro.setDireccion("Passeig del Mar, 45");
        centro.setCiudad("Palamós");
        centro.setPais("España");
        centro.setTelefono("+34 972 000 111");
        centro.setEmail("info@oceandive.example.com");
        centro.setSitioWeb("https://oceandive.example.com");
        centro.setCertificaciones("PADI 5 Estrellas, SSI, DAN");
        centro.setValoracionPromedio(4.8);
        centro.setActivo(true);
        centro.setUsuario(empresa);
        centro = centroBuceoRepository.save(centro);

        // ── 5. Inmersiones de ejemplo ─────────────────────────────────────
        Inmersion inm1 = new Inmersion();
        inm1.setTitulo("Islas Medas — Ruta de los Corales");
        inm1.setDescripcion("Inmersión espectacular por los fondos de las Islas Medas, uno de los parques marinos más importantes del Mediterráneo. Verás corales, meros y barracudas.");
        inm1.setFechaInmersion(LocalDateTime.now().plusDays(7));
        inm1.setDuracion(60);
        inm1.setProfundidadMaxima(25.0);
        inm1.setNivelRequerido("Open Water");
        inm1.setPrecio(65.0);
        inm1.setPlazasTotales(8);
        inm1.setPlazasDisponibles(8);
        inm1.setUbicacion("Islas Medas, Girona");
        inm1.setEquipoIncluido("Traje, botella, regulador, ordenador de buceo");
        inm1.setActivo(true);
        inm1.setCentroBuceo(centro);
        inmersionRepository.save(inm1);

        Inmersion inm2 = new Inmersion();
        inm2.setTitulo("Cabo Creus — Noche bajo el mar");
        inm2.setDescripcion("Inmersión nocturna en el Cabo de Creus. Una experiencia única donde pulpos, morenas y cangrejos cobran protagonismo bajo la luz de las linternas.");
        inm2.setFechaInmersion(LocalDateTime.now().plusDays(14));
        inm2.setDuracion(45);
        inm2.setProfundidadMaxima(18.0);
        inm2.setNivelRequerido("Advanced Open Water");
        inm2.setPrecio(85.0);
        inm2.setPlazasTotales(6);
        inm2.setPlazasDisponibles(4);
        inm2.setUbicacion("Cabo de Creus, Girona");
        inm2.setEquipoIncluido("Traje, botella, regulador, linterna de buceo");
        inm2.setActivo(true);
        inm2.setCentroBuceo(centro);
        inmersionRepository.save(inm2);

        // ── 6. Publicaciones de ejemplo ───────────────────────────────────
        Publicacion pub1 = new Publicacion();
        pub1.setContenido("¡Increíble inmersión hoy en las Islas Medas! Vi un mero enorme de casi 30 kg y un banco de barracudas impresionante. El agua estaba a 22°C y la visibilidad superaba los 20 metros. ¡Qué sitio tan espectacular! 🤿🐟");
        pub1.setLugarInmersion("Islas Medas, Girona");
        pub1.setProfundidadMaxima(22.5);
        pub1.setTemperaturaAgua(22.0);
        pub1.setVisibilidad(20.0);
        pub1.setEspeciesVistas("Mero gigante, banco de barracudas, pulpo común, estrella de mar");
        pub1.setUsuario(comun);
        publicacionRepository.save(pub1);

        Publicacion pub2 = new Publicacion();
        pub2.setContenido("Nueva temporada en Ocean Dive Center 🏫 Ya tenemos disponibles las inmersiones de verano en las Medas. Plazas limitadas — ¡reserva la tuya antes de que se agoten! Este año también añadimos buceo nocturno en Cabo de Creus 🌙");
        pub2.setLugarInmersion("Palamós, Costa Brava");
        pub2.setUsuario(empresa);
        publicacionRepository.save(pub2);

        // ── 7. ADMINISTRADOR ──────────────────────────────────────────────
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setEmail("admin@diveconnect.com");
        admin.setPassword(pass);
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        admin.setBiografia("Administrador de la plataforma DiveConnect.");
        admin.setActivo(true);
        usuarioRepository.save(admin);

        log.info("=== DataInitializer: inicialización completa ===");
        log.info("  buceador     / admin  → USUARIO_COMUN");
        log.info("  oceandive    / admin  → USUARIO_EMPRESA (con centro + 2 inmersiones)");
        log.info("  admin        / admin  → ADMINISTRADOR");
    }

}
