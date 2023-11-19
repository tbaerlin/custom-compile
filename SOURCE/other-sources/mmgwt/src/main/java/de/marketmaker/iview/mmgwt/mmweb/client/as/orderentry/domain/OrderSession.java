package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.BHLDeactivatedObjectFilter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.Filter;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AccountInfo;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.AllocateOrderSessionDataResponse;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptor;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptorBHL;
import de.marketmaker.iview.pmxml.SessionState;
import de.marketmaker.iview.pmxml.ShellMMRef;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellMMTypeDesc;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 30.10.12 10:38
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 * @author Markus Dick
 */

public class OrderSession<F extends OrderSessionFeaturesDescriptor> {
    //I18n messages that are intended to be used in OrderMethods take the OrderSession as a parameter
    //and that are expected to change between the different broking modules.
    public interface Messages {
        String orderEntryOrcTimeout();
        String orderEntryOrcTimeoutSendOrder();
        String orderEntryOrcTimeoutChangeOrder();
        String orderEntryOrcTimeoutCancelOrder();
    }

    private final static Messages DEFAULT_MESSAGES = new DefaultMessages();

    private final String handle;
    private final BrokerageModuleID brokerageModuleID;
    private final SessionState orderSessionState;
    private final F features;
    private final AccountInfo accountInfo;
    private final Set<ShellMMType> tradableSecurityTypes;
    private final Messages messages;

    private OrderSession(BrokerageModuleID brokerageModuleID, F features, AllocateOrderSessionDataResponse response, Messages m) {
        this.handle = response.getHandle();
        this.brokerageModuleID = brokerageModuleID;
        if(brokerageModuleID != response.getBrokerageModul()) {
            throw new IllegalArgumentException("Parameter brokerageModuleID and AllocateOrderSessionDataResponse.brokerageModuleID do not match!"); //$NON-NLS$
        }
        this.orderSessionState = response.getOrderSessionState();
        this.accountInfo = response.getAccountInfo();
        this.features = features;
        this.messages = m;

        if(this.features == null) {
            this.tradableSecurityTypes = Collections.emptySet();
        }
        else {
            this.tradableSecurityTypes = new HashSet<>();
            for(ShellMMTypeDesc typeDesc : this.features.getTradableSecurityTypes()) {
                this.tradableSecurityTypes.add(typeDesc.getT());
            }
        }

    }

    public String getHandle() {
        return this.handle;
    }

    public BrokerageModuleID getBrokerageModuleID() {
        return this.brokerageModuleID;
    }

    public SessionState getSessionState() {
        return this.orderSessionState;
    }

    public ShellMMRef getOwner() {
        return this.accountInfo.getOwner();
    }

    public ShellMMRef getPortfolio() {
        return this.accountInfo.getPortfolio();
    }

    public AccountRef getSecurityAccount() {
        return this.accountInfo.getSecurityAccount();
    }

    public List<AccountData> getAccountList() {
        return this.accountInfo.getAccountList();
    }

    public List<OrderSecurityInfo> getSecurityList() {
        return this.accountInfo.getSecurityList();
    }

    public F getFeatures() {
        return this.features;
    }

    public AccountInfo getAccountInfo() {
        return this.accountInfo;
    }

    public Set<ShellMMType> getTradableSecurityTypes() {
        return this.tradableSecurityTypes;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public static OrderSession createOrderSession(AllocateOrderSessionDataResponse response) {
        switch(response.getBrokerageModul()) {
            case BM_BHLKGS:
                return new OrderSession.OrderSessionBHLKGS(response);
            case BM_FUCHSBRIEFE:
                return new OrderSession.OrderSessionFuchsbriefe(response);
            case BM_HA:
                return new OrderSession.OrderSessionHA(response);
            default:
                return new OrderSession.DefaultOrderSession(response);
        }
    }

    private final static class DefaultMessages implements Messages {
        @Override
        public String orderEntryOrcTimeout() {
            return I18n.I.orderEntryOrcTimeout();
        }

        @Override
        public String orderEntryOrcTimeoutSendOrder() {
            return I18n.I.orderEntryOrcTimeoutSendOrder();
        }

        @Override
        public String orderEntryOrcTimeoutChangeOrder() {
            return I18n.I.orderEntryOrcTimeoutChangeOrder();
        }

        @Override
        public String orderEntryOrcTimeoutCancelOrder() {
            return I18n.I.orderEntryOrcTimeoutCancelOrder();
        }
    }

    public static class DefaultOrderSession extends OrderSession<OrderSessionFeaturesDescriptor> {
        private DefaultOrderSession(AllocateOrderSessionDataResponse response) {
            super(response.getBrokerageModul(), response.getFeatures(), response, DEFAULT_MESSAGES);
        }
    }

    public static class OrderSessionHA extends OrderSession<OrderSessionFeaturesDescriptor> {
        public OrderSessionHA(AllocateOrderSessionDataResponse response) {
            super(BrokerageModuleID.BM_HA, response.getFeatures(), response, DEFAULT_MESSAGES);
        }
    }

    public static class OrderSessionFuchsbriefe extends OrderSession<OrderSessionFeaturesDescriptor> {
        public OrderSessionFuchsbriefe(AllocateOrderSessionDataResponse response) {
            super(BrokerageModuleID.BM_FUCHSBRIEFE, response.getFeatures(), response, DEFAULT_MESSAGES);
        }
    }

    public static class OrderSessionBHLKGS extends OrderSession<OrderSessionFeaturesDescriptorBHL> {
        private final static class MessagesBHLKGS implements Messages {
            @Override
            public String orderEntryOrcTimeout() {
                return I18n.I.orderEntryOrcTimeout();
            }

            @Override
            public String orderEntryOrcTimeoutSendOrder() {
                return I18n.I.orderEntryOrcTimeoutSendOrderBHLKGS();
            }

            @Override
            public String orderEntryOrcTimeoutChangeOrder() {
                return I18n.I.orderEntryOrcTimeoutChangeOrderBHLKGS();
            }

            @Override
            public String orderEntryOrcTimeoutCancelOrder() {
                return I18n.I.orderEntryOrcTimeoutCancelOrderBHLKGS();
            }
        }

        private final static MessagesBHLKGS MESSAGES_BHLKGS = new MessagesBHLKGS();

        private final AccountFilterBHL accountFilter;
        private List<AccountData> filteredAccounts;
        private final ArrayList<TextWithKey> kwsOrderBlaetternStateIDs;

        public OrderSessionBHLKGS(AllocateOrderSessionDataResponse response) {
            super(BrokerageModuleID.BM_BHLKGS, (OrderSessionFeaturesDescriptorBHL)response.getFeatures(), response, MESSAGES_BHLKGS);

            //TODO: In future versions, this list should be delivered by pm server.
            this.kwsOrderBlaetternStateIDs = new ArrayList<>();
            this.kwsOrderBlaetternStateIDs.addAll(Arrays.asList(
                    OrderUtils.newTextWithKey("N", "Offene oder teilausgeführte Orders", true), //$NON-NLS$
                    OrderUtils.newTextWithKey("G", "Gesamtausgeführte Orders"), //$NON-NLS$
                    OrderUtils.newTextWithKey("A", "Alle Orders inkl. gestrichene"))); //$NON-NLS$

            if(Customer.INSTANCE.isCustomerAS() && "BHL".equals(Customer.INSTANCE.asCustomerAS().getOrderEntryAccountChoiceFilterName())) { //$NON-NLS$
                this.accountFilter = new AccountFilterBHL();
            }
            else {
                this.accountFilter = null;
            }
        }

        public List<TextWithKey> getKWSOrderBlaetternStateIDs() {
            return this.kwsOrderBlaetternStateIDs;
        }

        @Override
        public List<AccountData> getAccountList() {
            if(this.accountFilter == null) {
                return super.getAccountList();
            }
            if(this.filteredAccounts == null) {
                this.filteredAccounts = OrderUtils.filter(super.getAccountList(), this.accountFilter);
            }
            return this.filteredAccounts;
        }

        private static class AccountFilterBHL implements Filter<AccountData> {
            private static final BHLDeactivatedObjectFilter BHL_DEACTIVATED_OBJECT_FILTER = new BHLDeactivatedObjectFilter();
            @Override
            public boolean isAcceptable(AccountData accountData) {
                return accountData == null
                        || BHL_DEACTIVATED_OBJECT_FILTER.isAcceptable(accountData.getNumber());
            }
        }
    }
}

