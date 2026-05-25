package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.FacilityReply;
import com.example.campus.entity.User;
import com.example.campus.repository.EvaluationMapper;
import com.example.campus.repository.FacilityReplyMapper;
import com.example.campus.repository.UserMapper;
import com.example.campus.service.FacilityReplyService;
import com.example.campus.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设施官方回复业务实现类
 */
@Service
public class FacilityReplyServiceImpl extends ServiceImpl<FacilityReplyMapper, FacilityReply> implements FacilityReplyService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserNotificationService notificationService;

    @Override
    public boolean replyEvaluation(Long evaluationId, Long adminId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        FacilityReply reply = new FacilityReply();
        reply.setEvaluationId(evaluationId);
        reply.setAdminId(adminId);
        reply.setContent(content);
        reply.setCreateTime(LocalDateTime.now());
        reply.setUpdateTime(LocalDateTime.now());

        boolean success = this.save(reply);

        if (success) {
            // 发送官方回复通知
            sendOfficialReplyNotification(evaluationId, adminId);
        }

        return success;
    }

    private void sendOfficialReplyNotification(Long evaluationId, Long adminId) {
        try {
            Evaluation evaluation = evaluationMapper.selectById(evaluationId);
            if (evaluation == null) {
                return;
            }

            User admin = userMapper.selectById(adminId);
            String adminName = admin != null ? admin.getNickname() : "管理员";

            notificationService.createNotification(
                    evaluation.getUserId(),
                    "OFFICIAL_REPLY",
                    "官方回复了你的评价",
                    adminName + " 回复了你对设施的评价",
                    adminId,
                    evaluationId,
                    "EVALUATION"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<FacilityReply> getRepliesByEvaluationId(Long evaluationId) {
        QueryWrapper<FacilityReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evaluation_id", evaluationId)
                    .orderByDesc("create_time");
        return this.list(queryWrapper);
    }
}