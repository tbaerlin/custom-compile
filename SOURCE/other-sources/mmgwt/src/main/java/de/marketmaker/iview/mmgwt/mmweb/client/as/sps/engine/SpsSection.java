package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.widgets.ContentResizePanel;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;

import java.util.List;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public class SpsSection extends SpsContainer implements HasBindFeature, HasCaption, HasFixedNorthWidget {
    private final BindFeature<SpsProperty> bindFeature;
    private Boolean trailingStyle = null;
    private FlowPanel errorPanel;
    private ErrorPanelPosition errorPanelPosition = ErrorPanelPosition.TOP;
    private SpsWidget fixedNorthWidget;

    public enum ErrorPanelPosition { TOP, BOTTOM }

    public SpsSection() {
        this.bindFeature = new BindFeature<>(this);
    }

    public SpsSection withErrorPanelPosition(ErrorPanelPosition position) {
        this.errorPanelPosition = position;
        return this;
    }

    @Override
    public HasCaption withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    @Override
    public BindFeature<SpsProperty> getBindFeature() {
        return this.bindFeature;
    }

    @Override
    public void onPropertyChange() {
        // nothing to do
    }

    private boolean isTrailingStyle() {
        if (this.trailingStyle == null) {
            this.trailingStyle = hasStyle("trailing"); // $NON-NLS$
            if (this.trailingStyle && getChildrenFeature().getChildren().isEmpty()) {
                Firebug.warn("SpsSection with trailing style, but without children: " + getDescId());
                this.trailingStyle = Boolean.FALSE;
            }
        }
        return this.trailingStyle;
    }

    @Override
    public boolean hasCaption() {
        if (isTrailingStyle()) {
            return getChildrenFeature().getChildren().get(0).hasCaption();
        }
        return super.hasCaption();
    }

    @Override
    protected HTML createCaptionWidget() {
        if (isTrailingStyle()) {
            // The first SPS widget provides the captionWidget, all other GWT widgets go to a horizontal panel.
            return getChildrenFeature().getChildren().get(0).getCaptionWidget();
        }

        final HTML captionWidget = super.createCaptionWidget();
        if (!isForceCaptionWidget() && captionWidget != null) {
            captionWidget.setStyleName("sps-section-header");
        }
        return  captionWidget;
    }

    @Override
    protected Widget createCaptionPanel() {
        return !isTrailingStyle() && !isForceCaptionWidget() && getCaptionWidget() != null
                ? super.getCaptionWidget()
                : super.createCaptionPanel();
    }

    @Override
    protected SimplePanel createInfoIconPanel() {
        if (isTrailingStyle()) {
            // The first SPS widget provides the infoIconPanel, all other GWT widgets go to a horizontal panel.
            return getChildrenFeature().getChildren().get(0).getInfoIconPanel();
        }

        return super.createInfoIconPanel();
    }

    @Override
    protected void updateInfoIconPanel() {
        if (!isTrailingStyle()) {
            super.updateInfoIconPanel();
            final ErrorMM error = getError();
            this.errorPanel.clear();
            if (error == null) {
                this.errorPanel.setVisible(false);
            }
            else {
                updateErrorPanelSeverity(error);
                this.errorPanel.add(IconImage.get(SpsUtil.getSeverityIconStyle(error.getErrorSeverity())).createImage());
                this.errorPanel.add(new InlineLabel(error.getErrorString()));
                this.errorPanel.setVisible(true);
            }
        }
    }

    private void updateErrorPanelSeverity(ErrorMM error) {
        for (ErrorSeverity severity : ErrorSeverity.values()) {
            this.errorPanel.removeStyleName(severity.value());
        }
        final ErrorSeverity severity = error.getErrorSeverity();
        if(severity != null) {
            this.errorPanel.addStyleName(severity.value());
        }
    }

    @Override
    protected Panel createWidget() {
        return isTrailingStyle()
                ? createWidgetTrailing()
                : createWidgetDefault();
    }

    private Panel createWidgetTrailing() {
        // The first SPS widget provides the label (its first GWT widget), all other GWT widgets go to a horizontal panel.
        final HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName(getBaseStyle());
        final List<SpsWidget> children = getChildrenFeature().getChildren();
        panel.add(children.get(0).getWidget());
        for (int i = 1; i < children.size(); i++) {
            for (Widget widget : children.get(i).asWidgets()) {
                panel.add(widget);
            }
        }
        return panel;
    }

    private Panel createWidgetDefault() {
        final FlowPanel panelTaskDescription = getLevel() == 0 ? null : createTaskDescriptionPanel(); // if level==0 createNorthWidget is used
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName(getBaseStyle());
        if (panelTaskDescription != null) {
            panel.add(panelTaskDescription);
        }
        this.errorPanel = new FlowPanel();
        this.errorPanel.setStyleName("sps-form-error sps-section-error");  // $NON-NLS$
        this.errorPanel.setVisible(false);
        if(this.errorPanelPosition == ErrorPanelPosition.TOP) {
            panel.add(this.errorPanel);
        }
        if (hasStyle("float")) { // $NON-NLS$
            withFloatClear(addChildWidgetsSingle(panel, "sps-float")); // $NON-NLS$
        }
        else {
            panel.add(createSectionWidget());
        }
        if(this.errorPanelPosition == ErrorPanelPosition.BOTTOM) {
            panel.add(this.errorPanel);
        }
        return panel;
    }

    /**
     * @see de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.SpsSectionWithFixedSectionMock
     */
    @Override
    public Widget createNorthWidget() {
        final FlowPanel taskDescriptionPanel = createTaskDescriptionPanel();
        if(this.fixedNorthWidget == null) {
            return taskDescriptionPanel;
        }

        final FlowPanel container = new FlowPanel();
        container.setStyleName("sps-fixed-north-section-container");

        if(taskDescriptionPanel != null) {
            container.add(taskDescriptionPanel);
        }
        for (Widget widget : this.fixedNorthWidget.asWidgets()) {
            container.add(widget);
        }

        final ContentResizePanel crp = new ContentResizePanel();
        crp.setContentWidget(container);
        return crp;
    }

    private FlowPanel createTaskDescriptionPanel() {
        if (!hasDescription() || isForceCaptionWidget()) {
            return null;
        }
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("sps-section-description");
        final IconImageIcon descriptionIcon = IconImage.isDefined(getDescriptionIcon())
                ? addIcon(panel, getDescriptionIcon())
                : getLevel() == 0 ? addIcon(panel, "PmIcon:Activity") : null; // $NON-NLS$
        final Label descriptionLabel = new HTML(StringUtility.toHtmlLines(getDescription()));
        descriptionLabel.setStyleName("sps-section-descriptionText");
        panel.add(descriptionLabel);
        if (descriptionIcon != null) {
            final Label clearLabel = new Label();
            clearLabel.setStyleName("sps-section-descriptionClear");
            panel.add(clearLabel);
        }
        return panel;
    }

    private IconImageIcon addIcon(FlowPanel panel, String iconClass) {
        final IconImageIcon descriptionIcon = IconImage.getIcon(iconClass);
        descriptionIcon.setStyleName("sps-section-descriptionIcon");
        panel.add(descriptionIcon);
        return descriptionIcon;
    }

    protected Widget createSectionWidget() {
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName(getBaseStyle());
        return addChildWidgets(panel, "sps-child"); // $NON-NLS$
    }

    private Panel withFloatClear(Panel panel) {
        final Label label = new Label();
        label.getElement().getStyle().setClear(Style.Clear.BOTH);
        panel.add(label);
        return panel;
    }

    @Override
    public void setFixedNorthWidget(SpsWidget fixedNorthWidget) {
        this.fixedNorthWidget = fixedNorthWidget;
    }

    @Override
    public List<SpsWidget> findWidgets(BindToken bindToken, List<SpsWidget> spsWidgets) {
        if (this.fixedNorthWidget != null) {
            this.fixedNorthWidget.findWidgets(bindToken, spsWidgets);
        }
        return super.findWidgets(bindToken, spsWidgets);
    }
}
