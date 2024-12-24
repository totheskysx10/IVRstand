package com.good.ivrstand.app.repository;

import com.good.ivrstand.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsernameIgnoreCase(String name);
    User findById(long id);
}
