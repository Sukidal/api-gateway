package org.imooc.core.filter.mock;

import lombok.extern.slf4j.Slf4j;
import org.imooc.common.config.Rule;
import org.imooc.common.utils.JSONUtil;
import org.imooc.core.Config;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;
import org.imooc.core.helper.ResponseHelper;
import org.imooc.core.response.GatewayResponse;

import java.util.Map;

import static org.imooc.common.constants.FilterConst.*;

@Slf4j
@FilterAspect(id=MOCK_FILTER_ID,
        name = MOCK_FILTER_NAME,
        order = MOCK_FILTER_ORDER)
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
