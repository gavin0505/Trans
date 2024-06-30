package cc.forim.trans.statistics.infra.annotation;

import com.baomidou.dynamic.datasource.annotation.DS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多数据源注解-ClickHouse
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DS("clickhouse")
public @interface ClickHouseMapper {
}