/*
 * PmxmlFunctionListPresenter.java
 *
 * Created on 12.11.2012
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.debug;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModelBuilder;
import de.marketmaker.iview.pmxml.FunctionDesc;
import de.marketmaker.iview.pmxml.FunctionListResponse;
import de.marketmaker.iview.pmxml.VoidRequest;

import java.util.List;

/**
 * @author Markus Dick
 */
public class PmxmlFunctionListPresenter {
    interface Display {
        void updateTableDataModel(TableDataModel model);
        DialogBox getDialogBox();
        void addOkHandler(ClickHandler clickHandler);
    }

    private final DmxmlContext context;
    private final DmxmlContext.Block<FunctionListResponse> block;
    private final Display display;

    public PmxmlFunctionListPresenter() {
        this.context = new DmxmlContext();
        this.block = this.context.addBlock("PM_FunctionList"); //$NON-NLS$
        this.block.setParameter(new VoidRequest());
        this.context.addBlock("PM_FunctionList"); //$NON-NLS$

        this.display = new PmxmlFunctionListView();
        this.display.addOkHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PmxmlFunctionListPresenter.this.display.getDialogBox().hide();
            }
        });
    }

    public void show() {
        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(ResponseType result) {
                if(PmxmlFunctionListPresenter.this.block.isResponseOk()) {
                    final List<FunctionDesc> functionDescriptions =
                            PmxmlFunctionListPresenter.this.block.getResult().getList();
                    TableDataModelBuilder tdmb = new TableDataModelBuilder(functionDescriptions.size(), 3);
                    for(FunctionDesc functionDesc : functionDescriptions) {
                        tdmb.addRow(functionDesc.getKey(),
                                functionDesc.getRequest(),
                                functionDesc.getResponse());
                    }

                    PmxmlFunctionListPresenter.this.display.updateTableDataModel(tdmb.getResult());
                    PmxmlFunctionListPresenter.this.display.getDialogBox().center();
                }
            }
        });
    }
}
