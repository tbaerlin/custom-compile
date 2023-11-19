/*
 * AccountMmTalkerController.java
 *
 * Created on 16.01.13 12:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.IsPrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Lösch
 */
public class AccountPortraitController extends AbstractDepotObjectPortraitController<Account> implements IsPrivacyModeProvider {
    private BlockAndTalker<Account.AccountsTalker, List<Account>, Account> batAccount;
    private BlockAndTalker<UserDefinedFields.Talker, List<UserDefinedFields>, UserDefinedFields> batUserDefinedFields;
    private Account account;

    private InvestorPrivacyModeProvider privacyModeProvider;

    public AccountPortraitController() {
        super(MMClassIndex.CI_T_KONTO, MMClassIndex.CI_T_KONTO, UMRightBody.UMRB_READ_DOCUMENTS, UMRightBody.UMRB_EDIT_ACTIVITY_INSTANCE);
    }

    @Override
    public BlockAndTalker[] initBats() {
        batAccount = new BlockAndTalker<>(this.context, new Account.AccountsTalker());

        batUserDefinedFields = new BlockAndTalker<>(
                this.context,
                new UserDefinedFields.Talker(
                        Formula.create("Konto").withUserFieldCategories( // $NON-NLS$
                                PmWebSupport.getInstance().getUserFieldDecls(ShellMMType.ST_KONTO)
                        )
                )
        );

        privacyModeProvider = new InvestorPrivacyModeProvider(this.context) {
            @Override
            public HistoryToken getPrivacyModeEntryToken() {
                return newAccountToken();
            }
        };

        return new BlockAndTalker[]{
                this.batAccount,
                this.batUserDefinedFields,
                this.privacyModeProvider.getBat()
        };
    }

    private HistoryToken newAccountToken() {
        final HistoryToken.Builder builder = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_ACCOUNT)
                .with(OBJECTID_KEY, this.account.getId());
        return builder.build();
    }

    protected NavItemSpec initNavItems(List<WorkspaceSheetDesc> sheets) {
        final UserObjectController<Account, AccountPortraitController, UserObjectDisplay.UserObjectPresenter<Account>> accountUOC = new UserObjectController<>(this, new AccountView());
        final HistoryToken defaultToken = newAccountToken();
        final HistoryToken investorToken = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE).with(OBJECTID_KEY, this.account.getInvestorId()).build();

        final List<NavItemSpec> linkedChildren = new ArrayList<>();
        linkedChildren.add(new NavItemSpec("VI-" + this.account.getInvestorId(), this.account.getInvestorName(), investorToken, // $NON-NLS$
                EmptyContext.create(this.account.getName()).withIconKey("pm-investor-account")).withIsTransient().withIcon("pm-investor")); // $NON-NLS$
        if (StringUtil.hasText(this.account.getPortfolioId())) {
            final HistoryToken portfolioToken = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PORTFOLIO).with(OBJECTID_KEY, this.account.getPortfolioId()).build();
            linkedChildren.add(new NavItemSpec("VP-" + this.account.getPortfolioId(), this.account.getPortfolioName(), portfolioToken, // $NON-NLS$
                    EmptyContext.create(this.account.getName()).withIconKey("pm-investor-account")).withIsTransient().withIcon("pm-investor-portfolio")); // $NON-NLS$
        }
        final NavItemSpec root = new NavItemSpec("root", "root"); // $NON-NLS$
        root.addChild(
                setFallback(new NavItemSpec(AbstractUserObjectView.SC_STATIC, I18n.I.staticData(), defaultToken, accountUOC)).withOpenWithSelection().withClosingSiblings().addChildren(
                        new NavItemSpec(AbstractUserObjectView.SC_USER_DEFINED_FIELDS, I18n.I.userDefinedFields(), defaultToken, accountUOC)
                )
        );

        root.addChild(new NavItemSpec("V", I18n.I.linkedObjects()).withClosingSiblings().addChildren(linkedChildren)); // $NON-NLS$

        addActivityNode(root);

        addDmsNode(root);

        root.addChild(new NavItemSpec("R", I18n.I.pmAnalyses()).withHasDelegate().withClosingSiblings()  // $NON-NLS$
                .withSelectFirstChildOnOpen()
                .addChildren(createLayoutChildren(defaultToken, this.account.getName(), this.account.getId(), sheets)));

        return root;
    }

    @Override
    protected ShellMMType getShellMMType() {
        return ShellMMType.ST_KONTO;
    }

    @Override
    protected Widget getNavNorthWidget() {
        if (this.account == null) {
            Firebug.log("account is null!");
            return null;
        }
        return ObjectWidgetFactory.createAccountWidget(this.account);
    }

    @Override
    public Account createUserObject() {
        final List<Account> resultObject = this.batAccount.createResultObject();
        assert resultObject.size() == 1 : "unexpected account list size. Must be 1!";

        final List<UserDefinedFields> userDefinedFieldsList = this.batUserDefinedFields.createResultObject();
        assert userDefinedFieldsList.size() == 1 : "unexpected user defined fields list size. Must be 1!";

        final Account account = resultObject.get(0);
        account.setUserDefinedFields(userDefinedFieldsList.get(0));
        account.withAlerts(getAlerts());

        return account;
    }

    @Override
    protected Account getUserObject() {
        return this.account;
    }

    @Override
    protected void setUserObject(Account userObject) {
        this.account = userObject;
    }

    @Override
    protected String getName(Account userObject) {
        return I18n.I.staticData() + " " + userObject.getName();
    }

    @Override
    protected String getControllerId() {
        return PmWebModule.HISTORY_TOKEN_ACCOUNT;
    }

    @Override
    public PrivacyModeProvider asPrivacyModeProvider() {
        return this.privacyModeProvider;
    }
}