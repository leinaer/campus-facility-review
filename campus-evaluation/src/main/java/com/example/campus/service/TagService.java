package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Tag;

import java.util.List;

/**
 * 标签业务接口
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取热门标签列表
     * @param limit 数量限制
     */
    List<Tag> getHotTags(int limit);

    /**
     * 根据标签名称查找或创建标签
     * @param tagName 标签名称
     * @return 标签对象
     */
    Tag findOrCreateTag(String tagName);

    /**
     * 批量查找或创建标签
     * @param tagNames 标签名称列表
     * @return 标签ID列表
     */
    List<Long> findOrCreateTags(List<String> tagNames);

    /**
     * 增加标签使用次数
     * @param tagId 标签ID
     */
    void incrementUseCount(Long tagId);

    /**
     * 获取活动标签列表
     */
    List<Tag> getActivityTags();
}
