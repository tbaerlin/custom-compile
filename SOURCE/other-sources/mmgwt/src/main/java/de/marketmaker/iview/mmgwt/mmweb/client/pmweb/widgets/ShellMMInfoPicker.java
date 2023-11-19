/*
 * ShellMMInfoPicker.java
 *
 * Created on 24.06.2014 12:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.MultiWidgetFocusSupport;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasEditWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmPlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SearchMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SelectFolderForm;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SelectPmSymbolController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SelectPmSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SymbolUtil;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

/**
 * @author Markus Dick
 */
public class ShellMMInfoPicker extends Composite implements Focusable, HasFocusHandlers, HasBlurHandlers, HasValue<ShellMMInfo>, HasEditWidget, ConfigurableSnippet, ValueChangeHandler<String>, HasValueChangeHandlers<ShellMMInfo> {

    public enum SelectSymbolFormStyle {
        // Currently it is not possible to distinguish between DEPOT_OBJECT and FOLDER searches,
        // because both are of TiType tiFolder. It may be possible to implement this
        // by checking the set ShellMMTypes against static hard coded lists to decide on the
        // search type to use. However, due to maintenance issues, pm core strongly recommends
        // to use the search type COMPLETE (searches everything!) for all TiFolder typed requests.
        // Unfortunately, this search does not consider the given list of ShellMMTypes.
        // Hence, a search result may also contain securities and other not explicitly set shell types.
        // TODO: Press pm core to solve this issue ASAP!
        FOLDER(SelectPmSymbolController.SearchMode.COMPLETE),
        SYMBOL(SelectPmSymbolController.SearchMode.INSTRUMENT),
        SYMBOL_WITH_ORDER_ENTRY_AVAILABILITY(SelectPmSymbolController.SearchMode.INSTRUMENT);

        private final SelectPmSymbolController.SearchMode searchMode;

        SelectSymbolFormStyle(SelectPmSymbolController.SearchMode searchMode) {
            this.searchMode = searchMode;
        }

        private SelectPmSymbolController.SearchMode getSearchMode() {
            return this.searchMode;
        }

        private boolean isInstrumentSearch() {
            return this.searchMode == SelectPmSymbolController.SearchMode.INSTRUMENT;
        }
    }

    private SnippetConfigurationView snippetConfigurationView;
    private SelectPmSymbolController selectPmSymbolController;
    private SelectSymbolForm selectSymbolForm;

    private final TextBox textBox;
    private final IconImageIcon contextLink = IconImage.getIcon("jump-to-url") // $NON-NLS$
            .withClickHandler(event -> onShellMMInfoLinkClicked());
    private final Button searchButton;

    private HashSet<ShellMMType> shellMMTypes;

    private SelectSymbolFormStyle selectSymbolFormStyle;
    private ShellMMInfo value;

    private Supplier<HistoryContext> historyContextSupplier;

    public ShellMMInfoPicker() {
        this.shellMMTypes = new HashSet<>();

        final HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName("edit-shellMMInfo");

        final FlowPanel textBoxAndContextLink = new FlowPanel();
        this.textBox = new TextBox();
        this.textBox.addValueChangeHandler(this);
        this.textBox.addKeyPressHandler(event -> {
            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                ShellMMInfoPicker.this.textBox.setFocus(false);
            }
        });
        textBoxAndContextLink.add(this.textBox);

        this.contextLink.setVisible(false);
        textBoxAndContextLink.add(this.contextLink);

        panel.add(textBoxAndContextLink);

        this.searchButton = Button.icon("mm-icon-finder") // $NON-NLS$
                .clickHandler(event -> showSnippetConfigView())
                .build();
        panel.add(searchButton);

        lazilyInitSnippetConfigurationView();

        initWidget(panel);
    }

    private void lazilyInitSnippetConfigurationView() {
        if(this.snippetConfigurationView != null) {
            return;
        }

        //SymbolParameterType does not matter in this context!
        this.snippetConfigurationView = new SnippetConfigurationView(this, SnippetConfigurationView.SymbolParameterType.IID);
        this.snippetConfigurationView.addActionPerformedHandler(event -> {
            if (MmwebServiceAsyncProxy.INSTANCE.hasPendingRequests()) {
                MmwebServiceAsyncProxy.INSTANCE.doCancelPending();
                ShellMMInfoPicker.this.selectSymbolForm.reset();
            }
        });
        this.snippetConfigurationView.addActionPerformedHandler(event -> {
            if (SnippetConfigurationView.Actions.CANCEL.name().equals(event.getKey())) {
                updateLabel(getValue());
            }
        });
    }

    public ShellMMInfoPicker withHistoryContextSupplier(Supplier<HistoryContext> historyContextSupplier) {
        this.historyContextSupplier = historyContextSupplier;
        return this;
    }

    public void setShellMMTypes(Collection<ShellMMType> shellMMTypes) {
        this.shellMMTypes.clear();
        this.shellMMTypes.addAll(shellMMTypes);
    }

    public void setSelectSymbolFormStyle(SelectSymbolFormStyle selectSymbolFormStyle) {
        this.selectSymbolFormStyle = selectSymbolFormStyle;
    }

    @Override
    public HashMap<String, String> getCopyOfParameters() {
        return new HashMap<>();
    }

    @Override
    public void setParameters(HashMap<String, String> params) {
        final int selected = this.selectSymbolForm.getSelected();
        if(selected < 0) {
            return;
        }
        setValue(this.selectPmSymbolController.getResult(selected), true);
    }

    private void updateLabel(ShellMMInfo value) {
        String label = null;
        if(value != null) {
            label = value.getBezeichnung();
        }
        this.textBox.setValue(label);

        this.contextLink.setVisible(this.historyContextSupplier != null && PmPlaceUtil.canGoTo(value));
    }

    private void onShellMMInfoLinkClicked() {
        if(this.historyContextSupplier == null) {
            return;
        }

        final ShellMMInfo value = getValue();
        if(PmPlaceUtil.canGoTo(value)) {
            PmPlaceUtil.goTo(value, this.historyContextSupplier.get());
        }
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        final String rawValue = event.getValue();
        if(!StringUtil.hasText(rawValue)) {
            setValue(null, true);
            return;
        }
        final String value = rawValue.trim().toUpperCase();

        if(this.selectSymbolFormStyle.isInstrumentSearch() && SymbolUtil.isIsin(value)) {
            SearchMethods.INSTANCE.instrumentSearchIsin(value, this.shellMMTypes, new ShellMMInfoAsyncCallback(value));
        }
        else if (this.selectSymbolFormStyle.isInstrumentSearch() && SymbolUtil.isWkn(value)) {
            SearchMethods.INSTANCE.instrumentSearchWkn(value, this.shellMMTypes, new ShellMMInfoAsyncCallback(value));
        }
        else {
            showSnippetConfigView(rawValue);
        }
    }

    @Override
    public ShellMMInfo getValue() {
        return this.value;
    }

    @Override
    public void setValue(ShellMMInfo value) {
        setValue(value, false);
    }

    @Override
    public void setValue(ShellMMInfo value, boolean fireEvents) {
        final ShellMMInfo oldValue = this.value;
        this.value = value;
        updateLabel(value);
        if(fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    public void showSnippetConfigView() {
        configureSnippetConfigurationView();
        this.snippetConfigurationView.show();
    }

    public void showSnippetConfigView(String value) {
        configureSnippetConfigurationView();
        this.snippetConfigurationView.show(value);
    }

    private void configureSnippetConfigurationView() {
        lazilyInitSnippetConfigurationView();
        configureSelectSymbolForm();
    }

    private void configureSelectSymbolForm() {
        if(this.selectSymbolForm != null) {
            this.snippetConfigurationView.removeFormWidget(this.selectSymbolForm);
        }

        switch(this.selectSymbolFormStyle) {
            case SYMBOL:
                this.selectPmSymbolController = new SelectPmSymbolController(this.selectSymbolFormStyle.getSearchMode());
                this.selectSymbolForm = SelectPmSymbolForm.createPm(
                        this.snippetConfigurationView.getParams(),
                        ShellMMTypeUtil.toStringArray(this.shellMMTypes),
                        this.selectPmSymbolController);
                break;

            case SYMBOL_WITH_ORDER_ENTRY_AVAILABILITY:
                this.selectPmSymbolController = SelectPmSymbolController.createControllerForOrderEntry(this.shellMMTypes);
                this.selectSymbolForm = SelectPmSymbolForm.createPmWithOrderEntryAvail(
                        this.snippetConfigurationView.getParams(),
                        this.shellMMTypes,
                        this.selectPmSymbolController);
                break;

            case FOLDER:
            default:
                this.selectPmSymbolController = new SelectPmSymbolController(this.selectSymbolFormStyle.getSearchMode(), this.shellMMTypes);
                this.selectSymbolForm = new SelectFolderForm(
                        this.snippetConfigurationView.getParams(),
                        ShellMMTypeUtil.toStringArray(this.shellMMTypes),
                        this.selectPmSymbolController);
        }

        this.snippetConfigurationView.addFormWidget(this.selectSymbolForm);
    }

    private class ShellMMInfoAsyncCallback implements AsyncCallback<ShellMMInfo> {
        private final String searchString;

        public ShellMMInfoAsyncCallback(String searchString) {
            this.searchString = searchString;
        }

        @Override
        public void onFailure(Throwable caught) {
            Firebug.error("<ShellMMInfoPicker> wkn/isin " + this.searchString + " not found ", caught);
            showSnippetConfigView(this.searchString);
        }

        @Override
        public void onSuccess(ShellMMInfo result) {
            Firebug.debug("<ShellMMInfoPicker> wkn/isin " + this.searchString + " found: " + result.getISIN());
            ShellMMInfoPicker.this.setValue(result, true);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ShellMMInfo> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public int getTabIndex() {
        return this.textBox.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        this.textBox.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        this.textBox.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        this.textBox.setTabIndex(index);
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return MultiWidgetFocusSupport.addFocusHandler(handler, this.textBox, this.searchButton);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return MultiWidgetFocusSupport.addBlurHandler(handler, this.textBox, this.searchButton);
    }

    @Override
    public String getStringValue() {
        final String value = this.textBox.getValue();
        return value == null || value.isEmpty() ? null : value;
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return this.textBox.addKeyUpHandler(handler);
    }
}
