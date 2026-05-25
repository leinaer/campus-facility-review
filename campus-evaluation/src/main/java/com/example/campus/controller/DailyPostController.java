//package com.example.campus.controller;
//
//import com.example.campus.annotation.OperationLog;
//import com.example.campus.entity.DailyPost;
//import com.example.campus.exception.BusinessException;
//import com.example.campus.service.DailyPostService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.servlet.http.HttpServletRequest;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/post")
//public class DailyPostController {
//
//    @Autowired
//    private DailyPostService dailyPostService;
//
//    @PostMapping("/publish")
//    @OperationLog(module = "帖子管理", type = "发布", description = "发布日常帖子")
//    public Map<String, Object> publishPost(@RequestBody DailyPost post, HttpServletRequest request) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            String userIdStr = (String) request.getAttribute("userId");
//            if (userIdStr == null) {
//                result.put("code", 401);
//                result.put("msg", "请先登录");
//                return result;
//            }
//
//            Long userId = Long.parseLong(userIdStr);
//            post.setUserId(userId);
//
//            boolean success = dailyPostService.publishPost(post);
//
//            if (success) {
//                result.put("code", 200);
//                result.put("msg", "发布成功");
//                result.put("postId", post.getPostId());
//            } else {
//                result.put("code", 500);
//                result.put("msg", "发布失败");
//            }
//        } catch (BusinessException e) {
//            result.put("code", e.getCode());
//            result.put("msg", e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "发布失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @GetMapping("/discover")
//    public Map<String, Object> getDiscoverPosts(@RequestParam(defaultValue = "hot") String sortBy,
//                                                @RequestParam(defaultValue = "1") int page,
//                                                @RequestParam(defaultValue = "10") int size) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            Map<String, Object> data = dailyPostService.getDiscoverPosts(page, size, sortBy);
//
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", data);
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @GetMapping("/detail/{postId}")
//    public Map<String, Object> getPostDetail(@PathVariable Long postId) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            DailyPost post = dailyPostService.getPostDetail(postId);
//
//            if (post != null) {
//                result.put("code", 200);
//                result.put("msg", "获取成功");
//                result.put("data", post);
//            } else {
//                result.put("code", 404);
//                result.put("msg", "帖子不存在");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @GetMapping("/my-posts")
//    public Map<String, Object> getMyPosts(@RequestParam(defaultValue = "1") int page,
//                                          @RequestParam(defaultValue = "10") int size,
//                                          HttpServletRequest request) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            String userIdStr = (String) request.getAttribute("userId");
//            if (userIdStr == null) {
//                result.put("code", 401);
//                result.put("msg", "请先登录");
//                return result;
//            }
//
//            Long userId = Long.parseLong(userIdStr);
//            Map<String, Object> data = dailyPostService.getMyPosts(userId, page, size);
//
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", data);
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @GetMapping("/by-topic/{topicId}")
//    public Map<String, Object> getPostsByTopic(@PathVariable Long topicId,
//                                               @RequestParam(defaultValue = "1") int page,
//                                               @RequestParam(defaultValue = "10") int size) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            Map<String, Object> data = dailyPostService.getPostsByTopic(topicId, page, size);
//
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", data);
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @DeleteMapping("/{postId}")
//    @OperationLog(module = "帖子管理", type = "删除", description = "删除帖子")
//    public Map<String, Object> deletePost(@PathVariable Long postId, HttpServletRequest request) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            String userIdStr = (String) request.getAttribute("userId");
//            if (userIdStr == null) {
//                result.put("code", 401);
//                result.put("msg", "请先登录");
//                return result;
//            }
//
//            Long userId = Long.parseLong(userIdStr);
//            boolean success = dailyPostService.deletePost(postId, userId);
//
//            if (success) {
//                result.put("code", 200);
//                result.put("msg", "删除成功");
//            } else {
//                result.put("code", 403);
//                result.put("msg", "无权删除或帖子不存在");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "删除失败：" + e.getMessage());
//        }
//        return result;
//    }
//}


package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.annotation.OperationLog;
import com.example.campus.common.Result;
import com.example.campus.entity.DailyPost;
import com.example.campus.exception.BusinessException;
import com.example.campus.service.DailyPostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/post")
public class DailyPostController {

    @Autowired
    private DailyPostService dailyPostService;

    @OperationLog(module = "帖子管理", type = "发布", description = "发布日常帖子")
    @PostMapping("/publish")
    public Map<String, Object> publishPost(@RequestBody DailyPost post, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            post.setUserId(userId);

            boolean success = dailyPostService.publishPost(post);

            if (success) {
                result.put("code", 200);
                result.put("msg", "发布成功");
                result.put("postId", post.getPostId());
            } else {
                result.put("code", 500);
                result.put("msg", "发布失败");
            }
        } catch (BusinessException e) {
            result.put("code", e.getCode());
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "发布失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/discover")
    public Map<String, Object> getDiscoverPosts(@RequestParam(defaultValue = "hot") String sortBy,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = dailyPostService.getDiscoverPosts(page, size, sortBy);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/detail/{postId}")
    public Map<String, Object> getPostDetail(@PathVariable Long postId) {
        Map<String, Object> result = new HashMap<>();
        try {
            DailyPost post = dailyPostService.getPostDetail(postId);

            if (post != null) {
                result.put("code", 200);
                result.put("msg", "获取成功");
                result.put("data", post);
            } else {
                result.put("code", 404);
                result.put("msg", "帖子不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/my-posts")
    public Map<String, Object> getMyPosts(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            Map<String, Object> data = dailyPostService.getMyPosts(userId, page, size);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/by-topic/{topicId}")
    public Map<String, Object> getPostsByTopic(@PathVariable Long topicId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = dailyPostService.getPostsByTopic(topicId, page, size);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{postId}")
    @OperationLog(module = "帖子管理", type = "删除", description = "删除帖子")
    public Map<String, Object> deletePost(@PathVariable Long postId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean success = dailyPostService.deletePost(postId, userId);

            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 403);
                result.put("msg", "无权删除或帖子不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/admin/list")
    public Map<String, Object> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<DailyPost> postPage = new Page<>(page, size);
            LambdaQueryWrapper<DailyPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(DailyPost::getCreateTime);

            Page<DailyPost> pageResult = dailyPostService.page(postPage, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getRecords());
            data.put("total", pageResult.getTotal());
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/admin/status/{postId}")
    public Map<String, Object> adminUpdateStatus(
            @PathVariable Long postId,
            @RequestBody Map<String, Integer> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer status = param.get("status");
            DailyPost post = dailyPostService.getById(postId);
            if (post == null) {
                result.put("code", 404);
                result.put("msg", "帖子不存在");
                return result;
            }
            post.setStatus(status);
            dailyPostService.updateById(post);
            result.put("code", 200);
            result.put("msg", "操作成功");
        } catch (Exception e) {
            log.error("更新帖子状态失败", e);
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
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
                result.put("msg", "请选择要删除的帖子");
                return result;
            }
            dailyPostService.removeByIds(ids);
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
                result.put("msg", "请选择要操作的帖子");
                return result;
            }

            List<DailyPost> posts = dailyPostService.listByIds(ids);
            posts.forEach(post -> post.setStatus(status));
            dailyPostService.updateBatchById(posts);

            result.put("code", 200);
            result.put("msg", "批量操作成功");
        } catch (Exception e) {
            log.error("批量更新状态失败", e);
            result.put("code", 500);
            result.put("msg", "批量操作失败：" + e.getMessage());
        }
        return result;
    }
}
