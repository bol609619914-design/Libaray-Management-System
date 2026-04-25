package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.PasswordChangeRequest;
import com.example.library.dto.ProfileUpdateRequest;
import com.example.library.entity.User;
import com.example.library.exception.BusinessException;
import com.example.library.mapper.UserMapper;
import com.example.library.security.CurrentUser;
import com.example.library.security.JwtService;
import com.example.library.vo.LoginResponse;
import com.example.library.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用或限制");
        }
        String token = jwtService.createToken(new CurrentUser(user.getId(), user.getUsername(), user.getRole()));
        return new LoginResponse(token, UserVO.from(user));
    }

    public UserVO me(CurrentUser currentUser) {
        return UserVO.from(mustGet(currentUser.id()));
    }

    @Transactional
    public UserVO updateProfile(CurrentUser currentUser, ProfileUpdateRequest request) {
        User user = mustGet(currentUser.id());
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        userMapper.updateById(user);
        return UserVO.from(user);
    }

    @Transactional
    public void changePassword(CurrentUser currentUser, PasswordChangeRequest request) {
        User user = mustGet(currentUser.id());
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }

    private User mustGet(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }
}

