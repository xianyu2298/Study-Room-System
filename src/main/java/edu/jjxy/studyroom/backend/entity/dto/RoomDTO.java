package edu.jjxy.studyroom.backend.entity.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalTime;

/**
 * 自习室创建/更新 DTO
 */
@Data
public class RoomDTO {

    /**
     * 自习室ID（更新时需要）
     */
    private Long id;

    /**
     * 自习室编号
     */
    @NotBlank(message = "自习室编号不能为空")
    @Length(max = 20, message = "自习室编号长度不能超过20")
    private String roomNo;

    /**
     * 自习室名称
     */
    @NotBlank(message = "自习室名称不能为空")
    @Length(max = 50, message = "自习室名称长度不能超过50")
    private String name;

    /**
     * 所属区域
     */
    @NotBlank(message = "所属区域不能为空")
    @Length(max = 30, message = "所属区域长度不能超过30")
    private String area;

    /**
     * 每日开放时间（格式：HH:mm:ss）
     */
    @NotBlank(message = "开放时间不能为空")
    private String openTime;

    /**
     * 每日关闭时间（格式：HH:mm:ss）
     */
    @NotBlank(message = "关闭时间不能为空")
    private String closeTime;

    /**
     * 总座位数
     */
    @NotNull(message = "总座位数不能为空")
    @Range(min = 1, max = 1000, message = "总座位数需在1-1000之间")
    private Integer totalSeat;

    /**
     * 环境配置（多个用逗号分隔，如：空调,插座,WiFi）
     */
    private String environment;

    /**
     * 状态：0=正常、1=禁用
     */
    private Integer status;
}
