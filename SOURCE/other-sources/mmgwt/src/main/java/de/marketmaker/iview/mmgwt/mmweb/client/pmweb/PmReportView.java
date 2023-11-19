package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsModule;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card.PmReportCard;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card.PmReportCardController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card.PmReportCardView;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.CloseCardEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.CloseCardHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.DisplayCardEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.DisplayCardHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AnimationFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsCenterPanelHeadingInvisible;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.EvalLayoutTableResponse;
import de.marketmaker.iview.pmxml.LayoutDesc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 26.05.2010 14:11:07
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */
public class PmReportView implements ContentView, NeedsCenterPanelHeadingInvisible, NeedsScrollLayout,
        DisplayCardHandler, CloseCardHandler, AsModule {
    private final ContentPanel panel;

    private final ContentPanel innerPanel;

    private final CardLayout cardLayout;

    private ContentPanel asModulePanel = null;

    private Map<Integer, PmReportCardView> mapCards = new HashMap<>();

    private PmReportCardView currentCardView;

    public PmReportView() {
        this.cardLayout = new CardLayout();
        this.panel = new ContentPanel(this.cardLayout);
        this.panel.addStyleName("mm-content");
        this.panel.addStyleName("mm-contentData");
        this.panel.addStyleName("pm-reportView");
        this.panel.setHeaderVisible(false);
        this.panel.add(new Label(I18n.I.pmNoInvestorSelected()));

        this.innerPanel = new ContentPanel(new FitLayout());
        this.innerPanel.setScrollMode(Style.Scroll.ALWAYS);
        this.innerPanel.setHeaderVisible(true);
        this.innerPanel.setBorders(false);
        this.innerPanel.getHeader().addTool(createToolButton("mm-tool-settings", new SelectionListener<IconButtonEvent>() { // $NON-NLS-0$
            @Override
            public void componentSelected(IconButtonEvent ce) {
                showReportSettings();
            }
        }));
        this.innerPanel.getHeader().addTool(createToolButton("mm-message-close", new SelectionListener<IconButtonEvent>() { // $NON-NLS-0$

            @Override
            public void componentSelected(IconButtonEvent ce) {
                closeCurrentCard();
            }
        }));
        this.innerPanel.getHeader().setText(I18n.I.pmNoInvestorSelected());

        EventBusRegistry.get().addHandler(DisplayCardEvent.getType(), this);
        EventBusRegistry.get().addHandler(CloseCardEvent.getType(), this);
    }

    private ToolButton createToolButton(final String style,
            SelectionListener<IconButtonEvent> listener) {
        final ToolButton button = new ToolButton(style, listener);
        button.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent baseEvent) {
                if (button.isEnabled()) {
                    button.setStyleName(style + "-hover");
                }
            }
        });
        button.addListener(Events.OnMouseOut, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent baseEvent) {
                button.setStyleName(style);
            }
        });
        return button;
    }

    public Component getAsModuleComponent() {
        if (this.asModulePanel == null) {
            this.asModulePanel = new ContentPanel(new BorderLayout());
            this.asModulePanel.setHeaderVisible(false);

            final BorderLayoutData northData = new BorderLayoutData(Style.LayoutRegion.NORTH, 73);
            northData.setMargins(new Margins(0, 0, 4, 0));
            final Label lblSubmenuDummy = new Label();
            lblSubmenuDummy.setStyleName("as-submenuDummy");
            final SimplePanel header = new SimplePanel();
            header.setWidget(lblSubmenuDummy);
            this.asModulePanel.add(header, northData);

            final BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
            this.asModulePanel.add(this.panel, centerData);
        }
        return this.asModulePanel;
    }

    public Widget getWidget() {
        return this.panel;
    }

    public void onBeforeHide() {
    }

    public void displayCard(DisplayCardEvent event) {
        final PmReportCard card = event.getCard();
        this.currentCardView = this.mapCards.get(card.getCardId());
        if (this.currentCardView == null) {
            this.currentCardView = new PmReportCardView(card);
            this.mapCards.put(card.getCardId(), this.currentCardView);
            this.panel.add(this.currentCardView);
        }
        this.currentCardView.activateReport(card.getReport());
        setActiveItem(this.currentCardView);
    }

    public void closeCard(CloseCardEvent event) {
        final PmReportCard card = event.getCard();
        final PmReportCardView deletedCardView = this.mapCards.remove(card.getCardId());
        this.panel.remove(deletedCardView);
    }

    public void showPdfReport(final String uri) {
        ActionPerformedEvent.fire("X_PM_PDF"); // $NON-NLS-0$
        final Frame frame = new Frame(uri);
        setInnerContent(frame, true);
    }

    private void setActiveItem(PmReportCardView cardView) {
        final PmReportCard card = cardView.getCard();
        this.cardLayout.setActiveItem(cardView);
        final InvestorItem investorItem = card.getInvestorItem();
        this.innerPanel.getHeader().setIcon(investorItem.getType().getIcon());
        this.innerPanel.getHeader().setText(investorItem.getName() + " - " + card.getReport().getLayout().getLayoutName());
        setInnerContent(new Label("Daten werden geladen"), false); // $NON-NLS$
        this.currentCardView.setContentWidget(this.innerPanel);
    }

    private void closeCurrentCard() {
        PmReportCardController.getInstance().closeCard(this.currentCardView.getCard().getCardId());
    }

    public void showReportSettings() {
        final FlowPanel panel = new FlowPanel();
        final PmReportCard card = this.currentCardView.getCard();
        final AnalysisMetadataForm metadataForm = new AnalysisMetadataForm(card.getReport(), card.getMapParameters(), null);

        panel.add(metadataForm);
        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(10);
        table.setWidget(0, 0, new Button(I18n.I.accept(), IconImage.get("mm-set-icon"), new SelectionListener<ButtonEvent>() { // $NON-NLS$
            @Override
            public void componentSelected(ButtonEvent ce) {
                final HashMap<String, String> mapParameters = metadataForm.getLayoutParameters();
                card.putMapParameters(mapParameters);
                final PmReportCardController cardController = PmReportCardController.getInstance();
                cardController.gotoCard(card);
                cardController.putGlobalParameters(mapParameters);
            }
        }));
        table.setWidget(0, 1, new Button(I18n.I.reset(), IconImage.get("mm-reset-icon"), new SelectionListener<ButtonEvent>() { // $NON-NLS$

            @Override
            public void componentSelected(ButtonEvent ce) {
                metadataForm.resetToDefaults();
            }
        }));
        table.setWidget(0, 2, new Button(I18n.I.cancel(), IconImage.get("mm-element-cancel"), new SelectionListener<ButtonEvent>() { // $NON-NLS$

            @Override
            public void componentSelected(ButtonEvent ce) {
                PmReportCardController.getInstance().gotoCard(card);
            }
        }));
        panel.add(table);
        setInnerContent(new ScrollPanel(panel), true);
    }

    private void setInnerContent(final Widget widget, boolean faded) {
        if (!faded) {
            this.innerPanel.removeAll();
            this.innerPanel.add(widget);
            this.innerPanel.layout();
            return;
        }

        String animationSpec = SessionData.INSTANCE.getUserProperty("reportAnimation"); // $NON-NLS$
        if (animationSpec == null) {
            animationSpec = "fadeIn(2000)"; // $NON-NLS$
        }
        AnimationFactory.AnimationControl animation = AnimationFactory.createAnimation(animationSpec, widget);
        animation.beforeStart();

        this.innerPanel.removeAll();
        this.innerPanel.add(widget);
        this.innerPanel.layout();

        animation.run();
    }

    public void showTable(EvalLayoutTableResponse response) {
        final Widget tableWidget = createTableWidget(response.getTable(), new DTTableRenderer.Options());
        final ContentPanel panel = new ContentPanel(new BorderLayout());
        panel.setHeaderVisible(false);
        panel.add(tableWidget, new BorderLayoutData(Style.LayoutRegion.CENTER));
        setInnerContent(panel, true);
    }

    public PmReportTableWidget createTableWidget(DTTable dtTable, DTTableRenderer.Options options) {
        final LayoutDesc layoutDesc =  this.currentCardView.getCard().getReport();
        final PmReportTableWidget w = new PmReportTableWidget(null);
        w.update(dtTable, options, DTTableUtils.getInitialDiagramIndex(dtTable), layoutDesc.getLayout().getLayoutName(), null);
        return w;
    }
}