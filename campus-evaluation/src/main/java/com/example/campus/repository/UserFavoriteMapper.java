package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campus.entity.Facility;
import com.example.campus.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户收藏数据访问接口
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    /**
     * 查询用户收藏的设施列表
     */
    @Select("SELECT f.* FROM facility f " +
            "INNER JOIN user_favorite uf ON f.facility_id = uf.facility_id " +
            "WHERE uf.user_id = #{userId} " +
            "ORDER BY uf.create_time DESC")
    IPage<Facility> selectMyFavorites(IPage<Facility> page, @Param("userId") Long userId);
}