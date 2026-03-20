package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notice")
public class Notice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer isTop;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long createBy;

    private Integer deleteFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public boolean isTop() {
        return isTop != null && isTop == 1;
    }

    public boolean isDeleted() {
        return deleteFlag != null && deleteFlag == 1;
    }
}
