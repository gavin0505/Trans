package cc.forim.trans.statistics.service;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.statistics.infra.dto.TPlusOneDTO;
import cc.forim.trans.statistics.infra.dto.TimeAndGeoSearchDTO;
import cc.forim.trans.statistics.infra.vo.AllByGeoAndTimeSpanVO;
import cc.forim.trans.statistics.infra.vo.PanelGroupVO;
import cc.forim.trans.statistics.infra.vo.TPlusOneVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

public interface StatisticsService {

    /**
     * 获取T+1的UrlMap数据
     *
     * @param dto T+1请求参数
     * @return T+1的UrlMap数据
     */
    ResultVO<TPlusOneVO> getTPlusOne(TPlusOneDTO dto);


    /**
     * 通过时间维度和地理维度，获取全部信息
     *
     * @param dto    时间和地理标签查到的
     * @param userId 用户id
     * @return 指定时间维度和地理维度下的全部信息
     */
    ResultVO<AllByGeoAndTimeSpanVO> getTFromGeoAndTimeSpan(@RequestBody TimeAndGeoSearchDTO dto,

                                                           @RequestHeader Long userId);

    /**
     * 获取首页面板的数值信息
     *
     * @param userId 用户id
     * @return 首页面板的数值信息
     */
    ResultVO<PanelGroupVO> getPanelGroup(@RequestHeader Long userId);

}
