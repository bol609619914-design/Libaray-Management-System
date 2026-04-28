package com.example.library.vo;

import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record BorrowRecordVO(
        Long id,
        Long userId,
        String readerName,
        String readerUsername,
        Long bookId,
        String bookTitle,
        String bookAuthor,
        String isbn,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        LocalDateTime returnedAt,
        Integer renewCount,
        String status,
        Integer overdueDays,
        BigDecimal fineAmount,
        String fineStatus,
        String handledBy,
        String note
) {
    public static BorrowRecordVO from(BorrowRecord record, Map<Long, User> users, Map<Long, Book> books) {
        User user = users.get(record.getUserId());
        Book book = books.get(record.getBookId());
        return new BorrowRecordVO(
                record.getId(),
                record.getUserId(),
                user == null ? "未知读者" : user.getRealName(),
                user == null ? "-" : user.getUsername(),
                record.getBookId(),
                book == null ? "未知图书" : book.getTitle(),
                book == null ? "-" : book.getAuthor(),
                book == null ? "-" : book.getIsbn(),
                record.getBorrowedAt(),
                record.getDueAt(),
                record.getReturnedAt(),
                record.getRenewCount(),
                record.getStatus(),
                record.getOverdueDays(),
                record.getFineAmount(),
                record.getFineStatus(),
                record.getHandledBy(),
                record.getNote()
        );
    }
}
