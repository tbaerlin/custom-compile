/*
 * InvestorMmTalkerController.java
 *
 * Created on 16.01.13 12:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.IsPrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItemComparator;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContextProducer;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.DepotObjectBouquet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Investor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.OwnerPersonLink;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Lösch
 */
public class InvestorPortraitController extends AbstractDepotObjectPortraitController<DepotObjectBouquet<Investor, Portfolio, Account, Depot>>
        implements IsPrivacyModeProvider {
    public static final String ICON_KEY = "pm-investor";  // $NON-NLS$

    private BlockAndTalker<Investor.InvestorTalker, Investor, Investor> batInvestor;
    private BlockAndTalker<Portfolio.PortfoliosTalker, List<Portfolio>, Portfolio> batPortfolio;
    private BlockAndTalker<Account.AccountsTalker, List<Account>, Account> batAccount;
    private BlockAndTalker<Depot.DepotsTalker, List<Depot>, Depot> batDepot;
    private BlockAndTalker<UserDefinedFields.Talker, List<UserDefinedFields>, UserDefinedFields> batUserDefinedFields;

    private DepotObjectBouquet<Investor, Portfolio, Account, Depot> bouquet;

    private InvestorPrivacyModeProvider privacyModeProvider;

    public InvestorPortraitController() {
        super(MMClassIndex.CI_T_INHABER, MMClassIndex.CI_T_INHABER, UMRightBody.UMRB_READ_DOCUMENTS, UMRightBody.UMRB_EDIT_ACTIVITY_INSTANCE);
    }

    @Override
    public BlockAndTalker[] initBats() {
        this.batInvestor = new BlockAndTalker<>(this.context, new Investor.InvestorTalker());
        this.batPortfolio = new BlockAndTalker<>(this.context, new Portfolio.PortfoliosTalker());
        this.batAccount = new BlockAndTalker<>(this.context, new Account.AccountsTalker());
        this.batDepot = new BlockAndTalker<>(this.context, new Depot.DepotsTalker());
        this.batUserDefinedFields = new BlockAndTalker<>(
                this.context,
                new UserDefinedFields.Talker(
                        Formula.create("Inhaber").withUserFieldCategories( // $NON-NLS$
                                PmWebSupport.getInstance().getUserFieldDecls(ShellMMType.ST_INHABER)
                        )
                )
        );

        this.privacyModeProvider = new InvestorPrivacyModeProvider(this.context);

        return new BlockAndTalker[]{
                this.batInvestor,
                this.batUserDefinedFields,
                this.batPortfolio,
                this.batDepot,
                this.batAccount,
                this.privacyModeProvider.getBat()
        };
    }

    protected NavItemSpec initNavItems(List<WorkspaceSheetDesc> sheets) {
        final NavItemSpec linked;

        final String investorName = this.bouquet.getRootObject().getName();
        final HistoryToken defaultToken = newInvestorToken();
        final UserObjectController<DepotObjectBouquet<Investor, Portfolio, Account, Depot>, InvestorPortraitController,
                UserObjectDisplay.UserObjectPresenter<DepotObjectBouquet<Investor, Portfolio, Account, Depot>>>
                investorUOC = new UserObjectController<>(this, new InvestorView());

        final NavItemSpec root = new NavItemSpec("root", "root"); //$NON-NLS$

        final ArrayList<NavItemSpec> staticDataNavItemSpecs = new ArrayList<>();
        staticDataNavItemSpecs.add(new NavItemSpec(AbstractUserObjectView.SC_CONTACT, I18n.I.contact(), defaultToken, investorUOC));
        if(Selector.AS_ACTIVITIES.isAllowed()) {
            staticDataNavItemSpecs.add(new NavItemSpec(AbstractUserObjectView.SC_LINKED_PERSONS, I18n.I.linkedPersons(), defaultToken, investorUOC));
        }
        staticDataNavItemSpecs.add(new NavItemSpec(AbstractUserObjectView.SC_REPORTING, I18n.I.reportingDetails(), defaultToken, investorUOC));
        staticDataNavItemSpecs.add(new NavItemSpec(AbstractUserObjectView.SC_TAX, I18n.I.taxDetails(), defaultToken, investorUOC));
        staticDataNavItemSpecs.add(new NavItemSpec(AbstractUserObjectView.SC_USER_DEFINED_FIELDS, I18n.I.userDefinedFields(), defaultToken, investorUOC));
        root.addChild(setFallback(new NavItemSpec(AbstractUserObjectView.SC_STATIC, I18n.I.staticData(), defaultToken, investorUOC)).withOpenWithSelection().withClosingSiblings().addChildren(staticDataNavItemSpecs));

        root.addChild(linked = new NavItemSpec("V", I18n.I.linkedObjects()).withClosingSiblings()); // $NON-NLS$

        handleOrderEntryNavItems(root);

        addActivityNode(root);

        addDmsNode(root);

        //sets also the default sub controller, which should be a dashboard if there is one.
        //So the order of addDashboard and createLayoutChildren matters!
        addDashboard(root);

        root.addChild(new NavItemSpec("R", I18n.I.pmAnalyses()).withHasDelegate().withClosingSiblings()  // $NON-NLS$
                        .withSelectFirstChildOnOpen()
                        .addChildren(createLayoutChildren(defaultToken, investorName,
                                this.bouquet.getRootObject().getId(), sheets)));

        final Map<String, NavItemSpec> mapPortfolioToNis = new HashMap<>(this.bouquet.getAs().size() * 4 / 3);

        for (final Portfolio portfolio : ContextItemComparator.sort(this.bouquet.getAs())) {
            Firebug.debug("<InvestorMmTalkerController> initNavItems portfolio: " + portfolio.getId());
            final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PORTFOLIO)
                    .with(OBJECTID_KEY, portfolio.getId())
                    .build();

            final HistoryContextProducer contextProducer = PmItemListContext.createProducerForPortfolio(investorName,
                    ICON_KEY, portfolio, bouquet.getAs());

            final NavItemSpec nis = new NavItemSpec("VP-" + portfolio.getId(), portfolio.getName(), token, contextProducer) // $NON-NLS$
                    .withIsTransient().withIcon("pm-investor-portfolio", SafeHtmlUtils.fromString(I18n.I.pmPortfolio())).withOpenWithParent(); // $NON-NLS$
            linked.addChild(nis);
            mapPortfolioToNis.put(portfolio.getId(), nis);
        }

        for (final Depot depot : ContextItemComparator.sort(this.bouquet.getCs())) {
            final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_DEPOT)
                    .with(OBJECTID_KEY, depot.getId())
                    .build();

            final HistoryContextProducer contextProducer = PmItemListContext.createProducerForDepot(investorName,
                    ICON_KEY, depot, bouquet.getCs());

            final NavItemSpec nis = new NavItemSpec("VD-" + depot.getId(), depot.getName(), token, contextProducer)  // $NON-NLS$
                    .withIsTransient().withIcon("pm-investor-depot", SafeHtmlUtils.fromString(I18n.I.pmDepot())); // $NON-NLS$
            final NavItemSpec portfolioNis = mapPortfolioToNis.get(depot.getPortfolioId());
            if (portfolioNis == null) {
                linked.addChild(nis);
            }
            else {
                portfolioNis.addChild(nis);
            }
        }

        for (final Account account : ContextItemComparator.sort(this.bouquet.getBs())) {
            final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_ACCOUNT)
                    .with(OBJECTID_KEY, account.getId())
                    .build();

            final HistoryContextProducer contextProducer = PmItemListContext.createProducerForAccount(investorName,
                    ICON_KEY, account, bouquet.getBs());

            final NavItemSpec nis = new NavItemSpec("VA-" + account.getId(), account.getName(), token, contextProducer) // $NON-NLS$
                    .withIsTransient().withIcon("pm-investor-account", SafeHtmlUtils.fromString(I18n.I.pmAccount())); // $NON-NLS$
            final NavItemSpec portfolioNis = mapPortfolioToNis.get(account.getPortfolioId());
            if (portfolioNis == null) {
                linked.addChild(nis);
            }
            else {
                portfolioNis.addChild(nis);
            }
        }

        final Investor investor = this.bouquet.getRootObject();
        final ArrayList<Person> persons = new ArrayList<>(investor.linkedPersons().size());
        for (final OwnerPersonLink personLink : ContextItemComparator.sort(investor.linkedPersons())) {
            if(personLink != null) {
                final Person person = personLink.getPerson();
                persons.add(person);

                final HistoryToken token = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_PERSON)
                        .with(OBJECTID_KEY, personLink.getPerson().getId())
                        .build();

                final HistoryContextProducer contextProducer = PmItemListContext.createProducerForPerson(investorName,
                        ICON_KEY, person, persons);

                linked.addChild(new NavItemSpec("VA-" + personLink.getPerson().getId(), person.getName(), token, //$NON-NLS$
                        contextProducer).withIsTransient().withIcon("pm-investor-person", SafeHtmlUtils.fromString(I18n.I.person()))); // $NON-NLS$
            }
        }

        return root;
    }

    public HistoryToken newInvestorToken() {
        return createInvestorToken(this.bouquet.getRootObject().getId());
    }

    public static HistoryToken createInvestorToken(String objectId) {
        final HistoryToken.Builder builder = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE)
                .with(OBJECTID_KEY, objectId);
        return builder.build();
    }

    @Override
    protected ShellMMType getShellMMType() {
        return ShellMMType.ST_INHABER;
    }

    @Override
    protected Widget getNavNorthWidget() {
        if (this.bouquet == null) {
            Firebug.debug("investor is null!");
            return null;
        }
        return ObjectWidgetFactory.createInvestorWidget(this.bouquet.getRootObject());
    }

    @Override
    public DepotObjectBouquet<Investor, Portfolio, Account, Depot> createUserObject() {
        final Investor investor = (Investor) this.batInvestor.createResultObject().withAlerts(getAlerts());
        investor.setUserDefinedFields(this.batUserDefinedFields.createResultObject().get(0));
        return new DepotObjectBouquet<>(investor, this.batPortfolio.createResultObject(),
                this.batAccount.createResultObject(), this.batDepot.createResultObject());
    }

    @Override
    protected DepotObjectBouquet<Investor, Portfolio, Account, Depot> getUserObject() {
        return this.bouquet;
    }

    @Override
    protected void setUserObject(DepotObjectBouquet<Investor, Portfolio, Account, Depot> userObject) {
        this.bouquet = userObject;
    }

    @Override
    protected String getName(DepotObjectBouquet<Investor, Portfolio, Account, Depot> userObject) {
        return I18n.I.staticData() + " " + userObject.getRootObject().getName();
    }

    @Override
    protected String getControllerId() {
        return PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE;
    }

    @Override
    public PrivacyModeProvider asPrivacyModeProvider() {
        return this.privacyModeProvider;
    }
}