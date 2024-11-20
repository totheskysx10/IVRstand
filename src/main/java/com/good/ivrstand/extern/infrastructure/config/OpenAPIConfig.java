package com.good.ivrstand.extern.infrastructure.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Конфигурация Swagger.
 */
@Configuration
public class OpenAPIConfig {

    @Value("${openapi-requests.url}")
    private String url;

    /**
     * Конфигурирует настройки Swagger.
     */
    @Bean
    public OpenAPI myOpenAPI() {
        Server server = new Server();
        server.setUrl(url);

        Contact contact = new Contact();
        contact.setName("Команда Хорошая");

        Info info = new Info()
                .title("IVRstand")
                .version("1.0.0")
                .contact(contact)
                .description("Приложенние для управления IVR-стендом");

        return new OpenAPI().info(info).servers(List.of(server));
    }
}
