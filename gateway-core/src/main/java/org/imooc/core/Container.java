package org.imooc.core;

import lombok.extern.slf4j.Slf4j;
import org.imooc.core.netty.NettyHttpClient;
import org.imooc.core.netty.NettyHttpServer;
import org.imooc.core.netty.processor.DisruptorNettyCoreProcessor;
import org.imooc.core.netty.processor.NettyCoreProcessor;
import org.imooc.core.netty.processor.NettyProcessor;

import static org.imooc.common.constants.GatewayConst.BUFFER_TYPE_PARALLEL;

@Slf4j
public class Container implements LifeCycle {
    private final Config config;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
        if(BUFFER_TYPE_PARALLEL.equals(config.getBufferType())){
            this.nettyProcessor = new DisruptorNettyCoreProcessor(nettyCoreProcessor);
        }else{
            this.nettyProcessor = nettyCoreProcessor;
        }

        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);

        this.nettyHttpClient = new NettyHttpClient(config,
                nettyHttpServer.getEventLoopGroupWorker());
    }

    @Override
    public void start() {
        nettyProcessor.start();
        nettyHttpServer.start();;
        nettyHttpClient.start();
        log.info("api gateway started!");
    }

    @Override
    public void shutdown() {
        nettyProcessor.shutDown();
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
    }
}
