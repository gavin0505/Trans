package cc.forim.trans.shorturl.dao;

import cc.forim.trans.shorturl.infra.entity.CompressionCode;
import cc.forim.trans.shorturl.infra.entity.DomainConf;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 压缩码映射DB处理类
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Mapper
public interface CompressionCodeMapper extends BaseMapper<CompressionCode> {

    /**
     * 获取一个最新可用的压缩码
     *
     * @param domainConfId 短链域名配置id
     * @return 压缩码
     */
    CompressionCode getLatestAvailableCompressionCode(@Param("domainConfId") Long domainConfId);

    /**
     * 更新压缩码状态
     *
     * @param record 压缩码
     * @return 改变结果
     */
    Integer updateByPrimaryKeySelective(CompressionCode record);

    /**
     * 使压缩码失效
     *
     * @param expiredByCode 过期的压缩码
     * @return 改变结果
     */
    Integer expiredByCodes(@Param("codes") List<String> expiredByCode);

    /**
     * 判断压缩码是否存在
     *
     * @param domainConfId    域名配置id
     * @param compressionCode 压缩码
     * @return 判断结果
     */
    Long hasCompressionCode(@Param("domainId") Long domainConfId, @Param("code") String compressionCode);

    Integer updateCodeStatusById(@Param("id")Long id, @Param("status")Integer status);

    Long selectIdByCodeAndDomainConfId(@Param("compressionCode")String code, @Param("confId") Long id);
}