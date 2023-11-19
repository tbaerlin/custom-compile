/*
 * SnippetConfigurationView.java
 *
 * Created on 05.05.2008 12:50:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.Activatable;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnippetConfigurationView implements ConfigurationPresenter {
    public enum SymbolParameterType { IID, ISIN }

    public static String[] DEFAULT_FILTER_TYPES = null;

    public static boolean QUOTE_SELECTION_WITH_VWDCODE = false;

    private final HandlerManager handlerManager = new HandlerManager(this);

    private final ArrayList<Widget> forms = new ArrayList<>();

    private final HashMap<String, String> params;

    private final ConfigurableSnippet snippet;

    private int activeItem = 0;

    private Button backButton;

    private Button nextButton;

    private Label page;

    private SimplePanel content;

    private final SymbolParameterType symbolParameterType;

    public SnippetConfigurationView(ConfigurableSnippet s) {
        this.snippet = s;
        this.params = this.snippet.getCopyOfParameters();
        this.symbolParameterType = SymbolParameterType.IID;
    }
    public SnippetConfigurationView(ConfigurableSnippet s, SymbolParameterType symbolParameterType) {
        this.snippet = s;
        this.params = this.snippet.getCopyOfParameters();
        this.symbolParameterType = symbolParameterType;
    }

    public void addSelectSymbol(String[] filterTypes) {
        addSelectSymbol(filterTypes, null, null);
    }

    public void addSelectSymbol(String[] filterTypes, String filterForUnderlyingsForType,
                                 Boolean filterForUnderlyingsOfLeveragProducts) {
        addFormWidget(SelectSymbolForm.create(this.getParams(),
                (filterTypes != null) ? filterTypes : DEFAULT_FILTER_TYPES, filterForUnderlyingsForType,
                filterForUnderlyingsOfLeveragProducts, this.symbolParameterType));
        addFormWidget(new SelectQuoteForm(getParams()));
    }

    public void addSelectSymbol(String[] filterTypes,
                                String filterForUnderlyingsForType,
                                Boolean filterForUnderlyingsOfLeveragProducts,
                                SelectSymbolFormControllerInterface selectSymbolFormControllerInterface,
                                boolean showQuoteDataColumns) {
        addFormWidget(
                SelectSymbolForm.create(getParams(),
                        (filterTypes != null) ? filterTypes : DEFAULT_FILTER_TYPES,
                        filterForUnderlyingsForType,
                        filterForUnderlyingsOfLeveragProducts,
                        selectSymbolFormControllerInterface,
                        this.symbolParameterType,
                        showQuoteDataColumns
                ));
        addFormWidget(new SelectQuoteForm(getParams()));
    }

    public void addSelectedCertSymbol(String certCategory) {
        addFormWidget(SelectSymbolForm.createCert(getParams(), certCategory, this.symbolParameterType));
        addFormWidget(new SelectQuoteForm(getParams()));
    }

    public void addConfigurePages() {
        addFormWidget(new PagesConfigurationForm(getParams()));
    }

    public void addFormWidget(Widget widget) {
        widget.setPixelSize(500, 300);
        this.forms.add(widget);
    }

    public void removeFormWidget(Widget widget) {
        this.forms.remove(widget);
    }

    public HashMap<String, String> getParams() {
        return this.params;
    }

    public void show() {
        show(true);
    }

    @Override
    public void show(String presetSearchString) {
        getParams().put(SelectSymbolForm.PRESET_SEARCH_STRING, presetSearchString);
        show();
    }

    public void show(boolean showMarketPage) {
        assert forms.size() > 0;

        this.content = new SimplePanel(this.forms.get(0));

        final DialogIfc dialog = Dialog.getImpl().createDialog()
                .withTitle(I18n.I.selection())
                .withWidget(this.content)
                .withStyle("mm-noPadding") // $NON-NLS$
                .withCloseButton()
                .withButton(I18n.I.ok(), new Command() {
                    @Override
                    public void execute() {
                        switchPageFromTo(forms.get(activeItem), null);
                        snippet.setParameters(params);
                        fireEvent(new ActionPerformedEvent(Actions.OK.name()));
                    }
                })
                .withButton(I18n.I.cancel(), new Command() {
                    @Override
                    public void execute() {
                        fireEvent(new ActionPerformedEvent(Actions.CANCEL.name()));
                    }
                });

        if (this.forms.size() > 1 && showMarketPage) {
            this.page = new Label(getPage(0));

            this.backButton = Button.text(I18n.I.back())
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            gotoPage(-1);
                        }
                    })
                    .build();
            this.backButton.setEnabled(false); // we start with 0, so no back

            this.nextButton = Button.text(I18n.I.proceed())
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            gotoPage(1);
                        }
                    })
                    .build();
            this.nextButton.setEnabled(this.forms.size() != 1);

            final FloatingToolbar tb = new FloatingToolbar();
            tb.add(this.backButton);
            tb.addEmpty("5px"); // $NON-NLS$
            tb.addSeparator();
            tb.addEmpty("5px"); // $NON-NLS$
            tb.add(this.page);
            tb.addFill();
            tb.addSeparator();
            tb.addEmpty("5px"); // $NON-NLS$
            tb.add(this.nextButton);
            dialog.withBottomWidget(tb);
        }

        final Widget firstForm = this.forms.get(this.activeItem);
        if (firstForm instanceof Activatable) {
            ((Activatable)firstForm).activate();
        }
        if (firstForm instanceof HasFocusWidget) {
            dialog.withFocusWidget(((HasFocusWidget) firstForm).getFocusWidget());
        }

        dialog.show();
    }

    private void gotoPage(int offset) {
        final int oldPage = this.activeItem;
        this.activeItem = this.activeItem + offset;
        Firebug.log("gotoPage " + oldPage + " to " + this.activeItem); // $NON-NLS-0$ $NON-NLS-1$
        switchPageFromTo(this.forms.get(oldPage), this.forms.get(this.activeItem));
        this.backButton.setEnabled(this.activeItem != 0);
        this.nextButton.setEnabled(activeItem + 1 < this.forms.size());
        this.page.setText(getPage(this.activeItem));
        this.content.setWidget(this.forms.get(this.activeItem));
    }

    private void switchPageFromTo(Widget oldPanel, Widget newPanel) {
        if (oldPanel != null && (oldPanel instanceof Activatable)) {
            ((Activatable) oldPanel).deactivate();
        }
        if (newPanel != null && (newPanel instanceof Activatable)) {
            ((Activatable) newPanel).activate();
        }
    }

    private String getPage(final int n) {
        return I18n.I.page() + " " + (n + 1) + "/" + this.forms.size();  // $NON-NLS-0$ $NON-NLS-1$
    }

    @Override
    public void addConfigurationWidget(Widget widget, AbstractImagePrototype icon, String label) {
        addFormWidget(widget);
        if(widget instanceof HasCancellablePendingRequests) {
            addActionPerformedHandler(new CancelPendingRequests((HasCancellablePendingRequests)widget));
        }
    }

    @Override
    public HandlerRegistration addActionPerformedHandler(ActionPerformedHandler handler) {
        return this.handlerManager.addHandler(ActionPerformedEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }
}