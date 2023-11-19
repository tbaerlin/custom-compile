/*
 * ParameterMap.java
 *
 * Created on 25.03.2014 08:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Markus Dick
 */
@NonNLS
public final class ParameterMap implements Map<String, Object> {
    public static final String DEPOT_ID = "DEPOT_ID";
    //The initially set depot-id. If Order Entry is opened via the investor, this parameter has to be null!
    public static final String DEPOT_ID_INITIAL = "DEPOT_ID_INITIAL";
    public static final String INVESTOR_ID = "INVESTOR_ID";
    public static final String ORDER_ACTION_TYPE = "ORDER_ACTION_TYPE";
    public static final String ISIN = "ISIN";
    public static final String SETTLEMENT_ACCOUNT_ID = "SETTLEMENT_ACCOUNT_ID";

    public static final String QUANTITY = "QUANTITY";

    public static final String LIMIT_CURRENCY_ISO_3 = "LIMIT_CURRENCY_ISO_3";
    public static final String LIMIT_VALUE = "LIMIT_VALUE";

    public static final String ORDER_MESSAGE = "ORDER_MESSAGE";
    public static final String ORDER_NUMBER = "ORDER_NUMBER";
    public static final String PROCESSED_QUANTITY = "PROCESSED_QUANTITY";

    public static final String PERSON_CUSTOMER_NUMBER = "PERSON_CUSTOMER_NUMBER";

    public static final String COMMENT = "COMMENT";

    public static final String ACTIVITY_INSTANCE_ID = "ACTIVITY_INSTANCE_ID";
    public static final String ACTIVITY_LIST_ENTRY_ID = "ACTIVITY_LIST_ENTRY_ID";

    private final Map<String, Object> map = new HashMap<>();

    public ParameterMap() {
    }

    public ParameterMap(String depotId) {
        setDepotId(depotId);
    }

    private void setOrResetParameter(String name, Object value) {
        if(name == null) throw new IllegalArgumentException("<ParameterMap> name of parameter must not be null!");
        if(value == null) {
            this.map.remove(name);
            return;
        }

        this.map.put(name, value);
    }

    public void setActivityInstanceId(String activityInstanceId) {
        setOrResetParameter(ACTIVITY_INSTANCE_ID, activityInstanceId);
    }

    public String getActivityInstanceId() {
        return (String)this.map.get(ACTIVITY_INSTANCE_ID);
    }

    @SuppressWarnings("unused")
    public void setActivityListEntryId(String activityListEntryId) {
        setOrResetParameter(ACTIVITY_LIST_ENTRY_ID, activityListEntryId);
    }

    public String getActivityListEntryId() {
        return (String)this.map.get(ACTIVITY_LIST_ENTRY_ID);
    }

    public void setDepotId(String depotId) {
        setOrResetParameter(DEPOT_ID, depotId);
    }

    @SuppressWarnings("unused")
    public void setDepotIdInitial(String depotId) {
        setOrResetParameter(DEPOT_ID_INITIAL, depotId);
    }

    public String getDepotId() {
        final String depotId = (String) this.map.get(DEPOT_ID);

        if(!StringUtil.hasText(depotId)) {
            return getDepotIdInitial();
        }

        return depotId;
    }
    public String getDepotIdInitial() {
        return (String)this.map.get(DEPOT_ID_INITIAL);
    }

    @SuppressWarnings("unused")
    public void setSettlementAccount(String depotId) {
        setOrResetParameter(SETTLEMENT_ACCOUNT_ID, depotId);
    }

    public String getSettlementAccount() {
        return (String)this.map.get(SETTLEMENT_ACCOUNT_ID);
    }

    public void setOrderActionType(OrderActionType orderActionType) {
        setOrResetParameter(ORDER_ACTION_TYPE, orderActionType);
    }

    public OrderActionType getOrderActionType() {
        return (OrderActionType)this.map.get(ORDER_ACTION_TYPE);
    }

    public String getInvestorId() {
        return (String)this.map.get(INVESTOR_ID);
    }

    public void setInvestorId(String investorId) {
        setOrResetParameter(INVESTOR_ID, investorId);
    }

    public void setIsin(String isin) {
        setOrResetParameter(ISIN, isin);
    }

    public String getIsin() {
        return (String)this.map.get(ISIN);
    }

    public void setQuantity(String quantity) {
        setOrResetParameter(QUANTITY, quantity);
    }

    public String getQuantity() {
        return (String)this.map.get(QUANTITY);
    }

    public void setComment(String orderMessage) {
        setOrResetParameter(COMMENT, orderMessage);
    }

    public String getComment() {
        return (String)this.map.get(COMMENT);
    }

    public void setOrderNumber(String orderNumber) {
        setOrResetParameter(ORDER_NUMBER, orderNumber);
    }

    public String getOrderNumber() {
        return (String)this.map.get(ORDER_NUMBER);
    }

    public void setOrderMessage(String orderMessage) {
        setOrResetParameter(ORDER_MESSAGE, orderMessage);
    }

    public String getLimitValue() {
        return (String)this.map.get(LIMIT_VALUE);
    }

    @SuppressWarnings("unused")
    public void setLimitValue(String orderMessage) {
        setOrResetParameter(LIMIT_VALUE, orderMessage);
    }

    public String getLimitCurrencyIso3() {
        return (String)this.map.get(LIMIT_CURRENCY_ISO_3);
    }

    @SuppressWarnings("unused")
    public void setLimitCurrencyIso3(String orderMessage) {
        setOrResetParameter(LIMIT_CURRENCY_ISO_3, orderMessage);
    }

    public String getPersonCustomerNumber() {
        return (String)this.map.get(PERSON_CUSTOMER_NUMBER);
    }

    @SuppressWarnings("unused")
    public void setPersonCustomer(String personNumber) {
        setOrResetParameter(PERSON_CUSTOMER_NUMBER, personNumber);
    }

    @SuppressWarnings("unused")
    public String getOrderMessage() {
        return (String)this.map.get(ORDER_MESSAGE);
    }

    public String getProcessedQuantity() {
        return (String)this.map.get(PROCESSED_QUANTITY);
    }

    public void setProcessedQuantity(String processedQuantity) {
        setOrResetParameter(PROCESSED_QUANTITY, processedQuantity);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return this.map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return this.map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return this.map.remove(key);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void putAll(Map<? extends String, ?> m) {
        this.map.putAll(m);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public String toString() {
        return "ParameterMap" + map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterMap)) return false;

        final ParameterMap that = (ParameterMap) o;

        return this.map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
}