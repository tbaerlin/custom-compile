/*
 * AbstractUserObjectView.java
 *
 * Created on 18.03.13 10:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.HasUserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.MMTalkStringListEntryNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ScrollPanel;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMDistribution;
import de.marketmaker.iview.pmxml.MMDistributionValueProcessed;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.UserField;
import de.marketmaker.iview.pmxml.UserFieldDeclarationDesc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Markus Dick
 */
public abstract class AbstractUserObjectView<T, P extends UserObjectDisplay.UserObjectPresenter<T>>
        implements UserObjectDisplay<T, P> {
    public static final String SECTION_STYLE = "section"; //$NON-NLS$

    public static final String AS_OBJECT_VIEW_FIELD_STYLE = "as-objectView-field"; //$NON-NLS$
    public static final String MULTILINE_STYLE = "multiline"; //$NON-NLS$
    private static final String MULTILINE_NO_LABEL_STYLE = "nolabel"; //$NON-NLS$

    public static final String AS_OBJECT_VIEW_LABEL_STYLE = "as-objectView-label"; //$NON-NLS$

    public static final String AS_OBJECT_VIEW_VALUE_STYLE = "as-objectView-value"; //$NON-NLS$

    public static final String AS_OBJECT_VIEW_LIST_STYLE = "as-objectView-list"; //$NON-NLS$
    public static final String AS_OBJECT_VIEW_VALUE_CONTAINER_STYLE = AS_OBJECT_VIEW_VALUE_STYLE + " container"; //$NON-NLS$
    public static final String COLUMNS_2_STYLE = "columns2"; //$NON-NLS$
    public static final String COLUMNS_3_STYLE = "columns3"; //$NON-NLS$

    public static final String SC_STATIC = "S";  // $NON-NLS$
    public static final String SC_USER_DEFINED_FIELDS = "S_UF";  // $NON-NLS$
    public static final String SC_ADVISOR = "S_A";  // $NON-NLS$
    public static final String SC_INVESTMENT_POLICIES = "S_IP";  // $NON-NLS$
    public static final String SC_REPORTING = "S_R";  // $NON-NLS$
    public static final String SC_GENERAL_COMMENT = "S_GC";  // $NON-NLS$
    public static final String SC_ALLOCATION_COMMENTS = "S_AC";  // $NON-NLS$
    public static final String SC_CONTACT= "S_C";  // $NON-NLS$
    public static final String SC_LINKED_PERSONS = "S_LP";  // $NON-NLS$
    public static final String SC_LINKED_INVESTORS = "S_LI";  // $NON-NLS$
    public static final String SC_LINKED_PROSPECTS = "S_LPR";  // $NON-NLS$
    public static final String SC_TAX = "S_T";  // $NON-NLS$
    public static final String SC_PROFILE = "S_P"; // $NON-NLS$
    public static final String SC_PORTFOLIO_VERSION = "S_PV"; // $NON-NLS$

    private final FlowPanel layout;
    private final ScrollPanel scroller;
    private final DockLayoutPanel viewLayout;

    private final SimpleLayoutPanel toolbarPanel;
    private final FloatingToolbar toolbar;

    private final HashMap<String, UIObject> idSections = new HashMap<>();

    private T userObject;
    public P presenter;

    public AbstractUserObjectView() {
        this(true);
    }

    public AbstractUserObjectView(boolean standalone) {
        this.layout = new FlowPanel();
        this.scroller = new ScrollPanel(this.layout);
        this.scroller.setAlwaysShowScrollBars(false);

        this.toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S);

        this.toolbarPanel = new SimpleLayoutPanel();
        this.toolbarPanel.addStyleName("x-panel-tbar");
        this.toolbarPanel.add(this.toolbar);

        this.viewLayout = new DockLayoutPanel(Style.Unit.PX);
        this.viewLayout.addNorth(toolbarPanel, this.toolbar.getToolbarHeightPixel());
        this.viewLayout.add(this.scroller);

        setToolbarVisible(false);

        if(standalone) {
            this.layout.setStyleName(UserObjectDisplay.VIEW_STYLE);
        }
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    protected P getPresenter() {
        return this.presenter;
    }

    static HTML createListWidget(SafeHtmlBuilder sb) {
        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName(AS_OBJECT_VIEW_FIELD_STYLE);
        html.addStyleName("list"); //$NON-NLS$
        return html;
    }

    static SafeHtmlBuilder appendValueContainerDivOpen(SafeHtmlBuilder sb, String uniqueId) {
        return appendDivOpen(sb, uniqueId, AS_OBJECT_VIEW_VALUE_CONTAINER_STYLE, false);
    }

    protected HasWidgets getLayout() {
        return this.layout;
    }

    protected FloatingToolbar getToolbar() {
        return this.toolbar;
    }

    protected void setToolbarVisible(boolean visible) {
        this.viewLayout.setWidgetHidden(this.toolbarPanel, !visible);
    }

    @Override
    public void setEditButtonVisible(boolean visible) {
        //overwrite if required;
    }

    protected T getUserObject() {
        return this.userObject;
    }

    protected static void addMultilineField(HasWidgets hw, String value) {
        final Widget field = createField(null, value, true, true);
        hw.add(field);
    }

    protected static void addMultilineField(HasWidgets hw, String label, String value) {
        final Widget field = createField(label, value, true);
        hw.add(field);
    }

    protected static void addField(HasWidgets hw, String label, Widget valueWidget) {
        final Widget field = createField(label, valueWidget);
        hw.add(field);
    }

    public static void addField(HasWidgets hw, String label, String value) {
        final Widget field = createField(label, value);
        hw.add(field);
    }

    protected static void addField(HasWidgets hw, String label, Boolean value) {
        final Label widget = new Label(Renderer.BOOLEAN_YES_NO_RENDERER.render(value));
        widget.setStyleName(AS_OBJECT_VIEW_VALUE_STYLE);
        addField(hw, label, widget);
    }

    public void addField(HasWidgets hw, String label, List<MMTalkStringListEntryNode> stringNodeList) {
        if(stringNodeList == null || stringNodeList.isEmpty()) {
            hw.add(createField(label, Renderer.STRING_DOUBLE_DASH.render(null)));
            return;
        }

        final SafeHtmlBuilder sb = appendLabel(new SafeHtmlBuilder(), label);
        appendListDivOpen(sb);

        for(final MMTalkStringListEntryNode node : stringNodeList) {
            final String value = node.getValue();
            appendValue(sb, value != null ? value : "");
        }

        appendDivClose(sb);

        hw.add(createListWidget(sb));
    }

    public static void addField(HasWidgets hw, String label, String currentValue, Renderer<MM> historyItemRenderer, final List<HistoryItem> rawHistoryItemList) {
        final List<HistoryItem> historyItems = MmTalkHelper.cloneWithoutNulls(rawHistoryItemList);
        MmTalkHelper.sortHistoryItems(historyItems);
        final boolean withHistoryItems = historyItems != null && !historyItems.isEmpty();

        final SafeHtmlBuilder sb = appendLabel(new SafeHtmlBuilder(), label);
        appendListDivOpen(sb);

        if(withHistoryItems) {
            appendValue(sb, "withHistory", currentValue); //$NON-NLS$
        }
        else {
            appendValue(sb, currentValue);
        }

        final HashSet<String> historicIds = new HashSet<>();

        if(withHistoryItems) {
            for(final HistoryItem item : historyItems) {
                final String uniqueId = Document.get().createUniqueId();
                historicIds.add(uniqueId);

                appendValueContainerDivOpen(sb, uniqueId);

                final String value = historyItemRenderer.render(item.getDataItem());
                appendDivOpen(sb, null, COLUMNS_2_STYLE, true);
                if(value != null) {
                    sb.appendEscaped(value);
                }
                appendDivClose(sb);

                final String date = PmRenderers.DATE_TIME_STRING.render(item.getDate());
                appendDivOpen(sb, null, COLUMNS_2_STYLE, true);
                if(value != null) {
                    sb.appendEscaped(date);
                }
                appendDivClose(sb);

                appendDivClose(sb);
            }
        }
        appendDivClose(sb);

        final HTMLPanel hp = new HTMLPanel(sb.toSafeHtml());
        hw.add(hp);
        hp.setStyleName(AS_OBJECT_VIEW_FIELD_STYLE);
        hp.addStyleName("list"); //$NON-NLS$

        if(withHistoryItems) {
            hp.add(createShowHistoryTrigger(historicIds, hp, false));

            for(final String id : historicIds) {
                hp.getElementById(id).getStyle().setDisplay(Style.Display.NONE);
            }
        }
    }

    private static Widget createShowHistoryTrigger(final HashSet<String> historicIds, final HTMLPanel hp, boolean mmDistribution) {
        final Button button = Button.text(I18n.I.history())
                .clickHandler(new ClickHandler() {
                    private boolean visible = false;

                    @Override
                    public void onClick(ClickEvent event) {
                        for (final String id : historicIds) {
                            if (this.visible) {
                                hp.getElementById(id).getStyle().setDisplay(Style.Display.NONE);
                            }
                            else {
                                hp.getElementById(id).getStyle().clearDisplay();
                            }
                        }
                        this.visible = !this.visible;
                    }
                }).build();
        if(mmDistribution) {
            button.addStyleName("showHistoryMMDistribution");
        }
        else {
            button.addStyleName("showHistory");
        }
        return button;
    }

    protected Panel addSection(String id, String label) {
        final Panel p = addSection(this.layout, label);
        this.idSections.put(id, p);
        return p;
    }

    protected Panel addDividerSection(String id, String label) {
        final Panel p = addSection(id, label);
        p.addStyleName("divider");
        return p;
    }

    public static Panel addSection(HasWidgets layout, String label) {
        final FlowPanel p = new FlowPanel();
        layout.add(p);
        p.add(createSectionHeader(label));
        p.addStyleName(SECTION_STYLE);
        return p;
    }

    public static void addSubHeading(HasWidgets hw, String label) {
        hw.add(createSubHeader(label));
    }

    protected void addCreationDeactivationDates(HasWidgets hw, String creationDate, String deactivationDate) {
        addField(hw, I18n.I.createdOn(), PmRenderers.DATE_TIME_STRING.render(creationDate));
        addField(hw, I18n.I.deactivatedOn(), PmRenderers.DATE_TIME_STRING.render(deactivationDate));
    }

    protected void addUserDefinedFields(HasUserDefinedFields hasUserDefinedFields) {
        final UserDefinedFields userDefinedFields = hasUserDefinedFields.getUserDefinedFields();

        if(userDefinedFields == null) {
            return;
        }

        final Panel p = addSection(SC_USER_DEFINED_FIELDS, I18n.I.userDefinedFields());
        addUserDefinedFields(p, userDefinedFields);
    }

    public static void addUserDefinedFields(HasWidgets hw, UserDefinedFields userDefinedFields) {
        for(PmWebSupport.UserFieldCategoryWithUserFields c: userDefinedFields.getCategories()) {
            if(!c.isDefaultCategory()) {
                addSubHeading(hw, c.getName());
            }

            List<UserFieldDeclarationDesc> declarations = c.getUserFieldDeclarationDescs();
            List<UserField> fields = c.getUserFields();

            for(int i = 0; i < fields.size(); i++) {
                final UserField uf = fields.get(i);
                final MM dataItem = uf.getDataItem();
                final UserFieldDeclarationDesc declaration = declarations.get(i);
                final List<HistoryItem> historyList = MmTalkHelper.cloneWithoutNulls(userDefinedFields.getHistoryList(uf.getName()));
                final String label = declaration.getDisplayName();

                if(TiType.TI_DISTRIBUTION.equals(declaration.getDecl().getFieldType())) {
                    addDistributionField(hw, label, dataItem, historyList);
                }
                else{
                    final String data = PmRenderers.UserFieldDataItemRenderer.get(declaration).render(dataItem);
                    final Renderer<MM> renderer = PmRenderers.UserFieldDataItemRenderer.get(declaration);
                    addField(hw, label, data, renderer, historyList);
                }
            }
        }
    }

    private static void addDistributionField(HasWidgets hw, String label, MM currentValue, List<HistoryItem> rawHistoryItemList) {
        final MMDistribution value = asMMDistribution(currentValue);
        final List<HistoryItem> historyItems = MmTalkHelper.cloneWithoutNulls(rawHistoryItemList);
        MmTalkHelper.sortHistoryItems(historyItems);
        final boolean withHistoryItems = historyItems != null && !historyItems.isEmpty();
        final String showHistoryIdHook = Document.get().createUniqueId();
        final SafeHtmlBuilder sb = appendLabel(new SafeHtmlBuilder(), label);

        appendListDivOpen(sb);
        if(value == null) {
            appendValue(sb, "");
        }
        else {
            appendMMDistributionValue(sb, null, value);
            if(withHistoryItems) {
                appendDivOpen(sb, null, "showHistoryDistributionContainer", false); //container for show history button    // $NON-NLS$
                appendDivOpen(sb, showHistoryIdHook, null, false); //placeholder for show history id
                appendDivClose(sb);
                appendDivClose(sb);
            }
        }
        appendDivClose(sb);

        final HashSet<String> historicIds = new HashSet<>();
        if(withHistoryItems) {
            boolean firstItem = true;
            for(final HistoryItem item : historyItems) {
                final String uniqueId1 = Document.get().createUniqueId();
                final String uniqueId2 = Document.get().createUniqueId();
                historicIds.add(uniqueId1);
                historicIds.add(uniqueId2);

                appendListDivOpen(sb, uniqueId1);

                appendDivOpen(sb, uniqueId2, AS_OBJECT_VIEW_VALUE_CONTAINER_STYLE + (firstItem ? " mmDistributionHistoryDateFirst" : " mmDistributionHistoryDate") , false);  // $NON-NLS$
                if(firstItem) {
                    firstItem = false;
                }
                appendDivOpen(sb, null, null , true);
                sb.append(SafeHtmlUtils.fromString(PmRenderers.DATE_TIME_STRING.render(item.getDate())));
                appendDivClose(sb);

                appendDivClose(sb);

                final MMDistribution mmDistribution = asMMDistribution(item.getDataItem());
                if(mmDistribution == null) {
                    appendValue(sb, ""); //$NON-NLS$
                }
                else {
                    appendMMDistributionValue(sb, historicIds, mmDistribution);
                }

                appendDivClose(sb);
            }
        }

        final HTMLPanel hp = new HTMLPanel(sb.toSafeHtml());
        hw.add(hp);
        hp.setStyleName(AS_OBJECT_VIEW_FIELD_STYLE);
        hp.addStyleName("list"); //$NON-NLS$

        if(withHistoryItems) {
            hp.addAndReplaceElement(createShowHistoryTrigger(historicIds, hp, true), showHistoryIdHook);

            for(final String id : historicIds) {
                hp.getElementById(id).getStyle().setDisplay(Style.Display.NONE);
            }
        }
    }

    private static MMDistribution asMMDistribution(MM currentValue) {
        return currentValue instanceof MMDistribution ? (MMDistribution) currentValue : null;
    }

    private static void appendMMDistributionValue(SafeHtmlBuilder sb, Set<String> uniqueIds, MMDistribution value) {
        for(final MMDistributionValueProcessed item : value.getContent()) {
            final String uniqueId = uniqueIds != null ? Document.get().createUniqueId() : null;
            if(uniqueIds != null) {
                uniqueIds.add(uniqueId);
            }

            appendValueContainerDivOpen(sb, uniqueId);

            final String displayName = item.getValue().getDisplayName();
            appendDivOpen(sb, null, COLUMNS_3_STYLE, true);  // add columns 3 style
            if (displayName != null) {
                sb.appendEscaped(displayName);
            }
            appendDivClose(sb);

            final String normalizedWeight = Renderer.PERCENT23.render(item.getNormalizedWeight());
            appendDivOpen(sb, null, COLUMNS_3_STYLE, true);   // add columns 3 style
            if (normalizedWeight != null) {
                sb.appendEscaped(normalizedWeight);
            }
            appendDivClose(sb);

            final String weight = Renderer.PRICE23.render(item.getValue().getWeight());
            appendDivOpen(sb, null, COLUMNS_3_STYLE, true);   // add columns 3 style
            if (weight != null) {
                sb.appendEscaped(weight);
            }
            appendDivClose(sb);

            appendDivClose(sb);
        }
    }

    @Override
    public Widget asWidget() {
        return this.viewLayout;
    }

    static SafeHtmlBuilder appendLabel(SafeHtmlBuilder sb, String label) {
        return appendTextDiv(sb, AS_OBJECT_VIEW_LABEL_STYLE, label);
    }

    static SafeHtmlBuilder appendValue(SafeHtmlBuilder sb, String value) {
        return appendTextDiv(sb, AS_OBJECT_VIEW_VALUE_STYLE, value);
    }

    static SafeHtmlBuilder appendValue(SafeHtmlBuilder sb, String additionalStyle, String value) {
        return appendTextDiv(sb, AS_OBJECT_VIEW_VALUE_STYLE + " " + additionalStyle, value);
    }

    static SafeHtmlBuilder appendTextDiv(SafeHtmlBuilder sb, String styleName, String value) {
        appendDivOpen(sb, null, styleName, true);
        if(StringUtil.hasText(value)) {
            sb.appendEscaped(value);
        }
        return appendDivClose(sb);
    }

    static SafeHtmlBuilder appendDivOpen(SafeHtmlBuilder sb, String uniqueId, String styleClasses, boolean autoCompletion) {
        sb.appendHtmlConstant("<div");

        if(StringUtil.hasText(uniqueId)) {
            sb.appendHtmlConstant(" id=\"").appendHtmlConstant(uniqueId).appendHtmlConstant("\"");
        }

        if (autoCompletion) {
            sb.appendHtmlConstant(" completion=\"auto\"");
        }

        if(styleClasses == null) {
            return sb.appendHtmlConstant(">"); //$NON-NLS$
        }

        return sb.appendHtmlConstant(" class=\"") //$NON-NLS$
                .appendHtmlConstant(styleClasses)
                .appendHtmlConstant("\">"); //$NON-NLS$
    }

    static SafeHtmlBuilder appendListDivOpen(SafeHtmlBuilder sb) {
        return appendListDivOpen(sb, null);
    }

    static SafeHtmlBuilder appendListDivOpen(SafeHtmlBuilder sb, String uniqueId) {
        return appendDivOpen(sb, uniqueId, AS_OBJECT_VIEW_LIST_STYLE, false);
    }

    static SafeHtmlBuilder appendDivClose(SafeHtmlBuilder sb) {
        return sb.appendHtmlConstant("</div>"); //$NON-NLS$
    }

    private static Widget createField(String label, String value) {
        return createField(label, value, false, false);
    }

    private static Widget createField(String label, String rawValue, boolean multiline) {
        return createField(label, rawValue, multiline, false);
    }

    private static Widget createField(String label, String rawValue, boolean multiline, boolean multilineWithoutLabel) {
        final String value = rawValue == null ? "" : rawValue;
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();

        if(!(multiline && multilineWithoutLabel)) {
            appendLabel(sb, label);
        }
        appendValue(sb, value);

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName(AS_OBJECT_VIEW_FIELD_STYLE);
        if(multiline) {
            html.addStyleName(MULTILINE_STYLE);

            if(multilineWithoutLabel) {
                html.addStyleName(MULTILINE_NO_LABEL_STYLE);
            }
        }
        return html;
    }

    private static Widget createField(String label, Widget valueWidget) {
        final FlowPanel p = new FlowPanel();
        final Label l = new Label(label);
        p.add(l);
        Tooltip.addAutoCompletion(l);
        l.setStyleName(AS_OBJECT_VIEW_LABEL_STYLE);
        p.add(valueWidget);
        valueWidget.addStyleName(AS_OBJECT_VIEW_VALUE_STYLE);
        p.setStyleName(AS_OBJECT_VIEW_FIELD_STYLE);
        return p;
    }

    private static Widget createSectionHeader(String heading) {
        final Label label = new Label(heading);
        label.setStyleName("as-objectView-header"); //$NON-NLS$
        return label;
    }

    private static Widget createSubHeader(String heading) {
        final Label label = new Label(heading);
        label.setStyleName("as-objectView-subheader"); //$NON-NLS$
        return label;
    }

    public void setSelectedSection(String id) {
        for(UIObject uio : this.idSections.values()) {
            uio.removeStyleName("selected");
        }
        final UIObject uio = this.idSections.get(id);
        uio.addStyleName("selected");
    }

    @Override
    public void ensureVisible(String id) {
        if(!StringUtil.hasText(id)) return;

        final UIObject uio = this.idSections.get(id);
        if(uio == null) {
            return;
        }

        setSelectedSection(id);

        this.scroller.scrollToTop(uio);
    }

    @Override
    public void updateView(T userObject) {
        this.userObject = userObject;
        this.idSections.clear();
        this.layout.clear();
    }

    @Override
    public String getPrintHtml() {
        return "<div class=\"" + VIEW_STYLE + "\">" + this.layout.getElement().getInnerHTML() + "</div>"; //$NON-NLS$
    }
}
