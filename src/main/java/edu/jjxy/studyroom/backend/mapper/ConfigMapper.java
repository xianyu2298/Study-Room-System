package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置Mapper
 */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {

    /**
     * 根据键名查询配置值
     */
    @Select("SELECT config_value FROM config WHERE config_key = #{configKey} LIMIT 1")
    Integer selectValueByKey(@Param("configKey") String configKey);
}
