package org.imooc.core.filter.monitor;

import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;

import static org.imooc.common.constants.FilterConst.*;


@Slf4j
@FilterAspect(id=MONITOR_FILTER_ID,
        name = MONITOR_FILTER_NAME,
        order = MONITOR_FILTER_ORDER)
public class MonitorFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        ctx.setTimerSample(Timer.start());
    }
}
