package cc.forim.trans.shorturl.infra.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 禁止直接通过端口访问，而需要走网关转发
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Configuration
@Slf4j
public class SaTokenConfigure {
//    @Bean
//    public SaServletFilter getSaServletFilter() {
//        return new SaServletFilter()
//                .addInclude("/**")
//                // 放行swagger3
//                .addExclude("/swagger**/**", "/webjars/**", "/v3/**",
//                        "/doc.html/**", "/error", "/favicon.ico")
//                .setAuth(obj -> {
//                    // 校验 Same-Token 身份凭证     —— 以下两句代码可简化为：SaSameUtil.checkCurrentRequestToken();
//                    String token = SaHolder.getRequest().getHeader(SaSameUtil.SAME_TOKEN);
//                    SaSameUtil.checkToken(token);
//                    log.info("token: {}", token);
//                })
//                .setError(e -> {
//                    return SaResult.error(e.getMessage());
//                })
//                ;
//    }
}
