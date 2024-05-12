package org.imooc.core.disruptor;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 监听接口
 * @USER: WuYang
 * @DATE: 2023/5/6 23:30
 */
public interface EventListener<E> {

    void onEvent(E event);

    void onException(Throwable ex,long sequence,E event);

}
