package com.diveconnect.controller;

import com.diveconnect.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadControllerTest {

    private UploadController controller;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        controller = new UploadController();
        tempDir = Files.createTempDirectory("dc-upload-test-");
        ReflectionTestUtils.setField(controller, "uploadDir", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Borrar el directorio temporal recursivamente
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(java.io.File::delete);
        }
    }

    @Test
    @DisplayName("Subida correcta: devuelve URL bajo /uploads/<uuid>.png y guarda el fichero")
    void subidaCorrecta_pngTrivial() throws IOException {
        byte[] pngBytes = new byte[]{
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0,0,0,0x0D,0x49,0x48,0x44,0x52,0,0,0,1,0,0,0,1,8,2,0,0,0,
            (byte)0x90,0x77,0x53,(byte)0xDE,
            0,0,0,0x0C,0x49,0x44,0x41,0x54,8,(byte)0x99,0x63,(byte)0xF8,
            (byte)0xFF,(byte)0xFF,0x3F,0,0x05,(byte)0xFE,0x02,(byte)0xFE,(byte)0xA7,0x35,
            (byte)0x81,(byte)0xD4,0,0,0,0,0x49,0x45,0x4E,0x44,
            (byte)0xAE,0x42,0x60,(byte)0x82
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto-test.png", "image/png", pngBytes);

        ResponseEntity<Map<String, Object>> resp = controller.subirArchivo(file);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat((String) body.get("url")).startsWith("/uploads/").endsWith(".png");
        assertThat(body.get("tipo")).isEqualTo("FOTO");
        assertThat((Long) body.get("sizeBytes")).isPositive();

        String filename = ((String) body.get("filename"));
        Path destFile = tempDir.resolve(filename);
        assertThat(Files.exists(destFile)).isTrue();
        assertThat(Files.size(destFile)).isEqualTo(pngBytes.length);
    }

    @Test
    @DisplayName("Subida con video correcto: devuelve tipo VIDEO")
    void subidaVideo_correctaDevuelveTipoVideo() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "clip.mp4", "video/mp4", new byte[]{0,1,2,3});
        ResponseEntity<Map<String, Object>> resp = controller.subirArchivo(file);
        assertThat(resp.getBody().get("tipo")).isEqualTo("VIDEO");
    }

    @Test
    @DisplayName("Sin fichero: BadRequestException con mensaje claro")
    void sinFichero_lanzaBadRequest() {
        MockMultipartFile empty = new MockMultipartFile("file", "x", "image/png", new byte[]{});
        assertThatThrownBy(() -> controller.subirArchivo(empty))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No se ha enviado");
    }

    @Test
    @DisplayName("MIME no aceptado (text/plain): rechazado")
    void mimeNoAceptado_rechazado() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.txt", "text/plain", "hola".getBytes());
        assertThatThrownBy(() -> controller.subirArchivo(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("imágenes o videos");
    }

    @Test
    @DisplayName("Extensión peligrosa (.exe disfrazada de image/png) rechazada por la lista blanca")
    void extensionFueraListaBlanca_rechazada() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "malo.exe", "image/png", new byte[]{1,2,3});
        assertThatThrownBy(() -> controller.subirArchivo(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Extensión no permitida");
    }
}
