package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.CommentVote;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论投票 Mapper
 */
@Mapper
public interface CommentVoteMapper extends BaseMapper<CommentVote> {
}