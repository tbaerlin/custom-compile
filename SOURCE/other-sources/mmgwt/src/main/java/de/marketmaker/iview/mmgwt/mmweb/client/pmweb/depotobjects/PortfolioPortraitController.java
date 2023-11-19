/*
 * PortfolioPortraitController.java
 *
 * Created on 16.01.13 12:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.as.IsPrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItemComparator;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContextProducer;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.CreatePortfolioVersionPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PortfolioVersion;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.DeletePortfolioVersionRequest;
import de.marketmaker.iview.pmxml.DeletePortfolioVersionResponse;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.List;

/**
 * @author Michael Lösch
 */
public class PortfolioPortraitController extends AbstractDepotObjectPortraitController<Portfolio> implements IsPrivacyModeProvider {
    public static final String ICON_KEY = "pm-investor-portfolio";  // $NON-NLS$
    public static final String EDIT_PORTFOLIO_ALLOWED_AS = "editPortfolioAllowedAS";  // $NON-NLS$
    public static final String SC_CREATE_PORTFOLIO_VERSION = "CREATE_PV";  // $NON-NLS$

    private final CreatePortfolioVersionPageController createPortfolioVersionPageController;

    private BlockAndTalker<Account.AccountsTalker, List<Account>, Account> batAccount;
    private BlockAndTalker<Depot.DepotsTalker, List<Depot>, Depot> batDepot;
    private BlockAndTalker<Portfolio.PortfoliosTalker, List<Portfolio>, Portfolio> batPortfolio;
    private BlockAndTalker<UserDefinedFields.PortfolioTalker, List<UserDefinedFields>, UserDefinedFields> batUserDefinedFields;

    private String effectiveSince;
    private Portfolio.PortfoliosTalker portfoliosTalker;
    private UserDefinedFields.PortfolioTalker userDefinedPortfolioFieldsTalker;
    private Portfolio portfolio;

    private boolean editAllowed = false;
    private InvestorPrivacyModeProvider privacyModeProvider;

    public PortfolioPortraitController() {
        super(MMClassIndex.CI_T_PORTFOLIO, MMClassIndex.CI_TMM_PORTFOLIO_VERSION, UMRightBody.UMRB_EDIT_MASTER_FILE_DATA, UMRightBody.UMRB_READ_DOCUMENTS, UMRightBody.UMRB_EDIT_ACTIVITY_INSTANCE);
        this.createPortfolioVersionPageController = new CreatePortfolioVersionPageController(this);
        getEditActivityController().withCancelCommand(new Command() {
            @Override
            public void execute() {
                onEditCancelled();
            }
        });
    }

    @Override
    public BlockAndTalker[] initBats() {
        this.portfoliosTalker = new Portfolio.PortfoliosTalker();
        this.userDefinedPortfolioFieldsTalker = new UserDefinedFields.PortfolioTalker(
                PmWebSupport.getInstance().getUserFieldDecls(ShellMMType.ST_PORTFOLIO));

        this.batAccount = new BlockAndTalker<>(this.context, new Account.AccountsTalker());
        this.batDepot = new BlockAndTalker<>(this.context, new Depot.DepotsTalker());
        this.batPortfolio = new BlockAndTalker<>(this.context, this.portfoliosTalker);
        this.batUserDefinedFields = new BlockAndTalker<>(this.context, userDefinedPortfolioFieldsTalker);
        this.privacyModeProvider = new InvestorPrivacyModeProvider(this.context) {
            @Override
            public HistoryToken getPrivacyModeEntryToken() {
                return newPortfolioToken();
            }

            @Override
            public void requestPrivacyModeActivatable(PrivacyModeActivatableCallback callback) {
                privacyModeActivatableCallback = callback;
            }
        };

        return new BlockAndTalker[]{
                this.batPortfolio,
                this.batUserDefinedFields,
                this.batDepot,
                this.batAccount,
                this.privacyModeProvider.getBat()
        };
    }

    private HistoryToken newPortfolioToken() {
        return newPortfolioTokenBuilder(this.portfolio.getId(), this.effectiveSince).build();
    }

    public static HistoryToken.Builder newPortfolioTokenBuilder(String portfolioId, String effectiveSince) {
        return HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PORTFOLIO)
                .with(OBJECTID_KEY, portfolioId)
                .with(Portfolio.EFFECTIVE_SINCE_PARAM, effectiveSince);
    }

    @Override
    public void onAfterEditSubmit() {
        newPortfolioTokenBuilder(this.portfolio.getId(), this.effectiveSince)
                .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractUserObjectView.SC_STATIC)
                .with(FORCE_RL, "t")  // $NON-NLS$
                .fire();
    }

    @Override
    public void onSuccess(ResponseType result) {
        this.editAllowed = this.priv.allowed(UMRightBody.UMRB_EDIT_MASTER_FILE_DATA);
        super.onSuccess(result);
    }

    @Override
    public void onEdit() {
        if (!checkEditActivity()) {
            return;
        }
        newEditPortfolioTokenBuilder(this.portfolio.getPortfolioVersion().getId()).fire();
    }

    public HistoryToken.Builder newEditPortfolioTokenBuilder(String portfolioVersionId) {
        return HistoryToken.Builder.fromCurrent()
                .with(PortfolioPortraitController.OBJECTID_KEY, this.portfolio.getId())
                .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractDepotObjectPortraitController.HISTORY_TOKEN_EDIT_ACTIVITY)
                .with(ActivityPageController.PARAM_APPLY_AS_MAIN_INPUT, portfolioVersionId)
                .with(ActivityPageController.PARAM_ACTIVITY_DEFINITION, this.blockEditAct.getResult().getInfo().getId());
    }

    public void onPortfolioVersionSelected(String portfolioId, String validDate) {
        final HistoryToken.Builder builder = PortfolioPortraitController.newPortfolioTokenBuilder(portfolioId, validDate);
        final String scToken = HistoryToken.current().get(NavItemSpec.SUBCONTROLLER_KEY);
        if (StringUtil.hasText(scToken)) {
            builder.with(NavItemSpec.SUBCONTROLLER_KEY, scToken);
        }
        builder.fire();
    }

    public void onCreatePortfolioVersion() {
        HistoryToken.Builder.fromCurrent()
                .with(PortfolioPortraitController.OBJECTID_KEY, this.portfolio.getId())
                .with(NavItemSpec.SUBCONTROLLER_KEY, SC_CREATE_PORTFOLIO_VERSION)
                .fire();
    }

    public void onClonePortfolioVersion() {
        HistoryToken.Builder.fromCurrent()
                .with(PortfolioPortraitController.OBJECTID_KEY, this.portfolio.getId())
                .with(NavItemSpec.SUBCONTROLLER_KEY, SC_CREATE_PORTFOLIO_VERSION)
                .with(CreatePortfolioVersionPageController.CLONE_SOURCE_KEY, this.portfolio.getPortfolioVersion().getId())
                .fire();
    }

    public void onSuccessfullyCreatedOrClonedPortfolioVersion(String portfolioVersionId, String effectiveFrom) {
        if (!checkEditActivity()) {
            PortfolioPortraitController.newPortfolioTokenBuilder(this.portfolio.getId(), effectiveFrom)
                    .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractUserObjectView.SC_STATIC)
                    .with(AbstractDepotObjectPortraitController.FORCE_RL, "t")  // $NON-NLS$
                    .fire();
            return;
        }
        newEditPortfolioTokenBuilder(portfolioVersionId)
                .with(Portfolio.EFFECTIVE_SINCE_PARAM, effectiveFrom)
                .with(AbstractDepotObjectPortraitController.FORCE_RL, "t")  // $NON-NLS$
                .fire();
    }

    protected NavItemSpec initNavItems(List<WorkspaceSheetDesc> sheets) {
        final NavItemSpec linked;
        final String portfolioName = this.portfolio.getName();

        final HistoryToken defaultToken = newPortfolioToken();
        final PortfolioUserObjectController portfolioUOC = new PortfolioUserObjectController(this, new PortfolioView(), this.editAllowed);
        final HistoryToken investorToken = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE)
                .with(OBJECTID_KEY, this.portfolio.getInvestorId()).build();

        final NavItemSpec root = new NavItemSpec("root", "root"); // $NON-NLS$
        root.addChild(setFallback(new NavItemSpec(AbstractUserObjectView.SC_STATIC, I18n.I.staticData(), defaultToken, portfolioUOC)).withOpenWithSelection().withClosingSiblings().addChildren(
                new NavItemSpec(AbstractUserObjectView.SC_PORTFOLIO_VERSION, I18n.I.portfolioVersion(), defaultToken, portfolioUOC),
                new NavItemSpec(AbstractUserObjectView.SC_PROFILE, I18n.I.portfolioProfile(), defaultToken, portfolioUOC),
                new NavItemSpec(AbstractUserObjectView.SC_INVESTMENT_POLICIES, I18n.I.investmentPolicies(), defaultToken, portfolioUOC),
                new NavItemSpec(AbstractUserObjectView.SC_REPORTING, I18n.I.reporting(), defaultToken, portfolioUOC),
                new NavItemSpec(AbstractUserObjectView.SC_ADVISOR, I18n.I.advisor(), defaultToken, portfolioUOC),
                new NavItemSpec(AbstractUserObjectView.SC_GENERAL_COMMENT, I18n.I.generalComment(), defaultToken, portfolioUOC),
                new NavItemSpec(AbstractUserObjectView.SC_ALLOCATION_COMMENTS, I18n.I.allocationComments(), defaultToken, portfolioUOC),
                new NavItemSpec(AbstractUserObjectView.SC_USER_DEFINED_FIELDS, I18n.I.userDefinedFields(), defaultToken, portfolioUOC)
        ));
        if (this.editAllowed) {
            addEditNode(root);
            addCreatePortfolioVersionNode(root);
        }

        root.addChild(linked = new NavItemSpec("V", I18n.I.linkedObjects()).withHasDelegate().withClosingSiblings().addChildren( //$NON-NLS$
                new NavItemSpec("VI-" + this.portfolio.getInvestorId(), this.portfolio.getInvestorName(), investorToken, // $NON-NLS$
                        EmptyContext.create(portfolioName).withIconKey(ICON_KEY)).withIsTransient().withIcon("pm-investor", SafeHtmlUtils.fromString(I18n.I.pmInvestor())) // $NON-NLS$
        ));

        handleOrderEntryNavItems(root);

        addActivityNode(root);

        addDmsNode(root);

        //sets also the default sub controller, which should be a dashboard if there is one.
        //So the order of addDashboard and createLayoutChildren matters!
        addDashboard(root);

        root.addChild(new NavItemSpec("R", I18n.I.pmAnalyses()).withClosingSiblings().withHasDelegate()  // $NON-NLS$
                .withSelectFirstChildOnOpen()
                .addChildren(createLayoutChildren(defaultToken, portfolioName, this.portfolio.getId(), sheets)));

        for (final Depot depot : ContextItemComparator.sort(this.portfolio.getDepots())) {
            final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_DEPOT)
                    .with(OBJECTID_KEY, depot.getId())
                    .build();

            final HistoryContextProducer contextProducer = PmItemListContext.createProducerForDepot(portfolioName,
                    ICON_KEY, depot, portfolio.getDepots());

            linked.addChild(new NavItemSpec("VD-" + depot.getId(), depot.getName(), token, contextProducer) // $NON-NLS$
                    .withIsTransient().withIcon("pm-investor-depot")); // $NON-NLS$
        }

        for (final Account account : ContextItemComparator.sort(this.portfolio.getAccounts())) {
            final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_ACCOUNT)
                    .with(OBJECTID_KEY, account.getId())
                    .build();

            final HistoryContextProducer contextProducer = PmItemListContext.createProducerForAccount(portfolioName,
                    ICON_KEY, account, portfolio.getAccounts());

            linked.addChild(new NavItemSpec("VA-" + account.getId(), account.getName(), token, contextProducer) // $NON-NLS$
                    .withIsTransient().withIcon("pm-investor-account")); // $NON-NLS$
        }

        return root;
    }

    private void addCreatePortfolioVersionNode(NavItemSpec root) {
        final NavItemSpec navItemSpecChild = new NavItemSpec(SC_CREATE_PORTFOLIO_VERSION, I18n.I.portfolioVersionCreate(), null, this.createPortfolioVersionPageController);
        navItemSpecChild.setVisible(false);
        root.addChild(navItemSpecChild).withIsTransient();
    }

    @Override
    protected ShellMMType getShellMMType() {
        return ShellMMType.ST_PORTFOLIO;
    }

    @Override
    protected Widget getNavNorthWidget() {
        if (this.portfolio == null) {
            Firebug.debug("portfolio is null!");
            return null;
        }
        return ObjectWidgetFactory.createPortfolioWidget(this.portfolio);
    }

    @Override
    public Portfolio createUserObject() {
        final List<Portfolio> resultObject = this.batPortfolio.createResultObject();
        assert resultObject.size() == 1 : "unexpected portfolio list size. Must be 1!";
        final Portfolio portfolio = resultObject.get(0);
        portfolio.setUserDefinedFields(this.batUserDefinedFields.createResultObject().get(0));
        portfolio.getAccounts().addAll(this.batAccount.createResultObject());
        portfolio.getDepots().addAll(this.batDepot.createResultObject());
        portfolio.withAlerts(getAlerts());
        return portfolio;
    }

    @Override
    protected Portfolio getUserObject() {
        return this.portfolio;
    }

    @Override
    protected void setUserObject(Portfolio userObject) {
        this.portfolio = userObject;
    }

    @Override
    protected String getName(Portfolio userObject) {
        final PortfolioVersion pv = userObject.getPortfolioVersion();

        final StringBuilder sb = new StringBuilder(I18n.I.staticData()).append(" ").append(userObject.getName());

        if (pv != null) {
            sb.append(" (").append(PmRenderers.PORTFOLIO_VERSION_DATE.render(pv.getVersionValidFromDate())); //$NON-NLS$
            if (StringUtil.hasText(pv.getVersionName())) {
                sb.append(" ").append(pv.getVersionName());
            }
            sb.append(")"); //$NON-NLS$
        }

        return sb.toString();
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        /**
         * The empty String indicates to setEffectiveSince that the current date should be used.
         * The string "null" indicates that the PortfolioVersion whose date is DefaultMM should be used.
         * Any other String is set as a date.
         * {@link de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.EffectivePortfolioVersionMethod}
         */
        final String effectiveSinceParam = event.getHistoryToken().get(Portfolio.EFFECTIVE_SINCE_PARAM, "");

        if (!effectiveSinceParam.equals(this.effectiveSince)) {
            Firebug.debug("effectiveSince has changed");
            Firebug.debug("setting databaseID of Portfolio to null");
            Firebug.debug("Setting effectiveSince param of PortfolioVersion to " + this.effectiveSince);
            this.effectiveSince = effectiveSinceParam;
            this.portfoliosTalker.setEffectiveSince(effectiveSinceParam);
            this.userDefinedPortfolioFieldsTalker.setEffectiveSince(effectiveSinceParam);
            this.batPortfolio.setDatabaseId(null);
            this.batUserDefinedFields.setDatabaseId(null);
        }

        super.onPlaceChange(event);
    }

    @Override
    protected String getControllerId() {
        return PmWebModule.HISTORY_TOKEN_PORTFOLIO;
    }

    public void onDeletePortfolioVersion() {
        if (this.portfolio == null || this.portfolio.getPortfolioVersion() == null) {
            return;
        }

        final Portfolio p = this.portfolio;
        final PortfolioVersion pv = this.portfolio.getPortfolioVersion();

        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(false);

        final DmxmlContext.Block<DeletePortfolioVersionResponse> block = dmxmlContext.addBlock("PM_DeletePortfolioVersion"); // $NON-NLS$

        final DeletePortfolioVersionRequest request = new DeletePortfolioVersionRequest();
        request.setPortfolioId(p.getId());
        request.setPortfolioVersionId(pv.getId());

        block.setParameter(request);
        block.setToBeRequested();

        dmxmlContext.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (block.isResponseOk()) {
                    final DeletePortfolioVersionResponse response = block.getResult();
                    switch (response.getResponseType()) {
                        case DPVRT_OK:
                            newPortfolioTokenBuilder(p.getId(), "")
                                    .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractUserObjectView.SC_STATIC)
                                    .with(FORCE_RL, "t")  // $NON-NLS$
                                    .fire();
                            break;
                        case DPVRT_INSUFFICIENT_RIGHTS:
                            AbstractMainController.INSTANCE.showError(I18n.I.portfolioVersionDeleteErrorInsufficientRights());
                            break;
                        case DPVRT_UNKNOWN:
                        default:
                            AbstractMainController.INSTANCE.showError(I18n.I.portfolioVersionDeleteErrorUnknown());
                    }
                    dmxmlContext.removeBlock(block);
                }
                else {
                    String message = I18n.I.portfolioVersionDeleteErrorUnknown();
                    if (this.throwable != null) {
                        message += "\n" + SafeHtmlUtils.htmlEscape(this.throwable.getMessage());  // $NON-NLS$
                    }
                    AbstractMainController.INSTANCE.showError(message);

                    dmxmlContext.removeBlock(block);
                }
            }
        });
    }

    @Override
    public PrivacyModeProvider asPrivacyModeProvider() {
        return this.privacyModeProvider;
    }
}