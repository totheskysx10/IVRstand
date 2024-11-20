package com.good.ivrstand.app.repository;

import com.good.ivrstand.domain.Role;
import com.good.ivrstand.domain.enumeration.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(UserRole name);
    Role findByName(UserRole name);
}
