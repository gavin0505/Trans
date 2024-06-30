package cc.forim.trans.user.service;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.user.infra.dto.VerifyCodeDTO;

/**
 * 验证码服务接口
 * @author Gavin Zhang
 * @version V1.0
 */
public interface VerifyCodeService {

    /**
     * 获取验证码
     *
     * @param verifyCodeDto 验证码
     * @return 验证码
     */
    ResultVO<String> getVerifyCode(VerifyCodeDTO verifyCodeDto);

    /**
     * 注册验证码是否有效
     *
     * @param account 邮箱
     * @param code    验证码
     * @return true-有效；false-无效
     */
    Boolean ifRegisterVerifyCodeValid(String account, String code);
}