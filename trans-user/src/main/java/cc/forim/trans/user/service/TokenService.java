package cc.forim.trans.user.service;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.user.infra.dto.GetUserInfoByTokenDTO;
import cc.forim.trans.user.infra.vo.UserInfoVO;

/**
 * Token服务接口
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public interface TokenService {

    /**
     * 通过token获取用户信息
     *
     * @param getUserInfoByTokenDto dto
     * @return 用户信息
     */
    ResultVO<UserInfoVO> getUserInfoByToken(GetUserInfoByTokenDTO getUserInfoByTokenDto);
}
