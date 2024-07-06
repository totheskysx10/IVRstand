package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Класс, представляющий роль в системе.
 */
@Entity
@Table(name = "roles")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role implements GrantedAuthority {

    /**
     * Идентификатор роли.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "role_id")
    private Long id;

    /**
     * Название роли.
     */
    @Getter
    @Setter
    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    private UserRole name;

    /**
     * Пользователи, связанные с этой ролью.
     * Поле не сохраняется в базе данных.
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    @Getter
    private List<User> users;

    /**
     * Возвращает название роли в качестве предоставляемого полномочия.
     * @return Название роли.
     */
    @Override
    public String getAuthority() {
        return getName().toString();
    }
}
