package com.good.ivrstand.extern.infrastructure.authentication;

import com.good.ivrstand.app.service.EncodeService;
import com.good.ivrstand.app.service.UserService;
import com.good.ivrstand.domain.User;
import com.good.ivrstand.exception.DifferentPasswordsException;
import com.good.ivrstand.exception.TokenRefreshException;
import com.good.ivrstand.exception.UserDuplicateException;
import com.good.ivrstand.exception.UserRolesException;
import com.good.ivrstand.extern.api.dto.UserRegisterDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EncodeService encodeService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser() throws UserDuplicateException, DifferentPasswordsException, UserRolesException {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUsername("testUser");
        userRegisterDTO.setPassword("password123");
        userRegisterDTO.setPasswordConfirm("password123");

        when(bCryptPasswordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refreshToken");

        Map<String, String> tokens = authService.registerUser(userRegisterDTO);

        verify(userService).createUser(any(User.class));
        assertEquals("accessToken", tokens.get("token"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
    }

    @Test
    void registerUserPasswordMismatch() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUsername("testUser");
        userRegisterDTO.setPassword("password123");
        userRegisterDTO.setPasswordConfirm("differentPassword");

        Exception e = assertThrows(DifferentPasswordsException.class, () -> authService.registerUser(userRegisterDTO));
        assertEquals("Пароли не совпадают!", e.getMessage());
    }

    @Test
    void loginUser() {
        String username = "testUser";
        String password = "password123";

        when(userService.userDetailsService()).thenReturn(userDetailsService);
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(userService.userDetailsService().loadUserByUsername(username)).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(mockUserDetails, "encryptedPassword")).thenReturn("refreshToken");
        when(encodeService.encrypt(password)).thenReturn("encryptedPassword");

        Map<String, String> tokens = authService.loginUser(username, password);

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
        assertEquals("accessToken", tokens.get("token"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
    }

    @Test
    void refreshToken() throws TokenRefreshException {
        String token = "accessToken";
        String refreshToken = "refreshToken";
        String username = "testUser";
        String password = "password123";

        when(userService.userDetailsService()).thenReturn(userDetailsService);
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(userService.userDetailsService().loadUserByUsername(username)).thenReturn(mockUserDetails);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.extractPassword(refreshToken)).thenReturn("encryptedPassword");
        when(encodeService.decrypt("encryptedPassword")).thenReturn(password);
        when(encodeService.encrypt(password)).thenReturn("password123");
        when(jwtService.isTokenValid(refreshToken, TokenType.REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.generateToken(mockUserDetails)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(mockUserDetails, "password123")).thenReturn("refreshToken");

        Map<String, String> tokens = authService.refreshToken(token, refreshToken);

        verify(jwtService).isTokenValid(refreshToken, TokenType.REFRESH_TOKEN);
        assertNotNull(tokens.get("token"));
        assertNotNull(tokens.get("refreshToken"));
    }

    @Test
    void refreshTokenInvalidRefreshToken() {
        String token = "accessToken";
        String refreshToken = "invalidRefreshToken";

        when(jwtService.extractUsername(token)).thenReturn("user");
        when(jwtService.extractPassword(refreshToken)).thenReturn("wrongPassword");
        when(jwtService.isTokenValid(refreshToken, TokenType.REFRESH_TOKEN)).thenReturn(false);

        Exception e = assertThrows(TokenRefreshException.class, () -> authService.refreshToken(token, refreshToken));
        assertEquals("Ошибка обновления токена", e.getMessage());
    }
}
