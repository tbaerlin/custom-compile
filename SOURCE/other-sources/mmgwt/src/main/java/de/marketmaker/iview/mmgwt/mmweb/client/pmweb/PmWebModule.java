package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsSimpleHtmlController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.PmAsyncManager;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AccountPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.DepotPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorConfigController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.PersonPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.PortfolioPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.ProspectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer.ExplorerController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.PmSearchController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityNavPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.workspace.GlobalAnalysisPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author umaurer
 */
public class PmWebModule {
    public static final String HISTORY_TOKEN_REPORT = "PM_R"; // $NON-NLS-0$
    public static final String HISTORY_TOKEN_INVESTOR_CONFIG = "PM_IC"; // $NON-NLS-0$
    public static final String HISTORY_TOKEN_INVESTOR_PROFILE = "PM_IP"; // $NON-NLS$
    public static final String HISTORY_TOKEN_ACCOUNT = "PM_A"; // $NON-NLS$
    public static final String HISTORY_TOKEN_PORTFOLIO = "PM_P"; // $NON-NLS$
    public static final String HISTORY_TOKEN_PROSPECT = "PM_PCT"; // $NON-NLS$
    public static final String HISTORY_TOKEN_PERSON = "PM_PN"; // $NON-NLS$
    public static final String HISTORY_TOKEN_DEPOT = "PM_D"; // $NON-NLS$

    public static final String HISTORY_TOKEN_USER_DEFINED_FIELDS = "PM_UF"; // $NON-NLS$

    public static final String HISTORY_TOKEN_SEARCH_DEPOT = "PM_S_DP"; // $NON-NLS$
    public static final String HISTORY_TOKEN_SEARCH_INSTRUMENT = "PM_S_IS"; // $NON-NLS$

    public static final String HISTORY_TOKEN_CUSTOM_SECURITY = "P_PM"; // $NON-NLS$
    public static final String HISTORY_TOKEN_CUSTOM_SECURITY_OVERVIEW = "PM"; // $NON-NLS$
    public static final String TOKEN_NAME_CUSTOM_SECURITY_TYPE = "st"; // $NON-NLS$
    // used in the same way as '.iid' in mmf
    public static final String PM_SECURITY_ID_SUFFIX = ".sid"; // $NON-NLS$

    public static final String HISTORY_TOKEN_EXPLORER = "PM_EX"; // $NON-NLS$

    public static final String HISTORY_TOKEN_GLOBAL_ANALYSIS = "PM_GA"; // $NON-NLS$

    public static final String HISTORY_TOKEN_ACTIVITY = "ACT"; // $NON-NLS$

    public static final String HISTORY_TOKEN_CREATE_PROSPECT = "PPC"; // $NON-NLS$

    public static Command createLoader() {
        return new AsyncModuleCommand();
    }

    static class AsyncModuleCommand implements Command, AsyncCallback<ResponseType> {
        public void execute() {
            if (!SessionData.isWithPmBackend()) {
                finish();
                return;
            }

            AbstractMainController.INSTANCE.updateProgress(I18n.I.loadPmWebModule());
            GWT.runAsync(new RunAsyncCallback() {
                public void onFailure(Throwable err) {
                    Firebug.warn("load pmweb failed", err);
                    finish();
                }

                public void onSuccess() {
                    if (!PmWebSupport.createInstance(AsyncModuleCommand.this)) {
                        finish();
                    }
                }
            });
        }

        public void onFailure(Throwable caught) {
            finish();
        }

        public void onSuccess(ResponseType result) { // result is null, only specified to implement interface
            //TODO: REMOVE PmReportController! ONLY NEEDED FOR JIWAY (zone: pmreport) RIGHT NOW!
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_REPORT, new PmReportController());

            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_INVESTOR_CONFIG, new InvestorConfigController());

            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_INVESTOR_PROFILE, new InvestorPortraitController());
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_PROSPECT, new ProspectPortraitController());
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_PORTFOLIO, new PortfolioPortraitController());
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_DEPOT, new DepotPortraitController());
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_ACCOUNT, new AccountPortraitController());
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_PERSON, new PersonPortraitController());

            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_SEARCH_DEPOT, PmSearchController.createPmDepotSearchController());
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_SEARCH_INSTRUMENT, PmSearchController.createPmInstrumentSearchController());

            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_EXPLORER, ExplorerController.INSTANCE);
            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_GLOBAL_ANALYSIS, new GlobalAnalysisPageController());

            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_CUSTOM_SECURITY, new SecurityPortraitController(AbstractMainController.INSTANCE.getView()));

            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_ACTIVITY, new ActivityNavPageController());

            AbstractMainController.INSTANCE.addController(HISTORY_TOKEN_CREATE_PROSPECT, new CreateProspectPageController(AbstractMainController.INSTANCE.getView()));

            final ContentContainer cc = AbstractMainController.INSTANCE.getView();
            AbstractMainController.INSTANCE.addControllerCheckJson(true, "H_CS", AsSimpleHtmlController.createAdvisorySolutionWithPmLoginCustomerServiceInfo(cc)); // $NON-NLS$
            AbstractMainController.INSTANCE.addControllerCheckJson(false, "H_I", AsSimpleHtmlController.createAdvisorySolutionVersionDetails(cc)); // $NON-NLS$

            createAsyncSession();
        }

        private void finish() {
            AbstractMainController.INSTANCE.runInitSequence();
        }

        private void createAsyncSession() {
            final User user = SessionData.INSTANCE.getUser();
            Firebug.debug("create async session for user " + user.getUid());
            PmAsyncManager.createInstance(user.getUid());
            finish();
        }

    }
}