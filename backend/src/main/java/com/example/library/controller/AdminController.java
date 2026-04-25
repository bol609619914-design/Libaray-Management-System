package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.dto.UserSaveRequest;
import com.example.library.entity.User;
import com.example.library.service.AdminService;
import com.example.library.vo.StatsVO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ApiResponse<StatsVO> stats() {
        return ApiResponse.ok(adminService.stats());
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> users(@RequestParam(required = false) String role) {
        return ApiResponse.ok(adminService.users(role));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<User> createUser(@RequestBody UserSaveRequest request) {
        return ApiResponse.ok(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody UserSaveRequest request) {
        return ApiResponse.ok(adminService.updateUser(id, request));
    }

    @PutMapping("/users/{id}/disable")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> disableUser(@PathVariable Long id) {
        adminService.disableUser(id);
        return ApiResponse.ok();
    }
}

