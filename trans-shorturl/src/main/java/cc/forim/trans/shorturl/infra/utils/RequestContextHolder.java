package cc.forim.trans.shorturl.infra.utils;

import cc.forim.trans.shorturl.infra.domain.RequestContext;

/**
 * 请求验证上下文
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public class RequestContextHolder {
    private static final ThreadLocal<RequestContext> requestContext = new ThreadLocal<>();

    public static void setContext(RequestContext context) {
        requestContext.set(context);
    }

    public static RequestContext getContext() {
        return requestContext.get();
    }

    public static void clear() {
        requestContext.remove();
    }
}
