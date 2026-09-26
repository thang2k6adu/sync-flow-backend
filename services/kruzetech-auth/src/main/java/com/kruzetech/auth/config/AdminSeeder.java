package com.kruzetech.auth.config;

import com.kruzetech.auth.entity.Role;
import com.kruzetech.auth.entity.User;
import com.kruzetech.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Tạo tài khoản ADMIN lần đầu khi đặt SEED_ADMIN_EMAIL + SEED_ADMIN_PASSWORD (thay cho prisma/seed.ts). */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AppProperties props;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AppProperties props, UserRepository users, PasswordEncoder passwordEncoder) {
        this.props = props;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = props.seed().adminEmail();
        String password = props.seed().adminPassword();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password) || users.existsByEmail(email)) {
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFirstName("Admin");
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Seeded admin user {}", email);
    }
}
