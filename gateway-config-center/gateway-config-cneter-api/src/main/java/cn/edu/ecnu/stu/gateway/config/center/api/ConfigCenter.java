package cn.edu.ecnu.stu.gateway.config.center.api;

public interface ConfigCenter {

    void init(String serverAddr, String env);

    void subscribeRulesChange(RulesChangeListener listener);
}
