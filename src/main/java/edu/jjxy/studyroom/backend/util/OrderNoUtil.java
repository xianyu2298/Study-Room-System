package edu.jjxy.studyroom.backend.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 订单号生成工具
 */
public class OrderNoUtil {

    private OrderNoUtil() {}

    /**
     * 生成预约订单号
     * 格式：RSV + 时间戳 + 随机数
     * @return 订单号
     */
    public static String generateReserveNo() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "RSV" + timestamp + random;
    }

    /**
     * 生成二维码内容
     * @param reserveId 预约ID
     * @param userId 用户ID
     * @return 二维码内容
     */
    public static String generateQrContent(Long reserveId, Long userId) {
        return "STUDYROOM:" + reserveId + ":" + userId + ":" + System.currentTimeMillis();
    }

    /**
     * 生成唯一ID
     * @return UUID
     */
    public static String generateUniqueId() {
        return IdUtil.fastSimpleUUID();
    }
}
