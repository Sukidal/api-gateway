package org.imooc.core.filter;

import org.imooc.core.context.GatewayContext;

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
