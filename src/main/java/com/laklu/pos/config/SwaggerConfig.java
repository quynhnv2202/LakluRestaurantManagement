package com.laklu.pos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;
import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Value("${app.base.url}")
    private String baseUrl;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Server productionServer = new Server();
        productionServer.setUrl("https://api.laklu.com");
        productionServer.setDescription("Production Server");

        Server developmentServer = new Server();
        developmentServer.setUrl("http://localhost:8080");
        developmentServer.setDescription("Development Server");

        return new OpenAPI()
                .info(new Info().title("Laklu POS API")
                        .version("1.0")
                        .description("API documentation for Laklu POS system"))
                .servers(Arrays.asList(productionServer,developmentServer))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
