/*
 * PortfolioView.java
 *
 * Created on 17.01.13 10:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Advisor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PortfolioProfileBase;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PortfolioVersion;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PortfolioVersionListItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class PortfolioView extends AbstractUserObjectView<Portfolio, UserObjectDisplay.PortfolioUserObjectPresenter> {
    public static final String VALID_DATE = "validDate";  // $NON-NLS$
    public static final String PORTFOLIO_ID = "portfolioId";  // $NON-NLS$

    private final Menu portfolioVersionMenu;
    private final MenuButton portfolioVersionSelectButton;
    private final Button editButton;
    private final Button createButton;
    private final Button cloneButton;
    private final Button deleteButton;
    private boolean editButtonsVisible;

    public PortfolioView() {
        super();

        final FloatingToolbar toolbar = getToolbar();

        this.editButton = Button.icon("x-tool-btn-edit") // $NON-NLS$
                .tooltip(I18n.I.portfolioEdit())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        getPresenter().onEditButtonClicked();
                    }
                }).build();
        this.editButton.setVisible(false);
        toolbar.add(this.editButton);

        this.portfolioVersionMenu = new Menu();

        this.portfolioVersionSelectButton = new MenuButton()
                .withMenu(this.portfolioVersionMenu)
                .withClickOpensMenu();
        this.portfolioVersionSelectButton.addStyleName("as-uod-portfolioVersion-selectButton");
        this.portfolioVersionSelectButton.setText(I18n.I.portfolioVersion());
        Tooltip.addQtip(this.portfolioVersionSelectButton, I18n.I.portfolioVersionSelectVersionHint());
        toolbar.add(this.portfolioVersionSelectButton);

        this.createButton = Button.icon("x-tool-btn-plus") // $NON-NLS$
                .tooltip(I18n.I.portfolioVersionCreate())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        getPresenter().onCreatePortfolioVersion();
                    }
                }).build();
        toolbar.add(this.createButton);

        this.deleteButton = Button.icon("x-tool-btn-minus") // $NON-NLS$
                .tooltip(I18n.I.portfolioVersionDelete())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        if (getUserObject() == null || getUserObject().getPortfolioVersion() == null) {
                            return;
                        }

                        final PortfolioVersion pv = getUserObject().getPortfolioVersion();
                        final String effectiveFrom = PmRenderers.DATE_TIME_STRING.render(pv.getVersionValidFromDate());
                        final String message = StringUtil.hasText(effectiveFrom)
                                ? I18n.I.portfolioVersionDeleteQuestion(pv.getVersionName(), effectiveFrom)
                                : I18n.I.portfolioVersionDeleteQuestionNoDate(pv.getVersionName());

                        Dialog.confirm(message, new Command() {
                            @Override
                            public void execute() {
                                getPresenter().onDeletePortfolioVersion();
                            }
                        });
                    }
                }).build();
        toolbar.add(this.deleteButton);

        this.cloneButton = Button.icon("x-tool-copy") // $NON-NLS$
                .tooltip(I18n.I.portfolioVersionCreateClone())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        getPresenter().onClonePortfolioVersion();
                    }
                }).build();
        toolbar.add(this.cloneButton);

        setToolbarVisible(true);
    }

    @Override
    public void updateView(Portfolio portfolio) {
        super.updateView(portfolio);
        final PortfolioVersion pv = portfolio.getPortfolioVersion();

        updateToolbar(portfolio);

        addStaticData(portfolio);
        addPortfolioVersionFields(pv);
        addProfile(pv);
        addInvestmentPoliciesFields(pv);
        addReportingFields(pv);
        addAdvisorWidget(pv != null
                ? pv.getAdvisor()
                : null);
        addGeneralCommentField(pv);
        addAllocationCommentField(pv);
        addUserDefinedFields(portfolio);
    }

    @Override
    public void setEditButtonVisible(boolean visible) {
        // If editing an portfolio is not allowed, then editing the portfolio version is also not allowed.
        // Hence, we can handle the visibility of all buttons with the visibility property of the edit button.
        this.editButtonsVisible = visible;
        this.editButton.setVisible(visible);
        this.createButton.setVisible(visible);
        this.cloneButton.setVisible(visible);
        this.deleteButton.setVisible(visible);
    }

    private void updateToolbar(final Portfolio portfolio) {
        final List<PortfolioVersionListItem> portfolioVersions = new ArrayList<>(portfolio.getPortfolioVersionList());

        updateEditButtonsVisibility(portfolioVersions);

        Collections.reverse(portfolioVersions);
        this.portfolioVersionMenu.removeAll();

        if(portfolioVersions.isEmpty()) {
            this.portfolioVersionSelectButton.setEnabled(false);
            return;
        }

        this.portfolioVersionSelectButton.setEnabled(portfolioVersions.size() > 1);

        final String versionValidFromDate = portfolio.getPortfolioVersion().getVersionValidFromDate();
        for (PortfolioVersionListItem item : portfolioVersions) {
            final MenuItem menuItem = createPortfolioVersionItem(portfolio.getId(), item);
            this.portfolioVersionMenu.add(menuItem);
            if(StringUtil.equals(versionValidFromDate, (String)menuItem.getData(VALID_DATE))) {
                menuItem.addStyleName("selected");
                this.portfolioVersionMenu.setSelectedItem(menuItem);
            }
        }
    }

    private void updateEditButtonsVisibility(List<PortfolioVersionListItem> portfolioVersions) {
        final boolean visible = !portfolioVersions.isEmpty() && this.editButtonsVisible;

        this.editButton.setVisible(visible);
        this.cloneButton.setVisible(visible);
        this.deleteButton.setVisible(visible);
    }

    private MenuItem createPortfolioVersionItem(String portfolioId, final PortfolioVersionListItem item) {
        final String versionValidFromDate = item.getVersionValidFromDate();

        String label = I18n.I.portfolioVersion() + " " + PmRenderers.PORTFOLIO_VERSION_DATE.render(versionValidFromDate);
        if(StringUtil.hasText(item.getVersionName())) {
            label += " " + item.getVersionName();
        }

        final MenuItem menuItem = new MenuItem(label).withData(VALID_DATE, versionValidFromDate).withData(PORTFOLIO_ID, portfolioId);
        menuItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (menuItem != null) {
                    final String portfolioId = (String) menuItem.getData(PORTFOLIO_ID);
                    final String validDate = (String) menuItem.getData(VALID_DATE);
                    getPresenter().onPortfolioVersionSelected(portfolioId, validDate);
                    }
                }
            }
        );
        return menuItem;
    }

    private void addStaticData(Portfolio portfolio) {
        final Panel p = addDividerSection(SC_STATIC, I18n.I.staticData());
        setSelectedSection(SC_STATIC);

        if(SessionData.INSTANCE.isUserPropertyTrue("showPortfolioAndVersionIDs")) {  // $NON-NLS$
            addField(p, "Portfolio ID", portfolio.getId());  // $NON-NLS$
            final PortfolioVersion portfolioVersion = portfolio.getPortfolioVersion();
            addField(p, "Portfolio version ID", portfolioVersion.getId());  // $NON-NLS$
            addField(p, "Portfolio version's profile ID", portfolioVersion.getProfile() != null ? portfolioVersion.getProfile().getId() : "");  // $NON-NLS$
        }

        addField(p, I18n.I.pmInvestor(), portfolio.getInvestorName());
        addField(p, I18n.I.pmPortfolio(), portfolio.getName());
        addField(p, I18n.I.portfolioNumber(), portfolio.getPortfolioNumber());
        addField(p, I18n.I.createdOn(), PmRenderers.DATE_TIME_STRING.render(portfolio.getCreationDate()));
        addField(p, I18n.I.performanceCalculationFrom(), PmRenderers.DATE_TIME_STRING.render(portfolio.getPerformanceStartDate()));
        addField(p, I18n.I.liquidateInvestmentOn(), PmRenderers.DATE_TIME_STRING.render(portfolio.getLiquidateInvestmentDate()));

        addField(p,
                PmRenderers.DATA_STATUS_LABEL.render(I18n.I.dataStatus(), portfolio.getDataStatusDate()),
                PmRenderers.DATA_STATUS.render(portfolio.getDataStatus()));

        addField(p, I18n.I.zone(), portfolio.getZone());

        addSubHeading(p, I18n.I.lastNotifications());
        addField(p, I18n.I.scheduledReporting(), PmRenderers.DATE_TIME_STRING.render(portfolio.getScheduledReportingLastNotification()));
        addField(p, I18n.I.lossThreshold(), PmRenderers.DATE_TIME_STRING.render(portfolio.getLossThresholdLastNotification()));
    }

    private void addPortfolioVersionFields(PortfolioVersion pv) {
        final Panel p = addDividerSection(SC_PORTFOLIO_VERSION, I18n.I.portfolioVersion());
        setSelectedSection(SC_PORTFOLIO_VERSION);

        addField(p, I18n.I.portfolioVersionName(), pv.getVersionName());
        addField(p, I18n.I.portfolioVersionValidFrom(), PmRenderers.DATE_TIME_STRING.render(pv.getVersionValidFromDate()));
    }

    private void addProfile(PortfolioVersion portfolioVersion) {
        final Panel p = addSection(SC_PROFILE, I18n.I.portfolioProfile());

        addField(p, I18n.I.portfolioProfileKey(), portfolioVersion.getProfileKey());

        final String profileName = portfolioVersion.hasProfile() ? portfolioVersion.getProfile().getName() : null;
        addField(p, I18n.I.portfolioProfileName(), profileName);
    }

    private void addAdvisorWidget(Advisor advisor) {
        final Panel p = addSection(SC_ADVISOR, I18n.I.advisor());
        AdvisorWidget av = new AdvisorWidget();
        p.add(av.asWidget());
        av.setValue(advisor);
    }

    private void addInvestmentPoliciesFields(PortfolioProfileBase ppb) {
        final Panel p = addSection(SC_INVESTMENT_POLICIES, I18n.I.investmentPolicies());
        addField(p, I18n.I.benchmark(), ppb.getBenchmarkSecurityName());
        addField(p, I18n.I.benchmark2(), ppb.getBenchmark2SecurityName());
        addField(p, I18n.I.assetAllocation(), ppb.getAssetAllocationName());
        addField(p, I18n.I.restrictions(), ppb.getRestrictions());
        addField(p, I18n.I.riskLimit(), Renderer.PERCENT23.render(ppb.getRiskLimit()));
        addField(p, I18n.I.investmentAgent(), ppb.getInvestmentAgentName());
        addField(p, I18n.I.financialPortfolioManagement(), ppb.isFinancialPortfolioManagement());
    }

    private void addReportingFields(PortfolioProfileBase ppb) {
        final Panel p = addSection(SC_REPORTING, I18n.I.reporting());
        addField(p, I18n.I.analysisCurrency(), ppb.getAnalysisCurrency());
        addField(p, I18n.I.reportingProfile(), ppb.getScheduledReportingProfileName());
        addField(p, I18n.I.reportingFrequency(), PmRenderers.REPORTING_FREQUENCY.render(ppb.getReportingFrequency()));
        addField(p, I18n.I.scheduledReportingActive(), ppb.isScheduledReportingActive());
        addField(p, I18n.I.lossThreshold(), Renderer.PERCENT23.render(ppb.getLossThreshold()));
    }

    protected void addGeneralCommentField(PortfolioVersion portfolioVersion) {
        final Panel p = addSection(SC_GENERAL_COMMENT, I18n.I.generalComment());
        addMultilineField(p, portfolioVersion.getGeneralComment());
    }

    protected void addAllocationCommentField(PortfolioVersion portfolioVersion) {
        final Panel p = addSection(SC_ALLOCATION_COMMENTS, I18n.I.allocationComments());
        addMultilineField(p, portfolioVersion.getAllocationComments());
    }
}
