package com.good.ivrstand.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Класс, представляющий пользователя в системе.
 */
@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    /**
     * Идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "user_id")
    private Long id;

    /**
     * Имя пользователя. Минимальная длина - 5 символов.
     */
    @Size(min=5, message = "Не меньше 5 знаков")
    @Column(name = "user_name")
    private String username;

    /**
     * Почта пользователя.
     */
    @Getter
    @Column(name = "user_email")
    private String email;

    /**
     * Пароль пользователя. Минимальная длина - 5 символов.
     */
    @Size(min=5, message = "Не меньше 5 знаков")
    @Setter
    @Column(name = "user_pass")
    private String password;

    /**
     * Роли, назначенные пользователю. Связь с таблицей ролей.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Getter
    @JsonIgnore
    private List<Role> roles;

    /**
     * Флаг, указывающий на подтверждение email.
     */
    @Getter
    @Setter
    @Column(name = "user_conf")
    private boolean emailConfirmed;

    /**
     * Токен сброса пользователя.
     */
    @Getter
    @Setter
    @Column(name = "user_reset_token")
    private String resetToken;

    /**
     * Имя пользователя.
     */
    @Getter
    @Setter
    @Column(name = "user_first_name")
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    @Getter
    @Setter
    @Column(name = "user_last_name")
    private String lastName;

    /**
     * Возвращает коллекцию полномочий, предоставленных пользователю.
     * @return Коллекция объектов {@link GrantedAuthority}.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    /**
     * Возвращает пароль пользователя.
     * @return Пароль пользователя.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Возвращает имя пользователя.
     * @return Имя пользователя.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Проверяет, не истек ли срок действия учетной записи пользователя.
     * @return {@code false}, если срок действия учетной записи истек.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Проверяет, не заблокирована ли учетная запись пользователя.
     * @return {@code false}, если учетная запись заблокирована.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Проверяет, не истек ли срок действия учетных данных (пароля) пользователя.
     * @return {@code false}, если срок действия учетных данных истек.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, включена ли учетная запись пользователя.
     * @return {@code false}, если учетная запись отключена.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
