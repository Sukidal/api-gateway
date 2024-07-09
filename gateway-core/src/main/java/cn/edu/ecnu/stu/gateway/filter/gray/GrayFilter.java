package cn.edu.ecnu.stu.gateway.filter.gray;

import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.Config;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;

@Slf4j
@FilterAspect(
        id= FilterConst.GRAY_FILTER_ID,
        name = FilterConst.GRAY_FILTER_NAME,
        order = FilterConst.GRAY_FILTER_ORDER
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
