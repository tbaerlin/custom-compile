/*
 * LookupSecurityDataResponseMock.java
 *
 * Created on 16.01.13 14:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderResultCode;
import de.marketmaker.iview.pmxml.OrderResultMSGType;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSingleQuote;
import de.marketmaker.iview.pmxml.OrderValidationServerityType;
import de.marketmaker.iview.pmxml.OrderValidationType;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.ValidationMessage;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author Markus Dick
 */
@NonNLS
public class LookupSecurityDataResponseMockHA {
    public static LookupSecurityDataResponse get(OrderSession session, String isin) {
        final LookupSecurityDataResponse response = new LookupSecurityDataResponse();

        response.setMSGType(OrderResultMSGType.OMT_RESULT);
        response.setCode(OrderResultCode.ORC_OK);

        final LookupSecurityInfo linfo = new LookupSecurityInfo();
        final OrderSecurityInfo osi = new OrderSecurityInfo();

        osi.setId("23");
        osi.setISIN(isin);
        osi.setISQuotedPerUnit(true);
        osi.setKursfaktor("0.5");
        osi.setBezeichnung("vwd AG");
        osi.setTyp(ShellMMType.ST_AKTIE);

        linfo.getCurrencyList().add(CurrencyMock.EUR_CA);
        linfo.getCurrencyList().add(CurrencyMock.USD_CA);
        linfo.getCurrencyList().add(CurrencyMock.CHF_CA);
        linfo.getCurrencyList().add(CurrencyMock.HKD_CA);

        OrderSingleQuote osqEur = new OrderSingleQuote();
        osqEur.setValue("2.42");
        osqEur.setCurrency(CurrencyMock.EUR);

        OrderSingleQuote osqUsd = new OrderSingleQuote();
        osqUsd.setValue("5.23");
        osqUsd.setCurrency(CurrencyMock.USD);

        OrderSingleQuote osqChf = new OrderSingleQuote();
        osqChf.setValue("2.23");
        osqChf.setCurrency(CurrencyMock.CHF);

        linfo.getQuoteList().add(osqEur);
        linfo.getQuoteList().add(osqUsd);
        linfo.getQuoteList().add(osqChf);

        final OrderExchangeInfo oxiBestEx = new OrderExchangeInfo();
        oxiBestEx.setName("Best Execution");
        oxiBestEx.setISOCode("Best Ex.");
        oxiBestEx.setBestExecution(true);
        oxiBestEx.setID("bestex");
        oxiBestEx.setUseExtern(false);

        final OrderExchangeInfo oxiKag = new OrderExchangeInfo();
        oxiKag.setName("KAG");
        oxiKag.setISOCode("KAG");
        oxiKag.setBestExecution(true);
        oxiKag.setID("KAG");
        oxiKag.setUseExtern(false);

        final OrderExchangeInfo oxiFfm = new OrderExchangeInfo();
        oxiFfm.setName("Frankfurt");
        oxiFfm.setISOCode("XFRA");
        oxiFfm.setBestExecution(true);
        oxiFfm.setID("XFRA");
        oxiFfm.setUseExtern(false);

        final OrderExchangeInfo oxiXetr = new OrderExchangeInfo();
        oxiXetr.setName("Xetra");
        oxiXetr.setISOCode("XETR");
        oxiXetr.setBestExecution(true);
        oxiXetr.setID("XETR");
        oxiXetr.setUseExtern(false);

        linfo.getExchangeList().add(oxiBestEx);
        linfo.getExchangeList().add(oxiKag);
        linfo.getExchangeList().add(oxiFfm);
        linfo.getExchangeList().add(oxiXetr);

        response.setSecurityInfo(linfo);

        return response;
    }

    public static LookupSecurityDataResponse addMoreCurrenciesThanQuotes(final LookupSecurityDataResponse response) {
        response.getSecurityInfo().getCurrencyList().add(CurrencyMock.AUD_CA);
        return response;
    }

    public static LookupSecurityDataResponse addInfoValidationMessage(final LookupSecurityDataResponse response) {
        final List<ValidationMessage> messages = response.getValidationMsgList();

        final ValidationMessage info = new ValidationMessage();
        info.setTyp(OrderValidationType.VT_INFO);
        info.setMsg("Ihre Drei-Raum-Rakete muss aufgetankt werden!");
        info.setServerity(OrderValidationServerityType.VST_INFO);
        messages.add(info);

        return response;
    }

    public static LookupSecurityDataResponse addErrorValidationMessage(final LookupSecurityDataResponse response) {
        final List<ValidationMessage> messages = response.getValidationMsgList();

        final ValidationMessage error = new ValidationMessage();
        error.setTyp(OrderValidationType.VT_NOT_ENOUGH_CASH);
        error.setMsg("Das Geld reicht nicht zum Auftanken!");
        error.setServerity(OrderValidationServerityType.VST_ERROR);
        messages.add(error);

        return response;
    }

    public static LookupSecurityDataResponse addQuestionValidationMessage(final LookupSecurityDataResponse response) {
        final List<ValidationMessage> messages = response.getValidationMsgList();

        final ValidationMessage question = new ValidationMessage();
        question.setTyp(OrderValidationType.VT_INFO);
        question.setMsg("Wollen Sie wirklich auftanken?");
        question.setServerity(OrderValidationServerityType.VST_QUESTION);
        question.setAnswer(ThreeValueBoolean.TV_NULL);
        messages.add(question);

        return response;
    }

    public static LookupSecurityDataResponse addInitializedQuestionValidationMessage(final LookupSecurityDataResponse response) {
        final List<ValidationMessage> messages = response.getValidationMsgList();

        final ValidationMessage question = new ValidationMessage();
        question.setTyp(OrderValidationType.VT_INFO);
        question.setMsg("Wollen Sie wirklich auftanken?");
        question.setServerity(OrderValidationServerityType.VST_QUESTION);
        question.setAnswer(ThreeValueBoolean.TV_FALSE);
        messages.add(question);

        return response;
    }

    public static LookupSecurityDataResponse addWarningValidationMessage(final LookupSecurityDataResponse response) {
        final List<ValidationMessage> messages = response.getValidationMsgList();

        final ValidationMessage warning = new ValidationMessage();
        warning.setTyp(OrderValidationType.VT_INFO);
        warning.setMsg("Niedriger Treibstoffstand!");
        warning.setServerity(OrderValidationServerityType.VST_WARNING);
        messages.add(warning);

        return response;
    }

    /**
     * Adds a vtNBotEnoughCash order validation message
     * &lt;ValidationMsgList xsi:type=&quot;n:ValidationMessage&quot;&gt;
     * &lt;Typ&gt;vtNotEnoughCash&lt;/Typ&gt;
     * &lt;Serverity&gt;vstQuestion&lt;/Serverity&gt;
     * &lt;Msg&gt;Die vorhandene Liquidit&auml;t ist geringer als der aktuelle Kurswert der Order. Soll diese Order trotzdem aufgegeben werden?&lt;/Msg&gt;
     * &lt;Answer&gt;tvFalse&lt;/Answer&gt;
     * &lt;/ValidationMsgList&gt;
     */
    public static LookupSecurityDataResponse addNotEnoughCashValidationMessage(final LookupSecurityDataResponse response) {
        final List<ValidationMessage> messages = response.getValidationMsgList();

        final ValidationMessage message = new ValidationMessage();
        message.setTyp(OrderValidationType.VT_NOT_ENOUGH_CASH);
        message.setServerity(OrderValidationServerityType.VST_QUESTION);
        message.setMsg("Die vorhandene Liquidität ist geringer als der aktuelle Kurswert der Order. Soll diese Order trotzdem aufgegeben werden?");
        message.setAnswer(ThreeValueBoolean.TV_FALSE);

        messages.add(message);

        return response;
    }
}
