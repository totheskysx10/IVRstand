package com.good.ivrstand.extern.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotBlank
    @Email
    private String username;

    @NotBlank
    @Size(min = 8, message = "Не меньше 8 знаков")
    @Pattern(regexp = ".*[A-ZА-Я].*", message = "Пароль должен содержать хотя бы одну заглавную букву (русскую или английскую)")
    @Pattern(regexp = ".*[a-zа-я].*", message = "Пароль должен содержать хотя бы одну строчную букву (русскую или английскую)")
    @Pattern(regexp = ".*\\d.*", message = "Пароль должен содержать хотя бы одну цифру")
    private String password;

    @NotBlank
    @Size(min = 8, message = "Не меньше 8 знаков")
    @Pattern(regexp = ".*[A-ZА-Я].*", message = "Пароль должен содержать хотя бы одну заглавную букву (русскую или английскую)")
    @Pattern(regexp = ".*[a-zа-я].*", message = "Пароль должен содержать хотя бы одну строчную букву (русскую или английскую)")
    @Pattern(regexp = ".*\\d.*", message = "Пароль должен содержать хотя бы одну цифру")
    private String passwordConfirm;

    @Size(max = 100)
    @NotBlank
    private String firstName;

    @Size(max = 100)
    @NotBlank
    private String lastName;
}