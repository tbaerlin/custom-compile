/*
 * ValidationMessagePopup.java
 *
 * Created on 14.01.14
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.DependencyFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.HasDependencyFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContainerConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ValidationMessagePopup;
import de.marketmaker.iview.pmxml.ErrorMM;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * @author umaurer
 */
public abstract class SpsWidget<W extends Widget> implements HasVisibility, HasDependencyFeature {
    public enum ErrorType {
        MANDATORY("sps-validation-mandatory"), // $NON-NLS$
        MISC("sps-validation-misc"); // $NON-NLS$

        private final String style;

        ErrorType(String style) {
            this.style = style;
        }

        public String getStyle() {
            return this.style;
        }
    }

    private static final String STYLE_SPS_DISABLED = Styles.get().textBoxDisabled();

    protected static final String ATT_LEVEL = "sps-level"; // $NON-NLS$

    private DependencyFeature dependencyFeature = new DependencyFeature(this);

    private int level = -1;

    private SafeHtml caption;

    private String description;

    private String descriptionIcon;

    private String descId;

    private String baseStyle;

    private String cellStyle;

    private String style;

    private String tooltip;

    private int colSpan = -1;

    private boolean enabled = true;

    private Widget[] widgets;

    private boolean inline = false;

    private boolean readonly = false;

    private boolean mandatory = false;

    public boolean visible = true;

    private HTML captionWidget;

    private boolean captionWidgetDefined = false;

    private boolean forceCaptionWidget = false;

    private SimplePanel infoIconPanel;

    private boolean infoIconPanelDefined = false;

    private W widget;

    private boolean widgetDefined = false;

    private boolean omitMandatoryIcon = false;

    private ErrorMM error = null;

    private ContainerConfig containerConfig = (key, defaultValue) -> defaultValue;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCaption(String caption) {
        this.caption = StringUtil.hasText(caption) ? TextUtil.toSafeHtml(caption) : null;
    }

    public SafeHtml getCaption() {
        return this.caption;
    }

    public boolean hasCaption() {
        return this.caption != null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtil.hasText(description) ? description : null;
    }

    public String getDescriptionIcon() {
        return descriptionIcon;
    }

    public void setDescriptionIcon(String descriptionIcon) {
        this.descriptionIcon = StringUtil.hasText(descriptionIcon) ? descriptionIcon : null;
    }

    public boolean hasDescription() {
        return this.description != null;
    }

    public ContainerConfig getContainerConfig() {
        return this.containerConfig;
    }

    public void setContainerConfig(ContainerConfig containerConfig) {
        this.containerConfig = containerConfig;
        if (this instanceof HasChildrenFeature) {
            final ChildrenFeature childrenFeature = ((HasChildrenFeature) this).getChildrenFeature();
            for (SpsWidget spsWidget : childrenFeature.getChildren()) {
                spsWidget.setContainerConfig(containerConfig);
            }
        }
    }

    public Widget[] asWidgets() {
        this.widgets = createWidgets();

        for (int i = 0; i < this.widgets.length; i++) {
            final Widget widget = this.widgets[i];
            widget.getElement().setAttribute(ATT_LEVEL, String.valueOf(this.level));
            if (this.descId != null) {
                widget.getElement().setAttribute("desc-id", this.descId + "[" + i + "]"); // $NON-NLS$
            }
            if (this.style != null && !CssUtil.hasStyle(widget, "sps-caption-panel")) {   // $NON-NLS$
                widget.addStyleName(this.style);
            }
            if (!this.enabled) {
                widget.addStyleName(STYLE_SPS_DISABLED);
            }
        }
        return this.widgets;
    }

    protected Image createTooltipHelp() {
        final String tooltip = getTooltip();

        if (StringUtil.hasText(tooltip)) {
            final Image image = IconImage.get("sps-help").createImage(); // $NON-NLS$
            image.setStyleName("sps-tooltipHelp");
            Tooltip.addQtip(image, TextUtil.toSafeHtml(tooltip)).wrapped();
            return image;
        }

        return null;
    }

    protected void onWidgetConfigured() {
        try {
            setCaptionWidget(createCaptionWidget());
            setInfoIconPanel(createInfoIconPanel());
            setWidget(createWidget());
            updateInfoIconPanel();
            applyDefaultStyles();
        } catch (RuntimeException e) {
            Firebug.warn("Exception in " + getDescId() + " (" + getClass().getSimpleName() + ")", e);
            throw e;
        }
    }

    protected void applyDefaultStyles() {
        setSize(TextUtil.getDimensionFromSizeStyle(this.style));
        final W widget = getWidget();
        CssUtil.setPrefixedStylePx(widget, this.style, "width"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "height"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "padding"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "paddingTop"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "paddingRight"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "paddingBottom"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "paddingLeft"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "margin"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "marginTop"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "marginRight"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "marginBottom"); // $NON-NLS$
        CssUtil.setPrefixedStylePx(widget, this.style, "marginLeft"); // $NON-NLS$
        CssUtil.setPrefixedStyleColor(widget, this.style, "backgroundColor");  // $NON-NLS$
        CssUtil.setPrefixedStyle(widget, this.style, "textAlign"); // $NON-NLS$
    }

    protected void setCaptionWidget(HTML captionWidget) {
        if (this.captionWidgetDefined) {
            throw new RuntimeException("SpsWidget.captionWidget is already initialized (" + getDescId() + ") - " + getClass().getSimpleName()); // $NON-NLS$
        }
        this.captionWidget = captionWidget;
        this.captionWidgetDefined = true;
    }

    public HTML getCaptionWidget() {
        return this.captionWidget;
    }

    protected void setInfoIconPanel(SimplePanel infoIconPanel) {
        if (this.infoIconPanelDefined) {
            throw new RuntimeException("SpsWidget.infoIconPanel is already initialized (" + getDescId() + ")"); // $NON-NLS$
        }
        this.infoIconPanel = infoIconPanel;
        this.infoIconPanelDefined = true;
    }

    public SimplePanel getInfoIconPanel() {
        return this.infoIconPanel;
    }

    protected abstract W createWidget();

    protected void setWidget(W widget) {
        setWidget(widget, false);
    }

    protected void setWidget(W widget, boolean force) {
        if (this.widgetDefined && !force) {
            throw new RuntimeException("SpsWidget.widget is already initialized (" + getDescId() + ")"); // $NON-NLS$
        }
        this.widget = widget;
        this.widgetDefined = true;
    }

    public W getWidget() {
        return this.widget;
    }

    protected void forceCaptionWidget() {
        this.forceCaptionWidget = true;
    }

    protected boolean isForceCaptionWidget() {
        return this.forceCaptionWidget;
    }

    protected HTML createCaptionWidget() {
        final HTML html;
        if (hasCaption()) {
            html = new HTML(this.caption);
        }
        else if (this.forceCaptionWidget) {
            html = new HTML();
        }
        else {
            return null;
        }

        html.setStyleName("sps-caption");
        if (CssUtil.hasStyle(this.style, "sps-caption-emphasize")) { // $NON-NLS$
            html.addStyleName("sps-caption-emphasize"); // $NON-NLS$
        }
        return html;
    }

    protected SimplePanel createInfoIconPanel() {
        final SimplePanel panel = new SimplePanel();
        panel.setStyleName("sps-infoIcon-panel");
        return panel;
    }

    protected void updateInfoIconPanel() {
        updateInfoIconPanel(this.error);
    }

    protected void updateInfoIconPanel(ErrorMM error) {
        if (this.infoIconPanel == null) {
            return;
        }
        if (error != null && !isReadonly()) {
            final Image iconError = IconImage.get(SpsUtil.getSeverityIconStyle(error.getErrorSeverity())).createImage();
            iconError.addMouseOverHandler(event -> maybeShowErrorPopup(true));
            iconError.addMouseOutHandler(event -> ValidationMessagePopup.I.hide(true));
            setInfoIcon(iconError);
        }
        else if (isMandatory() && !this.omitMandatoryIcon && !isReadonly()) {
            final Image iconMandatory = IconImage.get("sps-field-mandatory").createImage(); // $NON-NLS$
            iconMandatory.setStyleName("sps-iconMandatory");
            Tooltip.addQtip(iconMandatory, I18n.I.mandatoryField());
            setInfoIcon(iconMandatory);
        }
        else {
            setInfoIcon(null);
        }
    }

    private void setInfoIcon(Image image) {
        this.infoIconPanel.setWidget(image);
        this.infoIconPanel.setVisible(image != null);
    }

    @SuppressWarnings("unused")
    public void setOmitMandatoryIcon(boolean omitMandatoryIcon) {
        this.omitMandatoryIcon = omitMandatoryIcon;
    }

    protected void checkWidgetInitialized() {
        if (!this.widgetDefined) {
            throw new RuntimeException("SpsWidget.widget is not initialized"); // $NON-NLS$
        }
        if (!this.captionWidgetDefined) {
            throw new RuntimeException("SpsWidget.captionWidget is not initialized"); // $NON-NLS$
        }
        if (!this.infoIconPanelDefined) {
            throw new RuntimeException("SpsWidget.infoIconPanel is not initialized"); // $NON-NLS$
        }
    }

    protected Widget[] createWidgets() {
        checkWidgetInitialized();

        final Widget widget = this.widget == null ? new Label() : this.widget;

        final Image tooltipHelp = createTooltipHelp();
        final Widget widgetWithTooltip;
        if (tooltipHelp == null) {
            widgetWithTooltip = widget;
        }
        else {
            final Panel p = new HorizontalPanel();
            p.add(widget);
            p.add(tooltipHelp);
            widgetWithTooltip = p;
        }

        final Widget captionPanel = createCaptionPanel();
        return captionPanel == null
                ? new Widget[]{widgetWithTooltip}
                : new Widget[]{captionPanel, widgetWithTooltip};
    }

    protected Widget createCaptionPanel() {
        if (!(hasCaption() || this.forceCaptionWidget)) {
            return null;
        }
        final FlowPanel panelCaption = new FlowPanel();
        panelCaption.setStyleName("sps-caption-panel");
        if (this.infoIconPanel != null) {
            panelCaption.add(this.infoIconPanel);
        }
        if (this.captionWidget != null) {
            panelCaption.add(this.captionWidget);
        }
        return panelCaption;
    }

    public Widget createNorthWidget() {
        return null;
    }

    public String getDescId() {
        return descId;
    }

    public void setDescId(String descId) {
        this.descId = descId;
    }

    public String getBaseStyle() {
        return baseStyle;
    }

    public void setBaseStyle(String baseStyle) {
        this.baseStyle = baseStyle;
    }

    public String getCellStyle() {
        return cellStyle;
    }

    @SuppressWarnings("unused")
    public void setCellStyle(String cellStyle) {
        this.cellStyle = cellStyle;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = StringUtil.hasText(style) ? style : null;
    }

    public boolean hasStyle(String... style) {
        return CssUtil.hasStyle(this.style, style);
    }

    public Float getStyleValueFloat(String prefix) {
        return CssUtil.getStyleValueFloat(this.style, prefix);
    }

    public Integer getStyleValueInt(String prefix) {
        return CssUtil.getStyleValueInt(this.style, prefix);
    }

    public void setSize(Dimension size) {
        if (size != null) {
            final Style style = getWidget().getElement().getStyle();
            style.setWidth(size.getWidth(), PX);
            style.setHeight(size.getHeight(), PX);
        }
    }

    public String getTooltip() {
        return this.tooltip;
    }

    public void setTooltip(String tooltip) {
        if (!StringUtil.hasText(tooltip)) {
            this.tooltip = null;
            return;
        }
        this.tooltip = tooltip;
    }

    public int getColSpan() {
        if (this.colSpan == -1) {
            final Integer colSpanStyle = getStyleValueInt("colSpan"); // $NON-NLS$
            this.colSpan = colSpanStyle == null ? 1 : colSpanStyle;
        }
        return colSpan;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (this.widgets != null) {
            for (Widget widget : this.widgets) {
                if (enabled) {
                    widget.removeStyleName(STYLE_SPS_DISABLED);
                }
                else {
                    widget.addStyleName(STYLE_SPS_DISABLED);
                }
            }
        }
    }

    public void visualizeError(ErrorMM error, boolean enabled) {
        if (enabled) {
            this.error = error;
            updateInfoIconPanel();
            if (hasFocus()) {
                maybeShowErrorPopup(false);
            }
        }
        else {
            this.error = null;
        }
    }

    protected void maybeShowErrorPopup(boolean force) {
        // implemented in SpsBoundWidget
    }

    protected ErrorMM getError() {
        return error;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getWidgetDescription() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getBaseStyle());
        if (this.descId != null) {
            sb.append('[').append(this.descId).append(']');
        }
        return sb.toString();
    }

    public Widget[] getWidgets() {
        return this.widgets;
    }

    public boolean focusFirst() {
        final Focusable focusable = getFocusable();
        if (focusable == null) {
            return false;
        }
        /* Calling setFocus deferred is strictly necessary for IE9, 10 and 11 as well as FF ESR 31 and FF ESR 38.
         * It seems not to be necessary for Chrome. See AS-1263 for further details.
         */
        Scheduler.get().scheduleDeferred(() -> BrowserSpecific.INSTANCE.setFocus(focusable, true));
        return true;
    }

    protected Focusable getFocusable() {
        return getWidget() instanceof Focusable ? (Focusable) getWidget() : null;
    }

    protected boolean hasFocus() {
        return WidgetUtil.hasFocus(getWidget());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    protected void addFocusHandlerChecked(FocusHandler handler) {
        final W w = getWidget();
        if (this instanceof HasFocusHandlers) {
            ((HasFocusHandlers) this).addFocusHandler(handler);
        }
        else if (w instanceof HasFocusHandlers) {
            ((HasFocusHandlers) w).addFocusHandler(handler);
        }
        else if (this instanceof NoValidationPopup) {
            // nothing to do
        }
        else if (SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            Notifications.add("Developer Info", "No addFocusHandler(...) available: " + this.getClass().getSimpleName()); // $NON-NLS$
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    protected void addBlurHandlerChecked(BlurHandler handler) {
        final W w = getWidget();
        if (this instanceof HasBlurHandlers) {
            ((HasBlurHandlers) this).addBlurHandler(handler);
        }
        else if (w instanceof HasBlurHandlers) {
            ((HasBlurHandlers) w).addBlurHandler(handler);
        }
        else if (this instanceof NoValidationPopup) {
            // nothing to do
        }
        else if (SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            Notifications.add("Developer Info", "No addBlurHandler(...) available: " + this.getClass().getSimpleName()); // $NON-NLS$
        }
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        final Widget[] widgets = getWidgets();
        if (widgets != null) {
            for (Widget widget : widgets) {
                widget.setVisible(visible);
            }
        }
        this.visible = visible;
    }

    @Override
    public DependencyFeature getDependencyFeature() {
        return this.dependencyFeature;
    }

    @Override
    public SpsWidget asSpsWidget() {
        return this;
    }

    public void updateProperties() {
        if (this instanceof RequiresPropertyUpdateBeforeSave) {
            ((RequiresPropertyUpdateBeforeSave) this).updatePropertyBeforeSave();
        }
        if (this instanceof HasChildrenFeature) {
            for (SpsWidget child : ((HasChildrenFeature) this).getChildrenFeature().getChildren()) {
                child.updateProperties();
            }
        }
    }

    public SpsWidget findWidget(BindToken bindToken) {
        final List<SpsWidget> widgets = findWidgets(bindToken, new ArrayList<>());
        final int widgetCount = widgets.size();
        if (widgetCount == 0) {
            throw new IllegalStateException("SpsWidgets not found for bindToken: " + bindToken); // $NON-NLS$
        }
        else if (widgetCount > 1) {
            throw new IllegalStateException("multiple (" + widgetCount + ") SpsWidgets found for bindToken: " + bindToken); // $NON-NLS$
        }
        return widgets.get(0);
    }

    public List<SpsWidget> findWidgets(BindToken bindToken, List<SpsWidget> spsWidgets) {
        if (this instanceof HasChildrenFeature) {
            final List<SpsWidget> children = ((HasChildrenFeature) this).getChildrenFeature().getChildren();
            for (SpsWidget child : children) {
                child.findWidgets(bindToken, spsWidgets);
            }
        }
        if (!(this instanceof HasBindFeature)) {
            return spsWidgets;
        }
        final BindFeature bindFeature = ((HasBindFeature) this).getBindFeature();
        final SpsProperty spsProperty = bindFeature.getSpsProperty();
        if (spsProperty != null && spsProperty.getBindToken().equals(bindToken)) {
            spsWidgets.add(this);
        }
        return spsWidgets;
    }

    public void selectAll() {
        final W w = getWidget();
        if (w instanceof ValueBoxBase) {
            Firebug.debug(this.getClass().getSimpleName() + ".selectAll()");
            ((ValueBoxBase) w).selectAll();
        }
        else {
            Firebug.debug(this.getClass().getSimpleName() + ".selectAll() -> not a ValueBoxBase");
        }
    }
}
