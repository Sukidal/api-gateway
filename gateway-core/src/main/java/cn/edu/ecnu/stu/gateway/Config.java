package cn.edu.ecnu.stu.gateway;

import com.lmax.disruptor.*;
import lombok.Data;
import cn.edu.ecnu.stu.gateway.common.config.Rule;
import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import cn.edu.ecnu.stu.gateway.filter.flowCtl.GuavaCountLimiter;

import java.util.List;

@Data
public class Config {

    public static void setConfig(Config config) {
        Config.config = config;
    }

    public static Config getInstance() {
        return Config.config;
    }

    private static Config config;

    private int port = 8888;

    private int prometheusPort = 18000;

    private String applicationName = "api-gateway";

    private String registryAddress = "127.0.0.1:8848";

    private String env = "dev";

    //netty
    private double grayRate = 0.001;

    private boolean enableGray = false;

    private boolean enableSecurity = false;

    private String secretKey = "";

    private String securityKeyName = "";

    private int eventLoopGroupBossNum = 2;

    private int eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors() / 2;

    private int maxContentLength = 64 * 1024 * 1024;

    //默认单异步模式
    private boolean whenComplete = true;

    //	Http Async 参数选项：

    //	连接超时时间
    private int httpConnectTimeout = 30 * 1000;

    //	请求超时时间
    private int httpRequestTimeout = 30 * 1000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 2;

    //	客户端请求最大连接数
    private int httpMaxConnections = 10000;

    //	客户端每个地址支持的最大连接数
    private int httpConnectionsPerHost = 8000;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

    private String bufferType = "parallel";

    private int bufferSize = 8 * 1024 * 1024;

    private int processThread = Runtime.getRuntime().availableProcessors() * 3 / 2;

    private String waitStrategy = "yielding";

    private String loadBalanceStrategy = FilterConst.LOAD_BALANCE_STRATEGY_ROUND_ROBIN;

    private String mockConfig = "{}";

    public WaitStrategy getWaitStrategy(){
        switch (waitStrategy){
            case "busySpin":
                return new BusySpinWaitStrategy();
            case "yielding":
                return new YieldingWaitStrategy();
            case "sleeping":
                return new SleepingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }

    private String ruleStr;

    private List<Rule> rules;

    public void updateRules(List<Rule> ruleList) {
        if(rules == null) {
            rules = ruleList;
        } else {
            for (Rule rule : ruleList) {
                int size = rules.size();
                boolean change = false;
                for(int i = 0; i < size; i++) {
                    Rule rule1 = rules.get(i);
                    if(rule1.getId().equals(rule.getId())) {
                        change = true;
                        rules.set(i, rule);
                        break;
                    }
                }
                if(!change)
                    rules.add(rule);
            }
        }
        rules.forEach(Rule::initFilterConfigMap);
        GuavaCountLimiter.init(rules);
    }

    public Rule getRule(String path) {
        if(path == null)
            return null;
        for (Rule rule : rules) {
            if(path.matches(rule.getUrlPattern()))
                return rule;
        }
        return null;
    }
    //扩展.......
}
