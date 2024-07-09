package cn.edu.ecnu.stu.gateway.filter.flowCtl;

import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;

@Slf4j
@FilterAspect(
        id=FilterConst.FLOW_CTL_FILTER_ID,
        name = FilterConst.FLOW_CTL_FILTER_NAME,
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
