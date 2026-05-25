package com.example.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Facility;
import com.example.campus.entity.UserFavorite;

/**
 * 用户收藏业务接口
 */
public interface UserFavoriteService extends IService<UserFavorite> {

    boolean toggleFavorite(Long facilityId, Long userId);

    boolean isFavorite(Long facilityId, Long userId);

    IPage<Facility> getMyFavorites(Long userId, int page, int size);
}