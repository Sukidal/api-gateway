package cn.edu.ecnu.stu.gateway.gateway.register.center.api;

import cn.edu.ecnu.stu.gateway.common.config.ServiceDefinition;
import cn.edu.ecnu.stu.gateway.common.config.ServiceInstance;

import java.util.List;

public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition,
                  List<ServiceInstance> serviceInstanceSet);
}
