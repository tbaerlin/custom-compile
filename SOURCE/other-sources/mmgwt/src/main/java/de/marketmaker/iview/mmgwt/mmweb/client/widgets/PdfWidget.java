/*
 * PdfWidget.java
 *
 * Created on 1/26/15 3:56 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceQueryEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PdfOptionPopup;

/**
 * @author kmilyut
 */
public class PdfWidget extends Composite {

    public enum PdfButtonState {
        DISABLED, ENABLED, WITH_OPTIONS
    }

    private ImageButton button;

    public PdfWidget(ImageButton button) {
        this.button = button;
        this.button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPdf();
            }
        });

        initWidget(button);
    }

    public void setPdfButtonState(PdfButtonState pdfButtonState) {
        switch (pdfButtonState) {
            case DISABLED:
                this.button.setEnabled(false);
                break;
            case ENABLED:
                this.button.setEnabled(true);
                this.button.setActive(false);
                break;
            case WITH_OPTIONS:
                this.button.setEnabled(true);
                this.button.setActive(true);
                break;
        }
    }

    public void openPdf() {
        final PdfOptionSpec spec = AbstractMainController.INSTANCE.getPdfOptionSpec();
        if (spec != null) {
            final Map<String, String> mapPageParameters = new HashMap<>();
            AbstractMainController.INSTANCE.addPdfPageParameters(mapPageParameters);
            if (spec.isWithOptions()) {
                PdfOptionPopup.showRelativeTo(button, spec, mapPageParameters);
            }
            else {
                String uri = PdfOptionHelper.getPdfUri(spec, mapPageParameters);
                Firebug.log("open pdf: " + uri); // $NON-NLS-0$
                ActionPerformedEvent.fire("X_PDF/" + PlaceQueryEvent.getCurrentPlace()); // $NON-NLS-0$
                Window.open(uri, "_blank", ""); // $NON-NLS-0$ $NON-NLS-1$
            }
        }
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }
}
