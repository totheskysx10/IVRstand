package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.app.service.UserService;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.exception.NotConfirmedEmailException;
import com.good.ivrstand.exception.ResetPasswordTokenException;
import com.good.ivrstand.exception.UserRolesException;
import com.good.ivrstand.exception.notfound.UserNotFoundException;
import com.good.ivrstand.extern.api.assembler.UserAssembler;
import com.good.ivrstand.extern.api.dto.UserDTO;
import com.good.ivrstand.extern.api.dto.UserUpdatePasswordDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "UserController", description = "Контроллер для управления пользователями")
public class UserController {

    private final UserService userService;
    private final UserAssembler userAssembler;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController(UserService userService, UserAssembler userAssembler, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.userAssembler = userAssembler;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Operation(summary = "Обновить пароль", description = "Обновляет пароль пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации токена"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@RequestParam String userId, @RequestParam String token, @Valid @RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        if (!userUpdatePasswordDTO.getPassword().equals(userUpdatePasswordDTO.getPasswordConfirm())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String encodedPass = bCryptPasswordEncoder.encode(userUpdatePasswordDTO.getPassword());
            userService.updatePassword(userId, encodedPass, token);
            return ResponseEntity.ok().build();
        } catch (ResetPasswordTokenException ex) {
            return ResponseEntity.badRequest().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Запросить сброс пароля", description = "Отправляет сообщение для сброса пароля.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сообщение успешно отправлено"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> sendPasswordResetMessage(@RequestParam String email) {
        try {
            userService.sendPasswordResetMessage(email);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Подтвердить email", description = "Подтверждает email пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email успешно подтвержден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(@RequestParam String userId) {
        try {
            userService.confirmEmail(userId);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Запросить подтверждение email", description = "Отправляет сообщение для подтверждения email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сообщение успешно отправлено"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/request-confirm-email")
    public ResponseEntity<Void> sendConfirmEmailMessage(@RequestParam String email) {
        try {
            userService.sendConfirmEmailMessage(email);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Получить пользователя по ID", description = "Получает информацию о пользователе по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(userAssembler.toModel(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по его идентификатору.")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно удален")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Назначить администратора", description = "Назначает пользователя администратором.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно назначен администратором"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже админ"),
            @ApiResponse(responseCode = "403", description = "Email не подтвержден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/admin/{userId}")
    public ResponseEntity<String> giveAdminRulesToUser(@PathVariable long userId) {
        try {
            userService.giveAdminRulesToUser(userId);
            return ResponseEntity.ok().build();
        } catch (NotConfirmedEmailException e) {
            return new ResponseEntity<>("Почта не подтверждена", HttpStatus.FORBIDDEN);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserRolesException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Снять права администратора", description = "Снимает с пользователя права администратора.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Права администратора успешно сняты"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже не админ"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/no-admin/{userId}")
    public ResponseEntity<Void> removeAdminRulesFromUser(@PathVariable long userId) {
        try {
            userService.removeAdminRulesFromUser(userId);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserRolesException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Обновить имя", description = "Обновляет имя пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Имя успешно обновлено"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/name/{userId}")
    public ResponseEntity<Void> updateFirstName(@PathVariable long userId, @RequestParam String name) {
        try {
            userService.updateFirstName(userId, name);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Обновить фамилию", description = "Обновляет фамилию пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фамилия успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/surname/{userId}")
    public ResponseEntity<Void> updateLastName(@PathVariable long userId, @RequestParam String surname) {
        try {
            userService.updateLastName(userId, surname);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
