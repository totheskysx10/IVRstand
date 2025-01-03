package com.good.ivrstand.app.service;

import com.good.ivrstand.app.repository.UserRepository;
import com.good.ivrstand.app.service.externinterfaces.EmailService;
import com.good.ivrstand.domain.enumeration.EmailData;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.domain.enumeration.UserRole;
import com.good.ivrstand.exception.NotConfirmedEmailException;
import com.good.ivrstand.exception.ResetPasswordTokenException;
import com.good.ivrstand.exception.UserDuplicateException;
import com.good.ivrstand.exception.UserRolesException;
import com.good.ivrstand.exception.notfound.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Сервис для работы с пользователями
 */
@Component
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final EncodeService encodeService;
    private final String resetPasswordLink;
    private final String confirmEmailLink;

    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       EmailService emailService,
                       TokenService tokenService,
                       EncodeService encodeService,
                       @Value("${auth.reset-password.link}") String resetPasswordLink,
                       @Value("${auth.confirm-email.link}") String confirmEmailLink) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.encodeService = encodeService;
        this.resetPasswordLink = resetPasswordLink;
        this.confirmEmailLink = confirmEmailLink;
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this;
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
        User user = userRepository.findByUsernameIgnoreCase(username);

        if (user == null)
            throw new UsernameNotFoundException("Пользователь с username " + username + " не найден");

        return user;
    }

    /**
     * Возвращает пользователя по его ID.
     *
     * @param id ID пользователя
     * @return пользователь
     * @throws UserNotFoundException если пользователь с данным ID не найден
     */
    public User getUserById(long id) throws UserNotFoundException {
        User user = userRepository.findById(id);

        if (user == null)
            throw new UserNotFoundException("Пользователь с id " + id + " не найден");

        log.debug("Найден пользователь с id {}", id);
        return user;

    }

    /**
     * Создаёт нового пользователя.
     *
     * @param user пользователь
     * @return созданный пользователь
     * @throws IllegalArgumentException если пользователь null
     * @throws UserDuplicateException   если пользователь с данным email уже существует
     */
    public User createUser(User user) throws UserDuplicateException, UserRolesException {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        User userFromDb = userRepository.findByUsernameIgnoreCase(user.getUsername());

        if (userFromDb != null)
            throw new UserDuplicateException("Пользователь с логином " + userFromDb.getUsername() + " уже есть в базе!");

        user.addRole(roleService.findRoleByName(UserRole.ROLE_USER));
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
     * @param userId      зашифрованный ID пользователя
     * @param encodedPass новый пароль
     * @param token       токен сброса
     */
    public void updatePassword(String userId, String encodedPass, String token) throws ResetPasswordTokenException, UserNotFoundException {
        long id = Long.parseLong(encodeService.decrypt(userId));
        User user = getUserById(id);
        if (user.getResetToken().equals(token)) {
            user.setPassword(encodedPass);
            tokenService.invalidateToken(id);
            userRepository.save(user);
            log.info("Обновлён пароль для пользователя с id {}", id);
        } else
            throw new ResetPasswordTokenException("Ошибка токена сброса пароля!");
    }

    /**
     * Создаёт токен сброса пароля.
     * Отправляет сообщение для сброса пароля на email.
     *
     * @param email email пользователя
     */
    public void sendPasswordResetMessage(String email) throws UserNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(email);

        if (user == null)
            throw new UserNotFoundException("Пользователь не может быть null");

        String id = encodeService.encrypt(user.getId().toString());
        String token = tokenService.generateResetPasswordToken();

        String message = String.format(EmailData.RESET_PASSWORD.getEmailMessage(), resetPasswordLink, id, token);
        emailService.sendEmail(email, EmailData.RESET_PASSWORD.getEmailSubject(), message);
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
    public void confirmEmail(String userId) throws UserNotFoundException {
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
     * @throws UserNotFoundException если пользователь с данным email не найден
     */
    public void sendConfirmEmailMessage(String email) throws UserNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(email);

        if (user == null)
            throw new UserNotFoundException("Пользователь не может быть null");

        String id = encodeService.encrypt(user.getId().toString());

        String message = String.format(EmailData.CONFIRM_EMAIL.getEmailMessage(), confirmEmailLink, id);
        emailService.sendEmail(email, EmailData.CONFIRM_EMAIL.getEmailSubject(), message);
        log.info("Отправлено письмо о подтверждении email");
    }

    /**
     * Назначает пользователю права администратора.
     *
     * @param userId ID пользователя
     * @throws NotConfirmedEmailException если email пользователя не подтвержден
     */
    public void giveAdminRulesToUser(long userId) throws NotConfirmedEmailException, UserNotFoundException, UserRolesException {
        User user = getUserById(userId);
        if (!user.isEmailConfirmed()) {
            throw new NotConfirmedEmailException("Пользователь без подтверждения Email не может быть админом!");
        }

        user.addRole(roleService.findRoleByName(UserRole.ROLE_ADMIN));
        userRepository.save(user);
        log.info("Пользователь с id {} теперь админ", userId);
    }

    /**
     * Убирает у пользователя права администратора.
     *
     * @param userId ID пользователя
     */
    public void removeAdminRulesFromUser(long userId) throws UserNotFoundException, UserRolesException {
        User user = getUserById(userId);
        user.removeRole(roleService.findRoleByName(UserRole.ROLE_ADMIN));
        userRepository.save(user);
        log.info("Пользователь с id {} больше не админ", userId);
    }

    /**
     * Обновляет имя пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @param name   Имя.
     */
    public void updateFirstName(long userId, String name) throws UserNotFoundException {
        User user = getUserById(userId);
        user.setFirstName(name);
        userRepository.save(user);
        log.info("Имя обновлено для пользователя с id {}", userId);
    }

    /**
     * Обновляет фамилию пользователя.
     *
     * @param userId  Идентификатор пользователя.
     * @param surname Фамилия.
     */
    public void updateLastName(long userId, String surname) throws UserNotFoundException {
        User user = getUserById(userId);
        user.setLastName(surname);
        userRepository.save(user);
        log.info("Фамилия обновлена для пользователя с id {}", userId);
    }
}
