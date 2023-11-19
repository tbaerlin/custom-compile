/*
 * OrderConfirmationModelBuilderBHLKGS.java
 *
 * Created on 23.08.13 12:27
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PriceWithCurrency;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ClearingData;
import de.marketmaker.iview.pmxml.ExternExchangeType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDataTypeBHL;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderStatusBHL;
import de.marketmaker.iview.pmxml.ProvisionType;
import de.marketmaker.iview.pmxml.TextWithKey;
import de.marketmaker.iview.pmxml.TextWithTyp;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderConfirmationDisplay.*;

/**
 * @author Markus Dick
 */
public class OrderConfirmationModelBuilderBHLKGS extends AbstractOrderConfirmationModelBuilder {
    public OrderConfirmationModelBuilderBHLKGS() {
        this(true);
    }

    public OrderConfirmationModelBuilderBHLKGS(boolean strict) {
        super(strict);
    }

    protected void addOrderNumberSection(String label, String externalMessage) {
        final Section section = new Section(label, 2);
        if(StringUtil.hasText(externalMessage)) {
            final List<OrderConfirmationDisplay.SimpleEntry> entries = section.getEntries();
            final String message = I18n.I.orderEntryExternalMessage(SafeHtmlUtils.htmlEscape(externalMessage));
            entries.add(new SimpleEntry(message));
        }
        getSections().add(section);
    }

    protected void addOrderNumberSectionForOrderDetails(OrderDataTypeBHL order) {
        final Section section = new Section(I18n.I.orderEntryOrderNumber() + ": " + order.getOrderNumber(), 2); //$NON-NLS$
        getSections().add(section);
    }

    protected void addSecuritySection2(OrderDataTypeBHL order, OrderSessionFeaturesDescriptorBHL sessionFeatures, OrderSecurityFeatureDescriptorBHL securityFeatures, LogBuilder log) {
        final OrderExchangeInfo orderExchangeInfo = order.getExchangeData();
        if(isStrict() && securityFeatures.isExchangeIsMandatory() && orderExchangeInfo == null) {
            DebugUtil.logToServer(log.add("OrderExchangeInfo is null although it is marked as mandatory").toString()); //$NON-NLS$
            throw new RuntimeException("OrderExchangeInfo is null although it is marked as mandatory. This should never happen"); //$NON-NLS$
        }
        final String orderExchangeInfoStr = renderStrict(orderExchangeInfo, OeRenderers.ORDER_EXCHANGE_INFO_RENDERER);

        final ExternExchangeType externExchangeType;
        if(orderExchangeInfo != null) {
            externExchangeType = orderExchangeInfo.getExternExchangeTyp();
        }
        else {
            externExchangeType = null;
        }
        final String externExchangeTypeStr = renderStrict(externExchangeType, OeRenderers.EXTERN_EXCHANGE_TYPE_RENDERER);

        final Section section = new Section("");
        final List<OrderConfirmationDisplay.SimpleEntry> entries = section.getEntries();

        entries.add(new Entry(externExchangeTypeStr, orderExchangeInfoStr));

        final TextWithKey tradingIndicator = OrderUtils.findTextWithKey(sessionFeatures.getTradingIndicator(), order.getTradingIdentifier());
        final String tradingIndicatorText = tradingIndicator != null ? tradingIndicator.getText() : null;
        entries.add(new Entry(I18n.I.orderEntryTradingIndicator(),
                Renderer.STRING_DOUBLE_DASH.render(tradingIndicatorText)));

        entries.add(new Entry(
                I18n.I.orderEntryExchangeAccordingToCustomer(),
                Renderer.BOOLEAN_YES_NO_RENDERER.render(order.isExchangeCustomerRequest())));

        getSections().add(section);
    }

    protected void addOrderSection(OrderDataTypeBHL order, LogBuilder log) {
        final Section section = new Section(I18n.I.orderEntryOrder());
        final List<OrderConfirmationDisplay.SimpleEntry> entries = section.getEntries();

        /* Show the so called KGS depot currency (OOODEPWHR), which is in our terms more the quote currency (KGS
         * supports only one single quote per instrument). Be aware that it contains either a valid ISO currency code
         * or "ST" for "Stück". order.isQuotedPerUnit reflects that accordingly.
         */
        final OrderCurrency depotCurrency = order.getDepotCurrencyData();
        if(isStrict() && depotCurrency == null) {
            DebugUtil.logToServer(log.add("order.depotCurrencyData (IO_OOODEPWHR) is null").toString()); //$NON-NLS$
            throw new RuntimeException("order.depotCurrencyData (IO_OOODEPWHR) is null. This should never happen"); //$NON-NLS$
        }

        final String orderCurrencyAbbr = renderStrict(depotCurrency, OeRenderers.ORDER_CURRENCY_RENDERER, Renderer.STRING_DOUBLE_DASH);

        entries.add(new Entry(
                I18n.I.orderEntryAmountNominal(),
                Renderer.PRICE_0MAX5.render(order.getQuantity())
                        + " " +
                        (order.isIsQuotedPerUnit() ? I18n.I.orderEntryAmountNominalPerUnit() : orderCurrencyAbbr))
        );

        final String approxValueOfContract = order.getMarketValue();
        if(StringUtil.hasText(approxValueOfContract) && !"0".equals(approxValueOfContract)) { //$NON-NLS$
            entries.add(new Entry(I18n.I.orderEntryBHLKGSApproxValueOfContract(),
                    Renderer.PRICE_0MAX5.render(approxValueOfContract)
                            + " "
                            + OeRenderers.ORDER_CURRENCY_RENDERER.render(order.getMarketValueCurrencyData())
            ));
        }

        if(order.getLagerstelleData() != null) {
            entries.add(new Entry(I18n.I.orderEntryDepository(), OeRenderers.DEPOSITORY_RENDERER.render(
                    order.getLagerstelleData(), order.getSperreData(), Renderer.STRING_DOUBLE_DASH.render(null)
            )));
        }

        getSections().add(section);
    }

    protected void addOrderTypesSection(OrderDataTypeBHL order, OrderSessionFeaturesDescriptorBHL sessionFeatures, LogBuilder log) {
        final OrderCurrency limitCurrency = order.getLimitCurrencyData();
        final String limitCurrencyAbbr = Renderer.STRING_DOUBLE_DASH.render(OeRenderers.ORDER_CURRENCY_RENDERER.render(limitCurrency));
        final TextWithKey limitClause = OrderUtils.findTextWithKey(sessionFeatures.getLimitOptions(), order.getLimitOptions());

        final String validityText;
        switch(order.getExpirationType()) {
            case OET_DAY:
                validityText = OeRenderers.BHLKGS_VALID_UNTIL_CHOICE_RENDERER.render(DisplayBHLKGS.ValidUntil.TODAY);
                break;
            case OET_DATE:
                validityText = PmRenderers.DATE_STRING.render(order.getExpirationDate());
                break;
            case OET_ULTIMO:
                validityText = OeRenderers.BHLKGS_VALID_UNTIL_CHOICE_RENDERER.render(DisplayBHLKGS.ValidUntil.ULTIMO);
                break;
            case OET_NA:
                validityText = OeRenderers.BHLKGS_VALID_UNTIL_CHOICE_RENDERER.render(DisplayBHLKGS.ValidUntil.DEFAULT);
                break;
            default:
                if(isStrict()) {
                    DebugUtil.logToServer(log.add("Unsupported expiration settings").add("order.expirationType", order.getExpirationType()).toString());  //$NON-NLS$
                    throw new RuntimeException("Unsupported expiration settings. This should never happen");  //$NON-NLS$
                }
                validityText = renderStrict(order.getExpirationType(), OeRenderers.ORDER_EXPIRATION_TYPE_RENDERER);
        }

        final Section section = new Section(I18n.I.orderEntryOrderTypes());
        final List<OrderConfirmationDisplay.SimpleEntry>entries = section.getEntries();
        entries.add(new Entry(I18n.I.orderEntryLimitOrStopLimit(), renderLimitValue(order.getLimit(), limitCurrencyAbbr)));
        entries.add(new Entry(I18n.I.orderEntryLimitClause(), OeRenderers.TEXT_WITH_KEY_RENDERER.render(limitClause)));
        entries.add(new Entry(I18n.I.orderEntryLimitAfterStopLimit(), renderLimitValue(order.getStop(), limitCurrencyAbbr)));
        entries.add(new Entry(I18n.I.orderEntryLimitTrailingAmountOrPercent(), OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_RENDERER.render(order.getTrailingPercent())));
        entries.add(new Entry(I18n.I.orderEntryLimitPeakSizeQuantity(), OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_RENDERER.render(order.getPeakSizeQuantity())));
        entries.add(new Entry(I18n.I.orderEntryLimitFee(), Renderer.BOOLEAN_YES_NO_RENDERER.render(order.isLimitFee())));
        entries.add(new Entry(I18n.I.orderEntryValidity(), validityText));
        getSections().add(section);
    }

    protected void addOrdererSection(OrderDataTypeBHL order, OrderSessionFeaturesDescriptorBHL features, LogBuilder log) {
        final TextWithKey orderer = OrderUtils.findTextWithKey(features.getOrderer(), order.getOrderer());
        if(isStrict() && orderer == null) {
            DebugUtil.logToServer(log.add("Orderer type not found").add("order.orderer", order.getOrderer()).toString()); //$NON-NLS$
            throw new RuntimeException("Selected orderer not found. This should never happen"); //$NON-NLS$
        }
        final String ordererText = orderer != null ? orderer.getText() : renderStrict(null, Renderer.STRING_DOUBLE_DASH);

        final Section section = new Section(I18n.I.orderEntryOrderer());
        final List<OrderConfirmationDisplay.SimpleEntry>entries = section.getEntries();
        entries.add(new Entry(I18n.I.orderEntryOrdererInterlocutor(), ordererText));

        if(StringUtil.hasText(order.getOrdererIdentifier())) {
            entries.add(new Entry(I18n.I.orderEntryOrdererIdentifier(),
                    Renderer.STRING_DOUBLE_DASH.render(order.getOrdererIdentifier())));
        }
        if(StringUtil.hasText(order.getOrdererCustomerNumber())) {
            entries.add(new Entry(I18n.I.orderEntryOrdererCustomerNumber(),
                    Renderer.STRING_DOUBLE_DASH.render(order.getOrdererCustomerNumber())));
        }

        getSections().add(section);
    }

    public void addMinutesOfTheConsultationSection(OrderDataTypeBHL order, OrderSessionFeaturesDescriptorBHL features) {
        final TextWithKey state = OrderUtils.findTextWithKey(features.getMinutesOfTheConsultation(), order.getMinutesOfTheConsultation());

        final Section section = new Section(I18n.I.orderEntryMinutesOfTheConsultation());
        final List<OrderConfirmationDisplay.SimpleEntry>entries = section.getEntries();
        entries.add(new Entry(I18n.I.orderEntryMinutesOfTheConsultationState(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(state)));
        entries.add(new Entry(I18n.I.orderEntryMinutesOfTheConsultationNumber(),
                Renderer.STRING_DOUBLE_DASH.render(order.getMinutesOfTheConsultationNumber())));
        entries.add(new Entry(I18n.I.orderEntryMinutesOfTheConsultationDate(),
                Renderer.STRING_DOUBLE_DASH.render(PmRenderers.DATE_STRING.render(order.getConsultationDate()))));

        getSections().add(section);
    }

    public void addCommissionSection(OrderDataTypeBHL order) {
        final ProvisionType commission = order.getProvisionType();
        final String differentCommission = order.getProvisionValue();

        final Section section = new Section(I18n.I.orderEntryBHLKGSSectionCommission());
        final List<OrderConfirmationDisplay.SimpleEntry>entries = section.getEntries();
        entries.add(new Entry(I18n.I.orderEntryBHLKGSCommission(), OeRenderers.BHLKGS_PROVISION_TYPE_ABBR_RENDERER.render(commission)));
        entries.add(new Entry(I18n.I.orderEntryBHLKGSDifferentCommission(),
                OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_RENDERER.render(differentCommission)));

        getSections().add(section);
    }

    protected void addOthersSection(OrderDataTypeBHL order, OrderSessionFeaturesDescriptorBHL features, LogBuilder log) {
        final TextWithKey settlementType = OrderUtils.findTextWithKey(features.getSettlementTypes(), order.getSettlementType());
        if(isStrict() && settlementType == null) {
            DebugUtil.logToServer(log.add("Settlement type not found").add("order.settlementType", order.getSettlementType()).toString()); //$NON-NLS$
            throw new RuntimeException("Selected settlement type not found. This should never happen"); //$NON-NLS$
        }

        final TextWithKey businessSegment = OrderUtils.findTextWithKey(features.getBusinessSegments(), order.getBusinessSegment());
        if(isStrict() && businessSegment == null) {
            DebugUtil.logToServer(log.add("Business segment not found").add("order.businessSegment", order.getBusinessSegment()).toString()); //$NON-NLS$
            throw new RuntimeException("Selected business segment not found. This should never happen"); //$NON-NLS$
        }

        final TextWithKey placementOfOrderVia = OrderUtils.findTextWithKey(features.getOrderPlacementVia(), order.getOrderPlacementVia());

        final Section section = new Section(I18n.I.others());
        final List<OrderConfirmationDisplay.SimpleEntry>entries = section.getEntries();
        entries.add(new Entry(I18n.I.orderEntrySettlementType(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(settlementType)));
        entries.add(new Entry(I18n.I.orderEntryBusinessSegment(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(businessSegment)));
        entries.add(new OrderConfirmationDisplay.Entry(I18n.I.orderEntryPlacingOfOrderVia(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(placementOfOrderVia)));
        entries.add(new Entry(I18n.I.orderEntryExternalTypist(),
                Renderer.STRING_DOUBLE_DASH.render(order.getOrdererExtern())));
        entries.add(new Entry(I18n.I.orderEntryBHLKGSContractDate(),
                PmRenderers.DATE_TIME_STRING.render(order.getOrderDateTime())));
        getSections().add(section);
    }

    protected void addTextSections(OrderDataTypeBHL order, OrderSessionFeaturesDescriptorBHL features, LogBuilder log) {
        final List<TextWithKey> cannedTexts = features.getTextLibrariesBillingDocument();
        final TextWithKey cannedTextForBillingReceipt1 = OrderUtils.findTextWithKey(cannedTexts, order.getBillingDocument1());
        final TextWithKey cannedTextForBillingReceipt2 = OrderUtils.findTextWithKey(cannedTexts, order.getBillingDocument2());
        final TextWithKey cannedTextForBillingReceipt3 = OrderUtils.findTextWithKey(cannedTexts, order.getBillingDocument3());

        final TextWithKey cannedTextForOrderConfirmation = OrderUtils.findTextWithKey(features.getTextLibrariesOrderConfirmation(),
                order.getOrderConfirmation());

        String textForOrderReceipt1 = null;
        String textForOrderReceipt2 = null;
        String textForInternalUse1 = null;
        String textForInternalUse2 = null;

        final List<TextWithTyp> textWithTypes = order.getFreeText();
        for(TextWithTyp t : textWithTypes) {
            switch(t.getTyp()) {
                case TT_FREE_TEXT_ORDER_DOCUMENT_1:
                    textForOrderReceipt1 = t.getText();
                    break;
                case TT_FREE_TEXT_ORDER_DOCUMENT_2:
                    textForOrderReceipt2 = t.getText();
                    break;
                case TT_INTERNAL_TEXT_1:
                    textForInternalUse1 = t.getText();
                    break;
                case TT_INTERNAL_TEXT_2:
                    textForInternalUse2 = t.getText();
                    break;
            }
        }

        final Section section1 = new Section(I18n.I.orderEntryCannedTextForBillingReceiptSection(), 2);
        final List<OrderConfirmationDisplay.SimpleEntry>entries1 = section1.getEntries();
        entries1.add(new Entry(I18n.I.orderEntryCannedTextForBillingReceipt1(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(cannedTextForBillingReceipt1)));
        entries1.add(new Entry(I18n.I.orderEntryCannedTextForBillingReceipt2(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(cannedTextForBillingReceipt2)));
        entries1.add(new Entry(I18n.I.orderEntryCannedTextForBillingReceipt3(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(cannedTextForBillingReceipt3)));
        getSections().add(section1);

        final Section section2 = new Section(I18n.I.orderEntryCannedTextForOrderConfirmationSection(), 2);
        section2.getEntries().add(new Entry(I18n.I.orderEntryCannedText(),
                OeRenderers.TEXT_WITH_KEY_RENDERER.render(cannedTextForOrderConfirmation)));
        getSections().add(section2);

        final Section section3 = new Section(I18n.I.orderEntryTextForOrderReceiptSection(), 2);
        section3.getEntries().add(new Entry(I18n.I.orderEntryTextForOrderReceipt1(),
                Renderer.STRING_DOUBLE_DASH.render(textForOrderReceipt1)));
        section3.getEntries().add(new Entry(I18n.I.orderEntryTextForOrderReceipt2(),
                Renderer.STRING_DOUBLE_DASH.render(textForOrderReceipt2)));
        getSections().add(section3);

        final Section section4 = new Section(I18n.I.orderEntryTextForInternalUseSection(), 2);
        final List<OrderConfirmationDisplay.SimpleEntry>entries4 = section4.getEntries();
        entries4.add(new Entry(I18n.I.orderEntryTextForInternalUse1(),
                Renderer.STRING_DOUBLE_DASH.render(textForInternalUse1)));
        entries4.add(new Entry(I18n.I.orderEntryTextForInternalUse2(),
                Renderer.STRING_DOUBLE_DASH.render(textForInternalUse2)));
        getSections().add(section4);
    }

    protected void addCompletedTransactionsSection(OrderDataTypeBHL order) {
        final Section section = new Section(I18n.I.orderEntryBHLKGSCompletedTransactions(), 2);
        if(order.getClearingData() != null && !order.getClearingData().isEmpty()) {
            section.getEntries().add(getClearingDataWidget(order.getClearingData()));
        }
        else if(StringUtil.hasText(order.getClearingDataErrorMessage())) {
            section.getEntries().add(new SimpleEntry(order.getClearingDataErrorMessage()));
        }
        else {
            section.getEntries().add(new SimpleEntry(I18n.I.none()));
        }
        this.getSections().add(section);
    }

    protected void addPredecessorAndSuccessorTransactionsSection(OrderDataTypeBHL order) {
        final Section section = new Section(I18n.I.orderEntryBHLKGSPredecessorAndSuccessorTransactions(), 2);

        if(order.getClearingData() != null && !order.getClearingData().isEmpty()) {
            final List<ClearingData> filteredClearingData = new ArrayList<ClearingData>();

            for(final ClearingData clearingData : order.getClearingData()) {
                if(isNotEmptyNorZero(clearingData.getGeschaeftsnummerVorgaenger())
                        || isNotEmptyNorZero(clearingData.getGeschaeftsnummerNachfolger())) {
                    filteredClearingData.add(clearingData);
                }
            }

            if(!filteredClearingData.isEmpty()) {
                section.getEntries().add(getPredecessorAndSuccessorTransactionsClearingDataWidget(filteredClearingData));
            }
            else {
                section.getEntries().add(new SimpleEntry(I18n.I.none()));
            }
        }
        else if(StringUtil.hasText(order.getClearingDataErrorMessage())) {
            section.getEntries().add(new SimpleEntry(order.getClearingDataErrorMessage()));
        }
        else {
            section.getEntries().add(new SimpleEntry(I18n.I.none()));
        }
        this.getSections().add(section);
    }

    protected void addStatusSections(OrderDataTypeBHL order, OrderSessionFeaturesDescriptorBHL features, boolean withCancellation) {
        final Section section = new Section(I18n.I.orderEntryState(), 2);
        final List<SimpleEntry> entries = section.getEntries();

        if(withCancellation) {
            final TextWithKey reason = OrderUtils.findTextWithKey(features.getCancellationReason(), order.getCancellationReason());

            entries.add(new Entry(I18n.I.orderEntryKGSOrderCancellationReason(),
                    OeRenderers.TEXT_WITH_KEY_RENDERER.render(reason)));
            entries.add(new Entry(I18n.I.orderEntryKGSPrintCancellationConfirmation(),
                    Renderer.BOOLEAN_YES_NO_RENDERER.render(order.isCancellationConfirmationPrint())));
        }
        section.getEntries().add(getStatusWidgetEntry(order));
        this.getSections().add(section);
    }

    private WidgetEntry getStatusWidgetEntry(OrderDataTypeBHL order) {
        final ArrayList<OrderStatusBHL> status = new ArrayList<OrderStatusBHL>(10);
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState090(), order.getStatus090()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState091(), order.getStatus091()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState092(), order.getStatus092()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState093(), order.getStatus093()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState094(), order.getStatus094()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState095(), order.getStatus095()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState096(), order.getStatus096()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState097(), order.getStatus097()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState098(), order.getStatus098()));
        status.add(getDescribedOrderStatus(I18n.I.orderEntryKGSOrderState099(), order.getStatus099()));

        final TableColumnModelBuilder tb = new TableColumnModelBuilder();
        tb.addColumns(
                new TableColumn("", 150f),
                new TableColumn(I18n.I.orderEntryState(), 130f),
                new TableColumn(I18n.I.dateTime(), 115f).withRenderer(OeRenderers.PM_DATE_TIME_STRING_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.user(), 90f)
        );
        final SnippetTableWidget snippetTableWidget = new SnippetTableWidget(tb.asTableColumnModel());

        final TableDataModel tableDataModel = DefaultTableDataModel.create(status, new AbstractRowMapper<OrderStatusBHL>() {
            @Override
            public Object[] mapRow(OrderStatusBHL os) {
                return new Object[]{
                        os.getDescription(),
                        os.getText(),
                        os.getDateTime(),
                        os.getEditor()
                };
            }
        });
        snippetTableWidget.updateData(tableDataModel);

        return new WidgetEntry(snippetTableWidget);
    }

    private WidgetEntry getClearingDataWidget(List<ClearingData> clearingDataList) {
        final TableColumnModelBuilder tb = new TableColumnModelBuilder();
        tb.addColumns(
                new TableColumn(I18n.I.orderEntryBHLKGSTransactionNumberAbbr(), 80f).withRenderer(TableCellRenderers.STRING),
                new TableColumn(I18n.I.orderEntryState(), 70f).withRenderer(TableCellRenderers.STRING),
                new TableColumn(I18n.I.orderEntryAmountNominal(), 100f).withRenderer(OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryBHLKGSCurrencyAbbr(), .0f).withRenderer(TableCellRenderers.STRING),
                new TableColumn(I18n.I.price(), 75f).withRenderer(OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryBHLKGSCurrencyAbbr(), .0f).withRenderer(TableCellRenderers.STRING),
                new TableColumn(I18n.I.orderEntryBHLKGSForeignCurrencyPriceAbbr(), 65f).withRenderer(OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryBHLKGSValueDate(), 70f).withRenderer(OeRenderers.PM_DATE_STRING_TABLE_CELL_RENDERER)
        );
        final SnippetTableWidget snippetTableWidget = new SnippetTableWidget(tb.asTableColumnModel());
        final TableDataModel tableDataModel = DefaultTableDataModel.create(clearingDataList, new AbstractRowMapper<ClearingData>() {
            @Override
            public Object[] mapRow(ClearingData cd) {
                return new Object[]{
                        cd.getGeschaeftsnummer(),
                        cd.getStatus(),
                        cd.getNennwert(),
                        cd.getWaehrung(),
                        cd.getKurs(),
                        cd.getDevisenkursWaehrung(),
                        cd.getDevisenkurs(),
                        cd.getValuta()
                };
            }
        });
        snippetTableWidget.updateData(tableDataModel);

        return new WidgetEntry(snippetTableWidget);
    }

    private WidgetEntry getPredecessorAndSuccessorTransactionsClearingDataWidget(List<ClearingData> clearingDataList) {
        final TableColumnModelBuilder tb = new TableColumnModelBuilder();
        tb.addColumns(
                new TableColumn(I18n.I.orderEntryBHLKGSTransactionNumberAbbr(), 80f).withRenderer(TableCellRenderers.STRING),
                new TableColumn(I18n.I.orderEntryBHLKGSPrecedingTransactionNumberAbbr(), 80f).withRenderer(OeRenderers.STRING_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryBHHLKSSucceedingTransactionNumberAbbr(), 80f).withRenderer(OeRenderers.STRING_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER)
        );
        final SnippetTableWidget snippetTableWidget = new SnippetTableWidget(tb.asTableColumnModel());
        final TableDataModel tableDataModel = DefaultTableDataModel.create(clearingDataList, new AbstractRowMapper<ClearingData>() {
            @Override
            public Object[] mapRow(ClearingData cd) {
                return new Object[]{
                        cd.getGeschaeftsnummer(),
                        cd.getGeschaeftsnummerVorgaenger(),
                        cd.getGeschaeftsnummerNachfolger()
                };
            }
        });
        snippetTableWidget.updateData(tableDataModel);

        return new WidgetEntry(snippetTableWidget);
    }

    private OrderStatusBHL getDescribedOrderStatus(String description, OrderStatusBHL status) {
        if(status != null) {
            status.setDescription(description);
        }
        return status;
    }

    private String renderLimitValue(String value, String currencyAbbr) {
        return OeRenderers.PRICE_WITH_CURRENCY_NOT_NULL_OR_ZERO_RENDERER.render(new PriceWithCurrency(value, currencyAbbr));
    }

    public void addReversalSection(OrderDataTypeBHL order) {
        //TODO: pending until BhL knows what they want...
        final Section section = new Section(I18n.I.orderEntryBHLKGSReversal());
        final List<SimpleEntry> entries = section.getEntries();

        String reversalReferenceNumber = "";
        if(isNotEmptyNorZero(order.getOOOSTORGESCH10())) {
            reversalReferenceNumber += order.getOOOSTORGESCH10();
        }
        if(isNotEmptyNorZero(order.getOOOSTORGESCH3())) {
            if(!reversalReferenceNumber.isEmpty()) {
                reversalReferenceNumber += " ";
            }
            reversalReferenceNumber += order.getOOOSTORGESCH3();
        }

        entries.add(new Entry(I18n.I.orderEntryBHLKGSReversalReferenceNumber(),
                Renderer.STRING_DOUBLE_DASH.render(reversalReferenceNumber)));

        this.getSections().add(section);
    }

    private static boolean isNotEmptyNorZero(String value) {
        return StringUtil.hasText(value) && !"0".equals(value); //$NON-NLS$
    }
}
