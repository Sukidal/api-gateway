package cn.edu.ecnu.stu.gateway.filter.loadbalance;

import cn.edu.ecnu.stu.gateway.common.config.ServiceInstance;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;

/**
 * 负载均衡顶级接口
 */
public interface IGatewayLoadBalanceRule {

    /**
     * 通过上下文参数获取服务实例
     * @param ctx
     * @return
     */
    ServiceInstance choose(GatewayContext ctx);

    /**
     * 通过服务ID拿到对应的服务实例
     *
     * @param serviceId
     * @param gray
     * @return
     */
    ServiceInstance choose(String serviceId, boolean gray);
}
