package cc.forim.trans.shorturl.service;

import cc.forim.trans.shorturl.infra.entity.CompressionCode;
import cc.forim.trans.shorturl.infra.entity.UrlMap;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import java.util.Date;

/**
 * 事务管理
 *
 * @author Gavin Zhang
 * @version V1.0
 */

public interface TransactionalService {

    /**
     * 保存短链映射和更新压缩码状态
     *
     * @param urlMap          url映射
     * @param compressionCode 压缩码
     */
    void saveUrlMapAndUpdateCompressCode(UrlMap urlMap, CompressionCode compressionCode);

    /**
     * 续签短链接
     *
     * @param urlMap url映射
     */
    void saveShortUrlRenewalMessage(UrlMap urlMap);


    /**
     * 具体的删除过期URL映射操作
     *
     * @param type   服务类型
     * @param setKey Set键名
     */
    void deleteExpireUrlMap(String type, String setKey);


    void deleteExpireUrlMapCompensation(Long domainConfId,
                                        String type,
                                        Date date,
                                        UpdateWrapper<UrlMap> urlMapUpdateWrapper);


}