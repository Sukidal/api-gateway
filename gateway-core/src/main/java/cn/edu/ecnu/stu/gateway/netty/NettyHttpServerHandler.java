package cn.edu.ecnu.stu.gateway.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import cn.edu.ecnu.stu.gateway.context.HttpRequestWrapper;
import cn.edu.ecnu.stu.gateway.netty.processor.NettyProcessor;

public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    private final NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest request = (FullHttpRequest) msg;
        HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
        httpRequestWrapper.setCtx(ctx);
        httpRequestWrapper.setRequest(request);

        nettyProcessor.process(httpRequestWrapper);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
