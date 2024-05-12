package org.imooc.core.netty.processor;

import org.imooc.core.context.HttpRequestWrapper;

public interface NettyProcessor {

    void process(HttpRequestWrapper wrapper);

    void  start();

    void shutDown();
}
