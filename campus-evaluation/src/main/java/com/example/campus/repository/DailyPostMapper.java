package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campus.entity.DailyPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 日常分享数据访问接口（Mapper）
 * 继承 BaseMapper 后，自动拥有增删改查方法
 */
@Mapper
public interface DailyPostMapper extends BaseMapper<DailyPost> {

    /**
     * 查询用户收藏的帖子列表
     */
    @Select("SELECT dp.* FROM daily_post dp " +
            "INNER JOIN post_collect pc ON dp.post_id = pc.post_id " +
            "WHERE pc.user_id = #{userId} AND dp.status = 1 " +
            "ORDER BY pc.create_time DESC")
    IPage<DailyPost> selectMyCollects(IPage<DailyPost> page, @Param("userId") Long userId);
}