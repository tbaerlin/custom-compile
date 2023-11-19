/*
 * KurseDde.java
 *
 * Created on 27.10.2008 10:12:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.webxl;

import de.marketmaker.istar.common.util.XmlUtil;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.DataBinderUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WelcheDepotsDde extends AbstractDde {
    private static final DataBinderUtils.Mapping MAPPING =
            new DataBinderUtils.Mapping().add("Xun", "xun");

    protected DataBinderUtils.Mapping getParameterMapping() {
        return MAPPING;
    }

    public WelcheDepotsDde() {
        super(Command.class);
    }

    protected String getContent(Object o) {
        final Command cmd = (Command) o;

        final UserContext userContext = getUserContext(getUserCommand());
        final User user = userContext.getUser();

        final StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<depots>");
        int count = 1;
        for (final Portfolio portfolio : user.getWatchlists()) {
            add(sb, count, portfolio);
            count++;
        }
        for (final Portfolio portfolio : user.getPortfolios()) {
            add(sb, count, portfolio);
            count++;
        }
        sb.append("</depots>");

        return sb.toString();
    }

    private void add(StringBuilder sb, int count, Portfolio portfolio) {
        sb.append("<depot_").append(count).append(">")
                .append("<Depotnummer>").append(portfolio.getId()).append("</Depotnummer>")
                .append("<Depotname>").append(XmlUtil.encode(portfolio.getName())).append("</Depotname>")
                .append("</depot_").append(count).append(">");
    }
}