package com.kuet.ums.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 50) String username,
        @Size(min = 6, message = "password must be at least 6 characters") String password
) {}
