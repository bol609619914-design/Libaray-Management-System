package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.dto.UserSaveRequest;
import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.User;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.vo.StatsVO;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserMapper userMapper, BookMapper bookMapper, BorrowRecordMapper borrowRecordMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public StatsVO stats() {
        long readers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "READER"));
        long books = bookMapper.selectCount(new LambdaQueryWrapper<Book>());
        long active = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>().in(BorrowRecord::getStatus, List.of("BORROWED", "OVERDUE")));
        long overdue = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getStatus, "OVERDUE"));
        BigDecimal unpaid = borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getFineStatus, "UNPAID"))
                .stream()
                .map(BorrowRecord::getFineAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new StatsVO(readers, books, active, overdue, unpaid);
    }

    public List<User> users(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt);
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        return userMapper.selectList(wrapper);
    }

    @Transactional
    public User createUser(UserSaveRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password() == null || request.password().isBlank() ? "123456" : request.password()));
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setStatus(request.status() == null ? "ACTIVE" : request.status());
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public User updateUser(Long id, UserSaveRequest request) {
        User user = userMapper.selectById(id);
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setStatus(request.status());
        userMapper.updateById(user);
        return user;
    }

    @Transactional
    public void disableUser(Long id) {
        User user = userMapper.selectById(id);
        user.setStatus("DISABLED");
        userMapper.updateById(user);
    }
}
