package cc.forim.trans.shorturl.infra.constant;

/**
 * 普通常量枚举
 *
 * @author Gavin Zhang
 * @version V1.0
 */

public class CommonConstant {

    public static final String HASH = "hash";

    public static final String ZSET = "zset";

    public static final String SET = "set";

    public static final String COLON = ":";

    public static final String PERCENT_SIGN = "%";

    public static final String SUCCESS = "200";
    public static final String FAILED = "500";


    /**
     * 空缓存标识
     */
    public static final String EMPTY_CACHE = "NOPE";

    public static final int EMPTY = 0;

    /**
     * 数据库，已删除
     */
    public static final int DELETED = 1;

    /**
     * 数据库，数据存在
     */
    public static final int EXIST = 0;

    /**
     * 七天的秒数
     */
    public static final long SEVEN_DAY_SECONDS = 604800L;

    /**
     * 现在就失效
     */
    public static final long INVALID_NOW = 1L;
}