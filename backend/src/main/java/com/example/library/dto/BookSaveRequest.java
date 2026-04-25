package com.example.library.dto;

import java.time.LocalDate;

public record BookSaveRequest(
        String isbn,
        String title,
        String author,
        Long categoryId,
        Long publisherId,
        String tagNames,
        LocalDate publishedDate,
        String coverUrl,
        String summary,
        Integer totalStock,
        Integer availableStock,
        String shelfCode,
        String status
) {
}

