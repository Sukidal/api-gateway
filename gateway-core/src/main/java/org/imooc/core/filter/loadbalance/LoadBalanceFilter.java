package org.imooc.core.filter.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.imooc.common.config.ServiceInstance;
import org.imooc.common.exception.NotFoundException;
import org.imooc.core.Config;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;
import org.imooc.core.request.GatewayRequest;

import static org.imooc.common.constants.FilterConst.*;
import static org.imooc.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 负载均衡过滤器
 * @USER: WuYang
 * @DATE: 2023/3/12 22:02
 */
@Slf4j
@FilterAspect(id=LOAD_BALANCE_FILTER_ID,
              name = LOAD_BALANCE_FILTER_NAME,
              order = LOAD_BALANCE_FILTER_ORDER)
public class LoadBalanceFilter implements Filter {

    private final IGatewayLoadBalanceRule gatewayLoadBalanceRule = getLoadBalanceRule();

    @Override
    public void doFilter(GatewayContext ctx){
        String serviceId = ctx.getServiceId();
        GatewayRequest request = ctx.getRequest();
        if(serviceId == null) {
            request.setRedirectUrl(ctx.getRule().getUrl() + request.getModifyPath());
            return;
        }
        ServiceInstance serviceInstance = gatewayLoadBalanceRule.choose(serviceId, ctx.isGray());
        if(serviceInstance == null){
            log.warn("No instance available for:{}",serviceId);
            throw new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }
        String serviceInstanceId = serviceInstance.getServiceInstanceId();
        request.setRedirectUrl(request.getModifyScheme() + serviceInstanceId + request.getModifyPath());
    }

    /**
     * 根据配置获取负载均衡器
     *
     * @return
     */
    public IGatewayLoadBalanceRule getLoadBalanceRule() {
        IGatewayLoadBalanceRule loadBalanceRule;
        String strategy = Config.getInstance().getLoadBalanceStrategy();
        if (strategy.equals(LOAD_BALANCE_STRATEGY_ROUND_ROBIN)) {
            loadBalanceRule = RoundRobinLoadBalanceRule.getInstance();
        } else {
            loadBalanceRule = RandomLoadBalanceRule.getInstance();
        }
        return loadBalanceRule;
    }
}
