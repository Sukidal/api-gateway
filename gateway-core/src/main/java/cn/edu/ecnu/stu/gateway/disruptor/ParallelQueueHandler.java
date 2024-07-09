package cn.edu.ecnu.stu.gateway.disruptor;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ParallelQueueHandler<E> implements ParallelQueue<E> {

    private final RingBuffer<Holder> ringBuffer;

    private final EventListener<E> eventListener;

    private final WorkerPool<Holder> workerPool;

    private final ExecutorService executorService;

    private final EventTranslatorOneArg<Holder,E> eventTranslator;

    public ParallelQueueHandler(Builder<E> builder) {
        this.executorService = Executors.newFixedThreadPool(builder.threads,
                new ThreadFactoryBuilder().setNameFormat("ParallelQueueHandler"+builder.namePrefix+"-pool-%d").build());

        this.eventListener = builder.listener;
        this.eventTranslator = new HolderEventTranslator();

        //创建RingBuffer
        ringBuffer = RingBuffer.create(
                builder.producerType,
                new HolderEventFactory(),
                builder.bufferSize,
                builder.waitStrategy
        );

        //通过RingBuffer 创建屏障
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        //创建多个消费者组
        WorkHandler<Holder>[] workHandlers = new WorkHandler[builder.threads];
        for (int i = 0; i < workHandlers.length; i++) {
            workHandlers[i] = new HolderWorkHandler();
        }

        //创建多消费者线程池
        WorkerPool<Holder> workerPool = new WorkerPool<>(
                ringBuffer,
                sequenceBarrier,
                new HolderExceptionHandler(),
                workHandlers
        );

        //设置多消费者的Sequence序号，主要用于统计消费进度，
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        this.workerPool = workerPool;
    }

    @Override
    public void add(E event) {
         if(ringBuffer == null){
             process(this.eventListener, new IllegalStateException("ParallelQueueHandler is close"),event);
         }
         try {
             ringBuffer.publishEvent(this.eventTranslator,event);
         }catch (NullPointerException e){
             process(this.eventListener, new IllegalStateException("ParallelQueueHandler is close"),event);
         }
    }

    @Override
    public void start() {
        workerPool.start(executorService);
    }

    @Override
    public void shutDown() {
        if(ringBuffer == null){
            return;
        }
        if(workerPool != null){
            workerPool.drainAndHalt();
        }
        if(executorService != null){
            executorService.shutdown();
        }
    }

    private static <E> void process(EventListener<E> listener,Throwable e,E event){
        listener.onException(e,-1,event);
    }

    public static class Builder<E>{

        private ProducerType producerType = ProducerType.MULTI;

        private int bufferSize = 1024 * 1024;

        private int threads = Runtime.getRuntime().availableProcessors();

        private String namePrefix = "";

        private WaitStrategy waitStrategy = new YieldingWaitStrategy();

        private EventListener<E> listener;

        public Builder<E> setProducerType(ProducerType producerType) {
            Preconditions.checkNotNull(producerType);
            this.producerType = producerType;
            return  this;
        }

        public Builder<E> setBufferSize(int bufferSize) {
            Preconditions.checkArgument(Integer.bitCount(bufferSize) == 1);
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<E> setThreads(int threads) {
            Preconditions.checkArgument(threads > 0);
            this.threads = threads;
            return this;
        }

        public Builder<E> setNamePrefix(String namePrefix) {
            Preconditions.checkNotNull(namePrefix);
            this.namePrefix = namePrefix;
            return this;
        }

        public Builder<E> setWaitStrategy(WaitStrategy waitStrategy) {
            Preconditions.checkNotNull(waitStrategy);
            this.waitStrategy = waitStrategy;
            return  this;
        }

        public Builder<E> setListener(EventListener<E> listener) {
            Preconditions.checkNotNull(listener);
            this.listener = listener;
            return this;
        }

        public ParallelQueueHandler<E> build(){
            return new ParallelQueueHandler<>(this);
        }
    }

    public class Holder{
        private E event;

        public void setValue(E event) {
            this.event = event;
        }
    }

    private class HolderExceptionHandler implements  ExceptionHandler<Holder>{

        @Override
        public void handleEventException(Throwable throwable, long l, Holder event) {
            Holder holder = event;
            try {
                eventListener.onException(throwable,l,holder.event);
            }catch (Exception e){

            }finally {
                holder.setValue(null);
            }

        }

        @Override
        public void handleOnStartException(Throwable throwable) {
           throw new UnsupportedOperationException(throwable);
        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }
    }

    private class HolderWorkHandler implements WorkHandler<Holder>{
        @Override
        public void onEvent(Holder holder) {
            eventListener.onEvent(holder.event);
            holder.setValue(null);
        }
    }

    private class HolderEventFactory implements EventFactory<Holder>{
        @Override
        public Holder newInstance() {
            return new Holder();
        }
    }

    private class HolderEventTranslator implements EventTranslatorOneArg<Holder,E>{
        @Override
        public void translateTo(Holder holder, long l, E e) {
            holder.setValue(e);
        }
    }
}
