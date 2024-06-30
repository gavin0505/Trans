package cc.forim.trans.user.service.impl;

import cc.forim.trans.common.ResultVO;
import cc.forim.trans.common.utils.StpAdminUtil;
import cc.forim.trans.common.utils.StpSuperAdminUtil;
import cc.forim.trans.common.utils.StpUserUtil;
import cc.forim.trans.user.dao.RoleMapper;
import cc.forim.trans.user.dao.RoleMappingMapper;
import cc.forim.trans.user.dao.UserMapper;
import cc.forim.trans.user.infra.dto.GetUserInfoByTokenDTO;
import cc.forim.trans.user.infra.vo.UserInfoVO;
import cc.forim.trans.user.service.TokenService;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static cc.forim.trans.user.infra.common.RoleCommon.*;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

@Service("tokenServiceImpl")
public class TokenServiceImpl implements TokenService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMappingMapper roleMappingMapper;

    @Override
    public ResultVO<UserInfoVO> getUserInfoByToken(GetUserInfoByTokenDTO getUserInfoByTokenDto) {

        // 1. 获取角色
        String role = getUserTypeByToken(getUserInfoByTokenDto.getToken());

        // 2. 组装UserInfo
        UserInfoVO userInfoVo = assembleUserInfoByRole(role);

        if (ObjectUtil.isNotNull(userInfoVo)) {
            return ResultVO.success(userInfoVo);
        }
        return ResultVO.error("获取用户信息错误，请检查token");
    }

    /**
     * 通过Token获取用户角色
     *
     * @param token token
     * @return 对应角色
     */
    private String getUserTypeByToken(String token) {

        if (ObjectUtil.isNotNull(StpUserUtil.getLoginIdByToken(token))) {
            return StpUserUtil.getLoginType();
        } else if (ObjectUtil.isNotNull(StpAdminUtil.getLoginIdByToken(token))) {
            return StpAdminUtil.getLoginType();
        } else if (ObjectUtil.isNotNull(StpSuperAdminUtil.getLoginIdByToken(token))) {
            return StpSuperAdminUtil.getLoginType();
        }
        return StrUtil.EMPTY;
    }

    /**
     * 通过角色组装UserInfo
     *
     * @param role 角色
     * @return UserInfo
     */
    private UserInfoVO assembleUserInfoByRole(String role) {

        SaTokenInfo tokenInfo = null;
        switch (role) {
            case SUPER_ADMIN -> {
                tokenInfo = StpSuperAdminUtil.getTokenInfo();
                assembleUserInfo(tokenInfo);
                return assembleUserInfo(tokenInfo);
            }
            case ADMIN -> {
                tokenInfo = StpAdminUtil.getTokenInfo();
                assembleUserInfo(tokenInfo);
                return assembleUserInfo(tokenInfo);
            }
            case USER -> {
                tokenInfo = StpUserUtil.getTokenInfo();
                assembleUserInfo(tokenInfo);
                return assembleUserInfo(tokenInfo);
            }
        }
        return null;
    }

    /**
     * 从SaTokenInfo组装UserInfo
     */
    private UserInfoVO assembleUserInfo(SaTokenInfo saTokenInfo) {

        UserInfoVO userInfoVo = new UserInfoVO();

        // 获取用户id
        Object id = saTokenInfo.getLoginId();

        // 查询用户名和权限
        if (ObjectUtil.isNotNull(id)) {
            String username = userMapper.selectUsernameById(Integer.parseInt(id.toString()));
            userInfoVo.setUsername(username);

            // 查角色
            Integer roleId = roleMappingMapper.getRoleIdFromUserId(Integer.parseInt(id.toString()));
            String role = roleMapper.getRoleCodeById(roleId);

            List<String> rolesArr = Lists.newArrayList(role);
            userInfoVo.setRole(rolesArr);

            // 设置用户id
            userInfoVo.setUserId(Long.parseLong(id.toString()));
        }

        return userInfoVo;
    }
}