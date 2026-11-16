package com.kuet.ums.security;

import com.kuet.ums.entity.Admin;
import com.kuet.ums.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public DataSeeder(AdminRepository adminRepo, PasswordEncoder passwordEncoder,
                      @Value("${app.default-admin.username}") String username,
                      @Value("${app.default-admin.password}") String password) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (adminRepo.count() == 0) {
            Admin admin = new Admin();
            admin.setName("System Admin");
            admin.setEmail("admin@kuet.ac.bd");
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            adminRepo.save(admin);
            log.info("Default admin created -> username: {}", username);
        }
    }
}
