package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PrivFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.BlockAndTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DatabaseId;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.internaltypes.DMSSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 12.03.15
 * Copyright (c) vwd GmbH. All Rights Reserved.
 *
 * @author mloesch
 */
public class DmsPresenter implements DmsDisplay.Presenter, PrivacyMode.InterestedParty{

    private DmsDisplay display;
    DmxmlContext.Block<DMSSearchResult> searchBlock;
    private final List<SearchResultListener> listener = new ArrayList<>();
    private final List<SearchResultListener> tmpListeners = new ArrayList<>();
    private String pagingHandle;

    public DmsPresenter() {
        this(new DmxmlContext());
    }

    public DmsPresenter(DmxmlContext context) {
        this.searchBlock = context.addBlock("AS_DMSSearch"); // $NON-NLS$
        this.searchBlock.setParameter("count", 1000); // $NON-NLS$
        this.searchBlock.setParameter("sortBy", "date"); // $NON-NLS$
        this.searchBlock.setParameter("ascending", "false"); // $NON-NLS$
        this.searchBlock.setParameter("customerDesktopActive", "false"); // $NON-NLS$
        this.searchBlock.setParameter("postboxFeatureActive", Selector.AS_POSTBOX.isAllowed()); // $NON-NLS$

        PrivacyMode.subscribe(this);
    }

    public void destroy() {
        PrivacyMode.unsubscribe(this);
    }

    @Override
    public void privacyModeStateChanged(boolean privacyModeActive, PrivacyMode.StateChangeProcessedCallback processed) {
        this.searchBlock.setParameter("customerDesktopActive", privacyModeActive);  // $NON-NLS$
        this.pagingHandle = null;
        processed.privacyModeStateChangeProcessed(this);
    }

    @Override
    public void requestDmsMetadata(String objectId, final DmsDisplay.DmsMetadataCallback callback) {
        final DmsMetadata.Talker talker = new DmsMetadata.Talker(Formula.create().getFormula());
        final DmxmlContext context = new DmxmlContext();
        final PrivFeature priv = new PrivFeature(context, MMClassIndex.CI_T_SHELL_MM, UMRightBody.UMRB_READ_DOCUMENTS, UMRightBody.UMRB_WRITE_DOCUMENTS);
        priv.setId(objectId);
        final BlockAndTalker<DmsMetadata.Talker, DmsMetadata, DmsMetadata> bat = new BlockAndTalker<>(context, talker);
        final DatabaseId dbid = new DatabaseId();
        dbid.setId(objectId);
        bat.setPrivacyModeActive(PrivacyMode.isActive());
        talker.getQuery().getIds().add(dbid);
        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                final String err = I18n.I.dmsErrorMetadataRequestFailed();
                AbstractMainController.INSTANCE.showError(err);
                DebugUtil.logToServer(err, throwable);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                final DmsMetadata metadata = bat.createResultObject();
                metadata.setPriv(priv);
                callback.metadataAvailable(metadata);
            }
        });
    }

    @Override
    public void update(MmJsDate dateFrom, MmJsDate dateTo) {
        DmsPresenter.setToMidnight(dateTo);
        this.searchBlock.setParameter("dateTo", MmTalkHelper.asMMDateTime(dateTo).getValue()); // $NON-NLS$
        this.searchBlock.setParameter("dateFrom", MmTalkHelper.asMMDateTime(dateFrom).getValue()); // $NON-NLS$
        update();
    }

    @Override
    public void update(DmsMetadata metadata, LayoutDesc layoutDesc) {
        final MmJsDate to = new MmJsDate();
        final MmJsDate from = new MmJsDate(to.getFullYear() - 1, to.getMonth(), to.getDay()); //to minus one year
        update(metadata, layoutDesc, from, to);
    }

    @Override
    public void update(DmsMetadata metadata, LayoutDesc layoutDesc, MmJsDate dateFrom, MmJsDate dateTo) {
        this.searchBlock.removeParameter("pagingHandle"); // $NON-NLS$
        this.searchBlock.setParameter("id", metadata.getIdentifier()); // $NON-NLS$
        this.searchBlock.setParameter("type", metadata.getType().value()); // $NON-NLS$
        if (layoutDesc != null) {
            this.searchBlock.setParameter("templateName", layoutDesc.getLayout().getLayoutName()); // $NON-NLS$
        }
        setToMidnight(dateTo);
        this.searchBlock.setParameter("dateTo", MmTalkHelper.asMMDateTime(dateTo).getValue()); // $NON-NLS$
        this.searchBlock.setParameter("dateFrom", MmTalkHelper.asMMDateTime(dateFrom).getValue()); // $NON-NLS$
        update();
    }

    @Override
    public void update(Config config) {
        setToMidnight(config.dateTo);
        this.searchBlock.setParameter("dateTo", MmTalkHelper.asMMDateTime(config.dateTo).getValue()); // $NON-NLS$
        this.searchBlock.setParameter("dateFrom", MmTalkHelper.asMMDateTime(config.dateFrom).getValue()); // $NON-NLS$
        this.searchBlock.setParameter("comment", config.comment); // $NON-NLS$
        this.searchBlock.setParameter("documentName", config.name); // $NON-NLS$
        if (!config.documentTypes.isEmpty()) {
            this.searchBlock.setParameters("documentTypes", config.documentTypes.toArray(new String[config.documentTypes.size()])); // $NON-NLS$
        }
        else {
            this.searchBlock.removeParameter("documentTypes"); // $NON-NLS$
        }
        update();
    }

    static void setToMidnight(MmJsDate date) {
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
    }

    @Override
    public void layoutWhenUpdateDone() {
        if (this.searchBlock.isPending()) {
            final SearchResultListener listener = new SearchResultListener() {
                @Override
                public void onSearchResult(DMSSearchResult response) {
                    display.layout();
                }
            };
            this.tmpListeners.add(listener);
            addSearchResultListener(listener);
        }
        else {
            this.display.layout();
        }
    }

    @Override
    public void update() {
        update(false);
    }

    @Override
    public void updateForPaging() {
        update(true);
    }

    private void update(boolean usePagingHandle) {
        if (usePagingHandle && StringUtil.hasText(this.pagingHandle)) {
            this.searchBlock.setParameter("pagingHandle", this.pagingHandle); // $NON-NLS$
        }

        this.searchBlock.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                Firebug.error("dms search failed!", throwable);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                if (!searchBlock.isResponseOk()) {
                    return;
                }
                pagingHandle = searchBlock.getResult().getPagingHandle();
                display.updateData(searchBlock.getResult());
                for (SearchResultListener srl : listener) {
                    srl.onSearchResult(searchBlock.getResult());
                }
                cleanupTmpListeners();
                searchBlock.removeParameter("pagingHandle"); // $NON-NLS$
            }
        });
    }

    private void cleanupTmpListeners() {
        this.listener.removeAll(this.tmpListeners);
        this.tmpListeners.clear();
    }

    @Override
    public void download(DocumentMetadata dm) {
        Window.open(AnalysisController.createUrl("dms", dm.getDocumentHandle()), dm.getDocumentName(), ""); // $NON-NLS$
    }

    @Override
    public void setDisplay(DmsDisplay display) {
        this.display = display;
    }

    @Override
    public void addSearchResultListener(SearchResultListener listener) {
        this.listener.add(listener);
    }
}