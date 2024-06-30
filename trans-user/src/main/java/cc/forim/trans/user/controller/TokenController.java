package cc.forim.trans.user.controller;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.user.infra.dto.GetUserInfoByTokenDTO;
import cc.forim.trans.user.service.TokenService;
import cc.forim.trans.user.infra.vo.UserInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

@RestController
@Api(tags = "Token管理 API")
@RequestMapping("/token")
public class TokenController {

    @Resource(name = "tokenServiceImpl")
    private TokenService tokenService;

    @ApiOperation("通过Token获取用户信息")
    @PostMapping("/getUserInfoByToken")
    public ResultVO<UserInfoVO> getUserInfoByToken(@RequestBody @Valid GetUserInfoByTokenDTO getUserInfoByTokenDto) {
        return tokenService.getUserInfoByToken(getUserInfoByTokenDto);
    }
}