package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * 自习室视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomVo {

    /**
     * 自习室ID
     */
    private Long id;

    /**
     * 自习室编号
     */
    private String roomNo;

    /**
     * 自习室名称
     */
    private String name;

    /**
     * 所属区域
     */
    private String area;

    /**
     * 每日开放时间
     */
    private String openTime;

    /**
     * 每日关闭时间
     */
    private String closeTime;

    /**
     * 总座位数
     */
    private Integer totalSeat;

    /**
     * 已添加座位数
     */
    private Integer seatCount;

    /**
     * 可用座位数
     */
    private Integer availableSeatCount;

    /**
     * 环境配置
     */
    private String environment;

    /**
     * 环境配置列表
     */
    private List<String> environmentList;

    /**
     * 状态：0=正常、1=禁用
     */
    private Integer status;

    /**
     * 是否已配置完成
     */
    private Boolean isConfigured;

    /**
     * 今日预约数
     */
    private Integer todayReserveCount;
}
