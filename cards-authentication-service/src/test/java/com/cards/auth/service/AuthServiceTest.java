package com.cards.auth.service;

import com.cards.auth.domain.Role;
import com.cards.auth.domain.User;
import com.cards.auth.dto.LoginRequest;
import com.cards.auth.dto.RegisterRequest;
import com.cards.auth.repository.RefreshTokenRepository;
import com.cards.auth.repository.RoleRepository;
import com.cards.auth.repository.UserRepository;
import com.cards.auth.security.OAuth2ClientProperties;
import com.cards.auth.security.OAuth2TokenIssuer;
import com.cards.common.error.ConflictException;
import com.cards.common.error.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OAuth2TokenIssuer tokenIssuer;
    @Mock
    private OAuth2ClientProperties oauth2ClientProperties;
    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private AuthService authService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(UUID.randomUUID()).name("ROLE_USER").build();
        lenient().when(oauth2ClientProperties.getAccessTokenTtlMinutes()).thenReturn(15L);
        lenient().when(oauth2ClientProperties.getRefreshTokenTtlDays()).thenReturn(7L);
    }

    @Test
    void registerFailsWhenEmailExists() {
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
        RegisterRequest request = new RegisterRequest();
        request.setEmail("a@b.com");
        request.setPassword("password123");
        request.setFullName("Ada");
        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void loginFailsForBadPassword() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("a@b.com")
                .passwordHash("{bcrypt}x")
                .fullName("Ada")
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("a@b.com");
        request.setPassword("wrong");
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void registerSucceedsAndIssuesToken() {
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("{bcrypt}hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(tokenIssuer.issueAccessToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("a@b.com");
        request.setPassword("password123");
        request.setFullName("Ada Lovelace");

        var response = authService.register(request);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        verify(refreshTokenRepository).save(any());
    }
}
