package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DragDropSupport;

/**
 * User: umaurer
 * Date: 10.06.13
 * Time: 15:27
 */
public class SnippetPanel extends Composite implements ContentPanelIfc {
    private final FlowPanel panel = new FlowPanel();
    private final FlowPanel header = new FlowPanel();
    private HorizontalPanel headerTools = null;
    private final InlineHTML headerText = new InlineHTML();
    private final SimplePanel panelTopWidget = new SimplePanel();
    private final SimplePanel body = new SimplePanel();
    private HandlerRegistration draggableHandlerRegistration;

    public SnippetPanel() {
        this.panel.setStyleName("mm-snippet");

        this.header.setStyleName("mm-snippet-header");
        this.header.add(this.headerText);
        this.panel.add(this.header);

        this.panelTopWidget.setStyleName("mm-snippet-top");
        this.panelTopWidget.setVisible(false);
        this.panel.add(this.panelTopWidget);

        this.body.setStyleName("mm-snippet-body");
        this.panel.add(this.body);

        initWidget(this.panel);
    }

    @Override
    public void setHeading(String headerHtmlText) {
        this.headerText.setHTML(headerHtmlText);
    }

    @Override
    public void setHeaderText(String header) {
        this.headerText.setText(header);
    }

    @Override
    public void setHeader(SafeHtml headerText) {
        this.headerText.setHTML(headerText);
    }

    @Override
    public void setHeaderVisible(boolean visible) {
        this.header.setVisible(visible);
    }

    @Override
    public String getHeading() {
        return this.headerText.getHTML();
    }

    @Override
    public void setTopWidget(Widget widget) {
        if (widget == null) {
            this.panelTopWidget.setVisible(false);
        }
        else {
            this.panelTopWidget.setWidget(widget);
            this.panelTopWidget.setVisible(true);
        }
    }

    @Override
    public Widget getTopWidget() {
        return this.panelTopWidget.getWidget();
    }

    @Override
    public void setBottomWidget(Widget widget) {
        Firebug.error("SnippetPanel.setBottomWidget() not yet implemented!!!");
    }

    @Override
    public void setContentWidget(Widget widget) {
        this.body.setWidget(widget);
    }

    @Override
    public Widget addHeaderTool(String iconClass, HeaderToolCommand command) {
        return addHeaderTool(iconClass, null, command);
    }

    @Override
    public Widget addHeaderTool(String iconClass, String tooltip, final HeaderToolCommand command) {
        final Image image = IconImage.get(iconClass).createImage();
        image.setStyleName("mm-snippet-header-tool");
        if (tooltip != null) {
            Tooltip.addQtip(image, tooltip);
        }
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                command.execute(image);
            }
        });
        getHeaderToolsPanel().add(image);
        return image;
    }

    private HorizontalPanel getHeaderToolsPanel() {
        if (this.headerTools == null) {
            this.headerTools = new HorizontalPanel();
            this.headerTools.setStyleName("mm-snippet-header-tools");
            this.header.insert(this.headerTools, 0);
        }
        return this.headerTools;
    }

    @Override
    public void setToolTooltip(Widget toolWidget, String tooltip) {
        Tooltip.addQtip(toolWidget, tooltip);
    }

    @Override
    public void setToolIcon(Widget toolWidget, String iconClass) {
        IconImage.get(iconClass).applyTo((Image) toolWidget);
    }

    @Override
    public Widget addFirstHeaderIcon(String iconClass) {
        final Image image = IconImage.get(iconClass).createImage();
        getHeaderToolsPanel().insert(image, 0);
        return image;
    }

    @Override
    public void setIconClass(Widget widget, String iconClass) {
        if (!(widget instanceof Image)) {
            Firebug.warn("SnippetPanel.setIconClass() - unhandled widget class: " + widget.getClass().getSimpleName());
            return;
        }
        IconImage.get(iconClass).applyTo((Image) widget);
    }

    @Override
    public boolean layout() {
        return true;
    }

    @Override
    public boolean layout(boolean force) {
        return true;
    }

    @Override
    public void enableDragging(String transferData, String logId) {
        if (this.draggableHandlerRegistration != null) {
            return;
        }
        this.draggableHandlerRegistration = DragDropSupport.makeDraggable(this.panel, this.header, transferData, logId);
    }

    @Override
    public void disableDragging() {
        if (this.draggableHandlerRegistration == null) {
            return;
        }
        this.draggableHandlerRegistration.removeHandler();
        this.draggableHandlerRegistration = null;
    }
}
