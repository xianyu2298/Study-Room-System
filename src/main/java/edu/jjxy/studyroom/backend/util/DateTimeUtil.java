package edu.jjxy.studyroom.backend.util;

import cn.hutool.core.date.DateUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateTimeUtil {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    private DateTimeUtil() {}

    // ==================== 时间戳转换 ====================

    /**
     * 获取当前时间戳（毫秒）
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前服务器时间
     */
    public static LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期
     */
    public static LocalDate getToday() {
        return LocalDate.now();
    }

    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime转Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // ==================== 格式化 ====================

    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化时间
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析时间字符串
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    // ==================== 时间计算 ====================

    /**
     * 计算两个时间的分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 计算两个时间的小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算预约时长（小时）
     */
    public static long calculateDurationHours(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 判断是否为同一天
     */
    public static boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }

    /**
     * 判断是否为当日预约
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 校准时间到30分钟粒度（向下取整）
     */
    public static LocalDateTime roundDownTo30Minutes(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        int roundedMinute = (minute / 30) * 30;
        return dateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
    }

    /**
     * 校准时间到30分钟粒度（向上取整）
     */
    public static LocalDateTime roundUpTo30Minutes(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        if (minute % 30 == 0) {
            return dateTime.withSecond(0).withNano(0);
        }
        int roundedMinute = ((minute / 30) + 1) * 30;
        if (roundedMinute >= 60) {
            return dateTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }
        return dateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
    }

    /**
     * 判断是否为30分钟整数倍
     */
    public static boolean isMultipleOf30Minutes(LocalDateTime start, LocalDateTime end) {
        long minutes = minutesBetween(start, end);
        return minutes % 30 == 0 && minutes >= 30;
    }

    /**
     * 获取一天的开始时间
     */
    public static LocalDateTime getDayStart(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 获取一天的结束时间
     */
    public static LocalDateTime getDayEnd(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}
