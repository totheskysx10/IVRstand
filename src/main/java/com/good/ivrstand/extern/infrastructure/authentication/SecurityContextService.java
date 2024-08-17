package com.good.ivrstand.extern.infrastructure.authentication;

import com.good.ivrstand.app.UserRepository;
import com.good.ivrstand.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextService {

    private final UserRepository userRepository;

    public SecurityContextService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isCurrentAuthId(long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object context = authentication.getPrincipal();
            if (context instanceof User) {
                User contextUser = (User)context;
                String username = contextUser.getEmail();

                User user = userRepository.findByEmailIgnoreCase(username);

                if (user != null) {
                    return user.getId() == userId;
                }
            }
        }
        return false;
    }
}
