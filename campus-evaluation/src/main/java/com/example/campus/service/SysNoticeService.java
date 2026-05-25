package com.example.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.SysNotice;
import com.example.campus.repository.SysNoticeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysNoticeService extends ServiceImpl<SysNoticeMapper, SysNotice> {

    public List<SysNotice> getLatestNotices(int limit) {
        QueryWrapper<SysNotice> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("is_top", "publish_time")
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }

    public boolean publishNotice(SysNotice notice) {
        notice.setPublishTime(LocalDateTime.now());
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        return this.save(notice);
    }
}
