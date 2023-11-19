/*
 * SpsPibKiidAvailabilityIconWidget.java
 *
 * Created on 17.12.2014 09:33
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.function.Supplier;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.DmxmlContextFacade;

/**
 * @author Markus Dick
 */
public class SpsPibKiidAvailabilityIconWidget extends SpsPibKiidAvailabilityWidget {
    private static final SafeHtml EMPTY_ICON_SAFE_HTML = IconImage.get("empty-24").getSafeHtml();  // $NON-NLS$
    private static final SafeHtml CHECK_UNDETERMINED_ICON_SAFE_HTML = IconImage.get("PmIcon:CheckUndetermined").getSafeHtml();  // $NON-NLS$
    private static final SafeHtml RELOAD_ICON_SAFE_HTML = IconImage.get("as-reload-24 active").getSafeHtml();  // $NON-NLS$
    private static final SafeHtml CHECK_NOT_OK_ICON_SAFE_HTML = IconImage.get("PmIcon:CheckNotOk").getSafeHtml();  // $NON-NLS$
    private static final SafeHtml CHECK_OK_SAFE_HTML = IconImage.get("PmIcon:CheckOk").getSafeHtml();  // $NON-NLS$

    public SpsPibKiidAvailabilityIconWidget(Supplier<DmxmlContextFacade> dmxmlContextSupplier) {
        super(dmxmlContextSupplier);

        this.kidLabel.setHTML(CHECK_OK_SAFE_HTML);
        this.pibLabel.setHTML(CHECK_OK_SAFE_HTML);
        this.kiidLabel.setHTML(CHECK_OK_SAFE_HTML);

        updateWidgets(Selector.AS_DOCMAN.isAllowed() ? State.NOT_REQUESTED : State.NOT_ALLOWED);
    }

    @Override
    protected void showStatus(String text, String style, State state) {
        if(state == null) {
            this.statusLabel.setHTML(CHECK_UNDETERMINED_ICON_SAFE_HTML);
            return;
        }
        switch(state) {
            case NOT_AVAILABLE:
                this.statusLabel.setHTML(CHECK_NOT_OK_ICON_SAFE_HTML);
                break;
            case LOADING:
                this.statusLabel.setHTML(RELOAD_ICON_SAFE_HTML);
                break;
            case ERROR:
                this.statusLabel.setHTML(EMPTY_ICON_SAFE_HTML);
                break;
            default:
                this.statusLabel.setHTML(CHECK_UNDETERMINED_ICON_SAFE_HTML);
        }
        this.statusLabel.setVisible(true);
        hideDocTypeLabel();
    }
}
