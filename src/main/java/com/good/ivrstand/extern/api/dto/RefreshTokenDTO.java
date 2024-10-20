package com.good.ivrstand.extern.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RefreshTokenDTO {

    @JsonProperty("refreshToken")
    private final String refreshToken;

    @JsonCreator
    public RefreshTokenDTO(@JsonProperty("refreshToken") String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
