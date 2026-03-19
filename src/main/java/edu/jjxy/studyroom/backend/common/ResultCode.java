package edu.jjxy.studyroom.backend.common;

/**
 * 响应状态码枚举
 * 错误码规范：
 * 1000-1999: 用户模块
 * 2000-2999: 预约模块
 * 3000-3999: 签到模块
 * 4000-4999: 管理员模块
 * 5000-5999: 系统模块
 */
public enum ResultCode {

    // ==================== 成功 ====================
    SUCCESS(0, "操作成功"),

    // ==================== 用户模块 (1000-1999) ====================
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "该学号/邮箱已注册"),
    USER_DISABLED(1003, "该账号已被禁用"),
    USER_LOCKED(1004, "账号临时锁定，请稍后重试"),
    USER_BANNED(1005, "您因爽约次数过多，被禁止预约至%s"),
    PASSWORD_ERROR(1006, "密码错误"),
    PASSWORD_FORMAT_ERROR(1007, "密码需至少6位，且包含数字和字母"),
    EMAIL_FORMAT_ERROR(1008, "邮箱格式不正确"),
    EMAIL_ALREADY_EXISTS(1009, "该邮箱已注册"),
    STUDENT_NO_ALREADY_EXISTS(1010, "该学号已注册"),
    EMAIL_CODE_EXPIRED(1011, "验证码已过期，请重新获取"),
    EMAIL_CODE_ERROR(1012, "验证码错误"),
    EMAIL_CODE_FREQUENT(1013, "发送过于频繁，请稍后再试"),
    EMAIL_CODE_DAILY_LIMIT(1014, "今日发送次数已达上限"),
    EMAIL_CODE_NOT_FOUND(1015, "请先获取验证码"),
    IDEMPOTENT_ERROR(1016, "请勿重复提交"),
    OLD_PASSWORD_ERROR(1017, "原密码错误"),
    LOGIN_MAX_ERROR(1018, "连续密码错误%d次，账号已锁定%s分钟"),

    // ==================== 预约模块 (2000-2999) ====================
    RESERVE_NOT_FOUND(2001, "预约记录不存在"),
    RESERVE_CONFLICT(2002, "您当前已有有效预约，不可重复预约"),
    RESERVE_SEAT_UNAVAILABLE(2003, "该座位已被预约，请重新选择"),
    RESERVE_TIME_INVALID(2004, "仅支持当日预约，请勿选择跨天时段"),
    RESERVE_TIME_BEFORE_NOW(2005, "预约开始时间不能早于当前时间"),
    RESERVE_TIME_BEFORE_OPEN(2006, "预约开始时间不能早于自习室开放时间"),
    RESERVE_TIME_AFTER_CLOSE(2007, "预约结束时间不能晚于自习室关闭时间"),
    RESERVE_DURATION_INVALID(2008, "预约时长需在1-%d小时之间"),
    RESERVE_TIME_GRANULARITY_ERROR(2009, "预约时段需为30分钟整数倍"),
    RESERVE_CANCEL_TOO_LATE(2010, "临近预约开始，不可取消"),
    RESERVE_CANNOT_CANCEL(2011, "该预约状态不可操作"),
    RESERVE_NOT_YOURS(2012, "无权操作此预约"),
    ROOM_NOT_CONFIGURED(2013, "该自习室暂未配置完成，暂不可预约"),

    // ==================== 签到模块 (3000-3999) ====================
    SIGN_TIMEOUT(3001, "签到超时，预约已取消并记爽约1次"),
    SIGN_TIME_NOT_ARRIVED(3002, "未到签到时间，请稍后再试"),
    SIGN_STATUS_ERROR(3003, "预约状态不允许签到"),
    SIGN_DEVICE_ERROR(3004, "请在预约登录的设备上完成签到"),
    SIGN_CODE_EXPIRED(3005, "签到码已过期"),
    SIGN_CODE_ERROR(3006, "签到码错误"),
    SIGN_CODE_USED(3007, "签到码已使用"),
    SIGN_OUT_TIME_ERROR(3008, "未到预约开始时间或已过结束时间，无法签退"),

    // ==================== 管理员模块 (4000-4999) ====================
    NO_PERMISSION(4001, "权限不足"),
    ROOM_NOT_FOUND(4002, "自习室不存在"),
    ROOM_NO_EXISTS(4003, "该自习室编号已存在"),
    ROOM_NAME_EXISTS(4004, "该自习室名称已存在"),
    ROOM_HAS_EFFECTIVE_RESERVE(4005, "该自习室存在有效预约，不可删除"),
    ROOM_SEAT_COUNT_ERROR(4006, "新总座位数不可小于当前已添加座位数（%d个）"),
    SEAT_NOT_FOUND(4007, "座位不存在"),
    SEAT_NO_EXISTS(4008, "该自习室已存在该座位号"),
    SEAT_COUNT_EXCEED(4009, "该自习室总座位数为%d，已添加%d个，不可超量添加"),
    SEAT_BATCH_MAX_LIMIT(4010, "单次批量操作最多%s条"),
    AREA_EXISTS(4011, "该区域已存在"),
    AREA_HAS_ROOMS(4012, "该区域下存在自习室，请先删除/迁移"),
    USER_NOT_STUDENT(4013, "该用户不是学生账号"),
    USER_CANNOT_DELETE(4014, "学生账号不可删除，仅支持禁用"),
    BATCH_MAX_LIMIT(4015, "单次批量操作最多%d条，请分批处理"),
    VIOLATION_NOT_FOUND(4016, "违规记录不存在"),
    CONFIG_INVALID(4017, "配置项逻辑冲突，请重新设置"),
    NOTICE_NOT_FOUND(4018, "公告不存在"),
    TOP_NOTICE_EXISTS(4019, "已存在置顶公告，请先取消原有置顶"),

    // ==================== 系统模块 (5000-5999) ====================
    INTERNAL_SERVER_ERROR(5001, "服务器异常，请稍后重试"),
    PARAMETER_ERROR(5002, "参数校验失败：%s"),
    DATA_NOT_FOUND(5003, "数据不存在"),
    DATA_CONFLICT(5004, "数据冲突"),
    NETWORK_ERROR(5005, "网络异常，请稍后重试"),
    CAPTCHA_ERROR(5006, "验证码错误"),
    CAPTCHA_EXPIRED(5007, "验证码已过期，请刷新重试"),
    REQUEST_METHOD_ERROR(5008, "请求方法不支持"),
    TOKEN_EXPIRED(5009, "登录已过期，请重新登录"),
    TOKEN_INVALID(5010, "无效的认证信息，请重新登录"),
    TOKEN_ERROR(5011, "认证信息解析失败，请重新登录");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
