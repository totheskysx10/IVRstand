package com.good.ivrstand.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.good.ivrstand.exception.UserRolesException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
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
     *
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
     *
     * @return false, если срок действия учетной записи истек.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Проверяет, не заблокирована ли учетная запись пользователя.
     *
     * @return false, если учетная запись заблокирована.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Проверяет, не истек ли срок действия учетных данных (пароля) пользователя.
     *
     * @return false, если срок действия учетных данных истек.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, включена ли учетная запись пользователя.
     *
     * @return false, если учетная запись отключена.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Возвращает неизменяемую коллекцию с ролями пользователя
     */
    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    /**
     * Добавляет роль пользователю
     * @param role роль
     * @throws UserRolesException если роль уже есть
     */
    public void addRole(Role role) throws UserRolesException {
        if (roles.contains(role)) {
            throw new UserRolesException("Пользователь уже имеет роль " + role.getName());
        }

        roles.add(role);
    }

    /**
     * Удаляет роль пользователя
     * @param role роль
     * @throws UserRolesException если роли уже нет
     */
    public void removeRole(Role role) throws UserRolesException {
        if (!roles.contains(role)) {
            throw new UserRolesException("Пользователь уже не имеет роли " + role.getName());
        }

        roles.remove(role);
    }
}
