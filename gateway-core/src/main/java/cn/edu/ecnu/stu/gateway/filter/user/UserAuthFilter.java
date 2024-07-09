package cn.edu.ecnu.stu.gateway.filter.user;

import cn.edu.ecnu.stu.gateway.common.constants.FilterConst;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import cn.edu.ecnu.stu.gateway.common.enums.ResponseCode;
import cn.edu.ecnu.stu.gateway.common.exception.ResponseException;
import cn.edu.ecnu.stu.gateway.Config;
import cn.edu.ecnu.stu.gateway.context.GatewayContext;
import cn.edu.ecnu.stu.gateway.filter.Filter;
import cn.edu.ecnu.stu.gateway.filter.FilterAspect;

@Slf4j
@FilterAspect(id = FilterConst.USER_AUTH_FILTER_ID,
        name = FilterConst.USER_AUTH_FILTER_NAME,
        order = FilterConst.USER_AUTH_FILTER_ORDER )
public class UserAuthFilter implements Filter {
    private final String secretKey;
    private final String securityKeyName;

    public UserAuthFilter() {
        Config config = Config.getInstance();
        secretKey = config.getSecretKey();
        securityKeyName = config.getSecurityKeyName();
    }

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        //检查是否需要用户鉴权
        if (ctx.getRule().getFilterConfig(FilterConst.USER_AUTH_FILTER_ID) == null) {
            return;
        }

        String token = ctx.getRequest().getCookie(securityKeyName).value();
        if (StringUtils.isBlank(token)) {
            token = ctx.getRequest().getHeaders().get(securityKeyName);
        }
        if (StringUtils.isBlank(token)) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }

        try {
            //解析用户id
            long userId = parseUserId(token);
            //把用户id传给下游
            ctx.getRequest().setUserId(userId);
        } catch (Exception e) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }

    }

    private long parseUserId(String token) {
        Jwt jwt = Jwts.parser().setSigningKey(secretKey).parse(token);
        return Long.parseLong(((DefaultClaims)jwt.getBody()).getSubject());
    }
}
