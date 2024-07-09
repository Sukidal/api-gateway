package cn.edu.ecnu.stu.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;


import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GatewayFilterChain {

    private List<Filter> filters = new ArrayList<>();

    public GatewayFilterChain addFilter(Filter filter){
        filters.add(filter);
        return this;
    }

    public GatewayFilterChain addFilterList(List<Filter> filter){
        filters.addAll(filter);
        return this;
    }


    public GatewayContext doFilter(GatewayContext ctx) throws Exception {
        if(filters.isEmpty()){
            return ctx;
        }
        try {
            for(Filter fl: filters){
                fl.doFilter(ctx);
                if (ctx.isTerminated()) {
                    break;
                }
            }
        } catch (Exception e){
            log.error("执行过滤器发生异常,异常信息：{}",e.getMessage());
            throw e;
        }
        return ctx;
    }
}
