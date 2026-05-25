package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.PostLike;
import com.example.campus.entity.User;
import com.example.campus.repository.DailyPostMapper;
import com.example.campus.repository.PostLikeMapper;
import com.example.campus.repository.UserMapper;
import com.example.campus.service.PostLikeService;
import com.example.campus.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 帖子点赞业务实现类
 */
@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {

    @Autowired
    private DailyPostMapper dailyPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserNotificationService notificationService;

    @Override
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        QueryWrapper<PostLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId).eq("user_id", userId);
        PostLike existingLike = this.getOne(queryWrapper);

        if (existingLike != null) {
            this.removeById(existingLike.getLikeId());
            decrementPostLikeCount(postId);
            return false;
        } else {
            PostLike newLike = new PostLike();
            newLike.setPostId(postId);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            this.save(newLike);
            incrementPostLikeCount(postId);
            
            // 发送点赞通知
            sendLikeNotification(postId, userId);
            
            return true;
        }
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        QueryWrapper<PostLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId).eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    private void incrementPostLikeCount(Long postId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            post.setUpdateTime(LocalDateTime.now());
            dailyPostMapper.updateById(post);
        }
    }

    private void decrementPostLikeCount(Long postId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post != null && post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            post.setUpdateTime(LocalDateTime.now());
            dailyPostMapper.updateById(post);
        }
    }

    private void sendLikeNotification(Long postId, Long fromUserId) {
        try {
            DailyPost post = dailyPostMapper.selectById(postId);
            if (post == null || post.getUserId().equals(fromUserId)) {
                return;
            }

            User fromUser = userMapper.selectById(fromUserId);
            String userName = fromUser != null ? fromUser.getNickname() : "匿名用户";

            notificationService.createNotification(
                    post.getUserId(),
                    "LIKE",
                    "有人赞了你的帖子",
                    userName + " 赞了你的帖子",
                    fromUserId,
                    postId,
                    "POST"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}