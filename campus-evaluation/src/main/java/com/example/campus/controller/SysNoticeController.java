package com.example.campus.controller;

import com.example.campus.common.Result;
import com.example.campus.entity.SysNotice;
import com.example.campus.service.SysNoticeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeService noticeService;

    @GetMapping("/list")
    public Result<List<SysNotice>> getNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(noticeService.getLatestNotices(size));
    }

    @PostMapping("/publish")
    public Result<Void> publishNotice(@RequestBody SysNotice notice, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error("无权操作");
        }
        noticeService.publishNotice(notice);
        return Result.success(null);
    }

    @PutMapping("/{noticeId}")
    public Result<Void> updateNotice(@PathVariable Long noticeId, @RequestBody SysNotice notice) {
        notice.setNoticeId(noticeId);
        noticeService.updateById(notice);
        return Result.success(null);
    }

    @DeleteMapping("/{noticeId}")
    public Result<Void> deleteNotice(@PathVariable Long noticeId) {
        noticeService.removeById(noticeId);
        return Result.success(null);
    }

    @PutMapping("/{noticeId}/top")
    public Result<Void> toggleTop(@PathVariable Long noticeId, @RequestParam Boolean isTop) {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(noticeId);
        notice.setIsTop(isTop ? 1 : 0);
        noticeService.updateById(notice);
        return Result.success(null);
    }
}
