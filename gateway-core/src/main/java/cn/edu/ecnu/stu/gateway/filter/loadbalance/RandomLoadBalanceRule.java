package cn.edu.ecnu.stu.gateway.filter.loadbalance;

import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.config.DynamicServiceManager;
import cn.edu.ecnu.stu.gateway.common.config.ServiceInstance;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class RandomLoadBalanceRule implements IGatewayLoadBalanceRule{

    private static volatile RandomLoadBalanceRule randomLoadBalanceRule;

    public static RandomLoadBalanceRule getInstance(){
        if(randomLoadBalanceRule == null) {
            synchronized (RandomLoadBalanceRule.class) {
                if(randomLoadBalanceRule == null)
                    randomLoadBalanceRule = new RandomLoadBalanceRule();
            }
        }
        return randomLoadBalanceRule;
    }


    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        String serviceId = ctx.getServiceId();
        return choose(serviceId, ctx.isGray());
    }

    @Override
    public ServiceInstance choose(String serviceId, boolean gray) {
        List<ServiceInstance> instances =  DynamicServiceManager.getInstance().getServiceInstanceByServiceId(serviceId, gray);
        if(instances == null || instances.isEmpty())
            return null;
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        return instances.get(index);
    }
}
