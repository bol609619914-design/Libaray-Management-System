package com.example.library.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(@NotNull Long bookId, Long userId, String note) {
}

