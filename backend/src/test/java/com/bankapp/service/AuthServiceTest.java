package com.bankapp.service;

import com.bankapp.dto.request.RegisterRequest;
import com.bankapp.dto.response.UserDto;
import com.bankapp.entity.Role;
import com.bankapp.entity.RoleName;
import com.bankapp.entity.User;
import com.bankapp.exception.DuplicateResourceException;
import com.bankapp.repository.RoleRepository;
import com.bankapp.repository.UserRepository;
import com.bankapp.security.JwtUtil;
import com.bankapp.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setUsername("johndoe");
        request.setEmail("john@example.com");
        request.setPassword("Secret123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("9876543210");
    }

    @Test
    void register_shouldCreateNewCustomer_whenUsernameAndEmailAreUnique() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(Role.builder().id(1L).name(RoleName.ROLE_CUSTOMER).build()));
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(100L);
            return u;
        });

        UserDto result = authService.register(request);

        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getRoles()).contains("ROLE_CUSTOMER");
        verify(passwordEncoder, times(1)).encode("Secret123");
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
    }
}
