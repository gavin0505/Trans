package cc.forim.trans.shorturl.infra.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 序列转换工具
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public enum ConvertSequenceUtil {

    /**
     * 枚举单例
     */
    INSTANCE;

    /**
     * 63个字符的字典
     */
    private static final String DIC = "X6Mk0NbihHuLPJgS7da3Ywr8RcWxUQDe5FfnKvto1pCOIj49zq-2VyEGmZTAlBs";
    private static final int SCALE = 63;
    private static final int MIN_LENGTH = 7;

    /**
     * 数字转63进制，不足7位则左填充0
     *
     * @param num 唯一数
     * @return 63进制数
     */
    public String encode63(Long num) {
        StringBuilder builder = new StringBuilder();
        int remainder;
        while (num > SCALE - 1) {
            remainder = Long.valueOf(num % SCALE).intValue();
            builder.append(DIC.charAt(remainder));
            num = num / SCALE;
        }
        builder.append(DIC.charAt(num.intValue()));
        String value = builder.reverse().toString();
        return StringUtils.leftPad(value, MIN_LENGTH, '0');
    }
}