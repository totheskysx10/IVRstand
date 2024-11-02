package com.good.ivrstand.app;

import com.good.ivrstand.domain.Role;
import com.good.ivrstand.domain.UserRole;
import org.springframework.stereotype.Component;

/**
 * Сервис для работы с ролями
 */
@Component
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Проверяет наличие ролей ROLE_USER и ROLE_ADMIN в базе данных,
     * и создает их, если они не существуют.
     */
    private void checkRoles() {
        if (!roleRepository.existsByName(UserRole.ROLE_USER)) {
            Role roleUser = Role.builder()
                    .name(UserRole.ROLE_USER)
                    .build();
            roleRepository.save(roleUser);
        }

        if (!roleRepository.existsByName(UserRole.ROLE_ADMIN)) {
            Role roleAdmin = Role.builder()
                    .name(UserRole.ROLE_ADMIN)
                    .build();
            roleRepository.save(roleAdmin);
        }
    }

    /**
     * Находит роль по ее имени. Если роли ROLE_USER и ROLE_ADMIN отсутствуют, они будут созданы.
     *
     * @param name имя роли
     * @return найденная роль
     */
    public Role findRoleByName(UserRole name) {
        checkRoles();
        return roleRepository.findByName(name);
    }
}
