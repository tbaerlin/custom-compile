/*
 * PagingFeature.java
 *
 * Created on 16.07.2008 12:19:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.paging;

import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * A mediator between a widget ({@link PagingWidgets}) and a data source ({@link Pager}).
 * Responsible for notifying widget when new data come and forwarding paging events
 * from UI to the data source.
 * @author Ulrich Maurer
 */
public class PagingFeature implements PagedDataSupport, PagingEvent.Callback {
    private final PageLoader pageLoader;

    private Pager pager;

    private int pageSize;

    private int currentPage = -1;

    private int numPages = 0;

    private int resultOffset = -1;

    private int resultCount = 0;

    private int resultTotal = 0;

    private PagingWidgets widgets;

    public PagingFeature(PageLoader pageLoader, Pager pager, int pageSize) {
        this.pageLoader = pageLoader;
        this.pageSize = pageSize;
        this.pager = pager;
        this.pager.setCount(pageSize);
    }

    public PagingFeature(PageLoader pageLoader, DmxmlContext.Block<? extends BlockListType> block,
            int pageSize) {
        this(pageLoader, new DefaultBlockListTypePagerImpl(block), pageSize);
        if (block != null) {
            resetPaging();
        }
    }

    public void resetPaging() {
        this.pager.setOffset(0);
        this.pager.setCount(this.pageSize);
    }

    public void setPagingWidgets(PagingWidgets widgets) {
        this.widgets = widgets;
        this.widgets.setPagingEventHandler(this);
        firePageLoadEvent();
    }

    public void setBlock(DmxmlContext.Block<? extends BlockListType> block) {
        final boolean blockWasNull = (this.pager == null);
        this.pager = new DefaultBlockListTypePagerImpl(block);
        if (blockWasNull) {
            resetPaging();
        }
    }

    public void setPager(Pager pager) {
        final boolean blockWasNull = (this.pager == null);
        this.pager = pager;
        if (blockWasNull) {
            resetPaging();
        }
    }

    public void onResult() {
        if (!this.pager.isResponseOk()) {
            resetValues();
        }
        else {
            this.resultOffset = this.pager.getOffset();
            this.resultCount = this.pager.getCount();
            this.resultTotal = this.pager.getTotal();
            this.currentPage = this.resultOffset / this.pageSize;
            this.numPages = (this.resultTotal + this.pageSize - 1) / this.pageSize;
//            DebugUtil.logToFirebugConsole("cp = " + this.currentPage + ", num = " + this.numPages
//                + " offset = " + result.getOffset() + ", total = " + result.getTotal());
        }

        firePageLoadEvent();
    }

    public void forceNullPage() {
        resetValues();
        firePageLoadEvent();
    }

    private void resetValues() {
        this.resultOffset = -1;
        this.resultCount = 0;
        this.resultTotal = 0;
        this.currentPage = -1;
        this.numPages = 0;
    }

    private void firePageLoadEvent() {
        if (this.widgets != null) {
            this.widgets.handleEvent(new PageLoadedEvent(this.currentPage, this.numPages, this.resultOffset, this.resultCount, this.resultTotal, this.pageSize));
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void gotoFirstPage() {
        if (this.numPages > 1) {
            gotoPage(0);
        }
    }

    public void gotoNextPage() {
        if (this.currentPage + 1 < this.numPages) {
            gotoPage(this.currentPage + 1);
        }
    }

    public void gotoPage(int page) {
        if (page >= 0 && page < this.numPages) {
            final int offset = page * this.pageSize;
            pager.setOffset(offset);
            this.pageLoader.reload();
        }
    }

    public void gotoPreviousPage() {
        if (this.currentPage >= 1) {
            gotoPage(this.currentPage - 1);
        }
    }

    public void gotoLastPage() {
        if (this.numPages > 1) {
            gotoPage(this.numPages - 1);
        }
    }

    public void handle(PagingEvent event) {
        switch (event.getAction()) {
            case FIRST:
                gotoFirstPage();
                break;
            case PREVIOUS:
                gotoPreviousPage();
                break;
            case SPECIFIED:
                gotoPage(event.getPage());
                break;
            case NEXT:
                gotoNextPage();
                break;
            case LAST:
                gotoLastPage();
                break;
            case REFRESH:
                this.pageLoader.reload();
                break;
        }
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.pager.setCount(this.pageSize);
        this.pageLoader.reload();
    }

    /**
     * Return the current page and the number of pages as String.
     * @return the current page and the number of pages as String.
     */
    public String getPageString() {
        return I18n.I.page() + (this.currentPage + 1) + I18n.I.from() + this.numPages;
    }

    /**
     * It is an adapter interface used by {@link PagingFeature} to communicate with a data source.
     * Different data sources have corresponding adapters (for example, {@link de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.ListPagerImpl}
     * and {@link DefaultBlockListTypePagerImpl}).
     */
    public static interface Pager {
        void setOffset(int offset);

        void setCount(int count);

        int getTotal();

        int getOffset();

        int getCount();

        boolean isResponseOk();
    }

    public static class DefaultBlockListTypePagerImpl implements Pager {
        private static final String KEY_OFFSET = "offset"; // $NON-NLS$

        private static final String KEY_COUNT = "count"; // $NON-NLS$

        private final DmxmlContext.Block<? extends BlockListType> block;

        public DefaultBlockListTypePagerImpl(DmxmlContext.Block<? extends BlockListType> block) {
            this.block = block;
        }

        @Override
        public void setOffset(int offset) {
            if (this.block != null) {
                this.block.setParameter(KEY_OFFSET, String.valueOf(offset));
            }
        }

        @Override
        public void setCount(int count) {
            if (this.block != null) {
                this.block.setParameter(KEY_COUNT, String.valueOf(count));
            }
        }

        @Override
        public int getTotal() {
            return Integer.parseInt(this.block.getResult().getTotal());
        }

        @Override
        public int getOffset() {
            return Integer.parseInt(this.block.getResult().getOffset());
        }

        @Override
        public int getCount() {
            return Integer.parseInt(this.block.getResult().getCount());
        }

        @Override
        public boolean isResponseOk() {
            return this.block.isResponseOk() && this.block.getResult() != null;
        }
    }
}
