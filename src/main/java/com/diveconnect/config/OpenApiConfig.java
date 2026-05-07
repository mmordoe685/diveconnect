package com.diveconnect.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI diveConnectOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DiveConnect REST API")
                        .description("API REST de DiveConnect: red social y plataforma de reservas para la comunidad submarinista.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Marcos Mordoñez")
                                .email("mmordoe685@g.educaand.es")
                                .url("https://github.com/mmordoe685/diveconnect"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/license/mit")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://diveconnect.onrender.com").description("Producción")
                ))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtenido en /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
    }
}
