/*
 * AbstractPageController.java
 *
 * Created on 24.03.2008 09:41:55
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractPageController extends ResponseTypeCallback implements
        PageController {
    private final ContentContainer contentContainer;

    protected final SessionData sessionData = Ginjector.INSTANCE.getSessionData();

    protected final DmxmlContext context;

    protected AbstractPageController() {
        this(null, new DmxmlContext());
    }

    protected AbstractPageController(DmxmlContext context) {
        this(null, context);
    }

    protected AbstractPageController(ContentContainer contentContainer) {
        this(contentContainer, new DmxmlContext());
    }

    protected AbstractPageController(ContentContainer contentContainer, DmxmlContext context) {
        this.contentContainer = contentContainer;
        this.context = context;
    }

    public ContentContainer getContentContainer() {
        return this.contentContainer != null ? this.contentContainer : AbstractMainController.INSTANCE.getView();
    }

    /**
     * Whenever this object is used as an AsyncCallback, this method will be invoked by the
     * default implementation of {@link #onFailure(Throwable)} and
     * {@link #onSuccess(de.marketmaker.iview.dmxml.ResponseType)}. Subclasses can override
     * this method to handle the response as they wish.
     */
    @Override
    protected void onResult() {
    }

    @Override
    public void activate() {
        // empty, subclasses can override
    }

    @Override
    public void deactivate() {
        // empty, subclasses can override
    }

    @Override
    public void destroy() {
    }

    @Override
    public void refresh() {
        this.context.issueRequest(this);
    }

    @Override
    public boolean supportsHistory() {
        return true;
    }

    @Override
    public String getPrintHtml() {
        // use getter method to access contentContainer as subclasses may override it
        return getContentContainer().getContent().getElement().getInnerHTML();
    }

    /**
     * @return {@literal false} as default, due to backward compatibility with MainController implementations.
     */
    @Override
    public boolean isPrintable() {
        return true;
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    @Override
    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return null;
    }

    @Override
    public void addPdfPageParameters(Map<String, String> mapParameters) {
        // empty, subclasses can override
    }
}
