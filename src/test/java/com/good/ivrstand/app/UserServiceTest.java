package com.good.ivrstand.app;

import com.good.ivrstand.app.repository.UserRepository;
import com.good.ivrstand.app.service.*;
import com.good.ivrstand.app.service.externinterfaces.EmailService;
import com.good.ivrstand.domain.Role;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.domain.enumeration.UserRole;
import com.good.ivrstand.exception.NotConfirmedEmailException;
import com.good.ivrstand.exception.ResetPasswordTokenException;
import com.good.ivrstand.exception.UserDuplicateException;
import com.good.ivrstand.exception.UserRolesException;
import com.good.ivrstand.exception.notfound.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private EncodeService encodeService;

    @BeforeEach
    public void setUp() {
        this.userService = new UserService(
                userRepository,
                roleService,
                emailService,
                tokenService,
                encodeService,
                "reset",
                "confirm"
        );
    }

    @Test
    public void loadUserByUsernameTest() {
        User user = User.builder()
                .username("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .build();

        when(userRepository.findByUsernameIgnoreCase("test@example.com")).thenReturn(user);
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertEquals("test@example.com", userDetails.getUsername());
    }

    @Test
    public void getUserByIdTest() throws UserNotFoundException {
        User user = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .build();

        when(userRepository.findById(1)).thenReturn(user);
        User userFound = userService.getUserById(1);

        assertEquals("test@example.com", userFound.getUsername());
    }

    @Test
    public void createUserTest() throws UserDuplicateException, UserRolesException {
        User user1 = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        Role roleUser = Role.builder()
                .name(UserRole.ROLE_USER)
                .build();

        user2.addRole(roleUser);

        when(roleService.findRoleByName(UserRole.ROLE_USER)).thenReturn(roleUser);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.findByUsernameIgnoreCase(user1.getUsername())).thenReturn(null);
        when(userRepository.save(user1)).thenReturn(user1);
        User createdUser1 = userService.createUser(user1);

        when(userRepository.findByUsernameIgnoreCase(user1.getUsername())).thenReturn(user2);
        when(userRepository.save(user1)).thenReturn(user1);

        assertNotNull(createdUser1);
        assertEquals("test@example.com", createdUser1.getUsername());

        when(userRepository.findByUsernameIgnoreCase(user2.getUsername())).thenReturn(user2);

        UserDuplicateException exceptionEmail = assertThrows(UserDuplicateException.class, () -> userService.createUser(user2));

        assertEquals("Пользователь с логином test@example.com уже есть в базе!", exceptionEmail.getMessage());
    }

    @Test
    public void deleteUserTest() {
        User user = User.builder()
                .id(1L)
                .username("min@list.ru")
                .username("test")
                .build();

        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void updatePasswordTest() throws ResetPasswordTokenException, UserNotFoundException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("token")
                .build();

        User user2 = User.builder()
                .id(20L)
                .username("min@list.ru")
                .password("test")
                .resetToken("no-token")
                .build();

        when(userRepository.findById(23)).thenReturn(user1);
        when(userRepository.findById(20)).thenReturn(user2);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(encodeService.decrypt("mCUMoT5ilyKYdeOa8iFI+w==")).thenReturn("23");
        when(encodeService.decrypt("ykcfU3ayqLU9YpCFhDtu+A==")).thenReturn("20");

        userService.updatePassword("mCUMoT5ilyKYdeOa8iFI+w==", "encodedPassword", "token");

        ResetPasswordTokenException exception = assertThrows(ResetPasswordTokenException.class, () ->
                userService.updatePassword("ykcfU3ayqLU9YpCFhDtu+A==", "encodedPassword", "token"));

        assertEquals("Ошибка токена сброса пароля!", exception.getMessage());
        assertEquals("encodedPassword", user1.getPassword());
    }

    @Test
    public void sendPasswordResetMessageTest() throws UserNotFoundException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("token")
                .build();

        when(userRepository.findByUsernameIgnoreCase("min@list.ru")).thenReturn(user1);
        when(tokenService.generateResetPasswordToken()).thenReturn("token1");
        when(encodeService.encrypt("23")).thenReturn("mCUMoT5ilyKYdeOa8iFI+w==");

        userService.sendPasswordResetMessage(user1.getUsername());

        verify(emailService, times(1)).sendEmail("min@list.ru",
                "IVRstand - Восстановление пароля",
                """
                        <html>
                            <body>
                                <p>Чтобы сменить пароль и восстановить доступ, пройдите по ссылке (действует в течение 20 минут):</p>
                                <a href="resetmCUMoT5ilyKYdeOa8iFI+w==&token=token1">Сбросить пароль</a>
                            </body>
                        </html>""");
    }

    @Test
    public void sendConfirmEmailMessageTest() throws UserNotFoundException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("token")
                .build();

        when(userRepository.findByUsernameIgnoreCase("min@list.ru")).thenReturn(user1);
        when(encodeService.encrypt("23")).thenReturn("mCUMoT5ilyKYdeOa8iFI+w==");

        userService.sendConfirmEmailMessage(user1.getUsername());

        verify(emailService, times(1)).sendEmail("min@list.ru", "IVRstand - Подтверждение почты",
                """
                        <html>
                            <body>
                                <p>Чтобы подтвердить адрес электронной почты, пройдите по ссылке:</p>
                                <a href="confirmmCUMoT5ilyKYdeOa8iFI+w==">Подтвердить</a>
                            </body>
                        </html>""");
    }

    @Test
    public void confirmEmailTest() throws UserNotFoundException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(false)
                .build();

        when(userRepository.findById(23)).thenReturn(user1);
        when(encodeService.decrypt("mCUMoT5ilyKYdeOa8iFI+w==")).thenReturn("23");

        userService.confirmEmail("mCUMoT5ilyKYdeOa8iFI+w==");

        assertTrue(user1.isEmailConfirmed());
    }

    @Test
    public void giveAdminRulesToUserTest() throws NotConfirmedEmailException, UserNotFoundException, UserRolesException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(false)
                .roles(new ArrayList<>())
                .build();

        User user2 = User.builder()
                .id(24L)
                .username("min@list.ru")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        when(userRepository.findById(23)).thenReturn(user1);
        when(userRepository.findById(24)).thenReturn(user2);
        Role roleUser = Role.builder()
                .name(UserRole.ROLE_USER)
                .build();
        Role roleAdmin = Role.builder()
                .name(UserRole.ROLE_ADMIN)
                .build();

        user1.addRole(roleUser);
        user2.addRole(roleUser);

        when(roleService.findRoleByName(UserRole.ROLE_USER)).thenReturn(roleUser);
        when(roleService.findRoleByName(UserRole.ROLE_ADMIN)).thenReturn(roleAdmin);

        userService.giveAdminRulesToUser(24);

        NotConfirmedEmailException exception = assertThrows(NotConfirmedEmailException.class, () -> userService.giveAdminRulesToUser(23));

        assertEquals("Пользователь без подтверждения Email не может быть админом!", exception.getMessage());
        assertEquals(2, user2.getRoles().size());
    }

    @Test
    public void giveAdminRulesToAdminTest() throws UserRolesException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        when(userRepository.findById(23)).thenReturn(user1);
        Role roleAdmin = Role.builder()
                .name(UserRole.ROLE_ADMIN)
                .build();

        user1.addRole(roleAdmin);

        when(roleService.findRoleByName(UserRole.ROLE_ADMIN)).thenReturn(roleAdmin);

        Exception exception = assertThrows(UserRolesException.class, () -> userService.giveAdminRulesToUser(23));

        assertEquals("Пользователь уже имеет роль ROLE_ADMIN", exception.getMessage());
    }

    @Test
    public void removeAdminRulesFromUserTest() throws UserNotFoundException, UserRolesException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        when(userRepository.findById(23)).thenReturn(user1);
        Role roleUser = Role.builder()
                .name(UserRole.ROLE_USER)
                .build();
        Role roleAdmin = Role.builder()
                .name(UserRole.ROLE_ADMIN)
                .build();

        user1.addRole(roleUser);
        user1.addRole(roleAdmin);

        when(roleService.findRoleByName(UserRole.ROLE_ADMIN)).thenReturn(roleAdmin);
        when(roleService.findRoleByName(UserRole.ROLE_ADMIN)).thenReturn(roleUser);

        userService.removeAdminRulesFromUser(23);

        assertEquals(1, user1.getRoles().size());
    }

    @Test
    public void removeAdminRulesFromNotAdminTest() throws UserRolesException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        when(userRepository.findById(23)).thenReturn(user1);
        Role roleUser = Role.builder()
                .name(UserRole.ROLE_USER)
                .build();
        Role roleAdmin = Role.builder()
                .name(UserRole.ROLE_ADMIN)
                .build();

        user1.addRole(roleUser);

        when(roleService.findRoleByName(UserRole.ROLE_USER)).thenReturn(roleUser);
        when(roleService.findRoleByName(UserRole.ROLE_ADMIN)).thenReturn(roleAdmin);

        Exception exception = assertThrows(UserRolesException.class, () -> userService.removeAdminRulesFromUser(23));

        assertEquals("Пользователь уже не имеет роли ROLE_ADMIN", exception.getMessage());
    }

    @Test
    public void updateFirstNameTest() throws UserNotFoundException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .firstName("name")
                .lastName("surname")
                .resetToken("no-token")
                .emailConfirmed(true)
                .build();

        when(userRepository.findById(23)).thenReturn(user1);

        userService.updateFirstName(23, "fname");

        assertEquals("fname", user1.getFirstName());
    }

    @Test
    public void updateLastNameTest() throws UserNotFoundException {
        User user1 = User.builder()
                .id(23L)
                .username("min@list.ru")
                .password("test")
                .firstName("name")
                .lastName("surname")
                .resetToken("no-token")
                .emailConfirmed(true)
                .build();

        when(userRepository.findById(23)).thenReturn(user1);

        userService.updateLastName(23, "lname");

        assertEquals("lname", user1.getLastName());
    }
}
