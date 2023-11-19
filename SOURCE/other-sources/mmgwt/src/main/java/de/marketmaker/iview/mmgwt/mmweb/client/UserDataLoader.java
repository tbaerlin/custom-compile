/*
 * UserDataLoader.java
 *
 * Created on 03.02.2009 14:29:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.dmxml.MSCUserConfiguration;
import de.marketmaker.iview.dmxml.MSCUserLists;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PortfolioActions;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserDataLoader extends ResponseTypeCallback implements Command {
    private final DmxmlContext context = new DmxmlContext();

    private DmxmlContext.Block<MSCUserConfiguration> blockUserConfiguration;

    private DmxmlContext.Block<MSCUserLists> blockUserLists;

    private DmxmlContext.Block<MSCUserLists> blockProfiDepots;

    public UserDataLoader() {
        // cannot init here, SessionData.INSTANCE.getUser() may still return null
    }

    public void execute() {
        AbstractMainController.INSTANCE.updateProgress(I18n.I.loadUserdata());
        issueRequest();
    }

    private void issueRequest() {
        this.blockUserConfiguration = createConfigurationBlock();

        this.blockUserLists = createUserListsBlock(SessionData.INSTANCE.getUser().getVwdId());

        final String uid = SessionData.INSTANCE.getUserProperty(AppConfig.PROP_KEY_MUSTERDEPOT_USERID);
        if (uid != null) {
            this.blockProfiDepots = createUserListsBlock(uid);
        }

        this.context.setCancellable(false);
        this.context.issueRequest(this);
    }

    private DmxmlContext.Block<MSCUserLists> createUserListsBlock(String uid) {
        final DmxmlContext.Block<MSCUserLists> result = this.context.addBlock("MSC_User_Lists"); // $NON-NLS-0$
        result.setParameter("userid", uid); // $NON-NLS-0$
        result.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        result.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        return result;
    }

    private DmxmlContext.Block<MSCUserConfiguration> createConfigurationBlock() {
        final DmxmlContext.Block<MSCUserConfiguration> result
                = this.context.addBlock("MSC_User_Configuration"); // $NON-NLS-0$
        result.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        return result;
    }

    @Override
    protected void onResult() {
        if (this.throwable == null) {
            handleResult();
        }
        else {
            DebugUtil.logToServer("UserDataLoader failed", this.throwable); // $NON-NLS-0$
        }
    }

    private void handleResult() {
        if (this.blockUserLists.isResponseOk() &&
                this.blockUserLists.getResult().getPortfolios().getElement().isEmpty()) {
            final List<String> currencies = GuiDefsLoader.getPortfolioCurrencies();
            PortfolioActions.INSTANCE.createPortfolio(I18n.I.portfolio() + "1", "0", currencies.isEmpty() // $NON-NLS$
                    ? "EUR" // $NON-NLS$
                    : currencies.get(0), new ResponseTypeCallback() {
                @Override
                protected void onResult() {
                    issueRequest();
                }
            });
            return;
        }

        if (this.blockUserLists.isResponseOk() && this.blockUserConfiguration.isResponseOk()) {
            SessionData.INSTANCE.setUserData(this.blockUserLists.getResult(), this.blockUserConfiguration.getResult());
        }
        if (this.blockProfiDepots != null && this.blockProfiDepots.isResponseOk()) {
            SessionData.INSTANCE.setProfiData(this.blockProfiDepots.getResult());
        }

        AbstractMainController.INSTANCE.runInitSequence();
    }
}