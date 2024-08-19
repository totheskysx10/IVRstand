package com.good.ivrstand.app;

import com.good.ivrstand.domain.EmailData;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.domain.UserRole;
import com.good.ivrstand.exception.NotConfirmedEmailException;
import com.good.ivrstand.exception.TokenException;
import com.good.ivrstand.exception.UserDuplicateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Сервисный класс для работы с пользователями
 */
@Component
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleService roleService;
    private final EmailService emailService;
    private final MailBuilder mailBuilder;
    private final TokenService tokenService;
    private final EncodeService encodeService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleService roleService, EmailService emailService, MailBuilder mailBuilder, TokenService tokenService, EncodeService encodeService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleService = roleService;
        this.emailService = emailService;
        this.mailBuilder = mailBuilder;
        this.tokenService = tokenService;
        this.encodeService = encodeService;
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::loadUserByUsername;
    }

    /**
     * Загружает пользователя по его email.
     *
     * @param username имя пользователя
     * @return детали пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null)
            throw new UsernameNotFoundException("User not found");

        return user;
    }

    /**
     * Возвращает пользователя по его ID.
     *
     * @param id ID пользователя
     * @return пользователь
     * @throws IllegalArgumentException если пользователь с данным ID не найден
     */
    public User getUserById(long id) {
        User user = userRepository.findById(id);

        if (user == null)
            throw new IllegalArgumentException("Пользователь с id " + id + " не найден");
        else {
            log.debug("Найден пользователь с id {}", id);
            return user;
        }
    }

    /**
     * Создаёт нового пользователя.
     *
     * @param user пользователь
     * @return созданный пользователь
     * @throws IllegalArgumentException если пользователь null
     * @throws UserDuplicateException если пользователь с данным email уже существует
     */
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        User userFromDbEmail = userRepository.findByEmailIgnoreCase(user.getEmail());

        if (userFromDbEmail != null)
            throw new UserDuplicateException("Пользователь с email " + userFromDbEmail.getEmail() + " уже есть в базе!");

        User userFromDbName = userRepository.findByUsernameIgnoreCase(user.getUsername());

        if (userFromDbName != null)
            throw new UserDuplicateException("Пользователь с логином " + userFromDbName.getUsername() + " уже есть в базе!");

        user.getRoles().add(roleService.findRoleByName(UserRole.ROLE_USER));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        User createdUser = userRepository.save(user);
        log.info("Создан пользователь с id {}", createdUser.getId());
        return createdUser;
    }

    /**
     * Удаляет пользователя по его ID.
     *
     * @param userId ID пользователя
     */
    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            log.info("Удалён пользователь с id {}", userId);
        }
    }

    /**
     * Обновляет пароль пользователя.
     *
     * @param userId зашифрованный ID пользователя
     * @param pass новый пароль
     * @param token токен сброса
     */
    public void updatePassword(String userId, String pass, String token) {
        long id = Long.parseLong(encodeService.decrypt(userId));
        User user = getUserById(id);
        if (user.getResetToken().equals(token)) {
            user.setPassword(bCryptPasswordEncoder.encode(pass));
            tokenService.invalidateToken(id);
            userRepository.save(user);
            log.info("Обновлён пароль для пользователя с id {}", id);
        }
        else
            throw new TokenException("Ошибка токена сброса!");
    }

    /**
     * Отправляет сообщение для сброса пароля на email.
     *
     * @param email email пользователя
     * @throws IllegalArgumentException если пользователь с данным email не найден
     */
    public void sendPasswordResetMessage(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);

        if (user == null)
            throw new IllegalArgumentException("Пользователь не может быть null");

        String id = encodeService.encrypt(user.getId().toString());
        String token = tokenService.generateResetPasswordToken();

        EmailData emailData = mailBuilder.buildResetPasswordMessage(user.getEmail(), id, token);
        emailService.sendEmail(emailData);
        tokenService.scheduleTokenInvalidation(user.getId(), 20 * 60 * 1000);
        user.setResetToken(token);
        userRepository.save(user);
        log.info("Отправлена заявка на сброс пароля");
    }

    /**
     * Подтверждает email пользователя.
     *
     * @param userId зашифрованный ID пользователя
     */
    public void confirmEmail(String userId) {
        long id = Long.parseLong(encodeService.decrypt(userId));
        User user = getUserById(id);
        user.setEmailConfirmed(true);
        userRepository.save(user);
        log.info("Подтверждён адрес эл. почты для пользователя с id {}", id);
    }

    /**
     * Отправляет сообщение для подтверждения email на email пользователя.
     *
     * @param email email пользователя
     * @throws IllegalArgumentException если пользователь с данным email не найден
     */
    public void sendConfirmEmailMessage(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);

        if (user == null)
            throw new IllegalArgumentException("Пользователь не может быть null");

        String id = encodeService.encrypt(user.getId().toString());

        EmailData emailData = mailBuilder.buildConfirmEmailMessage(user.getEmail(), id);
        emailService.sendEmail(emailData);
        log.info("Отправлено письмо о подтверждении email");
    }

    /**
     * Назначает пользователю права администратора.
     *
     * @param userId ID пользователя
     * @throws NotConfirmedEmailException если email пользователя не подтвержден
     */
    public void giveAdminRulesToUser(long userId) {
        User user = getUserById(userId);
        if (user != null) {
            if (user.isEmailConfirmed()) {
                if (!user.getRoles().contains(roleService.findRoleByName(UserRole.ROLE_ADMIN))) {
                    user.getRoles().add(roleService.findRoleByName(UserRole.ROLE_ADMIN));
                    userRepository.save(user);
                    log.info("Пользователь с id {} теперь админ", userId);
                }
            } else
                throw new NotConfirmedEmailException("Пользователь без подтверждения Email не может быть админом!");
        }
    }

    /**
     * Убирает у пользователя права администратора.
     *
     * @param userId ID пользователя
     */
    public void removeAdminRulesFromUser(long userId) {
        User user = getUserById(userId);
        if (user != null) {
            if (user.getRoles().contains(roleService.findRoleByName(UserRole.ROLE_ADMIN))) {
                user.getRoles().remove(roleService.findRoleByName(UserRole.ROLE_ADMIN));
                userRepository.save(user);
                log.info("Пользователь с id {} больше не админ", userId);
            }
        }
    }

    /**
     * Обновляет имя пользователя.
     *
     * @param userId  Идентификатор пользователя.
     * @param name Имя.
     */
    public void updateFirstName(long userId, String name) {
        User user = getUserById(userId);
        if (user != null) {
            user.setFirstName(name);
            userRepository.save(user);
            log.info("Имя обновлено для пользователя с id {}", userId);
        }
    }

    /**
     * Обновляет фамилию пользователя.
     *
     * @param userId  Идентификатор пользователя.
     * @param surname Фамилия.
     */
    public void updateLastName(long userId, String surname) {
        User user = getUserById(userId);
        if (user != null) {
            user.setLastName(surname);
            userRepository.save(user);
            log.info("Фамилия обновлена для пользователя с id {}", userId);
        }
    }
}
