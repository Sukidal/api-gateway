package cn.edu.ecnu.stu.gateway.filter.loadbalance;

import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.config.ServiceInstance;
import cn.edu.ecnu.stu.gateway.common.exception.NotFoundException;
import cn.edu.ecnu.stu.gateway.Config;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;
import cn.edu.ecnu.stu.gateway.request.GatewayRequest;

import static cn.edu.ecnu.stu.gateway.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

@Slf4j
@FilterAspect(id = FilterConst.LOAD_BALANCE_FILTER_ID,
              name = FilterConst.LOAD_BALANCE_FILTER_NAME,
              order = FilterConst.LOAD_BALANCE_FILTER_ORDER)
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
        if (strategy.equals(FilterConst.LOAD_BALANCE_STRATEGY_ROUND_ROBIN)) {
            loadBalanceRule = RoundRobinLoadBalanceRule.getInstance();
        } else {
            loadBalanceRule = RandomLoadBalanceRule.getInstance();
        }
        return loadBalanceRule;
    }
}
