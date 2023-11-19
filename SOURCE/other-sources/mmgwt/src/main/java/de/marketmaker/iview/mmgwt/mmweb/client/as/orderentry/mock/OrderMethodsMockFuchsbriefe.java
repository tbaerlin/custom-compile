/*
 * OrderSessionMockFuchsbriefe.java
 *
 * Created on 28.10.13 10:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSessionContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AccountInfo;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.AllocateOrderSessionDataResponse;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.DBBank;
import de.marketmaker.iview.pmxml.ExternExchangeType;
import de.marketmaker.iview.pmxml.GetOrderbookRequest;
import de.marketmaker.iview.pmxml.GetOrderbookResponse;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.MarketMode;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderExecutionOption;
import de.marketmaker.iview.pmxml.OrderExpirationType;
import de.marketmaker.iview.pmxml.OrderLimitType;
import de.marketmaker.iview.pmxml.OrderResultCode;
import de.marketmaker.iview.pmxml.OrderResultMSGType;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptor;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptor;
import de.marketmaker.iview.pmxml.OrderSingleQuote;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.OrderValidationServerityType;
import de.marketmaker.iview.pmxml.OrderValidationType;
import de.marketmaker.iview.pmxml.SendOrderDataResponse;
import de.marketmaker.iview.pmxml.SessionState;
import de.marketmaker.iview.pmxml.ShellMMRef;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellMMTypeDesc;
import de.marketmaker.iview.pmxml.ValidateOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidationMessage;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock.MockUtil.*;

/**
 * Enable order entry mocks by setting the property orderEntry in your DevMmweb to mock.
 * Be aware that depot objects with specific object IDs must exist in your pm database.
 * <code>&lt;set-property name=&quot;orderEntry&quot; value=&quot;mock&quot;/&lt;</code>
 * </code>&lt;set-property name=&quot;orderEntry&quot; value=&quot;prod&quot;/&lt;</code>
 *
 * The property fuchsbriefeMockAllDepotIds resides still in guidefs.
 *
 * @author Markus Dick
 */
@NonNLS
public class OrderMethodsMockFuchsbriefe extends OrderMethods implements OrderMethodsMockInterface {
    public static final String MOCK_HANDLE_FUCHSBRIEFE = "MOCK_HANDLE_FUCHSBRIEFE";
    public static final String MOCK_DEPOT_ID = "26466804";
    private static final String MM_SECURITY_ID_STOCK = "90577300"; //wkn: BASF11
    private static final String MM_SECURITY_ID_STOCK_PM = "&123456789"; //arbitrageButton should not be shown
    private static final String MM_SECURITY_ID_STOCK_IID_DOES_NOT_EXIST = "1234567890"; //arbitrageButton should not be shown because iid does not exist
    private static final String MM_SECURITY_ID_BOND = "43006011"; //isin: XS0412154378 wkn A0T6EG A0T6EG
    public static final String FUCHSBRIEFE_MOCK_ALL_DEPOT_IDS_GUIDEFS_KEY = "fuchsbriefeMockAllDepotIds";

    /**
     * Works only for public depots, because private depots do never qualify for order entry.
     */
    private final boolean fuchsbriefeMockAllDepotIds;

    public OrderMethodsMockFuchsbriefe() {
        this.fuchsbriefeMockAllDepotIds = getMockAllDepotIdsGuidefValue();
        Firebug.debug("<OrderMethodsMock> " + FUCHSBRIEFE_MOCK_ALL_DEPOT_IDS_GUIDEFS_KEY + "=" + this.fuchsbriefeMockAllDepotIds);
    }

    private static Boolean getMockAllDepotIdsGuidefValue() {
        final JSONWrapper oeConfig = SessionData.INSTANCE.getGuiDefs().get("order-entry");  // $NON-NLS$
        if(oeConfig != null) {
            final JSONWrapper value = oeConfig.get(FUCHSBRIEFE_MOCK_ALL_DEPOT_IDS_GUIDEFS_KEY);
            if(value != null) {
                return value.booleanValue();
            }
        }
        return false;
    }

    @Override
    public void allocateOrderSession(OrderSessionContainer containerHandle, String depotId, AsyncCallback<OrderSession> callback) {
        Firebug.info("<OrderSessionMockFuchsbriefe.create>");

        final ShellMMRef ownerRef = new ShellMMRef();
        ownerRef.setId("12345678");
        ownerRef.setName("Test AS Inhaber Fuchsbriefe");
        ownerRef.setNumber("AS-INH-FB-01");

        final ShellMMRef portfolioRef = new ShellMMRef();
        portfolioRef.setId("12345678");
        portfolioRef.setName("Test AS Portfolio Fuchsbriefe");
        portfolioRef.setNumber("AS-PTF-FB-01");

        final AccountRef depotRef = new AccountRef();
        depotRef.setId(MOCK_DEPOT_ID);
        depotRef.setName("Test AS Depot Fuchsbriefe");
        depotRef.setNumber("AS-DEP-FB-01");

        final DBBank dbBank = new DBBank();
        dbBank.setBankname("Fuchsbriefe Mock");
        dbBank.setBrokerageModuleID(BrokerageModuleID.BM_FUCHSBRIEFE.value());

        depotRef.setBank(dbBank);
        depotRef.setCurrency(CurrencyMock.EUR);

        final AccountData accountData = new AccountData();
        accountData.setId("12345678");
        accountData.setName("Test AS Konto Fuchsbriefe");
        accountData.setNumber("AS-KTO-FB-01");
        accountData.setBank(dbBank);
        accountData.setBalance("42000.0");
        accountData.setCurrency(CurrencyMock.EUR);

        final AccountInfo accountInfo = new AccountInfo();
        accountInfo.setOwner(ownerRef);
        accountInfo.setPortfolio(portfolioRef);
        accountInfo.setSecurityAccount(depotRef);
        accountInfo.getAccountList().add(accountData);

        final OrderSessionFeaturesDescriptor sessionFeaturesDescriptor = new OrderSessionFeaturesDescriptor();
        for(OrderActionType type : new OrderActionType[] { OrderActionType.AT_BUY, OrderActionType.AT_SELL } ) {
            OrderAction orderAction = new OrderAction();
            orderAction.setValue(type);
            sessionFeaturesDescriptor.getOrderActions().add(orderAction);
        }
        for(ShellMMType type : new ShellMMType[] { ShellMMType.ST_AKTIE, ShellMMType.ST_ANLEIHE, ShellMMType.ST_FOND} ) {
            ShellMMTypeDesc desc = new ShellMMTypeDesc();
            desc.setT(type);
            sessionFeaturesDescriptor.getTradableSecurityTypes().add(desc);
        }

        final AllocateOrderSessionDataResponse response = new AllocateOrderSessionDataResponse();
        response.setBrokerageModul(BrokerageModuleID.BM_FUCHSBRIEFE);
        response.setAccountInfo(accountInfo);
        response.setOrderSessionState(SessionState.SS_AUTHENTICATED);
        response.setFeatures(sessionFeaturesDescriptor);
        response.setHandle(MOCK_HANDLE_FUCHSBRIEFE);

        callback.onSuccess(new OrderSession.OrderSessionFuchsbriefe(response));
    }

    @Override
    public void closeOrderSession(OrderSession orderSession, AsyncCallback<OrderSession> callback) {
        Firebug.info("<OrderSessionMockFuchsbriefe.closeOrderSession> this mock impl. does nothing!");
    }

    @Override
    public void lookupSecurity(final OrderSession orderSession, OrderTransaktionType type, String isin, AsyncCallback<LookupSecurityDataResponse> callback) {
        Firebug.info("<OrderSessionMockFuchsbriefe.lookupSecurity> isin=" + isin + " type=" + type);
        Firebug.info("<OrderSessionMockFuchsbriefe.lookupSecurity> other ISINs: " +
                "DE4200000009 throws ORC_SECURITY_NOT_FOUND (with externalMessage), " +
                "XX2300000005 Stock (order in units), " +
                "XX2350000004 Bond (order in amount)");

        if("DE4200000009".equals(isin)) {
            callback.onFailure(
                    new OrderMethods.PmxmlOrderEntryResponseException(
                            "Security DE4200000009 not found!",  //internal PM message
                            OrderResultCode.ORC_SECURITY_NOT_FOUND,
                            OrderResultMSGType.OMT_ERROR));
            return;
        }

        final LookupSecurityInfo lookupSecurityInfo = new LookupSecurityInfo();

        final List<OrderExchangeInfo> orderExchangeInfos = lookupSecurityInfo.getExchangeList();
        //nur eine kleine Auswahl, tatsächliche Plätze unklar.
        orderExchangeInfos.addAll(Arrays.asList(
                newOrderExchangeInfo("Frankfurt_101_EUR", "XFRA", "101"),
                newOrderExchangeInfo("München_102_NO_QUOTE", "XMUN", "102"),
                newOrderExchangeInfo("Stuttgart_103_EUR", "XSTU", "103"),
                newOrderExchangeInfo("Zürich_201_CHF_EUR", "XSWX", "201"),
                newOrderExchangeInfo("NYSE_301_USD", "XNYS", "301"),
                newOrderExchangeInfo("Hong Kong_401_HKD_USD", "XHKG", "401")
        ));

        final OrderSecurityInfo orderSecurityInfo = new OrderSecurityInfo();
        orderSecurityInfo.setKursfaktor("1.0");
        orderSecurityInfo.setWKN("DUMMY");
        orderSecurityInfo.setISIN(isin);
        orderSecurityInfo.setBezeichnung("DUMMY SECURITY");
        if("XX2350000004".equals(isin)) {
            orderSecurityInfo.setISQuotedPerUnit(false);
            orderSecurityInfo.setTyp(ShellMMType.ST_ANLEIHE);
            orderSecurityInfo.setId("2354");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_BOND);
        }
        else if("XX2354200006".equals(isin)) {
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("23542");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_STOCK_PM);
        }
        else if("XX2354260000".equals(isin)) {
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("235426");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_STOCK_IID_DOES_NOT_EXIST);
        }
        else {
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("235");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_STOCK);
        }

        final OrderSingleQuote orderSingleQuoteEUR101 = new OrderSingleQuote();
        orderSingleQuoteEUR101.setValue("42.101");
        orderSingleQuoteEUR101.setExchangeID("101");
        orderSingleQuoteEUR101.setCurrency(CurrencyMock.EUR);
        orderSingleQuoteEUR101.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        final OrderSingleQuote orderSingleQuoteEUR103 = new OrderSingleQuote();
        orderSingleQuoteEUR103.setValue("42.103");
        orderSingleQuoteEUR103.setExchangeID("103");
        orderSingleQuoteEUR103.setCurrency(CurrencyMock.EUR);
        orderSingleQuoteEUR103.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        final OrderSingleQuote orderSingleQuoteCHF201 = new OrderSingleQuote();
        orderSingleQuoteCHF201.setExchangeID("201");
        orderSingleQuoteCHF201.setValue("21.201");
        orderSingleQuoteCHF201.setCurrency(CurrencyMock.CHF);
        orderSingleQuoteCHF201.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        final OrderSingleQuote orderSingleQuoteEUR201 = new OrderSingleQuote();
        orderSingleQuoteEUR201.setValue("42.201");
        orderSingleQuoteEUR201.setExchangeID("201");
        orderSingleQuoteEUR201.setCurrency(CurrencyMock.EUR);
        orderSingleQuoteEUR201.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        final OrderSingleQuote orderSingleQuoteUSD301 = new OrderSingleQuote();
        orderSingleQuoteUSD301.setValue("51.301");
        orderSingleQuoteUSD301.setExchangeID("301");
        orderSingleQuoteUSD301.setCurrency(CurrencyMock.USD);
        orderSingleQuoteUSD301.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        final OrderSingleQuote orderSingleQuoteHKD401 = new OrderSingleQuote();
        orderSingleQuoteHKD401.setValue("168.401");
        orderSingleQuoteHKD401.setExchangeID("401");
        orderSingleQuoteHKD401.setCurrency(CurrencyMock.HKD);
        orderSingleQuoteHKD401.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        final OrderSingleQuote orderSingleQuoteUSD401 = new OrderSingleQuote();
        orderSingleQuoteUSD401.setValue("51.401");
        orderSingleQuoteUSD401.setExchangeID("401");
        orderSingleQuoteUSD401.setCurrency(CurrencyMock.USD);
        orderSingleQuoteUSD401.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        final OrderSingleQuote orderSingleQuoteXXE = new OrderSingleQuote();
        orderSingleQuoteXXE.setValue("23.42");
        orderSingleQuoteXXE.setCurrency(CurrencyMock.XXE);
        orderSingleQuoteXXE.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));

        lookupSecurityInfo.setSecurity(orderSecurityInfo);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteEUR101);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteEUR103);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteCHF201);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteEUR201);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteUSD301);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteHKD401);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteUSD401);
        lookupSecurityInfo.getQuoteList().add(orderSingleQuoteXXE);

        lookupSecurityInfo.getCurrencyList().addAll(Arrays.asList(
                CurrencyMock.USD_CA,
                CurrencyMock.EUR_CA_EXT_INFO_DEFAULT,
                CurrencyMock.CHF_CA,
                CurrencyMock.HKD_CA,
                CurrencyMock.XXE_CA
                ));

        final OrderSecurityFeatureDescriptor orderSecurityFeatureDescriptor = new OrderSecurityFeatureDescriptor();

        final LookupSecurityDataResponse response = new LookupSecurityDataResponse();
        response.setFeatures(orderSecurityFeatureDescriptor);
        response.setSecurityInfo(lookupSecurityInfo);

        callback.onSuccess(response);
    }

    @Override
    public void validateOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<ValidateOrderDataResponse> callback) {
        Firebug.info("<OrderSessionMockFuchsbriefe.validateOrder>");

        final ValidateOrderDataResponse response = new ValidateOrderDataResponse();
        final List<ValidationMessage> messages = response.getValidationMsgList();

        final OrderDataTypeMock copyOfOrder = new OrderDataTypeMock(order);
        response.setOrder(copyOfOrder);

        if(order.getTyp() == null) {
            messages.add(createValidationMessage("Transaction type not set"));
        }
        if(order.getKontoData() == null) {
            messages.add(createValidationMessage("Account not selected"));
        }
        if(order.getSecurityData() == null) {
            messages.add(createValidationMessage("No security selected"));
        }
        if(order.getExchangeData() == null) {
            messages.add(createValidationMessage("No exchange selected"));
        }
        if(!order.isIsOrderInAmount() && !StringUtil.hasText(order.getQuantity())) {
            messages.add(createValidationMessage("Quantity not set"));
        }
        if(OrderExpirationType.OET_DATE == order.getExpirationType() && !StringUtil.hasText(order.getExpirationDate())) {
            messages.add(createValidationMessage("No validity date supplied"));
        }

        if(!messages.isEmpty()) {
            response.setMSGType(OrderResultMSGType.OMT_ERROR);
            response.setCode(OrderResultCode.ORC_VALIDATE_ORDER_FAILED);
            callback.onFailure(new OrderMethods.PmxmlValidateOrderResponseException(response, order));
            return;
        }

        response.setMSGType(OrderResultMSGType.OMT_RESULT);
        response.setCode(OrderResultCode.ORC_OK);
        callback.onSuccess(response);
    }

    private ValidationMessage createValidationMessage(String message) {
        final ValidationMessage validationMessage = new ValidationMessage();
        validationMessage.setTyp(OrderValidationType.VT_INFO);
        validationMessage.setServerity(OrderValidationServerityType.VST_ERROR);
        validationMessage.setMsg(message);
        return validationMessage;
    }

    @Override
    public void queryOrderBook(OrderSession orderSession, GetOrderbookRequest request, AsyncCallback<GetOrderbookResponse> callback) {
        callback.onFailure(new UnsupportedOperationException("queryOrderBook is not supported for Fuchsbriefe!"));
    }

    private static OrderExchangeInfo newOrderExchangeInfo(String label, String iso, String id) {
        final OrderExchangeInfo info = new OrderExchangeInfo();
        info.setName(label);
        info.setISOCode(iso);
        info.setID(id);
        info.setExternExchangeTyp(ExternExchangeType.EET_DOMESTIC);
        return info;
    }

    @Override
    public void sendOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<SendOrderDataResponse> callback) {
        if(order == null) {
            Firebug.info("<OrderSessionMockFuchsbriefe.sendOrder> failure: OrderDataType is null");
            callback.onFailure(new OrderMethods.PmxmlOrderEntryResponseException(
                    "OrderDataType is null",
                    OrderResultCode.ORC_ERROR,
                    OrderResultMSGType.OMT_ERROR));
            return;
        }

        Firebug.info("<OrderSessionMockFuchsbriefe.sendOrder>");
        final SendOrderDataResponse response = new SendOrderDataResponse();
        response.setOrder(order);

        final String orderNumberRaw = Long.toHexString(System.currentTimeMillis()).toUpperCase();
        final String orderNumber = orderNumberRaw.substring(orderNumberRaw.length() - 5, orderNumberRaw.length() - 1);

        order.setOrderNumber(orderNumber);

        if(validationMessages != null) {
            response.getValidationMsgList().addAll(validationMessages);
        }

        callback.onSuccess(response);
    }

    @Override
    public void loginBroker(OrderSession orderSession, String user, String password, AsyncCallback<Void> callback) {
        Firebug.info("<OrderSessionMockFuchsbriefe.loginBroker>");
        callback.onSuccess(null);
    }

    @Override
    public boolean isModuleSupported(BrokerageModuleID id) {
        return BrokerageModuleID.BM_FUCHSBRIEFE.equals(id);
    }

    @Override
    public boolean isSessionHandled(OrderSession orderSession) {
        return MOCK_HANDLE_FUCHSBRIEFE.equals(orderSession.getHandle());
    }

    @Override
    public boolean isDepotIdHandled(String depotId) {
        return this.fuchsbriefeMockAllDepotIds || MOCK_DEPOT_ID.equals(depotId);
    }

    @Override
    public OrderMethods asOrderMethods() {
        return this;
    }

    /**
     * Creates an order and fills it with default values as it will be delivered by Delphi.
     */
    static class OrderDataTypeMock extends OrderDataType {
        private static final String ZERO = "0";

        public OrderDataTypeMock(OrderDataType other) {
            this();

            this.id = fill(this.id, other.getId());
            this.bmId = fill(this.id, other.getBMId());
            this.bm = other.getBM();
            this.activityId = fill(this.activityId, other.getActivityId());
            this.orderSuggestionRef = fill(this.orderSuggestionRef, other.getOrderSuggestionRef());
            this.externRef = fill(this.id, other.getExternRef());
            this.externRef2 = fill(this.id, other.getExternRef2());
            this.orderNumber = fill(this.id, other.getOrderNumber());
            this.typ = other.getTyp();
            isShort = other.isIsShort();
            this.inhaberData = fill(this.inhaberData, other.getInhaberData());
            this.depotData = fill(this.depotData, other.getDepotData());
            this.kontoData = fill(this.kontoData, other.getKontoData());
            this.debitAccount = fill(this.debitAccount, other.getDebitAccount());
            this.lagerstelleData = fill(this.lagerstelleData, other.getLagerstelleData());
            this.securityData = fill(this.securityData, other.getSecurityData());
            this.exchangeData = fill(this.exchangeData, other.getExchangeData());
            this.quantity = fill(this.quantity, other.getQuantity());
            this.isOrderInAmount = other.isIsOrderInAmount();
            this.orderAmount = fill(this.orderAmount, other.getOrderAmount());
            this.orderCurrencyData = fill(this.orderCurrencyData, other.getOrderCurrencyData());
            this.executionOption = other.getExecutionOption();
            this.executionOption = fillEnum(this.executionOption, other.getExecutionOption());
            this.expirationDate = fill(this.expirationDate, other.getExpirationDate());
            this.expirationType = fillEnum(this.expirationType, other.getExpirationType());
            this.stop = fill(this.stop, other.getStop());
            this.limit = fill(this.limit, other.getLimit());
            this.limitType = fillEnum(this.limitType, other.getLimitType());
            this.limitInfos = fill(this.limitInfos, other.getLimitInfos());
            this.marketMode = fillEnum(this.marketMode, other.getMarketMode());
            this.orderer = fill(this.orderer, other.getOrderer());
            this.bonusPayments = fill(this.bonusPayments, other.getBonusPayments());
            this.discount = fill(this.discount, other.getDiscount());
            this.prospectus = fill(this.prospectus, other.getProspectus());
            this.trailingDelta = fill(this.trailingDelta, other.getTrailingDelta());
            this.issueSurchargeKickback = fill(this.issueSurchargeKickback, other.getIssueSurchargeKickback());
            this.remark = fill(this.remark, other.getRemark());
            this.remark2 = fill(this.remark2, other.getRemark2());
        }

        public OrderDataTypeMock() {
            this.id = ZERO;
            this.bmId = Integer.toString(BrokerageModuleID.BM_FUCHSBRIEFE.ordinal());
            this.bm = BrokerageModuleID.BM_FUCHSBRIEFE;
            this.activityId = ZERO;
            this.orderSuggestionRef = "";
            this.externRef = ZERO;
            this.externRef2 = ZERO;
            this.orderNumber = ZERO;
            this.typ = OrderTransaktionType.TT_BUY;
            this.isShort = false;
            this.inhaberData = null;
            this.depotData = null;
            this.kontoData = null;
            this.debitAccount = "";
            this.lagerstelleData = null;
            this.securityData = null;
            this.exchangeData = null;
            this.quantity = ZERO;
            this.isOrderInAmount = false;
            this.orderAmount = ZERO;
            this.orderCurrencyData = null;
            this.executionOption = OrderExecutionOption.OEO_NA;
            this.expirationDate = "1899-12-30T00:00:00.000+01:00";
            this.expirationType = OrderExpirationType.OET_NA;
            this.limit = ZERO;
            this.stop = ZERO;
            this.limitType = OrderLimitType.OLT_NA;
            this.limitInfos = "";
            this.marketMode = MarketMode.OMM_NA;
            this.orderer = "";
            this.bonusPayments = ZERO;
            this.discount = ZERO;
            this.prospectus = ZERO;
            this.trailingDelta = ZERO;
            this.issueSurchargeKickback = ZERO;
            this.remark = "";
            this.remark2 = "";
        }
    }
}