package cc.forim.trans.shorturl.infra.config;

import cc.forim.trans.shorturl.infra.register.SequenceGenerator;
import cc.forim.trans.shorturl.infra.register.SnowflakeSequenceGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 序列生成器自动注入
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Configuration
public class SequenceGeneratorAutoConfiguration {
    // todo 确定机器id
    @Bean
    @ConditionalOnMissingBean
    public SequenceGenerator snowflakeSequenceGenerator(@Value("${trans.snowflake.machine.id}") Long machineId) {
        SnowflakeSequenceGenerator sequenceGenerator = new SnowflakeSequenceGenerator(machineId);
        sequenceGenerator.init();
        return sequenceGenerator;
    }
}