package cc.forim.trans.user.service.impl;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.common.lock.DistributeLockFactory;
import cc.forim.trans.common.lock.DistributedLock;
import cc.forim.trans.common.utils.StpAdminUtil;
import cc.forim.trans.common.utils.StpSuperAdminUtil;
import cc.forim.trans.common.utils.StpUserUtil;
import cc.forim.trans.user.dao.RoleMapper;
import cc.forim.trans.user.dao.RoleMappingMapper;
import cc.forim.trans.user.dao.UserMapper;
import cc.forim.trans.user.infra.dto.LoginDTO;
import cc.forim.trans.user.infra.dto.LogoutDTO;
import cc.forim.trans.user.infra.dto.RegisterMsgDTO;
import cc.forim.trans.user.infra.enums.CommonConstant;
import cc.forim.trans.user.infra.enums.LockKey;
import cc.forim.trans.user.service.AccountService;
import cc.forim.trans.user.service.TransactionService;
import cc.forim.trans.user.service.VerifyCodeService;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cc.forim.trans.user.infra.common.RoleCommon.*;

/**
 * 账户服务实现类
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Service("accountServiceImpl")
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final int LOGIN_BY_USERNAME = 1;

    private static final int NULL = 0;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RoleMappingMapper roleMappingMapper;

    @Resource(name = "transactionServiceImpl")
    private TransactionService transactionService;

    @Resource(name = "verifyCodeServiceImpl")
    private VerifyCodeService verifyCodeService;

    @Resource(name = "distributeLockFactory")
    private DistributeLockFactory distributeLockFactory;

    @Override
    public ResultVO<String> register(RegisterMsgDTO registerMsgDto) {

        // 0. 上锁
        // 创建分布式锁
        String usernameLockName = LockKey.REGISTER_NEW_ACCOUNT.getCode() + CommonConstant.COLON + registerMsgDto.getUsername();
        String emailLockName = LockKey.REGISTER_NEW_EMAIL.getCode() + CommonConstant.COLON + registerMsgDto.getEmail();
        DistributedLock usernameLock = distributeLockFactory.provideDistributedLock(usernameLockName);
        DistributedLock emailLock = distributeLockFactory.provideDistributedLock(emailLockName);

        if (!(emailLock.tryLock(LockKey.REGISTER_NEW_EMAIL.getWaitTime(), LockKey.REGISTER_NEW_EMAIL.getReleaseTime(), TimeUnit.MILLISECONDS)
                &&
                usernameLock.tryLock(LockKey.REGISTER_NEW_EMAIL.getWaitTime(), LockKey.REGISTER_NEW_ACCOUNT.getReleaseTime(), TimeUnit.MILLISECONDS))) {
            return ResultVO.error("用户名或邮箱被占用，请重试！");
        }

        try {

            // 1. 验证邮箱唯一性
            if (ifEmailAccountUnique(registerMsgDto.getEmail())) {
                return ResultVO.error("邮箱已被注册！");
            }

            // 2. 验证用户名唯一性
            if (ifUsernameUnique(registerMsgDto.getUsername())) {
                return ResultVO.error("该用户名已被使用！");
            }

            // 3. 核对验证码有效性
            if (!verifyCodeService.ifRegisterVerifyCodeValid(registerMsgDto.getEmail(), registerMsgDto.getCode())) {
                return ResultVO.error("验证码失效，请重新获取！");
            }

            // 密码加密
            registerMsgDto.setPassword(SaSecureUtil.sha256(registerMsgDto.getPassword()));

            // 4. 写入注册数据
            transactionService.saveNewRegisterAccount(registerMsgDto);

            log.info("注册成功：【用户：{}，邮箱：{}】", registerMsgDto.getUsername(), registerMsgDto.getEmail());

            return ResultVO.success("注册成功！");
        } finally {
            // 5. 释放锁
            usernameLock.unlock();
            emailLock.unlock();
        }
    }

    private boolean ifEmailAccountUnique(String email) {
        return nullValueHandle(userMapper.selectIdByEmail(email)) > NULL;
    }

    private boolean ifUsernameUnique(String username) {
        return nullValueHandle(userMapper.selectIdByUsername(username)) > NULL;
    }

    /**
     * Integer的空值处理，若输入为空值，则返回0，否则返回原值
     */
    private Integer nullValueHandle(Integer id) {
        Optional<Integer> optionalValue = Optional.ofNullable(id);
        id = optionalValue.orElse(NULL);
        return id;
    }

    @Override
    public ResultVO<String> login(LoginDTO loginDto) {

        // 加密
        loginDto.setPassword(SaSecureUtil.sha256(loginDto.getPassword()));

        // 通过Username登录
        if (loginDto.getType() == LOGIN_BY_USERNAME) {
            // 获取账号id
            Integer id = nullValueHandle(userMapper.login(loginDto.getUsername(), loginDto.getPassword()));
            log.info("id: {}", id);
            // 账号存在即登录
            if (id > NULL) {
                // 获取账号角色
                String role = getRoleCodeByUserId(id);
                String token = null;

                // 根据不同角色进行登录
                switch (role) {
                    case SUPER_ADMIN -> {
                        StpSuperAdminUtil.login(id);
                        token = StpSuperAdminUtil.getTokenInfo().getTokenValue();
                    }
                    case ADMIN -> {
                        StpAdminUtil.login(id);
                        token = StpAdminUtil.getTokenInfo().getTokenValue();
                    }
                    case USER -> {
                        StpUserUtil.login(id);
                        token = StpUserUtil.getTokenInfo().getTokenValue();
                    }
                }

                // 更新最后登录时间
                userMapper.updateLogged(id, DateUtil.date());

                log.info("【用户】id: {} 登录成功", id);

                return ResultVO.success(token);
            }
        }
        return ResultVO.error("登录失败，用户名或密码错误");
    }

    /**
     * 获取用户所属角色
     *
     * @param userId 用户id
     * @return 对应角色状态码，如，超级管理员：super-admin
     */
    private String getRoleCodeByUserId(Integer userId) {
        Integer roleId = roleMappingMapper.getRoleIdFromUserId(userId);
        return roleMapper.getRoleCodeById(roleId);
    }

    @Override
    public ResultVO<String> logout(LogoutDTO logoutDto) {

        // user登出
        Object userId = StpUserUtil.getLoginIdByToken(logoutDto.getToken());
        if (ObjectUtil.isNotNull(userId)) {
            StpUserUtil.logout(userId);
            log.info("【用户】id: {} 登出成功", userId);
            return ResultVO.success("用户登出成功！");
        }

        // admin登出
        Object adminId = StpAdminUtil.getLoginIdByToken(logoutDto.getToken());
        if (ObjectUtil.isNotNull(adminId)) {
            StpAdminUtil.logout(adminId);
            log.info("【管理员】id: {} 登出成功", adminId);
            return ResultVO.success("管理员登出成功！");
        }

        // super-admin登出
        Object superAdminId = StpSuperAdminUtil.getLoginIdByToken(logoutDto.getToken());
        if (ObjectUtil.isNotNull(superAdminId)) {
            StpSuperAdminUtil.logout(superAdminId);
            log.info("【超级管理员】id: {} 登出成功", superAdminId);
            return ResultVO.success("超级管理员登出成功！");
        }

        return ResultVO.error("token错误，登出失败");
    }

    @Override
    public ResultVO<String> ifLogin(String token) {

        // user
        Object userId = StpUserUtil.getLoginIdByToken(token);
        if (ObjectUtil.isNotNull(userId)) {
            return ResultVO.success("用户已登录！");
        }

        // admin
        Object adminId = StpAdminUtil.getLoginIdByToken(token);
        if (ObjectUtil.isNotNull(adminId)) {
            return ResultVO.success("管理员已登录！");
        }

        // super-admin
        Object superAdminId = StpSuperAdminUtil.getLoginIdByToken(token);
        if (ObjectUtil.isNotNull(superAdminId)) {
            return ResultVO.success("超级管理员已登录！");
        }

        return ResultVO.error("token错误，用户未登录");
    }
}