package com.cards.auth.config;

import com.cards.auth.domain.Role;
import com.cards.auth.domain.User;
import com.cards.auth.repository.RoleRepository;
import com.cards.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Loads demo users on startup when they are missing.
 * Passwords are encoded at runtime so we never store plain text or hand-written hashes in SQL.
 * See docs/USERS.md for the human-readable seed catalog.
 */
@Component
@Profile("!prod")
public class AuthDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthDataInitializer.class);

    /** Stable id for Ada — referenced by account/payment seed data. */
    public static final UUID ADA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    /** Stable id for Ben. */
    public static final UUID BEN_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    /** Stable id for Cara (admin). */
    public static final UUID CARA_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataInitializer(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Runs once at application start and inserts missing seed users.
     *
     * @param args startup arguments (unused)
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER missing — run Flyway first"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN missing — run Flyway first"));

        ensureUser(ADA_ID, "ada.lovelace@cards.local", "Ada Lovelace", "Password123!", Set.of(userRole));
        ensureUser(BEN_ID, "ben.franklin@cards.local", "Ben Franklin", "Password123!", Set.of(userRole));
        ensureUser(CARA_ID, "cara.admin@cards.local", "Cara Admin", "Password123!", Set.of(userRole, adminRole));
        log.info("Auth seed users ready (see docs/USERS.md)");
    }

    /**
     * Creates a user only if the email is not already present.
     *
     * @param id       fixed UUID for cross-service seed links
     * @param email    login email
     * @param fullName display name
     * @param rawPassword plain password to encode
     * @param roles    roles to attach
     */
    private void ensureUser(UUID id, String email, String fullName, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User user = User.builder()
                .id(id)
                .email(email)
                .fullName(fullName)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .roles(roles)
                .build();
        userRepository.save(user);
        log.info("Seeded user {}", email);
    }
}
