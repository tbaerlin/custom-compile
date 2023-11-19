package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTML;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil.appendAttribute;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HTMLWithLinks;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author umaurer
 */
public class SitemapController extends AbstractPageController {
    private static final String LID_ATTR = LinkManager.LID_ATTR;

    private static final EventListener EVENT_LISTENER = new EventListener() {
        public void onBrowserEvent(Event event) {
            if (DOM.eventGetType(event) != Event.ONCLICK) {
                return;
            }
            final Element e = DOM.eventGetTarget(event);
            PlaceUtil.goTo(e.getAttribute(LID_ATTR));
            event.preventDefault();
        }
    };

    public SitemapController(ContentContainer cc) {
        super(cc);
    }

    private void display(MenuModel model) {
        final ArrayList<MenuModel.Item> listItems = model.getItems();
        if (listItems == null) {
            Firebug.log("keine Menüelemente vorhanden"); // $NON-NLS-0$
            return;
        }
        final StringBuffer sb = new StringBuffer();
        for (MenuModel.Item item : listItems) {
            addItem(sb, item);
        }
        final HTML html = new HTMLWithLinks(sb.toString(), EVENT_LISTENER);
        html.setStyleName("mm-simpleHtmlView"); // $NON-NLS-0$
        getContentContainer().setContent(html);
    }

    private void addItem(StringBuffer sb, MenuModel.Item item) {
        if (item.isHidden()) {
            return;
        }
        final String controllerId = getControllerId(item);
        sb.append("<div class=\"mm-indent\"><a"); // $NON-NLS-0$
        appendAttribute(sb, LID_ATTR, controllerId);
        appendAttribute(sb, "href", "#" + controllerId); // $NON-NLS-0$ $NON-NLS-1$
        sb.append(">").append(item.getName()).append("</a>"); // $NON-NLS-0$ $NON-NLS-1$
        final ArrayList<MenuModel.Item> listItems = item.getItems();
        if (listItems != null) {
            for (MenuModel.Item subItem : listItems) {
                addItem(sb, subItem);
            }
        }
        sb.append("</div>"); // $NON-NLS-0$
    }

    private String getControllerId(MenuModel.Item item) {
        final String controllerId = item.getControllerId();
        return controllerId == null ? item.getId() : controllerId;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final String param1 = event.getHistoryToken().get(1, null);
        if ("json".equals(param1)) { // $NON-NLS$
            displayJson(AbstractMainController.INSTANCE.getMenuModel());
            return;
        }
        display(AbstractMainController.INSTANCE.getMenuModel());
    }
    
    private void displayJson(MenuModel model) {
        final ArrayList<MenuModel.Item> listItems = model.getItems();
        if (listItems == null) {
            Firebug.log("keine Menüelemente vorhanden"); // $NON-NLS-0$
            return;
        }
        final StringBuffer sb = new StringBuffer();
        sb.append("<pre>{\n"); // $NON-NLS-0$
        addItemJson(sb, "    ", listItems); // $NON-NLS-0$
        sb.append("}</pre>"); // $NON-NLS-0$
        final HTML html = new HTML(sb.toString());
        html.setStyleName("mm-simpleHtmlView"); // $NON-NLS-0$
        getContentContainer().setContent(html);
    }

    private void addItemJson(StringBuffer sb, String indent, List<MenuModel.Item> listItems) {
        sb.append(indent);
        sb.append("\"items\":["); // $NON-NLS-0$
        final String subIndent = indent + "    "; // $NON-NLS-0$
        String komma = "\n"; // $NON-NLS-0$
        for (MenuModel.Item item : listItems) {
            sb.append(komma);
            sb.append(subIndent).append("{"); // $NON-NLS-0$
            sb.append("\"id\":\"").append(item.getId()).append("\", "); // $NON-NLS-0$ $NON-NLS-1$
            if (item.getControllerId() != null) {
                sb.append("\"controllerId\":\"").append(item.getControllerId()).append("\", "); // $NON-NLS-0$ $NON-NLS-1$
            }
            sb.append("\"name\":\"").append(item.getName()).append("\""); // $NON-NLS-0$ $NON-NLS-1$
            final String iconStyle = item.getIconStyle();
            if (iconStyle != null) {
                sb.append(", \"icon\":\"").append(iconStyle).append("\""); // $NON-NLS-0$ $NON-NLS-1$
            }
            if (item.isHidden()) {
                sb.append(", \"hidden\":\"true\""); // $NON-NLS-0$
            }
            final ArrayList<MenuModel.Item> listSubItems = item.getItems();
            if (listSubItems != null) {
                sb.append(",\n"); // $NON-NLS-0$
                addItemJson(sb, subIndent + "    ", listSubItems); // $NON-NLS-0$
                sb.append(subIndent);
            }
            sb.append("}"); // $NON-NLS-0$
            komma = ",\n"; // $NON-NLS-0$
        }
        sb.append("\n").append(indent).append("]\n"); // $NON-NLS-0$ $NON-NLS-1$
    }
}
