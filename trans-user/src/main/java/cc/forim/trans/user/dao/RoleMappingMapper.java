package cc.forim.trans.user.dao;

import cc.forim.trans.user.infra.entity.SysUserRoleMapping;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户-角色关系映射类
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Mapper
public interface RoleMappingMapper extends BaseMapper<SysUserRoleMapping> {

    /**
     * 通过用户id获取其角色id
     *
     * @param userId 用户id
     * @return 用户角色id
     */
    Integer getRoleIdFromUserId(@Param("userId") Integer userId);
}
