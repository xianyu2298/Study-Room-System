package edu.jjxy.studyroom.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.Violation;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.entity.vo.ViolationVo;
import edu.jjxy.studyroom.backend.mapper.ViolationMapper;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 违规记录服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ViolationMapper violationMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询违规记录（管理端）
     */
    public Page<ViolationVo> getViolationPage(Integer pageNum, Integer pageSize,
            Long userId, Integer type) {
        Page<Violation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Violation> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(Violation::getUserId, userId);
        if (type != null) wrapper.eq(Violation::getType, type);
        wrapper.orderByDesc(Violation::getCreateTime);
        Page<Violation> result = violationMapper.selectPage(page, wrapper);

        Page<ViolationVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVo).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 标记处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleViolation(Long id, String handleResult) {
        Violation v = violationMapper.selectById(id);
        if (v == null) throw new BusinessException(ResultCode.VIOLATION_NOT_FOUND);
        v.setHandleResult(handleResult);
        violationMapper.updateById(v);
        log.info("标记违规处理结果 - id: {}, result: {}", id, handleResult);
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

        if (v.getUserId() != null) {
            User user = userMapper.selectById(v.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
                vo.setStudentNo(user.getStudentNo());
            }
        }
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
