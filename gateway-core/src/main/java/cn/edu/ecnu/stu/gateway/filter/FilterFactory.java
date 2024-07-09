package cn.edu.ecnu.stu.gateway.filter;

import cn.edu.ecnu.stu.gateway.context.GatewayContext;

/**
 * 工厂接口
 */
public interface FilterFactory {

    /**
     * 构建过滤器链条
     * @param ctx
     * @return
     * @throws Exception
     */
    GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception;

    Filter getFilter(String filterId);
}
