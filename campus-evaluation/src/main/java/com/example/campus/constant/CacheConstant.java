package com.example.campus.constant;

/**
 * Redis缓存常量
 */
public class CacheConstant {

    /**
     * 缓存前缀
     */
    public static final String CACHE_PREFIX = "campus:";

    /**
     * 分类列表缓存key
     */
    public static final String CATEGORY_LIST = CACHE_PREFIX + "category:list";

    /**
     * 设施详情缓存key模板
     */
    public static final String FACILITY_DETAIL = CACHE_PREFIX + "facility:detail:%d";

    /**
     * 热门设施榜单缓存key
     */
    public static final String FACILITY_TOP = CACHE_PREFIX + "facility:top:%s:%d";

    /**
     * 用户Token黑名单（用于登出）
     */
    public static final String TOKEN_BLACKLIST = CACHE_PREFIX + "token:blacklist:%s";

    /**
     * 验证码缓存key
     */
    public static final String VERIFY_CODE = CACHE_PREFIX + "verify:code:%s";

    /**
     * 热门搜索词缓存key
     */
    public static final String HOT_SEARCH_KEYWORDS = CACHE_PREFIX + "search:hot";


    /**
     * 缓存过期时间（秒）
     */
    public static final long CATEGORY_EXPIRE = 3600; // 分类缓存1小时
    public static final long FACILITY_DETAIL_EXPIRE = 300; // 设施详情缓存5分钟
    public static final long FACILITY_TOP_EXPIRE = 600; // 排行榜缓存10分钟
    public static final long TOKEN_BLACKLIST_EXPIRE = 86400; // Token黑名单24小时
    public static final long HOT_SEARCH_KEYWORDS_EXPIRE = 3600; // 热门搜索缓存1小时
}
