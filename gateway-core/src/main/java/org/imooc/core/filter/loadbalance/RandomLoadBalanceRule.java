package org.imooc.core.filter.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.imooc.common.config.DynamicServiceManager;
import org.imooc.common.config.ServiceInstance;
import org.imooc.common.exception.NotFoundException;
import org.imooc.core.context.GatewayContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.imooc.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 负载均衡-随机
 * @USER: WuYang
 * @DATE: 2023/3/12 22:13
 */
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
