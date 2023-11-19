package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.PMByteReport;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisMetadataCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisMetadataHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card.PmReportCardController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Investor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.BlockAndTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.EvalLayoutRequest;
import de.marketmaker.iview.pmxml.EvalLayoutTableResponse;
import de.marketmaker.iview.pmxml.Key;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.MMLayoutType;
import de.marketmaker.iview.pmxml.Parameter;
import de.marketmaker.iview.pmxml.QueryStandardSelection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 26.05.2010 14:04:37
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 * @author Ulrich Maurer
 */

//TODO: REMOVE THIS CLASS! ONLY NEEDED FOR JIWAY (zone: pmreport) RIGHT NOW!

public class PmReportController extends AbstractPageController {
    private final PmReportView view;

    private final DmxmlContext.Block<PMByteReport> blockPdfReport;
    private final DmxmlContext.Block<EvalLayoutTableResponse> blockTable;
    private int cardCounter = 0;

    public PmReportController() {
        super();
        Firebug.log("initialize PmReportController");
        this.view = new PmReportView();
        this.blockPdfReport = this.context.addBlock("PM_EvalLayoutPdf"); // $NON-NLS-0$
        this.blockTable = this.context.addBlock("PM_EvalLayoutTable"); // $NON-NLS$
        PmReportCardController.getInstance();
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                getContentContainer().setContent(view);
            }
        });

        try {
            final HistoryToken historyToken = event.getHistoryToken();
            if (historyToken.getAllParamCount() == 1) {
                return;
            }
            final HashMap<String, String> mapParameters = historyToken.getNamedParams();
            final String investorId = mapParameters.remove("investorId"); // $NON-NLS$
            final int cardId;
            InvestorItem investor = null;
            final String id;
            // that part is a hack for the pmreport zone. ////////////////////////////////////////////////////////////////
            if (investorId != null) {
                final String sCardId = mapParameters.remove("cardId"); // $NON-NLS$
                if (sCardId == null) {
                    cardId = ++this.cardCounter;
                    PmReportCardController.getInstance().closeAllCards();
                }
                else {
                    cardId = Integer.parseInt(sCardId);
                }
                id = investorId;
                final DmxmlContext batContext = new DmxmlContext();
                final BlockAndTalker<Investor.InvestorTalker, Investor, Investor> investorTalker =
                        new BlockAndTalker<Investor.InvestorTalker, Investor, Investor>(batContext, new Investor.InvestorTalker());
                investorTalker.setDatabaseId(investorId);
                batContext.issueRequest(new AsyncCallback<ResponseType>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Firebug.error("could not request investor", caught);
                    }

                    @Override
                    public void onSuccess(ResponseType result) {
                        final Investor resultObject = investorTalker.createResultObject();
                        final InvestorItem investorItem = new InvestorItem(resultObject.getNumber(), InvestorItem.Type.Holder,
                                resultObject.getName(), "PUBLIC", false); // $NON-NLS$
                        prepareRequest(investorItem, investorId, cardId, mapParameters);
                    }
                });
            }
            ///// eof hack ///////////////////////////////////////////////////////////////////////////////////////////////
            else {
                cardId = Integer.parseInt(removeTokenChecked(mapParameters, "cardId")); // $NON-NLS$
                final String investorKey = mapParameters.remove("investorKey"); // $NON-NLS$
                investor = InvestorItem.getByKey(investorKey);
                id = investorKey;
            }
            prepareRequest(investor, id, cardId, mapParameters);
        } catch (Exception e) {
            Firebug.error("Exception in PmReportController.onPlaceChange()", e);
        }
    }

    private void prepareRequest(final InvestorItem investor, String id, final int cardId, final HashMap<String, String> mapParameters) {
        if (investor == null) {
            Firebug.log("undefined investor id/key: " + id); // $NON-NLS$
            return;
        }

        final String layoutGuid = removeTokenChecked(mapParameters, "layoutGuid"); // $NON-NLS-0$
        final LayoutDesc layoutDesc = PmWebSupport.getInstance().getReportByGuidId(layoutGuid);
        if (layoutDesc == null) {
            throw new IllegalArgumentException("layoutDesc == null! undefined \"layoutGuid: " + layoutGuid); // $NON-NLS$
        }

        final MMLayoutType layoutType = layoutDesc.getLayout().getLayoutType();
        final DmxmlContext.Block block;
        final EvalLayoutRequest req = new EvalLayoutRequest();
        req.setLayoutGuid(layoutDesc.getLayout().getGuid());
        if (layoutType == MMLayoutType.LTP_CRYSTAL || layoutType == MMLayoutType.LTP_REPORT_MAPPE) {
            this.blockPdfReport.setEnabled(true);
            this.blockTable.setEnabled(false);
            block = this.blockPdfReport;
        }
        else {
            this.blockTable.setEnabled(true);
            this.blockPdfReport.setEnabled(false);
            block = this.blockTable;
        }
        block.setParameter(req);

        AnalysisMetadataHandler.getInstance().getMetadata(layoutGuid, new AnalysisMetadataCallback() {

            @Override
            public void onMetadataAvailable(LayoutDesc report, String jsonConfig) {
                addLayoutParameters(req, mapParameters, report);

                PmReportCardController.getInstance().displayCard(cardId, investor, report, mapParameters);
                final QueryStandardSelection query = new QueryStandardSelection();
                final Key key = new Key();
                Firebug.log("setting query values");
                Firebug.log("key.setKey(" + investor.getId() + ")");
                key.setKey(investor.getId());
                query.getKeys().add(key);
                Firebug.log("query.getShellMMType(" + investor.getType().getShellMMType() + ")");
                query.setDataItemType(investor.getType().getShellMMType());
                req.setQuery(query);
                refresh();
            }
        });
    }

    private void addLayoutParameters(EvalLayoutRequest req, Map<String, String> layoutParameters, LayoutDesc metadata) {
        if (layoutParameters.isEmpty()) {
            return;
        }
        final Map<String, Parameter> map = MmTalkHelper.toParameterMap(layoutParameters, metadata);
        for (String s : map.keySet()) {
            final Parameter value = map.get(s);
            if (value == null) {
                continue;
            }
            req.getParameters().add(value);
        }

    }

    private String removeTokenChecked(Map<String, String> mapParameters, String key) {
        final String value = mapParameters.remove(key);
        if (!StringUtil.hasText(value)) {
            throw new IllegalArgumentException("key not specified: " + key); // $NON-NLS$
        }
        return value;
    }

    @Override
    protected void onResult() {
        onResult(this.blockPdfReport.isEnabled() ?
                this.blockPdfReport :
                this.blockTable);
    }

    private void onResult(DmxmlContext.Block block) {
        if (!block.isResponseOk()) {
            AbstractMainController.INSTANCE.showError(I18n.I.cannotCreateEvaluation());
            Firebug.log(block.getKey() + ": result is not ok - " + block.getError().getDescription()); // $NON-NLS-0$
            return;
        }
        if (this.blockPdfReport.isEnabled()) {
            this.view.showPdfReport(ChartUrlFactory.getUrl(this.blockPdfReport.getResult().getRequest()));
        }
        else if (this.blockTable.isEnabled()) {
            this.view.showTable(this.blockTable.getResult());
        }
        else {
            Firebug.log("PmReportController <onResult> NO BLOCK ENABLED!");
        }
    }
}