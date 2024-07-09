package cn.edu.ecnu.stu.gateway.filter.monitor;

import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;


@Slf4j
@FilterAspect(id = FilterConst.MONITOR_FILTER_ID,
        name = FilterConst.MONITOR_FILTER_NAME,
        order = FilterConst.MONITOR_FILTER_ORDER)
public class MonitorFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        ctx.setTimerSample(Timer.start());
    }
}
