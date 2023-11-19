package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

/**
 * Created with IntelliJ IDEA.
 * User: umaurer
 * Date: 22.04.13
 * Time: 15:52
 */
public class ModuleIcon implements IsWidget, HasSelectionHandlers<ModuleIcon> {
    private static ModuleIcon selectedIcon = null;

    private final HTML html;
    private final HandlerManager handlerManager = new HandlerManager(this);
    private Widget navigationWidget;


    public enum SelectionState {
        SELECTED, WAS_SELECTED, UNSELECTED
    }

    public ModuleIcon(String iconClass, String title, final Widget navigationWidget) {
        this.navigationWidget = navigationWidget;
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.append(IconImage.get(iconClass).getSafeHtml());
        sb.appendHtmlConstant("<br/>"); // $NON-NLS$
        sb.appendEscaped(title);
        this.html = new HTML(sb.toSafeHtml());
        this.html.setStyleName("as-icon");
        this.html.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (selectedIcon != null) {
                    selectedIcon.setSelectionState(SelectionState.UNSELECTED);
                }
                selectedIcon = ModuleIcon.this;
                selectedIcon.setSelectionState(SelectionState.SELECTED);
                SelectionEvent.fire(selectedIcon, selectedIcon);
            }
        });
    }

    @Override
    public Widget asWidget() {
        return this.html;
    }

    public Widget getNavigationWidget() {
        return this.navigationWidget;
    }

    public void setSelectionState(SelectionState selectionState) {
        this.html.setStyleName("as-icon");
        switch (selectionState) {
            case SELECTED:
                this.html.addStyleName("selected");
                break;
            case WAS_SELECTED:
                this.html.addStyleName("was-selected");
                break;
        }
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<ModuleIcon> handler) {
        return this.handlerManager.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }
}
