package cc.forim.trans.shorturl.infra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * 域名配置实体类
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@TableName("domain_conf")
public class DomainConf {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String domainValue;

    private String protocol;

    private String bizType;

    private Integer domainStatus;

    private Date createTime;

    private Date editTime;

    private Integer creator;

    private Integer editor;

    private Long version;

    private Integer deleted;

}
