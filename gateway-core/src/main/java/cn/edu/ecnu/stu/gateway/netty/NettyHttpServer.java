package cn.edu.ecnu.stu.gateway.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.common.utils.RemotingUtil;
import cn.edu.ecnu.stu.gateway.Config;
import cn.edu.ecnu.stu.gateway.LifeCycle;
import cn.edu.ecnu.stu.gateway.netty.processor.NettyProcessor;

import java.net.InetSocketAddress;

@Slf4j
public class NettyHttpServer implements LifeCycle {
    private final Config config;
    private final NettyProcessor nettyProcessor;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup eventLoopGroupBoss;

    @Getter
    private EventLoopGroup eventLoopGroupWorker;

    public NettyHttpServer(Config config, NettyProcessor nettyProcessor) {
        this.config = config;
        this.nettyProcessor = nettyProcessor;
        init();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEpoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(config.getEventLoopGroupBossNum(),
                new DefaultThreadFactory("netty-boss-nio"));
            this.eventLoopGroupWorker = new EpollEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                new DefaultThreadFactory("netty-worker-nio"));
        } else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(config.getEventLoopGroupBossNum(),
                new DefaultThreadFactory("netty-boss-nio"));
            this.eventLoopGroupWorker = new NioEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                new DefaultThreadFactory("netty-worker-nio"));
        }
    }

    public boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        this.serverBootstrap
            .group(eventLoopGroupBoss, eventLoopGroupWorker)
            .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 1024)			//	sync + accept = backlog
            .option(ChannelOption.SO_REUSEADDR, true)   	//	tcp端口重绑定
            .option(ChannelOption.SO_KEEPALIVE, false)  	//  如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
            .childOption(ChannelOption.TCP_NODELAY, true)   //	该参数的左右就是禁用Nagle算法，使用小数据传输时合并
            .childOption(ChannelOption.SO_SNDBUF, 65535)	//	设置发送数据缓冲区大小
            .childOption(ChannelOption.SO_RCVBUF, 65535)	//	设置接收数据缓冲区大小
            .localAddress(new InetSocketAddress(config.getPort()))
            .childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(
                        new HttpServerCodec(), //http编解码
                        new HttpObjectAggregator(config.getMaxContentLength()), //请求报文聚合成FullHttpRequest
                        new HttpServerExpectContinueHandler(),
                        new NettyHttpServerHandler(nettyProcessor),
                        new NettyServerConnectManagerHandler()
                    );
                }
            });

        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port {}", this.config.getPort());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void shutdown() {
        if (eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }

        if (eventLoopGroupWorker != null) {
            eventLoopGroupWorker.shutdownGracefully();
        }
    }
}
