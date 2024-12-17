package com.good.ivrstand.extern.infrastructure.authentication;

import com.good.ivrstand.app.repository.UserRepository;
import com.good.ivrstand.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Сервис контекста безопасности
 */
@Component
public class SecurityContextService {

    private final UserRepository userRepository;

    public SecurityContextService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Проверяет, авторизован ли в данный момент пользователь с переданным id.
     *
     * @param userId id пользователя
     * @return true, если пользователь с userId в данный момент авторизован
     */
    public boolean isCurrentAuthId(long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object context = authentication.getPrincipal();
            if (context instanceof User contextUser) {
                String username = contextUser.getUsername();

                User user = userRepository.findByUsernameIgnoreCase(username);

                if (user != null) {
                    return user.getId() == userId;
                }
            }
        }
        return false;
    }
}
