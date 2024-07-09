package cn.edu.ecnu.stu.gateway.gateway.register.center.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.config.ServiceDefinition;
import cn.edu.ecnu.stu.gateway.common.config.ServiceInstance;
import cn.edu.ecnu.stu.gateway.common.constants.BasicConst;
import cn.edu.ecnu.stu.gateway.gateway.register.center.api.RegisterCenter;
import cn.edu.ecnu.stu.gateway.gateway.register.center.api.RegisterCenterListener;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class NacosRegisterCenter implements RegisterCenter {
    private String registerAddress;

    private String env;

    //主要用于维护服务实例信息
    private NamingService namingService;

    //主要用于维护服务定义信息
    private NamingMaintainService namingMaintainService;

    //监听器列表
    private List<RegisterCenterListener> registerCenterListenerList = new CopyOnWriteArrayList<>();

    @Override
    public void init(String registerAddress, String env) {
        this.registerAddress = registerAddress;
        this.env = env;

        try {
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
            this.namingService = NamingFactory.createNamingService(registerAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            //构造nacos实例信息
            Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
            nacosInstance.setPort(serviceInstance.getPort());
            nacosInstance.setIp(serviceInstance.getIp());
//            nacosInstance.setMetadata(Map.of(GatewayConst.META_DATA_KEY,
//                JSON.toJSONString(serviceInstance)));

            //注册
            namingService.registerInstance(serviceDefinition.getServiceId(), env, nacosInstance);

//            //更新服务定义
//            namingMaintainService.updateService(serviceDefinition.getServiceId(), env, 0,
//                Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceDefinition)));

            log.info("register {} {}", serviceDefinition, serviceInstance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            namingService.registerInstance(serviceDefinition.getServiceId(),
                env, serviceInstance.getIp(), serviceInstance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeAllServices(RegisterCenterListener registerCenterListener) {
        registerCenterListenerList.add(registerCenterListener);
        doSubscribeAllServices();
        //可能有新服务加入，所以需要有一个定时任务来检查
        ScheduledExecutorService scheduledThreadPool = Executors
            .newScheduledThreadPool(1, new NameThreadFactory("doSubscribeAllServices"));
        scheduledThreadPool.scheduleWithFixedDelay(() -> doSubscribeAllServices(),
            0, 10, TimeUnit.SECONDS);

    }

    private void doSubscribeAllServices() {
        try {
            //已经订阅的服务
            List<String> subscribeService = namingService.getSubscribeServices().stream()
                .map(ServiceInfo::getName).collect(Collectors.toList());

            int pageNo = 1;
            int pageSize = 100;

            //分页从nacos拿到服务列表
            List<String> serviceList = namingService
                .getServicesOfServer(pageNo, pageSize, env).getData();

            while (CollectionUtils.isNotEmpty(serviceList)) {
                log.info("service list size {}", serviceList.size());

                for (String service : serviceList) {
                    if (subscribeService.contains(service)) {
                        continue;
                    }
                    //nacos事件监听器
                    EventListener eventListener = new NacosRegisterListener();
                    List<Instance> allInstances = namingService.getAllInstances(service, env);
                    eventListener.onEvent(new NamingEvent(service, allInstances));
                    namingService.subscribe(service, env, eventListener);
                    log.info("subscribe {} {}", service, env);
                }

                serviceList = namingService
                    .getServicesOfServer(++pageNo, pageSize, env).getData();
            }

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    public class NacosRegisterListener implements EventListener {

        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;
                String serviceName = namingEvent.getServiceName();
                List<Instance> instances = namingEvent.getInstances();

                try {
                    //获取服务定义信息
                    Service service = namingMaintainService.queryService(serviceName, env);
                    serviceName = service.getName();
                    ServiceDefinition serviceDefinition = new ServiceDefinition();
                    serviceDefinition.setServiceId(serviceName);
                    serviceDefinition.setEnvType(env);
                    serviceDefinition.setEnable(true);

                    //获取服务实例信息
                    List<ServiceInstance> serviceInstances = instances.stream().map(instance -> {
                        ServiceInstance serviceInstance1 = new ServiceInstance();
                        serviceInstance1.setEnable(true);
                        String ip = instance.getIp();
                        int port = instance.getPort();
                        serviceInstance1.setIp(ip);
                        serviceInstance1.setWeight(instance.getWeight());
                        serviceInstance1.setPort(port);
                        serviceInstance1.setServiceId(instance.getServiceName());
                        serviceInstance1.setRegisterTime(System.currentTimeMillis());
                        serviceInstance1.setServiceInstanceId(ip + BasicConst.COLON_SEPARATOR + port);
                        Map<String, String> metadata = instance.getMetadata();
                        serviceInstance1.setGray(Boolean.parseBoolean(metadata.getOrDefault("gray", "false")));
                        return serviceInstance1;
                    }).collect(Collectors.toList());

                    registerCenterListenerList
                        .forEach(l -> l.onChange(serviceDefinition, serviceInstances));
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }
}
