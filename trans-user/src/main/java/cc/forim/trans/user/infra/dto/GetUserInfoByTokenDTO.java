package cc.forim.trans.user.infra.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 通过Token获取用户信息DTO
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("通过Token获取用户信息")
public class GetUserInfoByTokenDTO {

    @NotNull(message = "不能为空")
    @ApiModelProperty(value = "用户token", required = true)
    private String token;
}