package cc.forim.trans.shorturl.controller;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.shorturl.infra.dto.*;
import cc.forim.trans.shorturl.infra.vo.*;
import cc.forim.trans.shorturl.service.ShortUrlService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

@Api(tags = "短链接操作服务接口")
@RestController
@RequestMapping("/shortUrl")
public class ShortUrlController {

    @Resource(name = "shortUrlServiceImpl")
    private ShortUrlService shortUrlService;

    @ApiOperation("创建短URL接口")
    @PostMapping("/createShortUrl")
    public ResultVO<ShortUrlCreationVO> createShortUrl(
            @RequestBody GenerateShortUrlDTO dto,
            @RequestHeader("userId") String userId,
            @RequestHeader("requestId") String requestId
    ) {
        return shortUrlService.createShortUrlBiz(dto.setUserId(Long.parseLong(userId)).setRequestId(requestId));
    }

    @ApiOperation("查询短URL接口")
    @PostMapping("/getShortUrl")
    public ResultVO<ShortUrlQueryVO> getShortUrl(@RequestBody ShortUrlQueryDTO dto,
                                                 @RequestHeader("userId") Long userId,
                                                 @RequestHeader("requestId") String requestId) {
        return shortUrlService.getShortUrlBiz(dto.setUserId(userId).setRequestId(requestId));
    }

    @ApiOperation("查询短URL列表接口")
    @PostMapping("/getShortUrlList")
    public ResultVO<List<ShortUrlQueryVO>> getShortUrlList(@RequestBody ShortUrlQueryDTO dto,
                                                           @RequestHeader("userId") Long userId,
                                                           @RequestHeader("requestId") String requestId) {
        return shortUrlService.getShortUrlListBiz(dto.setUserId(userId).setRequestId(requestId));
    }

    @ApiOperation("修改短URL接口")
    @PostMapping("/updateShortUrl")
    public ResultVO<ShortUrlQueryVO> updateShortUrl(@RequestBody ShortUrlEditDTO dto,
                                                    @RequestHeader("userId") Long userId,
                                                    @RequestHeader("requestId") String requestId) {
        return shortUrlService.updateShortUrlBiz(dto.setUserId(userId).setRequestId(requestId));
    }

    @ApiOperation("删除短URL接口（下线）")
    @PostMapping("/deleteShortUrl")
    public ResultVO<ShortUrlDeleteVO> deleteShortUrl(@RequestBody ShortUrlDeleteDTO dto,
                                                     @RequestHeader("userId") Long userId,
                                                     @RequestHeader("requestId") String requestId) {
        return shortUrlService.deleteShortUrlBiz(dto.setUserId(userId).setRequestId(requestId));
    }

    @ApiOperation("续签短URL接口")
    @PostMapping("/renewalShortUrl")
    public ResultVO<ShortUrlRenewalVO> renewalShortUrl(@RequestBody RenewalShortUrlDTO dto,
                                                       @RequestHeader("userId") Long userId,
                                                       @RequestHeader("requestId") String requestId) {
        return shortUrlService.renewalShortUrlBiz(dto.setUserId(userId).setRequestId(requestId));
    }

    @ApiOperation("获取创建短网址时需要的域名配置信息接口")
    @GetMapping("/getDomainConfForCreatingShortUrl")
    public ResultVO<List<DomainConfSelectionVO>> getDomainConfForCreatingShortUrl() {
        return shortUrlService.getDomainConfForCreatingShortUrl();
    }

    @ApiOperation("获取短链接信息列表的条数接口")
    @PostMapping("/getShortUrlListCount")
    public ResultVO<Long> getShortUrlQueryCount(@RequestBody ShortUrlQueryDTO dto,
                                                @RequestHeader("userId") Long userId,
                                                @RequestHeader("requestId") String requestId) {
        return shortUrlService.getShortUrlQueryCount(dto.setUserId(userId).setRequestId(requestId));
    }

    @ApiOperation("获取短链映射信息的生命日期接口")
    @PostMapping("/getUrlMapById")
    public ResultVO<UrlMapLifeDateVO> getUrlMapById(@RequestBody UrlMapLifeDateDTO dto,
                                                    @RequestHeader("userId") Long userId,
                                                    @RequestHeader("requestId") String requestId) {
        return shortUrlService.getUrlMapById(dto.setUserId(userId).setRequestId(requestId));

    }
}
