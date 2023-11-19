/*
 * OrderConfirmationModelBuilderHA
 *
 * Created on 27.02.13 13:50
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
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.MMWaehrung;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderExpirationType;
import de.marketmaker.iview.pmxml.OrderLimitType;
import de.marketmaker.iview.pmxml.OrderTransaktionType;

import java.util.List;

/**
 * @author Markus Dick
 */
class OrderConfirmationModelBuilderHA extends AbstractOrderConfirmationModelBuilder {
    OrderConfirmationModelBuilderHA() {
        super(true);
    }

    protected void addOrderSection(OrderDataType order, LookupSecurityInfo security, LogBuilder log) {
        //Section Order 1
        Firebug.debug("Section: Order 1");
        addExchangeAndCurrencySection("", order, security.getCurrencyList(), log);

        //Section Order 2
        Firebug.debug("Section: Order 2");
        final OrderConfirmationDisplay.Section section2 = addAmountNominalSection(I18n.I.orderEntryOrder(), order);
        final List<OrderConfirmationDisplay.SimpleEntry> entries2 = section2.getEntries();

        final OrderCurrency orderCurrencyData = order.getOrderCurrencyData();
        final OrderLimitType olt = order.getLimitType();

        /* Info von Jörg:
         * Bei den Limits gibt es ja das Limitflag, den Limitwert und das Stopflag. Der Limittyp ist dann:
         * oltMarket: Limitflag und Stopflag auf false und keine Limitwert
         * oltLimit: Limitflag=true, Limitwert gesetzt und Stopflag=false
         * oltStop: Limitflag=false, Limitwert leer und Stopflag=true
         * oltStopLimit: Limitflag und Stopflag auf true und Limitwert gesetzt
         */
        final String limitText;
        final String stopText;
        switch(olt) {
            case OLT_MARKET:
                limitText = I18n.I.no();
                stopText = I18n.I.no();
                break;
            case OLT_LIMIT:
                limitText = renderConfirmLimitValue(I18n.I.yes(), order.getLimit(), orderCurrencyData.getKuerzel());
                stopText = I18n.I.no();
                break;
            case OLT_STOP:
                limitText = I18n.I.no();
                stopText = I18n.I.yes();
                break;
            case OLT_STOP_LIMIT:
                limitText = renderConfirmLimitValue(I18n.I.yes(), order.getLimit(), orderCurrencyData.getKuerzel());
                stopText = I18n.I.yes();
                break;
            default:
                DebugUtil.logToServer(log.add("Unsupported limit and stop value").add("order.limitType", olt).toString());
                throw new RuntimeException("Unsupported limit and stop value. This should never happen"); //$NON-NLS$
        }

        final String stopLabel;
        final OrderTransaktionType orderType = order.getTyp();
        switch(orderType) {
            case TT_BUY:
                stopLabel = I18n.I.orderEntryStopBuyMarket();
                break;
            case TT_SELL:
                stopLabel = I18n.I.orderEntryStopLossMarket();
                break;
            default:
                DebugUtil.logToServer(log.add("Unsupported order transaction").add("order.typ", orderType).toString());
                throw new RuntimeException("Unsupported order transaction. This should never happen"); //$NON-NLS$
        }

        entries2.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryLimit(), limitText));
        entries2.add(new OrderConfirmationDisplay.Entry(stopLabel, stopText));

        final String expirationDate = PmRenderers.DATE_STRING.render(order.getExpirationDate());

        final String expirationText;
        final OrderExpirationType expirationType = order.getExpirationType();
        switch(expirationType) {
            case OET_ULTIMO:
                expirationText = I18n.I.orderEntryUltimo();
                break;
            case OET_DAY:
                expirationText = I18n.I.orderEntryGoodForTheDay();
                break;
            case OET_DAY_BAW:
                expirationText = I18n.I.orderEntryGoodForTheDayUntilFurtherNotice();
                break;
            case OET_DATE:
                expirationText = expirationDate;
                break;
            default:
                DebugUtil.logToServer(log.add("Unsupported expiration settings").add("order.expirationType", expirationType).toString());
                throw new RuntimeException("Unsupported expiration settings. This should never happen");  //$NON-NLS$
        }

        //Section Order 3
        Firebug.debug("Section: Order 3");

        final OrderConfirmationDisplay.Section section3 = new OrderConfirmationDisplay.Section("");
        final List<OrderConfirmationDisplay.SimpleEntry>entries3 = section3.getEntries();
        entries3.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryValidity(), expirationText));
        getSections().add(section3);
    }

    protected OrderConfirmationDisplay.Section addHAHintSection(int index) {
        OrderConfirmationDisplay.Section section = new OrderConfirmationDisplay.Section("", 2);
        List<OrderConfirmationDisplay.SimpleEntry> entries = section.getEntries();
        entries.add(new OrderConfirmationDisplay.SimpleEntry(I18n.I.orderEntryHAOrderConfirmationMessage()));
        getSections().add(index, section);
        return section;
    }

    private String renderConfirmLimitValue(String choice, String limit, String currencySymbol) {
        return choice + ": " + Renderer.PRICE23.render(limit) + " " + currencySymbol; //$NON-NLS$
    }
}
