package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Banner;
import com.example.campus.repository.BannerMapper;
import com.example.campus.service.BannerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> getActiveBanners() {
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .orderByAsc("sort_order");
        return this.list(wrapper);
    }
}
