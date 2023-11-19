/*
 * AbstractDisplay.java
 *
 * Created on 29.10.13 13:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.List;
import java.util.Map;

/**
 * @author Markus Dick
 */
public interface DisplayAbstract<P extends DisplayAbstract.PresenterAbstract> extends Display<P> {
    void setOrderActions(List<OrderAction> actions);
    void setSelectedOrderAction(OrderAction action);
    OrderAction getSelectedOrderAction();

    void setInvestors(List<ShellMMInfo> investors, Map<String, ZoneDesc> zoneDescs);
    void setInvestorsSelectedIndex(int index);
    void setInvestorsEnabled(boolean enabled);
    String getSelectedInvestorId();

    void setInvestorNo(String investorNo);

    void setDepots(List<Depot> depotsOfInvestor);
    void setDepotsEnabled(boolean enabled);
    void setDepotsSelectedItem(Depot item);
    Depot getDepotsSelectedItem();

    void setAccounts(List<AccountData> accounts);
    void setAccountsSelectedItem(int value);
    int getAccountsSelectedItem();

    void setAccountBalance(String accountBalance, String currencySymbol);

    void setInstrumentType(ShellMMType type);
    void setInstrumentName(String name);
    void setWkn(String wkn);
    void setExchangeChoiceEnabled(boolean enable);
    void setExchangeCurrencyChoiceEnabled(boolean enable);
    void setExchangeChoices(List<OrderExchangeInfo> exchangeInfoList);
    void setExchangeCurrencyChoices(List<CurrencyAnnotated> exchangeCurrencyList);
    int getSelectedExchangeCurrency();
    void setSelectedExchangeCurrency(int currencyIndex);
    int getSelectedExchangeChoice();
    void setSelectedExchangeChoice(int exchangeIndex);
    void setDepotBankName(String depotBankName);
    void setAccountNo(String accountNo);
    void setSymbolsOfDepot(List<OrderSecurityInfo> securityDataList);
    void setSelectSymbolFromDepotEnabled(boolean enabled);
    void setPrice(String price);
    void setPriceDate(String date);
    void setCurrencyLabels(String currentCurrency);

    public interface PresenterAbstract extends Display.Presenter {
        void onAccountChanged(ChangeEvent event);
        void onOrderActionChanged(ChangeEvent event);
        void onSymbolSearchTextBoxValueChanged(ValueChangeEvent<String> event);
        void onSymbolSearchButtonClicked(ClickEvent event);
        void onSelectSymbolFromDepotSelected(SelectionEvent<OrderSecurityInfo> securityDataSelectionEvent);
        void onDepotsSelectedItemChanged(ChangeEvent event);
        void onInvestorChoiceChanged(ChangeEvent event);
        void onExchangeChangedHandler(ChangeEvent event);
        void onExchangeCurrencyChangedHandler(ChangeEvent event);
    }
}
