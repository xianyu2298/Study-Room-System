package edu.jjxy.studyroom.backend.entity.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 评价 DTO
 */
@Data
public class ReviewDTO {

    @NotNull(message = "预约ID不能为空")
    private Long reserveId;

    @NotNull(message = "评分不能为空")
    @Range(min = 1, max = 5, message = "评分需在1-5之间")
    private Integer score;

    @NotBlank(message = "评价内容不能为空")
    @Length(max = 200, message = "评价内容长度不能超过200")
    private String content;
}
