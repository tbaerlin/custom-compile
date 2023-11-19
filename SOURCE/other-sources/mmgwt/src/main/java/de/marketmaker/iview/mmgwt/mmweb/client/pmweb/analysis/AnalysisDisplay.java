/*
 * AnalysisDisplay.java
 *
 * Created on 22.07.2014 13:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterData;
import de.marketmaker.iview.pmxml.ActivityGadgetResult;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;

import java.util.List;
import java.util.Map;

/**
 * @author mdick
 */
public interface AnalysisDisplay<P extends AnalysisDisplay.Presenter> extends EvaluationController.View, IsWidget {
    void setPresenter(P presenter);

    P getPresenter();

    Widget getContentPanel();

    void hideProgressPanelCancelButton();
    void setProgressPanelBackgroundButtonVisible(boolean visible);

    PagingWidgets getChartPagingWidgets();

    void setLanguageButtonVisible(boolean visible);

    void setChartPagingWidgetsVisible(boolean visible);

    void setAggregationButtonVisible(boolean visible);

    void setSortOrderButtonVisible(boolean visible);

    void setConfigButtonEnabled(boolean enabled);

    void setConfigButtonVisible(boolean visible);

    void setUpdatePrintButtonEnabled(boolean enabled);

    void setXlsExportButtonVisible(boolean visible);

    void hideDmsButtons();

    void setDmsArchivButtonVisible(boolean visible);

    void setDmsPopupButtonVisible(boolean visible);

    void showPdf(String reportUrl, LayoutDesc layoutDescEx1, String handle);

    void showChart(String chartUrl, LayoutDesc layoutDescEx1, String handle);

    void showTable(DTTable dtTable, DTTableRenderer.Options dtTableOptions, Map<Integer,FilterData> filterData, LayoutDesc layoutDesc, Integer maxDiagramIdx, String handle);

    void showGadget(ActivityGadgetResult gadgetResult, LayoutDesc layoutDesc, String handle);

    void showReportSettings(Map<String, String> unmodifiableAnalysisParametersOfMetadataForm, Widget triggerWidget);

    void clear();

    boolean isPrintable();

    String getPrintHtml();

    void setDmsPresenter(DmsDisplay.Presenter dmsPresenter);

    void showArchiveDialog(DmsMetadata metadata);

    void setLanguages(List<Language> languages);

    void setSelectedLanguage(Language language);

    interface Presenter {
        void setAnalysisParametersOfMetadataForm(Map<String, String> analysisParameters);

        void xlsExport(String handle);

        void archive(String layoutName, String handle, String layoutType, String title, String comment, DmsMetadata metadata);

        void cancelAsyncAndCloseSession();

        void sendAsyncToBackground();

        void onShowReportSettings();

        void setFilterData(Map<Integer, FilterData> filterMetadata);

        void saveLayoutAndTableParams();

        Config getConfig();

        void setMaxDiagramIdx(Integer idx);

        void onArchiveButtonClicked();

        void onLanguageSelected(Language language);
    }
}
