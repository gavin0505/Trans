package cc.forim.trans.gateway.config;

import cc.forim.trans.common.utils.StpAdminUtil;
import cc.forim.trans.common.utils.StpSuperAdminUtil;
import cc.forim.trans.common.utils.StpUserUtil;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Gavin Zhang
 * @version V1.0
 */

@Configuration
@Slf4j
public class SaTokenConfigure {

    /**
     * 注册 Sa-Token全局过滤器
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                // 拦截全部path
                .addInclude("/**")
                // 开放地址
                .addExclude("/favicon.ico")
                .addExclude("/user/register/**")
                .addExclude("/user/login/doLogin")
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由，并排除/user/login/doLogin 用于开放登录
                    SaRouter.match("/**").check(r -> {
                        if (!StpUserUtil.isLogin() && !StpSuperAdminUtil.isLogin() && !StpAdminUtil.isLogin()) {
                            throw new SaTokenException("请登录后再访问接口");
                        }
                    });
                })
                // 异常处理方法：每次setAuth函数出现异常时进入
                .setError(e -> SaResult.error(e.getMessage()))
                ;
    }
}
