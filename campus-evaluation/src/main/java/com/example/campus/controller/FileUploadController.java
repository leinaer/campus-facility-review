package com.example.campus.controller;

import com.example.campus.exception.BusinessException;
import com.example.campus.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file,
                                           @RequestParam(defaultValue = "evaluation") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!isValidType(type)) {
                result.put("code", 400);
                result.put("msg", "无效的上传类型");
                return result;
            }

            String url = fileUploadUtil.uploadFile(file, type);
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", url);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/images")
    public Map<String, Object> uploadImages(@RequestParam("files") MultipartFile[] files,
                                            @RequestParam(defaultValue = "evaluation") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!isValidType(type)) {
                result.put("code", 400);
                result.put("msg", "无效的上传类型");
                return result;
            }

            String[] urls = fileUploadUtil.uploadFiles(files, type);
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", urls);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
        }
        return result;
    }

    private boolean isValidType(String type) {
        return "avatar".equals(type) ||
                "facility".equals(type) ||
                "evaluation".equals(type) ||
                "post".equals(type) ||
                "activity".equals(type) ||
                "category".equals(type) ||
                "banner".equals(type);
    }
}
