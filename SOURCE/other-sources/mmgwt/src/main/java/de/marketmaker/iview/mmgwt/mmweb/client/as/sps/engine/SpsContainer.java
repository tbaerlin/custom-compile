package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public abstract class SpsContainer extends SpsWidget<Panel> implements HasChildrenFeature {
    private final ChildrenFeature childrenFeature = new ChildrenFeature();
    private boolean formContainer = false;

    @Override
    public ChildrenFeature getChildrenFeature() {
        return this.childrenFeature;
    }

    public SpsContainer withFormContainer(boolean formContainer) {
        this.formContainer = formContainer;
        return this;
    }

    @Override
    public void release() {
        this.childrenFeature.releaseChildren();
    }

    @Override
    public boolean isFormContainer() {
        return this.formContainer;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setChildrenEnabled(enabled);
    }

    protected void setChildrenEnabled(boolean enabled) {
        for (SpsWidget spsWidget : this.childrenFeature.getChildren()) {
            spsWidget.setEnabled(enabled);
        }
    }

    protected Panel addChildWidgets(Panel panel, String pClass) {
        return addChildWidgetsMulti(panel, pClass);
    }

    protected Panel addChildWidgetsSingle(Panel panel, String pClass) {
        for (SpsWidget spsWidget : this.childrenFeature.getChildren()) {
            final Widget[] widgets = spsWidget.asWidgets();
            final FlowPanel spsWidgetPanel = new FlowPanel();
            spsWidgetPanel.setStyleName(pClass);
            for (Widget widget : widgets) {
                spsWidgetPanel.add(widget);
            }
            panel.add(spsWidgetPanel);
        }
        return panel;
    }

    protected Panel addChildWidgetsMulti(Panel panel, String pClass) {
        for (SpsWidget spsWidget : this.childrenFeature.getChildren()) {
            final Widget[] widgets = spsWidget.asWidgets();
            for (Widget widget : widgets) {
                final SimplePanel p = new SimplePanel(widget);
                p.setStyleName(pClass);
                panel.add(p);
            }
        }
        return panel;
    }

    @Override
    public void setInline(boolean inline) {
        super.setInline(inline);
        setChildrenInline(inline);
    }

    protected void setChildrenInline(boolean inline) {
        for (SpsWidget spsWidget : this.childrenFeature.getChildren()) {
            spsWidget.setInline(inline);
        }
    }

    @Override
    public boolean focusFirst() {
        for (SpsWidget spsWidget : getChildrenFeature().getChildren()) {
            if (spsWidget.focusFirst()) {
                return true;
            }
        }
        return false;
    }
}
