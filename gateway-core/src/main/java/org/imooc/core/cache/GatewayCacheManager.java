package org.imooc.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 缓存管理类
 * @USER: WuYang
 * @DATE: 2023/2/27 23:19
 */
public class GatewayCacheManager {

    public GatewayCacheManager() {
    }

    /**
     * 全局缓存，双层缓存
     */
    private final ConcurrentHashMap<String,Cache<String,?>> cacheMap = new ConcurrentHashMap<>();


    private static class  SingletonInstance {
        private static final GatewayCacheManager INSTANCE = new GatewayCacheManager();
    }

    public static GatewayCacheManager getInstance(){
        return SingletonInstance.INSTANCE;
    }

   /**
     * 根据全局缓存ID创建一个Caffeine对象
     * @param cacheId
     * @return
     * @param <V>
     */
   public <V> Cache<String,V> create(String cacheId){
        Cache<String,V> cache = Caffeine.newBuilder().build();
        cacheMap.put(cacheId,cache);
        return (Cache<String,V>)cacheMap.get(cacheId);
    }

    /**
     * 根据CacheID以及对象Key删除对应地 Caffine对象
     * @param cacheId
     * @param key
     * @param <V>
     */
    public <V> void remove(String cacheId,String key){
        Cache<String,V> cache = (Cache<String,V>)cacheMap.get(cacheId);
        if(cache != null){
            cache.invalidate(key);
        }
    }

    /**
     * 根据CacheID 删除Caffine对象
     * @param cacheId
     * @param <V>
     */
    public <V> void remove(String cacheId){
        Cache<String,V> cache = (Cache<String,V>)cacheMap.get(cacheId);
        if(cache != null){
            cache.invalidateAll();
        }
    }

    /**
     *  清除所有缓存
     * @param <V>
     */
    public <V> void removeAll(){
        cacheMap.values().forEach(cache -> cache.invalidateAll());
    }

}
