package org.imooc.gateway.config.center.api;

import org.imooc.common.config.Rule;

import java.util.List;

public interface RulesChangeListener {
    void onRulesChange(List<Rule> rules);
}
