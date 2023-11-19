package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 28.04.15
 */
public class DashboardSnippet extends AbstractSnippet<DashboardSnippet, DashboardSnippetView> implements ObjectIdSnippet {
    public static class Class extends SnippetClass {
        private final LayoutDesc layoutDesc;

        public Class(LayoutDesc layoutDesc) {
            super("Dashboard-" + layoutDesc.getLayout().getGuid(), layoutDesc.getLayout().getLayoutName()); // $NON-NLS$
            this.layoutDesc = layoutDesc;
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            Firebug.debug("newSnippet(" + getSnippetClassName() + ")");
            return new DashboardSnippet(context, config, this.layoutDesc);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            super.addDefaultParameters(config);
            config.put("needsInputObject", !this.layoutDesc.getLayout().isNoInput() && !this.layoutDesc.getShellMMTypes().isEmpty()); // $NON-NLS$
        }
    }

    private final LayoutDesc layoutDesc;
    private final AnalysisController ac;

    public DashboardSnippet(DmxmlContext context, final SnippetConfiguration config, final LayoutDesc layoutDesc) {
        super(context, config);
        setView(new DashboardSnippetView(this));
        this.ac = new AnalysisController(getView());
        this.layoutDesc = layoutDesc;
        if (!needsInputObject()) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    ac.evaluate(
                            Config.createGlobal(getHistoryToken(), layoutDesc.getLayout().getGuid(), null, PrivacyMode.isActive()),
                            config.getCopyOfParameters()
                    );
                }
            });
        }
    }

    private boolean needsInputObject() {
        return !this.layoutDesc.getLayout().isNoInput() && !this.layoutDesc.getShellMMTypes().isEmpty();
    }

    public void setObjectId(String dbId) {
        this.ac.evaluate(Config.createWithDatabaseId(getHistoryToken(), dbId, this.layoutDesc.getLayout().getGuid(),
                null, PrivacyMode.isActive()), getConfiguration().getCopyOfParameters());
    }

    private HistoryToken getHistoryToken() {
        return MainController.INSTANCE.getHistoryThreadManager().getActiveThreadHistoryItem().getPlaceChangeEvent().getHistoryToken();
    }

    @Override
    public void destroy() {
        Firebug.debug("<DashboardSnippet.destroy> layout " + (this.layoutDesc != null ? this.layoutDesc.getLayout().getGuid() : "layout is null"));
        this.ac.deactivate();
        this.ac.destroy();
    }

    @Override
    public boolean isConfigurable() {
        return this.layoutDesc.isParametersGiven() && !this.layoutDesc.getParameters().isEmpty();
    }

    @Override
    public void configure(Widget triggerWidget) {
        getView().showReportSettings(getCopyOfParameters(), triggerWidget);
    }

    void applyParams(HashMap<String, String> layoutParameters, boolean reload) {
        Firebug.logAsGroup("applyParams", layoutParameters);
        final Map<String, Parameter> paramsMap = MmTalkHelper.toParameterMap(layoutParameters, this.layoutDesc);
        this.ac.applyAnalysisParameters(paramsMap, this.layoutDesc);
        if (reload) {
            this.ac.reevaluate();
        }
    }

    @Override
    public void updateView() {
        // no impl. necessary here, because everything initially necessary is directly done in the view.
    }

    public LayoutDesc getLayoutDesc() {
        return this.layoutDesc;
    }
}