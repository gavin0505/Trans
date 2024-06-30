package cc.forim.trans.user.controller;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.user.infra.dto.LoginDTO;
import cc.forim.trans.user.infra.dto.LogoutDTO;
import cc.forim.trans.user.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 登录控制器
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2023/5/22 23:02
 */

@RestController
@Api(tags = "登录 API")
@RequestMapping("/login")
public class LoginController {

    @Resource(name = "accountServiceImpl")
    private AccountService accountService;

    @ApiOperation("登录")
    @PostMapping("/doLogin")
    public ResultVO<String> login(@RequestBody @Valid LoginDTO loginDto) {
        return accountService.login(loginDto);
    }

    @ApiOperation("登出")
    @PostMapping("/doLogout")
    public ResultVO<String> logout(@RequestBody @Valid LogoutDTO logoutDto) {
        return accountService.logout(logoutDto);
    }

    @ApiOperation("通过token判断是否登录")
    @GetMapping("/ifLogin/{token}")
    public ResultVO<String> ifLogin(@PathVariable String token) {
        return accountService.ifLogin(token);
    }
}