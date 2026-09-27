package com.bankapp.config;

import com.bankapp.entity.Role;
import com.bankapp.entity.RoleName;
import com.bankapp.entity.User;
import com.bankapp.repository.RoleRepository;
import com.bankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Seeds default roles and a bootstrap admin account on first application startup.
 * Safe to run repeatedly - all operations are idempotent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_CUSTOMER).build()));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

        if (!userRepository.existsByUsername("admin")) {
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(customerRole);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@bankapp.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .phoneNumber("0000000000")
                    .address("Head Office")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created -> username: admin / password: Admin@123");
        }
    }
}
