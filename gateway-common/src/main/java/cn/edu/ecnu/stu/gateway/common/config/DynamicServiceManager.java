package cn.edu.ecnu.stu.gateway.common.config;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 动态服务缓存配置管理类
 */
public class DynamicServiceManager {

	private final ConcurrentHashMap<String /* serviceId */ , ServiceDefinition>  serviceDefinitionMap = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String /* serviceId */ , List<ServiceInstance>>  serviceInstanceMap = new ConcurrentHashMap<>();

	private DynamicServiceManager() {
	}
	
	private static class SingletonHolder {
		private static final DynamicServiceManager INSTANCE = new DynamicServiceManager();
	}
	
	
	/***************** 	对服务定义缓存进行操作的系列方法 	***************/
	
	public static DynamicServiceManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void putServiceDefinition(String uniqueId, 
			ServiceDefinition serviceDefinition) {
		
		serviceDefinitionMap.put(uniqueId, serviceDefinition);;
	}
	
	public ServiceDefinition getServiceDefinition(String uniqueId) {
		return serviceDefinitionMap.get(uniqueId);
	}
	
	/***************** 	对服务实例缓存进行操作的系列方法 	***************/

	public List<ServiceInstance> getServiceInstanceByServiceId(String serviceId, boolean gray){
		List<ServiceInstance> serviceInstances = serviceInstanceMap.get(serviceId);
		if (CollectionUtils.isEmpty(serviceInstances)) {
			return serviceInstances;
		}

		if (gray) {
			return  serviceInstances.stream()
					.filter(ServiceInstance::isGray)
					.collect(Collectors.toList());
		}

		return serviceInstances;
	}
	
	public void addServiceInstance(String uniqueId, List<ServiceInstance> serviceInstances) {
		serviceInstanceMap.put(uniqueId, serviceInstances);
	}
}
