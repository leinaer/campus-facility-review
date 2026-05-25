package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {
    List<Banner> getActiveBanners();
}
