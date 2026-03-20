package edu.jjxy.studyroom.backend.service;

import edu.jjxy.studyroom.backend.entity.OperLog;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.entity.vo.OperLogVo;
import edu.jjxy.studyroom.backend.mapper.OperLogMapper;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogService {

    private final OperLogMapper operLogMapper;
    private final UserMapper userMapper;

    /**
     * 记录操作日志
     */
    public void log(Long operId, String module, String content, String ip) {
        OperLog log = new OperLog();
        log.setOperId(operId);
        log.setOperModule(module);
        log.setOperContent(content);
        log.setOperIp(ip);
        log.setOperTime(java.time.LocalDateTime.now());
        operLogMapper.insert(log);
    }

    /**
     * 查询最近日志
     */
    public List<OperLogVo> getRecentLogs(int limit) {
        return operLogMapper.selectRecentLogs(limit).stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    private OperLogVo convertToVo(OperLog log) {
        OperLogVo vo = new OperLogVo();
        vo.setId(log.getId());
        vo.setOperId(log.getOperId());
        vo.setOperModule(log.getOperModule());
        vo.setOperContent(log.getOperContent());
        vo.setOperIp(log.getOperIp());
        vo.setOperTime(log.getOperTime());
        if (log.getOperId() != null) {
            User user = userMapper.selectById(log.getOperId());
            if (user != null) vo.setOperName(user.getName());
        }
        return vo;
    }
}
