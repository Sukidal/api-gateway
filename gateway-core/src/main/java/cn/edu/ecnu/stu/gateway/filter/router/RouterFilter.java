package cn.edu.ecnu.stu.gateway.filter.router;

import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import cn.edu.ecnu.stu.gateway.common.enums.ResponseCode;
import cn.edu.ecnu.stu.gateway.common.exception.ConnectException;
import cn.edu.ecnu.stu.gateway.common.exception.ResponseException;
import cn.edu.ecnu.stu.gateway.ConfigLoader;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;
import cn.edu.ecnu.stu.gateway.helper.AsyncHttpHelper;
import cn.edu.ecnu.stu.gateway.helper.ResponseHelper;
import cn.edu.ecnu.stu.gateway.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION:路由过滤器
 * @USER: WuYang
 * @DATE: 2023/3/18 21:40
 */
@Slf4j
@FilterAspect(id = FilterConst.ROUTER_FILTER_ID,
              name = FilterConst.ROUTER_FILTER_NAME,
              order = FilterConst.ROUTER_FILTER_ORDER)
public class RouterFilter implements Filter {
    private static Logger accessLog = LoggerFactory.getLogger("accessLog");

    @Override
    public void doFilter(GatewayContext gatewayContext) throws Exception {
        Request request = gatewayContext.getRequest().build();
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);

        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();

        if (whenComplete) {
            future.whenComplete((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            });
        } else {
            future.whenCompleteAsync((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            });
        }
    }

    private void complete(Request request,
                          Response response,
                          Throwable throwable,
                          GatewayContext gatewayContext) {
        gatewayContext.releaseRequest();

        try {
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time out {}", url);
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                    gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    gatewayContext.setThrowable(new ConnectException(throwable,
                            gatewayContext.getServiceId(),
                            url, ResponseCode.HTTP_RESPONSE_ERROR));
                    gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Throwable t) {
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.INTERNAL_ERROR));
            log.error("complete error", t);
        } finally {
            gatewayContext.written();
            ResponseHelper.writeResponse(gatewayContext);

            accessLog.info(
                    "{} {} {} {} {} {} {}",
                    System.currentTimeMillis() - gatewayContext.getRequest().getBeginTime(),
                    gatewayContext.getRequest().getClientIp(),
                    gatewayContext.getRequest().getServiceId(),
                    gatewayContext.getRequest().getMethod(),
                    gatewayContext.getRequest().getPath(),
                    gatewayContext.getResponse().getHttpResponseStatus().code(),
                    gatewayContext.getResponse().getFutureResponse().getResponseBodyAsBytes().length
            );
        }
    }
}
