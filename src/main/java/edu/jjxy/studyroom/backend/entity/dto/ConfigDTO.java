package edu.jjxy.studyroom.backend.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 系统配置更新 DTO
 */
@Data
public class ConfigDTO {

    @NotNull(message = "配置键不能为空")
    private String configKey;

    @NotNull(message = "配置值不能为空")
    private Integer configValue;
}
