/*
 * VwdPageSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.MSCPageDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PageUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdPageSnippet extends AbstractSnippet<VwdPageSnippet, VwdPageSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("VwdPage", I18n.I.vwdPage()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new VwdPageSnippet(context, config);
        }

        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("pageId", VwdPageController.getDefaultPageId()); // $NON-NLS$
            config.put("renderingProperties", VwdPageController.createRenderingProperties()[0]); // $NON-NLS-0$
        }
    }

    private DmxmlContext.Block<MSCPageDisplay> block;

    private VwdPageSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setView(new VwdPageSnippetView(this, config));

        this.block = createBlock("MSC_PageDisplay"); // $NON-NLS-0$
        this.block.setParameters("renderingProperties", VwdPageController.createRenderingProperties()); // $NON-NLS-0$
        this.block.setParameter("currency", config.getString("currency", null)); // $NON-NLS-0$ $NON-NLS-1$

        onParametersChanged();
    }

    public void configure(Widget triggerWidget) {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addConfigurePages();
        configView.show();
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            getView().showError();
            return;
        }
        final String text = PageUtil.toPageText(this.block.getResult());
        if (!StringUtil.hasText(text)) {
            getView().showEmpty();
            return;
        }

        getView().showPage(text);
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        this.block.setParameter("pageId", config.getString("pageId", "1")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }

    boolean useLocalLinks() {
        return "true".equals(getConfiguration().getString("localLinks", null)); // $NON-NLS-0$ $NON-NLS-1$
    }

    void loadPage(String s) {
        getConfiguration().put("pageId", s); // $NON-NLS-0$
        onParametersChanged();
        this.contextController.reload();
    }
}
