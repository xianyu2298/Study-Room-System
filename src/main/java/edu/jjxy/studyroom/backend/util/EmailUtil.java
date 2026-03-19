package edu.jjxy.studyroom.backend.util;

import java.util.regex.Pattern;

/**
 * 邮箱工具类
 */
public class EmailUtil {

    /**
     * 邮箱正则表达式
     * 格式：xxx@xxx.xxx
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private EmailUtil() {}

    /**
     * 校验邮箱格式
     * @param email 邮箱
     * @return 是否合法
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 获取邮箱前缀（@之前部分）
     * @param email 邮箱
     * @return 邮箱前缀
     */
    public static String getEmailPrefix(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        return email.substring(0, email.indexOf("@"));
    }

    /**
     * 脱敏邮箱（保留前两位和@后第一位）
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (!isValidEmail(email)) {
            return email;
        }
        String prefix = getEmailPrefix(email);
        String suffix = email.substring(email.indexOf("@"));
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }
}
