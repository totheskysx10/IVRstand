package com.good.ivrstand.extern.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDTO {
    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String username;

    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String password;
}