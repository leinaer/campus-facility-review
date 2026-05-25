package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.EvaluationTagRelation;
import com.example.campus.entity.Facility;
import com.example.campus.entity.Tag;
import com.example.campus.exception.BusinessException;
import com.example.campus.repository.EvaluationMapper;
import com.example.campus.repository.EvaluationTagRelationMapper;
import com.example.campus.repository.FacilityMapper;
import com.example.campus.service.EvaluationService;
import com.example.campus.service.TagService;
import com.example.campus.utils.RedisUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.campus.utils.SensitiveWordFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Autowired
    private FacilityMapper facilityMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private TagService tagService;

    @Autowired
    private EvaluationTagRelationMapper evaluationTagRelationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getByFacilityId(Long facilityId, String sortBy, int page, int size) {
        Page<Evaluation> evaluationPage = new Page<>(page, size);
        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("facility_id", facilityId)
                .eq("status", 1);

        if ("high".equals(sortBy)) {
            queryWrapper.orderByDesc("rating");
        } else if ("low".equals(sortBy)) {
            queryWrapper.orderByAsc("rating");
        } else if ("withImage".equals(sortBy)) {
            queryWrapper.isNotNull("images")
                    .ne("images", "")
                    .orderByDesc("create_time");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<Evaluation> resultPage = this.page(evaluationPage, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", resultPage.getPages());

        return result;
    }

    @Override
    @Transactional
    public boolean publishEvaluation(Evaluation evaluation) {
        if (evaluation.getFacilityId() == null) {
            throw new BusinessException(400, "设施ID不能为空");
        }
        if (evaluation.getRating() == null || evaluation.getRating() < 1 || evaluation.getRating() > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }

        if (evaluation.getContent() == null || evaluation.getContent().trim().isEmpty()) {
            evaluation.setContent("该用户暂未填写具体评价内容");
        } else {
            evaluation.setContent(sensitiveWordFilter.filter(evaluation.getContent()));
        }

        evaluation.setLikeCount(0);
        evaluation.setCommentCount(0);
        evaluation.setHelpfulCount(0);
        evaluation.setStatus(1);
        evaluation.setCreateTime(LocalDateTime.now());
        evaluation.setUpdateTime(LocalDateTime.now());

        boolean success = this.save(evaluation);

        if (success) {
            updateFacilityRating(evaluation.getFacilityId());

            if (evaluation.getTags() != null && !evaluation.getTags().isEmpty()) {
                saveEvaluationTags(evaluation.getEvaluationId(), evaluation.getTags());
            }
        }

        return success;
    }

    private void saveEvaluationTags(Long evaluationId, String tagsJson) {
        try {
            List<String> tagNames = objectMapper.readValue(tagsJson, new TypeReference<List<String>>(){});
            if (tagNames == null || tagNames.isEmpty()) {
                return;
            }

            List<Long> tagIds = tagService.findOrCreateTags(tagNames);

            for (Long tagId : tagIds) {
                EvaluationTagRelation relation = new EvaluationTagRelation();
                relation.setEvaluationId(evaluationId);
                relation.setTagId(tagId);
                evaluationTagRelationMapper.insert(relation);

                tagService.incrementUseCount(tagId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Evaluation> getByUserId(Long userId) {
        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .isNotNull("facility_id")
                .eq("status", 1)
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public List<Evaluation> getLatestEvaluations(int limit, String filterType) {
        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);

        if ("featured".equals(filterType)) {
            queryWrapper.and(wrapper -> wrapper
                    .ge("like_count", 3)
                    .or()
                    .isNotNull("images")
                    .and(imageWrapper -> imageWrapper.ne("images", ""))
            );
        } else if ("withImage".equals(filterType)) {
            queryWrapper.isNotNull("images")
                    .ne("images", "");
        } else if ("highRating".equals(filterType)) {
            queryWrapper.ge("rating", 4);
        }

        queryWrapper.orderByDesc("create_time")
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean deleteEvaluation(Long evaluationId, Long userId) {
        Evaluation evaluation = this.getById(evaluationId);

        if (evaluation == null) {
            return false;
        }

        // 只能删除自己的评价
        if (!evaluation.getUserId().equals(userId)) {
            return false;
        }

        // 软删除
        evaluation.setStatus(0);
        evaluation.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(evaluation);

        if (success) {
            // 更新设施评分和评价数
            updateFacilityRating(evaluation.getFacilityId());
        }

        return success;
    }

    private void updateFacilityRating(Long facilityId) {
        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("facility_id", facilityId)
                .eq("status", 1);
        List<Evaluation> evaluations = this.list(queryWrapper);

        if (evaluations != null && !evaluations.isEmpty()) {
            double avgRating = evaluations.stream()
                    .mapToInt(Evaluation::getRating)
                    .average()
                    .orElse(0.0);

            Facility facility = facilityMapper.selectById(facilityId);
            if (facility != null) {
                facility.setRating(BigDecimal.valueOf(avgRating).setScale(1, java.math.RoundingMode.HALF_UP));
                facility.setReviewCount(evaluations.size());
                facility.setUpdateTime(LocalDateTime.now());
                facilityMapper.updateById(facility);

                redisUtil.delete(String.format(com.example.campus.constant.CacheConstant.FACILITY_DETAIL, facilityId));
                clearFacilityTopCache();
            }
        }
    }

    private void clearFacilityTopCache() {
        redisUtil.delete(String.format(com.example.campus.constant.CacheConstant.FACILITY_TOP, "high", 10));
        redisUtil.delete(String.format(com.example.campus.constant.CacheConstant.FACILITY_TOP, "low", 10));
        redisUtil.delete(String.format(com.example.campus.constant.CacheConstant.FACILITY_TOP, "high", 20));
        redisUtil.delete(String.format(com.example.campus.constant.CacheConstant.FACILITY_TOP, "low", 20));
        redisUtil.delete(String.format(com.example.campus.constant.CacheConstant.FACILITY_TOP, "high", 50));
        redisUtil.delete(String.format(com.example.campus.constant.CacheConstant.FACILITY_TOP, "low", 50));
    }
}
