package cn.edu.ecnu.stu.gateway.config.center.api;

import cn.edu.ecnu.stu.gateway.common.config.Rule;

import java.util.List;

public interface RulesChangeListener {
    void onRulesChange(List<Rule> rules);
}
