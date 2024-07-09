package cn.edu.ecnu.stu.gateway.netty.processor;

import cn.edu.ecnu.stu.gateway.context.HttpRequestWrapper;

public interface NettyProcessor {

    void process(HttpRequestWrapper wrapper);

    void  start();

    void shutDown();
}
