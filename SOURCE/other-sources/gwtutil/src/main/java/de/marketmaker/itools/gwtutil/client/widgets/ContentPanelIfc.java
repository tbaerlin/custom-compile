package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Author: umaurer
 * Created: 22.04.15
 */
public interface ContentPanelIfc extends IsWidget, HasAttachHandlers, HasVisibility {
    interface HeaderToolCommand {
        void execute(Widget headerToolWidget);
    }

    void setHeading(String headerHtmlText);
    void setHeaderText(String headerText);
    void setHeader(SafeHtml headerText);
    void setHeaderVisible(boolean visible);
    Element getElement();
    String getHeading();
    void setTopWidget(Widget widget);
    Widget getTopWidget();
    void setBottomWidget(Widget widget);
    void setContentWidget(Widget widget);
    Widget addHeaderTool(String iconClass, HeaderToolCommand command);
    Widget addHeaderTool(String iconClass, String tooltip, HeaderToolCommand command);
    void setToolTooltip(Widget toolWidget, String tooltip);
    void setToolIcon(Widget toolWidget, String iconClass);
    Widget addFirstHeaderIcon(String iconClass);
    void setIconClass(Widget widget, String iconClass);
    void enableDragging(String transfer, String logId);
    void disableDragging();

    // TODO: remove, after gxt is removed
    boolean layout();
    boolean layout(boolean force);
}
