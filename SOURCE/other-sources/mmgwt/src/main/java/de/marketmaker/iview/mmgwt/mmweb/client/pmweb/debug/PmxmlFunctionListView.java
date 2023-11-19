/*
 * PmxmlFunctionListView.java
 *
 * Created on 12.11.2012
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.debug;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * @author Markus Dick
 */
public class PmxmlFunctionListView implements PmxmlFunctionListPresenter.Display {
    interface Binder extends UiBinder<Panel, PmxmlFunctionListView> {}
    private static final Binder BINDER = GWT.create(Binder.class);

    @UiField
    Panel panel;

    @UiField(provided = true)
    SnippetTableWidget table;

    @UiField
    Button okButton;

    final DialogBox dialogBox;

    public PmxmlFunctionListView() {
        this.dialogBox = new DialogBox(true, true);
        this.dialogBox.setText("PM_FunctionList"); //$NON-NLS$
        this.dialogBox.setStylePrimaryName("mm-contentSnippet"); //$NON-NLS$

        TableColumnModel tcm = new DefaultTableColumnModel(new TableColumn[] {
                new TableColumn("Key", 20),  //$NON-NLS$
                new TableColumn("Request", 40), //$NON-NLS$
                new TableColumn("Response", 40) //$NON-NLS$
        });
        this.table = new SnippetTableWidget(tcm);

        this.dialogBox.setWidget(BINDER.createAndBindUi(this));

        //Hack! Dialog box should appear above all other widgets, especially GXT widgets.
        this.dialogBox.getElement().getStyle().setZIndex(10000);
    }

    @Override
    public void updateTableDataModel(TableDataModel model) {
        this.table.updateData(model);
    }

    @Override
    public DialogBox getDialogBox() {
        return this.dialogBox;
    }

    @Override
    public void addOkHandler(ClickHandler clickHandler) {
        this.okButton.addClickHandler(clickHandler);
    }
}
