package edu.jjxy.studyroom.backend.common;

/**
 * 系统常量定义
 */
public class Constants {

    private Constants() {}

    // ==================== 用户角色 ====================
    /**
     * 学生
     */
    public static final int ROLE_STUDENT = 0;

    /**
     * 普通管理员
     */
    public static final int ROLE_ADMIN = 1;

    /**
     * 超级管理员
     */
    public static final int ROLE_SUPER_ADMIN = 99;

    // ==================== 用户状态 ====================
    /**
     * 正常
     */
    public static final int STATUS_NORMAL = 0;

    /**
     * 禁用
     */
    public static final int STATUS_DISABLED = 1;

    /**
     * 锁定
     */
    public static final int STATUS_LOCKED = 2;

    // ==================== 座位状态 ====================
    /**
     * 可预约
     */
    public static final int SEAT_AVAILABLE = 0;

    /**
     * 已预约
     */
    public static final int SEAT_RESERVED = 1;

    /**
     * 已占用
     */
    public static final int SEAT_OCCUPIED = 2;

    /**
     * 禁用
     */
    public static final int SEAT_DISABLED = 3;

    /**
     * 维护中
     */
    public static final int SEAT_MAINTENANCE = 4;

    // ==================== 预约状态 ====================
    /**
     * 待签到
     */
    public static final int RESERVE_PENDING = 0;

    /**
     * 进行中
     */
    public static final int RESERVE_IN_PROGRESS = 1;

    /**
     * 已完成
     */
    public static final int RESERVE_COMPLETED = 2;

    /**
     * 已取消
     */
    public static final int RESERVE_CANCELLED = 3;

    /**
     * 爽约
     */
    public static final int RESERVE_NO_SHOW = 4;

    // ==================== 违规类型 ====================
    /**
     * 爽约
     */
    public static final int VIOLATION_NO_SHOW = 0;

    /**
     * 签到超时
     */
    public static final int VIOLATION_SIGN_TIMEOUT = 1;

    /**
     * 占座
     */
    public static final int VIOLATION_OCCUPYING = 2;

    // ==================== Redis Key 前缀 ====================
    /**
     * 登录错误计数
     */
    public static final String REDIS_LOGIN_ERROR_COUNT = "login:error:count:";

    /**
     * 登录锁定
     */
    public static final String REDIS_LOGIN_LOCK = "login:lock:";

    /**
     * 邮箱验证码
     */
    public static final String REDIS_EMAIL_CODE = "email:code:";

    /**
     * 邮箱发送计数（日）
     */
    public static final String REDIS_EMAIL_SEND_DAILY = "email:send:daily:";

    /**
     * 邮箱发送间隔
     */
    public static final String REDIS_EMAIL_SEND_INTERVAL = "email:send:interval:";

    /**
     * 幂等性校验
     */
    public static final String REDIS_IDEMPOTENCY = "idempotency:";

    /**
     * 热门自习室缓存
     */
    public static final String REDIS_HOT_ROOM = "hot:room:";

    /**
     * 热门座位缓存
     */
    public static final String REDIS_HOT_SEAT = "hot:seat:";

    /**
     * 用户会话
     */
    public static final String REDIS_USER_SESSION = "user:session:";

    /**
     * 签到码
     */
    public static final String REDIS_SIGN_CODE = "sign:code:";

    /**
     * 预约提醒
     */
    public static final String REDIS_RESERVE_REMIND = "reserve:remind:";

    // ==================== 系统配置 Key ====================
    /**
     * 最大预约时长
     */
    public static final String CONFIG_MAX_RESERVE_HOUR = "max_reserve_hour";

    /**
     * 提前取消分钟
     */
    public static final String CONFIG_CANCEL_BEFORE_MIN = "cancel_before_min";

    /**
     * 签到超时分钟
     */
    public static final String CONFIG_SIGN_TIMEOUT_MIN = "sign_timeout_min";

    /**
     * 爽约累计次数
     */
    public static final String CONFIG_BREAK_RULE_TIMES = "break_rule_times";

    /**
     * 禁止预约天数
     */
    public static final String CONFIG_BAN_DAYS = "ban_days";

    // ==================== 默认值 ====================
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * 默认昵称前缀（学生）
     */
    public static final String DEFAULT_NAME_PREFIX_STUDENT = "学生_";

    /**
     * 默认昵称前缀（管理员）
     */
    public static final String DEFAULT_NAME_PREFIX_ADMIN = "管理员_";

    /**
     * 默认环境配置
     */
    public static final String DEFAULT_ENVIRONMENT = "插座";

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== 时间常量 ====================
    /**
     * 预约提醒提前分钟数
     */
    public static final int RESERVE_REMIND_MINUTES = 15;

    /**
     * 二维码有效分钟数（签到超时后）
     */
    public static final int QR_CODE_VALID_MINUTES = 10;

    // ==================== 批量操作限制 ====================
    /**
     * 批量操作最大数量
     */
    public static final int BATCH_MAX_SIZE = 50;

    /**
     * 批量添加座位最大数量
     */
    public static final int BATCH_SEAT_MAX_SIZE = 100;

    // ==================== Shiro权限 ====================
    public static final String PERM_USER_VIEW = "user:view";
    public static final String PERM_USER_EDIT = "user:edit";
    public static final String PERM_USER_DISABLE = "user:disable";
    public static final String PERM_ROOM_VIEW = "room:view";
    public static final String PERM_ROOM_EDIT = "room:edit";
    public static final String PERM_ROOM_DELETE = "room:delete";
    public static final String PERM_SEAT_EDIT = "seat:edit";
    public static final String PERM_RESERVE_VIEW = "reserve:view";
    public static final String PERM_RESERVE_MANAGE = "reserve:manage";
    public static final String PERM_RESERVE_DELETE = "reserve:delete";
    public static final String PERM_VIOLATION_VIEW = "violation:view";
    public static final String PERM_VIOLATION_MANAGE = "violation:manage";
    public static final String PERM_NOTICE_VIEW = "notice:view";
    public static final String PERM_NOTICE_EDIT = "notice:edit";
    public static final String PERM_CONFIG_VIEW = "config:view";
    public static final String PERM_CONFIG_EDIT = "config:edit";
    public static final String PERM_STATISTICS_VIEW = "statistics:view";
    public static final String PERM_LOG_VIEW = "log:view";
    public static final String PERM_USER_MANAGE = "user:manage";

    // ==================== 操作日志模块 ====================
    public static final String LOG_MODULE_USER = "用户管理";
    public static final String LOG_MODULE_ROOM = "自习室管理";
    public static final String LOG_MODULE_SEAT = "座位管理";
    public static final String LOG_MODULE_RESERVE = "预约管理";
    public static final String LOG_MODULE_VIOLATION = "违规管理";
    public static final String LOG_MODULE_NOTICE = "公告管理";
    public static final String LOG_MODULE_CONFIG = "系统配置";
    public static final String LOG_MODULE_STATISTICS = "数据统计";
}
