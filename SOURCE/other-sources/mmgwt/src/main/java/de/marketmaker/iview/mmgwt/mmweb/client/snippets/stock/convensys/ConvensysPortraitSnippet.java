/*
 * ConvensysPortraitSnippet.java
 *
 * Created on 11.09.2008 17:04:30
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.convensys;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.Event;

import de.marketmaker.iview.dmxml.StringResult;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTextView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import static com.google.gwt.query.client.GQuery.*;
import static de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature.DZ_RELEASE_2016;
import static de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature.VWD_RELEASE_2015;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author mdick
 */
public class ConvensysPortraitSnippet
        extends AbstractSnippet<ConvensysPortraitSnippet, SnippetTextView<ConvensysPortraitSnippet>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("ConvensysPortrait"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ConvensysPortraitSnippet(context, config, Ginjector.INSTANCE.getFeatureFlags());
        }
    }

    private static final String VWD_EXPANDED_ATTRIBUTE = "vwd:expanded"; // $NON-NLS$

    private final FeatureFlags featureFlags;

    private DmxmlContext.Block<StringResult> block;

    private ConvensysPortraitSnippet(DmxmlContext context, SnippetConfiguration config,
            FeatureFlags featureFlags) {
        super(context, config);
        this.featureFlags = featureFlags;
        this.block = createBlock("STK_ConvensysData"); // $NON-NLS$
        final String contentKey = (I18n.I.locale().equals("de")) ? "dzbank/profiles" : "dzbank/profiles-en"; // $NON-NLS$
        this.block.setParameter("contentKey", contentKey); // $NON-NLS$

        this.setView(new SnippetTextView<ConvensysPortraitSnippet>(this) {
            protected void onContainerAvailable() {
                super.onContainerAvailable();
                this.container.asWidget().addStyleName("mm-snippet-convensys"); // $NON-NLS$
            }
        });
        getView().setStyleName("mm-convensys"); // $NON-NLS$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name,
            String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS$
    }


    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            setNoContent();
            return;
        }
        final String html = this.block.getResult().getResult();
        if (html != null) {
            getView().setHtml(html);
            attachToggleParticipationTrigger();
        }
        else {
            setNoContent();
        }
    }

    private void setNoContent() {
        getView().setText(I18n.I.noData());
    }

    private void attachToggleParticipationTrigger() {
        final JavaScriptObject list = DOMUtil.getElementsByTagNameImpl(getView().getWidget().getElement(), "a"); // $NON-NLS$
        final int n = DOMUtil.getLength(list);
        for (int i = 0; i < n; i++) {
            final Element anchor = DOMUtil.getElementFromList(i, list);
            final String aFlipId = anchor.getAttribute("mm:flipId"); // $NON-NLS$
            if ("convensys-participations-expand".equals(aFlipId)) { // $NON-NLS$
                if (this.featureFlags.isEnabled0(DZ_RELEASE_2016) || this.featureFlags.isEnabled0(VWD_RELEASE_2015)) {
                    attachToggleParticipationTrigger(anchor, getView().getWidget().getElement());
                }
                else {
                    //noinspection deprecation
                    attachExpandParticipationTrigger(anchor, getView().getWidget().getElement());
                }
            }
            else if ("convensys-quarters-toggle".equals(aFlipId)) { // $NON-NLS$
                attachToggleQuartersTrigger(anchor);
            }
        }
    }

    private void attachToggleParticipationTrigger(final Element anchor, final Element parent) {
        setRowsVisible(parent, false);
        $(anchor).click(new Function() {
            @Override
            public boolean f(Event e) {
                toggleParticipationsRows(anchor, parent);
                return false;
            }
        });
    }

    private static void toggleParticipationsRows(final Element anchor, final Element parent) {
        if (VWD_EXPANDED_ATTRIBUTE.equalsIgnoreCase(anchor.getAttribute(VWD_EXPANDED_ATTRIBUTE))) {
            // is expanded, so collapse on click; if clicked again, should expand.
            anchor.removeAttribute(VWD_EXPANDED_ATTRIBUTE);
            setRowsVisible(parent, false);
            DOMUtil.setTableRowVisible(anchor.getParentElement().getParentElement(), true);
            anchor.setInnerText(I18n.I.more());
        }
        else {
            // is collapsed, so expand on click; if clicked again, should collapse.
            anchor.setAttribute(VWD_EXPANDED_ATTRIBUTE, VWD_EXPANDED_ATTRIBUTE);
            setRowsVisible(parent, true);
            DOMUtil.setTableRowVisible(anchor.getParentElement().getParentElement(), true);
            anchor.setInnerText(I18n.I.less());
        }
    }

    private static void attachToggleQuartersTrigger(final Element anchor) {
        $(anchor).click(new Function() {
            @Override
            public boolean f(Event e) {
                toggleQuarters(anchor);
                return false;
            }
        });
    }

    private static void attachExpandParticipationTrigger(final Element anchor, final Element parent) {
        setRowsVisible(parent, false);
        $(anchor).click(new Function() {
            @Override
            public boolean f(Event e) {
                setRowsVisible(parent, true);
                return false;
            }
        });
    }

    private static void toggleQuarters(final Element anchor) {
        com.google.gwt.dom.client.Element tdElement = anchor.getParentElement();
        com.google.gwt.dom.client.Element trElement = tdElement.getParentElement();
        // next 3 rows should be the quarters
        com.google.gwt.dom.client.Element sibling1 = trElement.getNextSiblingElement();
        com.google.gwt.dom.client.Element sibling2 = sibling1.getNextSiblingElement();
        com.google.gwt.dom.client.Element sibling3 = sibling2.getNextSiblingElement();

        boolean isVisible = DOMUtil.isTableRowVisible(sibling1);
        anchor.setInnerHTML(isVisible ? "[+]" : "[-]");

        // bail out as soon as we hit a wrong row
        if (!"true".equals(sibling1.getAttribute("mm:isQuarterRow"))) { // $NON-NLS-0$ $NON-NLS-1$
            return;
        }
        DOMUtil.setTableRowVisible(sibling1, !isVisible);

        if (!"true".equals(sibling2.getAttribute("mm:isQuarterRow"))) { // $NON-NLS-0$ $NON-NLS-1$
            return;
        }
        DOMUtil.setTableRowVisible(sibling2, !isVisible);

        if (!"true".equals(sibling3.getAttribute("mm:isQuarterRow"))) { // $NON-NLS-0$ $NON-NLS-1$
            return;
        }
        DOMUtil.setTableRowVisible(sibling3, !isVisible);
    }

    public static void setRowsVisible(final Element parent, boolean visible) {
        final NodeList<Element> trElements = parent.getElementsByTagName("tr"); // $NON-NLS$
        final int rowCount = trElements.getLength();
        for (int row = 0; row < rowCount; row++) {
            final Element trElement = trElements.getItem(row);
            final String flipId = trElement.getAttribute("mm:flipId"); // $NON-NLS$
            if ("convensys-participations".equals(flipId)) { // $NON-NLS$
                DOMUtil.setTableRowVisible(trElement, visible);
            }
            else if ("convensys-participations-expand".equals(flipId)) { // $NON-NLS$
                DOMUtil.setTableRowVisible(trElement, !visible);
            }
        }
    }
}
