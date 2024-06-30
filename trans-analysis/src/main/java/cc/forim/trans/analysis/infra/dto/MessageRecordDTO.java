package cc.forim.trans.analysis.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenResty拿到的短网址处理对象
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecordDTO {

    private String serviceType;

    private String shortUrl;

    private String compressionCode;

    private String ipAddress;

    private String targetAddress;

    private String userAgent;

    private String datetime;

    private String traceId;

    private Long urlMapId;

    private Long userId;
}
