package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(value = "notice_id", type = IdType.AUTO)
    private Long noticeId;

    private String title;

    private String content;

    private LocalDateTime publishTime;

    private Integer isTop;
}
