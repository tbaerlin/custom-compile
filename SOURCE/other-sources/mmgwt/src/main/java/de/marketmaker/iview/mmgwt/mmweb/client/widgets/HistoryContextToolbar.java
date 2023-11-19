package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.*;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSelectButton;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;

import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 07.01.13
 */
public class HistoryContextToolbar implements IsWidget {
    private final int DATA_SET_LIMIT = 200;
    private final FlexTable table;

    public static HistoryContextToolbar create(final HistoryThreadManager threadManager, HistoryContext context) {
        final HistoryContextToolbar ct = new HistoryContextToolbar(threadManager, context);
        if (context instanceof ItemListContext) {
            ct.createSymbolListControls((ItemListContext) context);
        }
        return ct;
    }

    private HistoryContextToolbar(final HistoryThreadManager threadManager, HistoryContext context) {
        this.table = new FlexTable();
        this.table.setCellPadding(0);
        this.table.setCellSpacing(0);
        this.table.setStyleName("mm-toolbar as-navToolbar");
        this.table.setWidth("100%"); // $NON-NLS$
        this.table.setWidget(0, 0, createBreadCrumbWidget(threadManager, context));
        final FlexTable.FlexCellFormatter formatter = this.table.getFlexCellFormatter();
        formatter.setWidth(0, 0, "164px"); // 200 - 4 pixel table padding - 2x16 pixel buttons // $NON-NLS$
        formatter.setRowSpan(0, 0, 2);
        formatter.setStyleName(0, 0, "as-navTool");
    }

    private Widget createBreadCrumbWidget(final HistoryThreadManager threadManager, HistoryContext context) {
        final String iconKey = context.getIconKey();
        final Label label;
        if (iconKey != null) {
            final SafeHtml iconHtml = IconImage.get(iconKey).getSafeHtml();
            label = new HTML(new SafeHtmlBuilder().append(iconHtml).appendHtmlConstant("&nbsp;").appendEscaped(context.getName()).toSafeHtml());
        }
        else {
            label = new Label(context.getName());
        }
        makeFocusable(label);
        Tooltip.addAutoCompletion(label).withStyle("as-breadCrumb-tooltip mm-linkHover"); // $NON-NLS$
        final SimplePanel panel = new SimplePanel(label);
        panel.sinkEvents(Event.ONCLICK);
        panel.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                threadManager.backToBreadCrumb();
            }
        }, ClickEvent.getType());
        panel.setStyleName("as-breadCrumb");
        return panel;
    }

    private static void makeFocusable(final Label label) {
        de.marketmaker.itools.gwtutil.client.util.WidgetUtil.makeFocusable(label, new DefaultFocusKeyHandler() {
            @Override
            public boolean onFocusKeyClick() {
                de.marketmaker.itools.gwtutil.client.util.WidgetUtil.click(label);
                return true;
            }
        });
    }

    @SuppressWarnings(value = "unchecked")
    private void createSymbolListControls(final ItemListContext context) {
        final FlexTable.FlexCellFormatter formatter = this.table.getFlexCellFormatter();
        final List<? extends ContextItem> list = getFilteredList(context);

        final Menu menu = new Menu();
        MenuItem selectedItem = null;
        int selectedItemId = 0;
        for (int i = 0; i < list.size(); i++) {
            final ContextItem item = list.get(i);
            final MenuItem menuItem = new MenuItem(item.getName(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    context.getValue().setSelected(item);
                    context.action();
                }
            });
            menu.add(menuItem);
            if (item == context.getValue().getSelected()) {
                selectedItem = menuItem;
                selectedItemId = i;
            }
        }
        final ImageSelectButton buttonPopup = new ImageSelectButton(IconImage.get("list-icon").createImage(), null, null).withMenu(menu); // $NON-NLS$
        if (selectedItem != null) {
            buttonPopup.setSelectedItem(selectedItem);
        }
        this.table.setWidget(0, 1, buttonPopup);
        formatter.setRowSpan(0, 1, 2);
        formatter.setWidth(0, 1, "16px"); // $NON-NLS$
        formatter.setStyleName(0, 1, "as-navTool");

        final ContextItem previousItem = selectedItemId == 0 ? null : list.get(selectedItemId - 1);
        final ImageButton buttonUp = GuiUtil.createImageButton("list-up-icon", null, null, previousItem == null ? null : previousItem.getName()); // $NON-NLS$
        if (previousItem == null) {
            buttonUp.setEnabled(false);
        }
        else {
            buttonUp.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    context.getValue().setSelected(previousItem);
                    context.action();
                }
            });
        }
        this.table.setWidget(0, 2, buttonUp);
        formatter.setWidth(0, 2, "16px"); // $NON-NLS$
        formatter.getElement(0, 2).getStyle().setVerticalAlign(Style.VerticalAlign.BOTTOM);
        formatter.setStyleName(0, 2, "as-navTool");

        final ContextItem nextItem = selectedItemId + 1 >= list.size() ? null : list.get(selectedItemId + 1);
        final ImageButton buttonDown = GuiUtil.createImageButton("list-down-icon", null, null, nextItem == null ? null : nextItem.getName()); // $NON-NLS$
        if (nextItem == null) {
            buttonDown.setEnabled(false);
        }
        else {
            buttonDown.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    context.getValue().setSelected(nextItem);
                    context.action();
                }
            });
        }
        this.table.setWidget(1, 0, buttonDown);
        formatter.setWidth(1, 0, "16px"); // $NON-NLS$
        formatter.getElement(1, 0).getStyle().setVerticalAlign(Style.VerticalAlign.TOP);
        formatter.setStyleName(1, 0, "as-navTool");
    }

    /**
     * @param context
     * @return a window around the the selected holder/list-item
     */
    private List<? extends ContextItem> getFilteredList(ItemListContext<? extends ContextItem>  context){
        final List<? extends ContextItem> list = context.getValue().getContextItems();
        final ContextItem selected = context.getValue().getSelected();
        if (list.size() <= DATA_SET_LIMIT){
            return list;
        }
        final int indexOfSelectedItem = list.indexOf(selected);
        int fromIndex = indexOfSelectedItem - (DATA_SET_LIMIT / 2);
        if (fromIndex < 0){
            fromIndex = 0;
        }

        int toIndex = fromIndex + DATA_SET_LIMIT;
        if (toIndex > list.size()){
            fromIndex = list.size() - DATA_SET_LIMIT;
            toIndex = list.size();
        }
        return list.subList(fromIndex, toIndex);

    }

    @Override
    public Widget asWidget() {
        return this.table;
    }
}