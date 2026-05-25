package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.CommentLike;

public interface CommentLikeService extends IService<CommentLike> {

    boolean toggleLike(Long commentId, Long userId);

    boolean isLiked(Long commentId, Long userId);
}
