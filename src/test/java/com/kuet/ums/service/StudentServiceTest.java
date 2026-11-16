package com.kuet.ums.service;

import com.kuet.ums.dto.StudentRequest;
import com.kuet.ums.dto.StudentResponse;
import com.kuet.ums.entity.Student;
import com.kuet.ums.exception.BadRequestException;
import com.kuet.ums.exception.ConflictException;
import com.kuet.ums.exception.ResourceNotFoundException;
import com.kuet.ums.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    StudentRepository repo;
    @Mock
    AccountValidator accountValidator;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    StudentService service;

    private StudentRequest request(String password) {
        return new StudentRequest("Prottoy", "prottoy@stud.kuet.ac.bd", "student1", password, "2207040", "CSE", 2022);
    }

    private Student existing() {
        Student s = new Student();
        s.setId(1L);
        s.setName("Prottoy");
        s.setEmail("prottoy@stud.kuet.ac.bd");
        s.setUsername("student1");
        s.setPassword("old-hash");
        s.setRoll("2207040");
        s.setDepartment("CSE");
        s.setBatch(2022);
        return s;
    }

    @Test
    void createHashesPasswordBeforeSaving() {
        when(passwordEncoder.encode("stud123")).thenReturn("hashed");
        when(repo.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse response = service.create(request("stud123"));

        ArgumentCaptor<Student> saved = ArgumentCaptor.forClass(Student.class);
        verify(repo).save(saved.capture());
        assertEquals("hashed", saved.getValue().getPassword());
        assertEquals("2207040", response.roll());
        assertEquals("STUDENT", response.role());
    }

    @Test
    void createWithoutPasswordThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.create(request(null)));
        verify(repo, never()).save(any());
    }

    @Test
    void createWithTakenUsernameThrowsConflict() {
        doThrow(new ConflictException("taken")).when(accountValidator).ensureUsernameFree("student1");

        assertThrows(ConflictException.class, () -> service.create(request("stud123")));
        verify(repo, never()).save(any());
    }

    @Test
    void createWithDuplicateRollThrowsConflict() {
        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(repo.existsByRoll("2207040")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request("stud123")));
        verify(repo, never()).save(any());
    }

    @Test
    void findByIdThrowsNotFoundWhenMissing() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateWithoutPasswordKeepsOldPassword() {
        Student student = existing();
        when(repo.findById(1L)).thenReturn(Optional.of(student));
        when(repo.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse response = service.update(1L,
                new StudentRequest("Prottoy Kumar", "prottoy@stud.kuet.ac.bd", "student1", null, "2207040", "CSE", 2022));

        assertEquals("Prottoy Kumar", response.name());
        assertEquals("old-hash", student.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
