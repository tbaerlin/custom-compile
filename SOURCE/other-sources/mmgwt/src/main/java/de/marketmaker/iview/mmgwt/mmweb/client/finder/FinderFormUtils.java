package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Created on 07.10.11 10:20
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class FinderFormUtils {

    public static boolean shrunkAddTo(Widget innerWidget, FlexTable flexTable, CheckBox cb, int row,
                                      final FinderFormElement element) {
        return shrunkAddTo(innerWidget, flexTable, cb, row, element, null);
    }

    public static boolean shrunkAddTo(Widget innerWidget, FlexTable flexTable, CheckBox cb, int row,
                                      final FinderFormElement element, Widget configBtn) {
        return shrunkAddTo(innerWidget, flexTable, cb, row, element, configBtn, false);
    }

    public static boolean shrunkAddTo(Widget innerWidget, FlexTable flexTable, CheckBox cb, int row,
                                      final FinderFormElement element, Widget configBtn, boolean isHeadlineInWidget) {
        if (!element.isActive()) {
            return false;
        }
        flexTable.setWidget(row, 1, cb);
        if (configBtn != null) {
            flexTable.setWidget(row, 2, configBtn);
        }
        //noinspection GWTStyleCheck
        flexTable.getFlexCellFormatter().setStyleName(row, 1, "mm-finder-element title"); // $NON-NLS-0$
        if (innerWidget != null) {
            if (isHeadlineInWidget) {
                flexTable.setWidget(row, 1, innerWidget);
                flexTable.getFlexCellFormatter().setStyleName(row, 1, "mm-finder-element values"); // $NON-NLS-0$
                flexTable.getFlexCellFormatter().setColSpan(row, 1, 2);
            } else {
                final Widget panel = createFinderElement(innerWidget, flexTable, row, element);
                flexTable.setWidget(row, 1, panel);
            }

            return cb.getValue();
        }
        return false;
    }

    private static Widget createFinderElement(Widget innerWidget, FlexTable flexTable, int row, final FinderFormElement element) {
        VerticalPanel result = new VerticalPanel();
        final Widget headElement = flexTable.getWidget(row, 1);
        if (element instanceof CloneableFinderFormElement) {
            final Panel headLine = new HorizontalPanel();
            headLine.add(headElement);
            final CloneableFinderFormElement cloneable = (CloneableFinderFormElement) element;
            final IconImageIcon iconAdd = IconImage.getIcon("mm-plus") // $NON-NLS$
                    .withClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            cloneable.fireCloneEvent();
                        }
                    });
            iconAdd.addStyleName("mm-middle mm-link");
            headLine.add(iconAdd);

            if (cloneable.isClone()) {
                final IconImageIcon iconDelete = IconImage.getIcon("mm-minus") // $NON-NLS$
                        .withClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                ((CloneableFinderFormElement) element).fireDeletedEvent();
                            }
                        });
                iconDelete.addStyleName("mm-middle mm-link");
                headLine.add(iconDelete);
            }
            result.add(headLine);
        }
        else {
            result.add(headElement);
        }
        innerWidget.addStyleName("mm-live-finder-element values");
        result.add(innerWidget);
        return result;
    }

    public static int getInstanceCounterOfId(String id) {
        if (id.contains("-")) {
            final int idx = id.indexOf("-");
            final String instanceCounter = id.substring(idx + 1, id.length());
            if (id.endsWith(instanceCounter)) {
                return Integer.valueOf(instanceCounter);
            }
        }
        return 0;
    }

    public static String incId(String id) {
        return getOriginalId(id) + "-" + (getInstanceCounterOfId(id) + 1);
    }

    public static String getOriginalId(String id) {
        if (id.contains("-")) {
            final int idx = id.indexOf("-");
            final String tmp = id.substring(idx + 1, id.length());
            if (id.endsWith(tmp)) {
                return id.substring(0, idx);
            }
        }
        return id;
    }

    public static String concatId(FinderFormElement element, int instanceCount) {
        return getOriginalId(element.getId()) + "-" + String.valueOf(instanceCount);
    }

    public static void checkForActivation(FinderFormElement element, DomEvent event) {
        if (!element.getValue()) {
            element.setValue(true);
            element.fireEvent(event);
        }
    }

    public static String handleDefaultQuery(String query, String defaultQuery) {
        return StringUtil.hasText(query)
                ? query + (StringUtil.hasText(defaultQuery) ? "&&" : "") + defaultQuery
                : StringUtil.hasText(defaultQuery) ? defaultQuery : null;
    }
}
