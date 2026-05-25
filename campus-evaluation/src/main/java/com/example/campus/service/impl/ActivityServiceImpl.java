package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Activity;
import com.example.campus.entity.Tag;
import com.example.campus.repository.ActivityMapper;
import com.example.campus.service.ActivityService;
import com.example.campus.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动业务实现类
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private TagService tagService;

    @Override
    public List<Activity> getActiveActivities() {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .ge("end_time", LocalDateTime.now())
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean createActivity(Activity activity, String tagName) {
        // 创建活动标签
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setTagType(2); // 活动标签
        tag.setUseCount(0);
        tag.setStatus(1);
        tagService.save(tag);

        // 设置活动关联的标签ID
        activity.setTagId(tag.getTagId());
        activity.setParticipantCount(0);
        activity.setPostCount(0);
        activity.setStatus(1);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        return this.save(activity);
    }

    @Override
    @Transactional
    public boolean endActivity(Long activityId) {
        Activity activity = this.getById(activityId);
        if (activity != null) {
            activity.setStatus(0); // 已结束
            activity.setUpdateTime(LocalDateTime.now());
            return this.updateById(activity);
        }
        return false;
    }

    @Override
    @Transactional
    public void incrementParticipantCount(Long activityId) {
        Activity activity = this.getById(activityId);
        if (activity != null) {
            activity.setParticipantCount(activity.getParticipantCount() + 1);
            activity.setUpdateTime(LocalDateTime.now());
            this.updateById(activity);
        }
    }

    @Override
    @Transactional
    public void incrementPostCount(Long activityId) {
        Activity activity = this.getById(activityId);
        if (activity != null) {
            activity.setPostCount(activity.getPostCount() + 1);
            activity.setUpdateTime(LocalDateTime.now());
            this.updateById(activity);
        }
    }
}