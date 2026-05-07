package com.diveconnect.controller;

import com.diveconnect.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@Slf4j
@CrossOrigin(origins = "*")
public class UploadController {

    private static final List<String> ALLOWED_EXT = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif",
            "mp4", "webm", "mov", "m4v"
    );

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> subirArchivo(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No se ha enviado ningún archivo");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
            throw new BadRequestException("Sólo se aceptan imágenes o videos");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BadRequestException(
                "Extensión no permitida: " + ext + ". Permitidas: " + String.join(", ", ALLOWED_EXT));
        }

        // Crear directorio si no existe
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        // Nombre único basado en UUID para evitar colisiones y path traversal
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dest = dir.resolve(filename);

        // Defensa en profundidad: el destino debe estar dentro del dir
        if (!dest.startsWith(dir)) {
            throw new BadRequestException("Ruta de destino no válida");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = "/uploads/" + filename;
        String tipo = contentType.startsWith("video/") ? "VIDEO" : "FOTO";

        log.info("Archivo subido: {} ({} bytes, {})", filename, file.getSize(), tipo);

        return ResponseEntity.ok(Map.of(
                "url",        url,
                "tipo",       tipo,
                "filename",   filename,
                "sizeBytes",  file.getSize()
        ));
    }
}