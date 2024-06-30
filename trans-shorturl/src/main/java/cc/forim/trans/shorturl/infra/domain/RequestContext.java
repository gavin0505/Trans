package cc.forim.trans.shorturl.infra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求流转上下文
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {

    /**
     * 用户权限
     */
    private String userType;

    private Long userId;

    /**
     * TraceId
     */
    private String traceId;
}
