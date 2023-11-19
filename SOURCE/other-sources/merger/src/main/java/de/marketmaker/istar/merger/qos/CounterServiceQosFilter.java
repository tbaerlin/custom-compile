package de.marketmaker.istar.merger.qos;


import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResponseExtractor;

import de.marketmaker.istar.merger.provider.profile.CounterService;

public class CounterServiceQosFilter extends QosFilter<CounterService> implements
        CounterService {
    private static final ResponseExtractor<Boolean> RESPONSE_EXTRACTOR
            = response -> response.getStatusCode() == HttpStatus.OK;

    private String customer;

    private String user;

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setUser(String user) {
        this.user = user;
    }

    protected boolean tryService() throws Exception {
        return this.delegate.readAccount(this.customer, this.user, RESPONSE_EXTRACTOR);
    }

    public <T> T countAccess(String customer, String user, String selector, String quality,
            ResponseExtractor<T> handler) {
        if (isEnabled()) {
            return this.delegate.countAccess(customer, user, selector, quality, handler);
        }
        return null;
    }

    @Override
    public <T> T countAccess(String appId, String customer, String user,
            String ident, String quality, ResponseExtractor<T> handler) {
        return this.delegate.countAccess(appId, customer, user, ident, quality, handler);
    }

    public <T> T readAccount(String customer, String user, ResponseExtractor<T> handler) {
        if (isEnabled()) {
            return this.delegate.readAccount(customer, user, handler);
        }
        return null;
    }

    public void countLogin(String appId, String customer, String login) {
        if (isEnabled()) {
            this.delegate.countLogin(appId, customer, login);
        }
    }

    public void countLogout(String appId, String customer, String login) {
        if (isEnabled()) {
            this.delegate.countLogout(appId, customer, login);
        }
    }

    public void countLogin(String customer, String login) {
        countLogin("7", customer, login);
    }

    public void countLogout(String customer, String login) {
        countLogout("7", customer, login);
    }
}
