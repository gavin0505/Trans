package cc.forim.trans.statistics.controller;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.statistics.infra.dto.TPlusOneDTO;
import cc.forim.trans.statistics.infra.dto.TimeAndGeoSearchDTO;
import cc.forim.trans.statistics.infra.vo.AllByGeoAndTimeSpanVO;
import cc.forim.trans.statistics.infra.vo.PanelGroupVO;
import cc.forim.trans.statistics.infra.vo.TPlusOneVO;
import cc.forim.trans.statistics.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Gavin Zhang
 * @version V1.0
 */
@Api(tags = "大数据统计服务接口")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource(name = "statisticsServiceImpl")
    private StatisticsService statisticsService;

    @ApiOperation("获取映射的T+1信息接口")
    @PostMapping("/getTPlusOne")
    public ResultVO<TPlusOneVO> getTPlusOne(@RequestBody TPlusOneDTO dto) {
        return statisticsService.getTPlusOne(dto);
    }

    @ApiOperation("获取时间地理维度接口")
    @PostMapping("/getTFromGeoAndTimeSpan")
    public ResultVO<AllByGeoAndTimeSpanVO> getTFromGeoAndTimeSpan(
            @RequestBody TimeAndGeoSearchDTO dto,
            @RequestHeader Long userId) {
        return statisticsService.getTFromGeoAndTimeSpan(dto, userId);
    }

    @ApiOperation("获取首页面板数值接口")
    @PostMapping("/getPanelGroup")
    public ResultVO<PanelGroupVO> getPanelGroup(@RequestHeader Long userId) {
        return statisticsService.getPanelGroup(userId);
    }
}
