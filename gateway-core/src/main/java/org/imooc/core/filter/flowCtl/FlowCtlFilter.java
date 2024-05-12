package org.imooc.core.filter.flowCtl;

import lombok.extern.slf4j.Slf4j;
import org.imooc.common.constants.FilterConst;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 限流流控过滤器
 * @USER: WuYang
 * @DATE: 2023/4/15 22:36
 */
@Slf4j
@FilterAspect(
        id=FilterConst.FLOW_CTL_FILTER_ID,
        name = FilterConst.FLOW_CTL_FILTER_NAME
        order = FilterConst.FLOW_CTL_FILTER_ORDER
)
public class FlowCtlFilter implements Filter {

    private static final String LIMIT_MESSAGE = "系统繁忙，请稍后再试";

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        GuavaCountLimiter countLimiter = GuavaCountLimiter.getInstance(ctx.getRequest().getPath());
        if(countLimiter != null && !countLimiter.acquire()) {
            throw new RuntimeException(LIMIT_MESSAGE);
        }
    }
}
