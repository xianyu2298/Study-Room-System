package edu.jjxy.studyroom.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.Notice;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.entity.dto.NoticeDTO;
import edu.jjxy.studyroom.backend.entity.vo.NoticeVo;
import edu.jjxy.studyroom.backend.mapper.NoticeMapper;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询生效公告（学生端）
     */
    public List<NoticeVo> getActiveNotices() {
        List<Notice> notices = noticeMapper.selectActiveNotices();
        return notices.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    /**
     * 分页查询公告（管理端）
     */
    public Page<NoticeVo> getNoticePage(Integer pageNum, Integer pageSize, String keyword) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getDeleteFlag, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Notice::getTitle, keyword).or().like(Notice::getContent, keyword));
        }
        wrapper.orderByDesc(Notice::getIsTop).orderByDesc(Notice::getCreateTime);
        Page<Notice> result = noticeMapper.selectPage(page, wrapper);

        Page<NoticeVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVo).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 创建公告
     */
    @Transactional(rollbackFor = Exception.class)
    public void createNotice(Long adminId, NoticeDTO dto) {
        if (dto.getIsTop() != null && dto.getIsTop() == 1) {
            noticeMapper.clearAllTop();
        }
        Notice notice = convertToEntity(dto);
        notice.setCreateBy(adminId);
        notice.setDeleteFlag(0);
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        log.info("创建公告 - id: {}, title: {}", notice.getId(), dto.getTitle());
    }

    /**
     * 更新公告
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(Long adminId, NoticeDTO dto) {
        Notice notice = noticeMapper.selectById(dto.getId());
        if (notice == null) throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);

        if (dto.getIsTop() != null && dto.getIsTop() == 1 && notice.getIsTop() != 1) {
            noticeMapper.clearAllTop();
        }

        if (StringUtils.hasText(dto.getTitle())) notice.setTitle(dto.getTitle());
        if (StringUtils.hasText(dto.getContent())) notice.setContent(dto.getContent());
        if (dto.getIsTop() != null) notice.setIsTop(dto.getIsTop());
        if (StringUtils.hasText(dto.getStartTime())) notice.setStartTime(LocalDateTime.parse(dto.getStartTime(), DATETIME_FMT));
        if (StringUtils.hasText(dto.getEndTime())) notice.setEndTime(LocalDateTime.parse(dto.getEndTime(), DATETIME_FMT));
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        log.info("更新公告 - id: {}", dto.getId());
    }

    /**
     * 删除公告（逻辑删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        noticeMapper.logicalDeleteById(id);
        log.info("删除公告 - id: {}", id);
    }

    private Notice convertToEntity(NoticeDTO dto) {
        Notice notice = new Notice();
        if (dto.getId() != null) notice.setId(dto.getId());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        if (StringUtils.hasText(dto.getStartTime())) {
            notice.setStartTime(LocalDateTime.parse(dto.getStartTime(), DATETIME_FMT));
        }
        if (StringUtils.hasText(dto.getEndTime())) {
            notice.setEndTime(LocalDateTime.parse(dto.getEndTime(), DATETIME_FMT));
        }
        return notice;
    }

    private NoticeVo convertToVo(Notice n) {
        NoticeVo vo = new NoticeVo();
        vo.setId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setIsTop(n.getIsTop());
        vo.setIsTopName(n.getIsTop() != null && n.getIsTop() == 1 ? "置顶" : "");
        vo.setStartTime(n.getStartTime());
        vo.setEndTime(n.getEndTime());
        vo.setCreateBy(n.getCreateBy());
        if (n.getCreateBy() != null) {
            User user = userMapper.selectById(n.getCreateBy());
            if (user != null) vo.setCreateByName(user.getName());
        }
        vo.setCreateTime(n.getCreateTime());
        return vo;
    }
}
