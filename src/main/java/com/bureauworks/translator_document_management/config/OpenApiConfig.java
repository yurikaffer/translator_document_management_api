package com.bureauworks.translator_document_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gerenciamento de Documentos e Tradutores")
                        .version("1.0")
                        .description("API Rest para gerenciar documentos, importações de documentos e tradutores"));
    }
}
