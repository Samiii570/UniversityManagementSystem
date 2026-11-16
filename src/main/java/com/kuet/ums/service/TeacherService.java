package com.kuet.ums.service;

import com.kuet.ums.dto.TeacherRequest;
import com.kuet.ums.dto.TeacherResponse;
import com.kuet.ums.entity.Teacher;
import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.exception.ConflictException;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository repo;
    private final AccountValidator accountValidator;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository repo, AccountValidator accountValidator, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.accountValidator = accountValidator;
        this.passwordEncoder = passwordEncoder;
    }

    public List<TeacherResponse> findAll() {
        return repo.findAll().stream().map(TeacherResponse::from).toList();
    }

    public TeacherResponse findById(Long id) {
        return TeacherResponse.from(get(id));
    }

    @Transactional
    public TeacherResponse create(TeacherRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new BadRequestException("password is required");
        }
        accountValidator.ensureUsernameFree(req.username());
        if (repo.existsByEmail(req.email())) {
            throw new ConflictException("Email '" + req.email() + "' is already used");
        }

        Teacher e = new Teacher();
        apply(e, req);
        e.setPassword(passwordEncoder.encode(req.password()));
        return TeacherResponse.from(repo.save(e));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest req) {
        Teacher e = get(id);
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
        return TeacherResponse.from(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(get(id));
    }

    private Teacher get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + id));
    }

    private void apply(Teacher e, TeacherRequest req) {
        e.setName(req.name());
        e.setEmail(req.email());
        e.setUsername(req.username());
        e.setDepartment(req.department());
        e.setDesignation(req.designation());
    }
}
