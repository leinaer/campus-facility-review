package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Tag;
import com.example.campus.repository.TagMapper;
import com.example.campus.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签业务实现类
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Tag> getHotTags(int limit) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .orderByDesc("use_count")
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public Tag findOrCreateTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }

        // 查找现有标签
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_name", tagName.trim());
        Tag existingTag = this.getOne(queryWrapper);

        if (existingTag != null) {
            return existingTag;
        }

        // 创建新标签
        Tag newTag = new Tag();
        newTag.setTagName(tagName.trim());
        newTag.setTagType(1); // 普通标签
        newTag.setUseCount(0);
        newTag.setStatus(1);

        this.save(newTag);
        return newTag;
    }

    @Override
    @Transactional
    public List<Long> findOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        return tagNames.stream()
                .map(this::findOrCreateTag)
                .filter(tag -> tag != null)
                .map(Tag::getTagId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void incrementUseCount(Long tagId) {
        Tag tag = this.getById(tagId);
        if (tag != null) {
            tag.setUseCount(tag.getUseCount() + 1);
            this.updateById(tag);
        }
    }

    @Override
    public List<Tag> getActivityTags() {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_type", 2) // 活动标签
                .eq("status", 1)
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }
}
