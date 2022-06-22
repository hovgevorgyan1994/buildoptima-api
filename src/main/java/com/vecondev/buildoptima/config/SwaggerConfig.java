package com.vecondev.buildoptima.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

@OpenAPIDefinition(
    info =
        @Info(
            title = "BUILDOPTIMA API",
            version = "V1",
            description = "PROPERTY INFORMATION MANAGEMENT"))
@SecurityScheme(
    name = "api-security",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER)
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI methodArgumentNotValidResponseBody() {
    return new OpenAPI()
        .components(
            new Components()
                .addResponses(
                    "methodArgumentNotValidResponse",
                    new io.swagger.v3.oas.models.responses.ApiResponse()
                        .description("There is an invalid value in user input.")
                        .content(
                            new Content()
                                .addMediaType(
                                    MediaType.APPLICATION_JSON_VALUE,
                                    new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(
                                            new MapSchema()
                                                .addProperties(
                                                    "status",
                                                    new StringSchema()
                                                        .description("HTTP response status")
                                                        .example("BAD_REQUEST"))
                                                .addProperties(
                                                    "timestamp",
                                                    new ObjectSchema()
                                                        .description(
                                                            "Timestamp showing when the error occurred")
                                                        .example(LocalDateTime.now()))
                                                .addProperties(
                                                    "errors",
                                                    new ArraySchema()
                                                        .description(
                                                            "All validation errors referring to user input")
                                                        .example(
                                                            "The name length should be between 2 and 20!")))))));
  }
}
