package cn.edu.ecnu.stu.gateway.filter.http;

import com.alibaba.fastjson.JSON;
import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;
import cn.edu.ecnu.stu.gateway.request.GatewayRequest;

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
