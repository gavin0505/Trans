package cc.forim.trans.statistics.infra.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 分析表的实体
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@TableName("analysis")
public class Analysis {

    private String shortUrl;

    private String targetUrl;

    private String ipv4;

    private String ip;

    private String isp;

    private String province;

    private String city;

    private LocalDateTime datetime;

    private String os;

    private String osVersion;

    private String browser;

    private String browserVersion;

    private Integer mobile;
}
