/*
 * SpsOrderEntryWidget.java
 *
 * Created on 07.05.2014 10:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.HasReturnParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.PresenterDisposedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.PresenterDisposedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.IsBrokingAllowedMethod;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.OrderEntryGoToDelegate;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.MainInput;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.math.BigDecimal;
import java.util.HashSet;

/**
 * @author Markus Dick
 */
public class SpsOrderEntryWidget extends SpsBoundWidget<Button, SpsGroupProperty> implements PresenterDisposedHandler,
        SpsAfterPropertiesSetHandler {
    public static final String LIST_ENTRY_ID = "ListEntryID";  // $NON-NLS$
    public static final String SECURITY_ACCOUNT = "SecurityAccount";  // $NON-NLS$
    public static final String SECURITY_ACCOUNT_ID = "SecurityAccountID";  // $NON-NLS$
    public static final String TRANSACTION_TYPE = "TransactionType";  // $NON-NLS$
    public static final String SECURITY = "Security";  // $NON-NLS$
    public static final String QUANTITY = "Quantity";  // $NON-NLS$
    public static final String SETTLEMENT_ACCOUNT = "SettlementAccount";  // $NON-NLS$
    public static final String SETTLEMENT_ACCOUNT_ID = "SettlementAccountID";  // $NON-NLS$
    public static final String LIMIT = "Limit";  // $NON-NLS$
    public static final String LIMIT_CURRENCY = "LimitCurrency";  // $NON-NLS$
    public static final String PERSON_CUSTOMER_NUMBER = "PersonCustomerNumber";  // $NON-NLS$
    public static final String COMMENT = "Comment";  // $NON-NLS$
    public static final String ENABLED = "Enabled";  // $NON-NLS$
    public static final String PROCESSED_QUANTITY = "ProcessedQuantity";  // $NON-NLS$

    private final ParameterMap parameterMap;
    private final OrderEntryGoToDelegate orderEntry;
    private final Button button;
    private final ChangeHandler childPropertyChangeHandler;
    private final ChangeHandler depotChildPropertyChangeHandler;
    private final HashSet<HandlerRegistration> childPropertyChangeHandlerRegistrations;

    private final IsBrokingAllowedMethod isBrokingAllowedMethod;
    private Boolean brokingAllowed = null;
    private String brokingAllowedDepotId = null;

    private String activityInstanceId;
    private MainInput mainInput;
    private Command refreshInternalCommand;

    public SpsOrderEntryWidget() {
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);

        this.parameterMap = new ParameterMap();
        this.orderEntry = new OrderEntryGoToDelegate(
                OrderEntryGoToDelegate.Type.BY_PARAMETER_MAP,
                this.parameterMap,
                this);

        this.button = Button.text(getCaption())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        doOnClick();
                    }
                })
                .build();

        this.childPropertyChangeHandlerRegistrations = new HashSet<>();
        this.childPropertyChangeHandler = new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onChildPropertiesChanged();
            }
        };

        this.depotChildPropertyChangeHandler = new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onDepotChildPropertiesChanged();
            }
        };

        this.isBrokingAllowedMethod = new IsBrokingAllowedMethod(IsBrokingAllowedMethod.Type.ACTIVITY,
                new AsyncCallback<IsBrokingAllowedMethod.IsBrokingAllowedMethodResult>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.error("<SpsOrderEntryWidget.isBrokingAllowedMethod.onFailure>", caught);
                brokingAllowed = false;
                brokingAllowedDepotId = null;
                updateButtonVisibility();
            }

            @Override
            public void onSuccess(IsBrokingAllowedMethod.IsBrokingAllowedMethodResult result) {
                brokingAllowed = result.isAllowed();
                brokingAllowedDepotId = result.getDepotId();
                updateButtonVisibility();
                fillParameterMap();
            }
        });
    }

    public SpsOrderEntryWidget withActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
        return this;
    }

    public SpsOrderEntryWidget withMainInput(MainInput mainInput) {
        this.mainInput = mainInput;
        return this;
    }

    private void onChildPropertiesChanged() {
        try {
            updateButtonVisibility();
        }
        catch(IllegalStateException e) {
            Notifications.add(I18n.I.error(), e.getMessage());
        }
    }

    private void onDepotChildPropertiesChanged() {
        try {
            requestPrivilegeAndUpdateButtonVisibility();
        }
        catch(IllegalStateException e) {
            Notifications.add(I18n.I.error(), e.getMessage());
        }
    }

    @Override
    public void onPropertyChange() {
        //called only if any variables in the bind to the group change!
        Firebug.debug("<SpsOrderEntryWidget.onPropertyChange>");
        update();
    }

    @Override
    public void afterPropertiesSet() {
        update();
    }

    @Override
    public void release() {
        super.release();
        removeChildPropertyHandlers();
    }

    private void update() {
        if(!Selector.AS_ORDERING.isAllowed()) {
            this.button.setVisible(false);
            this.button.setEnabled(false);
            return;
        }

        try {
            updateChildPropertyChangeHandlers();
            requestPrivilegeAndUpdateButtonVisibility();
        }
        catch(IllegalStateException e) {
            Notifications.add(I18n.I.error(), e.getMessage());
        }
    }

    private void requestPrivilegeAndUpdateButtonVisibility() {
        if(this.mainInput == null) {
            Firebug.warn("<SpsOrderEntryWidget.requestPrivilegeAndUpdateButtonVisibility> mainInput is not set!");
            this.brokingAllowed = false;
            this.brokingAllowedDepotId = null;
            updateButtonVisibility();
            return;
        }

        final SpsGroupProperty group = getBindFeature().getSpsProperty();
        final String shellMMInfoIdOrString = getShellMMInfoIdOrString(group, SECURITY_ACCOUNT, SECURITY_ACCOUNT_ID);
        this.isBrokingAllowedMethod.invoke(StringUtil.hasText(shellMMInfoIdOrString)
                ? shellMMInfoIdOrString : this.mainInput.getId());
    }

    protected void doOnClick() {
        try {
            fillParameterMap();
        }
        catch(IllegalStateException e) {
            Notifications.add(I18n.I.error(), e.getMessage());
            return;
        }
        this.orderEntry.goTo(null);
    }

    private void updateChildPropertyChangeHandlers() {
        removeChildPropertyHandlers();

        final SpsGroupProperty group = getBindFeature().getSpsProperty();

        addChildPropertyChangeHandler(group, LIST_ENTRY_ID);
        addChildPropertyChangeHandler(group, ENABLED);
        addChildPropertyChangeHandler(group, TRANSACTION_TYPE);
        addChildPropertyChangeHandler(group, SECURITY);
        addChildPropertyChangeHandler(group, PROCESSED_QUANTITY);
        addDepotChildPropertyChangeHandler(group, SECURITY_ACCOUNT, SECURITY_ACCOUNT_ID);
    }

    private void addChildPropertyChangeHandler(SpsGroupProperty group, String childPropertyName) {
        addChildPropertyChangeHandler(group, this.childPropertyChangeHandler, childPropertyName);
    }

    private void addDepotChildPropertyChangeHandler(SpsGroupProperty group, String... childPropertyNames) {
        addChildPropertyChangeHandler(group, this.depotChildPropertyChangeHandler, childPropertyNames);
    }

    private void addChildPropertyChangeHandler(SpsGroupProperty group, ChangeHandler childPropertyChangeHandler, String... alternativeChildPropertyNames) {
        if(alternativeChildPropertyNames.length == 1) {
            final SpsProperty childProperty = group.get(alternativeChildPropertyNames[0]);
            if(childProperty != null) {
                this.childPropertyChangeHandlerRegistrations.add(childProperty.addChangeHandler(childPropertyChangeHandler));
                return;
            }
            else {
                throw new RuntimeException("Failed to bind changeHandler! Property \"" + alternativeChildPropertyNames[0] + "\" not found!");  // $NON-NLS$
            }
        }

        boolean childPropertyBound = false;
        for (String alternativeChildPropertyName : alternativeChildPropertyNames) {
            final SpsProperty childProperty = group.get(alternativeChildPropertyName);
            if(childProperty != null) {
                this.childPropertyChangeHandlerRegistrations.add(childProperty.addChangeHandler(childPropertyChangeHandler));
                childPropertyBound = true;
            }
        }
        if(!childPropertyBound) {
            throw new RuntimeException("Failed to bind changeHandler! At least one of the properties \"" + StringUtil.join(',', alternativeChildPropertyNames) + "\" must exist!");  // $NON-NLS$
        }
    }

    private void removeChildPropertyHandlers() {
        for (HandlerRegistration registration : this.childPropertyChangeHandlerRegistrations) {
            if(registration != null) {
                registration.removeHandler();
            }
        }
    }

    private void fillParameterMap() {
        final SpsGroupProperty group = getBindFeature().getSpsProperty();
        final HashSet<String> keys = toHashSet(group);

        this.parameterMap.clear();
        if(this.activityInstanceId != null) {
            this.parameterMap.setActivityInstanceId(this.activityInstanceId);
            this.parameterMap.setInvestorId(this.activityInstanceId);
        }
        if(this.mainInput != null) {
            if(MMClassIndex.CI_T_INHABER == this.mainInput.getMMClassIndex()) {
                this.parameterMap.setInvestorId(this.mainInput.getId());
            }
        }

        putString(group, keys, LIST_ENTRY_ID, ParameterMap.ACTIVITY_LIST_ENTRY_ID, false);

        final String shellMMInfoIdOrString = getShellMMInfoIdOrString(group, SECURITY_ACCOUNT, SECURITY_ACCOUNT_ID);
        if(StringUtil.hasText(this.brokingAllowedDepotId) && StringUtil.hasText(shellMMInfoIdOrString)
                && !StringUtil.equals(this.brokingAllowedDepotId, shellMMInfoIdOrString)) {
            final String msg = "SpsOrderEntryWidget - activity instance ID " + this.activityInstanceId + " and investor ID " + this.parameterMap.getInvestorId() + ": IsBrokingAllowed responded with depot ID " + this.brokingAllowedDepotId + " but explicitly set depot ID of activity was " + shellMMInfoIdOrString + ". Using set depot ID, but probably the user has not the necessary privileges for this depot";  // $NON-NLS$
            DebugUtil.logToServer(msg);
            DebugUtil.showDeveloperNotification(msg);
            Firebug.warn(msg);
        }
        putShellMMInfoIdOrString(group, keys, SECURITY_ACCOUNT, SECURITY_ACCOUNT_ID, ParameterMap.DEPOT_ID_INITIAL);

        putString(group, keys, PERSON_CUSTOMER_NUMBER, ParameterMap.PERSON_CUSTOMER_NUMBER);
        putOrderActionType(group, keys);
        putShellMMInfoISIN(group, keys, SECURITY, ParameterMap.ISIN);
        final BigDecimal remainingQuantity = getRemainingQuantity(group);
        if(remainingQuantity != null) {
            putString(keys, QUANTITY, remainingQuantity.toPlainString(), ParameterMap.QUANTITY);
        }
        else {
            putString(group, keys, QUANTITY, ParameterMap.QUANTITY);
        }
        putShellMMInfoIdOrString(group, keys, SETTLEMENT_ACCOUNT, SETTLEMENT_ACCOUNT_ID, ParameterMap.SETTLEMENT_ACCOUNT_ID);
        putString(group, keys, LIMIT, ParameterMap.LIMIT_VALUE);
        putString(group, keys, LIMIT_CURRENCY, ParameterMap.LIMIT_CURRENCY_ISO_3);
        putString(group, keys, COMMENT, ParameterMap.COMMENT);

        if(!keys.isEmpty()) {
            for (String key : new HashSet<>(keys)) {
                putDataItem(group, keys, key, key);
            }
        }
    }

    private void putShellMMInfoIdOrString(SpsGroupProperty group, HashSet<String> keys, String shellMMInfoBind, String stringValueBind, String parameterName) {
        final ShellMMInfo info = getShellMMInfo(group, shellMMInfoBind);
        keys.remove(shellMMInfoBind);
        if(info != null) {
            this.parameterMap.put(parameterName, info.getId());
        }
        else {
            putString(group, keys, stringValueBind, parameterName, false);
        }
    }

    private String getShellMMInfoIdOrString(SpsGroupProperty group, String shellMMInfoBind, String stringValueBind) {
        final ShellMMInfo info = getShellMMInfo(group, shellMMInfoBind);
        if(info != null) {
            return info.getId();
        }
        else {
            return getString(group, stringValueBind);
        }
    }

    private void updateButtonVisibility() {
        final SpsGroupProperty group = getBindFeature().getSpsProperty();
        final boolean enabled = "true".equals(getString(group, ENABLED));  // $NON-NLS$
        final OrderActionType orderActionType = SpsUtil.toOrderActionType(getString(group, TRANSACTION_TYPE));
        final boolean quantityProcessed = isQuantityProcessed(group);

        this.button.setEnabled(enabled && (this.brokingAllowed != null ? this.brokingAllowed : false) && !quantityProcessed && orderActionType != null);
    }

    private boolean isQuantityProcessed(SpsGroupProperty group) {
        BigDecimal subtract = getRemainingQuantity(group);
        return subtract != null && subtract.compareTo(BigDecimal.ZERO) <= 0;
    }

    public BigDecimal getBigDecimal(SpsGroupProperty group, String bindKey) {
        return toBigDecimal(getString(group, bindKey));
    }

    public BigDecimal getBigDecimal(SpsGroupProperty group, String bindKey, BigDecimal defaultValue) {
        return toBigDecimal(getString(group, bindKey), defaultValue);
    }

    private BigDecimal toBigDecimal(String numberString) {
        return toBigDecimal(numberString, null);
    }

    private BigDecimal toBigDecimal(String numberString, BigDecimal defaultValue) {
        if(StringUtil.hasText(numberString)) {
            try {
                return new BigDecimal(numberString);
            }
            catch(NumberFormatException nfe) {
                Firebug.error("Cannot convert \"" + numberString + "\" to decimal value", nfe);
            }
        }
        return defaultValue;
    }

    private BigDecimal getRemainingQuantity(SpsGroupProperty group) {
        final BigDecimal qty = getBigDecimal(group, QUANTITY);
        final BigDecimal pqty = getBigDecimal(group, PROCESSED_QUANTITY);

        if(qty != null && pqty != null) {
            return qty.subtract(pqty);
        }
        return null;
    }

    private HashSet<String> toHashSet(SpsGroupProperty group) {
        final HashSet<String> keys = new HashSet<>();
        for(SpsProperty child : group.getChildren()) {
            keys.add(child.getBindKey());
        }
        return keys;
    }

    private void putOrderActionType(SpsGroupProperty group, HashSet<String> bindKeys) {
        this.parameterMap.setOrderActionType(SpsUtil.toOrderActionType(getString(group, TRANSACTION_TYPE)));
        bindKeys.remove(TRANSACTION_TYPE);
    }

    private void putString(HashSet<String> bindKeys, String bindKey, String value, String parameterName) {
        this.parameterMap.put(parameterName, value);
        bindKeys.remove(bindKey);
    }

    private void putString(SpsGroupProperty group, HashSet<String> bindKeys, String bindKey, String parameterName, boolean propertyMustExist) {
        this.parameterMap.put(parameterName, getString(group, bindKey, propertyMustExist));
        bindKeys.remove(bindKey);
    }

    private void putString(SpsGroupProperty group, HashSet<String> bindKeys, String bindKey, String parameterName) {
        putString(group, bindKeys, bindKey, parameterName, false);
    }

    private void putDataItem(SpsGroupProperty group, HashSet<String> bindKeys, String bindKey, String parameterName) {
        this.parameterMap.put(parameterName, getDataItem(group, bindKey));
        bindKeys.remove(bindKey);
    }

    @SuppressWarnings("unused")
    private void putShellMMInfoId(SpsGroupProperty group, HashSet<String> bindKeys, String bindKey, String parameterName) {
        final ShellMMInfo info = getShellMMInfo(group, bindKey);
        bindKeys.remove(bindKey);

        if(info != null && ShellMMType.ST_DEPOT == info.getTyp()) {
            this.parameterMap.put(parameterName, info.getId());
        }
        else {
            this.parameterMap.put(parameterName, null);

            if(info != null && ShellMMType.ST_DEPOT != info.getTyp()) {
                Firebug.debug("<SpsOrderEntryWidget.putShellMMInfoId> ShellMMType.ST_DEPOT expected, but was" + info.getTyp());
            }
        }
    }

    private void putShellMMInfoISIN(SpsGroupProperty group, HashSet<String> bindKeys, String bindKey, String parameterName) {
        final ShellMMInfo info = getShellMMInfo(group, bindKey);
        bindKeys.remove(bindKey);
        if(info != null) {
            this.parameterMap.put(parameterName, info.getISIN());
        }
        else {
            this.parameterMap.put(parameterName, null);
            Firebug.debug("<SpsOrderEntryWidget.putShellMMInfoISIN> ShellMMInfo object is null! \"" + bindKey + "\" -> " + parameterName);
        }
    }

    @SuppressWarnings("unused")
    private void putShellMMInfo(SpsGroupProperty group, HashSet<String> bindKeys, String bindKey, String parameterName) {
        final ShellMMInfo info = getShellMMInfo(group, bindKey);
        bindKeys.remove(bindKey);
        if(info != null) {
            this.parameterMap.put(parameterName, info);
        }
        else {
            this.parameterMap.put(parameterName, null);
            Firebug.debug("<SpsOrderEntryWidget.putShellMMInfoId> ShellMMInfo object is null! \"" + bindKey + "\" -> " + parameterName);
        }
    }

    private static MM getDataItem(SpsGroupProperty groupProperty, String bindKey) {
        final SpsLeafProperty property = (SpsLeafProperty)groupProperty.get(bindKey);
        if(property == null) {
            return null;
        }

        return property.getDataItem();
    }

    private static ShellMMInfo getShellMMInfo(SpsGroupProperty groupProperty, String bindKey) {
        final SpsLeafProperty property = (SpsLeafProperty) groupProperty.get(bindKey);
        if(property == null){
            return null;
        }
        return property.getShellMMInfo();
    }

    private static String getString(SpsGroupProperty groupProperty, String bindKey) {
        return getString(groupProperty, bindKey, false);
    }

    private static String getString(SpsGroupProperty groupProperty, String bindKey, boolean propertyMustExist) {
        final SpsLeafProperty property = (SpsLeafProperty)groupProperty.get(bindKey);
        if(propertyMustExist && property == null) {
            throw new IllegalStateException("<SpsOrderEntryWidget> mandatory property " + bindKey + " is missing in bound group node!"); // $NON-NLS$
        }
        else if(property == null){
            return null;
        }

        return property.getStringValue();
    }

    @Override
    protected HTML createCaptionWidget() {
        return new HTML("&nbsp;"); // $NON-NLS$
    }

    @Override
    protected Button createWidget() {
        if (isReadonly()) {
            return null;
        }
        this.button.setHTML(getCaption());
        return this.button;
    }

    @Override
    public SafeHtml getCaption() {
        final SafeHtml caption = super.getCaption();
        if(caption == null)  {
            return SafeHtmlUtils.fromString(I18n.I.enterOrder());
        }
        return caption;
    }

    @Override
    public void onPresenterDisposed(PresenterDisposedEvent event) {
        final HasReturnParameterMap hasReturnParameterMap = event.asHasReturnParameterMap();
        Firebug.debug("<SpsOrderEntryWidget.onPresenterDisposed> hasReturnParameterMap?" + hasReturnParameterMap);
        if(hasReturnParameterMap != null) {
            final ParameterMap returnParameterMap = hasReturnParameterMap.getReturnParameterMap();
            Firebug.debug("<SpsOrderEntryWidget.onPresenterDisposed> " + returnParameterMap);
            final SpsGroupProperty group = getBindFeature().getSpsProperty();

            final String processedQuantity = addOrderedQuantityToProcessedQuantity(returnParameterMap, group);

            StringBuilder sb;
//            sb = updateChildProperty(group, ORDER_MESSAGE, returnParameterMap.getOrderMessage(), null);
//            sb = updateChildProperty(group, ORDER_NUMBER, returnParameterMap.getOrderNumber(), sb);
            sb = updateChildProperty(group, PROCESSED_QUANTITY, processedQuantity, null);
            //TODO: should we update all other map entries that correspond to child properties?
            if(sb != null) {
                Dialog.error(sb.toString());
            }
        }
        if(this.refreshInternalCommand != null) {
            this.refreshInternalCommand.execute();
        }
    }

    private String addOrderedQuantityToProcessedQuantity(ParameterMap returnParameterMap, SpsGroupProperty group) {
        final BigDecimal orderedQty = toBigDecimal(returnParameterMap.getProcessedQuantity());
        final BigDecimal pqty = getBigDecimal(group, PROCESSED_QUANTITY, BigDecimal.ZERO);
        final String processedQuantity;
        if(orderedQty != null) {
            processedQuantity = pqty.add(orderedQty).toPlainString();
        }
        else {
            processedQuantity = null;
        }
        return processedQuantity;
    }

    private StringBuilder updateChildProperty(SpsGroupProperty group, String propertyName, String value, StringBuilder exceptionMessage) {
        final SpsLeafProperty orderMessage = (SpsLeafProperty) group.get(propertyName);
        if(orderMessage != null) {
            orderMessage.setValue(value, true, true);
            return exceptionMessage;
        }

        final String msg = "Failed to update property " + propertyName + ". It does not exist!"; // $NON-NLS$
        Firebug.warn(msg);
        if(exceptionMessage == null) {
            exceptionMessage = new StringBuilder(msg);
        }
        else {
            exceptionMessage.append(' ').append(msg);
        }
        return exceptionMessage;
    }

    public SpsOrderEntryWidget withInternalRefresh(Command command) {
        this.refreshInternalCommand = command;
        return this;
    }
}
