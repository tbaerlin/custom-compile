/*
 * AbstractOrderConfirmationModelBuilder
 *
 * Created on 23.08.13 12:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.DBBank;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides some methods that may be commonly used to build OrderConfirmationModels.
 * This class is not intended to be necessarily the base class of all OrderConfirmationModel builders.
 *
 * @author Markus Dick
 */
public abstract class AbstractOrderConfirmationModelBuilder {
    private final List<OrderConfirmationDisplay.Section> sections = new ArrayList<>();

    private final boolean strict;

    protected AbstractOrderConfirmationModelBuilder(boolean strict) {
        this.strict = strict;
    }

    public boolean isStrict() {
        return strict;
    }

    protected List<OrderConfirmationDisplay.Section> getSections() {
        return this.sections;
    }

    protected void addTransactionSection(OrderDataType order) {
        final OrderConfirmationDisplay.Section section = new OrderConfirmationDisplay.Section(I18n.I.orderEntryTransaction(), 2);
        final List<OrderConfirmationDisplay.SimpleEntry>entries = section.getEntries();
        entries.add(new OrderConfirmationDisplay.SimpleEntry(renderStrict(order.getTyp(), OeRenderers.ORDER_TRANSACTION_TYPE_RENDERER)));
        this.sections.add(section);
    }

    protected void addPortfolioSection(OrderDataType order, LogBuilder log) {
        final AccountRef account = order.getKontoData();

        final String accountName;
        final String accountNumber;
        final String accountCurrency;
        final String bankName;
        final String blz;
        if(account == null) {
            if(this.strict) {
                DebugUtil.logToServer(log.add("Account of order is null!").toString());
                throw new RuntimeException("Account of order is null. This should never happen"); //$NON-NLS$
            }

            accountName = accountNumber = accountCurrency = bankName = blz = I18n.I.orderEntryDataNotAvailableInAS();
        }
        else {
            accountName = Renderer.STRING_DOUBLE_DASH.render(account.getName());
            accountNumber = account.getNumber();
            accountCurrency = OeRenderers.ORDER_CURRENCY_RENDERER.render(account.getCurrency());

            final DBBank bank = account.getBank();
            if(bank != null) {
                bankName = Renderer.STRING_DOUBLE_DASH.render(bank.getBankname());
                blz = Renderer.STRING_DOUBLE_DASH.render(bank.getBLZ());
            }
            else {
                bankName = blz = Renderer.STRING_DOUBLE_DASH.render(null);
            }
        }

        final OrderConfirmationDisplay.Section section1 = new OrderConfirmationDisplay.Section(I18n.I.portfolio());
        final List<OrderConfirmationDisplay.SimpleEntry>entries1 = section1.getEntries();
        entries1.add(new OrderConfirmationDisplay.Entry(I18n.I.pmInvestor(), Renderer.STRING_DOUBLE_DASH.render(order.getInhaberData().getName())));
        entries1.add(new OrderConfirmationDisplay.Entry(I18n.I.pmDepot(), Renderer.STRING_DOUBLE_DASH.render(order.getDepotData().getName())));
        entries1.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryAccount(), accountName));
        this.sections.add(section1);

        final OrderConfirmationDisplay.Section section2 = new OrderConfirmationDisplay.Section("");
        final List<OrderConfirmationDisplay.SimpleEntry>entries2 = section2.getEntries();
        entries2.add(new OrderConfirmationDisplay.Entry(I18n.I.investorNumberAbbr(), Renderer.STRING_DOUBLE_DASH.render(order.getInhaberData().getNumber())));
        entries2.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryDepotNo(), order.getDepotData().getNumber()));
        entries2.add(new OrderConfirmationDisplay.Entry(I18n.I.accountNo(), Renderer.STRING_DOUBLE_DASH.render(accountNumber)));
        entries2.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryAccountCurrency(), accountCurrency));

        entries2.add(new OrderConfirmationDisplay.Entry(I18n.I.bank(), bankName));
        entries2.add(new OrderConfirmationDisplay.Entry(I18n.I.bankCodeAbbr(), blz));
        //currently, we do not have the IBAN, hence, we also do not show the BIC
//        entries2.add(new OrderConfirmationDisplay.Entry("BIC", bank.getBIC()));

        this.sections.add(section2);
    }

    protected void addSecuritySection(OrderDataType order, LogBuilder log) {
        final OrderSecurityInfo securityData = order.getSecurityData();

        if(securityData == null) {
            log.add("Security of order is null!"); //$NON-NLS$
            DebugUtil.logToServer(log.toString());
            throw new RuntimeException("Security of order is null. This should never happen"); //$NON-NLS$
        }

        final OrderConfirmationDisplay.Section section = new OrderConfirmationDisplay.Section(I18n.I.instrument());
        final List<OrderConfirmationDisplay.SimpleEntry> entries = section.getEntries();

        final String instrumentNameLabel;
        if(ShellMMType.ST_UNBEKANNT != securityData.getTyp()) {
            instrumentNameLabel = PmRenderers.SHELL_MM_TYPE.render(securityData.getTyp());
        }
        else {
            instrumentNameLabel = I18n.I.orderEntryBHLKGSInstrumentName();
        }

        entries.add(new OrderConfirmationDisplay.Entry(instrumentNameLabel, securityData.getBezeichnung()));
        entries.add(new OrderConfirmationDisplay.Entry("ISIN", securityData.getISIN())); //$NON-NLS$
        entries.add(new OrderConfirmationDisplay.Entry("WKN", securityData.getNumber())); //$NON-NLS$
        this.sections.add(section);
    }

    protected OrderConfirmationDisplay.Section addExchangeAndCurrencySection(String sectionHeadline, OrderDataType order, List<CurrencyAnnotated> currencyList, LogBuilder log) {
        final OrderExchangeInfo orderExchangeInfo = order.getExchangeData();
        final String orderExchangeInfoStr = renderStrict(orderExchangeInfo, OeRenderers.ORDER_EXCHANGE_INFO_RENDERER);

        final OrderCurrency exchangeCurrency = order.getOrderCurrencyData();
        final String exchangeCurrencyStr = renderStrict(exchangeCurrency, OeRenderers.ORDER_CURRENCY_RENDERER);

        final OrderConfirmationDisplay.Section section = new OrderConfirmationDisplay.Section(sectionHeadline);
        final List<OrderConfirmationDisplay.SimpleEntry> entries = section.getEntries();
        entries.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryExchange(), orderExchangeInfoStr));
        entries.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryExchangeCurrency(), exchangeCurrencyStr));
        getSections().add(section);

        return section;
    }

    protected OrderConfirmationDisplay.Section addOrderNumberSection(int index, OrderDataType order) {
        OrderConfirmationDisplay.Section section = new OrderConfirmationDisplay.Section(I18n.I.orderEntryOrderSuccessfullyCreated(order.getOrderNumber()), 2);
        getSections().add(index, section);
        return section;
    }

    protected OrderConfirmationDisplay.Section addAmountNominalSection(String sectionHeadline, OrderDataType order) {
        final OrderConfirmationDisplay.Section section = new OrderConfirmationDisplay.Section(sectionHeadline);
        final List<OrderConfirmationDisplay.SimpleEntry> entries = section.getEntries();
        entries.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryAmountNominal(),
                Renderer.PRICE23.render(order.getQuantity()) + " " + I18n.I.orderEntryAmountPerCurrency())); //$NON-NLS$
        getSections().add(section);
        return section;
    }

    protected OrderExchangeInfo resolveOrderExchangeInfo(String exchangeId, LookupSecurityInfo security, LogBuilder log) {
        Firebug.debug("resolveOrderExchangeInfo");

        OrderExchangeInfo orderExchangeInfo = null;
        for(OrderExchangeInfo exchange : security.getExchangeList()) {
            if(exchange.getID().equals(exchangeId)) {
                orderExchangeInfo = exchange;
            }
        }
        if(this.strict && orderExchangeInfo == null) {
            DebugUtil.logToServer(log.add("Exchange of order not found").add("order.exchangeId", exchangeId).toString());
            throw new RuntimeException("Exchange of order not found. This should never happen"); //$NON-NLS$
        }
        return orderExchangeInfo;
    }

    protected CurrencyAnnotated resolveCurrencyAnnotated(String exchangeCurrencyId, List<CurrencyAnnotated> currencyList, LogBuilder log) {
        Firebug.debug("resolveCurrencyAnnotated");

        CurrencyAnnotated exchangeCurrency = null;
        for(CurrencyAnnotated currency : currencyList) {
            if(currency.getCurrency().getId().equals(exchangeCurrencyId)) {
                exchangeCurrency = currency;
            }
        }
        if(this.strict && exchangeCurrency == null) {
            DebugUtil.logToServer(log.add("Currency of order not found").add("order.orderCurrency", exchangeCurrencyId).toString());
            throw new RuntimeException("Currency of order not found. This should never happen"); //$NON-NLS$
        }
        return exchangeCurrency;
    }

    protected <T> String renderStrict(T rawValue, Renderer<T> renderer) {
        return renderStrict(rawValue, renderer, null);
    }

    protected <T> String renderStrict(T rawValue, Renderer<T> renderer, Renderer<String> stringRenderer) {
        if(!this.strict && rawValue == null) {
            return I18n.I.orderEntryDataNotAvailableInAS();
        }

        String value = renderer.render(rawValue);
        if(stringRenderer != null) {
            value = stringRenderer.render(value);
        }
        return value;
    }
}
