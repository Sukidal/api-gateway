package cn.edu.ecnu.stu.gateway.filter.flowCtl;

import com.google.common.util.concurrent.RateLimiter;
import cn.edu.ecnu.stu.gateway.common.config.FlowCtlConfig;
import cn.edu.ecnu.stu.gateway.common.config.Rule;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


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
