package cn.edu.ecnu.stu.gateway.filter.path;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;

import java.util.Map;
import java.util.regex.Pattern;

@FilterAspect(
        id = FilterConst.PATH_REWRITE_FILTER_ID,
        name = FilterConst.PATH_REWRITE_FILTER_NAME,
        order = FilterConst.PATH_REWRITE_FILTER_ORDER
)
@Slf4j
public class PathRewriteFilter implements Filter {

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        String config = ctx.getRule().getFilterConfig(FilterConst.PATH_REWRITE_FILTER_ID).getConfig();
        Map<String, String> configMap = JSON.parseObject(config, Map.class);
        String originPath = configMap.get("originPath");
        String regex = configMap.get("newPath");
        if(StringUtils.isEmpty(originPath) || StringUtils.isEmpty(regex)) {
            log.error("path can not be empty");
            throw new RuntimeException("path can not be empty");
        }
        String replacement = regex.replace("$\\", "$");
        Pattern pattern = Pattern.compile(originPath);
        String newPath = pattern.matcher(ctx.getRequest().getModifyPath()).replaceAll(replacement);
        ctx.getRequest().setModifyPath(newPath);
    }
}
