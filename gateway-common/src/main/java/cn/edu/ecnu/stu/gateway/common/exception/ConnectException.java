package cn.edu.ecnu.stu.gateway.common.exception;

import lombok.Getter;
import cn.edu.ecnu.stu.gateway.common.enums.ResponseCode;

public class ConnectException extends BaseException {

	private static final long serialVersionUID = -8503239867913964958L;

	@Getter
	private final String uniqueId;
	
	@Getter
	private final String requestUrl;
	
	public ConnectException(String uniqueId, String requestUrl) {
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}
	
	public ConnectException(Throwable cause, String uniqueId, String requestUrl, ResponseCode code) {
		super(code.getMessage(), cause, code);
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}

}
