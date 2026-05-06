
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `centros_buceo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `certificaciones` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ciudad` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `descripcion` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direccion` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_registro` datetime(6) DEFAULT NULL,
  `imagen_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitud` double DEFAULT NULL,
  `longitud` double DEFAULT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pais` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sitio_web` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefono` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `valoracion_promedio` double DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKc9uphwx8983dibr10pwinxpwp` (`usuario_id`),
  CONSTRAINT `FKhj1thpmfp44bjp5i73v5j7njb` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comentarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contenido` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_comentario` datetime(6) DEFAULT NULL,
  `publicacion_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK69vtiv6pfa3utlb34dbhnsphg` (`publicacion_id`),
  KEY `FKdts62yj83qe3k748cgcjvm48r` (`usuario_id`),
  CONSTRAINT `FK69vtiv6pfa3utlb34dbhnsphg` FOREIGN KEY (`publicacion_id`) REFERENCES `publicaciones` (`id`),
  CONSTRAINT `FKdts62yj83qe3k748cgcjvm48r` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fotos_punto_mapa` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `especie_avistada` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_hora` datetime(6) DEFAULT NULL,
  `fecha_subida` datetime(6) DEFAULT NULL,
  `url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `punto_mapa_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjjr7xnk4esfnf61us1w4pv0px` (`punto_mapa_id`),
  CONSTRAINT `FKjjr7xnk4esfnf61us1w4pv0px` FOREIGN KEY (`punto_mapa_id`) REFERENCES `puntos_mapa` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expira_en` datetime(6) NOT NULL,
  `fecha_publicacion` datetime(6) NOT NULL,
  `media_type` enum('FOTO','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_url` varchar(1500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `texto` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfm5ivk3917sertnr2l8ghgwnv` (`usuario_id`),
  CONSTRAINT `FKfm5ivk3917sertnr2l8ghgwnv` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inmersiones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL DEFAULT b'1',
  `descripcion` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `duracion` int DEFAULT NULL,
  `equipo_incluido` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_inmersion` datetime(6) NOT NULL,
  `imagen_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitud` double DEFAULT NULL,
  `longitud` double DEFAULT NULL,
  `nivel_requerido` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `plazas_disponibles` int DEFAULT NULL,
  `plazas_totales` int DEFAULT NULL,
  `precio` double DEFAULT NULL,
  `profundidad_maxima` double DEFAULT NULL,
  `titulo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ubicacion` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `centro_buceo_id` bigint NOT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `FKcwe0o22hkreub22alxsok1id4` (`centro_buceo_id`),
  CONSTRAINT `FKcwe0o22hkreub22alxsok1id4` FOREIGN KEY (`centro_buceo_id`) REFERENCES `centros_buceo` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `likes_publicacion` (
  `publicacion_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`publicacion_id`,`usuario_id`),
  KEY `FKefyek7smj1nnb3uxstg2di03r` (`usuario_id`),
  CONSTRAINT `FKefyek7smj1nnb3uxstg2di03r` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKmbvtawjdpudivnf3aurv0r1eq` FOREIGN KEY (`publicacion_id`) REFERENCES `publicaciones` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificaciones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accionable` bit(1) NOT NULL,
  `entidad_relacionada_id` bigint DEFAULT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `leida` bit(1) NOT NULL,
  `mensaje` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `resuelta` bit(1) NOT NULL,
  `tipo` enum('SOLICITUD_SEGUIMIENTO','SEGUIMIENTO_ACEPTADO','SEGUIMIENTO_RECHAZADO','NUEVO_SEGUIDOR','LIKE_PUBLICACION','COMENTARIO_PUBLICACION','RESERVA_CONFIRMADA','RESERVA_RECIBIDA','MENCION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `destinatario_id` bigint NOT NULL,
  `emisor_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notif_destinatario` (`destinatario_id`),
  KEY `idx_notif_leida` (`leida`),
  KEY `FK93ejqhq3va6nhwfnpat6mxl52` (`emisor_id`),
  CONSTRAINT `FK93ejqhq3va6nhwfnpat6mxl52` FOREIGN KEY (`emisor_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKrxqukj4ow44h862qlbgl54ij2` FOREIGN KEY (`destinatario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `publicaciones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contenido` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `especies_vistas` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_publicacion` datetime(6) DEFAULT NULL,
  `imagen_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lugar_inmersion` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numero_comentarios` int DEFAULT NULL,
  `numero_likes` int DEFAULT NULL,
  `profundidad_maxima` double DEFAULT NULL,
  `temperatura_agua` double DEFAULT NULL,
  `video_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `visibilidad` double DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcuualw35fb3065r7mjiijb898` (`usuario_id`),
  CONSTRAINT `FKcuualw35fb3065r7mjiijb898` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `puntos_mapa` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `corriente` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `descripcion` varchar(1500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `especies_vistas` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_observacion` datetime(6) DEFAULT NULL,
  `latitud` double NOT NULL,
  `longitud` double NOT NULL,
  `presion_bar` double DEFAULT NULL,
  `profundidad_metros` double DEFAULT NULL,
  `temperatura_agua` double DEFAULT NULL,
  `titulo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ultima_modificacion` datetime(6) DEFAULT NULL,
  `visibilidad_metros` double DEFAULT NULL,
  `autor_id` bigint NOT NULL,
  `inmersion_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi2skfdo46jlgbx0tkgnth83gi` (`autor_id`),
  KEY `FK51gc3uebxfmgc6hrwjfsrdllm` (`inmersion_id`),
  CONSTRAINT `FK51gc3uebxfmgc6hrwjfsrdllm` FOREIGN KEY (`inmersion_id`) REFERENCES `inmersiones` (`id`),
  CONSTRAINT `FKi2skfdo46jlgbx0tkgnth83gi` FOREIGN KEY (`autor_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` enum('CANCELADA','COMPLETADA','CONFIRMADA','PENDIENTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_reserva` datetime(6) DEFAULT NULL,
  `numero_personas` int NOT NULL,
  `observaciones` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `precio_total` double NOT NULL,
  `ultima_modificacion` datetime(6) DEFAULT NULL,
  `centro_buceo_id` bigint NOT NULL,
  `inmersion_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `payment_status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stripe_payment_intent_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stripe_session_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `paypal_capture_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `paypal_order_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK99smb1tpc0loxnekjsttqyfgi` (`centro_buceo_id`),
  KEY `FKeexymmdtbqomo9kvhm6yee7jo` (`inmersion_id`),
  KEY `FKcfh7qcr7oxomqk5hhbxdg2m7p` (`usuario_id`),
  CONSTRAINT `FK99smb1tpc0loxnekjsttqyfgi` FOREIGN KEY (`centro_buceo_id`) REFERENCES `centros_buceo` (`id`),
  CONSTRAINT `FKcfh7qcr7oxomqk5hhbxdg2m7p` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKeexymmdtbqomo9kvhm6yee7jo` FOREIGN KEY (`inmersion_id`) REFERENCES `inmersiones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seguidores` (
  `seguidor_id` bigint NOT NULL,
  `seguido_id` bigint NOT NULL,
  PRIMARY KEY (`seguidor_id`,`seguido_id`),
  KEY `FKn3gaite1jrccpntbv0p8k41l1` (`seguido_id`),
  CONSTRAINT `FKn3gaite1jrccpntbv0p8k41l1` FOREIGN KEY (`seguido_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKpsipyefvlvyu4fho9j9lp3a7u` FOREIGN KEY (`seguidor_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `solicitudes_seguimiento` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` enum('PENDIENTE','ACEPTADA','RECHAZADA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `fecha_respuesta` datetime(6) DEFAULT NULL,
  `destinatario_id` bigint NOT NULL,
  `solicitante_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sol_dest` (`destinatario_id`),
  KEY `idx_sol_solic` (`solicitante_id`),
  KEY `idx_sol_estado` (`estado`),
  CONSTRAINT `FK79ks32xlybblhm09u76d5qu1l` FOREIGN KEY (`destinatario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKb708v0yneq72fsg63g3x361am` FOREIGN KEY (`solicitante_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `biografia` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `descripcion_empresa` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direccion` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_registro` datetime(6) DEFAULT NULL,
  `foto_perfil` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nivel_certificacion` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre_empresa` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numero_inmersiones` int DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sitio_web` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefono` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tipo_usuario` enum('ADMINISTRADOR','USUARIO_COMUN','USUARIO_EMPRESA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `ultima_actualizacion` datetime(6) DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`),
  UNIQUE KEY `UKm2dvbwfge291euvmk6vkkocao` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

