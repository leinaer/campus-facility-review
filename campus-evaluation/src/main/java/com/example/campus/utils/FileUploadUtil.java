//package com.example.campus.utils;
//
//import com.example.campus.exception.BusinessException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import jakarta.annotation.PostConstruct;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.UUID;
//
///**
// * 文件上传工具类
// */
//@Component
//public class FileUploadUtil {
//
//    @Value("${file.upload.path}")
//    private String uploadPath;
//
//    @Value("${file.access.url}")
//    private String accessUrl;
//
//    @PostConstruct
//    public void init() {
//        try {
//            Path rootDir = Paths.get(uploadPath);
//            if (!Files.exists(rootDir)) {
//                Files.createDirectories(rootDir);
//                System.out.println("✅ 创建上传根目录: " + uploadPath);
//            }
//        } catch (IOException e) {
//            System.err.println("❌ 创建上传根目录失败: " + e.getMessage());
//        }
//    }
//
//    public String uploadFile(MultipartFile file, String folder) {
//
//        if (file.isEmpty()) {
//            throw new BusinessException(400, "文件不能为空");
//        }
//
//        if (file.getSize() > 5 * 1024 * 1024) {
//            throw new BusinessException(400, "文件大小不能超过5MB");
//        }
//
//        String originalFilename = file.getOriginalFilename();
//        if (originalFilename == null || !isValidImageType(originalFilename)) {
//            throw new BusinessException(400, "只支持图片格式（jpg、png、gif、webp）");
//        }
//
//        String extension = getFileExtension(originalFilename);
//        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
//
//        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
//
//        try {
//            Path targetDir = Paths.get(uploadPath, folder, datePath);
//            Files.createDirectories(targetDir);
//
//            Path destFile = targetDir.resolve(fileName);
//            file.transferTo(destFile.toFile());
//
//            String urlPath = accessUrl + "/" + folder + "/" + datePath + "/" + fileName;
//            System.out.println("✅ 文件上传成功: " + urlPath);
//
//            return urlPath;
//
//        } catch (IOException e) {
//            throw new BusinessException(500, "文件保存失败：" + e.getMessage());
//        }
//    }
//
//    public String[] uploadFiles(MultipartFile[] files, String folder) {
//        if (files == null || files.length == 0) {
//            throw new BusinessException(400, "文件不能为空");
//        }
//
//        if (files.length > 9) {
//            throw new BusinessException(400, "最多只能上传9张图片");
//        }
//
//        String[] urls = new String[files.length];
//        for (int i = 0; i < files.length; i++) {
//            urls[i] = uploadFile(files[i], folder);
//        }
//        return urls;
//    }
//
//    private boolean isValidImageType(String filename) {
//        String lowerCase = filename.toLowerCase();
//        return lowerCase.endsWith(".jpg") ||
//                lowerCase.endsWith(".jpeg") ||
//                lowerCase.endsWith(".png") ||
//                lowerCase.endsWith(".gif") ||
//                lowerCase.endsWith(".webp");
//    }
//
//    private String getFileExtension(String filename) {
//        int dotIndex = filename.lastIndexOf(".");
//        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
//            return filename.substring(dotIndex + 1).toLowerCase();
//        }
//        return "";
//    }
//}



package com.example.campus.utils;

import com.example.campus.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Component
public class FileUploadUtil {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url}")
    private String accessUrl;

    @Value("${server.port}")
    private String serverPort;

    @PostConstruct
    public void init() {
        try {
            Path rootDir = Paths.get(uploadPath);
            if (!Files.exists(rootDir)) {
                Files.createDirectories(rootDir);
                System.out.println("✅ 创建上传根目录: " + uploadPath);
            }
        } catch (IOException e) {
            System.err.println("❌ 创建上传根目录失败: " + e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file, String folder) {

        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageType(originalFilename)) {
            throw new BusinessException(400, "只支持图片格式（jpg、png、gif、webp）");
        }

        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        try {
            Path targetDir = Paths.get(uploadPath, folder, datePath);
            Files.createDirectories(targetDir);

            Path destFile = targetDir.resolve(fileName);
            file.transferTo(destFile.toFile());

            // 构建完整URL
            String urlPath = getBaseUrl() + accessUrl + "/" + folder + "/" + datePath + "/" + fileName;
            System.out.println("✅ 文件上传成功: " + urlPath);

            return urlPath;

        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败：" + e.getMessage());
        }
    }

    /**
     * 获取服务器基础URL
     */
    private String getBaseUrl() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String scheme = request.getScheme();
                String serverName = request.getServerName();
                return scheme + "://" + serverName + ":" + serverPort;
            }
        } catch (Exception e) {
            System.err.println("获取请求上下文失败: " + e.getMessage());
        }
        // 降级方案：使用localhost
        return "http://localhost:" + serverPort;
    }

    public String[] uploadFiles(MultipartFile[] files, String folder) {
        if (files == null || files.length == 0) {
            throw new BusinessException(400, "文件不能为空");
        }

        if (files.length > 9) {
            throw new BusinessException(400, "最多只能上传9张图片");
        }

        String[] urls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = uploadFile(files[i], folder);
        }
        return urls;
    }

    private boolean isValidImageType(String filename) {
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".jpg") ||
                lowerCase.endsWith(".jpeg") ||
                lowerCase.endsWith(".png") ||
                lowerCase.endsWith(".gif") ||
                lowerCase.endsWith(".webp");
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}

