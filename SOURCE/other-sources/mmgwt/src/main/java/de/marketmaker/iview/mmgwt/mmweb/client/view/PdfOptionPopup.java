package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.*;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

import java.util.Map;

/**
 * @author umaurer
 */
public class PdfOptionPopup implements PdfOptionView {
    private static final String DEFAULT_HREF = "javascript:;"; //$NON-NLS$

    private static final PdfOptionPopup INSTANCE = new PdfOptionPopup();

    private final PopupPanel popupPanel = new PopupPanel(true);
    private final FlexTable table = new FlexTable();
    private final FlexTable.FlexCellFormatter cellFormatter = this.table.getFlexCellFormatter();
    private Anchor anchor = null;
    private boolean useHttpPost = false;

    private PdfOptionHelper pdfOptionHelper = null;

    public PdfOptionPopup() {
        final FlowPanel panel = new FlowPanel();
        panel.add(this.table);

        this.popupPanel.setWidget(panel);
        this.popupPanel.setStyleName("x-menu"); // $NON-NLS-0$
    }

    public static void showRelativeTo(Widget widget, PdfOptionSpec spec, Map<String, String> mapPageParameters) {
        INSTANCE._showRelativeTo(widget, spec, mapPageParameters);
    }

    private void _showRelativeTo(Widget widget, PdfOptionSpec spec, Map<String, String> mapPageParameters) {
        if (this.pdfOptionHelper == null || !this.pdfOptionHelper.isFor(spec)) {
            this.table.removeAllRows();
            this.pdfOptionHelper = new PdfOptionHelper(this, spec, mapPageParameters);
        }
        else {
            this.pdfOptionHelper.setMapPageParameters(mapPageParameters);
        }
        this.popupPanel.showRelativeTo(widget);
    }

    @Override
    public void addOption(final String id, final String title, final boolean checked, final String style) {
        final int row = this.table.getRowCount();
        final CheckBox checkBox = new CheckBox(title);
        this.table.setWidget(row, 0, checkBox);
        this.cellFormatter.setStyleName(row, 0, style);
        checkBox.setValue(checked);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                pdfOptionHelper.setOption(id, booleanValueChangeEvent.getValue());
            }
        });
//        this.mapCheckBoxes.put(id, checkBox);

    }

    @Override
    public void addOption(final String id, final String title, final String[] values, final boolean checked, final String style) {
        assert values.length == 2;
        final int row = this.table.getRowCount();
        final CheckBox checkBox = new CheckBox(title);
        this.table.setWidget(row, 0, checkBox);
        this.cellFormatter.setStyleName(row, 0, style);
        checkBox.setValue(checked);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                pdfOptionHelper.setOption(id, booleanValueChangeEvent.getValue() ? values[0] : values[1]);
            }
        });
//        this.mapCheckBoxes.put(id, checkBox);

    }

    @Override
    public void addLink(String link, String style, boolean httpPost) {
        this.anchor = new Anchor(I18n.I.openPdfDocument(), false);
        addAnchor(this.anchor, style);

        updateLink(link, httpPost);

        this.anchor.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                ActionPerformedEvent.fire("X_PDF"); // $NON-NLS-0$
                if(PdfOptionPopup.this.useHttpPost) {

                    PhoneGapUtil.log("open PDF by post"); // $NON-NLS-0$

                    PdfOptionPopup.this.pdfOptionHelper.openPdfByPost();
                }
                PdfOptionPopup.this.popupPanel.hide();
            }
        });
    }

    @Override
    public void updateLink(String link, boolean httpPost) {
        if (this.anchor == null) {
            Firebug.log("Fehler: PdfOptionSnippetView.anchor not initialized"); // $NON-NLS-0$
            DebugUtil.logToServer("Fehler: PdfOptionSnippetView.anchor not initialized"); // $NON-NLS-0$
            return;
        }

        this.useHttpPost = httpPost;
        if(httpPost) {
            this.anchor.setHref(DEFAULT_HREF);
            this.anchor.setTarget(""); //$NON-NLS$
            return;
        }

// todo: JS - implement
//        if (PhoneGapUtil.isPhoneGap()) {
//            link = UrlBuilder.getServerPrefix(true) + "/" + link;
//            link = PhoneGapUtil.getPdfViewerUrl(link);
//            PhoneGapUtil.log("PdfOptionPopup - PDF Link: " + link);  //$NON-NLS$
//        }

        this.anchor.setHref(link);
        this.anchor.setTarget("_blank"); //$NON-NLS$
    }

    private void addAnchor(Anchor anchor, String style) {
        int row = this.table.getRowCount();
        this.table.setHTML(row, 0, "&nbsp;"); // $NON-NLS-0$
        row++;
        this.table.setWidget(row, 0, anchor);
        if (style != null) {
            this.cellFormatter.setStyleName(row, 0, style);
        }
    }
}
