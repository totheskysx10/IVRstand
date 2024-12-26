package com.good.ivrstand.extern.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TokenDTO {

    @NotNull
    @NotBlank
    @JsonProperty("token")
    private final String token;

    @JsonCreator
    public TokenDTO(@JsonProperty("token") String token) {
        this.token = token;
    }
}
