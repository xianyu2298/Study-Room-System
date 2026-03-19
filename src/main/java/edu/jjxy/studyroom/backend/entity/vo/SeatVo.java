package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 座位视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatVo {

    /**
     * 座位ID
     */
    private Long id;

    /**
     * 所属自习室ID
     */
    private Long roomId;

    /**
     * 所属自习室名称
     */
    private String roomName;

    /**
     * 座位号
     */
    private String seatNo;

    /**
     * 状态：0=可预约、1=已预约、2=已占用、3=禁用、4=维护中
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 是否热门：0=否、1=是
     */
    private Integer isHot;

    /**
     * 是否可预约
     */
    private Boolean available;

    /**
     * 7天预约次数
     */
    private Integer weekReserveCount;
}
