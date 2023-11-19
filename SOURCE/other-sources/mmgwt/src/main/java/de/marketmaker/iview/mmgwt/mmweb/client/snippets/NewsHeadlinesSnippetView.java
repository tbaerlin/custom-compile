/*
 * NewsHeadlinesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PhoneGapUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.user.client.Event.NativePreviewHandler;
import static de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsHeadlinesSnippet.DEFAULT_WITH_AGENCY;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsHeadlinesSnippetView extends SnippetView<NewsHeadlinesSnippet> {
    private TableColumnModel columnModel;

    private SnippetTableWidget tw;

    private final static int HEADLINE_COLUMN = 1;

    private static final float[][] COLUMN_WIDTH = new float[][]{
            {0.15625f, 0.84375f, 0f},   // colSpan == 1 && withAgency == false
            {0.06f, 0.85f, 0f},   // colSpan == 2 && withAgency == false
            {0.06f, 0.9f, 0f},     // colSpan == 3 && withAgency == false
            {0.09f, 0.54f, 0.15f}, // colSpan == 1 && withAgency == true
            {0.08f, 0.75f, 0.15f}, // colSpan == 2 && withAgency == true
            {0.08f, 0.8f, 0.15f}      // colSpan == 3 && withAgency == true
    };

    private static final int[] MESSAGE_LENGTH = new int[]{
            46, 70, 130, // withAgency == false 
            36, 85, 125  // withAgency == true
    };

    private PagingPanel pp;

    private Widget lbReload;

    private LinkListener<NWSSearchElement> linkListener;

    private Label finderLink = null;

    private SelectButton btnCountSelection;

    private List<Element> mouseEnterEventReceivers = new ArrayList<>();

    public NewsHeadlinesSnippetView(final NewsHeadlinesSnippet snippet) {
        super(snippet);
        setTitle(snippet.getConfiguration().getString("title")); // $NON-NLS-0$
    }

    public void onContainerAvailable() {
        final SnippetConfiguration config = getConfiguration();
        int colSpan = config.getInt("colSpan", 2); // $NON-NLS-0$
        final boolean withAgency = config.getBoolean("withAgency", DEFAULT_WITH_AGENCY); // $NON-NLS-0$

        int id = colSpan > 3 ? 2 : (colSpan - 1);
        if (withAgency) {
            id += 3;
        }
        final float timeWidth = config.getFloat("timeWidth", COLUMN_WIDTH[id][0]); // $NON-NLS-0$
        final float messageWidth = config.getFloat("messageWidth", COLUMN_WIDTH[id][1]); // $NON-NLS-0$
        final float agencyWidth = config.getFloat("agencyWidth", COLUMN_WIDTH[id][2]); // $NON-NLS-0$
        final int messageLength = config.getInt("messageLength", MESSAGE_LENGTH[id]); // $NON-NLS-0$

        final TableCellRenderers.HeadlinesLinkRenderer headlinesLinkRenderer
                = new TableCellRenderers.HeadlinesLinkRenderer(new LinkListener<NWSSearchElement>() {
            public void onClick(LinkContext<NWSSearchElement> context, Element e) {
                if (linkListener != null) {
                    linkListener.onClick(context, e);
                }
                else {
                    LinkContext.NEWS_LISTENER.onClick(context, e);
                }
            }
        }, messageLength, "--", false); // $NON-NLS-0$

        final int columnCount = withAgency ? 3 : 2;
        final TableColumn[] columns = new TableColumn[columnCount];
        columns[0] = new TableColumn(I18n.I.time(), timeWidth, TableCellRenderers.COMPACT_DATE_OR_TIME);
        columns[1] = new TableColumn(I18n.I.message(), messageWidth, headlinesLinkRenderer);

        if (withAgency) {
            columns[2] = new TableColumn(I18n.I.agency(), agencyWidth, TableCellRenderers.STRING_RIGHT);
        }

        this.columnModel = new DefaultTableColumnModel(columns);

        final String pagingMode = config.getString("paging", null); // $NON-NLS-0$
        if (pagingMode != null) {
            this.pp = createPagingPanel(getMode(pagingMode));
            this.pp.setHandler(this.snippet);
            if (this.btnCountSelection != null) {
                // cannot be done in initBtnCountSelection as setSelectedData fires event
                // that can only be processed if this.pp is already set
                final String count = snippet.getNewsCountSelection(null);
                if (count != null) {
                    this.btnCountSelection.setSelectedData("count", count); // $NON-NLS$
                }
            }
        }
        else {
            this.pp = null;

            this.finderLink = new Label(I18n.I.newsSearch());
            this.finderLink.setStyleName("mm-link-right"); // $NON-NLS-0$
            this.finderLink.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    snippet.openFinder();
                }
            });
        }
    }

    private PagingPanel createPagingPanel(PagingWidgets.Mode mode) {
        return new PagingPanel(new PagingPanel.Config(this.container)
                .withWidgetsConfig(createConfig(mode))
                .withPageSize(this.snippet.getPageSize()));
    }

    private PagingWidgets.Config createConfig(PagingWidgets.Mode mode) {
        final PagingWidgets.Config result = new PagingWidgets.Config().withMode(mode);
        final boolean disableFinderLink = getConfiguration().getBoolean("disableNWSFinderLink", false); // $NON-NLS$
        if (!disableFinderLink) {
            result.withAddSearchButton(new PagingWidgets.SearchCallback() {
                public void onSearch() {
                    snippet.openFinder();
                }
            });
        }
        if (getConfiguration().getBoolean("withCountSelection", false)) { // $NON-NLS-0$
            addCountSelection(result);
        }
        if (getConfiguration().getBoolean("withPdf", false) && !"true".equals(SessionData.INSTANCE.getGuiDefValue("disablePdf"))) { // $NON-NLS$
            result.withAddPdfButton(new PagingWidgets.PdfCallback() {
                public void onPdf() {
                    openPdf();
                }
            });
        }
        if (getConfiguration().getBoolean("withPin", false)) { // $NON-NLS-0$
            result.withAddPinButton(new PagingWidgets.PinCallback() {
                public void onPin(boolean pinned) {
                    snippet.onPinChanged(pinned);
                }
            });
        }
        return result;
    }

    private void addCountSelection(PagingWidgets.Config result) {
        final List<String[]> countSelections = getSelectionModel();
        if (countSelections.isEmpty()) {
            return;
        }
        initBtnCountSelection(countSelections);
        result.withAddOn(new PagingWidgets.ToolbarAddOn() {
            public void addTo(FloatingToolbar toolbar) {
                toolbar.add(btnCountSelection);
            }
        });
    }

    private void initBtnCountSelection(List<String[]> countSelections) {
        final Menu menuCount = new Menu();
        for (final String[] selection : countSelections) {
            final MenuItem menuItem = new MenuItem(selection[1], null);
            menuItem.setData("count", selection[0]); // $NON-NLS$
            menuCount.add(menuItem);
        }

        this.btnCountSelection = new SelectButton();
        this.btnCountSelection.setClickOpensMenu(true);
        this.btnCountSelection.withMenu(menuCount);
        this.btnCountSelection.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> event) {
                setCount((String) event.getSelectedItem().getData("count")); // $NON-NLS$
            }
        });
    }

    private void setCount(String count) {
        this.snippet.setNewsCountSelection(count);
        Integer pageSize = Integer.valueOf(count);
        this.snippet.setPageSize(pageSize);
        this.pp.setPageSize(pageSize);
    }

    private List<String[]> getSelectionModel() {
        final String[] newsCount = getConfiguration().getArray(NewsHeadlinesSnippet.NEWS_COUNT);
        final List<String[]> result = new ArrayList<>();
        final JSONWrapper array = SessionData.INSTANCE.getGuiDef(NewsHeadlinesSnippet.NEWS_COUNT_SELECTION).get("items"); // $NON-NLS$
        for (int i = 0; i < array.size(); i++) {
            final JSONWrapper item = array.get(i);
            final String title = item.get("title").stringValue(); // $NON-NLS$
            final String count = newsCount == null || i >= newsCount.length ? item.get("count").stringValue() : newsCount[i]; // $NON-NLS$
            result.add(new String[]{count, title});
        }
        return result;
    }

    private PagingWidgets.Mode getMode(String pagingMode) {
        if (pagingMode.equals("true")) { // $NON-NLS-0$
            return PagingWidgets.Mode.FULL;
        }
        if (pagingMode.equals("restricted")) { // $NON-NLS-0$
            return PagingWidgets.Mode.RESTRICTED;
        }
        throw new IllegalArgumentException("mode must be either 'true' or 'restricted', it is: " + pagingMode); // $NON-NLS-0$
    }

    public void setLinkListener(LinkListener<NWSSearchElement> linkListener) {
        this.linkListener = linkListener;
    }

    void update(TableDataModel dtm, int offset, int count, int total) {
        update(dtm);
        if (this.pp != null) {
            this.pp.update(offset, count, total);
        }
    }

    void update(TableDataModel tdm) {
        reloadTitle();
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(this.columnModel, "mm-snippetTable fixed"); // $NON-NLS-0$
            if (this.finderLink == null) {
                this.container.setContentWidget(this.tw);
            }
            else {
                final FlowPanel flowPanel = new FlowPanel();
                flowPanel.add(this.tw);
                flowPanel.add(this.finderLink);
                this.container.setContentWidget(flowPanel);
            }
        }
        this.tw.updateData(tdm);
        this.mouseEnterEventReceivers = new ArrayList<>();
        for (int i = 0; i < tdm.getRowCount(); i++) {
            final Element element = this.tw.getTdElement(i, HEADLINE_COLUMN);
            this.mouseEnterEventReceivers.add(element);
        }
        this.setUpMouseOverEvents();
        // sendFirstToLinkListener(tdm);

//        if (this.lbReload == null && SessionData.INSTANCE.isUserPropertyTrue(AppConfig.NEWS_HEADLINES_AUTO_RELOAD)) {
        if (this.lbReload == null) {
            this.lbReload = this.container.addFirstHeaderIcon("mm-snippet-reload-16"); // $NON-NLS$
        }

        this.container.layout();
    }

    private void openPdf() {
        final PdfOptionSpec spec = this.snippet.getPdfOptionSpec();
        if (spec == null) {
            return;
        }

        String uri = PdfOptionHelper.getPdfUri(spec, null);

        ActionPerformedEvent.fire("X_NWS_PDF"); // $NON-NLS-0$

        if (PhoneGapUtil.isPhoneGap()) {
            uri = UrlBuilder.ensureServerPrefix(uri, true);
            PhoneGapUtil.log("NewsHeadlinesSnippetView - PDF Link: " + uri);  //$NON-NLS$
            PhoneGapUtil.openPdfExternal(uri);
        } else {
            Window.open(PdfOptionHelper.getPdfUri(spec, null), "_blank", ""); // $NON-NLS$
        }
    }

    public void reloading(boolean value) {
        if (this.lbReload == null) {
            return;
        }
        this.container.setIconClass(this.lbReload, value ? "mm-snippet-reload-16-active" : "mm-snippet-reload-16"); // $NON-NLS$
    }

    public Integer getHeight() {
        if (this.tw == null) {
            return null;
        }
        final int height = this.tw.getOffsetHeight();
        return height == 0 ? null : height;
    }

    private void setUpMouseOverEvents() {
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            public void onPreviewNativeEvent(Event.NativePreviewEvent preview) {
                NativeEvent event = preview.getNativeEvent();
                Element target = event.getEventTarget().cast();
                if (event.getType().equalsIgnoreCase("mousemove")) { // $NON-NLS-0$
                    if (mouseEnterEventReceivers.contains(target)) {
                        checkHeadlineOverflow(target);
                        preview.consume();
                    }
                }
            }
        });
    }

    private void checkHeadlineOverflow(Element element) {
        if (element.getOffsetWidth() < element.getScrollWidth()) {
            final NodeList<Element> nodeList = element.getElementsByTagName("A"); // $NON-NLS-0$
            if (nodeList.getLength() < 1) {
                return;
            }
            final Element e = nodeList.getItem(0);
            appendTooltip(e, element.getInnerText());
        }
    }

    private void appendTooltip(Element e, String tooltip) {
        e.setAttribute(Tooltip.ATT_QTIP, tooltip);
        e.setAttribute(Tooltip.ATT_COMPLETION, Tooltip.ATT_QTIP);
        e.setAttribute(Tooltip.ATT_STYLE, "mm-linkHover"); // $NON-NLS-0$
    }
}
