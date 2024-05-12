package org.imooc.core.filter.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.imooc.common.config.DynamicServiceManager;
import org.imooc.common.config.ServiceInstance;
import org.imooc.core.context.GatewayContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 负载均衡-轮询算法
 * @USER: WuYang
 * @DATE: 2023/3/12 22:22
 */
@Slf4j
public class RoundRobinLoadBalanceRule implements IGatewayLoadBalanceRule{

    private final AtomicInteger position = new AtomicInteger(0);

    private static volatile RoundRobinLoadBalanceRule roundRobinLoadBalanceRule;

    public static RoundRobinLoadBalanceRule getInstance(){
        if (roundRobinLoadBalanceRule == null) {
            synchronized (RoundRobinLoadBalanceRule.class) {
                if(roundRobinLoadBalanceRule == null)
                    roundRobinLoadBalanceRule = new RoundRobinLoadBalanceRule();
            }
        }
        return roundRobinLoadBalanceRule;
    }

    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        return choose(ctx.getServiceId(), ctx.isGray());
    }

    @Override
    public ServiceInstance choose(String serviceId, boolean gray) {
        List<ServiceInstance> serviceInstances =  DynamicServiceManager.getInstance().getServiceInstanceByServiceId(serviceId, gray);
        if(serviceInstances == null || serviceInstances.isEmpty())
            return null;
        int index = this.position.getAndUpdate(new IntUnaryOperator() {
            @Override
            public int applyAsInt(int operand) {
                return (operand + 1) % serviceInstances.size();
            }
        });
        return serviceInstances.get(index);
    }
}
