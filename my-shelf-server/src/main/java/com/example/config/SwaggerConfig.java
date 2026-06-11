package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation.
 * Swagger UI доступен по адресу: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON доступен по адресу: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My Shelf Server API")
                        .version("1.0.0")
                        .description("API для мобильного приложения 'Моя полка' - организация личного гардероба")
                        .contact(new Contact()
                                .name("My Shelf Team")
                                .url("https://github.com/AstrakovBA/myShelf")));
    }
}
