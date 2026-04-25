package com.example.library.dto;

public record UserSaveRequest(
        String username,
        String password,
        String realName,
        String phone,
        String email,
        String role,
        String status
) {
}

