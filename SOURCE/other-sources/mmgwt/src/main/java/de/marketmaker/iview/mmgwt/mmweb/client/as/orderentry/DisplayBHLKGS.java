/*
 * DisplayBHLKGS.java
 *
 * Created on 15.08.13 16:25
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorGroup;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AuthorizedRepresentative;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderStock;
import de.marketmaker.iview.pmxml.ProvisionType;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
* @author Markus Dick
*/
public interface DisplayBHLKGS<P extends DisplayBHLKGS.PresenterBHLKGS> extends Display<P> {
    public static enum ValidUntil {DEFAULT, TODAY, ULTIMO, DATE }

    void setLoadingIndicatorVisible(boolean visible);

    void setOrderActionTypes(List<OrderActionType> actions);
    void setOrderActionTypesSelectedItem(OrderActionType action);
    OrderActionType getOrderActionTypesSelectedItem();
    void setOrderActionTypesEnabled(boolean enabled);
    void setOrderActionTypesVisible(boolean enabled);
    void setOrderActionTypeName(OrderActionType type);
    void setOrderActionTypeNameVisible(boolean visible);

    void setInvestorName(String investorName);
    void setInvestorNo(String investorNo);

    void setDepotName(String depotName);
    void setDepotNameVisible(boolean visible);

    void setDepots(List<Depot> depots);
    void setDepotsVisible(boolean visible);
    void setDepotsEnabled(boolean enabled);
    void setDepotsSelectedItem(Depot selectedItem);
    Depot getDepotsSelectedItem();

    void setAccounts(List<AccountData> accounts);
    AccountData getAccountsSelecedItem();
    void setAccountsSelectedItem(AccountData accountData);
    void setAccountNo(String accountNo);
    void setAccountBalance(String accountBalance, OrderCurrency currency);

    void setInstrumentName(String name);
    void setInstrumentType(ShellMMType type);
    void setWkn(String wkn);
    void setSelectInstrumentPanelVisible(boolean enabled);
    void setInstrumentNamePanelVisible(boolean visible);
    void setSymbolsOfDepot(List<OrderSecurityInfo> securityDataList);
    void setSelectSymbolFromDepotEnabled(boolean enabled);

    void setSelectIpoInstrumentPanelVisible(boolean enabled);
    void setSelectIpoInstrumentPanelEnabled(boolean enabled);

    void setShowArbitrageButtonEnabled(boolean enabled);
    void setShowArbitrageButtonVisible(boolean visible);

    void setExchangesDomestic(List<OrderExchangeInfo> exchanges);
    void setExchangesDomesticSelectedItem(OrderExchangeInfo selectedItem);
    OrderExchangeInfo getExchangesDomesticSelectedItem();
    void setExchangesDomesticEnabled(boolean enabled);
    void setExchangesDomesticValidatorEnabled(boolean enabled);

    void setExchangesForeign(List<OrderExchangeInfo> exchanges);
    void setExchangesForeignSelectedItem(OrderExchangeInfo selectedItem);
    OrderExchangeInfo getExchangesForeignSelectedItem();
    void setExchangesForeignEnabled(boolean enabled);
    void setExchangesForeignValidatorEnabled(boolean enabled);

    void setExchangesOther(List<OrderExchangeInfo> exchanges);
    void setExchangesOtherSelectedItem(OrderExchangeInfo selectedItem);
    OrderExchangeInfo getExchangesOtherSelectedItem();
    void setExchangesOtherEnabled(boolean enabled);
    void setExchangesOtherValidatorEnabled(boolean enabled);

    void setExchangeAccordingToCustomer(boolean checked);
    boolean isExchangeAccordingToCustomer();

    void setAmountNominal(String value);
    String getAmountNominal();
    void setAmountNominalEnabled(boolean enabled);
    void setAmountNominalCurrencyOrUnit(String currencyOrUnit);
    void setAmountNominalMaxValue(double maxValue);
    void resetAmountNominalMaxValue();

    void setDepositories(List<OrderStock> depositories);
    void setDepositoriesSelectedItem(OrderStock selectedItem);
    OrderStock getDepositoriesSelectedItem();
    void setDepositoriesEnabled(boolean enabled);

    void setTradingIndicators(List<TextWithKey> tradingIndicators);
    void setTradingIndicatorsSelectedItem(TextWithKey selectedItem);
    TextWithKey getTradingIndicatorsSelectedItem();
    void setTradingIndicatorsEnabled(boolean enabled);

    void setLimitOrStopLimit(String limit);
    String getLimitOrStopLimit();
    void setLimitOrStopLimitEnabled(boolean enabled);
    void setLimitOrStopLimitNotEmptyValidatorEnabled(boolean enabled);

    void setLimitTrailingAmountOrPercent(String value);
    String getLimitTrailingAmountOrPercent();
    void setLimitTrailingAmountOrPercentEnabled(boolean enabled);

    void setLimitPeakSizeQuantity(String value);
    String getLimitPeakSizeQuantity();
    void setLimitPeakSizeQuantityEnabled(boolean enabled);

    void setLimitCurrencies(List<CurrencyAnnotated> currencies);
    void setLimitCurrenciesSelectedItem(CurrencyAnnotated selectedItem);
    CurrencyAnnotated getLimitCurrenciesSelectedItem();
    void setLimitCurrenciesEnabled(boolean enabled);
    void setLimitCurrenciesValidatorEnabled(boolean enabled);

    void setLimitClauses(List<TextWithKey> limitClauses);
    void setLimitClausesSelectedItem(TextWithKey selectedItem);
    TextWithKey getLimitClausesSelectedItem();
    void setLimitClausesEnabled(boolean enable);

    void setLimitAfterStopLimit(String stopLimit);
    String getLimitAfterStopLimit();
    void setLimitAfterStopLimitEnabled(boolean enabled);

    void setLimitFee(boolean limitFee);
    boolean isLimitFee();

    ValidUntil getValidUntil();
    void setValidUntil(ValidUntil validUntil);
    void setValidUntilDate(MmJsDate mmJsDate);
    MmJsDate getValidUntilDate();

    void setAuthorizedRepresentatives(List<AuthorizedRepresentative> representatives);
    void setAuthorizedRepresentativesEnabled(boolean enabled);

    void setOrderers(List<TextWithKey> orderers);
    void setOrderersSelectedItem(TextWithKey selectedItem);
    TextWithKey getOrderersSelectedItem();
    void setOrderersEnabled(boolean enabled);

    void setOrdererIdentifier(String ordererIdentifier);
    String getOrdererIdentifier();
    void setOrdererIdentifierEnabled(boolean enabled);

    void setOrdererCustomerNumber(String ordererCustomerNo);
    String getOrdererCustomerNumber();
    void setOrdererCustomerNumberEnabled(boolean enabled);

    void setMinutesOfTheConsultationTypes(List<TextWithKey> types);
    void setMinutesOfTheConsultationTypesSelectedItem(TextWithKey selectedItem);
    TextWithKey getMinutesOfTheConsultationTypesSelectedItem();
    void setMinutesOfTheConsultationTypesEnabled(boolean enabled);

    void setMinutesOfTheConsultationNumber(String number);
    String getMinutesOfTheConsultationNumber();
    void setMinutesOfTheConsultationNumberEnabled(boolean enabled);

    void setMinutesOfTheConsultationDate(MmJsDate date);
    MmJsDate getMinutesOfTheConsultationDate();
    void setMinutesOfTheConsultationDateEnabled(boolean enabled);

    void setSettlementTypes(List<TextWithKey> types);
    void setSettlementTypesSelectedItem(TextWithKey selectedItem);
    TextWithKey getSettlementTypesSelectedItem();

    void setBusinessSegments(List<TextWithKey> businessSegments);
    void setBusinessSegmentsSelectedItem(TextWithKey item);
    TextWithKey getBusinessSegmentsSelectedItem();
    void setBusinessSegmentsEnabled(boolean enabled);

    void setPlacingOfOrderVias(List<TextWithKey> vias);
    void setPlacingOfOrderViasSelectedItem(TextWithKey selectedItem);
    TextWithKey getPlacingOfOrderViasSelectedItem();
    void setPlacingOfOrderViasEnabled(boolean enabled);

    void setCommission(List<ProvisionType> commission);
    void setCommissionSelectedItem(ProvisionType defaultTextWithKey);
    ProvisionType getCommissionSelectedItem();

    void setDifferentCommission(String differentCommission);
    String getDifferentCommission();
    void setDifferentCommissionEnabled(boolean enabled);

    void setExternalTypist(String typist);
    String getExternalTypist();

    void setContractDateTime(MmJsDate dateTime);
    MmJsDate getContractDateTime();

    void setCannedTextForBillingReceipts1(List<TextWithKey> cannedTexts);
    void setCannedTextForBillingReceipts1SelectedItem(TextWithKey selectedItem);
    TextWithKey getCannedTextForBillingReceipts1SelectedItem();

    void setCannedTextForBillingReceipts2(List<TextWithKey> cannedTexts);
    void setCannedTextForBillingReceipts2SelectedItem(TextWithKey selectedItem);
    TextWithKey getCannedTextForBillingReceipts2SelectedItem();

    void setCannedTextForBillingReceipts3(List<TextWithKey> cannedTexts);
    void setCannedTextForBillingReceipts3SelectedItem(TextWithKey selectedItem);
    TextWithKey getCannedTextForBillingReceipts3SelectedItem();

    void setCannedTextForOrderConfirmations(List<TextWithKey> cannedTexts);
    void setCannedTextForOrderConfirmationsSelectedItem(TextWithKey selectedItem);
    TextWithKey getCannedTextForOrderConfirmationsSelectedItem();

    void setTextForOrderReceipt1(String text);
    String getTextForOrderReceipt1();

    void setTextForOrderReceipt2(String text);
    String getTextForOrderReceipt2();

    void setTextForInternalUse1(String text);
    String getTextForInternalUse1();

    void setTextForInternalUse2(String text);
    String getTextForInternalUse2();

    ValidatorGroup getValidatorGroup();

    void setInstrumentDependingValuesEnabled(boolean enabled);

    public interface PresenterBHLKGS extends Display.Presenter {
        void onOrderActionTypeChanged(ChangeEvent changeEvent);
        void onDepotChanged(ChangeEvent changeEvent);
        void onSymbolSearchTextBoxValueChanged(ValueChangeEvent<String> changeEvent);
        void onSymbolSearchButtonClicked(ClickEvent clickEvent);
        void onSelectSymbolFromDepotSelected(SelectionEvent<OrderSecurityInfo> securityDataSelectionEvent);
        void onIpoSearchButtonClicked(ClickEvent clickEvent);
        void onShowArbitrageButtonClicked(ClickEvent clickEvent);
        void onAccountChanged(ChangeEvent changeEvent);
        void onExchangesDomesticChanged(ChangeEvent changeEvent);
        void onExchangesForeignChanged(ChangeEvent changeEvent);
        void onExchangesOtherChanged(ChangeEvent changeEvent);
        void onLimitChanged(ValueChangeEvent<String> valueChangeEvent);
        void onLimitCurrenciesChanged(ChangeEvent changeEvent);
        void onLimitClausesChanged(ChangeEvent changeEvent);
        void onSelectAuthorizedRepresentativeSelected(SelectionEvent<AuthorizedRepresentative> selectionEvent);
        void onOrdererChanged(ChangeEvent changeEvent);
        void onCommissionChanged(ChangeEvent changeEvent);
        void onBusinessSegmentChanged(ChangeEvent changeEvent);
        void onJointValidationEvent(ValidationEvent validationEvent);
    }
}
