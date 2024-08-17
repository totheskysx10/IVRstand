package com.good.ivrstand.extern.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdatePasswordDTO {

    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String password;

    @NotBlank
    @Size(min = 5, message = "Не меньше 5 знаков")
    private String passwordConfirm;
}
