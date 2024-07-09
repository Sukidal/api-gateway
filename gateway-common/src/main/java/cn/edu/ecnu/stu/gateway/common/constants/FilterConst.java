package cn.edu.ecnu.stu.gateway.common.constants;

/**
 * 负载均衡常量类
 */
public interface FilterConst {

    String MONITOR_FILTER_ID = "monitor_filter";
    String MONITOR_FILTER_NAME = "monitor_filter";
    int MONITOR_FILTER_ORDER = -1;

    String MONITOR_END_FILTER_ID = "monitor_end_filter";
    String MONITOR_END_FILTER_NAME = "monitor_end_filter";
    int MONITOR_END_FILTER_ORDER = Integer.MAX_VALUE;

    String GRAY_FILTER_ID = "gray_filter";
    String GRAY_FILTER_NAME = "gray_filter";
    int GRAY_FILTER_ORDER = 0;

    String MOCK_FILTER_ID = "mock_filter";
    String MOCK_FILTER_NAME = "mock_filter";
    int MOCK_FILTER_ORDER = 0;

    String USER_AUTH_FILTER_ID = "user_auth_filter";
    String USER_AUTH_FILTER_NAME = "user_auth_filter";
    int USER_AUTH_FILTER_ORDER = 1;

    String LOAD_BALANCE_FILTER_ID = "load_balance_filter";
    String LOAD_BALANCE_FILTER_NAME = "load_balance_filter";
    int LOAD_BALANCE_FILTER_ORDER = Integer.MAX_VALUE - 1;

    String LOAD_BALANCE_KEY = "load_balance";
    String LOAD_BALANCE_STRATEGY_RANDOM = "Random";
    String LOAD_BALANCE_STRATEGY_ROUND_ROBIN = "RoundRobin";

    String ROUTER_FILTER_ID = "router_filter";
    String ROUTER_FILTER_NAME = "router_filter";
    int ROUTER_FILTER_ORDER = Integer.MAX_VALUE;

    String ADD_HEADER_FILTER_ID = "add_header_filter";
    String ADD_HEADER_FILTER_NAME = "add_header_filter";
    int ADD_HEADER_FILTER_ORDER = 100;

    String ADD_REQUEST_PARAM_FILTER_ID = "add_request_param_filter";
    String ADD_REQUEST_PARAM_FILTER_NAME = "add_request_param_filter";
    int ADD_REQUEST_PARAM_FILTER_ORDER = 100;

    String ADD_REQUEST_BODY_FILTER_ID = "add_request_body_filter";
    String ADD_REQUEST_BODY_FILTER_NAME = "add_request_body_filter";
    int ADD_REQUEST_BODY_FILTER_ORDER = 100;

    String ADD_COOKIE_FILTER_ID = "add_cookie_filter";
    String ADD_COOKIE_FILTER_NAME = "add_cookie_filter";
    int ADD_COOKIE_FILTER_ORDER = 100;

    String PATH_REWRITE_FILTER_ID = "path_rewrite_filter";
    String PATH_REWRITE_FILTER_NAME = "path_rewrite_filter";
    int PATH_REWRITE_FILTER_ORDER = 100;

    String FLOW_CTL_FILTER_ID = "flow_ctl_filter";
    String FLOW_CTL_FILTER_NAME = "flow_ctl_filter";
    int FLOW_CTL_FILTER_ORDER = 0;
}
