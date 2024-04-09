package com.good.ivrstand.extern.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "IVRstand",
                description = "Приложенние для управления IVR-стендом", version = "1.0.0",
                contact = @Contact(
                        name = "Команда Хорошая"
                )
        )
)
public class OpenApiConfig {
}
