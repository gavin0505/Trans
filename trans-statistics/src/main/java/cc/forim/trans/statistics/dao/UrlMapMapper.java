package cc.forim.trans.statistics.dao;

import cc.forim.trans.statistics.infra.annotation.MySQLMapper;
import cc.forim.trans.statistics.infra.entity.UrlMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * UrlMap映射DB处理
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@MySQLMapper
public interface UrlMapMapper extends BaseMapper<UrlMap> {
}