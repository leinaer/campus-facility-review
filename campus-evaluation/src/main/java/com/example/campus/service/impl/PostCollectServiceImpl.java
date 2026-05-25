package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.PostCollect;
import com.example.campus.repository.DailyPostMapper;
import com.example.campus.repository.PostCollectMapper;
import com.example.campus.service.PostCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 帖子收藏业务实现类
 */
@Service
public class PostCollectServiceImpl extends ServiceImpl<PostCollectMapper, PostCollect> implements PostCollectService {

    @Autowired
    private DailyPostMapper dailyPostMapper;

    @Override
    @Transactional
    public boolean toggleCollect(Long postId, Long userId, String folder) {
        QueryWrapper<PostCollect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId).eq("user_id", userId);
        PostCollect existingCollect = this.getOne(queryWrapper);

        if (existingCollect != null) {
            this.removeById(existingCollect.getCollectId());
            decrementPostCollectCount(postId);
            return false;
        } else {
            PostCollect newCollect = new PostCollect();
            newCollect.setPostId(postId);
            newCollect.setUserId(userId);
            newCollect.setFolder(folder != null ? folder : "default");
            newCollect.setCreateTime(LocalDateTime.now());
            this.save(newCollect);
            incrementPostCollectCount(postId);
            return true;
        }
    }

    @Override
    public boolean isCollected(Long postId, Long userId) {
        QueryWrapper<PostCollect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId).eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public IPage<DailyPost> getMyCollects(Long userId, int page, int size) {
        Page<DailyPost> postPage = new Page<>(page, size);
        return dailyPostMapper.selectMyCollects(postPage, userId);
    }

    private void incrementPostCollectCount(Long postId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post != null) {
            post.setCollectCount(post.getCollectCount() + 1);
            post.setUpdateTime(LocalDateTime.now());
            dailyPostMapper.updateById(post);
        }
    }

    private void decrementPostCollectCount(Long postId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post != null && post.getCollectCount() > 0) {
            post.setCollectCount(post.getCollectCount() - 1);
            post.setUpdateTime(LocalDateTime.now());
            dailyPostMapper.updateById(post);
        }
    }
}