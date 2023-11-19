/*
 * DepotMmTalkerController.java
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
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ImplementedOrderModules;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.OrderBookDelegatePageController;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.List;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class DepotPortraitController extends AbstractDepotObjectPortraitController<Depot> implements IsPrivacyModeProvider {
    private BlockAndTalker<Depot.DepotsTalker, List<Depot>, Depot> batDepot;
    private BlockAndTalker<UserDefinedFields.Talker, List<UserDefinedFields>, UserDefinedFields> batUserDefinedFields;
    private InvestorPrivacyModeProvider privacyModeProvider;

    private Depot depot;

    private final OrderBookDelegatePageController orderBookPageController = new OrderBookDelegatePageController();

    public DepotPortraitController() {
        super(MMClassIndex.CI_T_DEPOT, MMClassIndex.CI_T_DEPOT, UMRightBody.UMRB_READ_DOCUMENTS, UMRightBody.UMRB_EDIT_ACTIVITY_INSTANCE);
    }

    @Override
    public BlockAndTalker[] initBats() {
        batDepot = new BlockAndTalker<>(this.context, new Depot.DepotsTalker());
        batUserDefinedFields = new BlockAndTalker<>(
                this.context,
                new UserDefinedFields.Talker(
                        Formula.create("Depot").withUserFieldCategories( //$NON-NLS$
                                PmWebSupport.getInstance().getUserFieldDecls(ShellMMType.ST_DEPOT)
                        )
                )
        );
        this.privacyModeProvider = new InvestorPrivacyModeProvider(this.context) {
            @Override
            public HistoryToken getPrivacyModeEntryToken() {
                return newDepotToken(getUserObject().getId());
            }
        };

        return new BlockAndTalker[]{
                this.batDepot,
                this.batUserDefinedFields,
                this.privacyModeProvider.getBat()
        };
    }

    private HistoryToken newDepotToken(String depotId) {
        final HistoryToken.Builder builder = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_DEPOT)
                .with(OBJECTID_KEY, depotId);
        return builder.build();
    }

    protected NavItemSpec initNavItems(List<WorkspaceSheetDesc> sheets) {
        final UserObjectController<Depot, DepotPortraitController, UserObjectDisplay.UserObjectPresenter<Depot>> depotUOD = new UserObjectController<>(this, new DepotView());

        final String depotId = this.depot.getId();
        final HistoryToken defaultToken = newDepotToken(depotId);
        final HistoryToken investorToken = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE).with(OBJECTID_KEY, this.depot.getInvestorId()).build();

        final NavItemSpec root = new NavItemSpec("root", "root"); //$NON-NLS$
        root.addChild(setFallback(new NavItemSpec(AbstractUserObjectView.SC_STATIC, I18n.I.staticData(), defaultToken, depotUOD))
                        .withOpenWithSelection().withClosingSiblings().addChildren(
                                new NavItemSpec(AbstractUserObjectView.SC_USER_DEFINED_FIELDS, I18n.I.userDefinedFields(), defaultToken, depotUOD))
        );

        final NavItemSpec linked = new NavItemSpec("V", I18n.I.linkedObjects()).withClosingSiblings(); //$NON-NLS$
        linked.addChild(new NavItemSpec("VI-" + this.depot.getInvestorId(), this.depot.getInvestorName(), investorToken, // $NON-NLS$
                EmptyContext.create(this.depot.getName()).withIconKey("pm-investor-depot")).withIsTransient().withIcon("pm-investor"));  // $NON-NLS$
        if (StringUtil.hasText(this.depot.getPortfolioId())) {
            final HistoryToken portfolioToken = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PORTFOLIO)
                    .with(OBJECTID_KEY, this.depot.getPortfolioId()).build();
            linked.addChild(new NavItemSpec("VP-" + this.depot.getPortfolioId(), this.depot.getPortfolioName(), portfolioToken, // $NON-NLS$
                    EmptyContext.create(this.depot.getName()).withIconKey("pm-investor-depot")).withIsTransient().withIcon("pm-investor-portfolio")); // $NON-NLS$
        }
        root.addChild(linked);

        handleOrderEntryNavItems(root);
        handleOrderBookNavItems(root);

        addActivityNode(root);

        addDmsNode(root);

        root.addChild(new NavItemSpec("R", I18n.I.pmAnalyses()).withHasDelegate().withClosingSiblings()  // $NON-NLS$
                .withSelectFirstChildOnOpen()
                .addChildren(createLayoutChildren(defaultToken, this.depot.getName(), depotId, sheets)));

        addStandardSettlementAccount(linked);
        return root;
    }

    private void addStandardSettlementAccount(NavItemSpec node) {
        final Account standardSettlementAccount = this.depot.getStandardSettlementAccount();
        if (standardSettlementAccount != null) {
            final String id = this.depot.getStandardSettlementAccount().getId();
            final String name = this.depot.getStandardSettlementAccount().getName();
            final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_ACCOUNT)
                    .with(OBJECTID_KEY, id)
                    .build();
            final String label = I18n.I.standardSettlementAccount() + " (" + name + ")"; // $NON-NLS$
            node.addChild(new NavItemSpec("VA-" + id, label, token, EmptyContext.create(this.depot.getName()) //$NON-NLS$
                    .withIconKey("pm-investor-depot")).withIsTransient().withIcon("pm-investor-account")); //$NON-NLS$
        }
    }

    private void handleOrderBookNavItems(NavItemSpec parent) {
        Firebug.debug("<DepotMmTalkerController.handleOrderBookNavItems>");
        if (!isStandaloneBrokingAllowed() && !isActivityBrokingAllowed()) {
            return;
        }

        final String allowedDepotId = getIdOfBrokingAllowedDepot();
        if(StringUtil.equals(this.depot.getId(), allowedDepotId) && ImplementedOrderModules.hasOrderBook(this.depot.getBank().getBrokerageModuleId())) {
            parent.addChild(new NavItemSpec("OE_OBK", I18n.I.orderBook(), //$NON-NLS$
                            newDepotToken(allowedDepotId), this.orderBookPageController).withIsTransient()
            );
        }
    }

    @Override
    public void refresh() {
        super.refresh();
        if (isStandaloneBrokingAllowed() && getCurrentPageController() == this.orderBookPageController) {
            this.orderBookPageController.refresh();
        }
    }

    @Override
    protected ShellMMType getShellMMType() {
        return ShellMMType.ST_DEPOT;
    }

    @Override
    protected Widget getNavNorthWidget() {
        if (this.depot == null) {
            Firebug.debug("depot is null!");
            return null;
        }
        return ObjectWidgetFactory.createDepotWidget(this.depot);
    }

    @Override
    public Depot createUserObject() {
        final List<Depot> resultObject = this.batDepot.createResultObject();
        assert resultObject.size() == 1 : "unexpected depot list size. Must be 1!";
        final Depot depot = resultObject.get(0);
        depot.setUserDefinedFields(batUserDefinedFields.createResultObject().get(0));
        depot.withAlerts(getAlerts());

        return depot;
    }

    @Override
    protected Depot getUserObject() {
        return this.depot;
    }

    @Override
    protected void setUserObject(Depot userObject) {
        this.depot = userObject;
    }

    @Override
    protected String getName(Depot userObject) {
        return I18n.I.staticData() + " " + userObject.getName();
    }

    @Override
    protected String getControllerId() {
        return PmWebModule.HISTORY_TOKEN_DEPOT;
    }

    @Override
    public PrivacyModeProvider asPrivacyModeProvider() {
        return this.privacyModeProvider;
    }
}