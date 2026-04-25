package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.dto.BorrowRequest;
import com.example.library.entity.BorrowRecord;
import com.example.library.security.CurrentUser;
import com.example.library.service.BorrowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {
    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/mine")
    public ApiResponse<List<BorrowRecord>> mine(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) String status) {
        return ApiResponse.ok(borrowService.myRecords(user, status));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<List<BorrowRecord>> all(@RequestParam(required = false) String status) {
        return ApiResponse.ok(borrowService.all(status));
    }

    @PostMapping
    public ApiResponse<BorrowRecord> borrow(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody BorrowRequest request) {
        return ApiResponse.ok(borrowService.borrow(user, request));
    }

    @PutMapping("/{id}/renew")
    public ApiResponse<BorrowRecord> renew(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return ApiResponse.ok(borrowService.renew(user, id));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<BorrowRecord> returnBook(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return ApiResponse.ok(borrowService.returnBook(user, id));
    }

    @PutMapping("/{id}/fine/paid")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<BorrowRecord> paid(@PathVariable Long id) {
        return ApiResponse.ok(borrowService.markPaid(id));
    }
}
