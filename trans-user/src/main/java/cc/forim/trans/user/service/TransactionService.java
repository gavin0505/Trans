package cc.forim.trans.user.service;

import cc.forim.trans.user.infra.dto.RegisterMsgDTO;

/**
 * 事务管理接口
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public interface TransactionService {

    /**
     * 保存新用户注册数据
     *
     * @param registerMsgDto 注册数据
     */
    void saveNewRegisterAccount(RegisterMsgDTO registerMsgDto);
}
