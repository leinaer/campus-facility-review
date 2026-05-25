package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Category;

import java.util.List;

/**
 * 分类业务接口
 */
public interface CategoryService extends IService<Category> {
    
    /**
     * 获取所有启用的分类（按排序号升序）
     */
    List<Category> getActiveCategories();
}
