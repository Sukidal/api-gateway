package org.imooc.core.netty.processor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.imooc.common.enums.ResponseCode;
import org.imooc.common.exception.BaseException;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.context.HttpRequestWrapper;
import org.imooc.core.filter.FilterFactory;
import org.imooc.core.filter.GatewayFilterChainFactory;
import org.imooc.core.helper.RequestHelper;
import org.imooc.core.helper.ResponseHelper;

@Slf4j
public class NettyCoreProcessor implements NettyProcessor {

    private final FilterFactory filterFactory = GatewayFilterChainFactory.getInstance();

    @Override
    public void process(HttpRequestWrapper wrapper) {
        FullHttpRequest request = wrapper.getRequest();
        ChannelHandlerContext ctx = wrapper.getCtx();

        try {
            GatewayContext gatewayContext = RequestHelper.doContext(request, ctx);
            //执行过滤器逻辑
            filterFactory.buildFilterChain(gatewayContext).doFilter(gatewayContext);
        } catch (BaseException e) {
            log.error("process error {} {}", e.getCode().getCode(), e.getCode().getMessage());
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(ctx, request, httpResponse);
        } catch (Throwable t) {
            log.error("process unknown error", t);
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(ctx, request, httpResponse);
        }
    }



    private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse httpResponse) {
        ctx.writeAndFlush(httpResponse)
                .addListener(ChannelFutureListener.CLOSE); //释放资源后关闭channel
        ReferenceCountUtil.release(request);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutDown() {

    }


}
