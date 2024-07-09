package cn.edu.ecnu.stu.gateway.filter.mock;

import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.utils.JSONUtil;
import cn.edu.ecnu.stu.gateway.Config;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;
import cn.edu.ecnu.stu.gateway.helper.ResponseHelper;
import cn.edu.ecnu.stu.gateway.response.GatewayResponse;

import java.util.Map;

@Slf4j
@FilterAspect(id = FilterConst.MOCK_FILTER_ID,
        name = FilterConst.MOCK_FILTER_NAME,
        order = FilterConst.MOCK_FILTER_ORDER)
public class MockFilter implements Filter {

    private Map<String, String> resultMap;

    public MockFilter() {
        try {
            resultMap = JSONUtil.parse(Config.getInstance().getMockConfig(), Map.class);
        } catch (Exception e) {
            log.error("mock config parse error", e);
            throw new RuntimeException("mock config parse error");
        }
    }

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        String value = resultMap.get(ctx.getRequest().getMethod().name() + " " + ctx.getRequest().getPath());
        if (value != null) {
            ctx.setResponse(GatewayResponse.buildGatewayResponse(value));
            ctx.written();
            ResponseHelper.writeResponse(ctx);
            log.info("mock {} {} {}", ctx.getRequest().getMethod(), ctx.getRequest().getPath(), value);
            ctx.terminated();
        }
    }
}
