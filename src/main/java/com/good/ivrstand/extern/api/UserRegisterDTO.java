package com.good.ivrstand.extern.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String username;

    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String password;

    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String passwordConfirm;

    @Size(max = 100)
    @NotBlank
    private String firstName;

    @Size(max = 100)
    @NotBlank
    private String lastName;
}