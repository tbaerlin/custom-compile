/*
 * LiveFinderNewsView.java
 *
 * Created on 9/8/14 9:24 AM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NewsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentViewAdapter;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.COMPACT_DATETIME;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.STRING_RIGHT;

/**
 * @author Stefan Willenbrock
 */
public class LiveFinderNewsView extends AbstractFinderView<LiveFinderNews> implements ContentContainer {

    private ContentView lastContentView;

    LiveFinderNewsView(LiveFinderNews controller) {
        super(controller);
    }

    @Override
    protected int getNumRowsInDataGrid() {
        return 3;
    }

    @Override
    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withMode(PagingWidgets.Mode.FULL);
    }

    @Override
    protected void initColumnModels(TableColumnModel[] columnModels) {
        TableCellRenderer linkRenderer = new TableCellRenderers.MaxLengthStringRenderer(130, "--") { // $NON-NLS-0$
            @Override
            public void render(Object data, StringBuffer sb, Context context) {
                LinkContext lc = (LinkContext) data;
                NWSSearchElement e = (NWSSearchElement) lc.data;
                final String headline = NewsUtil.headlineWithoutAgency(e);
                final String display = getMaxLengthText(headline);
                context.appendLink(lc, display, !display.equals(headline) ? headline : null, sb);
            }
        };

        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.time(), 0.1f, COMPACT_DATETIME).alignRight()
                , new TableColumn(I18n.I.text(), 0.8f, linkRenderer)
                , new TableColumn(I18n.I.agency(), 0.1f, STRING_RIGHT)
        });
    }

    @Override
    public void setContent(ContentView contentView) {
        if (this.lastContentView != null && this.lastContentView != contentView) {
            this.lastContentView.onBeforeHide();
        }
        this.lastContentView = contentView;

        this.g.setWidget(2, 0, contentView.getWidget());
    }

    @Override
    public void setContent(Widget w) {
        setContent(new ContentViewAdapter(w));
    }

    @Override
    public Widget getContent() {
        return this.lastContentView == null ? null : this.lastContentView.getWidget();
    }

    @Override
    public boolean isShowing(Widget w) {
        return this.lastContentView != null && lastContentView.getWidget() == w;
    }
}
