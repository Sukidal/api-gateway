package cn.edu.ecnu.stu.gateway.common.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * 一个服务定义会对应多个服务实例
 */
public class ServiceInstance implements Serializable {

	private static final long serialVersionUID = -7559569289189228478L;

	/**
	 * 	服务实例ID: ip:port
	 */
	protected String serviceInstanceId;
	
	/**
	 * 	服务定义唯一id： uniqueId
	 */
	protected String serviceId;

	/**
	 * 	服务实例地址： ip:port
	 */
	protected String ip;

	protected int port;
	/**
	 * 	权重信息
	 */
	protected double weight;
	
	/**
	 * 	服务注册的时间戳：后面我们做负载均衡，warmup预热
	 */
	protected long registerTime;
	
	/**
	 * 	服务实例启用禁用
	 */
	protected boolean enable = true;

	@Getter
	@Setter
	private boolean gray;

	public ServiceInstance() {
		super();
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public long getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(long registerTime) {
		this.registerTime = registerTime;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(this == null || getClass() != o.getClass()) {
			return false;
		}
		ServiceInstance serviceInstance = (ServiceInstance)o;
		return Objects.equals(serviceInstanceId, serviceInstance.serviceInstanceId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(serviceInstanceId);
	}
	
}
