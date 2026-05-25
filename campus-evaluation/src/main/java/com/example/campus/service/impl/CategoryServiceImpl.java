package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.constant.CacheConstant;
import com.example.campus.entity.Category;
import com.example.campus.repository.CategoryMapper;
import com.example.campus.service.CategoryService;
import com.example.campus.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 分类业务实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<Category> getActiveCategories() {
        String cacheKey = CacheConstant.CATEGORY_LIST;

        if (redisUtil.hasKey(cacheKey)) {
            return (List<Category>) redisUtil.get(cacheKey);
        }

        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).orderByAsc("sort_order");
        List<Category> categories = this.list(queryWrapper);

        redisUtil.set(cacheKey, categories, CacheConstant.CATEGORY_EXPIRE, TimeUnit.SECONDS);

        return categories;
    }

    @Override
    public boolean save(Category entity) {
        boolean result = super.save(entity);
        if (result) {
            redisUtil.delete(CacheConstant.CATEGORY_LIST);
        }
        return result;
    }

    @Override
    public boolean updateById(Category entity) {
        boolean result = super.updateById(entity);
        if (result) {
            redisUtil.delete(CacheConstant.CATEGORY_LIST);
        }
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            redisUtil.delete(CacheConstant.CATEGORY_LIST);
        }
        return result;
    }
}
