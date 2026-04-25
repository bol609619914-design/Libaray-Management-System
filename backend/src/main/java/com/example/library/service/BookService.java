package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.dto.BookQuery;
import com.example.library.dto.BookSaveRequest;
import com.example.library.entity.Book;
import com.example.library.exception.BusinessException;
import com.example.library.mapper.BookMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookService {
    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public Page<Book> search(BookQuery query) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.keyword())) {
            wrapper.and(w -> w.like(Book::getTitle, query.keyword())
                    .or().like(Book::getAuthor, query.keyword())
                    .or().like(Book::getIsbn, query.keyword()));
        }
        if (query.categoryId() != null) {
            wrapper.eq(Book::getCategoryId, query.categoryId());
        }
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(Book::getStatus, query.status());
        }
        wrapper.orderByDesc(Book::getCreatedAt);
        return bookMapper.selectPage(Page.of(query.pageOrDefault(), query.sizeOrDefault()), wrapper);
    }

    public Book detail(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        return book;
    }

    @Transactional
    public Book save(BookSaveRequest request) {
        Book book = new Book();
        apply(book, request);
        bookMapper.insert(book);
        return book;
    }

    @Transactional
    public Book update(Long id, BookSaveRequest request) {
        Book book = detail(id);
        apply(book, request);
        bookMapper.updateById(book);
        return book;
    }

    @Transactional
    public void changeStatus(Long id, String status) {
        Book book = detail(id);
        book.setStatus(status);
        bookMapper.updateById(book);
    }

    @Transactional
    public void remove(Long id) {
        bookMapper.deleteById(id);
    }

    private void apply(Book book, BookSaveRequest request) {
        book.setIsbn(request.isbn());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setCategoryId(request.categoryId());
        book.setPublisherId(request.publisherId());
        book.setTagNames(request.tagNames());
        book.setPublishedDate(request.publishedDate());
        book.setCoverUrl(request.coverUrl());
        book.setSummary(request.summary());
        book.setTotalStock(request.totalStock() == null ? 0 : request.totalStock());
        book.setAvailableStock(request.availableStock() == null ? book.getTotalStock() : request.availableStock());
        book.setShelfCode(request.shelfCode());
        book.setStatus(StringUtils.hasText(request.status()) ? request.status() : "ON_SHELF");
    }
}

