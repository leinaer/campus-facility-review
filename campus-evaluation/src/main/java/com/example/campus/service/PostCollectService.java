package com.example.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.PostCollect;

/**
 * 帖子收藏业务接口
 */
public interface PostCollectService extends IService<PostCollect> {

    /**
     * 切换收藏状态
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param folder 收藏夹名称
     * @return true-已收藏，false-已取消收藏
     */
    boolean toggleCollect(Long postId, Long userId, String folder);

    /**
     * 检查用户是否已收藏
     */
    boolean isCollected(Long postId, Long userId);
    /**
     * 获取用户的收藏列表（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 帖子分页数据
     */
    IPage<DailyPost> getMyCollects(Long userId, int page, int size);
}