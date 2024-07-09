package cn.edu.ecnu.stu.gateway.disruptor;


public interface ParallelQueue<E> {

    /**
     * 添加元素
     * @param event
     */
    void add(E event);

    /**
     * 启动
     */
    void start();

    /**
     * 销毁
     */
    void shutDown();

}
