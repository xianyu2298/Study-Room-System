package edu.jjxy.studyroom.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.entity.Violation;
import edu.jjxy.studyroom.backend.entity.vo.ViolationVo;
import edu.jjxy.studyroom.backend.mapper.ViolationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生违规记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentViolationService {

    private final ViolationMapper violationMapper;
    private final edu.jjxy.studyroom.backend.mapper.UserMapper userMapper;

    /**
     * 查询我的违规记录
     */
    public List<ViolationVo> getMyViolations(Long userId) {
        return violationMapper.selectByUserId(userId).stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    private ViolationVo convertToVo(Violation v) {
        ViolationVo vo = new ViolationVo();
        vo.setId(v.getId());
        vo.setUserId(v.getUserId());
        vo.setReserveId(v.getReserveId());
        vo.setType(v.getType());
        vo.setTypeName(getTypeName(v.getType()));
        vo.setReason(v.getReason());
        vo.setHandleResult(v.getHandleResult());
        vo.setCreateTime(v.getCreateTime());
        return vo;
    }

    private String getTypeName(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 0: return "爽约";
            case 1: return "签到超时";
            case 2: return "占座";
            default: return "未知";
        }
    }
}
