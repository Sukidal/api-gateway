package org.imooc.core.filter.gray;

import lombok.extern.slf4j.Slf4j;
import org.imooc.core.Config;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;

import static org.imooc.common.constants.FilterConst.*;

@Slf4j
@FilterAspect(
        id=GRAY_FILTER_ID,
        name = GRAY_FILTER_NAME,
        order = GRAY_FILTER_ORDER
)
public class GrayFilter implements Filter {

    private int grayRate;

    private boolean enableGray;

    public GrayFilter() {
        Config instance = Config.getInstance();
        grayRate = (int) (1 / instance.getGrayRate());
        enableGray = instance.isEnableGray();
    }

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        if(!enableGray)
            return;
        //测试灰度功能待时候使用
        String gray = ctx.getRequest().getHeaders().get("gray");
        if ("true".equals(gray)) {
            ctx.setGray(true);
        }

        String clientIp = ctx.getRequest().getClientIp();
        int res = clientIp.hashCode() % grayRate;
        if (res == 0) {
            ctx.setGray(true);
        }

    }
}
