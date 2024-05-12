package org.imooc.core.netty;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.imooc.core.Config;
import org.imooc.core.LifeCycle;
import org.imooc.core.helper.AsyncHttpHelper;

import java.io.IOException;

@Slf4j
public class NettyHttpClient implements LifeCycle {
    private final Config config;

    private final EventLoopGroup eventLoopGroupWoker;

    private AsyncHttpClient asyncHttpClient;

    public NettyHttpClient(Config config, EventLoopGroup eventLoopGroupWoker) {
        this.config = config;
        this.eventLoopGroupWoker = eventLoopGroupWoker;
        init();
    }

    @Override
    public void init() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroupWoker)
                .setConnectTimeout(config.getHttpConnectTimeout())
                .setRequestTimeout(config.getHttpRequestTimeout())
                .setMaxRedirects(config.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT) //池化的byteBuf分配器，提升性能
                .setCompressionEnforced(true)
                .setMaxConnections(config.getHttpMaxConnections())
                .setMaxConnectionsPerHost(config.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(config.getHttpPooledConnectionIdleTimeout());
        this.asyncHttpClient = new DefaultAsyncHttpClient(builder.build());
    }

    @Override
    public void start() {
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if (asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                log.error("NettyHttpClient shutdown error", e);
            }
        }
    }
}
