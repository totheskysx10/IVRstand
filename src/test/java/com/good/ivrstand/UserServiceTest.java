package com.good.ivrstand;

import com.good.ivrstand.app.*;
import com.good.ivrstand.domain.EmailData;
import com.good.ivrstand.domain.Role;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.domain.UserRole;
import com.good.ivrstand.exception.NotConfirmedEmailException;
import com.good.ivrstand.exception.TokenException;
import com.good.ivrstand.exception.UserDuplicateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @InjectMocks
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
    private MailBuilder mailBuilder;


    @Test
    public void loadUserByUsernameTest() {
        User user = User.builder()
                .username("User")
                .email("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(user);
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertEquals("User", userDetails.getUsername());
    }

    @Test
    public void getUserByIdTest() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .build();

        when(userRepository.findById(1)).thenReturn(user);
        User userFound = userService.getUserById(1);

        assertEquals("test@example.com", userFound.getEmail());
    }

    @Test
    public void createUserTest() {
        User user1 = User.builder()
                .id(1L)
                .username("User")
                .email("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("User1")
                .email("test@example.com")
                .password("test")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        User user3 = User.builder()
                .id(3L)
                .email("test2@example.com")
                .username("User")
                .password("test")
                .emailConfirmed(true)
                .roles(new ArrayList<>())
                .build();

        Role roleUser = Role.builder()
                .name(UserRole.ROLE_USER)
                .build();

        user1.getRoles().add(roleUser);
        user2.getRoles().add(roleUser);
        user3.getRoles().add(roleUser);

        when(roleService.findRoleByName(UserRole.ROLE_USER)).thenReturn(roleUser);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");

        when(userRepository.findByEmailIgnoreCase(user1.getEmail())).thenReturn(null);
        when(userRepository.findByUsernameIgnoreCase(user1.getUsername())).thenReturn(null);
        when(userRepository.save(user1)).thenReturn(user1);

        User createdUser1 = userService.createUser(user1);

        assertNotNull(createdUser1);
        assertEquals("test@example.com", createdUser1.getEmail());

        when(userRepository.findByEmailIgnoreCase(user2.getEmail())).thenReturn(user1);
        when(userRepository.findByUsernameIgnoreCase(user2.getUsername())).thenReturn(null);

        UserDuplicateException exceptionEmail = assertThrows(UserDuplicateException.class, () -> {
            userService.createUser(user2);
        });

        assertEquals("Пользователь с email test@example.com уже есть в базе!", exceptionEmail.getMessage());

        when(userRepository.findByEmailIgnoreCase(user3.getEmail())).thenReturn(null);
        when(userRepository.findByUsernameIgnoreCase(user3.getUsername())).thenReturn(user1);

        UserDuplicateException exceptionUsername = assertThrows(UserDuplicateException.class, () -> {
            userService.createUser(user3);
        });

        assertEquals("Пользователь с логином User уже есть в базе!", exceptionUsername.getMessage());
    }

    @Test
    public void deleteUserTest() {
        User user = User.builder()
                .id(1L)
                .email("min@list.ru")
                .username("test")
                .build();

        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void updatePasswordTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("token")
                .build();

        User user2 = User.builder()
                .id(20L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("no-token")
                .build();

        when(userRepository.findById(23)).thenReturn(user1);
        when(userRepository.findById(20)).thenReturn(user2);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");

        userService.updatePassword("mCUMoT5ilyKYdeOa8iFI+w==", "encodedPassword", "token");

        TokenException exception = assertThrows(TokenException.class, () -> {
            userService.updatePassword("ykcfU3ayqLU9YpCFhDtu+A==", "encodedPassword", "token");
        });

        assertEquals("Ошибка токена сброса!", exception.getMessage());
        assertEquals("encodedPassword", user1.getPassword());
    }

    @Test
    public void sendPasswordResetMessageTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("token")
                .build();

        when(userRepository.findByEmailIgnoreCase("min@list.ru")).thenReturn(user1);
        when(tokenService.generateResetPasswordToken()).thenReturn("token1");
        when(mailBuilder.buildResetPasswordMessage(anyString(), anyString(), eq("token1")))
                .thenReturn(new EmailData("min@list.ru", "Subject", "Message"));

        userService.sendPasswordResetMessage(user1.getEmail());

        verify(emailService, times(1)).sendEmail(any(EmailData.class));
    }

    @Test
    public void sendConfirmEmailMessageTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("token")
                .build();

        when(userRepository.findByEmailIgnoreCase("min@list.ru")).thenReturn(user1);
        when(mailBuilder.buildConfirmEmailMessage(anyString(), anyString()))
                .thenReturn(new EmailData("min@list.ru", "Subject", "Message"));

        userService.sendConfirmEmailMessage(user1.getEmail());

        verify(emailService, times(1)).sendEmail(any(EmailData.class));
    }

    @Test
    public void confirmEmailTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(false)
                .build();

        when(userRepository.findById(23)).thenReturn(user1);

        userService.confirmEmail("mCUMoT5ilyKYdeOa8iFI+w==");

        assertTrue(user1.isEmailConfirmed());
    }

    @Test
    public void giveAdminRulesToUserTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("no-token")
                .emailConfirmed(false)
                .roles(new ArrayList<>())
                .build();

        User user2 = User.builder()
                .id(24L)
                .email("min@list.ru")
                .username("test")
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

        user1.getRoles().add(roleUser);
        user2.getRoles().add(roleUser);

        when(roleService.findRoleByName(UserRole.ROLE_USER)).thenReturn(roleUser);
        when(roleService.findRoleByName(UserRole.ROLE_ADMIN)).thenReturn(roleAdmin);

        userService.giveAdminRulesToUser(24);

        NotConfirmedEmailException exception = assertThrows(NotConfirmedEmailException.class, () -> {
            userService.giveAdminRulesToUser(23);
        });

        assertEquals("Пользователь без подтверждения Email не может быть админом!", exception.getMessage());
        assertEquals(2, user2.getRoles().size());
    }

    @Test
    public void removeAdminRulesFromUserTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
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

        user1.getRoles().add(roleUser);

        when(roleService.findRoleByName(UserRole.ROLE_USER)).thenReturn(roleUser);
        when(roleService.findRoleByName(UserRole.ROLE_ADMIN)).thenReturn(roleAdmin);

        userService.giveAdminRulesToUser(23);
        userService.removeAdminRulesFromUser(23);

        assertEquals(1, user1.getRoles().size());
    }

    @Test
    public void updateFirstNameTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
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
    public void updateLastNameTest() {
        User user1 = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
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
