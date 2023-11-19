/*
 * OrderLogBuilder.java
 *
 * Created on 01.03.13 17:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.DBBank;
import de.marketmaker.iview.pmxml.ExternMMDBRef;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.ShellMMRef;
import de.marketmaker.iview.pmxml.TextWithKey;
import de.marketmaker.iview.pmxml.TextWithTyp;
import de.marketmaker.iview.pmxml.ValidationMessage;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author Markus Dick
 */
@NonNLS
public class OrderLogBuilder extends LogBuilder {
    public OrderLogBuilder(String name) {
        this(name, null);
    }

    public OrderLogBuilder(String name, OrderSession session) {
        super(name);
        addOrderSessionInfo(session);
    }

    public void addOrderSessionInfo(OrderSession session) {
        if(session != null) {
            add("orderSession.handle", session.getHandle());
            addEnum("orderSession.brokerageModuleID", session.getBrokerageModuleID());
        }
    }

    public void addOrderTransactionType(OrderDataType order, OrderActionType orderActionType) {
        addEnum("order.typ", order.getTyp());
        addEnum("display.orderActionType", orderActionType);
    }

    public void addActivityInstanceId(OrderDataType order, String id) {
        add("order.activityId", order.getActivityId());
        add("presenter.activityInstanceId", id);
    }

    public void addActivityListEntryId(OrderDataType order, String id) {
        add("order.orderSuggestionRef", order.getOrderSuggestionRef());
        add("presenter.activityListEntryId", id);
    }

    public void addInvestor(OrderDataType order, OrderSession session) {
        addShellMMRef("order.inhaberData", order.getInhaberData());
        add("display.owner.name", session.getOwner().getName());
        add("display.owner.number", session.getOwner().getNumber());
    }

    public void addDepot(OrderDataType order) {
        addAccountRef("order.depotData", order.getDepotData());
    }

    public void addAccountData(OrderDataType order, AccountData accountData) {
        addAccountRef("order.kontoData", order.getKontoData());
        addAccountRef("display.selectedAccountChoice", accountData);
    }

    public void addOrderCurrencyData(OrderDataType order, CurrencyAnnotated exchangeCurrency) {
        addCurrency("order.orderCurrencyData", order.getOrderCurrencyData());

        final OrderCurrency currency = exchangeCurrency.getCurrency();
        addCurrency("display.selectedExchangeCurrency.currency", currency);
    }

    public void addOrderSecurityInfo(OrderDataType order, OrderSecurityInfo orderSecurityInfo) {
        addOrderSecurityInfo("order.securityData", order.getSecurityData());
        add("display.currentSecurityInfo.security.wkn", orderSecurityInfo.getWKN());
        add("display.currentSecurityInfo.security.isin", orderSecurityInfo.getISIN());
        add("display.currentSecurityInfo.security.bezeichnung", orderSecurityInfo.getBezeichnung());
    }

    public void addExchangeInfo(OrderDataType order, OrderExchangeInfo orderExchangeInfo) {
        addExchangeInfo(order, "selectedExchangeChoice", orderExchangeInfo);
    }

    public void addExchangeInfo(OrderDataType order, String displayFieldName, OrderExchangeInfo orderExchangeInfo) {
        addOrderExchangeInfo("order.exchangeData", order.getExchangeData());
        addOrderExchangeInfo("display." + displayFieldName, orderExchangeInfo);
    }

    public void addLagerstelle(OrderDataType order) {
        addExternMMDBRef("order.lagerstelleData", order.getLagerstelleData());
    }

    public void addIsShort(OrderDataType order) {
        add("order.isShort", order.isIsShort());
    }

    public LogBuilder addValidationMessages(List<ValidationMessage> validationMessages) {
        beginList();
        for(ValidationMessage v : validationMessages) {
            beginObject("ValidationMessage");
            add("type", v.getTyp());
            add("severity", v.getServerity());
            add("message", v.getMsg());
            add("answer", v.getAnswer());
            endObject();
        }
        endList();

        return this;
    }

    public void addOrderSecurityInfo(String rootNodeName, OrderSecurityInfo orderSecurityInfo) {
        if(orderSecurityInfo == null) {
            add(rootNodeName, null);
            return;
        }

        add("" + rootNodeName + ".security.wkn", orderSecurityInfo.getWKN());
        add("" + rootNodeName + ".security.isin", orderSecurityInfo.getISIN());
        add("" + rootNodeName + ".security.bezeichnung", orderSecurityInfo.getBezeichnung());
    }

    public void addAccountRef(String rootNodeName, AccountRef accountRef) {
        if(accountRef == null) {
            add(rootNodeName, null);
            return;
        }

        addShellMMRef(rootNodeName, accountRef);
        if(StringUtil.hasText(accountRef.getIBAN())) {
            add("" + rootNodeName + ".iban", accountRef.getIBAN());
        }

        final DBBank bank = accountRef.getBank();
        if(bank != null) {
            add("" + rootNodeName + ".bank.bankname", bank.getBankname());
            if(StringUtil.hasText(bank.getBIC())) {
                add("" + rootNodeName + ".bank.bic", bank.getBIC());
            }
            add("" + rootNodeName + ".bank.blz", bank.getBLZ());
            add("" + rootNodeName + ".bank.brokerageModuleID", bank.getBrokerageModuleID());
        }

        final OrderCurrency currency = accountRef.getCurrency();
        if(currency != null) {
            addExternMMDBRef(rootNodeName, currency);
        }
    }

    public void addShellMMRef(String rootNodeName, ShellMMRef shellMMRef) {
        if(shellMMRef == null) {
            add(rootNodeName, null);
            return;
        }

        add("" + rootNodeName + ".id", shellMMRef.getId());
        add("" + rootNodeName + ".number", shellMMRef.getNumber());
        add("" + rootNodeName + ".name", shellMMRef.getName());
    }

    public void addOrderExchangeInfo(String rootNodeName, OrderExchangeInfo orderExchangeInfo) {
        if(orderExchangeInfo == null) {
            add(rootNodeName, null);
            return;
        }

        add("" + rootNodeName + ".id", orderExchangeInfo.getID());
        add("" + rootNodeName + ".isoCode", orderExchangeInfo.getISOCode());
        add("" + rootNodeName + ".name", orderExchangeInfo.getName());
        add("" + rootNodeName + ".isUseExtern", orderExchangeInfo.isUseExtern());
        if(orderExchangeInfo.isUseExtern()) {
            add("" + rootNodeName + ".externRef", orderExchangeInfo.getExternRef());
            add("" + rootNodeName + ".externCode", orderExchangeInfo.getExternCode());
            add("" + rootNodeName + ".externName", orderExchangeInfo.getExternName());
        }
        addEnum("" + rootNodeName + ".externExchangeTyp", orderExchangeInfo.getExternExchangeTyp());
    }

    public void addCurrencyAnnotated(String rootNodeName, CurrencyAnnotated currencyAnnotated) {
        if(currencyAnnotated == null) {
            add(rootNodeName, null);
            return;
        }
        addCurrency("" + rootNodeName + ".currency", currencyAnnotated.getCurrency());
    }

    public void addCurrency(String rootNodeName, OrderCurrency currency) {
        if(currency == null) {
            add(rootNodeName, null);
            return;
        }
        add("" +  rootNodeName + ".id", currency.getId());
        add("" +  rootNodeName + ".kuerzel", currency.getKuerzel());
        add("" + rootNodeName + ".bez", currency.getBez());
    }

    public void addExternMMDBRef(String rootNodeName, ExternMMDBRef ref) {
        if(ref == null) {
            add(rootNodeName, null);
            return;
        }
        add("" + rootNodeName + ".id", ref.getId());
        add("" + rootNodeName + ".bez", ref.getBez());
        add("" + rootNodeName + ".kuerzel", ref.getKuerzel());
    }

    public void addTextWithKey(String rootNodeName, TextWithKey textWithKey) {
        if(textWithKey == null) {
            add(rootNodeName, null);
            return;
        }
        add("" + rootNodeName + ".key", textWithKey.getKey());
        add("" + rootNodeName + ".text", textWithKey.getText());
    }

    public void addTextWithTypeList(String rootNodeName, List<TextWithTyp> textWithTypes) {
        beginComplexParameter(rootNodeName).beginList();
        for(TextWithTyp twt : textWithTypes) {
            beginObject("TextWithTyp").addEnum("type", twt.getTyp())
                    .add("text", twt.getText())
                    .endObject();
        }
        endList().endComplexParameter();
    }
}
