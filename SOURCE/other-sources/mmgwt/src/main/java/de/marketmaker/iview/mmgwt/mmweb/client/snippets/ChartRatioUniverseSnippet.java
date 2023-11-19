/*
 * ChartRatioUniverseSnippet.java
 *
 * Created on 14.05.2008 15:12:29
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.IMGRatioUniverse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.DZ_BANK_USER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ChartRatioUniverseSnippet extends AbstractSnippet<ChartRatioUniverseSnippet, ChartRatioUniverseSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("ChartRatioUniverse"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ChartRatioUniverseSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 2); // $NON-NLS$
        }
    }

    protected DmxmlContext.Block<IMGRatioUniverse> block;

    public ChartRatioUniverseSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("IMG_RatioUniverse"); // $NON-NLS$

        final String instrumentType = config.getString("type"); // $NON-NLS$
        final String field = config.getString("field");  // $NON-NLS$
        this.block.setParameter("type", instrumentType); // $NON-NLS$
        this.block.setParameter("field", field); // $NON-NLS$
        this.block.setParameter("query", getQuery()); // $NON-NLS$
        this.block.setParameter("minCount", config.getString("minCount")); // $NON-NLS$
        this.block.setParameter("width", config.getString("width")); // $NON-NLS$
        this.block.setParameter("height", config.getString("height")); // $NON-NLS$
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$

        this.setView(new ChartRatioUniverseSnippetView(this, instrumentType, field));
    }

    private String getQuery() {
        StringBuilder sb = new StringBuilder();
        appendToQuery(sb, "issuername", getGuiDefList("issuerlist")); // $NON-NLS-0$ $NON-NLS-1$
        appendToQuery(sb, "market", getGuiDefList("marketList")); // $NON-NLS-0$ $NON-NLS-1$
        if (DZ_BANK_USER.isAllowed()) {
            sb.append("&&dzIsLeverageProduct=='false'"); // $NON-NLS-0$
        }
        return sb.toString();
    }

    private void appendToQuery(StringBuilder sb, String queryKey, String[] queryValues) {
        if (queryValues == null || queryValues.length == 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("&&"); // $NON-NLS$
        }
        sb.append(queryKey).append("=='"); // $NON-NLS-0$
        for (int i = 0; i < queryValues.length; i++) {
            if (i > 0) {
                sb.append("@"); // $NON-NLS$
            }
            sb.append(queryValues[i]);
        }
        sb.append("'"); // $NON-NLS$
    }

    protected String[] getGuiDefList(String listName) {
        final String[] issuerList = getConfiguration().getArray(listName);
        if (issuerList == null) {
            Firebug.log(getClass().getSimpleName() + " <getGuiDefList> returns null for " + listName);
        }
        return issuerList;
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            final ErrorType et = this.block.getError();
            if (et == null) {
                getView().update(this.block.toString(), false);
            } else {
                getView().update(et.getCode() + ": " + et.getDescription() + " / " + this.block.toString(), false); // $NON-NLS$
            }
            return;
        }
        final IMGRatioUniverse ipr = this.block.getResult();
        getView().update(ipr);
    }
}
