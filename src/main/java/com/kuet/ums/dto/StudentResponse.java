package com.kuet.ums.dto;

import com.kuet.ums.entity.Student;

public record StudentResponse(Long id, String name, String email, String username,
                              String roll, String department, Integer batch, String role) {
    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getUsername(),
                s.getRoll(), s.getDepartment(), s.getBatch(), s.getRole().name());
    }
}
