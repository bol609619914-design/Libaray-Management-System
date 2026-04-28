package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024;
    private final Path coverDir = Paths.get("uploads", "covers").toAbsolutePath().normalize();

    @PostMapping("/covers")
    @PreAuthorize("hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ApiResponse<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("请选择封面图片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("封面图片不能超过 3MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持 JPG、PNG、WebP 封面图片");
        }

        Files.createDirectories(coverDir);
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String fileName = UUID.randomUUID() + extension;
        Path target = coverDir.resolve(fileName).normalize();
        file.transferTo(target);
        return ApiResponse.ok(Map.of("url", "/uploads/covers/" + fileName));
    }
}
