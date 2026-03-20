package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 公告 Mapper
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    @Select("SELECT * FROM notice WHERE delete_flag = 0 AND start_time <= NOW() AND end_time >= NOW() ORDER BY is_top DESC, create_time DESC")
    List<Notice> selectActiveNotices();

    @Select("SELECT COUNT(*) FROM notice WHERE is_top = 1 AND delete_flag = 0")
    int countTopNotices();

    @Update("UPDATE notice SET delete_flag = 1 WHERE id = #{id}")
    int logicalDeleteById(@Param("id") Long id);

    @Update("UPDATE notice SET is_top = 0 WHERE is_top = 1 AND delete_flag = 0")
    int clearAllTop();
}
