package cc.forim.trans.shorturl.infra.register;

import lombok.RequiredArgsConstructor;

/**
 * 改造的雪花算法序列生成器
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@RequiredArgsConstructor
public class SnowflakeSequenceGenerator implements SequenceGenerator {

    private final long machineId;

    private SnowFlake javaSnowflake;

    public void init() {
        this.javaSnowflake = new SnowFlake(machineId);
    }

    @Override
    public long generate() {
        return javaSnowflake.nextId();
    }
}
