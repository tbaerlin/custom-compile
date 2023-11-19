package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.internaltypes.DMSSearchResult;

import static com.google.gwt.query.client.GQuery.$;

/**
 * Created on 12.03.15
 * Copyright (c) vwd GmbH. All Rights Reserved.
 *
 * @author mloesch
 */
public class DmsContextPopup implements DmsDisplay {
    private final PopupPanel popup = new PopupPanel(true, true);
    private final LayoutPanel contentPanel = new LayoutPanel();
    private final FlowPanel entriesPanel = new FlowPanel();
    private final FlowPanel detailPanel = new FlowPanel();
    private final FlowPanel configPanel = new FlowPanel();

    private final DmsDisplay.Presenter presenter;
    private DMSSearchResult searchResult;
    private Button confBtn;

    private static final String STYLE_TOOL_PANEL = "tool-panel"; // $NON-NLS$
    private final static int widthInPx = 400;
    private final static String width = "400px";  // $NON-NLS$
    private FlexTable resultTable;

    public DmsContextPopup(Presenter presenter) {
        initHandlers();

        this.presenter = presenter;
        this.presenter.setDisplay(this);

        this.configPanel.setWidth(width);
        this.configPanel.setHeight("140px"); // $NON-NLS$

        this.contentPanel.setWidth(width);
        this.contentPanel.setHeight("30px"); // $NON-NLS$
        this.contentPanel.add(this.entriesPanel);
        this.contentPanel.add(this.detailPanel);
        this.contentPanel.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);

        this.entriesPanel.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
        this.entriesPanel.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        initEntriesPanel();

        setInitialChildWidth();
        this.contentPanel.forceLayout();

        final FlowPanel basePanel = new FlowPanel();
        basePanel.addStyleName("flipper");
        basePanel.add(this.contentPanel);
        this.contentPanel.addStyleName("front");
        basePanel.add(this.configPanel);
        this.configPanel.addStyleName("back");

        this.popup.addStyleName("flip-container");
        this.popup.getElement().setId("flipContainer"); // $NON-NLS$

        this.popup.add(basePanel);
        this.popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> closeEvent) {
                popup.removeStyleName("flip");
                showResults(false);
            }
        });
    }

    private void initHandlers() {
        this.popup.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                PopupPanelFix.addFrameDummy(popup);
            }
        });
        this.popup.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent e) {
                if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    popup.hide();
                }
            }
        }, KeyDownEvent.getType());
    }

    @Override
    public void updateData(DMSSearchResult result) {
        if (result == null) {
            throw new IllegalStateException("dms search result must not be null!"); // $NON-NLS$
        }
        this.searchResult = result;
    }

    @Override
    public void layout() {
        updateEntriesPanel();
        updateConfigPanel(this.searchResult);
    }

    private void showResults(boolean withAnimation) {
        setInitialChildWidth();
        this.contentPanel.animate(withAnimation ? 300 : 0, new Layout.AnimationCallback() {
            @Override
            public void onAnimationComplete() {
                setContentPanelHeight();
            }

            @Override
            public void onLayout(Layout.Layer layer, double v) {
                //nothing
            }
        });
    }

    private void showResults() {
        showResults(true);
    }

    private void setContentPanelHeight() {
        if (this.searchResult == null) {
            return;
        }
        if (this.searchResult.getMetaData().size() > 7) { //this means scrollbars will be visible
            this.contentPanel.setHeight("280px"); // $NON-NLS$
            addScrollbarStyle();
        }
        else {
            this.contentPanel.setHeight(Integer.valueOf(40 + searchResult.getMetaData().size() * 30) + "px"); // $NON-NLS$
            removeScrollbarStyle();
        }
    }

    private void addScrollbarStyle() {
        this.confBtn.addStyleName("scroll");
        //find all elements with style tool-panel and add scroll.
        //these elements are panels. if one would do this without gquery,
        //it'd be necessary to save every created panel in a list...
        $("." + STYLE_TOOL_PANEL, this.entriesPanel).addClass("scroll"); // $NON-NLS$
    }

    private void removeScrollbarStyle() {
        this.confBtn.removeStyleName("scroll");
        $("." + STYLE_TOOL_PANEL, this.entriesPanel).removeClass("scroll"); // $NON-NLS$
    }

    private void setInitialChildWidth() {
        this.contentPanel.setWidgetLeftWidth(this.entriesPanel, 0, Style.Unit.PX, widthInPx, Style.Unit.PX);
        this.contentPanel.setWidgetRightWidth(this.detailPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
    }


    public void showRelativeTo(UIObject target) {
        DOMUtil.setTopZIndex(this.popup);
        this.popup.showRelativeTo(target);
        WidgetUtil.makeFocusable(this.popup);
        WidgetUtil.deferredSetFocus(this.popup);
    }

    private void showContentPanel(boolean update) {
        if (update) {
            presenter.layoutWhenUpdateDone();
        }
        else {
            showResults();
        }
    }

    // EntriesPanel ////////////////////////////////////////////////////////////////////////////////////////////////////
    private void initEntriesPanel() {
        addEntriesPanelHead();
        this.resultTable = new FlexTable();
        this.resultTable.addStyleName("as-dms-popup-table");
        this.entriesPanel.add(this.resultTable);
    }

    private void addEntriesPanelHead() {
        this.entriesPanel.clear();
        final Panel headPanel = new FlowPanel();
        final Label label = new Label(I18n.I.dmsArchivedDocuments());
        label.addStyleName("as-dms-popup-header");
        headPanel.add(label);

        this.confBtn = Button.icon("as-tool-settings").tooltip(I18n.I.settings()).clickHandler(new ClickHandler() { // $NON-NLS$
            @Override
            public void onClick(ClickEvent clickEvent) {
                showConfig();
            }
        }).build();
        this.confBtn.addStyleName("as-dms-popup-conf-btn");

        headPanel.add(this.confBtn);
        headPanel.addStyleName("as-dms-popup-header-panel");

        this.entriesPanel.add(headPanel);
    }

    private void updateEntriesPanel() {
        this.resultTable.removeAllRows();
        if (this.searchResult.getMetaData() == null) {
            return;
        }
        for (int i = 0, size = this.searchResult.getMetaData().size(); i < size; i++) {
            final DocumentMetadata dm = this.searchResult.getMetaData().get(i);
            int col = 0;
            this.resultTable.setWidget(i, col, new Label(PmRenderers.DATE_STRING.render(dm.getDateCreated())));
            this.resultTable.getFlexCellFormatter().addStyleName(i, col++, "date-col");

            final Label lbName = new Label(dm.getDocumentName());
            this.resultTable.setWidget(i, col, lbName);
            this.resultTable.getFlexCellFormatter().addStyleName(i, col++, "name-col");

            final Button btnDownload = Button.icon("as-loadFromArchive").tooltip(I18n.I.download()).clickHandler(new ClickHandler() { // $NON-NLS$
                @Override
                public void onClick(ClickEvent clickEvent) {
                    presenter.download(dm);
                }
            }).build();
            final Button btnDetails = Button.icon("mm-list-move-right").tooltip(I18n.I.details()).clickHandler(new ClickHandler() { // $NON-NLS$
                @Override
                public void onClick(ClickEvent clickEvent) {
                    showDetails(dm);
                }
            }).build();
            final Panel btnPanel = new HorizontalPanel();
            btnPanel.addStyleName(STYLE_TOOL_PANEL);
            btnPanel.add(btnDownload);
            btnPanel.add(btnDetails);

            this.resultTable.setWidget(i, col, btnPanel);
            this.resultTable.getFlexCellFormatter().addStyleName(i, col, "tool-col");
        }
        showResults();
    }

    //ConfigPanel //////////////////////////////////////////////////////////////////////////////////////////////////////
    private void updateConfigPanel(final DMSSearchResult res) {
        this.configPanel.clear();

        final Label head = new Label(I18n.I.settings());
        head.addStyleName("as-dms-popup-header");
        this.configPanel.add(head);

        final DateBox fromBox = DateBox.factory().withDate(JsDateFormatter.parseDdmmyyyy(res.getFromDate(), true))
                .withAllowNull().withIconWidet(IconImage.getIcon("sps-calendar")).build(); // $NON-NLS$

        final DateBox toBox = DateBox.factory().withDate(JsDateFormatter.parseDdmmyyyy(res.getToDate(), true))
                .withAllowNull().withIconWidet(IconImage.getIcon("sps-calendar")).build(); // $NON-NLS$

        int row = -1;
        final FlexTable ft = new FlexTable();
        ft.addStyleName("as-dms-popup-conf-table");
        ft.setText(++row, 0, I18n.I.fromUpperCase());
        ft.setWidget(row, 1, fromBox);
        ft.getFlexCellFormatter().addStyleName(row, 0, "label");

        ft.setText(++row, 0, I18n.I.toUpperCase());
        ft.setWidget(row, 1, toBox);
        ft.getFlexCellFormatter().addStyleName(row, 0, "label");

        this.configPanel.add(ft);

        final Button applyConfBtn = Button.text(I18n.I.accept()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                presenter.update(fromBox.getDate(), toBox.getDate());
                hideConfig(true);
            }
        }).build();
        applyConfBtn.addStyleName("as-dms-popup-conf-btn");
        final Button cancelConfBtn = Button.text(I18n.I.cancel()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                hideConfig(false);
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        updateConfigPanel(res); //resets dateboxes
                    }
                });
            }
        }).build();
        cancelConfBtn.addStyleName("as-dms-popup-conf-btn");
        final FlowPanel btnPanel = new FlowPanel();
        btnPanel.addStyleName("as-dms-conf-btn-panel");
        btnPanel.add(applyConfBtn);
        btnPanel.add(cancelConfBtn);

        this.configPanel.add(btnPanel);
    }

    private void showConfig() {
        if (GQuery.browser.msie) {
            this.contentPanel.removeStyleName("front");
            this.configPanel.removeStyleName("back");
            this.contentPanel.addStyleName("back");
            this.configPanel.addStyleName("front");
        }
        else {
            this.popup.addStyleName("flip");
        }
        this.contentPanel.setHeight("140px"); // $NON-NLS$
    }

    private void hideConfig(final boolean update) {
        if (GQuery.browser.msie) {
            this.contentPanel.removeStyleName("back");
            this.configPanel.removeStyleName("front");
            this.contentPanel.addStyleName("front");
            this.configPanel.addStyleName("back");
            showContentPanel(update);
            return;
        }
        //TODO: handle IE
        final boolean[] transitionDone = new boolean[1];
        transitionDone[0] = false;
        //actually, one would use $().one(events...) to sink the transitionend events
        //due to the fact that one() needs the Event.EVNETNAME int representation of an event and
        //the lack of 4 event types needed, the one() behaviour is simulated by using a bool array...
        $(this.popup).on("webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend", // $NON-NLS$
                new Function() {
                    @Override
                    public void f() {
                        if (!transitionDone[0]) {
                            showContentPanel(update);
                            transitionDone[0] = true;
                        }
                    }
                });
        this.popup.removeStyleName("flip");
    }


    //DetailPanel //////////////////////////////////////////////////////////////////////////////////////////////////////
    private void addDetailPanelHead(final DocumentMetadata dm) {
        this.detailPanel.clear();
        final Panel headPanel = new FlowPanel();
        final Button backBtn = Button.icon("mm-list-move-left").tooltip(I18n.I.back()).clickHandler(new ClickHandler() { // $NON-NLS$
            @Override
            public void onClick(ClickEvent clickEvent) {
                showResults();
            }
        }).build();
        backBtn.addStyleName("as-dms-popup-conf-btn");
        headPanel.add(backBtn);
        final Label label = new Label(I18n.I.details());
        label.addStyleName("as-dms-popup-header");
        headPanel.add(label);

        final Button btnDownload = Button.icon("as-loadFromArchive").tooltip(I18n.I.download()).clickHandler(new ClickHandler() { // $NON-NLS$
            @Override
            public void onClick(ClickEvent clickEvent) {
                presenter.download(dm);
            }
        }).build();
        btnDownload.addStyleName("as-dms-popup-conf-btn");
        headPanel.add(btnDownload);
        headPanel.addStyleName("as-dms-popup-header-panel");

        this.detailPanel.add(headPanel);
    }

    private void showDetails(DocumentMetadata dm) {
        addDetailPanelHead(dm);
        int row = -1;
        final FlexTable ft = new FlexTable();
        ft.addStyleName("as-dms-popup-details-table");
        ft.setText(++row, 0, I18n.I.title());

        ft.setWidget(row, 1, new HTML(dm.getDocumentName()));
        ft.getFlexCellFormatter().addStyleName(row, 0, "label");

        ft.setText(++row, 0, I18n.I.type());
        ft.setWidget(row, 1, new HTML(dm.getDocumentType()));
        ft.getFlexCellFormatter().addStyleName(row, 0, "label");

        ft.setText(++row, 0, I18n.I.user());
        ft.getFlexCellFormatter().addStyleName(row, 0, "label");
        ft.setText(row, 1, dm.getCreatedBy());

        ft.setText(++row, 0, I18n.I.date());
        ft.getFlexCellFormatter().addStyleName(row, 0, "label");
        ft.setText(row, 1, PmRenderers.DATE_TIME_STRING.render(dm.getDateCreated()));

        ft.setText(++row, 0, I18n.I.comment());
        ft.getFlexCellFormatter().addStyleName(row, 0, "label");
        final Label lbComment = new Label(dm.getComment());
        lbComment.addStyleName("as-dms-popup-details-comment");
        ft.setWidget(++row, 0, lbComment);
        ft.getFlexCellFormatter().setColSpan(row, 0, 2);

        this.detailPanel.add(ft);

        this.contentPanel.setWidgetLeftWidth(this.entriesPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        this.contentPanel.setWidgetRightWidth(this.detailPanel, 0, Style.Unit.PX, widthInPx, Style.Unit.PX);
        this.contentPanel.animate(300, new Layout.AnimationCallback() {
            @Override
            public void onAnimationComplete() {
                contentPanel.setHeight("270px"); // $NON-NLS$
            }

            @Override
            public void onLayout(Layout.Layer layer, double v) {
                //nothing
            }
        });
    }
}