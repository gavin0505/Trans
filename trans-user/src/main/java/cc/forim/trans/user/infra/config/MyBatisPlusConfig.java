package cc.forim.trans.user.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

@Configuration
@MapperScan("cc.forim.trans.user")
public class MyBatisPlusConfig {
}