package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardSnippetErrorUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityLogUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.ParameterConfigPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.pmxml.ActivityGadgetResult;
import de.marketmaker.iview.pmxml.ActivityTaskDecl;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.SectionDesc;

import java.util.List;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 28.04.15
 */
public class DashboardSnippetView extends SnippetView<DashboardSnippet> implements
        AnalysisDisplay<AnalysisDisplay.Presenter> {
    private final FlowPanel panel = new FlowPanel();

    private AnalysisDisplay.Presenter ac;

    private SpsWidget spsRootWidget = null;

    private Widget reloadWidget;

    protected DashboardSnippetView(DashboardSnippet snippet) {
        super(snippet);
        this.panel.setStyleName("sps-taskView");

        setTitle(getConfiguration().getString("title")); // $NON-NLS$
        if (SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            showDeveloperReloadWidget("called from DashboardSnippetView()");  // $NON-NLS$
        }
        else {
            showReloadWidget();
        }
    }

    @Override
    protected void onContainerAvailable() {
        this.container.setContentWidget(this.panel);
        if (this.spsRootWidget != null) {
            spsRootWidget.setContainerConfig(getConfiguration());
        }
    }

    public void showError(ErrorType error) {
        showWidgets(new Label(error.getDescription()));
    }

    public void showWidgets(Widget... widgets) {
        internalClear();
        for (Widget widget : widgets) {
            this.panel.add(widget);
        }
    }

    @Override
    public void showReportSettings(Map<String, String> params, Widget triggerWidget) {
        new ParameterConfigPopup(snippet.getLayoutDesc(), params, params1 -> {
            snippet.applyParams(params1, true);
            snippet.setParameters(params1);
        }).getPopupPanel().showRelativeTo(triggerWidget);
    }

    @Override
    public void clear() {
        // It is necessary to show the reload widget here, because clear is called by the analysis controller just
        // before starting the evaluation, which causes reload widget to vanish if we do not show it here.
        // Clear is only called in AnalysisController.fetch and AnalysisController.evaluate.
        if (SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            showDeveloperReloadWidget("called from DashboardSnippetView.clear()");  // $NON-NLS$
        }
        else {
            showReloadWidget();
        }
    }

    private void internalClear() {
        this.panel.clear();
    }

    @Override
    public void setPresenter(AnalysisDisplay.Presenter presenter) {
        this.ac = presenter;
    }

    @Override
    public AnalysisDisplay.Presenter getPresenter() {
        return this.ac;
    }

    @Override
    public Widget getContentPanel() {
        return this.panel;
    }

    @Override
    public void showPdf(String reportUrl, LayoutDesc layoutDescEx1, String handle) {
        //nothing to do
    }

    @Override
    public void showChart(String chartUrl, LayoutDesc layoutDescEx1, String handle) {
        //nothing to do
    }

    @Override
    public void showTable(DTTable dtTable, DTTableRenderer.Options dtTableOptions,
            Map<Integer, FilterData> filterData, LayoutDesc layoutDesc, Integer maxDiagramIdx,
            String handle) {
        //nothing to do
    }

    @Override
    public void showGadget(ActivityGadgetResult g, LayoutDesc layoutDesc, String handle) {
        if (g == null) {
            Firebug.warn("pm snippet \"" + getTitle() + "\": EvalLayoutGadgetResponse.GadgetResult is null");
            internalClear();
            this.panel.add(new HTML(DashboardSnippetErrorUtil.getErrorHtml(
                    I18n.I.dashboardWidgetNotAvailableText(),
                    I18n.I.dashboardWidgetNotAvailableTooltipResultIsNull())));
            return;
        }
        final ActivityTaskDecl decl = g.getTaskDecl();
        final DataContainerCompositeNode dataDeclRoot = decl.getTypeDecls().getRoot();
        final DataContainerCompositeNode dataRoot = g.getTaskData().getData().getContainer().getRoot();

        if (SessionData.INSTANCE.isUserPropertyTrue("fbSpsTasks")) { // $NON-NLS$
            ActivityLogUtil.logForm("Snippet", decl.getTaskId(), decl.getFormDesc(), decl.getSubmitCapabilities(), dataDeclRoot, dataRoot, null); // $NON-NLS$
        }

        Firebug.info("shellMMTypes: " + layoutDesc.getShellMMTypes().toString());
        Firebug.info("isNoInput: " + layoutDesc.getLayout().isNoInput());

        final Context spsContext = new Context(dataDeclRoot, null, null, null, null, null, null, null, null);
        spsContext.transferDataToProperties(dataRoot, dataDeclRoot, false);
        final SectionDesc rootSectionDesc = decl.getFormDesc().getRoot();
        if (rootSectionDesc.getCaption() != null) {
            setTitle(rootSectionDesc.getCaption());
            rootSectionDesc.setCaption(null);
        }
        this.spsRootWidget = spsContext.getEngine().createSpsWidget(rootSectionDesc);
        this.spsRootWidget.setContainerConfig(getConfiguration());
        showWidgets(this.spsRootWidget.asWidgets());
        spsContext.replayChangeEvents();
        SpsAfterPropertiesSetEvent.fireAndRemoveHandlers();
    }

    @Override
    public void showError(LayoutDesc layoutDescEx1, String... messages) {
        internalClear();
        final StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message);
        }
        this.panel.add(new Label(sb.toString()));
    }

    @Override
    public Widget asWidget() {
        return this.panel;
    }


    @Override
    public boolean isPrintable() {
        return false;
    }

    @Override
    public String getPrintHtml() {
        return null;
    }

    @Override
    public void setDmsPresenter(DmsDisplay.Presenter dmsPresenter) {
        //nothing to do
    }

    @Override
    public void showArchiveDialog(DmsMetadata metadata) {
        //nothing to do
    }

    @Override
    public void setLanguages(List<Language> languages) {
        //nothing to do
    }

    @Override
    public void setSelectedLanguage(Language language) {
        //nothing to do
    }

    @Override
    public void updateAsyncHandleMarker(String asyncHandle) {
        if (asyncHandle == null) {
            this.container.getElement().removeAttribute(ASYNC_HANDLE_MARKER);
            return;
        }
        this.container.getElement().setAttribute(ASYNC_HANDLE_MARKER, asyncHandle);
    }

    @Override
    public void startProgress(String header, String progressText) {
        showReloadWidget();
    }

    public void showReloadWidget() {
        if (this.reloadWidget == null) {
            this.reloadWidget = createReloadWidget();
        }
        //avoid flickering/starting the loading indicator again and again, as with showWidgets
        if (this.reloadWidget.getParent() != this.panel) {
            this.panel.clear();
            this.panel.add(this.reloadWidget);
        }
    }

    public void showDeveloperReloadWidget(String message) {
        final HTML html = new HTML("Developer Mode Progress Indicator: " + message);  // $NON-NLS$
        SimplePanel widgets = new SimplePanel(html);
        showWidgets(widgets);
    }

    private Widget createReloadWidget() {
        final Image image = IconImage.get("as-reload-48 active").createImage(); // $NON-NLS$
        final SimplePanel panel = new SimplePanel(image);
        final Style style = panel.getElement().getStyle();
        style.setProperty("margin", "32px auto"); // $NON-NLS$
        style.setTextAlign(Style.TextAlign.CENTER);
        return panel;
    }

    @Override
    public void setProgress(String progressText, int progress) {
        //nothing to do
    }

    @Override
    public void hideProgressPanelCancelButton() {
        //nothing to do
    }

    @Override
    public void setProgressPanelBackgroundButtonVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public PagingWidgets getChartPagingWidgets() {
        return null;
    }

    @Override
    public void setLanguageButtonVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public void setChartPagingWidgetsVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public void setAggregationButtonVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public void setSortOrderButtonVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public void setConfigButtonEnabled(boolean enabled) {
        //nothing to do
    }

    @Override
    public void setConfigButtonVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public void setUpdatePrintButtonEnabled(boolean enabled) {
        //nothing to do
    }

    @Override
    public void setXlsExportButtonVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public void hideDmsButtons() {
        //nothing to do
    }

    @Override
    public void setDmsArchivButtonVisible(boolean visible) {
        //nothing to do
    }

    @Override
    public void setDmsPopupButtonVisible(boolean visible) {
        //nothing to do
    }
}
