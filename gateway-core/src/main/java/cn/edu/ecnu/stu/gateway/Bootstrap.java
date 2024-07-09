package cn.edu.ecnu.stu.gateway;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.config.DynamicServiceManager;
import cn.edu.ecnu.stu.gateway.common.config.ServiceDefinition;
import cn.edu.ecnu.stu.gateway.common.config.ServiceInstance;
import cn.edu.ecnu.stu.gateway.common.constants.BasicConst;
import cn.edu.ecnu.stu.gateway.common.utils.NetUtils;
import cn.edu.ecnu.stu.gateway.common.utils.TimeUtil;
import cn.edu.ecnu.stu.gateway.config.center.api.ConfigCenter;
import cn.edu.ecnu.stu.gateway.gateway.register.center.api.RegisterCenter;
import cn.edu.ecnu.stu.gateway.gateway.register.center.api.RegisterCenterListener;

import java.util.List;
import java.util.ServiceLoader;

/**
 * API网关启动类
 *
 */
@Slf4j
public class Bootstrap {
    public static void main(String[] args) {
        //加载网关核心静态配置
        Config config = ConfigLoader.getInstance().load(args);
        Config.setConfig(config);
        System.out.println(config.getPort());

        //插件初始化
        //配置中心管理器初始化，连接配置中心，监听配置的新增、修改、删除
        ServiceLoader<ConfigCenter> serviceLoader = ServiceLoader.load(ConfigCenter.class);
        final ConfigCenter configCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found ConfigCenter impl");
            return new RuntimeException("not found ConfigCenter impl");
        });
        configCenter.init(config.getRegistryAddress(), config.getEnv());
        configCenter.subscribeRulesChange(config::updateRules);

        //启动容器
        Container container = new Container(config);
        container.start();

        //连接注册中心，将注册中心的实例加载到本地
        final RegisterCenter registerCenter = registerAndSubscribe(config);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                registerCenter.deregister(buildGatewayServiceDefinition(config),
                    buildGatewayServiceInstance(config));
                container.shutdown();
            }
        });
    }

    private static RegisterCenter registerAndSubscribe(Config config) {
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        final RegisterCenter registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found RegisterCenter impl");
            return new RuntimeException("not found RegisterCenter impl");
        });
        registerCenter.init(config.getRegistryAddress(), config.getEnv());

        //构造网关服务定义和服务实例
        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);

        //注册
        registerCenter.register(serviceDefinition, serviceInstance);

        //订阅
        registerCenter.subscribeAllServices(new RegisterCenterListener() {
            @Override
            public void onChange(ServiceDefinition serviceDefinition, List<ServiceInstance> serviceInstances) {
                log.info("refresh service and instance: {} {}", serviceDefinition.getServiceId(),
                    JSON.toJSON(serviceInstances));
                DynamicServiceManager manager = DynamicServiceManager.getInstance();
                manager.addServiceInstance(serviceDefinition.getServiceId(), serviceInstances);
                manager.putServiceDefinition(serviceDefinition.getServiceId(),serviceDefinition);
            }
        });
        return registerCenter;
    }

    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp = NetUtils.getLocalIp();
        int port = config.getPort();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp + BasicConst.COLON_SEPARATOR + port);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        return serviceInstance;
    }

    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }
}
