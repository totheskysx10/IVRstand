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
 * Пользователь в системе.
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
     * Имя пользователя.
     * Обязательно должно быть эл. почтой.
     */
    @Column(name = "user_name")
    private String username;

    /**
     * Пароль пользователя.
     */
    @Setter
    @Column(name = "user_pass")
    private String password;

    /**
     * Роли, назначенные пользователю.
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
     * Токен сброса пароля пользователя.
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
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Возвращает имя пользователя.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Проверяет, не истек ли срок действия учетной записи пользователя.
     * @return false, если срок действия учетной записи истек.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Проверяет, не заблокирована ли учетная запись пользователя.
     * @return false, если учетная запись заблокирована.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Проверяет, не истек ли срок действия учетных данных (пароля) пользователя.
     * @return false, если срок действия учетных данных истек.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, включена ли учетная запись пользователя.
     * @return false, если учетная запись отключена.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
