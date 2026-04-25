package com.example.library.dto;

public record BookQuery(String keyword, Long categoryId, String status, long page, long size) {
    public long pageOrDefault() {
        return page <= 0 ? 1 : page;
    }

    public long sizeOrDefault() {
        return size <= 0 ? 10 : Math.min(size, 100);
    }
}

