/*
 * PagingPanel.java
 *
 * Created on 11.06.2008 13:00:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoadedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PagingPanel implements PagingEvent.Callback {

    public interface Handler {
        void ackNewOffset(int offset);
    }

    protected PagingWidgets pagingWidgets;

    private int pageSize;

    private int currentPage = 0;

    private int numPages = 0;

    private Handler handler;

    public static class Config {
        private PagingWidgets.Config widgetsConfig = new PagingWidgets.Config();

        private final ContentPanelIfc panel;

        private int pageSize = 10;

        private boolean pagingOnTop = true;

        public Config(ContentPanelIfc panel) {
            this.panel = panel;
        }

        public Config withWidgetsConfig(PagingWidgets.Config widgetsConfig) {
            this.widgetsConfig = widgetsConfig;
            return this;
        }

        public Config withPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Config withPagingOnTop(boolean pagingOnTop) {
            this.pagingOnTop = pagingOnTop;
            return this;
        }
    }

    public PagingPanel(PagingPanel.Config config) {
        this.pageSize = config.pageSize;
        this.pagingWidgets = new PagingWidgets(config.widgetsConfig);
        this.pagingWidgets.setPagingEventHandler(this);

        if (config.pagingOnTop) {
            config.panel.setTopWidget(this.pagingWidgets.getToolbar());
        }
        else {
            config.panel.setBottomWidget(this.pagingWidgets.getToolbar());
        }
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        handle(new PagingEvent(PagingEvent.Action.REFRESH));
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void update(int offset, int count, int total) {
        if (total == 0) {
            this.currentPage = 0;
            this.numPages = 0;
        }
        else {
            this.currentPage = offset / this.pageSize;
            this.numPages = (total + this.pageSize - 1) / this.pageSize;
        }
        this.pagingWidgets.handleEvent(new PageLoadedEvent(this.currentPage, this.numPages, offset, count, total, this.pageSize));
    }

    public void handle(PagingEvent event) {
        if (this.handler != null) {
            final int offset = computeNewOffset(event);
            this.handler.ackNewOffset(offset);
        }
    }

    private int computeNewOffset(PagingEvent event) {
        return computeNewPage(event) * this.pageSize;
    }

    private int computeNewPage(PagingEvent event) {
        switch (event.getAction()) {
            case FIRST:
                return 0;
            case PREVIOUS:
                return Math.max(0, this.currentPage - 1);
            case SPECIFIED:
                return event.getPage();
            case NEXT:
                return Math.min(this.currentPage + 1, this.numPages - 1);
            case LAST:
                return (this.numPages - 1);
            case REFRESH:
                return this.currentPage;
            default:
                throw new IllegalArgumentException("unknown PagingEvent type: " + event.getAction()); // $NON-NLS-0$
        }
    }

    public void setVisible(boolean visible) {
        this.pagingWidgets.getToolbar().setVisible(visible);
    }

}
