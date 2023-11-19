package de.marketmaker.istar.merger.provider.profile;

import org.springframework.web.client.ResponseExtractor;


public interface CounterService {
    <T> T countAccess(String customer, String user, String selector,
                      String quality, ResponseExtractor<T> handler);

    <T> T countAccess(String appId, String customer, String user,
            String ident,
            String quality, ResponseExtractor<T> handler);

    <T> T readAccount(String customer, String user, ResponseExtractor<T> handler);

    /**
     * increment the user's login counter.
     * @param appId appId
     * @param customer mandant
     * @param login the user's login name
     */
    void countLogin(String appId, String customer, String login);

    void countLogout(String appId, String customer, String login);

    @Deprecated
    void countLogin(String customer, String login);
    @Deprecated
    void countLogout(String customer, String login);
}
