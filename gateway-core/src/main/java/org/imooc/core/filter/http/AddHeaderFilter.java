package org.imooc.core.filter.http;

import com.alibaba.fastjson.JSON;
import org.imooc.common.constants.FilterConst;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;
import org.imooc.core.request.GatewayRequest;

import java.util.Map;

@FilterAspect(
        id = FilterConst.ADD_HEADER_FILTER_ID,
        name = FilterConst.ADD_HEADER_FILTER_NAME,
        order = FilterConst.ADD_HEADER_FILTER_ORDER
)
public class AddHeaderFilter implements Filter {

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        String config = ctx.getRule().getFilterConfig(FilterConst.ADD_HEADER_FILTER_ID).getConfig();
        Map<String, String> map = JSON.parseObject(config, Map.class);
        GatewayRequest request = ctx.getRequest();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
    }

}
