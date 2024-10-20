package com.good.ivrstand.extern.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RefreshAndAuthTokenDTO extends TokenDTO {
    private final String refreshToken;

    @JsonCreator
    public RefreshAndAuthTokenDTO(@JsonProperty("token") String token, @JsonProperty("refreshToken") String refreshToken) {
        super(token);
        this.refreshToken = refreshToken;
    }
}
