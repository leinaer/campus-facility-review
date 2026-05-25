package com.example.campus.controller;

import com.example.campus.entity.Category;
import com.example.campus.exception.BusinessException;
import com.example.campus.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Map<String, Object> getCategories() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Category> categories = categoryService.getActiveCategories();
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", categories);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 管理员获取所有分类（包括禁用的）
     */
    @GetMapping("/admin/list")
    public Map<String, Object> getAllCategories() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Category> categories = categoryService.list();
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", categories);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> addCategory(@RequestBody Category category) {
        Map<String, Object> result = new HashMap<>();
        try {
            category.setStatus(1);
            boolean success = categoryService.save(category);
            if (success) {
                result.put("code", 200);
                result.put("msg", "添加成功");
            } else {
                result.put("code", 500);
                result.put("msg", "添加失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "添加失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> updateCategory(@RequestBody Category category) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = categoryService.updateById(category);
            if (success) {
                result.put("code", 200);
                result.put("msg", "更新成功");
            } else {
                result.put("code", 500);
                result.put("msg", "更新失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = categoryService.removeById(id);
            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 切换分类状态（启用/禁用）
     */
    @PutMapping("/status/{id}")
    public Map<String, Object> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            Category category = categoryService.getById(id);
            if (category == null) {
                result.put("code", 404);
                result.put("msg", "分类不存在");
                return result;
            }

            Integer status = param.get("status");
            if (status == null || (status != 0 && status != 1)) {
                result.put("code", 400);
                result.put("msg", "状态参数错误");
                return result;
            }

            category.setStatus(status);
            boolean success = categoryService.updateById(category);

            if (success) {
                result.put("code", 200);
                result.put("msg", status == 1 ? "启用成功" : "禁用成功");
            } else {
                result.put("code", 500);
                result.put("msg", "操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }
}
