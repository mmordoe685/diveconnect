package com.diveconnect.config;

import com.diveconnect.entity.*;
import com.diveconnect.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inicializa la base de datos con datos de demostración completos.
 * Contraseña para todos los usuarios: admin
 * Se ejecuta solo si los usuarios de prueba no existen aún.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository     usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository  comentarioRepository;
    private final ReservaRepository     reservaRepository;
    private final InmersionRepository   inmersionRepository;
    private final CentroBuceoRepository centroBuceoRepository;
    private final PasswordEncoder       passwordEncoder;

    @Override
    public void run(String... args) {
        // Solo inicializar si no existen usuarios de demo
        if (usuarioRepository.existsByUsername("admin")
            && usuarioRepository.existsByUsername("buceador")
            && usuarioRepository.existsByUsername("oceandive")) {
            log.info("=== DataInitializer: datos ya existen, saltando ===");
            return;
        }
        log.info("=== DataInitializer: creando datos de demostración ===");
        borrarTodo();
        poblar();
        log.info("=== DataInitializer: completado con éxito ===");
    }

    // ─── Borrado ───────────────────────────────────────────────────────────────

    @Transactional
    public void borrarTodo() {
        comentarioRepository.deleteAll();
        reservaRepository.deleteAll();
        publicacionRepository.deleteAll();
        inmersionRepository.deleteAll();
        centroBuceoRepository.deleteAll();
        usuarioRepository.clearSeguidores();
        usuarioRepository.deleteAll();
    }

    // ─── Creación ──────────────────────────────────────────────────────────────

    private void poblar() {
        String pass = passwordEncoder.encode("admin");

        // ── ADMINISTRADORES ──────────────────────────────────────────────────
        Usuario admin = crearAdmin("admin", "admin@diveconnect.com",
            "Administrador principal de DiveConnect. Supervisa el correcto funcionamiento de la plataforma.", pass);
        Usuario superadmin = crearAdmin("superadmin", "superadmin@diveconnect.com",
            "Super Administrador con acceso completo a todos los módulos de la plataforma.", pass);

        // ── EMPRESAS ─────────────────────────────────────────────────────────
        Usuario uOcean = crearEmpresa("oceandive", "oceandive@diveconnect.com",
            "Ocean Dive Center", "Centro de buceo de referencia en la Costa Brava. +20 años de experiencia.",
            "Passeig del Mar, 45, Palamós", "+34 972 100 200", "https://oceandive.es", pass);
        Usuario uBlue  = crearEmpresa("blueworld", "blueworld@diveconnect.com",
            "Blue World Diving", "Tu aventura submarina en la Costa Blanca. Cursos PADI, snorkel y excursiones.",
            "Av. del Puerto, 12, Alicante", "+34 965 200 300", "https://blueworlddiving.es", pass);
        Usuario uIsland= crearEmpresa("islanddive", "islanddive@diveconnect.com",
            "Island Dive Center", "El centro de buceo premium de Ibiza. Descubre los fondos cristalinos del Mediterráneo.",
            "Carrer de la Mar, 8, Ibiza", "+34 971 300 400", "https://islanddive.es", pass);

        // ── USUARIOS COMUNES ─────────────────────────────────────────────────
        Usuario carlos  = crearComun("buceador",    "buceador@diveconnect.com",
            "Apasionado del buceo desde los 18 años. He explorado el Mediterráneo, el Mar Rojo y el Caribe. Cada inmersión es una aventura única 🤿",
            "Advanced Open Water", 87, "https://ui-avatars.com/api/?name=Carlos+M&background=0077B6&color=fff&size=200", pass);
        Usuario marina  = crearComun("marinalopez", "marina@diveconnect.com",
            "Bióloga marina y buceadora entusiasta. Me encanta fotografiar fauna marina y concienciar sobre la conservación de los océanos 🐠",
            "Open Water", 23, "https://ui-avatars.com/api/?name=Marina+L&background=00B4D8&color=fff&size=200", pass);
        Usuario javier  = crearComun("javier_sub",  "javier@diveconnect.com",
            "Divemaster certificado con más de 200 inmersiones. Especialista en buceo técnico y en cuevas. Instagramer submarino con 15K seguidores.",
            "Divemaster", 234, "https://ui-avatars.com/api/?name=Javier+T&background=023E8A&color=fff&size=200", pass);
        Usuario sofia   = crearComun("sofia_buceo", "sofia@diveconnect.com",
            "Rescue Diver y futura instructora PADI. Amante de los tiburones y de los fondos del Atlántico. Buceo + viajes = mi vida perfecta ❤️",
            "Rescue Diver", 156, "https://ui-avatars.com/api/?name=Sofia+G&background=48CAE4&color=023E8A&size=200", pass);
        Usuario pablo   = crearComun("pablo_oc",    "pablo@diveconnect.com",
            "Recién sacado el Open Water y ya enamorado del submarinismo. Cada vez que bajo al agua descubro algo increíble.",
            "Open Water", 12, "https://ui-avatars.com/api/?name=Pablo+O&background=0096C7&color=fff&size=200", pass);
        Usuario lucia   = crearComun("lucia_dive",  "lucia@diveconnect.com",
            "Fotógrafa submarina con 7 años de experiencia. Mis fotos han aparecido en National Geographic España. El océano es mi estudio.",
            "Advanced Open Water", 67, "https://ui-avatars.com/api/?name=Lucia+F&background=0077B6&color=fff&size=200", pass);

        // ── CENTROS DE BUCEO ─────────────────────────────────────────────────
        CentroBuceo centroOcean = crearCentro(uOcean,
            "Ocean Dive Center",
            "Centro de buceo de referencia en la Costa Brava. Somos centro PADI 5 Estrellas con más de 20 años de experiencia formando buceadores. Disponemos de las mejores embarcaciones y el equipo más moderno del mercado.",
            "Passeig del Mar, 45", "Palamós", "España", "Catalunya",
            "+34 972 100 200", "info@oceandive.es", "https://oceandive.es",
            "PADI 5 Estrellas, SSI Gold Palm, DAN Partner",
            "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&q=80", 4.8);

        CentroBuceo centroBlue = crearCentro(uBlue,
            "Blue World Diving",
            "Sumérgete en las aguas transparentes de la Costa Blanca con nuestro equipo de instructores certificados. Ofrecemos cursos para todos los niveles, alquiler de equipo y emocionantes excursiones a los mejores puntos de buceo.",
            "Av. del Puerto, 12", "Alicante", "España", "Valencia",
            "+34 965 200 300", "info@blueworlddiving.es", "https://blueworlddiving.es",
            "PADI Resort, SSI, NAUI",
            "https://images.unsplash.com/photo-1559825481-12a05cc00344?w=800&q=80", 4.6);

        CentroBuceo centroIsland = crearCentro(uIsland,
            "Island Dive Center",
            "Vive la experiencia de bucear en las aguas cristalinas de Ibiza. Nuestro centro premium ofrece inmersiones exclusivas, cursos personalizados y expediciones únicas. La joya del submarinismo en las Baleares.",
            "Carrer de la Mar, 8", "Ibiza", "España", "Baleares",
            "+34 971 300 400", "info@islanddive.es", "https://islanddive.es",
            "PADI 5 Estrellas IDC, SSI Platinum",
            "https://images.unsplash.com/photo-1682687982501-1e58ab814714?w=800&q=80", 4.9);

        // ── INMERSIONES ──────────────────────────────────────────────────────
        // Ocean Dive Center (4 inmersiones)
        Inmersion io1 = crearInmersion(centroOcean, "Islas Medas — Ruta de los Corales",
            "Una de las inmersiones más espectaculares del Mediterráneo. Exploraremos los fondos de las Islas Medas, Parque Natural Marino, donde habitan meros gigantes, pulpos, morenas y bancos de barracudas. Visibilidad garantizada de 20+ metros.",
            7, 60, 22.0, "Open Water", 65.0, 8, 6,
            "Islas Medas, Girona", 42.052, 3.222,
            "Traje, botella 12L, regulador, chaleco, ordenador de buceo",
            "https://images.unsplash.com/photo-1518020382113-a7e8fc38eac9?w=800&q=80");

        Inmersion io2 = crearInmersion(centroOcean, "Cabo de Creus — Inmersión Nocturna",
            "Una experiencia única e irrepetible: el Cabo de Creus bajo las estrellas. En la oscuridad, pulpos, cangrejos ermitaños y morenas cobran protagonismo absoluto. Con linternas de buceo y en grupos reducidos para máxima seguridad.",
            14, 50, 18.0, "Advanced Open Water", 85.0, 6, 4,
            "Cabo de Creus, Girona", 42.319, 3.317,
            "Traje, botella, regulador, linterna principal + backup",
            "https://images.unsplash.com/photo-1551244072-5d12893278bc?w=800&q=80");

        Inmersion io3 = crearInmersion(centroOcean, "La Vaca — Pecios y Naufragios",
            "Buceo sobre el naufragio del carguero La Vaca, hundido en 1917. Una ventana al pasado llena de historia y vida marina. Corales, esponjas y decenas de especies han convertido este barco en un arrecife artificial fascinante.",
            21, 55, 28.0, "Advanced Open Water", 95.0, 5, 5,
            "La Vaca, Costa Brava", 41.735, 2.943,
            "Traje seco disponible, botella 12L, regulador, linterna",
            "https://images.unsplash.com/photo-1631462353222-a8fe574c69e2?w=800&q=80");

        Inmersion io4 = crearInmersion(centroOcean, "Curso Open Water — Primera Inmersión",
            "¿Nunca has buceado? Este es tu primer paso. Nuestros instructores certificados te guiarán en una sesión teórica + práctica en aguas someras. Al finalizar, recibirás la certificación provisional PADI Open Water.",
            3, 90, 6.0, "Sin certificación", 120.0, 4, 3,
            "Cala Aiguablava, Begur", 41.946, 3.208,
            "Todo el equipo incluido, material teórico, certificación provisional",
            "https://images.unsplash.com/photo-1682687982183-c2937a74257c?w=800&q=80");

        // Blue World Diving (4 inmersiones)
        Inmersion ib1 = crearInmersion(centroBlue, "Isla Tabarca — Reserva Marina",
            "La única isla habitada de la Comunitat Valenciana esconde uno de los fondos marinos más ricos del Mediterráneo occidental. Posidonia oceánica, tortugas bobas y miles de peces nos esperan en esta reserva natural protegida.",
            5, 55, 20.0, "Open Water", 55.0, 10, 7,
            "Isla Tabarca, Alicante", 38.163, -0.477,
            "Botella 10L, regulador, chaleco. Traje de alquiler disponible.",
            "https://images.unsplash.com/photo-1682687220795-796d3f6638a3?w=800&q=80");

        Inmersion ib2 = crearInmersion(centroBlue, "Cabo Huertas — El Bajo del Pargo",
            "El Bajo del Pargo es uno de los secretos mejor guardados de Alicante. Una formación rocosa submarina llena de vida: morenas, grandes sargos, dentones y ocasionalmente águilas de mar. Para buceadores que quieren algo especial.",
            10, 60, 25.0, "Advanced Open Water", 70.0, 8, 5,
            "Cabo Huertas, Alicante", 38.344, -0.394,
            "Botella 12L, regulador completo, ordenador",
            "https://images.unsplash.com/photo-1544551763-77ef2d0cfc6c?w=800&q=80");

        Inmersion ib3 = crearInmersion(centroBlue, "Jávea — Cova Tallada",
            "La Cova Tallada es una cueva marina única tallada en la roca caliza. Con luz natural que penetra desde el exterior y estalactitas submarinas, esta inmersión es una de las más fotogénicas de la costa valenciana.",
            18, 45, 15.0, "Open Water", 60.0, 6, 6,
            "Cova Tallada, Jávea", 38.745, 0.237,
            "Botella, regulador, linterna de buceo",
            "https://images.unsplash.com/photo-1559825481-12a05cc00344?w=800&q=80");

        Inmersion ib4 = crearInmersion(centroBlue, "Snorkel + Bautizo de Buceo",
            "La actividad perfecta para familias y principiantes. Comenzamos con snorkel en aguas cristalinas y terminamos con un bautizo de buceo en zona protegida. Sin experiencia previa necesaria. Apto desde 8 años.",
            2, 120, 4.0, "Sin experiencia", 45.0, 12, 10,
            "Playa del Postiguet, Alicante", 38.352, -0.483,
            "Máscaras, tubos, aletas y equipo completo de bautizo incluido",
            "https://images.unsplash.com/photo-1682687982501-1e58ab814714?w=800&q=80");

        // Island Dive Center (4 inmersiones)
        Inmersion ii1 = crearInmersion(centroIsland, "Ses Margalides — Aguas Cristalinas",
            "Las Margalides son un conjunto de islotes al norte de Ibiza con aguas de visibilidad excepcional (hasta 40m). El contraste entre los fondos arenosos y las formaciones rocosas llenas de color hace de esta inmersión una joya absoluta.",
            6, 65, 30.0, "Advanced Open Water", 90.0, 6, 4,
            "Ses Margalides, Ibiza", 39.076, 1.302,
            "Botella 12L, regulador, ordenador Suunto incluido",
            "https://images.unsplash.com/photo-1576513657268-8e2173b1e0a5?w=800&q=80");

        Inmersion ii2 = crearInmersion(centroIsland, "Es Vedrà — El Misterio del Mediterráneo",
            "Es Vedrà, el islote más misterioso del Mediterráneo, esconde bajo sus aguas un mundo submarino extraordinario. Gran profundidad, corrientes moderadas y una fauna espectacular hacen de esta inmersión una experiencia para no olvidar.",
            12, 60, 35.0, "Advanced Open Water", 105.0, 5, 3,
            "Es Vedrà, Ibiza", 38.868, 1.198,
            "Botella 12L, regulador, ordenador, chaleco top-of-line",
            "https://images.unsplash.com/photo-1631462353222-a8fe574c69e2?w=800&q=80");

        Inmersion ii3 = crearInmersion(centroIsland, "Formentera — La Posidonia Patrimonio UNESCO",
            "El paseo marítimo más bonito del Mediterráneo bajo el agua. Las praderas de posidonia oceánica de Formentera son Patrimonio de la Humanidad. Aguas turquesas, vida marina abundante y una experiencia de paz absoluta.",
            9, 50, 12.0, "Open Water", 75.0, 8, 6,
            "Formentera, Baleares", 38.703, 1.452,
            "Equipo completo + fotografía submarina disponible en alquiler",
            "https://images.unsplash.com/photo-1518020382113-a7e8fc38eac9?w=800&q=80");

        Inmersion ii4 = crearInmersion(centroIsland, "Sunset Dive — Inmersión al Atardecer",
            "Una experiencia única: bucear mientras el sol se pone sobre el Mediterráneo. Las últimas luces del día crean una atmósfera mágica bajo el agua. Termina con cena en cubierta de nuestro catamarán privado. Máximo exclusivo.",
            4, 45, 20.0, "Open Water", 150.0, 4, 2,
            "Costa Oeste, Ibiza", 38.900, 1.203,
            "Equipo premium + cena a bordo del catamarán incluida",
            "https://images.unsplash.com/photo-1682687220795-796d3f6638a3?w=800&q=80");

        // ── PUBLICACIONES ─────────────────────────────────────────────────────
        crearPublicacion(carlos,
            "¡Increíble jornada hoy en las Islas Medas con Ocean Dive Center! Un mero de casi 30kg nos acompañó durante toda la inmersión. La visibilidad superaba los 25 metros y el agua estaba a 22°C. No me canso de este lugar 🤿🐟",
            "Islas Medas, Girona", 22.5, 22.0, 25.0, "Mero gigante, banco de barracudas, pulpo común, morena");

        crearPublicacion(marina,
            "Buceo científico hoy en la reserva de Tabarca 🔬 Hemos catalogado 3 nuevas colonias de posidonia y avistado una tortuga boba que ya tenía marcada del año pasado. El trabajo de conservación marina es fundamental. Gracias a @blueworld por su apoyo 🌊",
            "Isla Tabarca, Alicante", 15.0, 20.0, 18.0, "Tortuga boba, posidonia oceánica, sargos, doncellas");

        crearPublicacion(javier,
            "200+ inmersiones y cada vez me sigue sorprendiendo el mar 🙌 Hoy en la Cova Tallada de Jávea: estalactitas bajo el agua, rayos de luz atravesando las grietas y una serenidad absoluta. Este es el buceo que me apasiona. Vídeo completo en mi canal de YouTube.",
            "Cova Tallada, Jávea", 14.0, 18.0, 15.0, "Estalactitas submarinas, moray, congrios, langostas");

        crearPublicacion(sofia,
            "Primer avistamiento de tiburón marrajo en España! 🦈 A 35m en el Cabo de Creus, una hembra de unos 2 metros nos pasó muy cerca. Momento único que nunca olvidaré. El mar sigue siendo el ecosistema más fascinante del planeta. #SaveTheSharks",
            "Cabo de Creus, Girona", 35.0, 16.0, 20.0, "Tiburón marrajo, atunes, agujas de mar");

        crearPublicacion(pablo,
            "¡Mi primera inmersión real fuera de la formación! Las medas me han dejado sin palabras 😍 Antes pensaba que el buceo era 'aburrido' y ahora no pienso en otra cosa. Gracias a los instructores de @oceandive por hacer que me sienta seguro bajo el agua. Próximo objetivo: Advanced Open Water!",
            "Islas Medas, Girona", 10.0, 22.0, 20.0, "Pulpos, peces loro, pez vela, estrellas de mar");

        crearPublicacion(lucia,
            "Mi foto favorita de este año 📸 Una morena asomándose de su madriguera en Ibiza con los rayos de sol creando un halo dorado a su alrededor. Esta imagen ganó el concurso de fotografía submarina de @NatGeoEspana. El submarinismo me ha dado mi profesión y mi pasión.",
            "Ses Margalides, Ibiza", 25.0, 24.0, 35.0, "Morena gigante, pez escorpión, nudibranquios");

        crearPublicacion(uOcean,
            "🎉 NUEVA TEMPORADA 2026 en Ocean Dive Center! Ya disponibles las plazas para nuestras inmersiones estrella: Islas Medas, Cabo de Creus y el emocionante pecio La Vaca. Grupos reducidos (máx. 6 personas), instructores certificados PADI y el mejor equipo. ¡Reserva ya tu aventura!",
            "Ocean Dive Center, Palamós", null, null, null, null);

        crearPublicacion(uBlue,
            "🌊 Blue World Diving te invita a descubrir la Isla Tabarca! Reserva marina protegida, aguas cristalinas y la mejor biodiversidad de la Costa Blanca. Este mes tenemos descuento del 20% en grupos de 4 o más personas. ¡No dejes pasar esta oportunidad! Contáctanos 📞",
            "Isla Tabarca, Alicante", null, null, null, null);

        crearPublicacion(uIsland,
            "✨ EXPERIENCIA EXCLUSIVA: Sunset Dive en Ibiza! Bucea al atardecer en las aguas más cristalinas del Mediterráneo y termina la noche con una cena exclusiva en nuestro catamarán. Solo 4 plazas por sesión. Para los que buscan algo verdaderamente especial. 🌅🤿",
            "Costa Oeste, Ibiza", null, null, null, null);

        crearPublicacion(carlos,
            "Hoy hice el Rescue Diver con @oceandive y fue la experiencia más intensa de mi vida como buceador. Situaciones de emergencia simuladas, rescate de buceadores en pánico, navegación sin visibilidad... Muy recomendable para cualquier buceador que quiera mejorar su seguridad.",
            "Cala Montjoi, Girona", 8.0, 20.0, 15.0, "Simulacros de rescate, trabajo en equipo");

        crearPublicacion(marina,
            "Hoy doy una charla en el colegio sobre los ecosistemas marinos mediterráneos 🌊 Cada vez que veo la cara de los niños cuando ven las fotos de mis inmersiones... eso es lo que me motiva a seguir. El mar necesita más defensores. ¿Os animáis a aprender a bucear? 🤿",
            null, null, null, null, null);

        crearPublicacion(javier,
            "CHALLENGE: ¿Cuántas especies diferentes puedes identificar en una sola inmersión? Mi récord personal son 47 en las Medas. Comparte tus fotos con #DiveConnectChallenge y el ganador se lleva 3 meses de premium gratis. ¡Empieza la competición! 🐠🦑🦈",
            null, null, null, null, null);

        crearPublicacion(sofia,
            "Acabo de conseguir la certificación de Rescue Diver de PADI! 🎓 Fue un curso intenso de 4 días pero increíblemente valioso. Ahora puedo ayudar en situaciones de emergencia bajo el agua. El próximo paso: Divemaster. El sueño de convertirme en instructora está cada vez más cerca 💪",
            null, null, null, null, null);

        // ── RESERVAS ─────────────────────────────────────────────────────────
        crearReserva(carlos,  io1, 1, EstadoReserva.CONFIRMADA);
        crearReserva(marina,  ib1, 2, EstadoReserva.CONFIRMADA);
        crearReserva(javier,  ii1, 1, EstadoReserva.COMPLETADA);
        crearReserva(sofia,   io2, 1, EstadoReserva.PENDIENTE);
        crearReserva(pablo,   io4, 1, EstadoReserva.CONFIRMADA);
        crearReserva(lucia,   ii2, 1, EstadoReserva.COMPLETADA);
        crearReserva(carlos,  ib2, 1, EstadoReserva.PENDIENTE);
        crearReserva(marina,  ii3, 2, EstadoReserva.CONFIRMADA);
        crearReserva(javier,  io3, 1, EstadoReserva.COMPLETADA);
        crearReserva(sofia,   ib3, 1, EstadoReserva.PENDIENTE);
        crearReserva(pablo,   ib4, 3, EstadoReserva.CONFIRMADA);
        crearReserva(lucia,   ii4, 1, EstadoReserva.PENDIENTE);

        // ── SEGUIDORES ────────────────────────────────────────────────────────
        seguir(carlos,  javier);
        seguir(carlos,  marina);
        seguir(carlos,  uOcean);
        seguir(marina,  javier);
        seguir(marina,  sofia);
        seguir(marina,  uBlue);
        seguir(javier,  sofia);
        seguir(javier,  lucia);
        seguir(sofia,   carlos);
        seguir(sofia,   lucia);
        seguir(pablo,   carlos);
        seguir(pablo,   javier);
        seguir(pablo,   uOcean);
        seguir(lucia,   marina);
        seguir(lucia,   uIsland);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private Usuario crearAdmin(String username, String email, String bio, String pass) {
        Usuario u = new Usuario();
        u.setUsername(username); u.setEmail(email); u.setPassword(pass);
        u.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        u.setBiografia(bio); u.setActivo(true);
        u.setFotoPerfil("https://ui-avatars.com/api/?name=" + username + "&background=5B21B6&color=fff&size=200");
        return usuarioRepository.save(u);
    }

    private Usuario crearEmpresa(String username, String email, String nombre, String desc,
                                  String dir, String tel, String web, String pass) {
        Usuario u = new Usuario();
        u.setUsername(username); u.setEmail(email); u.setPassword(pass);
        u.setTipoUsuario(TipoUsuario.USUARIO_EMPRESA);
        u.setNombreEmpresa(nombre); u.setDescripcionEmpresa(desc);
        u.setDireccion(dir); u.setTelefono(tel); u.setSitioWeb(web);
        u.setActivo(true);
        u.setFotoPerfil("https://ui-avatars.com/api/?name=" + nombre.replace(" ", "+") + "&background=0077B6&color=fff&size=200");
        return usuarioRepository.save(u);
    }

    private Usuario crearComun(String username, String email, String bio,
                                String nivel, int numInmersiones, String foto, String pass) {
        Usuario u = new Usuario();
        u.setUsername(username); u.setEmail(email); u.setPassword(pass);
        u.setTipoUsuario(TipoUsuario.USUARIO_COMUN);
        u.setBiografia(bio); u.setNivelCertificacion(nivel);
        u.setNumeroInmersiones(numInmersiones); u.setFotoPerfil(foto);
        u.setActivo(true);
        return usuarioRepository.save(u);
    }

    private CentroBuceo crearCentro(Usuario usuario, String nombre, String desc,
                                     String dir, String ciudad, String pais, String region,
                                     String tel, String email, String web, String cert,
                                     String imagen, double valoracion) {
        CentroBuceo c = new CentroBuceo();
        c.setNombre(nombre); c.setDescripcion(desc);
        c.setDireccion(dir); c.setCiudad(ciudad); c.setPais(pais);
        c.setTelefono(tel); c.setEmail(email); c.setSitioWeb(web);
        c.setCertificaciones(cert); c.setImagenUrl(imagen);
        c.setValoracionPromedio(valoracion); c.setActivo(true);
        c.setUsuario(usuario);
        return centroBuceoRepository.save(c);
    }

    private Inmersion crearInmersion(CentroBuceo centro, String titulo, String desc,
                                      int diasDesdeHoy, int duracion, double profundidad,
                                      String nivel, double precio, int plazasTotales, int plazasDisp,
                                      String ubicacion, double lat, double lon,
                                      String equipo, String imagen) {
        Inmersion i = new Inmersion();
        i.setTitulo(titulo); i.setDescripcion(desc);
        i.setFechaInmersion(LocalDateTime.now().plusDays(diasDesdeHoy));
        i.setDuracion(duracion); i.setProfundidadMaxima(profundidad);
        i.setNivelRequerido(nivel); i.setPrecio(precio);
        i.setPlazasTotales(plazasTotales); i.setPlazasDisponibles(plazasDisp);
        i.setUbicacion(ubicacion); i.setLatitud(lat); i.setLongitud(lon);
        i.setEquipoIncluido(equipo); i.setImagenUrl(imagen);
        i.setActivo(true); i.setCentroBuceo(centro);
        return inmersionRepository.save(i);
    }

    private void crearPublicacion(Usuario usuario, String contenido,
                                   String lugar, Double profundidad,
                                   Double temperatura, Double visibilidad, String especies) {
        Publicacion p = new Publicacion();
        p.setUsuario(usuario); p.setContenido(contenido);
        p.setLugarInmersion(lugar); p.setProfundidadMaxima(profundidad);
        p.setTemperaturaAgua(temperatura); p.setVisibilidad(visibilidad);
        p.setEspeciesVistas(especies);
        publicacionRepository.save(p);
    }

    private void crearReserva(Usuario usuario, Inmersion inmersion,
                               int personas, EstadoReserva estado) {
        Reserva r = new Reserva();
        r.setUsuario(usuario); r.setInmersion(inmersion);
        r.setCentroBuceo(inmersion.getCentroBuceo());
        r.setNumeroPersonas(personas);
        r.setPrecioTotal(inmersion.getPrecio() * personas);
        r.setEstado(estado);
        reservaRepository.save(r);

        // Ajustar plazas disponibles
        if (estado != EstadoReserva.CANCELADA) {
            int nuevasPlazas = Math.max(0, inmersion.getPlazasDisponibles() - personas);
            inmersion.setPlazasDisponibles(nuevasPlazas);
            inmersionRepository.save(inmersion);
        }
    }

    private void seguir(Usuario seguidor, Usuario seguido) {
        seguidor.getSiguiendo().add(seguido);
        usuarioRepository.save(seguidor);
    }
}
