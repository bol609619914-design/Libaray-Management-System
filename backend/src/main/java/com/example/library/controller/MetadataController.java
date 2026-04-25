package com.example.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.common.ApiResponse;
import com.example.library.entity.Announcement;
import com.example.library.entity.Category;
import com.example.library.entity.Feedback;
import com.example.library.entity.OperationLog;
import com.example.library.entity.Publisher;
import com.example.library.entity.SystemConfig;
import com.example.library.entity.Tag;
import com.example.library.mapper.AnnouncementMapper;
import com.example.library.mapper.CategoryMapper;
import com.example.library.mapper.FeedbackMapper;
import com.example.library.mapper.OperationLogMapper;
import com.example.library.mapper.PublisherMapper;
import com.example.library.mapper.SystemConfigMapper;
import com.example.library.mapper.TagMapper;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetadataController {
    private final CategoryMapper categoryMapper;
    private final PublisherMapper publisherMapper;
    private final TagMapper tagMapper;
    private final AnnouncementMapper announcementMapper;
    private final FeedbackMapper feedbackMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final OperationLogMapper operationLogMapper;

    public MetadataController(
            CategoryMapper categoryMapper,
            PublisherMapper publisherMapper,
            TagMapper tagMapper,
            AnnouncementMapper announcementMapper,
            FeedbackMapper feedbackMapper,
            SystemConfigMapper systemConfigMapper,
            OperationLogMapper operationLogMapper) {
        this.categoryMapper = categoryMapper;
        this.publisherMapper = publisherMapper;
        this.tagMapper = tagMapper;
        this.announcementMapper = announcementMapper;
        this.feedbackMapper = feedbackMapper;
        this.systemConfigMapper = systemConfigMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> categories() {
        return ApiResponse.ok(categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getName)));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Category> createCategory(@RequestBody Category category) {
        categoryMapper.insert(category);
        return ApiResponse.ok(category);
    }

    @GetMapping("/publishers")
    public ApiResponse<List<Publisher>> publishers() {
        return ApiResponse.ok(publisherMapper.selectList(new LambdaQueryWrapper<Publisher>().orderByAsc(Publisher::getName)));
    }

    @PostMapping("/publishers")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Publisher> createPublisher(@RequestBody Publisher publisher) {
        publisherMapper.insert(publisher);
        return ApiResponse.ok(publisher);
    }

    @GetMapping("/tags")
    public ApiResponse<List<Tag>> tags() {
        return ApiResponse.ok(tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName)));
    }

    @PostMapping("/tags")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Tag> createTag(@RequestBody Tag tag) {
        tagMapper.insert(tag);
        return ApiResponse.ok(tag);
    }

    @GetMapping("/announcements")
    public ApiResponse<List<Announcement>> announcements() {
        return ApiResponse.ok(announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, "PUBLISHED")
                .orderByDesc(Announcement::getCreatedAt)));
    }

    @PostMapping("/announcements")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        announcementMapper.insert(announcement);
        return ApiResponse.ok(announcement);
    }

    @PostMapping("/feedback")
    public ApiResponse<Feedback> feedback(@RequestBody Feedback feedback) {
        feedbackMapper.insert(feedback);
        return ApiResponse.ok(feedback);
    }

    @GetMapping("/feedback")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<List<Feedback>> feedbackList() {
        return ApiResponse.ok(feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>().orderByDesc(Feedback::getCreatedAt)));
    }

    @PutMapping("/feedback")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Void> updateFeedback(@RequestBody Feedback feedback) {
        feedbackMapper.updateById(feedback);
        return ApiResponse.ok();
    }

    @GetMapping("/configs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<SystemConfig>> configs() {
        return ApiResponse.ok(systemConfigMapper.selectList(new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getConfigKey)));
    }

    @PutMapping("/configs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> updateConfig(@RequestBody SystemConfig config) {
        systemConfigMapper.updateById(config);
        return ApiResponse.ok();
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<OperationLog>> logs() {
        return ApiResponse.ok(operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreatedAt)));
    }
}
