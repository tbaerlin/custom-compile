/*
 * SelectSymbolView.java
 *
 * Created on 11.08.2014 11:55
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.research;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DTRowGroup;
import de.marketmaker.iview.pmxml.ShellMMInfo;

/**
 * @author mdick
 */
public class SelectSymbolView extends AnalysisView<SelectSymbolDisplay.Presenter> implements SelectSymbolDisplay<SelectSymbolDisplay.Presenter> {
    private final EventListener rowSelectionListener = this::onRowClicked;

    private final DialogIfc dialog;
    private final SimpleLayoutPanel panel = new SimpleLayoutPanel();

    public SelectSymbolView() {
        setXlsExportButtonVisible(false);
        setChartPagingWidgetsVisible(false);
        setAggregationButtonVisible(false);
        setConfigButtonVisible(false);
        hideProgressPanelCancelButton();
        setProgressPanelBackgroundButtonVisible(false);
        setUpdatePrintButtonEnabled(false);

        this.panel.setStyleName("pm-analysisView");
        this.panel.add(super.asWidget());

        this.panel.setHeight("700px");  // $NON-NLS$
        this.panel.setWidth("1100px");  // $NON-NLS$

        this.dialog = createDialog();
    }

    @Override
    public void setPresenter(SelectSymbolDisplay.Presenter presenter) {
        super.setPresenter(presenter);
        this.tableWidget
                .withTableRendererSupplier(() -> new DTTableRenderer(tableWidget.getDTTable(),
                        tableWidget.getDTTableRendererOptions(), new RadioColumnRenderer()))
                .withClearTableBodyConsumers()
                .withTableBodyConsumer(this::initRowSelectionHandler);
    }

    private DialogIfc createDialog() {
        return Dialog.getImpl().createDialog()
                .withStyle("mm-noPadding")  // $NON-NLS$
                .withStyle("mm-noMaxWidth")  // $NON-NLS$
                .withWidget(this.panel)
                .withTitle(I18n.I.research())
                .withButton(I18n.I.ok(), () -> getPresenter().onOkClicked())
                .withButton(I18n.I.cancel(), () -> getPresenter().onCancelClicked())
                .withEscapeCommand(() -> getPresenter().onCancelClicked());
    }

    @Override
    public void setTitle(String title) {
        this.dialog.withTitle(title);
    }

    @Override
    public void show() {
        this.dialog.show();
    }

    @Override
    public void hide() {
        this.dialog.closePopup();
    }

    private void initRowSelectionHandler(Element tbody) {
        final NodeList<Element> trs = tbody.getElementsByTagName(TableRowElement.TAG);

        for (int i = 0; i < trs.getLength(); i++) {
            final Element tr = trs.getItem(i);

            DOM.setEventListener(tr, this.rowSelectionListener);
            DOM.sinkEvents(tr, Event.ONCLICK);
        }
    }

    private void onRowClicked(Event event) {
        switch (event.getTypeInt()) {
            case Event.ONCLICK:
                final Element e = Element.as(event.getEventTarget());
                final Element tr;

                if (TableCellElement.TAG_TD.equalsIgnoreCase(e.getTagName())) {
                    tr = Element.as(e.getParentNode());
                    final NodeList<Element> inputs = tr.getElementsByTagName("input"); // $NON-NLS$
                    if (inputs != null && inputs.getLength() > 0) {
                        final InputElement input = InputElement.as(inputs.getItem(0));
                        setRadioEnabled(input);
                        selectItem(input);
                    }
                } else if (InputElement.TAG.equalsIgnoreCase(e.getTagName())) {
                    selectItem(InputElement.as(e));
                    tr = Element.as(e.getParentNode().getParentNode());
                } else {
                    Firebug.debug("<SelectSymbolView.rowSelectionListener.onBrowserEvent> selection click was not on a td or input element! doing nothing...");
                    return;
                }

                this.tableWidget.getDTTableWidget().selectRow(tr);
        }
    }

    private void setRadioEnabled(InputElement input) {
        if ("radio".equalsIgnoreCase(input.getType())) {  // $NON-NLS$
            if (!input.isChecked()) {
                input.setChecked(true);
            }
        }
    }

    private void selectItem(InputElement input) {
        final String shellId = input.getValue();

        if (StringUtil.hasText(shellId)) {
            final DTRowGroup toplevelGroup = this.tableWidget.getDTTable().getToplevelGroup();
            //TODO: this findShellMMInfo call (recursive impl.) is a little bit slow on IE 11, 10 and 8
            //TODO: but not in IE 9!
            final ShellMMInfo selectedItem = DTTableUtils.findShellMMInfo(shellId, toplevelGroup);
            getPresenter().onItemSelected(selectedItem);
        } else {
            Firebug.debug("<SelectSymbolView.onRadioClicked> shellMMInfo-ID of selected row is null or empty!");
        }
    }

    @Override
    public Widget asWidget() {
        return this.panel;
    }
}
