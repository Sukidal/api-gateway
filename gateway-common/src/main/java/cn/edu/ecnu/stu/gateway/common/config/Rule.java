package cn.edu.ecnu.stu.gateway.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Data
public class Rule implements Serializable {

    /**
     * 规则ID，全局唯一
     */
    private String id;

    /**
     * 后端服务ID
     */
    private String serviceId;

    /**
     * 请求正则表达式
     */
    private String urlPattern;

    /**
     * 路径
     */
    private String url;

    private List<FilterConfig> filterConfigs = new ArrayList<>();

    private ConcurrentHashMap<String, FilterConfig> filterConfigMap = new ConcurrentHashMap<>();

    private List<FlowCtlConfig> flowCtlConfigs = new ArrayList<>();

    public Rule() {}

    @Data
    public static class FilterConfig{
        /**
         * 过滤器唯一ID
         */
        private String id;
        /**
         * 过滤器规则描述，{"timeOut":500,"balance":random}
         */
        private String config;

        @Override
        public boolean equals(Object o){
            if (this == o) return  true;

            if((o== null) || getClass() != o.getClass()){
                return false;
            }

            FilterConfig that =(FilterConfig)o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode(){
            return Objects.hash(id);
        }
    }

    /**
    * 通过一个指定的FilterID获取FilterConfig
    * @param id
    * @return
    */
    public FilterConfig getFilterConfig(String id){
        return filterConfigMap.get(id);
    }

    public void initFilterConfigMap() {
        for (FilterConfig filterConfig : filterConfigs) {
            filterConfigMap.put(filterConfig.id, filterConfig);
        }
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return  true;

        if((o== null) || getClass() != o.getClass()){
            return false;
        }

        FilterConfig that =(FilterConfig)o;
        return id.equals(that.id);
    }

    @Override
    public  int hashCode(){
        return Objects.hash(id);
    }
}
