package cc.forim.trans.user.infra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * URL映射实体类
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@TableName("url_map")
public class UrlMap {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String shortUrl;

    private String longUrl;

    private String shortUrlDigest;

    private String longUrlDigest;

    private String bizType;

    private String compressionCode;

    private String description;

    private Long userId;

    private Date expireTime;

    private Integer urlStatus;

    private String creator;

    private String editor;

    private Date createTime;

    private Date editTime;

    private Long version;

    private Integer deleted;
}
