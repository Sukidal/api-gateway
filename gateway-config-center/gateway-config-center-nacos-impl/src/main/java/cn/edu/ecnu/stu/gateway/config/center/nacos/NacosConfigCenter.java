package cn.edu.ecnu.stu.gateway.config.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.config.Rule;
import cn.edu.ecnu.stu.gateway.config.center.api.ConfigCenter;
import cn.edu.ecnu.stu.gateway.config.center.api.RulesChangeListener;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public class NacosConfigCenter implements ConfigCenter {
    private static final String DATA_ID = "api-gateway";

    private String serverAddr;

    private String env;

    private ConfigService configService;

    @Override
    public void init(String serverAddr, String env) {
        this.serverAddr = serverAddr;
        this.env = env;

        try {
            configService = NacosFactory.createConfigService(serverAddr);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Rule> parseRule(String config) {
        log.info("config from nacos: {}", config);
        List<Rule> rules = null;
        try {
            rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
        } catch (Exception e) {
            log.error("rules parse error");
        }
        return rules;
    }

    @Override
    public void subscribeRulesChange(RulesChangeListener listener) {
        try {
            //初始化通知
            String config = configService.getConfig(DATA_ID, env, 5000);
            //{"rules":[{}, {}]}
            List<Rule> rules = parseRule(config);
            listener.onRulesChange(rules);

            //监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    List<Rule> rules = parseRule(configInfo);
                    listener.onRulesChange(rules);
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}
