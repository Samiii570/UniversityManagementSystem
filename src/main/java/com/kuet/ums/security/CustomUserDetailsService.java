package com.kuet.ums.security;

import com.kuet.ums.entity.BaseUser;
import com.kuet.ums.repository.AdminRepository;
import com.kuet.ums.repository.StudentRepository;
import com.kuet.ums.repository.TeacherRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AdminRepository adminRepo;
    private final TeacherRepository teacherRepo;
    private final StudentRepository studentRepo;

    public CustomUserDetailsService(AdminRepository a, TeacherRepository t, StudentRepository s) {
        this.adminRepo = a;
        this.teacherRepo = t;
        this.studentRepo = s;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BaseUser user = adminRepo.findByUsername(username).<BaseUser>map(x -> x)
                .or(() -> teacherRepo.findByUsername(username).map(x -> x))
                .or(() -> studentRepo.findByUsername(username).map(x -> x))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
