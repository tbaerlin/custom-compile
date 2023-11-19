/*
 * CerComparisonController.java
 *
 * Created on 07.09.2010 11:06:20
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison;

import java.util.HashMap;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data.CerCompareChartData;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data.CerComparisonData;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data.CerDataSection;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.events.UpdateViewEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.events.UpdateViewHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.model.CerColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.model.CerTableModel;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Michael Lösch
 */
public class CerComparisonController extends AbstractPageController implements UpdateViewHandler {
    private final CerComparisonView view;
    private CerTableModel ctm;
    private SafeHtml origContentHeader;
    public final static String TOKEN = "M_CER_COMP"; // $NON-NLS$
    public final static CerComparisonController INSTANCE = new CerComparisonController();
    private final CerCompareChartData compareChart;


    private CerComparisonController() {
        super();
        this.compareChart = new CerCompareChartData(this.context);
        this.ctm = new CerTableModel(this.compareChart);
        this.view = new CerComparisonView(this, this.ctm);
        EventBusRegistry.get().addHandler(UpdateViewEvent.getType(), this);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final ContentContainer container = this.getContentContainer();
        if (container != null) {
            container.setContent(this.view);
            this.origContentHeader = AbstractMainController.INSTANCE.getContentHeader(null);
        }
    }

    private void initTableModel(CerComparisonData ccd) {
        if (this.ctm.getColumnCount() == 0) {
            final CerDataSection[] sections = CerDataSection.values();
            final HashMap<CerDataSection, String[]> mapSectionTitles = new HashMap<>(sections.length);
            for (CerDataSection section : sections) {
                mapSectionTitles.put(section, ccd.getFieldNamesOfSection(section));
            }
            this.ctm.setSectionTitles(mapSectionTitles);
        }
    }


    void add(final QuoteWithInstrument quote, CERDetailedStaticData data) {
        final String typeKey = data.getTypeKey();
        if (this.ctm.getColumnCount() == 0) {
            AbstractMainController.INSTANCE.getView().setContentHeader(StringUtil.asHeader(this.origContentHeader, data.getSubtype()));
        }
        final String qid = quote.getQuoteData().getQid();
        this.compareChart.add(qid);
        final CerComparisonData compData = new CerComparisonData(this.context, qid, typeKey);

        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(ResponseType result) {
                initTableModel(compData);

                final CerDataSection[] sections = CerDataSection.values();
                final HashMap<CerDataSection, CellData[]> mapCellDatas = new HashMap<>(sections.length);
                for (CerDataSection section : sections) {
                    mapCellDatas.put(section, compData.getFieldsOfSection(section));
                }
                ctm.add(new CerColumnModel(mapCellDatas), typeKey);
            }
        });
    }

    public void onUpdateView(UpdateViewEvent event) {
        if (this.ctm.getColumnCount() == 0) {
            AbstractMainController.INSTANCE.getView().setContentHeader(this.origContentHeader);
        }
    }

    String getTypeKey() {
        return this.ctm.getTypeKey();
    }

    public void setQuote(QuoteWithInstrument qwi) {
        this.view.setQuote(qwi);
    }

    @Override
    public String getPrintHtml() {
        return this.view.getPrintHtml();
    }
}