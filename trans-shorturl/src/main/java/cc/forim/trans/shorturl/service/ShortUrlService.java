package cc.forim.trans.shorturl.service;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.shorturl.infra.dto.*;
import cc.forim.trans.shorturl.infra.entity.UrlMap;
import cc.forim.trans.shorturl.infra.vo.*;

import java.util.List;

/**
 * 短链接操作服务
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public interface ShortUrlService {

    /**
     * 创建短链接业务
     *
     * @param dto 请求生成短链的传输数据
     * @return 短链接创建结果
     */
    ResultVO<ShortUrlCreationVO> createShortUrlBiz(GenerateShortUrlDTO dto);


    /**
     * 续签短链接
     *
     * @param dto 短链的传输数据
     * @return 短链接续签结果
     */
    ResultVO<ShortUrlRenewalVO> renewalShortUrlBiz(RenewalShortUrlDTO dto);

    /**
     * 获取单个短链接信息
     *
     * @param dto 短链查询条件
     * @return 短链接结果
     */
    ResultVO<ShortUrlQueryVO> getShortUrlBiz(ShortUrlQueryDTO dto);

    /**
     * 获取短链接信息列表
     *
     * @param dto 短链查询条件
     * @return 短链接结果
     */
    ResultVO<List<ShortUrlQueryVO>> getShortUrlListBiz(ShortUrlQueryDTO dto);

    /**
     * 修改短链接
     *
     * @param dto 短链的修改数据
     * @return 短链接修改后结果
     */
    ResultVO<ShortUrlQueryVO> updateShortUrlBiz(ShortUrlEditDTO dto);

    /**
     * 删除短链接（下线）
     *
     * @param dto 短链的查询条件
     * @return 短链接删除后结果
     */
    ResultVO<ShortUrlDeleteVO> deleteShortUrlBiz(ShortUrlDeleteDTO dto);

    /**
     * 获取创建短网址时需要的域名配置信息
     *
     * @return 域名配置
     */
    ResultVO<List<DomainConfSelectionVO>> getDomainConfForCreatingShortUrl();

    /**
     * 获取短链接信息列表的条数
     *
     * @param dto 短链查询条件
     * @return 短链接信息列表的条数
     */
    ResultVO<Long> getShortUrlQueryCount(ShortUrlQueryDTO dto);

    ResultVO<UrlMapLifeDateVO> getUrlMapById(UrlMapLifeDateDTO dto);

}
