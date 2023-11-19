/*
 * OrderFromSecurityPresenter.java
 *
 * Created on 19.02.13 09:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ImplementedOrderModules;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SearchMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.BlockAndTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Markus Dick
 */
public class OrderFromSecurityPresenter extends AbstractOrderViewContainerPresenter
        implements DisplayAbstract.PresenterAbstract {

    interface OrderFromSecurityDisplay extends DisplayAbstract<DisplayAbstract.PresenterAbstract> {
        void setInvestorNoEnabled(boolean enabled);
        void setDepotNoEnabled(boolean enabled);
        void setAccountNoEnabled(boolean enabled);

        void setAccountBalanceEnabled(boolean enabled);
        void setAccountChoiceEnabled(boolean enabled);
        void setPriceFieldEnabled(boolean enabled);

        void setPriceDateFieldEnabled(boolean enabled);
        void setSelectSymbolFromSearchEnabled(boolean enabled);

        void setPriceDateLabelVisible(boolean visible);
        void setPriceDateFieldVisible(boolean visible);
        void setPriceLabelVisible(boolean visible);
        void setPriceFieldVisible(boolean visible);
    }

    private final OrderFromSecurityDisplay display;
    private OrderEntryContext orderEntryContext;

    public OrderFromSecurityPresenter() {
        super(new OrderViewContainerView(I18n.I.orderEntryWindowTitle()));
        this.getOrderViewContainerDisplay().setPresenter(this);

        this.display = new OrderFromSecurityView();
        this.display.setPresenter(this);

        final List<OrderAction> orderActions = new ArrayList<>();
        final OrderAction buy = new OrderAction();
        buy.setValue(OrderActionType.AT_BUY);
        orderActions.add(buy);

        final OrderAction sell = new OrderAction();
        sell.setValue(OrderActionType.AT_SELL);
        orderActions.add(sell);
        this.display.setOrderActions(orderActions);
        this.display.setSelectedOrderAction(buy);

        this.display.setInvestorsEnabled(true);
        this.display.setInvestorNoEnabled(false);

        this.display.setDepotsEnabled(false);
        this.display.setDepotNoEnabled(false);

        this.display.setAccountChoiceEnabled(false);
        this.display.setAccountNoEnabled(false);
        this.display.setAccountBalanceEnabled(false);

        this.display.setPriceFieldEnabled(false);
        this.display.setPriceDateFieldEnabled(false);

        this.display.setSelectSymbolFromSearchEnabled(false);
        this.display.setSelectSymbolFromDepotEnabled(false);

        this.display.setPriceLabelVisible(false);
        this.display.setPriceFieldVisible(false);
        this.display.setPriceDateLabelVisible(false);
        this.display.setPriceDateFieldVisible(false);

        getOrderViewContainerDisplay().setExecuteOrderButtonEnabled(false);
        getOrderViewContainerDisplay().setContent(this.display.getOrderView());
    }

    private void loadInvestors() {
        Firebug.log("<OrderFromSecurityPresenter.loadInvestors>");
        final OrderFromSecurityDisplay d = this.display;

        SearchMethods.INSTANCE.depotObjectSearch("*", new AsyncCallback<ShellObjectSearchResponse>() { //$NON-NLS$
            @Override
            public void onSuccess(ShellObjectSearchResponse result) {
                List<ShellMMInfo> rawObjects = result.getObjects();
                List<ShellMMInfo> investorObjects = new ArrayList<>();
                for (ShellMMInfo rawObject : rawObjects) {
                    if (ShellMMType.ST_INHABER.equals(rawObject.getTyp())) {
                        investorObjects.add(rawObject);
                    }
                }

                if (investorObjects.isEmpty()) {
                    d.setInvestorsEnabled(false);

                    initDisplay(ShowState.LOAD_INVESTORS_FAILED);
                    return;
                }

                Collections.sort(investorObjects, new SearchMethods.ShellMMInfoComparator(true));

                ShellMMInfo yourChoice = new ShellMMInfo();
                yourChoice.setBezeichnung(I18n.I.orderEntrySelectInvestor());
                yourChoice.setId(Integer.toString(-1));
                investorObjects.add(0, yourChoice);

                d.setInvestorsEnabled(true);
                d.setInvestors(investorObjects, SearchMethods.INSTANCE.toMap(result.getZones()));
                d.setInvestorsSelectedIndex(0);

                initDisplay(ShowState.LOAD_INVESTORS_SUCCESS);
            }

            @Override
            public void onFailure(Throwable caught) {
                Firebug.log("<OrderFromSecurityPresenter.loadInvestors.onFailure>");
                d.setInvestors(Collections.<ShellMMInfo>emptyList(), Collections.<String, ZoneDesc>emptyMap());
                initDisplay(ShowState.LOAD_INVESTORS_FAILED);
            }
        });
    }

    @Override
    public void onInvestorChoiceChanged(ChangeEvent event) {
        checkDepotsSupported();
    }

    private void checkDepotsSupported() {
        Firebug.log("<OrderFromSecurityPresenter.checkDepotsSupported>");

        this.display.setInvestorsEnabled(false);

        final String investorId = this.display.getSelectedInvestorId();
        if(!StringUtil.hasText(investorId) || String.valueOf(-1).equals(investorId)) {
            this.display.setInvestorsSelectedIndex(0);
            return;
        }

        final DmxmlContext context = new DmxmlContext();
        final BlockAndTalker<Depot.DepotsTalker, List<Depot>, Depot> bat =
                new BlockAndTalker<>(context, new Depot.DepotsTalker());
        bat.setPrivacyModeActive(PrivacyMode.isActive());
        bat.setDatabaseId(investorId);

        Firebug.log("<OrderFromSecurityPresenter.checkDepotsSupported> investorId=" + investorId);

        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onSuccess(ResponseType result) {
                if(bat.getBlock().isResponseOk()) {
                    final List<Depot> depots = ImplementedOrderModules.filterDepots(bat.createResultObject());
                    if(depots.isEmpty()) {
                        onDepotsNotSupported();
                        return;
                    }
                    onDepotsSupported(depots.get(0).getId());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                Firebug.log("<OrderFromSecurityPresenter.fillDepots.onFailure>");

                OrderFromSecurityPresenter.this.display.setInvestorsSelectedIndex(0);
                OrderMethods.INSTANCE.showFailureMessage(caught, I18n.I.orderEntryFailedToCheckIfDepotsAreSupportedByBrokerageModules());
                OrderFromSecurityPresenter.this.display.setInvestorsEnabled(true);
            }
        });
    }

    protected void onDepotsSupported(String depotId) {
        dispose();

        final ParameterMap parameterMap = this.orderEntryContext.getParameterMap();
        parameterMap.setDepotId(depotId);
        parameterMap.setOrderActionType(this.display.getSelectedOrderAction().getValue());

        OrderModule.showByDepotId(this.orderEntryContext);
    }

    protected void onDepotsNotSupported() {
        OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(
                I18n.I.orderEntryNoDepotsWithSupportedBrokerageModulesForInvestorFound()));
        this.display.setInvestorsSelectedIndex(0);
        this.display.setInvestorsEnabled(true);
    }

    @Override
    public void onExecuteOrderClicked() {
        Firebug.log("onExecuteOrderClicked");
        throw new UnsupportedOperationException("Method OrderFromSecurityPresenter.onExecuteOrderClicked is not supported"); //$NON-NLS$
    }

    private void initDisplay() {
        final OrderAction orderAction = new OrderAction();
        orderAction.setValue(this.orderEntryContext.getParameterMap().getOrderActionType());
        this.display.setSelectedOrderAction(orderAction);

        initDisplay(ShowState.INIT);
    }

    private enum ShowState {INIT, LOAD_INVESTORS_SUCCESS, LOAD_INVESTORS_FAILED, LOAD_SECURITY_DATA_SUCCESS, LOAD_SECURITY_DATA_FAILED }
    private void initDisplay(ShowState state) {
        switch(state) {
            case INIT:
                this.display.setIsin(this.orderEntryContext.getParameterMap().getIsin());
                fillSecurityData();
                break;

            case LOAD_SECURITY_DATA_FAILED:
                firePresenterDisposedEvent();
                OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(I18n.I.orderEntryFailedToLoadSecurityData()));

            case LOAD_SECURITY_DATA_SUCCESS:
                loadInvestors();
                break;

            case LOAD_INVESTORS_SUCCESS:
                getOrderViewContainerDisplay().show();
                break;

            case LOAD_INVESTORS_FAILED:
                firePresenterDisposedEvent();
                OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(I18n.I.orderEntryFailedToLoadInverstors()));
                break;
        }
    }

    private void fillSecurityData() {
        SearchMethods.INSTANCE.instrumentSearch(this.orderEntryContext.getParameterMap().getIsin(), new AsyncCallback<ShellObjectSearchResponse>() {
            @Override
            public void onSuccess(ShellObjectSearchResponse result) {
                List<ShellMMInfo> instruments = result.getObjects();
                if(instruments.isEmpty()) {
                    initDisplay(ShowState.LOAD_SECURITY_DATA_FAILED);
                }

                final ShellMMInfo instrument = instruments.get(0);
                final OrderFromSecurityDisplay d = OrderFromSecurityPresenter.this.display;
                d.setIsin(instrument.getISIN());
                d.setWkn(instrument.getNumber());
                d.setInstrumentName(instrument.getBezeichnung());
                d.setInstrumentType(instrument.getTyp());

                initDisplay(ShowState.LOAD_SECURITY_DATA_SUCCESS);
            }

            @Override
            public void onFailure(Throwable caught) {
                initDisplay(ShowState.LOAD_SECURITY_DATA_FAILED);
            }
        });
    }

    @Override
    public void onAccountChanged(ChangeEvent event) {
        //do nothing
    }

    @Override
    public void onOrderActionChanged(ChangeEvent event) {
        //do nothing
    }

    @Override
    public void onSymbolSearchTextBoxValueChanged(ValueChangeEvent<String> event) {
        //do nothing
    }

    @Override
    public void onSymbolSearchButtonClicked(ClickEvent event) {
        //do nothing
    }

    @Override
    public void onSelectSymbolFromDepotSelected(SelectionEvent<OrderSecurityInfo> securityDataSelectionEvent) {
        //do nothing
    }

    @Override
    public void onDepotsSelectedItemChanged(ChangeEvent event) {
        //do nothing
    }

    @Override
    public void onExchangeChangedHandler(ChangeEvent event) {
        //do nothing
    }

    @Override
    public void onExchangeCurrencyChangedHandler(ChangeEvent event) {
        //do nothing
    }

    @Override
    public void show(OrderEntryContext orderEntryContext) {
        this.orderEntryContext = orderEntryContext;
        initDisplay();
    }
}
