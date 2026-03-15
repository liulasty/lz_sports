package com.lz.util;

import java.util.regex.Pattern;

/**
 * 自定义字符串工具类
 * 适配lz_sports单校版体育赛事管理系统的业务场景，覆盖空值判断、格式校验、字符串处理等核心需求
 */
public final class StringUtils {

    // QQ邮箱正则（项目核心注册规则）
    private static final Pattern QQ_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+@qq\\.com$");

    /**
     * 私有构造方法，禁止实例化工具类
     */
    private StringUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ============================ 基础空值判断 ============================

    /**
     * 判断字符串是否为空（null/空字符串/全空格）
     * @param str 待判断字符串
     * @return true=空，false=非空
     */
    public static boolean isBlank(CharSequence str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        // 遍历判断是否全为空白字符
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否非空（与isBlank相反）
     * @param str 待判断字符串
     * @return true=非空，false=空
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为null或空字符串（不包含全空格）
     * @param str 待判断字符串
     * @return true=空，false=非空
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否非null且非空字符串（不包含全空格）
     * @param str 待判断字符串
     * @return true=非空，false=空
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    // ============================ 业务格式校验 ============================

    /**
     * 校验是否为合法QQ邮箱（项目注册核心规则）
     * @param email 待校验邮箱
     * @return true=合法，false=不合法
     */
    public static boolean isQqEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return QQ_EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 校验学号格式（示例：支持8位数字学号，可根据学校实际规则调整）
     * @param studentId 待校验学号
     * @return true=合法，false=不合法
     */
    public static boolean isStudentId(String studentId) {
        if (isBlank(studentId)) {
            return false;
        }
        // 示例规则：8位纯数字
        return studentId.matches("^\\d{8}$");
    }

    // ============================ 字符串处理 ============================

    /**
     * 字符串空值替换（null/空字符串替换为默认值）
     * @param str 原字符串
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * 去除字符串两端空格，null值返回空字符串
     * @param str 原字符串
     * @return 处理后的字符串
     */
    public static String trim(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 拼接字符串（避免手动拼接+号，支持可变参数）
     * @param args 待拼接的字符串数组
     * @return 拼接后的字符串
     */
    public static String concat(Object... args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg == null ? "" : arg.toString());
        }
        return sb.toString();
    }

    /**
     * 生成赛事编号（业务场景：拼接前缀+日期+随机数）
     * @param prefix 前缀（如EVENT）
     * @param dateStr 日期字符串（如20260303）
     * @param random 随机数（如1234）
     * @return 格式化的赛事编号（如EVENT_20260303_1234）
     */
    public static String generateEventNo(String prefix, String dateStr, int random) {
        if (isBlank(prefix) || isBlank(dateStr)) {
            throw new IllegalArgumentException("赛事编号前缀和日期不能为空");
        }
        return concat(prefix, "_", dateStr, "_", String.format("%04d", random));
    }

    /**
     * 隐藏敏感信息（如邮箱：123456@qq.com → 123****@qq.com）
     * @param str 原字符串（邮箱/手机号等）
     * @param start 保留起始长度
     * @param end 保留结束长度
     * @return 脱敏后的字符串
     */
    public static String hideSensitive(String str, int start, int end) {
        if (isBlank(str) || start < 0 || end < 0 || start + end > str.length()) {
            return str;
        }
        // 处理邮箱特殊场景
        if (str.contains("@")) {
            String[] parts = str.split("@");
            if (parts.length != 2) {
                return str;
            }
            String username = parts[0];
            String domain = parts[1];
            if (username.length() <= start) {
                return concat(username.substring(0, username.length()), "****@", domain);
            }
            return concat(username.substring(0, start), "****", username.substring(username.length() - end), "@", domain);
        }
        // 通用脱敏
        return concat(str.substring(0, start), "****", str.substring(str.length() - end));
    }

    public static boolean hasText(String name) {
        if (name != null && !name.isEmpty()) {
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (!Character.isWhitespace(c)) {
                    return true;
                }
            }
        }
        return false;
    }
}