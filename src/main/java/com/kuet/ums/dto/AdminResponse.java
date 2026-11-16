package com.kuet.ums.dto;

import com.kuet.ums.entity.Admin;

public record AdminResponse(Long id, String name, String email, String username, String role) {
    public static AdminResponse from(Admin a) {
        return new AdminResponse(a.getId(), a.getName(), a.getEmail(), a.getUsername(), a.getRole().name());
    }
}
