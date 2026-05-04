package com.diveconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirigir raíz a index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos subidos desde el directorio configurado.
        // Aseguramos que la ruta termine con separador y forzamos prefijo "file:"
        // ya que Spring lo necesita para tratarla como sistema de ficheros.
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String absPath = uploadPath.toString().replace('\\', '/');
        if (!absPath.endsWith("/")) absPath = absPath + "/";
        String location = "file:" + absPath;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
