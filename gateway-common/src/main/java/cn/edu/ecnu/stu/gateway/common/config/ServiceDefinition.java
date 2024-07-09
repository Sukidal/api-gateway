package cn.edu.ecnu.stu.gateway.common.config;

import lombok.Builder;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 资源服务定义类：无论下游是什么样的服务都需要进行注册
 */
@Builder
public class ServiceDefinition implements Serializable {

	private static final long serialVersionUID = -8263365765897285189L;
	
	/**
	 * 	服务唯一id
	 */
	private String serviceId;
	
	/**
	 * 	环境名称
	 */
	private String envType;

	/**
	 * 	服务启用禁用
	 */
	private boolean enable = true;


	public ServiceDefinition() {
		super();
	}
	
	public ServiceDefinition(String serviceId,
                             String envType, boolean enable) {
		super();
		this.serviceId = serviceId;
		this.envType = envType;
		this.enable = enable;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(this == null || getClass() != o.getClass()) return false;
		ServiceDefinition serviceDefinition = (ServiceDefinition)o;
		return Objects.equals(serviceId, serviceDefinition.serviceId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(serviceId);
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getEnvType() {
		return envType;
	}

	public void setEnvType(String envType) {
		this.envType = envType;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	

}
