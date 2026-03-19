package edu.jjxy.studyroom.backend.util;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 密码工具类
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 密码复杂度正则：至少6位，包含数字和字母
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");

    private PasswordUtil() {}

    /**
     * 校验密码复杂度
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * MD5加密（多次加密增加安全性）
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        // 使用MD5加密，可以多次迭代
        String encrypted = password;
        for (int i = 0; i < 3; i++) {
            encrypted = DigestUtils.md5DigestAsHex(encrypted.getBytes(StandardCharsets.UTF_8));
        }
        return encrypted;
    }

    /**
     * 校验密码是否正确
     * @param inputPassword 输入的密码（未加密）
     * @param storedPassword 存储的密码（已加密）
     * @return 是否匹配
     */
    public static boolean verifyPassword(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) {
            return false;
        }
        // 兼容 studyroom.sql 历史版本中的 BCrypt 哈希（$2a$/$2b$/$2y$ 开头）
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(inputPassword, storedPassword);
            } catch (Exception e) {
                return false;
            }
        }
        // 当前策略：对明文连续 3 次 MD5
        return encryptPassword(inputPassword).equals(storedPassword);
    }

    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return password.toString();
    }
}
