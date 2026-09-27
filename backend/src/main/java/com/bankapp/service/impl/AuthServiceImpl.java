package com.bankapp.service.impl;

import com.bankapp.dto.request.LoginRequest;
import com.bankapp.dto.request.RegisterRequest;
import com.bankapp.dto.response.JwtResponse;
import com.bankapp.dto.response.UserDto;
import com.bankapp.entity.Role;
import com.bankapp.entity.RoleName;
import com.bankapp.entity.User;
import com.bankapp.exception.DuplicateResourceException;
import com.bankapp.exception.ResourceNotFoundException;
import com.bankapp.mapper.UserMapper;
import com.bankapp.repository.RoleRepository;
import com.bankapp.repository.UserRepository;
import com.bankapp.security.JwtUtil;
import com.bankapp.security.UserPrincipal;
import com.bankapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Default customer role not configured"));

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .enabled(true)
                .roles(roles)
                .build();

        User saved = userRepository.save(user);
        log.info("New customer registered: {}", saved.getUsername());
        return UserMapper.toDto(saved);
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal);

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("User logged in: {}", principal.getUsername());

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .id(principal.getId())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .roles(roles)
                .build();
    }
}
