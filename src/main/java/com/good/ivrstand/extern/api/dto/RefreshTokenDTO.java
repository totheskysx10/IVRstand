package com.good.ivrstand.extern.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RefreshTokenDTO {

    @NotNull
    @NotBlank
    @JsonProperty("refreshToken")
    private final String refreshToken;

    @JsonCreator
    public RefreshTokenDTO(@JsonProperty("refreshToken") String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
