package cc.forim.trans.user.dao;

import cc.forim.trans.user.infra.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户角色映射类
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Mapper
public interface RoleMapper extends BaseMapper<SysRole> {

    /**
     * 通过id获取权限码
     *
     * @param id id
     * @return 权限码，如：管理员-admin
     */
    String getRoleCodeById(@Param("id") Integer id);

}