package edu.jjxy.studyroom.backend.service;

import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.mapper.ConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigMapper configMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "config:";
    private static final int CACHE_MINUTES = 10;

    /**
     * 获取整型配置值
     */
    public int getIntValue(String key, int defaultValue) {
        String cacheKey = CACHE_KEY + key;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }

        edu.jjxy.studyroom.backend.entity.Config config = configMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<edu.jjxy.studyroom.backend.entity.Config>()
                        .eq(edu.jjxy.studyroom.backend.entity.Config::getConfigKey, key)
        );

        int value = (config != null && config.getConfigValue() != null) ? config.getConfigValue() : defaultValue;
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(value), CACHE_MINUTES, TimeUnit.MINUTES);
        return value;
    }

    /**
     * 更新配置项
     */
    public void updateConfig(Long adminId, edu.jjxy.studyroom.backend.entity.dto.ConfigDTO dto, String operIp) {
        edu.jjxy.studyroom.backend.entity.Config config = configMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<edu.jjxy.studyroom.backend.entity.Config>()
                        .eq(edu.jjxy.studyroom.backend.entity.Config::getConfigKey, dto.getConfigKey())
        );
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 逻辑校验
        if (dto.getConfigValue() == null || dto.getConfigValue() <= 0) {
            throw new BusinessException(ResultCode.CONFIG_INVALID);
        }

        config.setConfigValue(dto.getConfigValue());
        config.setUpdateBy(adminId);
        config.setUpdateTime(java.time.LocalDateTime.now());
        configMapper.updateById(config);

        // 清除缓存
        clearCache(dto.getConfigKey());
        log.info("更新配置 - key: {}, value: {}, adminId: {}", dto.getConfigKey(), dto.getConfigValue(), adminId);
    }

    /**
     * 清除配置缓存
     */
    public void clearCache(String key) {
        redisTemplate.delete(CACHE_KEY + key);
    }
}
