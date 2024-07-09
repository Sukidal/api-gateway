package cn.edu.ecnu.stu.gateway.filter.loadbalance;

import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.config.DynamicServiceManager;
import cn.edu.ecnu.stu.gateway.common.config.ServiceInstance;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

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
