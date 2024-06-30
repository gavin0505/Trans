package cc.forim.trans.analysis.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageOutputDTO {

    /**
     * 短网址
     */
    private String shortUrl;

    /**
     * 目标网址
     */
    private String targetUrl;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 访问ip运营商
     */
    private String isp;

    /**
     * 省份
     */
    private String convince;

    /**
     * 城市
     */
    private String city;

    /**
     * 访问时间
     */
    private Long datetime;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 浏览器版本
     */
    private String browserVersion;

    /**
     * 是否为移动设备
     */
    private Integer mobile;

    /**
     * TraceId, 系统日志用，不做CH存储
     */
    private String traceId;

    /**
     * DB中UrlMap的id
     */
    private Long urlMapId;

    /**
     * DB中用户id
     */
    private Long userId;
}
