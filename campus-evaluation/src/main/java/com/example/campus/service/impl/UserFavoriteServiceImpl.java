package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Facility;
import com.example.campus.entity.UserFavorite;
import com.example.campus.repository.FacilityMapper;
import com.example.campus.repository.UserFavoriteMapper;
import com.example.campus.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户收藏业务实现类
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    @Autowired
    private FacilityMapper facilityMapper;

    @Override
    @Transactional
    public boolean toggleFavorite(Long facilityId, Long userId) {
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("facility_id", facilityId).eq("user_id", userId);
        UserFavorite existingFavorite = this.getOne(queryWrapper);

        if (existingFavorite != null) {
            // 已收藏，取消收藏
            this.remove(queryWrapper);
            return false;
        } else {
            // 未收藏，添加收藏
            UserFavorite newFavorite = new UserFavorite();
            newFavorite.setFacilityId(facilityId);
            newFavorite.setUserId(userId);
            newFavorite.setCreateTime(LocalDateTime.now());
            this.save(newFavorite);
            return true;
        }
    }

    @Override
    public boolean isFavorite(Long facilityId, Long userId) {
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("facility_id", facilityId).eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public IPage<Facility> getMyFavorites(Long userId, int page, int size) {
        Page<Facility> pageParam = new Page<>(page, size);
        return baseMapper.selectMyFavorites(pageParam, userId);
    }
}