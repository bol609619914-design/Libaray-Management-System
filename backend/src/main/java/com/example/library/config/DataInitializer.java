package com.example.library.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.entity.Announcement;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.entity.Publisher;
import com.example.library.entity.SystemConfig;
import com.example.library.entity.Tag;
import com.example.library.entity.User;
import com.example.library.mapper.AnnouncementMapper;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.CategoryMapper;
import com.example.library.mapper.PublisherMapper;
import com.example.library.mapper.SystemConfigMapper;
import com.example.library.mapper.TagMapper;
import com.example.library.mapper.UserMapper;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final PublisherMapper publisherMapper;
    private final TagMapper tagMapper;
    private final BookMapper bookMapper;
    private final AnnouncementMapper announcementMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserMapper userMapper,
            CategoryMapper categoryMapper,
            PublisherMapper publisherMapper,
            TagMapper tagMapper,
            BookMapper bookMapper,
            AnnouncementMapper announcementMapper,
            SystemConfigMapper systemConfigMapper,
            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.publisherMapper = publisherMapper;
        this.tagMapper = tagMapper;
        this.bookMapper = bookMapper;
        this.announcementMapper = announcementMapper;
        this.systemConfigMapper = systemConfigMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        if (categoryMapper.selectCount(null) == 0) {
            Category literature = category("文学", "LITERATURE", "小说、散文、诗歌等文学读物");
            Category technology = category("计算机", "TECH", "软件工程、数据库、人工智能等技术书籍");
            Category history = category("历史", "HISTORY", "历史、人文与社会研究");
            Publisher people = publisher("人民邮电出版社", "service@ptpress.com.cn");
            publisher("机械工业出版社", "contact@cmpbook.com");
            tag("热门");
            tag("经典");
            tag("新书");
            book("9787115546081", "深入理解计算机系统", "Randal E. Bryant", technology.getId(), people.getId(), "经典,计算机", 5);
            book("9787020002207", "围城", "钱锺书", literature.getId(), people.getId(), "经典,文学", 8);
            book("9787101106472", "万历十五年", "黄仁宇", history.getId(), people.getId(), "热门,历史", 6);
        }
        if (announcementMapper.selectCount(null) == 0) {
            Announcement announcement = new Announcement();
            announcement.setTitle("图书馆系统试运行公告");
            announcement.setContent("线上检索、续借、罚款查询与反馈功能已开放，请读者及时更新个人联系方式。");
            announcement.setStatus("PUBLISHED");
            announcementMapper.insert(announcement);
        }
        if (systemConfigMapper.selectCount(null) == 0) {
            config("borrow.days", "30", "默认借阅天数");
            config("renew.max.count", "2", "最大续借次数");
            config("fine.per.day", "0.50", "每日逾期罚金");
        }
    }

    private void seedUsers() {
        user("reader", "reader123", "普通读者", "READER");
        user("librarian", "admin123", "图书管理员", "LIBRARIAN");
        user("superadmin", "root123", "超级管理员", "SUPER_ADMIN");
    }

    private void user(String username, String password, String realName, String role) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setPhone("13800000000");
        user.setEmail(username + "@library.local");
        user.setRole(role);
        user.setStatus("ACTIVE");
        userMapper.insert(user);
    }

    private Category category(String name, String code, String description) {
        Category category = new Category();
        category.setName(name);
        category.setCode(code);
        category.setDescription(description);
        categoryMapper.insert(category);
        return category;
    }

    private Publisher publisher(String name, String contact) {
        Publisher publisher = new Publisher();
        publisher.setName(name);
        publisher.setContact(contact);
        publisherMapper.insert(publisher);
        return publisher;
    }

    private void tag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);
    }

    private void book(String isbn, String title, String author, Long categoryId, Long publisherId, String tagNames, int stock) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategoryId(categoryId);
        book.setPublisherId(publisherId);
        book.setTagNames(tagNames);
        book.setPublishedDate(LocalDate.now().minusYears(3));
        book.setSummary("馆藏样例图书，用于演示检索、借阅、续借、库存维护等业务流程。");
        book.setTotalStock(stock);
        book.setAvailableStock(stock);
        book.setShelfCode("A-" + categoryId + "-" + stock);
        book.setStatus("ON_SHELF");
        bookMapper.insert(book);
    }

    private void config(String key, String value, String description) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        systemConfigMapper.insert(config);
    }
}
