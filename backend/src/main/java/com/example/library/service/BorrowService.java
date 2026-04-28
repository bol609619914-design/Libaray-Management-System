package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.config.LibraryProperties;
import com.example.library.dto.BorrowRequest;
import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.User;
import com.example.library.exception.BusinessException;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.security.CurrentUser;
import com.example.library.vo.BorrowRecordVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowService {
    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final LibraryProperties properties;

    public BorrowService(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper, UserMapper userMapper, LibraryProperties properties) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
        this.properties = properties;
    }

    public List<BorrowRecordVO> myRecords(CurrentUser user, String status) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, user.id())
                .orderByDesc(BorrowRecord::getBorrowedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        return toVOList(borrowRecordMapper.selectList(wrapper));
    }

    public List<BorrowRecordVO> all(String status) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>().orderByDesc(BorrowRecord::getBorrowedAt);
        if (status != null && !status.isBlank()) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }
        return toVOList(borrowRecordMapper.selectList(wrapper));
    }

    @Transactional
    public BorrowRecordVO borrow(CurrentUser operator, BorrowRequest request) {
        Long userId = request.userId();
        if ("READER".equals(operator.role())) {
            userId = operator.id();
        } else if (userId == null) {
            throw new BusinessException("管理员办理借阅必须指定读者");
        }
        Book book = bookMapper.selectById(request.bookId());
        if (book == null || !"ON_SHELF".equals(book.getStatus())) {
            throw new BusinessException("图书不存在或未上架");
        }
        if (book.getAvailableStock() == null || book.getAvailableStock() <= 0) {
            throw new BusinessException("库存不足");
        }
        book.setAvailableStock(book.getAvailableStock() - 1);
        bookMapper.updateById(book);

        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(book.getId());
        record.setBorrowedAt(now);
        record.setDueAt(now.plusDays(properties.getBorrowing().getDefaultDays()));
        record.setRenewCount(0);
        record.setStatus("BORROWED");
        record.setOverdueDays(0);
        record.setFineAmount(BigDecimal.ZERO);
        record.setFineStatus("NONE");
        record.setHandledBy(operator.username());
        record.setNote(request.note());
        borrowRecordMapper.insert(record);
        return toVO(record);
    }

    @Transactional
    public BorrowRecordVO renew(CurrentUser user, Long recordId) {
        BorrowRecord record = mustGet(recordId);
        if (!"BORROWED".equals(record.getStatus()) && !"OVERDUE".equals(record.getStatus())) {
            throw new BusinessException("当前状态不可续借");
        }
        if ("READER".equals(user.role()) && !record.getUserId().equals(user.id())) {
            throw new BusinessException(403, "只能续借自己的图书");
        }
        if (record.getRenewCount() >= properties.getBorrowing().getMaxRenewCount()) {
            throw new BusinessException("续借次数已达上限");
        }
        record.setRenewCount(record.getRenewCount() + 1);
        record.setDueAt(record.getDueAt().plusDays(properties.getBorrowing().getDefaultDays()));
        record.setStatus("BORROWED");
        borrowRecordMapper.updateById(record);
        return toVO(record);
    }

    @Transactional
    public BorrowRecordVO returnBook(CurrentUser operator, Long recordId) {
        BorrowRecord record = mustGet(recordId);
        if ("RETURNED".equals(record.getStatus())) {
            return toVO(record);
        }
        LocalDateTime now = LocalDateTime.now();
        long overdue = Math.max(0, ChronoUnit.DAYS.between(record.getDueAt(), now));
        record.setReturnedAt(now);
        record.setOverdueDays((int) overdue);
        record.setFineAmount(properties.getBorrowing().getFinePerDay().multiply(BigDecimal.valueOf(overdue)));
        record.setFineStatus(overdue > 0 ? "UNPAID" : "NONE");
        record.setStatus("RETURNED");
        record.setHandledBy(operator.username());
        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setAvailableStock((book.getAvailableStock() == null ? 0 : book.getAvailableStock()) + 1);
            bookMapper.updateById(book);
        }
        return toVO(record);
    }

    @Transactional
    public BorrowRecordVO markPaid(Long recordId) {
        BorrowRecord record = mustGet(recordId);
        record.setFineStatus("PAID");
        borrowRecordMapper.updateById(record);
        return toVO(record);
    }

    private BorrowRecord mustGet(Long id) {
        BorrowRecord record = borrowRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        return record;
    }

    private BorrowRecordVO toVO(BorrowRecord record) {
        User user = userMapper.selectById(record.getUserId());
        Book book = bookMapper.selectById(record.getBookId());
        return BorrowRecordVO.from(
                record,
                user == null ? Map.of() : Map.of(user.getId(), user),
                book == null ? Map.of() : Map.of(book.getId(), book)
        );
    }

    private List<BorrowRecordVO> toVOList(List<BorrowRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = records.stream().map(BorrowRecord::getUserId).distinct().toList();
        List<Long> bookIds = records.stream().map(BorrowRecord::getBookId).distinct().toList();
        Map<Long, User> users = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Book> books = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        return records.stream().map(record -> BorrowRecordVO.from(record, users, books)).toList();
    }
}
