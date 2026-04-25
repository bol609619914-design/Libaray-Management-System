package com.example.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.ApiResponse;
import com.example.library.dto.BookQuery;
import com.example.library.dto.BookSaveRequest;
import com.example.library.entity.Book;
import com.example.library.service.BookService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<Page<Book>> search(BookQuery query) {
        return ApiResponse.ok(bookService.search(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> detail(@PathVariable Long id) {
        return ApiResponse.ok(bookService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Book> create(@RequestBody BookSaveRequest request) {
        return ApiResponse.ok(bookService.save(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Book> update(@PathVariable Long id, @RequestBody BookSaveRequest request) {
        return ApiResponse.ok(bookService.update(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Void> status(@PathVariable Long id, @RequestParam String status) {
        bookService.changeStatus(id, status);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.remove(id);
        return ApiResponse.ok();
    }
}

