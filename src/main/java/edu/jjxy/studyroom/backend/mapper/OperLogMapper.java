package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.OperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志 Mapper（仅INSERT，禁止修改/删除）
 */
@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {

    @Select("SELECT * FROM oper_log ORDER BY oper_time DESC LIMIT #{limit}")
    List<OperLog> selectRecentLogs(@Param("limit") Integer limit);
}
