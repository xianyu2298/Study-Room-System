package edu.jjxy.studyroom.backend.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量添加座位 DTO
 */
@Data
public class BatchSeatDTO {

    /**
     * 所属自习室ID
     */
    @NotNull(message = "自习室ID不能为空")
    private Long roomId;

    /**
     * 座位号列表（如：A01, A02, A03...）
     */
    private List<String> seatNos;

    /**
     * 起始座位号（A01格式）
     */
    private String startSeatNo;

    /**
     * 结束座位号（A50格式）
     */
    private String endSeatNo;

    /**
     * 是否热门：0=否、1=是
     */
    private Integer isHot = 0;
}
