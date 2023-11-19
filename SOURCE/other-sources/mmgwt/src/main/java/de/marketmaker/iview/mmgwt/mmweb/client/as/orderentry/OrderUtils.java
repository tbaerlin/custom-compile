/*
 * OrderUtils.java
 *
 * Created on 16.10.13 09:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.Filter;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.PmxmlConstants;
import de.marketmaker.iview.pmxml.TextType;
import de.marketmaker.iview.pmxml.TextWithKey;
import de.marketmaker.iview.pmxml.TextWithTyp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Markus Dick
 */
public final class OrderUtils {
    private OrderUtils() {
        /* do nothing */
    }

    public static double calculateExpectedMarketValue(double quantity, double quoteValue, double conversionFactor) {
        //Original PM impl.: (Abs(AOrder.Quantity) * (QuoteValue / Kursfaktor))
        return Math.abs(quantity) * (quoteValue / conversionFactor);
    }

    public static TextWithKey newTextWithKey(String value, String label) {
       return newTextWithKey(value, label, false);
    }

    public static TextWithKey newTextWithKey(String value, String label, boolean isDefault) {
        final TextWithKey t = new TextWithKey();
        t.setKey(value);
        t.setText(label);
        t.setDefault(isDefault);
        return t;
    }

    public static TextWithKey findDefaultTextWithKey(List<TextWithKey> list, boolean selectFirst) {
        if(list == null || list.isEmpty()) {
            return null;
        }

        for(TextWithKey value : list) {
            if(value.isDefault()) {
                return value;
            }
        }

        if(selectFirst) {
            return list.get(0);
        }

        return null;
    }

    public static TextWithKey findTextWithKey(List<TextWithKey> textWithKeys, String key) {
        if(key != null) {
            for(TextWithKey twk : textWithKeys) {
                if(key.equals(twk.getKey())) {
                    return twk;
                }
            }
        }
        return null;
    }

    public static TextWithTyp findTextWithType(List<TextWithTyp> textWithTypes, TextType type) {
        if(type != null) {
            for(TextWithTyp twt : textWithTypes) {
                if(type.equals(twt.getTyp())) {
                    return twt;
                }
            }
        }
        return null;
    }

    public static TextWithTyp newTextWithType(TextType type, String label) {
        final TextWithTyp t = new TextWithTyp();
        t.setTyp(type);
        t.setText(label);
        return t;
    }

    public static CurrencyAnnotated findDefaultCurrencyAnnotated(List<CurrencyAnnotated> list) {
        for(CurrencyAnnotated value : list) {
            if(value.isIsDefault()) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> List<E> cast(Class<E> clazz, List<T> src) {
        for(T t : src) {
            if(t != null && clazz != t.getClass()) {
                Firebug.warn("PmxmlUtil.cast: expected type " + clazz.getName() + " but found " + t.getClass().getName());
            }
        }
        return (List<E>)src;
    }

    public static OrderActionType toOrderActionType(OrderTransaktionType orderTransaktionType) {
        if(orderTransaktionType == null) {
            return null;
        }

        switch(orderTransaktionType) {
            case TT_BUY:
                return OrderActionType.AT_BUY;
            case TT_SELL:
                return OrderActionType.AT_SELL;
            case TT_SUBSCRIBE:
                return OrderActionType.AT_SUBSCRIBE;
            default:
                return null;
        }
    }

    public static OrderTransaktionType toOrderTransactionType(OrderAction orderAction) {
        if(orderAction == null) {
            return toOrderTransactionType((OrderActionType)null);
        }
        return toOrderTransactionType(orderAction.getValue());
    }

    public static OrderTransaktionType toOrderTransactionType(OrderActionType orderActionType) {
        if(orderActionType == null) {
            return OrderTransaktionType.TT_NA;
        }

        switch(orderActionType) {
            case AT_BUY:
                return OrderTransaktionType.TT_BUY;
            case AT_SELL:
                return OrderTransaktionType.TT_SELL;
            case AT_SUBSCRIBE:
                return OrderTransaktionType.TT_SUBSCRIBE;
            default:
                return OrderTransaktionType.TT_NA;
        }
    }

    public static boolean isOrderActionSupported(OrderSession orderSession, OrderAction orderAction) {
        return orderAction != null && isOrderActionTypeSupported(orderSession, orderAction.getValue());
    }

    public static boolean isOrderActionTypeSupported(OrderSession orderSession, OrderActionType orderActionType) {
        if(orderActionType == null) {
            return false;
        }

        final List<OrderAction> orderActions = orderSession.getFeatures().getOrderActions();
        for (OrderAction orderAction : orderActions) {
            if(orderActionType.equals(orderAction.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static MmJsDate toMmJsDate(String pmMmTalkDateString) {
        if(StringUtil.hasText(pmMmTalkDateString)) {
            final Date date = Formatter.PM_DATE_TIME_FORMAT_MMTALK.parse(pmMmTalkDateString);
            if(!PmxmlConstants.ZERO_DATE.equals(date)) {
                return new MmJsDate(date);
            }
        }
        return null;
    }

    public static AccountData toAccountData(AccountRef r) {
        final AccountData a = new AccountData();
        a.setId(r.getId());
        a.setNumber(r.getNumber());
        a.setName(r.getName());
        a.setCurrency(r.getCurrency());
        a.setBank(r.getBank());
        return a;
    }

    public static <T> List<T> filter(List<T> items, Filter<T> filter) {
        if(filter == null) {
            return items;
        }

        final List<T> filtered = new ArrayList<T>();
        for(T item : items) {
            if(filter.isAcceptable(item)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public static boolean isWithDmXmlSymbolSearch() {
        return Customer.INSTANCE.isCustomerAS() && Customer.INSTANCE.asCustomerAS().isOrderEntryWithDmXmlSymbolSearch();
    }
}
