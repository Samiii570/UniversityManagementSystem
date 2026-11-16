package com.kuet.ums.service;

import com.kuet.ums.dto.StudentRequest;
import com.kuet.ums.dto.StudentResponse;
import com.kuet.ums.entity.Student;
import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.exception.ConflictException;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository repo;
    private final AccountValidator accountValidator;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository repo, AccountValidator accountValidator, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.accountValidator = accountValidator;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StudentResponse> findAll() {
        return repo.findAll().stream().map(StudentResponse::from).toList();
    }

    public StudentResponse findById(Long id) {
        return StudentResponse.from(get(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new BadRequestException("password is required");
        }
        accountValidator.ensureUsernameFree(req.username());
        if (repo.existsByEmail(req.email())) {
            throw new ConflictException("Email '" + req.email() + "' is already used");
        }
        if (repo.existsByRoll(req.roll())) {
            throw new ConflictException("Roll '" + req.roll() + "' already exists");
        }
        Student e = new Student();
        apply(e, req);
        e.setPassword(passwordEncoder.encode(req.password()));
        return StudentResponse.from(repo.save(e));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest req) {
        Student e = get(id);
        if (!e.getUsername().equals(req.username())) {
            accountValidator.ensureUsernameFree(req.username());
        }
        if (!e.getEmail().equals(req.email()) && repo.existsByEmail(req.email())) {
            throw new ConflictException("Email '" + req.email() + "' is already used");
        }
        if (!e.getRoll().equals(req.roll()) && repo.existsByRoll(req.roll())) {
            throw new ConflictException("Roll '" + req.roll() + "' already exists");
        }
        apply(e, req);
        if (req.password() != null && !req.password().isBlank()) {
            e.setPassword(passwordEncoder.encode(req.password()));
        }
        return StudentResponse.from(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(get(id));
    }

    private Student get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    private void apply(Student e, StudentRequest req) {
        e.setName(req.name());
        e.setEmail(req.email());
        e.setUsername(req.username());
        e.setRoll(req.roll());
        e.setDepartment(req.department());
        e.setBatch(req.batch());
    }
}
