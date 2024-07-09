package cn.edu.ecnu.stu.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import cn.edu.ecnu.stu.gateway.common.config.Rule;
import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GatewayFilterChainFactory  implements FilterFactory {

    private static class SingletonInstance{
        private static final GatewayFilterChainFactory INSTANCE = new GatewayFilterChainFactory();
    }

    public static GatewayFilterChainFactory getInstance(){
        return SingletonInstance.INSTANCE;
    }

    private final Cache<String,GatewayFilterChain> chainCache = Caffeine.newBuilder().recordStats().expireAfterWrite(10, TimeUnit.MINUTES).build();

    private Map<String,Filter> filterMap = new ConcurrentHashMap<>();

    public GatewayFilterChainFactory() {
        ServiceLoader<Filter>  serviceLoader = ServiceLoader.load(Filter.class);
        serviceLoader.stream().forEach(filterProvider -> {
            Filter filter = filterProvider.get();
            FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
            log.info("load filter success:{},{},{},{}",filter.getClass(),
                    annotation.id(),annotation.name(),annotation.order());
            if(annotation != null){
                //添加到过滤集合
                String filterId = annotation.id();
                if(StringUtils.isEmpty(filterId)){
                    filterId = filter.getClass().getName();
                }
                filterMap.put(filterId,filter);
            }
        });

    }

    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception {
        GatewayFilterChain chain = chainCache.getIfPresent(ctx.getRule().getId());
        if(chain != null)
            return chain;
        Rule rule = ctx.getRule();
        chain = new GatewayFilterChain();
        List<Filter> filters = new ArrayList<>();
        Collection<Rule.FilterConfig> filterConfigs = rule.getFilterConfigs();
        boolean canCached = true;
        for (Rule.FilterConfig filterConfig : filterConfigs) {
            if(filterConfig == null){
                continue;
            }
            String filterId = filterConfig.getId();
            if(StringUtils.isEmpty(filterId)) {
                log.error("filter id must not empty");
                throw new RuntimeException("filter id must not empty");
            }
            if(FilterConst.ROUTER_FILTER_ID.equals(filterId) || FilterConst.LOAD_BALANCE_FILTER_ID.equals(filterId) ||
                FilterConst.FLOW_CTL_FILTER_ID.equals(filterId))
                continue;
            Filter filter = getFilter(filterId);
            if(filter == null) {
                try {
                    Class<?> clazz = Class.forName(filterId);
                    Constructor<?> constructor = clazz.getDeclaredConstructor(null);
                    Object o = constructor.newInstance();
                    if(!(o instanceof Filter)) {
                        log.error("{} not a filter", filterId);
                        throw new RuntimeException(filterId + " not a filter");
                    }
                    filter = (Filter) o;
                    FilterAspect filterAspect = clazz.getAnnotation(FilterAspect.class);
                    if(filterAspect == null || !filterAspect.prototype())
                        putFilter(filterId, filter);
                } catch (ClassNotFoundException e) {
                    log.error("filter {} not found", filterId);
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    log.error("filter {} doesn't have no arg constructor", filterId);
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            if(filter.isPrototype())
                canCached = false;
            filters.add(filter);
        }
        //todo 添加路由过滤器-这是最后一步
        filters.add(getFilter(FilterConst.FLOW_CTL_FILTER_ID));
        filters.add(getFilter(FilterConst.LOAD_BALANCE_FILTER_ID));
        filters.add(getFilter(FilterConst.ROUTER_FILTER_ID));
        //排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));
        //添加到链表中
        chain.addFilterList(filters);
        if(canCached)
            chainCache.put(ctx.getRule().getId(), chain);
        return chain;
    }

    @Override
    public Filter getFilter(String filterId){
        return filterMap.get(filterId);
    }

    public void putFilter(String filterId, Filter filter) {
        filterMap.put(filterId, filter);
    }
}
