package cn.edu.ecnu.stu.gateway.helper;

import cn.edu.ecnu.stu.gateway.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import cn.edu.ecnu.stu.gateway.common.constants.BasicConst;
import cn.edu.ecnu.stu.gateway.common.exception.PathNoMatchedException;
import cn.edu.ecnu.stu.gateway.Config;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.request.GatewayRequest;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;



public class RequestHelper {

	public static GatewayContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {
		//根据请求对象获取规则
		Rule rule = Config.getInstance().getRule(request.uri());
		if(rule == null)
			throw new PathNoMatchedException();

		//	构建请求对象GatewayRequest
		GatewayRequest gateWayRequest = doRequest(request, ctx, rule);
		
		//	构建我们而定GateWayContext对象
		GatewayContext gatewayContext = new GatewayContext(
				ctx,
				HttpUtil.isKeepAlive(request),
				gateWayRequest,
				rule
		);


		//后续服务发现做完，这里都要改成动态的--以及在负载均衡算法实现
		//gatewayContext.getRequest().setModifyHost("127.0.0.1:8080");

		return gatewayContext;
	}
	
	/**
	 *构建Request请求对象
	 */
	private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx, Rule rule) {

		HttpHeaders headers = fullHttpRequest.headers();
		//	从header头获取必须要传入的关键属性 uniqueId
		String url = rule.getUrl();
		String serviceId = url.startsWith("lb://") ? url.substring(5) : null;
		
		String host = headers.get(HttpHeaderNames.HOST);
		headers.remove(HttpHeaderNames.HOST);
		HttpMethod method = fullHttpRequest.method();
		String uri = fullHttpRequest.uri();
		String clientIp = getClientIp(ctx, fullHttpRequest);
		String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
		Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

		GatewayRequest gatewayRequest = new GatewayRequest(serviceId,
				charset,
				clientIp,
				host, 
				uri, 
				method,
				contentType,
				headers,
				fullHttpRequest);
		
		return gatewayRequest;
	}
	
	/**
	 * 获取客户端ip
	 */
	private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
		String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);
		
		String clientIp = null;
		if(StringUtils.isNotEmpty(xForwardedValue)) {
			List<String> values = Arrays.asList(xForwardedValue.split(", "));
			if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
				clientIp = values.get(0);
			}
		}
		if(clientIp == null) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
			clientIp = inetSocketAddress.getAddress().getHostAddress();
		}
		return clientIp;
	}
}
