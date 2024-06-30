package cc.forim.trans.shorturl.dao;

import cc.forim.trans.shorturl.infra.entity.UrlMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * UrlMap映射DB处理
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Mapper
public interface UrlMapMapper extends BaseMapper<UrlMap> {

    /**
     * 通过id使映射失效
     *
     * @param id 主键
     */
    Integer expiredByIds(@Param("ids") List<Long> id);

    Integer updateExpireTimeById(@Param("id") Long id, @Param("expireTime") Date expireTime);

    List<UrlMap> selectByUrlStatusAndShortUrl(@Param("status") Integer urlStatus, @Param("shortUrl") String shortUrl);

    Integer selectCountByUrlStatusAndShortUrl(@Param("status") Integer urlStatus, @Param("shortUrl") String shortUrl);

    List<UrlMap> selectByUrlStatusAndShortUrlAndLongUrlAndUserId(@Param("status") Integer urlStatus,
                                                                 @Param("shortUrl") String shortUrl,
                                                                 @Param("longUrl") String longUrl,
                                                                 @Param("userId") Long userId);


    List<Long> selectInvalidUpdateUrlMapDomainConfId(@Param("expiredTime") Date expiredTime,
                                                     @Param("status") Integer status);

    List<UrlMap> selectByLongUrlAndShortUrlAndUserId(@Param("shortUrl") String shortUrl,
                                                     @Param("longUrl") String longUrl,
                                                     @Param("userId") Long userId);

    List<String> selectExpiredCompressionCodeByStatusAndBizTypeAndDomain(@Param("expiredTime") Date expiredTime,
                                                                         @Param("status") Integer status,
                                                                         @Param("bizType") String bizType,
                                                                         @Param("domainId") Long domainConfId);
}