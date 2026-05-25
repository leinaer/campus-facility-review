package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Activity;

import java.util.List;

/**
 * 活动业务接口
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 获取进行中的活动列表
     */
    List<Activity> getActiveActivities();

    /**
     * 创建活动（同时创建关联的活动标签）
     */
    boolean createActivity(Activity activity, String tagName);

    /**
     * 结束活动
     */
    boolean endActivity(Long activityId);

    /**
     * 增加活动参与人数
     */
    void incrementParticipantCount(Long activityId);

    /**
     * 增加活动帖子数量
     */
    void incrementPostCount(Long activityId);
}
