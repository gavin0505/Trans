package cc.forim.trans.user.infra.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录信息VO
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("登录信息VO")
public class UserInfoVO {

    @ApiModelProperty(value = "用户名", required = true)
    private String username;

    @ApiModelProperty(value = "角色", required = true)
    private List<String> role;

    @ApiModelProperty(value = "用户Id", required = true)
    private Long userId;
}