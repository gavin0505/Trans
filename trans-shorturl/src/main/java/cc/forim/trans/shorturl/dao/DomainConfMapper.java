package cc.forim.trans.shorturl.dao;

import cc.forim.trans.shorturl.infra.entity.DomainConf;
import cc.forim.trans.shorturl.infra.vo.DomainConfSelectionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 短链配置映射DB处理
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Mapper
public interface DomainConfMapper extends BaseMapper<DomainConf> {

    /**
     * 查找短链域名是否存在
     *
     * @param domain 需要判断的域名
     * @return 结果
     */
    DomainConf selectByDomain(@Param("domain") String domain, @Param("bizType") String bizType);

    DomainConf selectIdByDomainAndProtocol(@Param("domain") String domain,
                                           @Param("protocol") String protocol,
                                           @Param("bizType") String bizType);

    List<DomainConfSelectionVO> selectByDomainStatus();
}