package cn.edu.ecnu.stu.gateway.disruptor;

public interface EventListener<E> {

    void onEvent(E event);

    void onException(Throwable ex,long sequence,E event);

}
