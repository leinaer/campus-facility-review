package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campus.annotation.OperationLog;
import com.example.campus.common.Result;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.EvaluationLike;
import com.example.campus.entity.PostCollect;
import com.example.campus.entity.PostLike;
import com.example.campus.entity.User;
import com.example.campus.repository.DailyPostMapper;
import com.example.campus.repository.EvaluationLikeMapper;
import com.example.campus.repository.EvaluationMapper;
import com.example.campus.repository.PostCollectMapper;
import com.example.campus.repository.PostLikeMapper;
import com.example.campus.service.UserService;
import com.example.campus.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private UserService userService;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private DailyPostMapper dailyPostMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Autowired
    private EvaluationLikeMapper evaluationLikeMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @OperationLog(module = "认证", type = "LOGIN", description = "管理员后台登录")
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@RequestBody Map<String, String> param) {
        String token = param.get("token");

        try {
            if (token == null || token.isEmpty()) {
                return Result.error("请输入授权码");
            }

            String ADMIN_SECRET = "2026123";
            if (!ADMIN_SECRET.equals(token)) {
                return Result.error("授权码错误");
            }

            User adminUser = userService.getById(1L);
            if (adminUser == null || !"ADMIN".equals(adminUser.getRole())) {
                return Result.error("管理员账号不存在");
            }

            String jwtToken = JwtUtil.generateToken(adminUser.getUserId().toString(), adminUser.getRole());

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", jwtToken);
            loginData.put("userInfo", adminUser);

            return Result.success(loginData);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("登录失败：" + e.getMessage());
        }
    }


    @OperationLog(module = "认证", type = "LOGIN", description = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> param) {
        String code = param.get("code");

        try {
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            String openid = sessionInfo.getOpenid();

            User user = userService.findByOpenid(openid);
            if (user == null) {
                user = new User();
                user.setOpenId(openid);
                user.setNickname("用户" + System.currentTimeMillis());
                user.setAvatarUrl("https://example.com/default-avatar.png");
                user.setRole("USER");
                userService.save(user);
            }

            String jwtToken = JwtUtil.generateToken(user.getUserId().toString(), user.getRole() != null ? user.getRole() : "USER");

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", jwtToken);
            loginData.put("user", user);

            return Result.success(loginData);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @OperationLog(module = "认证", type = "LOGOUT", description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            JwtUtil.blacklistToken(token);
            return Result.success("退出成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("退出失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/stats")
    public Result<Map<String, Object>> getUserStats(HttpServletRequest request) {
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                return Result.error("请先登录");
            }

            Long userId = Long.parseLong(userIdStr);
            User user = userService.getById(userId);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 查询我的评价数量（设施评价，有facility_id的）
            QueryWrapper<Evaluation> evaluationQuery = new QueryWrapper<>();
            evaluationQuery.eq("user_id", userId)
                    .isNotNull("facility_id")
                    .eq("status", 1);
            long evaluationCount = evaluationMapper.selectCount(evaluationQuery);

            // 查询我的帖子数量
            QueryWrapper<DailyPost> postQuery = new QueryWrapper<>();
            postQuery.eq("user_id", userId)
                    .eq("status", 1);
            long postCount = dailyPostMapper.selectCount(postQuery);

            // 查询我的收藏数量（帖子收藏）
            QueryWrapper<PostCollect> collectQuery = new QueryWrapper<>();
            collectQuery.eq("user_id", userId);
            long collectCount = postCollectMapper.selectCount(collectQuery);

            // 查询我收到的点赞数（我的评价和帖子收到的点赞总和）
            QueryWrapper<EvaluationLike> evaluationLikeQuery = new QueryWrapper<>();
            evaluationLikeQuery.inSql("evaluation_id", 
                    "SELECT evaluation_id FROM evaluation WHERE user_id = " + userId + " AND status = 1 AND facility_id IS NOT NULL");
            long evaluationLikeCount = evaluationLikeMapper.selectCount(evaluationLikeQuery);

            QueryWrapper<PostLike> postLikeQuery = new QueryWrapper<>();
            postLikeQuery.inSql("post_id", 
                    "SELECT post_id FROM daily_post WHERE user_id = " + userId + " AND status = 1");
            long postLikeCount = postLikeMapper.selectCount(postLikeQuery);

            long likeReceived = evaluationLikeCount + postLikeCount;

            Map<String, Object> stats = new HashMap<>();
            stats.put("evaluationCount", evaluationCount);
            stats.put("postCount", postCount);
            stats.put("collectCount", collectCount);
            stats.put("likeReceived", likeReceived);

            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/level")
    public Result<Map<String, Object>> getUserLevel(HttpServletRequest request) {
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                return Result.error("请先登录");
            }

            Long userId = Long.parseLong(userIdStr);

            // 查询统计数据
            QueryWrapper<Evaluation> evaluationQuery = new QueryWrapper<>();
            evaluationQuery.eq("user_id", userId)
                    .isNotNull("facility_id")
                    .eq("status", 1);
            long evaluationCount = evaluationMapper.selectCount(evaluationQuery);

            QueryWrapper<DailyPost> postQuery = new QueryWrapper<>();
            postQuery.eq("user_id", userId)
                    .eq("status", 1);
            long postCount = dailyPostMapper.selectCount(postQuery);

            QueryWrapper<EvaluationLike> evaluationLikeQuery = new QueryWrapper<>();
            evaluationLikeQuery.inSql("evaluation_id", 
                    "SELECT evaluation_id FROM evaluation WHERE user_id = " + userId + " AND status = 1 AND facility_id IS NOT NULL");
            long evaluationLikeCount = evaluationLikeMapper.selectCount(evaluationLikeQuery);

            QueryWrapper<PostLike> postLikeQuery = new QueryWrapper<>();
            postLikeQuery.inSql("post_id", 
                    "SELECT post_id FROM daily_post WHERE user_id = " + userId + " AND status = 1");
            long postLikeCount = postLikeMapper.selectCount(postLikeQuery);

            long likeReceived = evaluationLikeCount + postLikeCount;

            // 计算总积分
            long totalScore = evaluationCount * 10 + postCount * 15 + likeReceived * 5;

            // 计算等级
            int level = 1;
            String title = "新手";
            long minScore = 0;
            long maxScore = 50;
            long nextLevelScore = 50;

            if (totalScore >= 1000) {
                level = 5;
                title = "达人";
                minScore = 1000;
                maxScore = Long.MAX_VALUE;
                nextLevelScore = Long.MAX_VALUE;
            } else if (totalScore >= 500) {
                level = 4;
                title = "专家";
                minScore = 500;
                maxScore = 1000;
                nextLevelScore = 1000;
            } else if (totalScore >= 200) {
                level = 3;
                title = "活跃";
                minScore = 200;
                maxScore = 500;
                nextLevelScore = 500;
            } else if (totalScore >= 50) {
                level = 2;
                title = "进阶";
                minScore = 50;
                maxScore = 200;
                nextLevelScore = 200;
            }

            // 计算进度
            long currentLevelProgress = totalScore - minScore;
            long nextLevelNeed = nextLevelScore - totalScore;
            double progressPercent = 0;
            
            if (nextLevelScore != Long.MAX_VALUE) {
                progressPercent = (double) currentLevelProgress / (maxScore - minScore) * 100;
            } else {
                progressPercent = 100; // 满级
            }

            Map<String, Object> levelData = new HashMap<>();
            levelData.put("level", level);
            levelData.put("title", title);
            levelData.put("totalScore", totalScore);
            levelData.put("evaluationCount", evaluationCount);
            levelData.put("postCount", postCount);
            levelData.put("likeReceived", likeReceived);
            levelData.put("currentLevelProgress", currentLevelProgress);
            levelData.put("nextLevelNeed", nextLevelScore == Long.MAX_VALUE ? 0 : nextLevelNeed);
            levelData.put("progressPercent", Math.min(progressPercent, 100));
            levelData.put("isMaxLevel", nextLevelScore == Long.MAX_VALUE);
            levelData.put("nextLevelTitle", level < 5 ? getNextLevelTitle(level) : "已满级");

            return Result.success(levelData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取等级失败：" + e.getMessage());
        }
    }

    private String getNextLevelTitle(int currentLevel) {
        switch (currentLevel) {
            case 1: return "进阶";
            case 2: return "活跃";
            case 3: return "专家";
            case 4: return "达人";
            default: return "未知";
        }
    }

}
