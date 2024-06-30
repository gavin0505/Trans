package cc.forim.trans.shorturl.cache;

import cc.forim.trans.common.utils.RedisUtil;
import cc.forim.trans.shorturl.infra.constant.UrlMapStatus;
import cc.forim.trans.shorturl.infra.dto.UrlMapCacheDTO;
import cc.forim.trans.shorturl.infra.dto.UrlMapRedisDTO;
import cc.forim.trans.shorturl.infra.entity.UrlMap;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

import static cc.forim.trans.shorturl.infra.constant.CacheKey.ACCESS_CODE_STRING_ID_PREFIX;
import static cc.forim.trans.shorturl.infra.constant.CacheKey.ACCESS_CODE_STRING_PREFIX;
import static cc.forim.trans.shorturl.infra.constant.CommonConstant.COLON;

/**
 * UrlMap缓存管理
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Component
public class UrlMapCacheManager {

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    /**
     * 刷新缓存中的UrlMap
     *
     * @param urlMap 新的urlMap
     */
    public void refreshUrlMapCache(UrlMap urlMap) {
        if (ObjectUtil.isNotEmpty(urlMap)) {
            refreshUrlMapCache(function.apply(urlMap));
        }
    }

    private void refreshUrlMapCache(UrlMapCacheDTO dto) {

        UrlMapRedisDTO redisDto = UrlMapRedisDTO.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .longUrl(dto.getLongUrl())
                .compressionCode(dto.getCompressionCode())
                .expireTime(dto.getExpireTime())
                .build();

        // LocalDateTime转换
        LocalDateTime localDateTime = LocalDateTimeUtil.of(dto.getExpireTime());
        // 计算自定义的过期时长
        long score = Long.parseLong(
                String.valueOf(localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() - DateUtil.currentSeconds())
        );
        if (score > 0) {
            redisUtil.set(ACCESS_CODE_STRING_PREFIX.getKey() + dto.getBizType() + COLON
                            + dto.getCompressionCode(),
                    redisDto,
                    score);
            redisUtil.set(ACCESS_CODE_STRING_ID_PREFIX.getKey() + dto.getBizType() + COLON + dto.getCompressionCode(), dto.getId());
//        } else {
//            throw new CreateShortUrlException(Integer.parseInt(EXPIRED_TIME_BEYOND_NOW_EXCEPTION.getCode()),
//                    EXPIRED_TIME_BEYOND_NOW_EXCEPTION.getDescription() + DateUtil.formatDateTime(dto.getExpireTime()));
        }
    }

    /**
     * 转换工具：UrlMap -> UrlMapCacheDto
     */
    private final Function<UrlMap, UrlMapCacheDTO> function = urlMap -> {
        UrlMapCacheDTO urlMapCacheDto = new UrlMapCacheDTO();

        urlMapCacheDto.setId(urlMap.getId());
        urlMapCacheDto.setDescription(urlMap.getDescription());
        urlMapCacheDto.setLongUrl(urlMap.getLongUrl());
        urlMapCacheDto.setShortUrl(urlMap.getShortUrl());
        urlMapCacheDto.setCompressionCode(urlMap.getCompressionCode());
        urlMapCacheDto.setBizType(urlMap.getBizType());
        urlMapCacheDto.setEnable(UrlMapStatus.AVAILABLE.getValue().equals(urlMap.getUrlStatus()));
        urlMapCacheDto.setExpireTime(urlMap.getExpireTime());
        urlMapCacheDto.setUserId(urlMap.getUserId());

        return urlMapCacheDto;
    };
}