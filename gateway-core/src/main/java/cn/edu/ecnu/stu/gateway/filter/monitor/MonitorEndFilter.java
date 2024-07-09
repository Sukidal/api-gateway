package cn.edu.ecnu.stu.gateway.filter.monitor;

import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import com.alibaba.nacos.client.naming.utils.RandomUtils;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import cn.edu.ecnu.stu.gateway.ConfigLoader;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@FilterAspect(id = FilterConst.MONITOR_END_FILTER_ID,
        name = FilterConst.MONITOR_END_FILTER_NAME,
        order = FilterConst.MONITOR_END_FILTER_ORDER)
public class MonitorEndFilter implements Filter {
    //普罗米修斯的注册表
    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public MonitorEndFilter() {
        this.prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        try {
            //暴露接口来提供普罗米修斯指标数据拉取
            HttpServer server = HttpServer.create(new InetSocketAddress(ConfigLoader.getConfig().getPrometheusPort()), 0);
            server.createContext("/prometheus", exchange -> {
                //获取指标数据的文本内容
                String scrape = prometheusMeterRegistry.scrape();

                //指标数据返回
                exchange.sendResponseHeaders(200, scrape.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()){
                    os.write(scrape.getBytes());
                }
            });

            new Thread(server::start).start();

        } catch (IOException exception) {
            log.error("prometheus http server start error", exception);
            throw new RuntimeException(exception);
        }
        log.info("prometheus http server start successful, port:{}", ConfigLoader.getConfig().getPrometheusPort());

        //mock
        Executors.newScheduledThreadPool(1000).scheduleAtFixedRate(() -> {
            Timer.Sample sample = Timer.start();
            try {
                Thread.sleep(RandomUtils.nextInt(100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Timer timer = prometheusMeterRegistry.timer("gateway_request",
                    "uniqueId", "backend-http-server:1.0.0",
                    "protocol", "http",
                    "path", "/http-server/ping" + RandomUtils.nextInt(10));
            sample.stop(timer);
        },200, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        Timer timer = prometheusMeterRegistry.timer("gateway_request",
                "serviceId", ctx.getServiceId(),
                "path", ctx.getRequest().getPath());
        ctx.getTimerSample().stop(timer);
    }
}
