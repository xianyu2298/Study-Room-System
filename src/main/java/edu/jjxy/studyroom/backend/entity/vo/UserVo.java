package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息VO（脱敏）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {

    private Long id;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 姓名/昵称
     */
    private String name;

    /**
     * 角色
     */
    private Integer role;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 禁止预约结束时间
     */
    private LocalDateTime banEndTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
