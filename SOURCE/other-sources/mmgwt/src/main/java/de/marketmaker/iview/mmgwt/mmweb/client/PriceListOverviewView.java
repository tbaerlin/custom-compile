/*
 * PriceSearchView.java
 *
 * Created on 19.03.2008 16:43:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import de.marketmaker.iview.dmxml.ListOverviewColumn;
import de.marketmaker.iview.dmxml.ListOverviewFinderItem;
import de.marketmaker.iview.dmxml.ListOverviewItem;
import de.marketmaker.iview.dmxml.ListOverviewItemList;
import de.marketmaker.iview.dmxml.ListOverviewLinkItem;
import de.marketmaker.iview.dmxml.ListOverviewListItem;
import de.marketmaker.iview.dmxml.ListOverviewMultiListItem;
import de.marketmaker.iview.dmxml.ListOverviewPageItem;
import de.marketmaker.iview.dmxml.ListOverviewSection;
import de.marketmaker.iview.dmxml.ListOverviewTitleItem;
import de.marketmaker.iview.dmxml.ListOverviewType;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.PageType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HTMLWithLinks;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil.appendAttribute;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceListOverviewView extends Composite {
    private final PriceListOverviewController controller;
    private final LinkManager linkManager = new LinkManager();

    /**
     * use a SimplePanel so that our html receives the onLoad/onUnload events for every update
     * and the event listener gets un-/registered correctly
     */
    private final ScrollPanel content = new ScrollPanel();

    public PriceListOverviewView(PriceListOverviewController controller) {
        initWidget(this.content);
        this.content.addStyleName("mm-contentData"); // $NON-NLS-0$
        this.controller = controller;
    }


    public void show(ListOverviewType result) {
        this.linkManager.clear();

        final StringBuffer sb = new StringBuffer();
        sb.append("<table"); // $NON-NLS$
        appendAttribute(sb, "class", "mm-listOverview"); // $NON-NLS$
        appendAttribute(sb, "cellspacing", "10"); // $NON-NLS$
        appendAttribute(sb, "cellpadding", "2"); // $NON-NLS$
        sb.append("><tr>"); // $NON-NLS$
        final int columnWidth = 100 / result.getColumn().size();
        for (final ListOverviewColumn column : result.getColumn()) {
            sb.append("<td width=\"").append(columnWidth).append("%\">"); // $NON-NLS$
            for (final ListOverviewSection section : column.getSection()) {
                final String namePath1 = section.getName();
                sb.append("<div class=\"section\">").append(section.getName()).append("</div>"); // $NON-NLS$
                boolean inTable = false;
                for (final ListOverviewItem item : section.getItem()) {
                    if (item.getFeatureFlag() != null) {
                        if (!FeatureFlags.isEnabled(item.getFeatureFlag())) {
                            continue;
                        }
                    }

                    final String namePath2 = namePath1 + "." + item.getName();
                    if (item instanceof ListOverviewItemList) {
                        if (!inTable) {
                            sb.append("<table class=\"table\" cellspacing=\"0\" cellpadding=\"0\">"); // $NON-NLS$
                            inTable = true;
                        }
                        final ListOverviewItemList itemList = (ListOverviewItemList) item;
                        sb.append("<tr>"); // $NON-NLS-0$
                        sb.append("<td class=\"itemname\">").append(itemList.getName()).append("</td>"); // $NON-NLS$
                        for (ListOverviewItem subItem : itemList.getItem()) {
                            final String namePath3 = namePath2 + "." + subItem.getName();
                            appendLink(sb, "td", subItem, namePath3); // $NON-NLS$
                        }
                        sb.append("</tr>"); // $NON-NLS$
                    }
                    else {
                        if (inTable) {
                            sb.append("</table>"); // $NON-NLS$
                            inTable = false;
                        }

                        if (item instanceof ListOverviewTitleItem) {
                            final ListOverviewTitleItem titleItem = (ListOverviewTitleItem) item;
                            sb.append("<div class=\"title\">").append(titleItem.getName()).append("</div>"); // $NON-NLS$
                        }
                        else {
                            appendLink(sb, "div", item, namePath2); // $NON-NLS$
                        }
                    }

                }
                if (inTable) {
                    sb.append("</table>"); // $NON-NLS$
                }
                sb.append("<div class=\"space\">&nbsp;</div>"); // $NON-NLS$
            }
            sb.append("</td>"); // $NON-NLS$
        }
        sb.append("</tr></table>"); // $NON-NLS$


        final HTML html = new HTMLWithLinks(sb.toString(), this.linkManager);
        html.setStyleName("mm-listOverview"); // $NON-NLS$
        this.content.setWidget(html);

        this.controller.getContentContainer().setContent(this);
    }

    private void appendLink(StringBuffer sb, String tag, ListOverviewItem item, String namePath) {
        final String token = getHistoryToken(item, namePath);
        if (token == null) {
            return;
        }
        sb.append("<").append(tag).append(">"); // $NON-NLS$
        this.linkManager.appendLink(token, item.getName(), null, sb);
        sb.append("</").append(tag).append(">"); // $NON-NLS$
    }

    private String getHistoryToken(ListOverviewItem listItem, String namePath) {
        if (listItem instanceof ListOverviewFinderItem) {
            final ListOverviewFinderItem item = (ListOverviewFinderItem) listItem;
            return "M_LF_" + item.getType() + StringUtil.TOKEN_DIVIDER + item.getQuery(); // $NON-NLS$
        }
        else if (listItem instanceof ListOverviewPageItem) {
            ListOverviewPageItem item = (ListOverviewPageItem) listItem;
            if (item.getDzPage() != null) {
                return PageType.DZBANK.getControllerName() + StringUtil.TOKEN_DIVIDER + item.getDzPage();
            }
            else {
                return PageType.VWD.getControllerName() + StringUtil.TOKEN_DIVIDER + item.getVwdPage();
            }
        }
        else if (listItem instanceof ListOverviewLinkItem) {
            ListOverviewLinkItem item = (ListOverviewLinkItem) listItem;
            return item.getToken();
        }
        else if (listItem instanceof ListOverviewListItem) {
            ListOverviewListItem item = (ListOverviewListItem) listItem;
            return "M_UB_KP" + StringUtil.TOKEN_DIVIDER + item.getId() + StringUtil.TOKEN_DIVIDER + namePath; // $NON-NLS$
        }
        else if (listItem instanceof ListOverviewMultiListItem) {
            ListOverviewMultiListItem item = (ListOverviewMultiListItem) listItem;
            return "M_UB_MPL" + StringUtil.TOKEN_DIVIDER + item.getList(); // $NON-NLS$
        }
        return "";
    }
}
