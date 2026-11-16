package com.kuet.ums.service;

import com.kuet.ums.dto.AdminRequest;
import com.kuet.ums.dto.AdminResponse;
import com.kuet.ums.entity.Admin;
import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.exception.ConflictException;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final AdminRepository repo;
    private final AccountValidator accountValidator;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository repo, AccountValidator accountValidator, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.accountValidator = accountValidator;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminResponse> findAll() {
        return repo.findAll().stream().map(AdminResponse::from).toList();
    }

    public AdminResponse findById(Long id) {
        return AdminResponse.from(get(id));
    }

    @Transactional
    public AdminResponse create(AdminRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new BadRequestException("password is required");
        }
        accountValidator.ensureUsernameFree(req.username());
        if (repo.existsByEmail(req.email())) {
            throw new ConflictException("Email '" + req.email() + "' is already used");
        }

        Admin e = new Admin();
        apply(e, req);
        e.setPassword(passwordEncoder.encode(req.password()));
        return AdminResponse.from(repo.save(e));
    }

    @Transactional
    public AdminResponse update(Long id, AdminRequest req) {
        Admin e = get(id);
        if (!e.getUsername().equals(req.username())) {
            accountValidator.ensureUsernameFree(req.username());
        }
        if (!e.getEmail().equals(req.email()) && repo.existsByEmail(req.email())) {
            throw new ConflictException("Email '" + req.email() + "' is already used");
        }

        apply(e, req);
        if (req.password() != null && !req.password().isBlank()) {
            e.setPassword(passwordEncoder.encode(req.password()));
        }
        return AdminResponse.from(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(get(id));
    }

    private Admin get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id " + id));
    }

    private void apply(Admin e, AdminRequest req) {
        e.setName(req.name());
        e.setEmail(req.email());
        e.setUsername(req.username());

    }
}
