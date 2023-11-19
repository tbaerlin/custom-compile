/*
 * DTTableWidget.java
 *
 * Created on 06.05.13 16:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.css.CSS;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RequiresResize;
import de.marketmaker.itools.gwtutil.client.event.KeyModifiers;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.ContentResizePanel;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ActivityDefinitionInfo;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.DTSortSpec;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.gwt.dom.client.Style.Unit;
import static com.google.gwt.dom.client.Style.Visibility;
import static com.google.gwt.query.client.GQuery.$;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext.*;

/**
 * @author Markus Dick
 */
@NonNLS
public class DTTableWidget extends Composite implements RequiresResize, HasColumnHeaderHandlers {
    public static final String SELECTED_CLASS_NAME = "selected";
    public static final String E_NBSP = "&nbsp;";

    private Supplier<DTTableRenderer> dtTableRendererSupplier;
    private final List<Consumer<Element>> tableBodyConsumers = new ArrayList<>();

    private DTTable currentDtTable;
    private DTTableRenderer.Options options;
    private final ArrayList<DTSortSpec> dtSortSpecs = new ArrayList<>();


    @SuppressWarnings("FieldCanBeLocal")
    private final FlowPanel dtBox;
    private final HTML dtHeader;
    private final ContentResizePanel crpBody;
    private final HTML dtBody;

    private final HTML gradientTop;
    private final HTML gradientBottom;

    private Element selectedRow;

    private String historyContextName;

    private Map<String, Element> mapSortableHeaders;

    private final EventListener rowClickedListener = event -> {
        switch(event.getTypeInt()) {
            case Event.ONCLICK:
                handleRowClick(event);
                break;
        }
    };

    private final EventListener linkClickedListener = this::handleLinkClick;

    private final EventListener columnHeaderEventLister = event -> {
        final Element target = getPmColIndexElement(Element.as(event.getEventTarget()));
        final Element th = TableCellElement.as(target);
        final String colIndexAttr = th.getAttribute(DTTableRenderer.PM_COL_INDEX_ATTRIBUTE_NAME);

        if(event.getTypeInt() == Event.ONCLICK) {
            if (target == null) {
                Firebug.warn("<DTTableWidget.onBrowserEvent> ONCLICK no parent element with pm-colindex attribute found");
                return;
            }
            handleSort(th, KeyModifiers.isCtrl(event));
            ColumnHeaderEvent.fire(DTTableWidget.this, th, Integer.parseInt(colIndexAttr), ColumnHeaderEvent.What.SORT_CLICKED, event);
        }
        if(event.getTypeInt() == Event.ONMOUSEOVER || event.getTypeInt() == Event.ONMOUSEOUT) {
            if (target == null) {
                Firebug.warn("<DTTableWidget.onBrowserEvent> ONMOUSEOVER|ONMOUSEOUT no parent element with pm-colindex attribute found");
                return;
            }
            if(StringUtil.hasText(colIndexAttr)) {
                try {
                    final ColumnHeaderEvent.What what = event.getTypeInt() == Event.ONMOUSEOVER
                            ? ColumnHeaderEvent.What.MOUSE_OVER : ColumnHeaderEvent.What.MOUSE_OUT;
                    ColumnHeaderEvent.fire(DTTableWidget.this, th, Integer.parseInt(colIndexAttr), what, event);
                }
                catch(NumberFormatException nfe) {
                    Firebug.warn("<DTTableWidget.onBrowserEvent> ONMOUSEOVER|ONMOUSEOUT columnIndex attribute is not a number \"" + colIndexAttr + "\"", nfe);
                }
            }
        }
    };

    public DTTableWidget() {
        this.dtBox = new FlowPanel();
        initWidget(this.dtBox);
        this.dtBox.setStyleName("dt-box");

        this.dtHeader = new HTML();
        this.dtHeader.setStyleName("dt-header");
        this.dtBox.add(this.dtHeader);

        this.dtBody = new HTML();
        this.crpBody = new ContentResizePanel();
        this.crpBody.setStyleName("dt-body");
        this.crpBody.setContentWidget(this.dtBody);
        this.crpBody.addResizeHandler(event -> setGradientVisibility(crpBody.getElement()));
        this.dtBox.add(this.crpBody);
//        this.dtBox.add(this.dtBody);
        this.crpBody.addHandler(event -> updateHeaderScrollPosition(crpBody.getElement()), ScrollEvent.getType());
        this.crpBody.sinkEvents(com.google.gwt.user.client.Event.ONSCROLL);

        this.gradientTop = new HTML(E_NBSP);
        this.dtBox.add(this.gradientTop);
        this.gradientTop.setStyleName("gradient-top");

        this.gradientBottom = new HTML(E_NBSP);
        this.dtBox.add(this.gradientBottom);
        this.gradientBottom.setStyleName("gradient-bottom");

        this.dtTableRendererSupplier = () -> new DTTableRenderer(DTTableWidget.this.currentDtTable, DTTableWidget.this.options);

        final Consumer<Element> rowMarkerInitializer = new Consumer<Element>() {
            @Override
            public void accept(Element element) {
                final NodeList<Element> trs = element.getElementsByTagName(TableRowElement.TAG);

                for (int i = 0; i < trs.getLength(); i++) {
                    final Element tr = trs.getItem(i);
                    DOM.setEventListener(tr, DTTableWidget.this.rowClickedListener);
                    DOM.sinkEvents(tr, Event.ONCLICK | Event.ONDBLCLICK);
                }
            }
        };

        final Consumer<Element> rowLinkInitializer = new Consumer<Element>() {
            @Override
            public void accept(Element element) {
                final NodeList<Element> as = element.getElementsByTagName(AnchorElement.TAG);

                for (int i = 0; i < as.getLength(); i++) {
                    final Element a = as.getItem(i);
                    DOM.setEventListener(a, DTTableWidget.this.linkClickedListener);
                    DOM.sinkEvents(a, Event.ONCLICK);
                }
            }
        };
        this.tableBodyConsumers.addAll(Arrays.asList(rowMarkerInitializer, rowLinkInitializer));
    }

    @Override
    public HandlerRegistration addColumnHeaderHandler(ColumnHeaderHandler handler) {
        return addHandler(handler, ColumnHeaderEvent.getType());
    }

    private void updateHeaderScrollPosition(Element target) {
        final Element headerTable = this.dtHeader.getElement()
                .getElementsByTagName(TableElement.TAG).getItem(0);
        headerTable.getStyle().setLeft(-target.getScrollLeft(), Unit.PX);

        setGradientVisibility(target);
    }

    private void handleLinkClick(Event event) {
        Element e = Element.as(event.getEventTarget());
        while(e != null) {
            if (AnchorElement.TAG.equalsIgnoreCase(e.getTagName())) {
                doLink((AnchorElement) e);
                break;
            }
            e = e.getParentElement();
        }
    }

    private void doLink(AnchorElement a) {
        if(a == null) {
            Firebug.debug("<DTTableWidget.doLink> anchor is null. Nothing to do.");
            return;
        }

        if(!doActivityLink(a)) {
            doShellMMLink(a);
        }
    }

    private boolean doActivityLink(AnchorElement a) {
        final String activityId = a.getAttribute(DTTableRenderer.PM_ACTIVITYID_ATTRIBUTE_NAME);
        final String activityName = a.getAttribute(DTTableRenderer.PM_ACTIVITYNAME_ATTRIBUTE_NAME);

        if(!(StringUtil.hasText(activityId) && StringUtil.hasText(activityName))) {
            return false;
        }

        Firebug.debug("<DTTableWidget.doActivityLink> attributes of anchor: activityId=" + activityId + " activityName=" + activityName);

        ActivityInstanceInfo aii = createActivityInstanceInfo(activityId, activityName);
        final List<ActivityInstanceInfo> contextItems = DTTableUtils.extractContextActivityItemsFor(aii, this.currentDtTable, this.options);
        final PmItemListContext.ActivityItemListContext ctx = createForActivity(this.historyContextName, null, aii, contextItems);
        ctx.doAction(ctx);

        return true;
    }

    private static ActivityInstanceInfo createActivityInstanceInfo(String activityId, String activityName) {
        final ActivityInstanceInfo aii = new ActivityInstanceInfo();
        aii.setId(activityId);

        final ActivityDefinitionInfo adi = new ActivityDefinitionInfo();
        adi.setName(activityName);
        aii.setDefinition(adi);

        return aii;
    }

    private List<ShellMMInfo> extractContextItems(ShellMMInfo info) {
        return DTTableUtils.extractContextShellMMInfoItemsFor(info, this.currentDtTable, this.options);
    }

    private void doShellMMLink(AnchorElement a) {
        final String shellMMType = a.getAttribute(DTTableRenderer.PM_SHELLTYPE_ATTRIBUTE_NAME);
        final String shellMMId = a.getAttribute(DTTableRenderer.PM_SHELLID_ATTRIBUTE_NAME);
        final String shellMMSid = a.getAttribute(DTTableRenderer.PM_SHELLMMSID_ATTRIBUTE_NAME);

        Firebug.debug("<DTTableWidget.doShellMMLink> attributes of anchor: shellMMType=" + shellMMType + " shellMMId=" + shellMMId + " shellMMSecurityID=" + shellMMSid);

        if(StringUtil.hasText(shellMMType)) {
            final ShellMMType type = ShellMMType.valueOf(shellMMType);

            final ShellMMInfo info = new ShellMMInfo();
            info.setTyp(type);
            info.setId(shellMMId);

            if(StringUtil.hasText(shellMMSid)) {
                info.setMMSecurityID(shellMMSid);
            }

            if(PmPlaceUtil.canGoTo(info)) {
                final List<ShellMMInfo> items = extractContextItems(info);
                final ShellMMInfoItemListContext ctx = createForShellMMInfo(this.historyContextName, info, items);
                ctx.doAction(ctx);
            }
        }
    }

    public void selectRow(Element tr) {
        if(!TableRowElement.TAG.equalsIgnoreCase(tr.getTagName())) {
            Firebug.warn("<DTTableWidget.selectRow> given element is not a TR element!");
        }

        if(this.selectedRow != null) {
            this.selectedRow.removeClassName(SELECTED_CLASS_NAME);
        }
        this.selectedRow = tr;
        tr.addClassName(SELECTED_CLASS_NAME);
    }

    private void handleRowClick(Event event) {
        Firebug.debug("<DTTableWidget.handleRowClick>");

        final Element e = Element.as(event.getEventTarget());
        final Element tr;

        if(TableCellElement.TAG_TD.equalsIgnoreCase(e.getTagName())) {
            tr = Element.as(e.getParentNode());
        }
        else {
            Firebug.debug("<DTTableWidget.handleRowClick> selection click was not on a td element! doing nothing...");
            return;
        }

        selectRow(tr);
    }

    public DTTableWidget withSupplier(
            Supplier<DTTableRenderer> dtTableRendererSupplier) {
        if(this.dtTableRendererSupplier == null) {
            throw new IllegalArgumentException("Parameter dtTableRendererSupplier must not be null!");
        }

        this.dtTableRendererSupplier = dtTableRendererSupplier;
        return this;
    }

    public DTTableWidget withClearTableBodyConsumers() {
        this.tableBodyConsumers.clear();
        return this;
    }

    public DTTableWidget withTableBodyConsumer(Consumer<Element> consumer) {
        this.tableBodyConsumers.add(consumer);
        return this;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        onResize();
    }

    @Override
    public void onResize() {
//        addCtrlKeyHandlers();
        determineHeaderHeightAndAdjustLayout();
    }

    @Override
    protected void onDetach() {
//        removeCtrlKeyHandlers();
        super.onDetach();
    }

    public void updateTable(DTTable dtTable) {
        if(dtTable == null) {
            this.dtHeader.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
            this.dtBody.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
            return;
        }

        this.currentDtTable = dtTable;

        this.dtSortSpecs.clear();
        if(this.options.isWithCustomSort()) {
            this.dtSortSpecs.addAll(this.options.getCustomSort());
        }
        else {
            this.dtSortSpecs.addAll(dtTable.getSortSpecs());
        }

        final DTTableRenderer renderer = this.dtTableRendererSupplier.get();
        this.dtHeader.setHTML(renderer.getHeadTable());
        initColumnHeaderListener();
        doUpdateTableBody(renderer);
        updateHeaderScrollPosition(this.crpBody.getElement());
    }

    private void setGradientVisibility(Element target) {
        final double right = target.getOffsetWidth() - target.getClientWidth();

        final Style gradientTopStyle = this.gradientTop.getElement().getStyle();
        gradientTopStyle.setRight(right, Unit.PX);
        gradientTopStyle.setVisibility(target.getScrollTop() > 1 ? Visibility.VISIBLE : Visibility.HIDDEN);

        final Style gradientBottomStyle = this.gradientBottom.getElement().getStyle();
        gradientBottomStyle.setRight(right, Unit.PX);
        gradientBottomStyle.setBottom(target.getOffsetHeight() - target.getClientHeight(), Unit.PX);

        final int scrollTopMax = BrowserSpecific.INSTANCE.getScrollTopMax(target);
        gradientBottomStyle.setVisibility(target.getScrollTop() < scrollTopMax - 1 ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    public void setOptions(DTTableRenderer.Options options) {
        this.options = options;
    }

    public DTTableRenderer.Options getOptions() {
        return this.options;
    }


    @SuppressWarnings("UnusedDeclaration")
    public DTTable getCurrentDtTable() {
        return this.currentDtTable;
    }

    private void initColumnHeaderListener() {
        final NodeList<Element> ths = this.dtHeader.getElement().getElementsByTagName(TableCellElement.TAG_TH);
        this.mapSortableHeaders = new HashMap<>();

        final String popupHookIcon = IconImage.get("pm-filter").getHTML();

        for(int i = 0; i < ths.getLength(); i++) {
            final Element th = ths.getItem(i);
            final String colIndexAttr = th.getAttribute(DTTableRenderer.PM_COL_INDEX_ATTRIBUTE_NAME);

            if(StringUtil.hasText(colIndexAttr)) {
                try {
                    final int colIndex = Integer.parseInt(colIndexAttr);
                    this.mapSortableHeaders.put(colIndexAttr, th);
                    DOM.setEventListener(th, this.columnHeaderEventLister);
                    DOM.sinkEvents(th, Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);

                    $(th).prepend("<div class=\"dt-popupHook\" style=\"display:NONE;\">" + popupHookIcon + "</div>")
                            .find("div.dt-popupHook")
                            .click(new Function() {
                                @SuppressWarnings("Contract") //There is no such contract defined.
                                @Override
                                public boolean f(final Event e) {
                                    ColumnHeaderEvent.fire(DTTableWidget.this, th, colIndex, ColumnHeaderEvent.What.POPUP_HOOK_CLICKED, e);
                                    return false;
                                }
                            });
                }
                catch(NumberFormatException nfe) {
                    Firebug.warn("<DTTableWidget.initColumnHeaderListener> failed to parse colIndex: \"" + colIndexAttr + "\"", nfe);
                }
            }
        }
        updateSortStyles(this.mapSortableHeaders);
    }

    public void setPopupHookVisible(final int colIndex, final boolean visible) {
        $(this.dtHeader).find("th").each(new Function() {
            @Override
            public void f(Element e) {
                final String colIndexAttr = e.getAttribute(DTTableRenderer.PM_COL_INDEX_ATTRIBUTE_NAME);
                try {
                    if (StringUtil.hasText(colIndexAttr) && colIndex == Integer.parseInt(colIndexAttr)) {
                        if (visible) {
                            $(e).children("div.dt-popupHook").css(CSS.DISPLAY.with(Style.Display.BLOCK));
                        } else {
                            $(e).children("div.dt-popupHook").css(CSS.DISPLAY.with(Style.Display.NONE));
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Firebug.warn("<DTTableWidget.setPopupHookVisible> colIndex=" + colIndex + ", visible=" + visible, nfe);
                }
            }
        });
    }

    private void updateSortStyles(Map<String, Element> mapSortableHeaders) {
        for (DTSortSpec spec : this.dtSortSpecs) {
            final String colIndex = spec.getColIndex();
            final Element th = mapSortableHeaders.get(colIndex);
            if (th != null) {
                th.setClassName(spec.isAscending() ? "asc" : "desc");
            }
        }
    }

    private void handleSort(Element th, boolean multiColumnSelect) {
        //Pressing control key and selecting a column appends a new sortSpec or toggles/removes an existing one.
        //Selecting a column without pressing the control key clears multiple columns and adds a new one or
        //toggles/removes an existing single sortSpec.
        final String colIndex = th.getAttribute(DTTableRenderer.PM_COL_INDEX_ATTRIBUTE_NAME);
        if((!multiColumnSelect && !this.dtSortSpecs.isEmpty()) &&
                (this.dtSortSpecs.size() > 1 || !colIndex.equals(this.dtSortSpecs.get(0).getColIndex()))) {
            this.dtSortSpecs.clear();
            clearSortStyles();
        }
        handleSortSpec(this.dtSortSpecs, th, colIndex);
        updateTableBody();
    }

    private void clearSortStyles() {
        final NodeList<Element> ths = this.dtHeader.getElement().getElementsByTagName(TableCellElement.TAG_TH);

        for(int i = 0; i < ths.getLength(); i++) {
            final Element th = ths.getItem(i);
            final String colIndex = th.getAttribute(DTTableRenderer.PM_COL_INDEX_ATTRIBUTE_NAME);

            if(StringUtil.hasText(colIndex)) {
                th.setClassName("");
            }
        }
    }

    private void handleSortSpec(ArrayList<DTSortSpec> specs, Element th, String colIndex) {
        final DTSortSpec sortSpec = findSortSpec(specs, colIndex);

        if(sortSpec == null) {
            final DTSortSpec newSortSpec = new DTSortSpec();
            newSortSpec.setColIndex(colIndex);
            newSortSpec.setAscending(true);
            th.setClassName("asc");
            specs.add(newSortSpec);
            Firebug.debug("<DTTableWidget.handleSortSpec> sortSpec for colIndex " + colIndex + " not found! Adding new one new size is " + specs.size());
        }
        else if (sortSpec.isAscending()) {
            sortSpec.setAscending(false);
            th.setClassName("desc");
            Firebug.debug("<DTTableWidget.handleSortSpec> sortSpec for colIndex " + colIndex + " found! toggled isAscending=" + sortSpec.isAscending());
        }
        else {
            specs.remove(sortSpec);
            th.setClassName("");
            Firebug.debug("<DTTableWidget.handleSortSpec> sortSpec for colIndex " + colIndex + " found! removed sortSpec");
        }
    }

    private static DTSortSpec findSortSpec(ArrayList<DTSortSpec> specs, String colIndex) {
        for(DTSortSpec spec : specs) {
            if(colIndex.equals(spec.getColIndex())) {
                return spec;
            }
        }
        return null;
    }

    private Element getPmColIndexElement(Element elt) {
        while (elt != null && !TableCellElement.TAG_TH.equalsIgnoreCase(elt.getTagName()) && !StringUtil.hasText(elt.getAttribute(DTTableRenderer.PM_COL_INDEX_ATTRIBUTE_NAME))) {
            elt = elt.getParentElement();
        }
        return elt;
    }

    private void updateTableBody() {
        if(this.currentDtTable == null) {
            Firebug.debug("<DTTableWidget.updateTableBody> Cannot update table body because current DTTable is null!");
            return;
        }

        this.options.withCustomSort(new ArrayList<>(this.dtSortSpecs)); //expose only a copy of the internal list due to side effects

        doUpdateTableBody(this.dtTableRendererSupplier.get());
    }

    private void determineHeaderHeightAndAdjustLayout() {
        final NodeList<Node> nodes = this.dtHeader.getElement().getChildNodes();

        int offsetHeight = 0;

        if(nodes.getLength() > 0) {
            final Node node = nodes.getItem(0);
            if(Element.is(node)) {
                final Element element = Element.as(node);
                if(TableElement.TAG.equalsIgnoreCase(element.getTagName())) {
                    final TableElement tableElement = (TableElement)element;
                    offsetHeight = tableElement.getOffsetHeight();
                }
            }
        }
        if(offsetHeight == 0) {
            return;
        }

        this.dtHeader.getElement().getStyle().setHeight(offsetHeight, Unit.PX);
        this.crpBody.getElement().getStyle().setTop(offsetHeight, Unit.PX);
        this.gradientTop.getElement().getStyle().setTop(offsetHeight, Unit.PX);
    }

    private void doUpdateTableBody(DTTableRenderer renderer) {
        this.dtBody.setHTML(renderer.getBodyTable());
        updateSortStyles(this.mapSortableHeaders);
        applyTableBodyConsumers();
    }

    private void applyTableBodyConsumers() {
        final Element tbody = this.dtBody.getElement().getElementsByTagName(TableElement.TAG)
                .getItem(0).getElementsByTagName("tbody").getItem(0);

        for (Consumer<Element> consumer : this.tableBodyConsumers) {
            consumer.accept(tbody);
        }
    }

    public void setHistoryContextName(String historyContextName) {
        this.historyContextName = historyContextName;
    }

    public void addPrintHtml(final SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<div class=\"dt-header\">");
        sb.appendHtmlConstant(this.dtHeader.getElement().getInnerHTML());
        sb.appendHtmlConstant("</div>");
        sb.appendHtmlConstant("<div class=\"dt-body\">");
        sb.appendHtmlConstant(this.dtBody.getElement().getInnerHTML());
        sb.appendHtmlConstant("</div>");
    }
}
