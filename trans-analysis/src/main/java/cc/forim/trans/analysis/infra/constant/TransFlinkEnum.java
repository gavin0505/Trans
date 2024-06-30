package cc.forim.trans.analysis.infra.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Trans的Flink相关枚举
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@RequiredArgsConstructor
@Getter
public enum TransFlinkEnum {

    /**
     * 刷洗到CK的SINK名
     */
    RECORD_SINK("trans_sink"),

    /**
     * 发送到CK的作业名
     */
    CK_JOB_NAME("trans_send_clickhouse")

    ;

    private final String description;
}
