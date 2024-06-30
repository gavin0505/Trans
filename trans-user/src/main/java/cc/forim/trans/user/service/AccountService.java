package cc.forim.trans.user.service;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.user.infra.dto.LoginDTO;
import cc.forim.trans.user.infra.dto.LogoutDTO;
import cc.forim.trans.user.infra.dto.RegisterMsgDTO;

/**
 * 账户服务
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public interface AccountService {

    /**
     * 注册
     *
     * @param registerMsgDto 注册信息载体
     * @return 注册结果
     */
    ResultVO<String> register(RegisterMsgDTO registerMsgDto);


    /**
     * 登录
     *
     * @param loginDto 登录信息载体
     * @return token
     */
    ResultVO<String> login(LoginDTO loginDto);

    /**
     * 登出
     *
     * @param logoutDto 登出信息载体
     * @return token
     */
    ResultVO<String> logout(LogoutDTO logoutDto);

    /**
     * 是否登录
     *
     * @param token token
     * @return 状态提示
     */
    ResultVO<String> ifLogin(String token);
}