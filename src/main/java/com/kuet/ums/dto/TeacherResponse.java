package com.kuet.ums.dto;

import com.kuet.ums.entity.Teacher;

public record TeacherResponse(Long id, String name, String email, String username,
                              String department, String designation, String role) {
    public static TeacherResponse from(Teacher t) {
        return new TeacherResponse(t.getId(), t.getName(), t.getEmail(), t.getUsername(),
                t.getDepartment(), t.getDesignation(), t.getRole().name());
    }
}
