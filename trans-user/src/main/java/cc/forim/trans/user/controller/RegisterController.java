package cc.forim.trans.user.controller;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.user.infra.dto.RegisterMsgDTO;
import cc.forim.trans.user.infra.dto.VerifyCodeDTO;
import cc.forim.trans.user.service.AccountService;
import cc.forim.trans.user.service.VerifyCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 注册控制器
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Api(tags = "注册 API")
@RestController
@RequestMapping("/register")
public class RegisterController {

    @Resource(name = "accountServiceImpl")
    private AccountService accountService;

    @Resource(name = "verifyCodeServiceImpl")
    private VerifyCodeService verifyCodeService;

    @ApiOperation("注册")
    @PostMapping("/doRegister")
    public ResultVO<String> register(@RequestBody @Valid RegisterMsgDTO registerMsgDto) {
        return accountService.register(registerMsgDto);
    }

    @ApiOperation("获取验证码")
    @PostMapping("/getVerifyCode")
    public ResultVO<String> getVerifyCode(@RequestBody VerifyCodeDTO verifyCodeDto) {
        return verifyCodeService.getVerifyCode(verifyCodeDto);
    }
}