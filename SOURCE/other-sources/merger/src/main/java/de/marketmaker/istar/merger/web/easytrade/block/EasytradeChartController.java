/*
 * EasytradeChartController.java
 *
 * Created on 18.09.2006 16:44:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.merger.web.easytrade.chart.BaseImgCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class EasytradeChartController extends EasytradeCommandController {
    private Map<String, String> layoutMappings = new HashMap<>();

    private String defaultMapping;

    protected EasytradeChartController(Class<? extends BaseImgCommand> cmdClass,
            String defaultMapping) {
        super(cmdClass);
        this.defaultMapping = defaultMapping;
    }

    public void setDefaultMapping(String defaultMapping) {
        this.defaultMapping = defaultMapping;
    }

    public String getDefaultMapping() {
        return defaultMapping;
    }

    public void setLayoutMappings(Map<String, String> layoutMappings) {
        this.layoutMappings = layoutMappings;
    }

    @Override
    protected void onBind(HttpServletRequest request, Object command) throws Exception {
        super.onBind(request, command);
        ((BaseImgCommand) command).setRequest(request);
    }

    protected String getMappedChartName(final BaseImgCommand cmd) {
        if (StringUtils.hasText(cmd.getChartName())) {
            return cmd.getChartName();
        }
        if (!StringUtils.hasText(cmd.getLayout())) {
            return this.defaultMapping;
        }
        final String mappedChartName = doGetMappedChartName(cmd);
        return mappedChartName != null ? mappedChartName : this.defaultMapping;
    }

    @SuppressWarnings({"unchecked"})
    private String doGetMappedChartName(final BaseImgCommand cmd) {
        final String layout = cmd.getLayout();

        final Map<String, String> atomLayoutMappings
                = (Map<String, String>) cmd.getContextMap().get("layouts");
        if (atomLayoutMappings != null && atomLayoutMappings.containsKey(layout)) {
            return atomLayoutMappings.get(layout);
        }

        return this.layoutMappings.get(layout);
    }

    private String toRequest(BaseImgCommand cmd) {
        final StringBuilder sb = new StringBuilder(100);
        if (StringUtils.hasText(cmd.getBaseUrl())) {
            sb.append(cmd.getBaseUrl());
            if (sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }
        }
        sb.append(getMappedChartName(cmd)).append("?");
        return cmd.appendParameters(sb).toString();
    }

    protected Map<String, Object> getDefaultModel(BaseImgCommand cmd) {
        final Map<String, Object> result = new HashMap<>();
        addRequestToModel(cmd, result);
        return result;
    }

    protected void addRequestToModel(BaseImgCommand cmd, Map<String, Object> model) {
        model.put("request", toRequest(cmd));
    }
}
