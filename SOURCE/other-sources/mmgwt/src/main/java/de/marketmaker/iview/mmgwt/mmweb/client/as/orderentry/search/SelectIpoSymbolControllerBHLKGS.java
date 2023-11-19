/*
 * SelectIpoSymbolControllerBHLKGS.java
 *
 * Created on 28.01.14 14:51:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.search;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.ListPagerImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolFormControllerInterface;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModelImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;
import de.marketmaker.iview.pmxml.GetIPOListResponse;
import de.marketmaker.iview.pmxml.IPODataType;

import java.util.Collections;
import java.util.List;

/**
 * @author Markus Dick
 */
public class SelectIpoSymbolControllerBHLKGS implements SelectSymbolFormControllerInterface, IndexedViewSelectionModel.Callback, PageLoader {
    private static final int DEFAULT_COUNT = 10;
    private static final String DEFAULT_VIEW_GROUP = "iposearch"; //$NON-NLS$

    private SelectSymbolForm view;
    private final IndexedViewSelectionModelImpl indexedViewSelectionModel;
    private final PagingFeature pagingFeature;
    private final ListPagerImpl<IPODataType> pager;
    private TableDataModel tableDataModel;
    private List<IPODataType> data;

    public SelectIpoSymbolControllerBHLKGS() {
        final ViewSpec[] viewSpec = new ViewSpec[] { new ViewSpec(I18n.I.all()) };
        this.indexedViewSelectionModel = new IndexedViewSelectionModelImpl(this, viewSpec, 0, DEFAULT_VIEW_GROUP);
        this.pager = new ListPagerImpl<IPODataType>();
        this.pagingFeature = new PagingFeature(this, this.pager, DEFAULT_COUNT);
        this.tableDataModel = null;
        this.data = Collections.emptyList();
    }

    public void search(OrderSession orderSession) {
        this.view.disableSearchControls();
        OrderMethods.INSTANCE.getIPOList(orderSession, new AsyncCallback<GetIPOListResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                onSearchResult(null);
            }

            @Override
            public void onSuccess(GetIPOListResponse result) {
               onSearchResult(result.getIPOList());
            }
        });
    }

    @Override
    public void search(String searchString) {
        if(StringUtil.hasText(searchString)) {
            Firebug.warn("<SelectIpoSymbolControllerBHLKGS.search> method not implemented!");
        }
    }

    private void onSearchResult(List<IPODataType> list) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.onSearchResult>");

        updateViewTypes();
        this.pager.setList(list);
        clearViewData();
        createViewData(list);
        updateViewSelectionModel();
        updateBlockListTypePager();
        createModelAndUpdateView();
    }

    private void clearViewData() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.clearViewData>");
        this.data.clear();
    }

    private void createViewData(List<IPODataType> resultList) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.createViewData>");
        this.data = resultList;
    }

    private void updateViewSelectionModel() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.updateViewSelectionModel>");

        final String viewSpecName = I18n.I.all() + " (" + this.data.size() + ")"; //$NON-NLS$
        final ViewSpec[] viewSpecs = new ViewSpec[] { new ViewSpec(viewSpecName) };

        this.indexedViewSelectionModel.update(viewSpecs, 0, DEFAULT_VIEW_GROUP);

        if (this.data.size() == 0) {
            this.indexedViewSelectionModel.setSelectable(0, false);
        }
    }

    private void updateBlockListTypePager() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.updateBlockListTypePager>");

        this.pager.setList(this.data);
        this.pager.setOffset(0);
    }

    private void createTableModel(List<IPODataType> result) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.createTableModel>");
        this.tableDataModel = DefaultTableDataModel.create(result,
                new AbstractRowMapper<IPODataType>() {
                    public Object[] mapRow(IPODataType o) {
                        return new Object[]{
                                true,
                                o.getSecurityName(),
                                o.getISIN(),
                                o.getWKN(),
                        };
                    }
                });
    }

    private void updateView() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.updateView>");

        this.pagingFeature.onResult();

        if (this.tableDataModel != null) {
            this.view.updateViewNames();
            this.view.show(this.tableDataModel);
        }
    }

    private QuoteWithInstrument toQwI(IPODataType o) {
        final InstrumentData instrumentData = new InstrumentData();
        instrumentData.setName(o.getSecurityName());
        instrumentData.setIsin(o.getISIN());
        instrumentData.setWkn(o.getWKN());

        return new QuoteWithInstrument(instrumentData, QuoteWithInstrument.NULL_QUOTE_DATA);
    }

    @Override
    public IndexedViewSelectionModel getIndexedViewSelectionModel() {
        return this.indexedViewSelectionModel;
    }

    @Override
    public void setView(SelectSymbolForm view) {
        this.view = view;
    }

    @Override
    public void setTypes(String[] types) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.updateViewTypes> not implemented! types = " + StringUtil.join(',', types));
    }

    @Override
    public void setWithMsc(boolean withMsc) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.setWithMsc> not implemented!");
    }

    public SelectIpoSymbolControllerBHLKGS withMsc() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.withMsc> not implemented!");
        return this;
    }

    private void updateViewTypes() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.updateViewTypes> not implemented!");
    }

    @Override
    public PagingFeature getPagingFeature() {
        return this.pagingFeature;
    }

    @Override
    public QuoteWithInstrument getResultQwi(int n) {
        final int size = this.pager.getCount();
        if (n < size) {
            return toQwI(this.pager.getSublist().get(n));
        }
        return null;
    }

    public IPODataType getResult(int n) {
        final int size = this.pager.getCount();
        if (n < size && n > -1) {
            return this.pager.getSublist().get(n);
        }
        return null;
    }

    @Override
    public boolean hasData() {
        return this.pager.isResponseOk();
    }

    @Override
    public void cancelPendingRequests() {
        MmwebServiceAsyncProxy.cancelPending();
    }

    @Override
    public void setFilterForUnderlyingsOfLeverageProducts(Boolean filterForUnderlyingsOfLeveragProducts, boolean fireChange) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.setFilterForUnderlyingsForType> " + //$NON-NLS$
                "filterForUnderlyingsOfLeveragProducts='" + filterForUnderlyingsOfLeveragProducts + "' fireChange='" + fireChange + "'"); //$NON-NLS$
    }

    @Override
    public void setFilterForUnderlyingsForType(String filterForUnderlyingsForType) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.setFilterForUnderlyingsForType> " + //$NON-NLS$
                "filterForUnderlyingsForType='" + filterForUnderlyingsForType); //$NON-NLS$
    }

    @Override
    public void setFilterForUnderlyingsForType(String filterForUnderlyingsForType, boolean fireChange) {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.setFilterForUnderlyingsForType> " + //$NON-NLS$
                "filterForUnderlyingsForType='" + filterForUnderlyingsForType + "' fireChange='" + fireChange + "'");//$NON-NLS$
    }

    @Override
    public void reload() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.reload>");
        createModelAndUpdateView();
    }

    private void createModelAndUpdateView() {
        final List<IPODataType> result = this.pager.getSublist();
        createTableModel(result);
        updateView();
    }

    @Override
    public void onViewChanged() {
        Firebug.debug("<SelectIpoSymbolControllerBHLKGS.onViewChanged>");

        this.pager.setList(this.data);
        this.pager.setOffset(0);

        reload();
    }
}
