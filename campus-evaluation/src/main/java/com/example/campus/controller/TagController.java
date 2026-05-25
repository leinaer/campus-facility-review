package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.entity.Tag;
import com.example.campus.service.TagService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/admin/list")
    public Map<String, Object> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer tagType) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<Tag> tagPage = new Page<>(page, size);
            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like(Tag::getTagName, keyword);
            }

            if (tagType != null) {
                wrapper.eq(Tag::getTagType, tagType);
            }

            wrapper.orderByDesc(Tag::getCreateTime);
            Page<Tag> pageResult = tagService.page(tagPage, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getRecords());
            data.put("total", pageResult.getTotal());
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/admin/add")
    public Map<String, Object> adminAdd(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Tag tag = new Tag();
            tag.setTagName((String) params.get("tagName"));
            tag.setTagType(params.get("tagType") != null ? (Integer) params.get("tagType") : 1);
            tag.setIcon((String) params.get("icon"));
            tag.setColor((String) params.get("color"));
            tag.setStatus(params.get("status") != null ? (Integer) params.get("status") : 1);
            tag.setUseCount(0);

            if (params.get("activityId") != null) {
                tag.setActivityId(Long.valueOf(params.get("activityId").toString()));
            }

            boolean success = tagService.save(tag);

            if (success) {
                result.put("code", 200);
                result.put("msg", "创建成功");
            } else {
                result.put("code", 500);
                result.put("msg", "创建失败");
            }
        } catch (Exception e) {
            log.error("创建标签失败", e);
            result.put("code", 500);
            result.put("msg", "创建失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/admin/update")
    public Map<String, Object> adminUpdate(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long tagId = Long.valueOf(params.get("tagId").toString());
            Tag tag = tagService.getById(tagId);
            if (tag == null) {
                result.put("code", 404);
                result.put("msg", "标签不存在");
                return result;
            }

            tag.setTagName((String) params.get("tagName"));
            tag.setTagType(params.get("tagType") != null ? (Integer) params.get("tagType") : tag.getTagType());
            tag.setIcon((String) params.get("icon"));
            tag.setColor((String) params.get("color"));
            tag.setStatus(params.get("status") != null ? (Integer) params.get("status") : tag.getStatus());

            if (params.get("activityId") != null) {
                tag.setActivityId(Long.valueOf(params.get("activityId").toString()));
            }

            boolean success = tagService.updateById(tag);

            if (success) {
                result.put("code", 200);
                result.put("msg", "更新成功");
            } else {
                result.put("code", 500);
                result.put("msg", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新标签失败", e);
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/admin/status/{tagId}")
    public Map<String, Object> adminUpdateStatus(
            @PathVariable Long tagId,
            @RequestBody Map<String, Integer> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer status = param.get("status");
            Tag tag = tagService.getById(tagId);
            if (tag == null) {
                result.put("code", 404);
                result.put("msg", "标签不存在");
                return result;
            }
            tag.setStatus(status);
            tagService.updateById(tag);
            result.put("code", 200);
            result.put("msg", "操作成功");
        } catch (Exception e) {
            log.error("更新标签状态失败", e);
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/admin/delete/{tagId}")
    public Map<String, Object> adminDelete(@PathVariable Long tagId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = tagService.removeById(tagId);
            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除标签失败", e);
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/admin/batch-delete")
    public Map<String, Object> adminBatchDelete(@RequestBody Map<String, List<Long>> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> ids = param.get("ids");
            if (ids == null || ids.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "请选择要删除的标签");
                return result;
            }
            tagService.removeByIds(ids);
            result.put("code", 200);
            result.put("msg", "批量删除成功");
        } catch (Exception e) {
            log.error("批量删除失败", e);
            result.put("code", 500);
            result.put("msg", "批量删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/admin/batch-status")
    public Map<String, Object> adminBatchStatus(@RequestBody Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) param.get("ids");
            Integer status = (Integer) param.get("status");

            if (ids == null || ids.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "请选择要操作的标签");
                return result;
            }

            List<Tag> tags = tagService.listByIds(ids);
            tags.forEach(tag -> tag.setStatus(status));
            tagService.updateBatchById(tags);

            result.put("code", 200);
            result.put("msg", "批量操作成功");
        } catch (Exception e) {
            log.error("批量更新状态失败", e);
            result.put("code", 500);
            result.put("msg", "批量操作失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/hot")
    public Map<String, Object> getHotTags(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Tag> tags = tagService.getHotTags(limit);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", tags);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/activity")
    public Map<String, Object> getActivityTags() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Tag> tags = tagService.getActivityTags();

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", tags);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/search")
    public Map<String, Object> searchTags(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Tag> tags = tagService.lambdaQuery()
                    .like(Tag::getTagName, keyword)
                    .eq(Tag::getStatus, 1)
                    .orderByDesc(Tag::getUseCount)
                    .list();

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", tags);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }
}
