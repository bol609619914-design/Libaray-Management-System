package com.example.library.vo;

import com.example.library.entity.User;

public record UserVO(Long id, String username, String realName, String phone, String email, String role, String status) {
    public static UserVO from(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getRealName(), user.getPhone(), user.getEmail(), user.getRole(), user.getStatus());
    }
}

