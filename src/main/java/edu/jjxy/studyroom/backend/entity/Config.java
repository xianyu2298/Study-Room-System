package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("config")
public class Config {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键名
     */
    private String configKey;

    /**
     * 配置值（正整数）
     */
    private Integer configValue;

    /**
     * 配置说明
     */
    private String remark;

    /**
     * 更新人（管理员ID）
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
