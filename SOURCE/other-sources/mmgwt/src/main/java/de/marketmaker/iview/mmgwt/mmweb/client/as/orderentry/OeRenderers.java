/*
 * OeRenderers.java
 *
 * Created on 24.01.13 12:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.i18n.client.NumberFormat;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PriceWithCurrency;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceWithCurrencyRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.ExternExchangeType;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDepository;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderExpirationType;
import de.marketmaker.iview.pmxml.OrderLock;
import de.marketmaker.iview.pmxml.OrderStock;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.ProvisionType;
import de.marketmaker.iview.pmxml.TextWithKey;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.DEFAULT_DECIMAL_SEPARATOR;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.DEFAULT_GROUPING_SEPARATOR;

/**
 * Some strictly OE specific renderers
 * @author Markus Dick
 */
public final class OeRenderers {
    public static final Renderer<OrderActionType> ORDER_ACTION_TYPE_RENDERER = new OrderActionTypeRenderer();
    public static final Renderer<OrderAction> ORDER_ACTION_RENDERER = new OrderActionRenderer();
    public static final Renderer<OrderAction> ORDER_ACTION_NOUN_RENDERER = new OrderActionNounRenderer();
    public static final Renderer<String> UNAMBIGUOUS_NUMBER_RENDERER = new UnambiguousNumberRenderer();
    public static final Renderer<String> UNAMBIGUOUS_NUMBER_RENDERER_0_5 = new UnambiguousNumberRenderer(0, 5);
    public static final Renderer<ExternExchangeType> EXTERN_EXCHANGE_TYPE_RENDERER = new ExternExchangeTypeRenderer();
    public static final Renderer<OrderExchangeInfo> ORDER_EXCHANGE_INFO_RENDERER = new OrderExchangeInfoRenderer();
    public static final Renderer<OrderTransaktionType> ORDER_TRANSACTION_TYPE_RENDERER = new OrderTransactionTypeRenderer();
    public static final Renderer<TextWithKey> TEXT_WITH_KEY_RENDERER = new TextWithKeyRenderer();
    public static final Renderer<OrderCurrency> ORDER_CURRENCY_RENDERER = new OrderCurrencyRenderer();
    public static final Renderer<PriceWithCurrency> PRICE_WITH_CURRENCY_NOT_NULL_OR_ZERO_RENDERER = new PriceWithCurrencyNotNullOrZeroRenderer();
    public static final Renderer<String> PRICE_NOT_EMPTY_OR_ZERO_RENDERER = new PriceNotEmptyOrZeroRenderer();
    public static final Renderer<String> STRING_NOT_EMPTY_OR_ZERO_RENDERER = new StringNotEmptyOrZeroRenderer();
    public static final Renderer<OrderExpirationType> ORDER_EXPIRATION_TYPE_RENDERER = new OrderExpirationTypeRenderer();
    public static final Renderer<OrderStock> BHLKGS_DEPOSITORY_LIST_ITEM_RENDERER = new DepositoryListItemRenderer();
    public static final DepositoryRenderer DEPOSITORY_RENDERER = new DepositoryRenderer();
    public static final Renderer<AccountRef> ACCOUNT_LIST_ITEM_RENDERER = new AccountListItemRenderer();

    public static final Renderer<DisplayHA.ValidUntilChoice> HA_VALID_UNTIL_CHOICE_RENDERER = new HAValidUntilChoiceRenderer();

    public static final Renderer<DisplayBHLKGS.ValidUntil> BHLKGS_VALID_UNTIL_CHOICE_RENDERER = new BHLKGSValidUntilChoiceRenderer();
    public static final Renderer<ProvisionType> BHLKGS_PROVISION_TYPE_RENDERER = new ProvisionTypeRenderer();
    public static final Renderer<ProvisionType> BHLKGS_PROVISION_TYPE_ABBR_RENDERER = new ProvisionTypeRenderer(true);

    public static final TableCellRenderer PRICE_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER
            = new TableCellRenderers.DelegateRenderer<>(PRICE_NOT_EMPTY_OR_ZERO_RENDERER, "mm-right"); // $NON-NLS$

    public static final TableCellRenderer PRICE_WITH_CURRENCY_TABLE_CELL_RENDERER =
            new TableCellRenderers.DelegateRenderer<>(PRICE_WITH_CURRENCY_NOT_NULL_OR_ZERO_RENDERER, "mm-right"); //$NON-NLS$

    public static final TableCellRenderer STRING_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER
            = new TableCellRenderers.DelegateRenderer<>(STRING_NOT_EMPTY_OR_ZERO_RENDERER);

    public static final TableCellRenderer PM_DATE_STRING_TABLE_CELL_RENDERER =
            new TableCellRenderers.DelegateRenderer<>(new Renderer<String>(){
                @Override
                public String render(String s) {
                    return STRING_DOUBLE_DASH.render(PmRenderers.DATE_STRING.render(s));
                }
            });

    public static final TableCellRenderer PM_DATE_TIME_STRING_TABLE_CELL_RENDERER =
            new TableCellRenderers.DelegateRenderer<>(new Renderer<String>(){
                @Override
                public String render(String s) {
                    return STRING_DOUBLE_DASH.render(PmRenderers.DATE_TIME_STRING.render(s));
                }
            });

    private OeRenderers() {
        /* do nothing */
    }

    public static class OrderActionTypeRenderer implements Renderer<OrderActionType> {
        @Override
        public String render(OrderActionType type) {
            if(type == null) {
                return STRING_DOUBLE_DASH.render(null);
            }

            switch(type) {
                case AT_BUY:
                    return I18n.I.orderEntryBuyTransaction();
                case AT_SELL:
                    return I18n.I.orderEntrySellTransaction();
                case AT_SUBSCRIBE:
                    return I18n.I.orderEntrySubscribeTransaction();
                default:
                    return type.name();
            }
        }
    }

    public static class OrderActionRenderer implements Renderer<OrderAction> {
        @Override
        public String render(OrderAction orderAction) {
            if(orderAction == null || orderAction.getValue() == null) {
                return STRING_DOUBLE_DASH.render(null);
            }

            return ORDER_ACTION_TYPE_RENDERER.render(orderAction.getValue());
        }
    }

    public static class OrderActionNounRenderer implements Renderer<OrderAction> {
        @Override
        public String render(OrderAction orderAction) {
            final OrderActionType orderActionType = orderAction.getValue();

            switch(orderActionType) {
                case AT_BUY:
                    return I18n.I.orderEntryBuying();
                case AT_SELL:
                    return I18n.I.orderEntrySelling();
                default:
                    return orderActionType.name();
            }
        }
    }

    public static class OrderTransactionTypeRenderer implements Renderer<OrderTransaktionType> {
        @Override
        public String render(OrderTransaktionType orderTransaktionType) {
            switch(orderTransaktionType) {
                case TT_BUY:
                    return I18n.I.orderEntryBuyTransaction();
                case TT_SELL:
                    return I18n.I.orderEntrySellTransaction();
                case TT_SUBSCRIBE:
                    return I18n.I.orderEntrySubscribeTransaction();
                default:
                    return orderTransaktionType.name();
            }
        }
    }

    public static class UnambiguousNumberRenderer implements Renderer<String> {
        private final NumberFormat numberFormat;

        public UnambiguousNumberRenderer() {
            this(NumberFormat.getFormat("0.###")); //$NON-NLS$
        }

        public UnambiguousNumberRenderer(int minFractionDigits, int maxFractionDigits) {
            this(NumberFormat.getFormat("0.###").overrideFractionDigits(minFractionDigits, maxFractionDigits)); //$NON-NLS$
        }

        private UnambiguousNumberRenderer(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
        }

        @Override
        public String render(String numberString) {
            if(StringUtil.hasText(numberString)) {
                try {
                    final String preNormalized = numberString.replace(String.valueOf(DEFAULT_GROUPING_SEPARATOR),
                            String.valueOf(DEFAULT_DECIMAL_SEPARATOR));

                    final int firstDecimal = preNormalized.indexOf(DEFAULT_DECIMAL_SEPARATOR);
                    final int secondDecimal = preNormalized.indexOf(DEFAULT_DECIMAL_SEPARATOR, firstDecimal + 1);

                    final String normalized;
                    if(secondDecimal < 0) {
                        normalized = preNormalized;
                    }
                    else {
                        normalized = preNormalized.substring(0, secondDecimal);
                    }

                    final double d = numberFormat.parse(normalized.trim());
                    return numberFormat.format(d);
                }
                catch(NumberFormatException e) {
                    Firebug.log("<UnambiguousNumberRenderer.render> Cannot render number: " + numberString);
                }
            }
            return numberString;
        }
    }

    public static class OrderExchangeInfoRenderer implements Renderer<OrderExchangeInfo> {
        @Override
        public String render(OrderExchangeInfo orderExchangeInfo) {
            if(orderExchangeInfo == null) {
                return Renderer.STRING_DOUBLE_DASH.render(null);
            }

            final String label;
            if(orderExchangeInfo.isUseExtern()) {
                label = orderExchangeInfo.getExternName();
            }
            else {
                label = orderExchangeInfo.getName();
            }

            if(orderExchangeInfo.isBestExecution()) {
                return label + " (" + I18n.I.orderEntryBestExecutionAbbr() + ")";
            }

            return label;
        }
    }

    public static class CurrencyAnnotatedRenderer implements Renderer<CurrencyAnnotated> {
        @Override
        public String render(CurrencyAnnotated currency) {
            if(currency == null) {
                return "";
            }

            //Add current holdings info from PSI Transaction Data
            if(StringUtil.hasText(currency.getExtInfo())) {
                return ORDER_CURRENCY_RENDERER.render(currency.getCurrency()) + " " + currency.getExtInfo();
            }

            return ORDER_CURRENCY_RENDERER.render(currency.getCurrency());
        }
    }

    public static class OrderCurrencyRenderer implements Renderer<OrderCurrency> {
        @Override
        public String render(OrderCurrency currency) {
            if(currency == null) {
                return "";
            }

            return currency.getKuerzel();
        }
    }

    public static class HAValidUntilChoiceRenderer implements Renderer<DisplayHA.ValidUntilChoice> {
        @Override
        public String render(DisplayHA.ValidUntilChoice validUntilChoice) {
            switch(validUntilChoice) {
                case GOOD_FOR_THE_DAY:
                    return I18n.I.orderEntryGoodForTheDay();
                case DATE:
                    return I18n.I.date();
                case ULTIMO:
                    return I18n.I.orderEntryUltimo();
                default:
                    return validUntilChoice.name();
            }
        }
    }

    public static class BHLKGSValidUntilChoiceRenderer implements Renderer<DisplayBHLKGS.ValidUntil> {
        @Override
        public String render(DisplayBHLKGS.ValidUntil validUntil) {
            switch(validUntil) {
                case TODAY:
                    return I18n.I.today();
                case DATE:
                    return I18n.I.date();
                case ULTIMO:
                    return I18n.I.orderEntryUltimo();
                case DEFAULT:
                default:
                    return I18n.I.orderEntryBHLKGSLimitValidityDefault();
            }
        }
    }

    public static class ExternExchangeTypeRenderer implements Renderer<ExternExchangeType> {
        @Override
        public String render(ExternExchangeType externExchangeType) {
            if(externExchangeType == null) {
                return STRING_DOUBLE_DASH.render(null);
            }

            switch(externExchangeType) {
                case EET_DOMESTIC:
                    return I18n.I.orderEntryBHLKGSExchangeDomestic();
                case EET_FOREIGN:
                    return I18n.I.orderEntryBHLKGSExchangeForeign();
                case EET_OTHER:
                default:
                    return I18n.I.orderEntryBHLKGSExchangeOther();
            }
        }
    }

    public static class TextWithKeyRenderer implements Renderer<TextWithKey> {
        @Override
        public String render(TextWithKey textWithKey) {
            if(textWithKey == null) {
                return Renderer.STRING_DOUBLE_DASH.render(null);
            }
            return Renderer.STRING_DOUBLE_DASH.render(textWithKey.getText());
        }
    }

    public static class PriceWithCurrencyNotNullOrZeroRenderer implements Renderer<PriceWithCurrency> {
        private static final Renderer<PriceWithCurrency> RENDERER = new PriceWithCurrencyRenderer(PRICE_0MAX5);

        @Override
        public String render(PriceWithCurrency value) {
            if(value != null) {
                if(!"0".equals(value.getPrice())) { //$NON-NLS$
                    return RENDERER.render(value);
                }
            }
            return Renderer.STRING_DOUBLE_DASH.render(null);
        }
    }

    public static class PriceNotEmptyOrZeroRenderer implements Renderer<String> {
        private static final Renderer<String> RENDERER = PRICE_0MAX5;

        @Override
        public String render(String value) {
            if(StringUtil.hasText(value)) {
                if(!"0".equals(value)) { //$NON-NLS$
                    return RENDERER.render(value);
                }
            }
            return Renderer.STRING_DOUBLE_DASH.render(null);
        }
    }

    public static class StringNotEmptyOrZeroRenderer implements Renderer<String> {
        @Override
        public String render(String value) {
            if("0".equals(value)) { //$NON-NLS$
                value = null;
            }

            return Renderer.STRING_DOUBLE_DASH.render(value);
        }
    }

    public static class OrderExpirationTypeRenderer implements Renderer<OrderExpirationType> {
        @Override
        public String render(OrderExpirationType orderExpirationType) {
            if(orderExpirationType == null) {
                return null;
            }
            return orderExpirationType.value();
        }
    }

    public static class DepositoryListItemRenderer implements Renderer<OrderStock> {
        @Override
        public String render(OrderStock orderStock) {
            if(orderStock == null) {
                return null;
            }

            final String fallBack = orderStock.getStockString();
            final String depository = orderStock.getDepositoryData() != null ? orderStock.getDepositoryData().getKuerzel() : null;
            final String lock = orderStock.getLockData() != null ? orderStock.getLockData().getKuerzel() : null;

            final String depositoryStr;
            if(!StringUtil.hasText(depository) && !StringUtil.hasText(lock)) {
                depositoryStr = fallBack;
            }
            else if(!StringUtil.hasText(lock)) {
                depositoryStr = depository;
            }
            else {
                depositoryStr = I18n.I.orderEntryBHLKGSDepositoryItemLock(depository, lock);
            }

            if(StringUtil.hasText(orderStock.getQuantityToSell())) {
                final String trimmed = depositoryStr != null ? depositoryStr.trim() : PmRenderers.PM_NA_ABBR;
                return I18n.I.orderEntryBHLKGSDepositoryItemQuantity(trimmed,
                        UNAMBIGUOUS_NUMBER_RENDERER_0_5.render(orderStock.getQuantityToSell()));
            }
            return depositoryStr;
        }
    }

    public static class DepositoryRenderer implements Renderer<OrderStock> {
        @Override
        public String render(OrderStock orderStock) {
            if(orderStock == null) {
                return null;
            }
            return render(orderStock.getDepositoryData(), orderStock.getLockData(), orderStock.getStockString());
        }

        public String render(OrderDepository depository, OrderLock lock, String fallBack) {
            if(depository == null && lock == null) {
                return fallBack;
            }
            else if(depository != null && (lock == null || !StringUtil.hasText(lock.getKuerzel()))) {
                return depository.getKuerzel();
            }
            else if(depository == null){
                return I18n.I.orderEntryBHLKGSDepositoryItemLock(PmRenderers.PM_NA_ABBR, lock.getKuerzel());
            }
            return I18n.I.orderEntryBHLKGSDepositoryItemLock(depository.getKuerzel(), lock.getKuerzel());
        }
    }

    public static class ProvisionTypeRenderer implements Renderer<ProvisionType> {
        private final boolean renderAbbr;

        public ProvisionTypeRenderer() {
            this(false);
        }
        public ProvisionTypeRenderer(boolean renderAbbr) {
            this.renderAbbr = renderAbbr;
        }

        @Override
        public String render(ProvisionType provisionType) {
            if(provisionType == null) {
                return null;
            }

            switch(provisionType) {
                case PT_NONE:
                    if(this.renderAbbr) {
                        return I18n.I.orderEntryBHLKGSProvisionTypeNONEAbbr();
                    }
                    return I18n.I.orderEntryBHLKGSProvisionTypeNONE();

                case PT_AMOUNT:
                    return I18n.I.orderEntryBHLKGSProvisionTypeAMOUNT();

                case PT_PERCENT:
                    return I18n.I.orderEntryBHLKGSProvisionTypePERCENT();

                case PT_NO_DIFFERING_PROVISION:
                    if(this.renderAbbr) {
                        return I18n.I.orderEntryBHLKGSProvisionTypeNO_DIFFERING_PROVISIONAbbr();
                    }
                    return I18n.I.orderEntryBHLKGSProvisionTypeNO_DIFFERING_PROVISION();

                default:
                    return provisionType.name();
            }
        }
    }

    public static class AccountListItemRenderer implements Renderer<AccountRef> {
        @Override
        public String render(AccountRef accountRef) {
            if(accountRef == null) {
                return STRING_DOUBLE_DASH.render(null);
            }

            final StringBuilder sb = new StringBuilder();

            if(StringUtil.hasText(accountRef.getName())) {
                sb.append(accountRef.getName());
            }
            else if(StringUtil.hasText(accountRef.getNumber())){
                sb.append(accountRef.getNumber());
            }
            else {
                sb.append(I18n.I.unknown());
            }

            if(accountRef.getCurrency() != null && StringUtil.hasText(accountRef.getCurrency().getKuerzel())) {
                sb.append(" (")
                        .append(accountRef.getCurrency().getKuerzel())
                        .append(")"); //$NON-NLS$
            }
            return sb.toString();
        }
    }
}
