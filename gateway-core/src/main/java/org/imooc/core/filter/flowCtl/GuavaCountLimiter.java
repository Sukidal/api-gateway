package org.imooc.core.filter.flowCtl;

import com.google.common.util.concurrent.RateLimiter;
import org.imooc.common.config.FlowCtlConfig;
import org.imooc.common.config.Rule;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 单机限流
 * @USER: WuYang
 * @DATE: 2023/4/15 23:17
 */
public class GuavaCountLimiter {

    private RateLimiter rateLimiter;

    public GuavaCountLimiter(long maxPermits) {
        rateLimiter = RateLimiter.create(maxPermits);
    }

    public GuavaCountLimiter(long maxPermits,long warmUpPeriodAsSecond) {
        rateLimiter = RateLimiter.create(maxPermits,warmUpPeriodAsSecond, TimeUnit.SECONDS);
    }

    private static final ConcurrentHashMap<String,GuavaCountLimiter> resourceRateLimiterMap = new ConcurrentHashMap<String,GuavaCountLimiter>();

    public static GuavaCountLimiter getInstance(String key) {
        return resourceRateLimiterMap.get(key);
    }

    public static void init(List<Rule> rules) {
        for (Rule rule : rules) {
            for (FlowCtlConfig flowCtlConfig : rule.getFlowCtlConfigs()) {
                resourceRateLimiterMap.put(flowCtlConfig.getPath(), new GuavaCountLimiter(flowCtlConfig.getPermitPerSecond()));
            }
        }
    }

    public boolean acquire(){
        return rateLimiter.tryAcquire(1);
    }
}
