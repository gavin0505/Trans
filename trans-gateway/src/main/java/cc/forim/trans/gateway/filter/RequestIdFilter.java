package cc.forim.trans.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static cc.forim.trans.gateway.common.GatewayConstant.BASE_ORDER;

/**
 * 全链路追踪id配置
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Component
@Slf4j
public class RequestIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // 使用时间戳-UUID做唯一追踪
        request.mutate().header("requestId", System.currentTimeMillis() + "-" +
                UUID.randomUUID());

        ServerWebExchange newExchange = exchange.mutate().request(request).build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return BASE_ORDER + 1;
    }
}
