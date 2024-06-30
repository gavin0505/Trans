package cc.forim.trans.gateway.filter;

import cc.forim.trans.common.utils.RedisUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

import static cc.forim.trans.gateway.common.GatewayConstant.BASE_ORDER;

/**
 * userId配置
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Component
@Slf4j
public class UserIdFilter implements GlobalFilter, Ordered {

    @Resource(name = "redisUtil")
    private RedisUtil redisUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 获取Cookies
        request.getCookies().forEach((name, cookies) -> {
            cookies.forEach(cookie -> {
                // 找到token
                if (StringUtils.equals(name, "trans-token-user") ||
                        StringUtils.equals(name, "trans-token-superAdmin") ||
                        StringUtils.equals(name, "trans-token-admin")) {
                    String[] keys = name.split("-");
                    //拼装Redis的key
                    String redisNamePrefix = keys[0] + "-" + keys[1] + ":" + keys[2] + ":" + "token";
                    String redisKey = redisNamePrefix + ":" + cookie.getValue();
                    // Redis拿userId
                    Object idObj = redisUtil.get(redisKey);
                    if (ObjectUtil.isNotEmpty(idObj)) {
                        request.mutate().header("userType", keys[2]);
                        request.mutate().header("userId", String.valueOf(idObj));
                    }
                }
            });
        });
        ServerWebExchange newExchange = exchange.mutate().request(request).build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return BASE_ORDER;
    }
}
