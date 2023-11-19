/*
 * OrderSessionMockBHLKGS.java
 *
 * Created on 10.07.13 13:16
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OeRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSessionContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AccountInfo;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.AllocateOrderSessionDataResponse;
import de.marketmaker.iview.pmxml.AuthorizedRepresentative;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.CancelOrderResponse;
import de.marketmaker.iview.pmxml.ChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.ClearingData;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.DBBank;
import de.marketmaker.iview.pmxml.DataResponse;
import de.marketmaker.iview.pmxml.ExternExchangeType;
import de.marketmaker.iview.pmxml.GetIPOListResponse;
import de.marketmaker.iview.pmxml.GetOrderDetailsResponse;
import de.marketmaker.iview.pmxml.GetOrderbookRequest;
import de.marketmaker.iview.pmxml.GetOrderbookResponse;
import de.marketmaker.iview.pmxml.IPODataType;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.MarketMode;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderDataTypeBHL;
import de.marketmaker.iview.pmxml.OrderDepository;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderExecutionOption;
import de.marketmaker.iview.pmxml.OrderExpirationType;
import de.marketmaker.iview.pmxml.OrderLimitType;
import de.marketmaker.iview.pmxml.OrderLock;
import de.marketmaker.iview.pmxml.OrderResultCode;
import de.marketmaker.iview.pmxml.OrderResultMSGType;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderStatusBHL;
import de.marketmaker.iview.pmxml.OrderStock;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.OrderValidationServerityType;
import de.marketmaker.iview.pmxml.OrderValidationType;
import de.marketmaker.iview.pmxml.OrderbookDataTypeBHL;
import de.marketmaker.iview.pmxml.ProvisionType;
import de.marketmaker.iview.pmxml.SendOrderDataResponse;
import de.marketmaker.iview.pmxml.SessionState;
import de.marketmaker.iview.pmxml.ShellMMRef;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellMMTypeDesc;
import de.marketmaker.iview.pmxml.TextType;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.ValidateChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidateOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidationMessage;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock.MockUtil.fill;
import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock.MockUtil.fillEnum;

/**
 * Enable order entry mocks by setting the property orderEntry in your DevMmweb to mock.
 * Be aware that depot objects with specific object IDs must exist in your pm database.
 * <code>&lt;set-property name=&quot;orderEntry&quot; value=&quot;mock&quot;/&lt;</code>
 * </code>&lt;set-property name=&quot;orderEntry&quot; value=&quot;prod&quot;/&lt;</code>
 *
 * @author Markus Dick
 */
@NonNLS
public class OrderMethodsMockBHLKGS extends OrderMethods implements OrderMethodsMockInterface {
    public static final HashMap<String, AccountRef> MOCKED_DEPOT_REFS;
    public static final Set<String> MOCKED_DEPOT_IDS;
    public static final Set<String> ISINS_IN_STOCK;

    public static final String PASSWORD_AS = "AS";

    public static final String ISIN_MOCK_STOCK = "XX2300000005";
    public static final String ISIN_MOCK_BOND = "XX2350000004";
    public static final String ISIN_MOCK_STOCK_CUSTOM_PM_IID = "XX2354200006";
    public static final String ISIN_MOCK_STOCK_IID_DOES_NOT_EXIST = "XX2354260000";
    public static final String ISIN_IPO_MOCK_SECURITY_FIRST_LIST_ENTRY = "XXIPO0000001";
    public static final String ISIN_ORC_SECURITY_NOT_FOUND_WITH_EXTERNAL_MESSAGE = "DE4200000009";

    private static final String DELPHI_ZERO_DATE = "1899-12-30T00:00:00.000+01:00";

    private static final String MM_SECURITY_ID_STOCK = "90577300"; //wkn: BASF11
    private static final String MM_SECURITY_ID_STOCK_PM = "&123456789"; //arbitrageButton should not be shown
    private static final String MM_SECURITY_ID_STOCK_IID_DOES_NOT_EXIST = "1234567890"; //arbitrageButton should not be shown because iid does not exist
    private static final String MM_SECURITY_ID_BOND = "43006011"; //isin: XS0412154378 wkn A0T6EG A0T6EG

    //Session properties that control mock behaviour, activate in AS search box with e.g.
    // +prop:oeValidateFailsAfterWarns=true
    private static final String OE_VALIDATE_FAILS_AFTER_WARNS = "oeValidateFailsAfterWarns";
    private static final String OE_DELETE_WARNS = "oeDeleteWarns";
    private static final String OE_DELETE_FAILS = "oeDeleteFails";
    public static final String STATUS_096_GESTRICHEN = "096# gestrichen";

    private static boolean authenticated = false;
    private static String brokingUser = null;

    public static long timeMillis = System.currentTimeMillis();
    public static long lastOrderNumber = 10000;

    public final static String MOCK_HANDLE_BANKHAUS_LAMPE_KGS = "MOCK_HANDLE_BANKHAUS_LAMPE_KGS";

    static {
        MOCKED_DEPOT_REFS = new HashMap<String, AccountRef>();

        final AccountRef depotRef1 = new AccountRef();
        depotRef1.setId("24084775");
        depotRef1.setName("Test BHL (as/pm[web])");
        depotRef1.setNumber("AS-DEP-BHL-01");
        depotRef1.setBank(newMeDBank());
        depotRef1.setCurrency(CurrencyMock.EUR);
        MOCKED_DEPOT_REFS.put(depotRef1.getId(), depotRef1);

        final AccountRef depotRef2 = new AccountRef();
        depotRef2.setId("27039958");
        depotRef2.setName("Test BHL (as/pm[web]) deaktiviert");
        depotRef2.setNumber("AS-DEP-BHL-02");
        depotRef2.setBank(newMeDBank());
        depotRef2.setCurrency(CurrencyMock.HKD);
        MOCKED_DEPOT_REFS.put(depotRef2.getId(), depotRef2);

        final AccountRef depotRef3 = new AccountRef();
        depotRef3.setId("28362741");
        depotRef3.setName("Test BHL (as/pm[web]) 3");
        depotRef3.setNumber("AS-DEP-BHL-03");
        depotRef3.setBank(newMeDBank());
        depotRef3.setCurrency(CurrencyMock.EUR);
        MOCKED_DEPOT_REFS.put(depotRef3.getId(), depotRef3);

        final AccountRef depotRef4 = new AccountRef();
        depotRef4.setId("64322");
        depotRef4.setName("Test BHL KGS override");
        depotRef4.setNumber("AS-DEP-BHL-04");
        depotRef4.setBank(newMeDBank());
        depotRef4.setCurrency(CurrencyMock.HKD);
        MOCKED_DEPOT_REFS.put(depotRef4.getId(), depotRef4);

        MOCKED_DEPOT_IDS = MOCKED_DEPOT_REFS.keySet();

        final List<OrderSecurityInfo> stock = fillSecurities(new ArrayList<OrderSecurityInfo>());
        ISINS_IN_STOCK = new HashSet<String>();
        for(OrderSecurityInfo osi : stock) {
            ISINS_IN_STOCK.add(osi.getISIN());
        }
    }

    private final List<OrderDataTypeBHL> orders = new ArrayList<OrderDataTypeBHL>();

    public OrderMethodsMockBHLKGS() {
        if(this.orders.isEmpty()) {
            addTenOrders(this.orders);
            addTenOrders(this.orders);
            addTenOrders(this.orders);
            addTenOrders(this.orders);
            addTenOrders(this.orders);
            addTenOrders(this.orders);
            addTenOrders(this.orders);
            addTenOrders(this.orders);
            addTenOrders(this.orders);
        }
    }

    @Override
    public boolean isModuleSupported(BrokerageModuleID id) {
        return BrokerageModuleID.BM_BHLKGS.equals(id);
    }

    @Override
    public boolean isSessionHandled(OrderSession orderSession) {
        return MOCK_HANDLE_BANKHAUS_LAMPE_KGS.equals(orderSession.getHandle());
    }

    @Override
    public boolean isDepotIdHandled(String depotId) {
        return MOCKED_DEPOT_IDS.contains(depotId);
    }

    @Override
    public OrderMethods asOrderMethods() {
        return this;
    }

    @Override
    public void allocateOrderSession(OrderSessionContainer containerHandle, String depotId, AsyncCallback<OrderSession> callback) {
        Firebug.info("<OrderSessionMockBHLKGS.create>");

        final ShellMMRef ownerRef = new ShellMMRef();
        ownerRef.setId("24084772");
        ownerRef.setName("Test BHL (as/pm[web])");
        ownerRef.setNumber("AS-INH-BHL-01");

        final ShellMMRef portfolioRef = new ShellMMRef();
        portfolioRef.setId("24084773");
        portfolioRef.setName("Test BHL (as/pm[web])");
        portfolioRef.setNumber("AS-PTF-BHL-01");

        final DBBank dbBank = new DBBank();
        dbBank.setBankname("Bankaus Lampe KG");
        dbBank.setBrokerageModuleID(BrokerageModuleID.BM_BHLKGS.value());
        dbBank.setBLZ("48020151");
        dbBank.setBIC("LAMPDEDD");

        final AccountData accountData = new AccountData();
        accountData.setId("24084774");
        accountData.setName("Test BHL (as/pm[web])");
        accountData.setNumber("AS-KTO-BHL-01");
        accountData.setBank(dbBank);
        accountData.setBalance("42000.0");
        accountData.setCurrency(CurrencyMock.EUR);

        final AccountInfo accountInfo = new AccountInfo();
        accountInfo.setOwner(ownerRef);
        accountInfo.setPortfolio(portfolioRef);
        accountInfo.setSecurityAccount(MOCKED_DEPOT_REFS.get(depotId));
        accountInfo.getAccountList().add(accountData);
        fillSecurities(accountInfo.getSecurityList());

        final OrderSessionFeaturesDescriptorBHL sessionFeaturesDescriptor = new OrderSessionFeaturesDescriptorMockBHLKGS();
        sessionFeaturesDescriptor.setApplicationName("OrderMethodsMockBHLKGS");
        if(authenticated) {
            for(OrderActionType type : new OrderActionType[] { OrderActionType.AT_BUY, OrderActionType.AT_SELL, OrderActionType.AT_SUBSCRIBE } ) {
                OrderAction orderAction = new OrderAction();
                orderAction.setValue(type);
                sessionFeaturesDescriptor.getOrderActions().add(orderAction);
            }

            for(ShellMMType type : new ShellMMType[] { ShellMMType.ST_AKTIE, ShellMMType.ST_ANLEIHE, ShellMMType.ST_FOND} ) {
                ShellMMTypeDesc desc = new ShellMMTypeDesc();
                desc.setT(type);
                sessionFeaturesDescriptor.getTradableSecurityTypes().add(desc);
            }
        }

        final AllocateOrderSessionDataResponse response = new AllocateOrderSessionDataResponse();
        response.setBrokerageModul(BrokerageModuleID.BM_BHLKGS);
        response.setAccountInfo(accountInfo);
        response.setFeatures(sessionFeaturesDescriptor);
        if(authenticated) {
            response.setOrderSessionState(SessionState.SS_AUTHENTICATED);
        }
        else {
            response.setOrderSessionState(SessionState.SS_INITIALISED);
        }
        response.setHandle(MOCK_HANDLE_BANKHAUS_LAMPE_KGS);

        callback.onSuccess(new OrderSession.OrderSessionBHLKGS(response));
    }

    @Override
    public void closeOrderSession(OrderSession orderSession, AsyncCallback<OrderSession> callback) {
        Firebug.info("<OrderSessionMockBHLKGS.closeOrderSession> this mock impl. does nothing!");
    }

    private static List<OrderSecurityInfo> fillSecurities(List<OrderSecurityInfo> securityList) {
        securityList.add(newOrderSecurityInfo(ISIN_MOCK_STOCK));
        securityList.add(newOrderSecurityInfo(ISIN_MOCK_BOND));
        securityList.add(newOrderSecurityInfo(ISIN_MOCK_STOCK_CUSTOM_PM_IID));
        securityList.add(newOrderSecurityInfo(ISIN_MOCK_STOCK_IID_DOES_NOT_EXIST));
        securityList.add(newOrderSecurityInfo(ISIN_IPO_MOCK_SECURITY_FIRST_LIST_ENTRY));
        securityList.add(newOrderSecurityInfo(ISIN_ORC_SECURITY_NOT_FOUND_WITH_EXTERNAL_MESSAGE));
        securityList.add(newOrderSecurityInfo("XX2354268003"));
        return securityList;
    }

    @Override
    public void lookupSecurity(OrderSession orderSession, OrderTransaktionType type, String isin, AsyncCallback<LookupSecurityDataResponse> callback) {
        Firebug.info("<OrderSessionMockBHLKGS.lookupSecurity> isin=" + isin);
        Firebug.info("<OrderSessionMockBHLKGS.lookupSecurity> other ISINs: " +
                ISIN_ORC_SECURITY_NOT_FOUND_WITH_EXTERNAL_MESSAGE + " throws ORC_SECURITY_NOT_FOUND (with externalMessage), " +
                ISIN_MOCK_STOCK + " Stock (order in units), " +
                ISIN_MOCK_BOND + " Bond (order in amount)");

        if(ISIN_ORC_SECURITY_NOT_FOUND_WITH_EXTERNAL_MESSAGE.equals(isin)) {
            callback.onFailure(
                    new OrderMethods.PmxmlOrderEntryResponseException(
                            "Mocked internal pm message: Security " + ISIN_ORC_SECURITY_NOT_FOUND_WITH_EXTERNAL_MESSAGE + " not found!",
                            OrderResultCode.ORC_SECURITY_NOT_FOUND,
                            OrderResultMSGType.OMT_ERROR,
                            "[ASMOCK] OrderAnlegen.IO_SOOOWKN: WKN NICHT VORHANDEN")); //external K-GS message
            return;
        }

        if(OrderTransaktionType.TT_SELL.equals(type) && !ISINS_IN_STOCK.contains(isin)) {
            callback.onFailure(
                    new OrderMethods.PmxmlOrderEntryResponseException(
                            "Mocked internal pm message: Security with " + isin + " is not in stock",
                            OrderResultCode.ORC_NO_STOCK,
                            OrderResultMSGType.OMT_ERROR,
                            "[ASMOCK] SachdepotBestandBlaetternV01.IO_SSDBWKN: KEIN BESTAND VORHANDEN")); //external K-GS message
            return;
        }

        final LookupSecurityInfo securityInfo = newLookupSecurityInfo(type, isin);
        final OrderSecurityFeatureDescriptorBHL features = newOrderSecurityFeatureDescriptorBHL(securityInfo.getSecurity().isISQuotedPerUnit());

        final LookupSecurityDataResponse response = new LookupSecurityDataResponse();
        response.setFeatures(features);
        response.setSecurityInfo(securityInfo);

        callback.onSuccess(response);
    }

    @Override
    public void getIPOList(OrderSession orderSession, AsyncCallback<GetIPOListResponse> callback) {
        final NumberFormat wknFormat = NumberFormat.getFormat("000000");
        final NumberFormat isinFormat = NumberFormat.getFormat("XXIPO0000000");

        final GetIPOListResponse response = new GetIPOListResponse();
        final List<IPODataType> list = response.getIPOList();

        list.add(newIpoDataType(wknFormat.format(1), isinFormat.format(1), "AdvisorySolution One AG NA IPO with a very long instrument name"));
        for(int i = 2; i < 1000; i++) {
            list.add(newIpoDataType(wknFormat.format(i), isinFormat.format(i), "AdvisorySolution" + i + " AG NA IPO"));
        }

        callback.onSuccess(response);
    }

    private IPODataType newIpoDataType(String wkn, String isin, String name) {
        final IPODataType ipoDataType = new IPODataType();
        ipoDataType.setWKN(wkn);
        ipoDataType.setISIN(isin);
        ipoDataType.setSecurityName(name);
        return ipoDataType;
    }

    private OrderSecurityFeatureDescriptorBHL newOrderSecurityFeatureDescriptorBHL(boolean isQuotedPerUnit) {
        final OrderSecurityFeatureDescriptorBHL orderSecurityFeatureDescriptor = new OrderSecurityFeatureDescriptorBHL();

        if(isQuotedPerUnit) {
            orderSecurityFeatureDescriptor.setDepotCurrencyData(CurrencyMock.OOODEPWHR_ST);
        }
        else {
            orderSecurityFeatureDescriptor.setDepotCurrencyData(CurrencyMock.EUR);
        }

        orderSecurityFeatureDescriptor.setExchangeIsMandatory(true);
        orderSecurityFeatureDescriptor.setAuthorizedRepresentativesFlag("A");

        orderSecurityFeatureDescriptor.getAuthorizedRepresentatives().addAll(Arrays.asList(
                newAuthorizedRepresentative("D", "1022228", "Depotinhaber, Manfred", "Depotinhaber"),
                newAuthorizedRepresentative("B", "1022229", "Verfügungsberechtigte", "Verfügungsberechtigter"),
                newAuthorizedRepresentative("B", "1202006", "Gräfin Egon HB-Berechtig", "HB-Berechtigter"),
                newAuthorizedRepresentative("B", "1334000", "Prinzessin Maisel-Verfüg", "Verfügungsberechtigter"),
                newAuthorizedRepresentative("B", "1335014", "Freiherr Landruth Verfüg", "Verfügungsberechtigter"),
                newAuthorizedRepresentative("B", "10333444", "Kontomitinhaberle, Heinz", "Kontomitinhaber"),
                newAuthorizedRepresentative("B", "1000000710", "Dispoberechtigt, Ohnefeh", "Verfügungsberechtigter")
        ));

        return orderSecurityFeatureDescriptor;
    }

    private AuthorizedRepresentative newAuthorizedRepresentative(String id, String number, String name, String authorization) {
        final AuthorizedRepresentative a = new AuthorizedRepresentative();
        a.setOrdererIdentifier(id);
        a.setNumber(number);
        a.setName(name);
        a.setAuthorization(authorization);
        return a;
    }

    private LookupSecurityInfo newLookupSecurityInfo(OrderTransaktionType type, String isin) {
        final OrderSecurityInfo orderSecurityInfo = newOrderSecurityInfo(isin);

        final LookupSecurityInfo lookupSecurityInfo = new LookupSecurityInfo();
        lookupSecurityInfo.setSecurity(orderSecurityInfo);

        //nur eine Auswahl, tatsächliche Währungen unklar.
        lookupSecurityInfo.getCurrencyList().addAll(Arrays.asList(
                CurrencyMock.XXE_CA,
                CurrencyMock.EUR_CA_DEFAULT,
                CurrencyMock.USD_CA,
                CurrencyMock.CHF_CA,
                CurrencyMock.HKD_CA
        ));

        final List<OrderStock> orderStocks = lookupSecurityInfo.getStockBalance();
        orderStocks.addAll(Arrays.asList(
                newOrderStock("1", "100100001", null, "42", "23"),
                newOrderStock("2", "100100002", null, "23", "5"),
                newOrderStock("3", "100100003", "KREDIT", "51", "51")
        ));

        final List<OrderExchangeInfo> orderExchangeInfos = lookupSecurityInfo.getExchangeList();
        //nur eine kleine Auswahl, tatsächliche Plätze unklar.
        orderExchangeInfos.addAll(Arrays.asList(
                newOrderExchangeInfoDomestic("Xetra", "EDE", "194"),
                newOrderExchangeInfoDomestic("Berlin-Bremen", "EDB", "100"),
                newOrderExchangeInfoDomestic("Düsseldorf", "EDD", "120"),
                newOrderExchangeInfoDomestic("Frankfurt", "EDF", "130"),
                newOrderExchangeInfoDomestic("Hamburg", "EDH", "140"),
                newOrderExchangeInfoDomestic("Hannover", "EDI", "150"),
                newOrderExchangeInfoDomestic("München", "EDM", "160"),
                newOrderExchangeInfoDomestic("Stuttgart", "EDS", "170")
        ));

        //nur eine kleine Auswahl, tatsächliche Plätze unklar.
        orderExchangeInfos.addAll(Arrays.asList(
                newOrderExchangeInfoForeign("Amsterdam", "ENA", "200"),
                newOrderExchangeInfoForeign("Athen", "EGA", "230"),
                newOrderExchangeInfoForeign("Bangkok", "FTB", "235"),
                newOrderExchangeInfoForeign("Barcelona", "EEB", "240"),
                newOrderExchangeInfoForeign("Bilbao", "EEC", "270"),
                newOrderExchangeInfoForeign("Bogota", "LKB", "280"),
                newOrderExchangeInfoForeign("Boston", "NAB", "289")
        ));

        //nur eine kleine Auswahl, tatsächliche Plätze unklar.
        orderExchangeInfos.addAll(Arrays.asList(
                newOrderExchangeInfoOther("Außerbörslich Ausland", "ADI", "978"),
                newOrderExchangeInfoOther("Aus. Direktgeschäft", "DAU", "976"),
                newOrderExchangeInfoOther("Aus. Investmentg.", "DAI", "977"),
                newOrderExchangeInfoOther("Inl. Direktgeschäft", "DIR", "970"),
                newOrderExchangeInfoOther("Inl. Investmentgeschäft", "DII", "974", OrderTransaktionType.TT_SUBSCRIBE == type),
                newOrderExchangeInfoOther("Festpreisgeschäft", "DIF", "971")
        ));
        return lookupSecurityInfo;
    }

    private static OrderSecurityInfo newOrderSecurityInfo(String isin) {
        final OrderSecurityInfo orderSecurityInfo = new OrderSecurityInfo();
        orderSecurityInfo.setKursfaktor("1.0");
        orderSecurityInfo.setWKN("ASMOCK");
        orderSecurityInfo.setNumber(orderSecurityInfo.getWKN());
        orderSecurityInfo.setISIN(isin);

        if(ISIN_MOCK_BOND.equals(isin)) {
            orderSecurityInfo.setBezeichnung("MOCK BOND");
            orderSecurityInfo.setISQuotedPerUnit(false);
            orderSecurityInfo.setTyp(ShellMMType.ST_ANLEIHE);
            orderSecurityInfo.setId("2354");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_BOND);
            orderSecurityInfo.setStock(Double.toString(10000d));
        }
        else if(ISIN_MOCK_STOCK_CUSTOM_PM_IID.equals(isin)) {
            orderSecurityInfo.setBezeichnung("MOCK STOCK CUSTOM PM IID");
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("23542");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_STOCK_PM);
            orderSecurityInfo.setStock(Double.toString(23542d));
        }
        else if(ISIN_MOCK_STOCK_IID_DOES_NOT_EXIST.equals(isin)) {
            orderSecurityInfo.setBezeichnung("MOCK STOCK IID DOES NOT EXIST");
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("235426");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_STOCK_IID_DOES_NOT_EXIST);
            orderSecurityInfo.setStock(Double.toString(235426d));
        }
        else if(ISIN_IPO_MOCK_SECURITY_FIRST_LIST_ENTRY.equals(isin)) {
            orderSecurityInfo.setBezeichnung("Advisory Solution 1 AG IPO with a very long security name to check " +
                    "ellipsis of IPO instrument name label");
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("2354268");
            orderSecurityInfo.setMMSecurityID(null);
            orderSecurityInfo.setStock(Double.toString(235426d));
        }
        else if(ISIN_ORC_SECURITY_NOT_FOUND_WITH_EXTERNAL_MESSAGE.equals(isin)) {
            orderSecurityInfo.setBezeichnung("MOCK STOCK ORC_SECURITY_NOT_FOUND WITH EXTERNAL MESSAGE");
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("235426814");
            orderSecurityInfo.setMMSecurityID(null);
            orderSecurityInfo.setStock(Double.toString(1d));
        }
        else {
            orderSecurityInfo.setBezeichnung("DEFAULT MOCK STOCK");
            orderSecurityInfo.setISQuotedPerUnit(true);
            orderSecurityInfo.setTyp(ShellMMType.ST_AKTIE);
            orderSecurityInfo.setId("235");
            orderSecurityInfo.setMMSecurityID(MM_SECURITY_ID_STOCK);
            orderSecurityInfo.setStock(Double.toString(1d));
        }
        return orderSecurityInfo;
    }

    private OrderStock newOrderStock(String id, String depository, String lock, String quantity, String quantityToSell) {
        final String stockString = lock != null ? depository + " (" + lock + ")" : depository;

        final OrderStock orderStock = new OrderStock();
        orderStock.setStockID(id);
        orderStock.setStockString(stockString);
        orderStock.setQuantity(quantity);
        orderStock.setQuantityToSell(quantityToSell);
        orderStock.setDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(newDay()));

        orderStock.setDepositoryData(newOrderDepository(id, depository));

        if(StringUtil.hasText(lock)) {
            final OrderLock orderLock = new OrderLock();
            orderLock.setId(id);
            orderLock.setBez(lock);
            orderLock.setKuerzel(lock);
            orderStock.setLockData(orderLock);
        }

        return orderStock;
    }

    private OrderDepository newOrderDepository(String id, String depository) {
        final OrderDepository orderDepository = new OrderDepository();
        orderDepository.setId(id);
        orderDepository.setBez(depository);
        orderDepository.setKuerzel(depository);
        return orderDepository;
    }

    @Override
    public void validateOrder(OrderSession orderSession, OrderDataType orderRaw, List<ValidationMessage> validationMessages, AsyncCallback<ValidateOrderDataResponse> callback) {
        Firebug.info("<OrderSessionMockBHLKGS.validateOrder>");

        final OrderDataTypeBHL order = (OrderDataTypeBHL) orderRaw;

        final ValidateOrderDataResponse response = new ValidateOrderDataResponse();
        response.setOrder(new OrderDataTypeBHLMock(order));

        final List<ValidationMessage> messages = response.getValidationMsgList();
        if(!doValidateOrder(validationMessages, order, response, messages)) {
            callback.onFailure(new OrderMethods.PmxmlValidateOrderResponseException(response, order));
            return;
        }

        response.setMSGType(OrderResultMSGType.OMT_RESULT);
        response.setCode(OrderResultCode.ORC_OK);
        callback.onSuccess(response);
    }

    private boolean doValidateOrder(List<ValidationMessage> validationMessages, OrderDataTypeBHL order, DataResponse response, List<ValidationMessage> messages) {
        if(SessionData.INSTANCE.isUserPropertyTrue(OE_VALIDATE_FAILS_AFTER_WARNS)) {
            if(validationMessages == null) {
                validationMessages = new ArrayList<ValidationMessage>();
            }
            //add the initial validation message if the message list is empty
            if(validationMessages.isEmpty()) {
                final ValidationMessage vm1 = createValidationMessage(OrderValidationServerityType.VST_QUESTION,
                        "Answer this first message and the following messages with YES and you will get a failure!");
                vm1.setAnswer(ThreeValueBoolean.TV_NULL);
                messages.add(vm1);

                for(int i = 0; i < 3; i++) {
                    final ValidationMessage vm2 = createValidationMessage(OrderValidationServerityType.VST_QUESTION,
                            "Answer this message and all preceding and succeeding messages with YES and you will get a failure!!");
                    vm2.setAnswer(ThreeValueBoolean.TV_NULL);
                    messages.add(vm2);
                }
            }
            else {
                //check that all responded messages are answered with yes,
                //if not send the requested messages back to the user
                boolean oneFalse = false;
                for(ValidationMessage vm : validationMessages) {
                    oneFalse |= ThreeValueBoolean.TV_FALSE.equals(vm.getAnswer())
                            || ThreeValueBoolean.TV_NULL.equals(vm.getAnswer());
                    messages.add(vm);
                }
                //answers already given by user, send failure message as expected!
                if(!oneFalse) {
                    messages.clear();
                    messages.add(createValidationErrorMessage("Here it is!"));
                    response.setMSGType(OrderResultMSGType.OMT_ERROR);
                }
            }
        }
        else {
            if(order.getTyp() == null) {
                messages.add(createValidationErrorMessage("Transaction type not set"));
            }
            if(order.getKontoData() == null) {
                messages.add(createValidationErrorMessage("Account not selected"));
            }
            if(order.getSecurityData() == null) {
                messages.add(createValidationErrorMessage("No security selected"));
            }
            if(order.getExchangeData() == null) {
                messages.add(createValidationErrorMessage("No exchange selected"));
            }
            if(!order.isIsOrderInAmount() && !StringUtil.hasText(order.getQuantity())) {
                messages.add(createValidationErrorMessage("Quantity not set"));
            }
            if(order.isIsOrderInAmount() && !StringUtil.hasText(order.getOrderAmount())) {
                messages.add(createValidationErrorMessage("Amount not set"));
            }
            if(StringUtil.hasText(order.getLimit()) && order.getLimitCurrencyData() == null) {
                messages.add(createValidationErrorMessage("No currency for limit selected"));
            }
            if(OrderExpirationType.OET_DATE == order.getExpirationType() && !StringUtil.hasText(order.getExpirationDate())) {
                messages.add(createValidationErrorMessage("No validity date supplied"));
            }
            if(("02".equals(order.getLimitOptions()) ||
                    "03".equals(order.getLimitOptions())) &&
                    StringUtil.hasText(order.getStop()) && !StringUtil.hasText(order.getLimit())) {
                messages.add(createValidationErrorMessage("Order limit option " + order.getLimitOptions() + " must not have a stop value but requires a limit!"));
            }
            if(!StringUtil.hasText(order.getSettlementType())) {
                messages.add(createValidationErrorMessage("Settlement type not selected"));
            }
            if(!StringUtil.hasText(order.getBusinessSegment())) {
                messages.add(createValidationErrorMessage("Business segment type not selected"));
            }
            if(!StringUtil.hasText(order.getOrderer())) {
                messages.add(createValidationErrorMessage("Orderer not selected"));
            }
            if("B".equals(order.getOrderer()) || "D".equals(order.getOrderer())) {
                if(!StringUtil.hasText(order.getOrdererIdentifier())) {
                    messages.add(createValidationErrorMessage("Orderer Name not set"));
                }
                if(!StringUtil.hasText(order.getOrdererCustomerNumber())) {
                    messages.add(createValidationErrorMessage("Orderer Customer Number not set"));
                }
            }
        }

        if(!messages.isEmpty()) {
            response.setMSGType(OrderResultMSGType.OMT_ERROR);
            response.setCode(OrderResultCode.ORC_VALIDATE_ORDER_FAILED);
            return false;
        }

        return true;
    }

    @Override
    public void validateChangeOrder(OrderSession orderSession, OrderDataType orderRaw, List<ValidationMessage> validationMessages, AsyncCallback<ValidateChangeOrderDataResponse> callback) {
        Firebug.info("<OrderSessionMockBHLKGS.validateChangeOrder>");

        final OrderDataTypeBHL order = (OrderDataTypeBHL) orderRaw;

        final ValidateChangeOrderDataResponse response = new ValidateChangeOrderDataResponse();
        response.setOrder(new OrderDataTypeBHLMock(order));

        final List<ValidationMessage> messages = response.getValidationMsgList();
        if(!doValidateOrder(validationMessages, order, response, messages)) {
            callback.onFailure(new OrderMethods.PmxmlValidateOrderResponseException(response, order));
            return;
        }

        response.setMSGType(OrderResultMSGType.OMT_RESULT);
        response.setCode(OrderResultCode.ORC_OK);
        callback.onSuccess(response);
    }

    private ValidationMessage createValidationMessage(OrderValidationServerityType type, String message) {
        final ValidationMessage validationMessage = new ValidationMessage();
        validationMessage.setTyp(OrderValidationType.VT_INFO);
        validationMessage.setServerity(type);
        validationMessage.setMsg(message);
        return validationMessage;
    }

    private ValidationMessage createValidationErrorMessage(String message) {
        return createValidationMessage(OrderValidationServerityType.VST_ERROR, message);
    }

    @Override
    public void queryOrderBook(OrderSession orderSession, GetOrderbookRequest request, final AsyncCallback<GetOrderbookResponse> callback) {
        Firebug.debug("<OrderSessionMockBHLKGS.queryOrderBook>");
        final GetOrderbookResponse result = new GetOrderbookResponse();
        List<OrderbookDataTypeBHL> entries = createOrderBookDataTypesBHL(orderSession.getSecurityAccount().getNumber(), orderSession.getOwner().getName());
        result.getOrders().addAll(entries);
        result.setTotalCount(Integer.toString(entries.size()));

        Timer t = new Timer() {
            @Override
            public void run() {
                callback.onSuccess(result);
            }
        };

        t.schedule(1000);
    }

    @Override
    public void cancelOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<CancelOrderResponse> callback) {
        final CancelOrderResponse response = new CancelOrderResponse();
        response.setOrder(order);

        final int indexOfOrder = findOrder(order.getOrderNumber(), callback);
        if(0 > indexOfOrder) {
            return;
        }

        if(SessionData.INSTANCE.isUserPropertyTrue(OE_DELETE_WARNS)) {
            final String msg = "Stet clita kasd gubergren, no sea takimata sanctus est nonumy eirmod magna sit amet.";

            if(validationMessages != null
                    && !validationMessages.isEmpty()
                    && msg.equals(validationMessages.get(0).getMsg())
                    && ThreeValueBoolean.TV_TRUE.equals(validationMessages.get(0).getAnswer())) {

                response.setMSGType(OrderResultMSGType.OMT_RESULT);
                response.setCode(OrderResultCode.ORC_OK);

                callback.onSuccess(response);
                return;
            }

            response.setMSGType(OrderResultMSGType.OMT_ERROR);
            response.setCode(OrderResultCode.ORC_VALIDATE_ORDER_FAILED);
            response.setDescription("Vel illum dolore eu feugiat nulla facilisis.");

            final ValidationMessage message = new ValidationMessage();
            message.setMsg(msg);
            message.setTyp(OrderValidationType.VST_KGS_3);
            message.setServerity(OrderValidationServerityType.VST_QUESTION);
            message.setAnswer(null);
            response.getValidationMsgList().add(message);

            callback.onFailure(new OrderMethods.PmxmlValidateOrderResponseException(response, order));
        }
        else if(SessionData.INSTANCE.isUserPropertyTrue(OE_DELETE_FAILS)) {
            response.setMSGType(OrderResultMSGType.OMT_ERROR);
            response.setCode(OrderResultCode.ORC_ERROR);
            response.setDescription("Ut wisi enim ad minim veniam ex ea commodo consequat.");

            callback.onFailure(new OrderMethods.PmxmlOrderEntryResponseException(response));
        }
        else {
            final OrderDataTypeBHL o = (OrderDataTypeBHL)order;

            if(!incrementChangeNumber(o, this.orders.get(indexOfOrder), callback)) {
                return;
            }

            o.setStatus096(newOrderStatus(STATUS_096_GESTRICHEN, Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()), brokingUser));
            o.setIsChangeAllowed(false);
            o.setIsDeleteAllowed(false);

            this.orders.set(indexOfOrder, o);

            response.setMSGType(OrderResultMSGType.OMT_RESULT);
            response.setCode(OrderResultCode.ORC_OK);

            callback.onSuccess(response);
        }
    }

    @Override
    public void getOrderDetails(final OrderSession orderSession, final String orderNumber, final AsyncCallback<GetOrderDetailsResponse> callback) {
        for(final OrderDataTypeBHL order : this.orders) {
            if(orderNumber.equals(order.getOrderNumber())) {
                final LookupSecurityInfo lookupSecurityInfo = newLookupSecurityInfo(order.getTyp(), order.getSecurityData().getISIN());
                final OrderSecurityFeatureDescriptorBHL securityFeatures = newOrderSecurityFeatureDescriptorBHL(order.isIsQuotedPerUnit());

                final GetOrderDetailsResponse response = new GetOrderDetailsResponse();
                Firebug.info("<OrderMethodsMockBHLKGS.getOrderDetails> orderNumber=" + orderNumber + " changeNumber=" + order.getChangeNumber());
                response.setOrder(order);
                response.setSecurityInfo(lookupSecurityInfo);
                response.setFeatures(securityFeatures);
                response.setCode(OrderResultCode.ORC_OK);

                Timer t = new Timer() {
                    @Override
                    public void run() {
                        callback.onSuccess(response);
                    }
                };

                t.schedule(750);
                return;
            }
        }

        final GetOrderDetailsResponse response = new GetOrderDetailsResponse();
        response.setCode(OrderResultCode.ORC_GET_ORDER_DETAIL_FAILED);
        response.setExternalMessage("Mocked order " + orderNumber + " not found!");
        callback.onSuccess(response);
    }

    private OrderDataTypeBHL newOrderDataTypeBHL(String orderNumber, OrderTransaktionType type, LookupSecurityInfo lookupSecurityInfo, OrderSecurityFeatureDescriptorBHL securityFeatures, double quantity) {
        final OrderDataTypeBHL o = new OrderDataTypeBHL();
        o.setBM(BrokerageModuleID.BM_BHLKGS);
        o.setOrderNumber(orderNumber);
        o.setChangeNumber(Integer.toString(0));
        o.setTyp(type);
        ShellMMRef investorData = new ShellMMRef();
        investorData.setId("24084772");
        investorData.setNumber("AS-INH-BHL-01");
        investorData.setName("Test BHL (as/pm[web])");
        o.setInhaberData(investorData);

        final AccountRef depotData = new AccountRef();
        depotData.setId("24084775");
        depotData.setNumber("AS-DEP-BHL-01");
        depotData.setName("Test BHL (as/pm[web])");
        depotData.setBank(newMeDBank());
        o.setDepotData(depotData);

        final AccountRef kontoData = new AccountRef();
        kontoData.setId("24084774");
        kontoData.setNumber("AS-KTO-BHL-01");
        kontoData.setName("Test BHL (as/pm[web])");
        kontoData.setCurrency(CurrencyMock.EUR);
        kontoData.setBank(newMeDBank());
        o.setKontoData(kontoData);

        final OrderSecurityInfo securityData = lookupSecurityInfo.getSecurity();
        o.setSecurityData(securityData);

        if(securityData.isISQuotedPerUnit()) {
            o.setDepotCurrencyData(CurrencyMock.OOODEPWHR_ST);
        }
        else {
            final CurrencyAnnotated defaultCurrency = OrderUtils.findDefaultCurrencyAnnotated(lookupSecurityInfo.getCurrencyList());
            if(defaultCurrency != null) {
                o.setDepotCurrencyData(defaultCurrency.getCurrency());
            }
        }

        if(OrderTransaktionType.TT_SELL == type) {
            o.setLagerstelleData(newOrderStock("2", "100100002", null, Double.toString(quantity), Double.toString(quantity)).getDepositoryData());
        }

        final OrderExchangeInfo orderExchangeInfo = new OrderExchangeInfo();
        orderExchangeInfo.setID("130");
        orderExchangeInfo.setName("Frankfurt");
        orderExchangeInfo.setISOCode("EDF");
        orderExchangeInfo.setExternExchangeTyp(ExternExchangeType.EET_DOMESTIC);
        o.setExchangeData(orderExchangeInfo);

        o.setTradingIdentifier("E");
        o.setExchangeCustomerRequest(true);
        o.setIsQuotedPerUnit(securityData.isISQuotedPerUnit());
        o.setDepotCurrencyData(securityFeatures.getDepotCurrencyData());
        o.setOrderAmount(null);
        o.setQuantity(Double.toString(quantity));
        o.setOrderCurrencyData(kontoData.getCurrency());

        o.setExpirationType(OrderExpirationType.OET_DATE);
        o.setExpirationDate("2013-10-22T14:05:26.000+01:00");
        o.setLimit("76.15");
        o.setLimitCurrencyData(CurrencyMock.EUR);
        o.setLimitOptions("03");
        o.setStop("76.5");
        o.setLimitFee(true);
        o.setSettlementType("0000");
        o.setBusinessSegment("1");
        o.setOrderPlacementVia("FI");
        o.setOrderer("D");
        o.setOrdererIdentifier("Depotinhaber, Manfred");
        o.setOrdererCustomerNumber("1022228");
        o.setMinutesOfTheConsultation("1");
        o.setMinutesOfTheConsultationNumber("123456");
        o.setConsultationDate("2013-11-12T00:00:00.000+01:00");
        o.setCustomerBonus("00");
        o.setProvisionType(ProvisionType.PT_NO_DIFFERING_PROVISION);
        o.setOrdererExtern("Kirk, James T.");
        o.setBillingDocument1("01");
        o.setBillingDocument2("02");
        o.setBillingDocument3("04");
        o.setOrderConfirmation("01");
        o.getFreeText().add(OrderUtils.newTextWithType(TextType.TT_FREE_TEXT_ORDER_DOCUMENT_1, "Freier Text für den Orderbeleg 1/2"));
        o.getFreeText().add(OrderUtils.newTextWithType(TextType.TT_FREE_TEXT_ORDER_DOCUMENT_2, "Freier Text für den Orderbeleg 2/2"));
        o.getFreeText().add(OrderUtils.newTextWithType(TextType.TT_INTERNAL_TEXT_1, "Interner Text 1/2"));
        o.getFreeText().add(OrderUtils.newTextWithType(TextType.TT_INTERNAL_TEXT_2, "Interner Text 2/2"));

        o.setOrderDateTime("2013-10-21T14:05:26.000+01:00");

        final List<ClearingData> clearingDataList = o.getClearingData();
        clearingDataList.add(newClearingData("12334", "abgerechn.", "10", "23.5", "EUR", "1", "EUR", "2013-10-22T14:05:23.000+01:00"));
        clearingDataList.add(newClearingData("56789", "abgerechn.", "20", "23.58", "EUR", "1", "EUR", "2013-10-22T14:10:23.000+01:00"));

        o.setStatus090(newOrderStatus("090# offen", DELPHI_ZERO_DATE, null));
        o.setStatus091(newOrderStatus("091# nicht gedruckt", DELPHI_ZERO_DATE, null));
        o.setStatus092(newOrderStatus("092# Nicht routingfähig", "2013-10-22T14:05:26.000+01:00", "CUBE"));
        o.setStatus093(newOrderStatus("093# nicht gedruckt", DELPHI_ZERO_DATE, null));
        o.setStatus094(newOrderStatus("094# nicht gedruckt", DELPHI_ZERO_DATE, null));
        o.setStatus095(newOrderStatus("095# noch nicht berechnet", DELPHI_ZERO_DATE, null));
        o.setStatus096(newOrderStatus("096# nicht gestrichen", DELPHI_ZERO_DATE, null));
        o.setStatus097(newOrderStatus("097# nicht geändert", DELPHI_ZERO_DATE, null));
        o.setStatus098(newOrderStatus("098# vollständig", "2013-10-22T14:05:26.000+01:00", "USER01"));
        o.setStatus099(newOrderStatus("099# nicht gedruckt", DELPHI_ZERO_DATE, null));
        o.setIsSupplementAllowed(true);
        o.setIsChangeAllowed(true);
        o.setIsDeleteAllowed(true);

        return o;
    }

    private OrderDataTypeBHL withPredecessorAndSuccessorClearingData(OrderDataTypeBHL o) {
        final List<ClearingData> cds = o.getClearingData();
        cds.clear();
        cds.add(withPredecessorAndSuccessor(newClearingData("2300000000001", "abgerechn.", "100", "23.6", "EUR", "1", "EUR", "2013-10-22T14:05:23.000+01:00"), "0", "2300000000002"));
        cds.add(withPredecessorAndSuccessor(newClearingData("2300000000002", "abgerechn.", "100", "23.5", "EUR", "1", "EUR", "2013-10-22T15:05:23.000+01:00"), "2300000000001", "2300000000003"));
        cds.add(withPredecessorAndSuccessor(newClearingData("2300000000003", "abgerechn.", "10", "23.5", "EUR", "1", "EUR", "2013-10-22T16:05:23.000+01:00"), "2300000000002", null));
        o.getSecurityData().setBezeichnung(o.getSecurityData().getBezeichnung() + " Predecessor/Successor transaction");
        return o;
    }

    private ClearingData newClearingData(String geschaeftsNummer, String status, String nennwert, String kurs, String waehrung, String devisenKurs, String devisenkursWaehrung, String valuta) {
        final ClearingData o = new ClearingData();
        o.setGeschaeftsnummer(geschaeftsNummer);
        o.setStatus(status);
        o.setNennwert(nennwert);
        o.setWaehrung(waehrung);
        o.setKurs(kurs);
        o.setDevisenkursWaehrung(devisenkursWaehrung);
        o.setDevisenkurs(devisenKurs);
        o.setValuta(valuta);
        o.setGeschaeftsnummerVorgaenger("0");
        o.setGeschaeftsnummerNachfolger("0");
        return o;
    }

    private ClearingData withPredecessorAndSuccessor(ClearingData cd, String predecessor, String successor) {
        cd.setGeschaeftsnummerVorgaenger(predecessor);
        cd.setGeschaeftsnummerNachfolger(successor);
        return cd;
    }

    public static class OrderSessionFeaturesDescriptorMockBHLKGS extends OrderSessionFeaturesDescriptorBHL {
        public OrderSessionFeaturesDescriptorMockBHLKGS() {
            getTradingIndicator().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("", ""),
                    OrderUtils.newTextWithKey("E", "Eröff.Kurs"),
                    OrderUtils.newTextWithKey("K", "Kassakurs"),
                    OrderUtils.newTextWithKey("S", "Schlussk.")
            ));

            getSettlementTypes().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("0000", "Brutto", true),
                    OrderUtils.newTextWithKey("0001", "Netto (ohne Courtage, Provision, Eigene Spesen)"),
                    OrderUtils.newTextWithKey("0002", "Franco Courtage"),
                    OrderUtils.newTextWithKey("0003", "Netto plus Courtage"),
                    OrderUtils.newTextWithKey("0004", "Mischkurs (wie netto)"),
                    OrderUtils.newTextWithKey("0005", "Netto plus fremde Spesen")
            ));

            getBusinessSegments().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("", ""),
                    OrderUtils.newTextWithKey("1", "Anlageberatung/VV"),
                    OrderUtils.newTextWithKey("2", "Beratungsfr. Geschäft"),
                    OrderUtils.newTextWithKey("3", "Handelsgeschäft GGP")
            ));

            getOrderPlacementVia().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("AL", "Allokationsorder"),
                    OrderUtils.newTextWithKey("CC", "Call-Center"),
                    OrderUtils.newTextWithKey("FA", "FAX"),
                    OrderUtils.newTextWithKey("FI", "Filiale"),
                    OrderUtils.newTextWithKey("HB", "Hausbesuch"),
                    OrderUtils.newTextWithKey("IT", "Internet (HBCI)"),
                    OrderUtils.newTextWithKey("SP", "Sparplan"),
                    OrderUtils.newTextWithKey("SW", "SWIFT"),
                    OrderUtils.newTextWithKey("TE", "Telefon"),
                    OrderUtils.newTextWithKey("AS", "advisory solution", true)
            ));

            //nur eine kleine Auswahl
            this.getTextLibrariesBillingDocument().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("00", ""),
                    OrderUtils.newTextWithKey("01", "Steuerbescheinigung folgt"),
                    OrderUtils.newTextWithKey("02", "Jahressteuerbescheinigung folgt"),
                    OrderUtils.newTextWithKey("03", "Keine Steuerbescheinigung"),
                    OrderUtils.newTextWithKey("04", "Steuerbescheinigung wird korrigiert"),
                    OrderUtils.newTextWithKey("05", "Bitte geben Sie die Steuerbescheinigung\nzurück"),
                    OrderUtils.newTextWithKey("06", "Ihre Bezugsrechte haben wir mangels anderer\nWeisung verkauft"),
                    OrderUtils.newTextWithKey("07", "Teilausführung Ihrer Order über Nominale ....."),
                    OrderUtils.newTextWithKey("38/39/40", "Beim Kauf o.g. Wertpapiere haben wir uns auf die\n" +
                            "reine Orderausführung beschränkt. Auf Ihren ausdrücklichen\n" +
                            "Wunsch hin haben wir keine Beratungsdienstleistung\n" +
                            "erbracht."),
                    OrderUtils.newTextWithKey("55", "Early-Order"),
                    OrderUtils.newTextWithKey("56", "Incentive-Order")
            ));

            //nur eine kleine Auswahl
            this.getTextLibrariesOrderConfirmation().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("00", ""),
                    OrderUtils.newTextWithKey("01", "Wir danken für den Auftrag"),
                    OrderUtils.newTextWithKey("02", "Ihr Auftrag wurde nicht ausgeführt. Die Zuteilung\nerfolgte im Losverfahren."),
                    OrderUtils.newTextWithKey("30", "Aufgrund Überzeichnung erfolgte die Zuteilung\nim Repartierungsverfahren."),
                    OrderUtils.newTextWithKey("32", "Ihr Auftrag ist leider nicht berücksichtigt worden."),
                    OrderUtils.newTextWithKey("32", "Aufgrund Überzeichnung erhielten wir leider keine\nZuteilung.")
            ));

            this.getLimitOptions().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("00", ""),
                    OrderUtils.newTextWithKey("01", "Interesse wahrend"),
                    OrderUtils.newTextWithKey("02", "stop loss (Verkauf)"),
                    OrderUtils.newTextWithKey("03", "stop to buy (Kauf)"),
                    OrderUtils.newTextWithKey("04", "circa-Kurs (10 % Abweichung)"),
                    OrderUtils.newTextWithKey("07", "Immediate-or-Cancel (Xetra)"),
                    OrderUtils.newTextWithKey("08", "Fill-or-Kill (Xetra)"),
                    OrderUtils.newTextWithKey("09", "Iceberg Order (Xetra)"),
                    OrderUtils.newTextWithKey("10", "Trailing Stop absol."),
                    OrderUtils.newTextWithKey("11", "Trailing Stop proz."),
                    OrderUtils.newTextWithKey("12", "One-Cancels-Other")
            ));

            //PM-eigene Liste ohne Entsprechung bei BHL
            this.getLimitRequiredByLimitOption().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("02", "stop loss (Verkauf)"),
                    OrderUtils.newTextWithKey("03", "stop to buy (Kauf)")
            ));

            //PM-eigene Liste ohne Entsprechung bei BHL
            this.getOrderer().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("D", "Depotinhaber", true),
                    OrderUtils.newTextWithKey("B", "Bevollmächtigter")
            ));

            this.setOrderNeedsNameKey("B");

            this.getMinutesOfTheConsultation().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("", ""),
                    OrderUtils.newTextWithKey("1", "Beratungsp. versandt"),
                    OrderUtils.newTextWithKey("2", "Beratungsp. weiterg.")
            ));

            this.getCancellationReason().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("", ""),
                    OrderUtils.newTextWithKey("A", "Kundenauftrag"),
                    OrderUtils.newTextWithKey("B", "Börse/H-System"),
                    OrderUtils.newTextWithKey("K", "Kursaussetzung"),
                    OrderUtils.newTextWithKey("N", "Nebenrechte"),
                    OrderUtils.newTextWithKey("O", "Ohne Angabe"),
                    OrderUtils.newTextWithKey("S", "Storno Geschaeft"),
                    OrderUtils.newTextWithKey("X", "Zwangseindeckung"),
                    OrderUtils.newTextWithKey("Z", "Keine Zuteilung")
            ));

            this.getCustomerBonus().addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("00", "Keine Kundenboni    "),
                    OrderUtils.newTextWithKey("01", "Mit Kundenboni      ")
            ));

            this.setAuftragsDateTime(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()));
        }
    }

    public static OrderExchangeInfo newOrderExchangeInfoDomestic(String label, String iso, String id) {
        return newOrderExchangeInfo(label, iso, id, ExternExchangeType.EET_DOMESTIC);
    }

    public static OrderExchangeInfo newOrderExchangeInfoForeign(String label, String iso, String id) {
        return newOrderExchangeInfo(label, iso, id, ExternExchangeType.EET_FOREIGN);
    }

    public static OrderExchangeInfo newOrderExchangeInfoOther(String label, String iso, String id, boolean isDefault) {
        return newOrderExchangeInfo(label, iso, id, ExternExchangeType.EET_OTHER, isDefault);
    }

    public static OrderExchangeInfo newOrderExchangeInfoOther(String label, String iso, String id) {
        return newOrderExchangeInfo(label, iso, id, ExternExchangeType.EET_OTHER);
    }

    private static OrderExchangeInfo newOrderExchangeInfo(String label, String iso, String id, ExternExchangeType type) {
        return newOrderExchangeInfo(label, iso, id, type, false);
    }

    private static OrderExchangeInfo newOrderExchangeInfo(String label, String iso, String id, ExternExchangeType type, boolean isDefault) {
        final OrderExchangeInfo info = new OrderExchangeInfo();
        info.setName(label);
        info.setISOCode(iso);
        info.setID(id);
        info.setExternExchangeTyp(type);
        info.setIsDefault(isDefault);
        return info;
    }

    private static OrderStatusBHL newOrderStatus(String text, String date, String editor) {
        final OrderStatusBHL s = new OrderStatusBHL();
        s.setText(text);
        s.setDateTime(date);
        s.setEditor(editor);
        return s;
    }

    public void sendOrder(OrderSession orderSession, OrderDataType orderRaw, List<ValidationMessage> validationMessages, AsyncCallback<SendOrderDataResponse> callback) {
        if(orderRaw == null) {
            Firebug.info("<OrderSessionMockBHLKGS.sendOrder> failure: OrderDataType is null");
            callback.onFailure(new OrderMethods.PmxmlOrderEntryResponseException(
                    "OrderDataType is null",
                    OrderResultCode.ORC_ERROR,
                    OrderResultMSGType.OMT_ERROR));
            return;
        }

        final OrderDataTypeBHL order = (OrderDataTypeBHL)orderRaw;

        Firebug.info("<OrderSessionMockBHLKGS.sendOrder>");
        final SendOrderDataResponse response = new SendOrderDataResponse();
        response.setOrder(order);

        final String orderNumber = Long.toString(++lastOrderNumber);
        order.setOrderNumber(orderNumber);
        order.setChangeNumber(Integer.toString(0));
        fillOrderStatus(order);
        order.setIsSupplementAllowed(true);
        order.setIsChangeAllowed(true);
        order.setIsDeleteAllowed(true);

        response.setExternalMessage("[ASMOCK] OrderAnlegen: ORDER MIT NUMMER " + orderNumber + " ERFOLGREICH ANGELEGT");
        if(validationMessages != null) {
            response.getValidationMsgList().addAll(validationMessages);
        }

        this.orders.add(0, order);
        callback.onSuccess(response);
    }

    private void fillOrderStatus(OrderDataTypeBHL order) {
        order.setStatus090(newOrderStatus("090# offen", DELPHI_ZERO_DATE, null));
        order.setStatus091(newOrderStatus("091# nicht gedruckt", DELPHI_ZERO_DATE, null));
        order.setStatus092(newOrderStatus("092# Nicht routingfähig", Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()), "CUBE"));
        order.setStatus093(newOrderStatus("093# nicht gedruckt", DELPHI_ZERO_DATE, null));
        order.setStatus094(newOrderStatus("094# nicht gedruckt", DELPHI_ZERO_DATE, null));
        order.setStatus095(newOrderStatus("095# noch nicht berechnet", DELPHI_ZERO_DATE, null));
        order.setStatus096(newOrderStatus("096# nicht gestrichen", DELPHI_ZERO_DATE, null));
        order.setStatus097(newOrderStatus("097# nicht geändert", DELPHI_ZERO_DATE, null));
        order.setStatus098(newOrderStatus("098# vollständig", Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()), brokingUser));
        order.setStatus099(newOrderStatus("099# nicht gedruckt", DELPHI_ZERO_DATE, null));
    }

    private void copyOrderStatus(OrderDataTypeBHL source, OrderDataTypeBHL target) {
        target.setStatus090(source.getStatus090());
        target.setStatus091(source.getStatus091());
        target.setStatus092(source.getStatus092());
        target.setStatus093(source.getStatus093());
        target.setStatus094(source.getStatus094());
        target.setStatus095(source.getStatus095());
        target.setStatus096(source.getStatus096());
        target.setStatus097(source.getStatus097());
        target.setStatus098(source.getStatus098());
        target.setStatus099(source.getStatus099());
    }

    @Override
    public void changeOrder(OrderSession orderSession, OrderDataType orderRaw, List<ValidationMessage> validationMessages, AsyncCallback<ChangeOrderDataResponse> callback) {
        if(orderRaw == null) {
            Firebug.info("<OrderSessionMockBHLKGS.changeOrder> failure: OrderDataType is null");
            callback.onFailure(new OrderMethods.PmxmlOrderEntryResponseException(
                    "OrderDataType is null",
                    OrderResultCode.ORC_ERROR,
                    OrderResultMSGType.OMT_ERROR));
            return;
        }

        final String orderNumber = orderRaw.getOrderNumber();
        if(!StringUtil.hasText(orderNumber) || "0".equals(orderNumber)) {
            Firebug.info("<OrderSessionMockBHLKGS.changeOrder> failure: change order request requires an order number");
            callback.onFailure(new OrderMethods.PmxmlOrderEntryResponseException(
                    "OrderDataType has no order number",
                    OrderResultCode.ORC_ERROR,
                    OrderResultMSGType.OMT_ERROR));
            return;
        }

        final OrderDataTypeBHL order = (OrderDataTypeBHL)orderRaw;

        Firebug.info("<OrderSessionMockBHLKGS.changeOrder>");
        final ChangeOrderDataResponse response = new ChangeOrderDataResponse();
        response.setOrder(order);
        response.setExternalMessage("[ASMOCK] OrderAendern: ORDER MIT NUMMER " + orderNumber + " ERFOLGREICH GEÄNDERT");

        final int indexOfOrder = findOrder(orderNumber, callback);
        if(0 > indexOfOrder) {
            return;
        }

        final OrderDataTypeBHL original = this.orders.get(indexOfOrder);
        if(!incrementChangeNumber(order, original, callback)) {
            return;
        }

        copyOrderStatus(original, order);
        order.setStatus097(newOrderStatus("097# geändert", Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date()), brokingUser));
        order.setIsChangeAllowed(true);
        order.setIsDeleteAllowed(true);

        this.orders.set(indexOfOrder, order);

        if(validationMessages != null) {
            response.getValidationMsgList().addAll(validationMessages);
        }

        callback.onSuccess(response);
    }

    private int findOrder(String orderNumber, AsyncCallback callback) {
        for(int i = 0; i < this.orders.size(); i++) {
            final OrderDataTypeBHL original = this.orders.get(i);
            if(orderNumber.equals(original.getOrderNumber())) {
                return i;
            }
        }

        final String message = "Order " + orderNumber + " not found!";
        callback.onFailure(new PmxmlOrderEntryResponseException(message, OrderResultCode.ORC_ERROR, OrderResultMSGType.OMT_ERROR, message));
        return -1;
    }

    private boolean incrementChangeNumber(OrderDataTypeBHL order, OrderDataTypeBHL original, AsyncCallback callback) {
        if(!StringUtil.hasText(order.getChangeNumber()) || !order.getChangeNumber().equals(original.getChangeNumber())) {
            final String message = "Given change number " + order.getChangeNumber() +  " differs from current change number " + original.getChangeNumber();
            callback.onFailure(new PmxmlOrderEntryResponseException(message, OrderResultCode.ORC_ERROR, OrderResultMSGType.OMT_ERROR, message));
            return false;
        }

        try {
            order.setChangeNumber(Integer.toString(Integer.parseInt(original.getChangeNumber()) + 1));
            return true;
        }
        catch(Exception e) {
            final String message = "Failed to parse current change number " + original.getChangeNumber();
            Firebug.error(message, e);
            callback.onFailure(new PmxmlOrderEntryResponseException(message, OrderResultCode.ORC_ERROR, OrderResultMSGType.OMT_ERROR, message));
            return false;
        }
    }

    @Override
    public void loginBroker(OrderSession orderSession, String user, String password, AsyncCallback<Void> callback) {
        Firebug.info("<OrderSessionMockBHLKGS.loginBroker>");
        if(PASSWORD_AS.equalsIgnoreCase(password)) {
            authenticated = true;
            brokingUser = user;
            callback.onSuccess(null);
        }
        else {
            final String passwordHint = "OrderSessionMockBHLKGS: Falsches Passwort! Nimm '" + PASSWORD_AS + "'";
            callback.onFailure(
                    new OrderMethods.PmxmlOrderEntryResponseException(passwordHint,
                            OrderResultCode.ORC_BROKER_LOGIN_FAILED,
                            OrderResultMSGType.OMT_ERROR,
                            "[ASMOCK] Anmelde-Fehler. Trace-Id: 1381993402521-285 ... " + passwordHint
                    ));
        }
    }

    public OrderDataTypeBHL newOrder(String orderNumber, OrderTransaktionType type, String isin, double quantity) {
        final LookupSecurityInfo lookupSecurityInfo = newLookupSecurityInfo(type, isin);
        final OrderSecurityFeatureDescriptorBHL securityFeatures = newOrderSecurityFeatureDescriptorBHL(lookupSecurityInfo.getSecurity().isISQuotedPerUnit());
        return newOrderDataTypeBHL(orderNumber, type, lookupSecurityInfo, securityFeatures, quantity);
    }

    public List<OrderbookDataTypeBHL> createOrderBookDataTypesBHL(String depotNo, String investorName) {
        final List<OrderbookDataTypeBHL> orderBook = new ArrayList<OrderbookDataTypeBHL>(this.orders.size());

        for(final OrderDataTypeBHL o : this.orders) {
            orderBook.add(toOrderbookDataTypeBHL(o));
        }

        return orderBook;
    }

    private void addTenOrders(List<OrderDataTypeBHL> orders) {
        final Random r = new Random();

        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_BUY, ISIN_MOCK_STOCK_CUSTOM_PM_IID, (int)(r.nextDouble() * 100)));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_SELL, ISIN_MOCK_STOCK_CUSTOM_PM_IID,(int)(r.nextDouble() * 100)));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_BUY, ISIN_MOCK_BOND, r.nextDouble() * 1000));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_SELL, ISIN_MOCK_BOND, r.nextDouble() * 1000));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_BUY, ISIN_MOCK_STOCK_IID_DOES_NOT_EXIST, (int)(r.nextDouble() * 100)));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_SELL, ISIN_MOCK_STOCK_IID_DOES_NOT_EXIST, (int)(r.nextDouble() * 100)));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_SUBSCRIBE, ISIN_IPO_MOCK_SECURITY_FIRST_LIST_ENTRY, (int)(r.nextDouble() * 100)));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_SUBSCRIBE, ISIN_MOCK_BOND, r.nextDouble() * 1000));
        orders.add(0, newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_SUBSCRIBE, ISIN_MOCK_STOCK_IID_DOES_NOT_EXIST, (int)(r.nextDouble() * 100)));

        orders.add(0, withPredecessorAndSuccessorClearingData(
                newOrder(Long.toString(++lastOrderNumber), OrderTransaktionType.TT_BUY, ISIN_MOCK_STOCK_CUSTOM_PM_IID, 10))
        );
    }

    private static Date newDay() {
        timeMillis -= 1000 * 60 * 60 * 24;
        return new Date(timeMillis);
    }

    private static DBBank newMeDBank() {
        final DBBank dbBank = new DBBank();
        dbBank.setBankname("Bankaus Lampe KG");
        dbBank.setBLZ("48020151");
        dbBank.setBIC("BHLBIC");
        return dbBank;
    }

    private static OrderbookDataTypeBHL toOrderbookDataTypeBHL(OrderDataTypeBHL o) {
        final OrderbookDataTypeBHL ob = new OrderbookDataTypeBHL();

        final String expirationDate;
        switch(o.getExpirationType()) {
            case OET_DATE:
                expirationDate = o.getExpirationDate();
                break;
            case OET_ULTIMO:
                expirationDate = Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new MmJsDate().getNextMonth().getFirstOfMonth().addDays(-1).getJavaDate());
                break;
            case OET_DAY:
                expirationDate = Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new MmJsDate().atMidnight().getJavaDate());
                break;
            default:
                expirationDate = null;
        }

        ob.setOwnerName(o.getInhaberData().getName());
        ob.setSecurityAccountNumber(o.getDepotData().getNumber());
        ob.setTransaktionType(OeRenderers.ORDER_TRANSACTION_TYPE_RENDERER.render(o.getTyp()));
        ob.setQuantity(o.getQuantity());
        ob.setWKN(o.getSecurityData().getNumber());
        ob.setSecurityName(o.getSecurityData().getBezeichnung());
        ob.setOrderCurrency(o.getOrderCurrencyData().getKuerzel());
        ob.setLimit(o.getLimit());
        ob.setLimitCurrency(o.getLimitCurrencyData() != null ? o.getLimitCurrencyData().getKuerzel() : null);
        ob.setLimitOption(o.getLimitOptions());
        ob.setExpirationDate(expirationDate);
        ob.setExchangeName(o.getExchangeData().getName());
        ob.setOrderDate(o.getOrderDateTime());
        ob.setUnfilledQuantity(o.getQuantity());
        ob.setOrderStatus((o.getStatus096() != null && STATUS_096_GESTRICHEN.equals(o.getStatus096().getText())) ? "gestrichen" : "offen");
        ob.setKGSFillStatus("offen");
        ob.setOrderNumber(o.getOrderNumber());
        ob.setIsSupplementAllowed(true);
        ob.setIsChangeAllowed(true);
        ob.setIsDeleteAllowed(true);

        return ob;
    }

    /**
     * Creates an order and fills it with default values as it will be delivered by Delphi.
     */
    static class OrderDataTypeBHLMock extends OrderDataTypeBHL {
        private static final String ZERO = "0";

        public OrderDataTypeBHLMock(OrderDataType orderDataType) {
            this();
            final OrderDataTypeBHL other = (OrderDataTypeBHL)orderDataType;

            this.id = fill(this.id, other.getId());
            this.bmId = fill(this.id, other.getBMId());
            this.bm = other.getBM();
            this.activityId = fill(this.activityId, other.getActivityId());
            this.orderSuggestionRef = fill(this.orderSuggestionRef, other.getOrderSuggestionRef());
            this.externRef = fill(this.externRef, other.getExternRef());
            this.externRef2 = fill(this.externRef2, other.getExternRef2());
            this.orderNumber = fill(this.orderNumber, other.getOrderNumber());
            this.changeNumber = fill(this.changeNumber, other.getChangeNumber());
            this.typ = other.getTyp();
            isShort = other.isIsShort();
            this.inhaberData = fill(this.inhaberData, other.getInhaberData());
            this.depotData = fill(this.depotData, other.getDepotData());
            this.kontoData = fill(this.kontoData, other.getKontoData());
            this.debitAccount = fill(this.debitAccount, other.getDebitAccount());
            this.lagerstelleData = fill(this.lagerstelleData, other.getLagerstelleData());
            this.sperreData = fill(this.sperreData, other.getSperreData());
            this.securityData = fill(this.securityData, other.getSecurityData());
            this.isQuotedPerUnit = other.isIsQuotedPerUnit();
            this.depotCurrencyData = fill(this.depotCurrencyData, other.getDepotCurrencyData());
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
            this.limitOptions = fill(this.limitOptions, other.getLimitOptions());
            this.limitType = fillEnum(this.limitType, other.getLimitType());
            this.limitCurrencyData = fill(this.limitCurrencyData, other.getLimitCurrencyData());
            this.peakSizeQuantity = fill(this.peakSizeQuantity, other.getPeakSizeQuantity());
            this.trailingPercent = fill(this.trailingPercent, other.getTrailingPercent());
            this.limitInfos = fill(this.limitInfos, other.getLimitInfos());
            this.limitFee = other.isLimitFee();
            this.marketMode = fillEnum(this.marketMode, other.getMarketMode());
            this.orderer = fill(this.orderer, other.getOrderer());
            this.ordererIdentifier = fill(this.ordererIdentifier, other.getOrdererIdentifier());
            this.ordererCustomerNumber = fill(this.ordererCustomerNumber, other.getOrdererCustomerNumber());
            this.ordererExtern = fill(this.ordererExtern, other.getOrdererExtern());
            this.orderPlacementVia = fill(this.orderPlacementVia, other.getOrderPlacementVia());
            this.bonusPayments = fill(this.bonusPayments, other.getBonusPayments());
            this.discount = fill(this.discount, other.getDiscount());
            this.prospectus = fill(this.prospectus, other.getProspectus());
            this.trailingDelta = fill(this.trailingDelta, other.getTrailingDelta());
            this.issueSurchargeKickback = fill(this.issueSurchargeKickback, other.getIssueSurchargeKickback());
            this.remark = fill(this.remark, other.getRemark());
            this.remark2 = fill(this.remark2, other.getRemark2());
            this.sellFromDepositing = other.isSellFromDepositing();
            this.marketValue = fill(this.marketValue, other.getMarketValue());
            this.marketValueCurrencyData = fill(this.marketValueCurrencyData, other.getMarketValueCurrencyData());
            this.exchangeCustomerRequest = other.isExchangeCustomerRequest();
            this.settlementType = fill(this.settlementType, other.getSettlementType());
            this.businessSegment = fill(this.businessSegment, other.getBusinessSegment());
            this.billingDocument1 = fill(this.billingDocument1, other.getBillingDocument1());
            this.billingDocument2 = fill(this.billingDocument2, other.getBillingDocument2());
            this.billingDocument3 = fill(this.billingDocument3, other.getBillingDocument3());
            this.orderConfirmation = fill(this.orderConfirmation, other.getOrderConfirmation());
            this.freeText = other.getFreeText();
            this.tradingIdentifier = fill(this.tradingIdentifier, other.getTradingIdentifier());
            this.minutesOfTheConsultation = fill(this.minutesOfTheConsultation, other.getMinutesOfTheConsultation());
            this.minutesOfTheConsultationNumber = fill(this.minutesOfTheConsultationNumber, other.getMinutesOfTheConsultationNumber());
            this.consultationDate = fill(this.consultationDate, other.getConsultationDate());
            this.cancellationReason = fill(this.cancellationReason, other.getCancellationReason());
            this.cancellationConfirmationPrint = other.isCancellationConfirmationPrint();
            this.orderDateTime = fill(this.orderDateTime, other.getOrderDateTime());
            this.provisionType = fill(this.provisionType, other.getProvisionType());
            this.provisionValue = fill(this.provisionValue, other.getProvisionValue());
            this.customerBonus = fill(this.customerBonus, other.getCustomerBonus());
            this.abwCustomerBonus = fill(this.abwCustomerBonus, other.getAbwCustomerBonus());
            this.clearingData = fill(this.clearingData, other.getClearingData());
            this.status090 = fill(this.status090, other.getStatus090());
            this.status091 = fill(this.status091, other.getStatus091());
            this.status092 = fill(this.status092, other.getStatus092());
            this.status093 = fill(this.status093, other.getStatus093());
            this.status094 = fill(this.status094, other.getStatus094());
            this.status095 = fill(this.status095, other.getStatus095());
            this.status096 = fill(this.status096, other.getStatus096());
            this.status097 = fill(this.status097, other.getStatus097());
            this.status098 = fill(this.status098, other.getStatus098());
            this.status099 = fill(this.status099, other.getStatus099());
            this.oooallokatnr = fill(this.oooallokatnr, other.getOOOALLOKATNR());
            this.ooostorgesch10 = fill(this.ooostorgesch10, other.getOOOSTORGESCH10());
            this.ooostorgesch3 = fill(this.ooostorgesch3, other.getOOOSTORGESCH3());
        }

        public OrderDataTypeBHLMock() {
            this.id = ZERO;
            this.bmId = Integer.toString(BrokerageModuleID.BM_BHLKGS.ordinal());
            this.bm = BrokerageModuleID.BM_BHLKGS;
            this.activityId = ZERO;
            this.orderSuggestionRef = "";
            this.externRef = ZERO;
            this.externRef2 = ZERO;
            this.orderNumber = ZERO;
            this.changeNumber = null;
            this.typ = OrderTransaktionType.TT_BUY;
            this.isShort = false;
            this.isQuotedPerUnit = false;
            this.depotCurrencyData = null;
            this.inhaberData = null;
            this.depotData = null;
            this.kontoData = null;
            this.debitAccount = "";
            this.lagerstelleData = null;
            this.sperreData = null;
            this.securityData = null;
            this.exchangeData = null;
            this.quantity = ZERO;
            this.isOrderInAmount = false;
            this.orderAmount = ZERO;
            this.orderCurrencyData = null;
            this.executionOption = OrderExecutionOption.OEO_NA;
            this.expirationDate = DELPHI_ZERO_DATE;
            this.expirationType = OrderExpirationType.OET_NA;
            this.limit = ZERO;
            this.stop = ZERO;
            this.limitOptions = null;
            this.limitType = OrderLimitType.OLT_NA;
            this.limitCurrencyData = null;
            this.limitInfos = "";
            this.limitFee = false;
            this.marketMode = MarketMode.OMM_NA;
            this.orderer = "";
            this.ordererIdentifier = "";
            this.ordererCustomerNumber = "";
            this.ordererExtern = "";
            this.orderPlacementVia = "";
            this.bonusPayments = ZERO;
            this.discount = ZERO;
            this.prospectus = ZERO;
            this.trailingDelta = ZERO;
            this.issueSurchargeKickback = ZERO;
            this.remark = "";
            this.remark2 = "";
            this.sellFromDepositing = false;
            this.marketValue = ZERO;
            this.marketValueCurrencyData = null;
            this.exchangeCustomerRequest = false;
            this.settlementType = "";
            this.businessSegment = "";
            this.billingDocument1 = "";
            this.billingDocument2 = "";
            this.billingDocument3 = "";
            this.orderConfirmation = "";
            this.freeText = null;
            this.tradingIdentifier = "";
            this.minutesOfTheConsultation = "";
            this.minutesOfTheConsultationNumber = "";
            this.consultationDate = DELPHI_ZERO_DATE;
            this.cancellationReason = "";
            this.cancellationConfirmationPrint = false;
            this.orderDateTime = null;
            this.clearingData = null;
            this.status090 = null;
            this.status091 = null;
            this.status092 = null;
            this.status093 = null;
            this.status094 = null;
            this.status095 = null;
            this.status096 = null;
            this.status097 = null;
            this.status098 = null;
            this.status099 = null;
            this.oooallokatnr = ZERO;
            this.ooostorgesch10 = ZERO;
            this.ooostorgesch3 = ZERO;
        }
    }
}