package com.kuet.ums.service;

import com.kuet.ums.exception.ConflictException;
import com.kuet.ums.repository.AdminRepository;
import com.kuet.ums.repository.StudentRepository;
import com.kuet.ums.repository.TeacherRepository;
import org.springframework.stereotype.Component;

@Component
public class AccountValidator {
    private final AdminRepository adminRepo;
    private final TeacherRepository teacherRepo;
    private final StudentRepository studentRepo;

    public AccountValidator(AdminRepository a, TeacherRepository t, StudentRepository s) {
        this.adminRepo = a;
        this.teacherRepo = t;
        this.studentRepo = s;
    }

    public void ensureUsernameFree(String username) {
        if (adminRepo.existsByUsername(username) || teacherRepo.existsByUsername(username)
                || studentRepo.existsByUsername(username)) {
            throw new ConflictException("Username '" + username + "' is already taken");
        }
    }
}
