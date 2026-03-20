package edu.jjxy.studyroom.backend.util;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 敏感词过滤工具
 */
@Slf4j
@Component
public class SensitiveWordUtil {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "傻逼", "白痴", "智障", "废物", "垃圾", "傻逼", "混蛋", "王八",
            "滚", "垃圾", "弱智", "有病", "SB", "sb", "FUCK", "fuck"
    ));

    private static final Pattern WORD_PATTERN;

    static {
        StringBuilder regex = new StringBuilder("(");
        boolean first = true;
        for (String word : SENSITIVE_WORDS) {
            if (!first) regex.append("|");
            regex.append(Pattern.quote(word));
            first = false;
        }
        regex.append(")");
        WORD_PATTERN = Pattern.compile(regex.toString());
    }

    /**
     * 检查是否包含敏感词
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return WORD_PATTERN.matcher(text).find();
    }

    /**
     * 过滤敏感词（替换为 *）
     */
    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return WORD_PATTERN.matcher(text).replaceAll("*");
    }

    /**
     * 获取匹配到的第一个敏感词
     */
    public static String findFirst(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        var matcher = WORD_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
