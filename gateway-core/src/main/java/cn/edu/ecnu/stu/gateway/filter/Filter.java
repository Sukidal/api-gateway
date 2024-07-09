package cn.edu.ecnu.stu.gateway.filter;

import cn.edu.ecnu.stu.gateway.context.GatewayContext;

/**
 * 过滤器顶级接口
 */
public interface Filter {

    void doFilter(GatewayContext ctx) throws Exception;

    default int getOrder(){
        FilterAspect annotation = this.getClass().getAnnotation(FilterAspect.class);
        if(annotation != null){
            return annotation.order();
        }
        return Integer.MAX_VALUE;
    }

    default boolean isPrototype() {
        FilterAspect annotation = this.getClass().getAnnotation(FilterAspect.class);
        return annotation != null && annotation.prototype();
    }
}
