package com.good.ivrstand.extern.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginDTO {
    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String username;

    @NotBlank
    @Size(min = 8, message = "Не меньше 8 знаков")
    private String password;
}