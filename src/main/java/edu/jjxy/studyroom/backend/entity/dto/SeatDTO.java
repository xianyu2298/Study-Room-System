package edu.jjxy.studyroom.backend.entity.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 座位创建/更新 DTO
 */
@Data
public class SeatDTO {

    /**
     * 座位ID（更新时需要）
     */
    private Long id;

    /**
     * 所属自习室ID
     */
    @NotNull(message = "自习室ID不能为空")
    private Long roomId;

    /**
     * 座位号（单个座位时使用）
     */
    @NotBlank(message = "座位号不能为空")
    @Length(max = 10, message = "座位号长度不能超过10")
    private String seatNo;

    /**
     * 状态：0=可预约、1=已预约、2=已占用、3=禁用、4=维护中
     */
    private Integer status;

    /**
     * 是否热门：0=否、1=是
     */
    private Integer isHot;
}
