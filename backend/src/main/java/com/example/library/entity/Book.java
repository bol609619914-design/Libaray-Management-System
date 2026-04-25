package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("books")
public class Book extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private Long categoryId;
    private Long publisherId;
    private String tagNames;
    private LocalDate publishedDate;
    private String coverUrl;
    private String summary;
    private Integer totalStock;
    private Integer availableStock;
    private String shelfCode;
    private String status;
}

