package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.ChildrenLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.Item;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.LayoutNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.BlockAndTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.workspace.ExplorerWorkspace;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DatabaseId;
import de.marketmaker.iview.pmxml.ExplorerTreeElement;
import de.marketmaker.iview.pmxml.GetExplorerRequest;
import de.marketmaker.iview.pmxml.GetExplorerResponse;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 15.04.13 08:16
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class ExplorerController extends AbstractPageController implements ChildrenLoader<Model.Item> {

    public static final String FOLDER_TOKEN_KEY = "f"; // $NON-NLS$
    public static final String LAYOUT_TOKEN_KEY = "l"; // $NON-NLS$
    public static final String TYPE_TOKEN_KEY = "t"; // $NON-NLS$
    private static final String LAST_FOLDER_KEY = "pm.ex.f."; // $NON-NLS$
    private List<ExplorerTreeElement> currentHolderTrees;
    private List<ExplorerTreeElement> currentPortfolioTrees;
    private List<ExplorerTreeElement> currentProspectTrees;
    private final AnalysisController analysisController;
    private HistoryToken historyToken;

    public enum Type {
        INVESTOR("I", I18n.I.customerLists()), // $NON-NLS$
        PORTFOLIO("P", I18n.I.portfolioLists()), // $NON-NLS$
        PROSPECT("R", I18n.I.prospectLists()); // $NON-NLS$

        private final String token;
        private final String displayText;

        Type(String token, String displayText) {
            this.token = token;
            this.displayText = displayText;
        }

        public String getToken() {
            return this.token;
        }

        public String getDisplayText() {
            return this.displayText;
        }

        public static Type fromToken(String token) {
            if (!StringUtil.hasText(token)) {
                return null;
            }
            final Type[] values = values();
            for (Type value : values) {
                if (value.getToken().toUpperCase().equals(token.toUpperCase())) {
                    return value;
                }
            }
            return null;
        }
    }

    private final GetExplorerRequest childrenReq;
    private HashMap<String, ShellMMInfo> currentFolderMap;
    private final BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> batFolderItemsInvestor;
    private final BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> batFolderItemsPortfolio;
    private final BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> batFolderItemsProspect;
    private Model currentModel;
    private LayoutNode currentLayoutGuid;
    private String currentFolderId;
    private final DmxmlContext.Block<GetExplorerResponse> explorerBlock;
    private final DmxmlContext.Block<GetExplorerResponse> childrenBlock;
    private final ExplorerView view;
    private Type currentType = null;
    private final Map<ShellMMType, String> iconMap = new HashMap<>();
    public static ExplorerController INSTANCE = new ExplorerController();


    private ExplorerController() {
        this.explorerBlock = this.context.addBlock("PM_Explorer"); // $NON-NLS$
        this.context.setCancellable(false);
        final GetExplorerRequest req = new GetExplorerRequest();
        this.explorerBlock.setParameter(req);
        this.childrenBlock = new DmxmlContext().addBlock("PM_Explorer"); // $NON-NLS$
        this.childrenReq = new GetExplorerRequest();
        this.childrenReq.setIncludeFilterResultFolder(true);
        this.childrenBlock.setParameter(this.childrenReq);

        this.batFolderItemsInvestor = new BlockAndTalker<>(
                new DmxmlContext(), new FolderItem.Talker("FolderItems.DeleteIfNot[#[](Typ=\"Inhaber\")].Sort[#Less;#Name]") // $NON-NLS$
        );
        this.batFolderItemsPortfolio = new BlockAndTalker<>(
                new DmxmlContext(), new FolderItem.Talker("FolderItems.DeleteIfNot[#[](Typ=\"Portfolio\")].Sort[#Less;#Name]") // $NON-NLS$
        );
        this.batFolderItemsProspect = new BlockAndTalker<>(
                new DmxmlContext(), new FolderItem.Talker("FolderItems.DeleteIfNot[#[](Typ=\"Interessent\")].Sort[#Less;#Name]") // $NON-NLS$
        );
        this.analysisController = new AnalysisController("EX"); // $NON-NLS$

        initIconMap();
        this.view = new ExplorerView(this, this.analysisController.getView().asWidget());
    }

    private void initIconMap() {
        this.iconMap.put(ShellMMType.ST_GRUPPE, "pm-folder"); // $NON-NLS$
        this.iconMap.put(ShellMMType.ST_INHABER, "pm-investor"); // $NON-NLS$
        this.iconMap.put(ShellMMType.ST_PORTFOLIO, "pm-investor-portfolio"); // $NON-NLS$
        this.iconMap.put(ShellMMType.ST_INTERESSENT, "pm-investor-prospect"); // $NON-NLS$
        this.iconMap.put(ShellMMType.ST_ORDNER, "pm-folder"); // $NON-NLS$
        this.iconMap.put(ShellMMType.ST_FILTER, "pm-filter"); // $NON-NLS$
        this.iconMap.put(ShellMMType.ST_FILTER_RESULT_FOLDER, "pm-subfilter"); // $NON-NLS$
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        this.historyToken = event.getHistoryToken();
        this.currentFolderId = this.historyToken.get(FOLDER_TOKEN_KEY);
        this.currentLayoutGuid = LayoutNode.create(this.historyToken.get(LAYOUT_TOKEN_KEY));

        final String type = this.historyToken.get(TYPE_TOKEN_KEY);
        Type tokenType = Type.fromToken(type);
        if (tokenType == null) {
            tokenType = Type.INVESTOR;
        }

        if (this.currentType != tokenType) { // if type has changed get new model
            this.currentType = tokenType;
            this.currentFolderId = null; // don't adopt folder or layout to the other type
            this.currentModel = null;
            super.refresh();
            return;
        }

        if (this.currentModel == null) { // this is the initial state
            super.refresh();
            return;
        }

        if (this.currentModel.hasData()) {
            handleInvalidFolderId();
            this.currentModel.select(this.currentFolderId);
            this.view.select(this.currentModel.getSelectedIds());
            show(event);
        }
        else {
            showNoData();
        }
    }

    private void handleInvalidFolderId() {
        if (!validFolderId(this.currentModel, this.currentFolderId)) {
            final String savedId = SessionData.INSTANCE.getUserProperty(LAST_FOLDER_KEY + this.currentType.getToken());
            if (validFolderId(this.currentModel, savedId)) {
                this.currentFolderId = savedId;
            }
            else {
                final Model.Item firstItem = this.currentModel.getFirstItem();
                if (firstItem != null) {
                    this.currentFolderId = firstItem.getId();
                }
            }
        }
    }

    private boolean validFolderId(Model model, String folderId) {
        return StringUtil.hasText(folderId) && model.getItem(folderId) != null;
    }

    @Override
    protected void onResult() {
        if (!this.explorerBlock.isResponseOk()) {
            Firebug.log("explorerBlock response not ok!");
            return;
        }

        if (this.explorerBlock.isEnabled()) {
            this.currentFolderMap = new HashMap<>();
            updateFolderMap(this.explorerBlock.getResult().getFolders());

            this.currentHolderTrees = this.explorerBlock.getResult().getHolderTrees();
            this.currentPortfolioTrees = this.explorerBlock.getResult().getPortfolioTrees();
            this.currentProspectTrees = this.explorerBlock.getResult().getInteressentTrees();

            this.explorerBlock.setEnabled(false); // TODO UM-20130726: this is a hack to avoid frequent explorer requests
        }

        final Model model;
        switch (this.currentType) {
            case INVESTOR:
                model = createModel(this.currentFolderMap, this.currentHolderTrees);
                break;
            case PORTFOLIO:
                model = createModel(this.currentFolderMap, this.currentPortfolioTrees);
                break;
            case PROSPECT:
                model = createModel(this.currentFolderMap, this.currentProspectTrees);
                break;
            default:
                throw new IllegalStateException("unknown type " + this.currentType); // $NON-NLS$
        }

        this.currentModel = model;
        if (this.currentModel.hasData()) {
            handleInvalidFolderId();
            this.currentModel.select(this.currentFolderId);
            this.view.updateExplorerTree(model);
            show(null);
        }
        else {
            showNoData();
        }
    }

    private void show(PlaceChangeEvent event) {
        getContentContainer().setContent(this.view.getPanel());
        if (this.currentLayoutGuid != null) {
            ExplorerWorkspace.getInstance().selectExplorerLayout(this.currentType, this.currentLayoutGuid);
            showLayout(event);
        }
        else {
            ExplorerWorkspace.getInstance().selectExplorerList(this.currentType);
            showFolderItems(this.currentType);
        }
    }

    private void updateFolderMap(List<ShellMMInfo> folders) {
        if (folders == null || folders.isEmpty()) {
            return;
        }
        for (ShellMMInfo folder : folders) {
            this.currentFolderMap.put(folder.getId(), folder);
        }
    }

    private Model createModel(Map<String, ShellMMInfo> folderMap, List<ExplorerTreeElement> tree) {
        final Model model = new Model();
        for (ExplorerTreeElement explorerTreeElement : tree) {
            final ShellMMInfo shellMMInfo = folderMap.get(explorerTreeElement.getId());
            final Model.Item item = model.createItem(
                    shellMMInfo.getId(), shellMMInfo.getBezeichnung(), this.iconMap.get(shellMMInfo.getTyp())
            );
            model.add(item);
            if (!explorerTreeElement.getChildren().isEmpty()) {
                addChildren(model, item, folderMap, explorerTreeElement);
            }
        }
        return model;
    }

    private Model addChildren(Model model, Model.Item item, Map<String, ShellMMInfo> folderMap, ExplorerTreeElement explorerTreeElement) {
        final List<ExplorerTreeElement> children = explorerTreeElement.getChildren();
        if (children.isEmpty()) {
            return model;
        }
        item.clearChildren();
        for (ExplorerTreeElement child : children) {
            final ShellMMInfo shellMMInfo = folderMap.get(child.getId());
            final Model.Item childItem = model.createItem(
                    shellMMInfo.getId(), shellMMInfo.getBezeichnung(),
                    this.iconMap.get(shellMMInfo.getTyp()), child.isMoreChildren(),
                    child.isOpenableWorkspace()
            );
            item.add(childItem);
            addChildren(model, childItem, folderMap, child);
        }
        return model;
    }

    public void requestChildren(final Model.Item item, final AsyncCallback<List<Model.Item>> callback) {
        final DatabaseId dbId = new DatabaseId();
        dbId.setId(item.getDatabaseId());
        this.childrenReq.setDatabaseId(dbId);
        this.childrenBlock.setToBeRequested();
        this.childrenBlock.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.log("could not load explorer data for node " + item);
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ResponseType result) {
                if (!childrenBlock.isResponseOk()) {
                    return;
                }
                final GetExplorerResponse explorerResponse = childrenBlock.getResult();
                updateFolderMap(explorerResponse.getFolders());
                switch (currentType) {
                    case INVESTOR:
                        final List<ExplorerTreeElement> holderTrees = explorerResponse.getHolderTrees();
                        if (holderTrees.isEmpty()) {
                            Firebug.warn("ExplorerController.requestChildren() -> onSuccess() -> holderTrees is empty");
                        }
                        else {
                            addChildren(currentModel, item, currentFolderMap, holderTrees.get(0));
                        }
                        break;
                    case PORTFOLIO:
                        final List<ExplorerTreeElement> portfolioTrees = explorerResponse.getPortfolioTrees();
                        if (portfolioTrees.isEmpty()) {
                            Firebug.warn("ExplorerController.requestChildren() -> onSuccess() -> portfolioTrees is empty");
                        }
                        else {
                            addChildren(currentModel, item, currentFolderMap, portfolioTrees.get(0));
                        }
                        break;
                    case PROSPECT:
                        final List<ExplorerTreeElement> prospectTrees = explorerResponse.getInteressentTrees();
                        if (prospectTrees.isEmpty()) {
                            Firebug.warn("ExplorerController.requestChildren() -> onSuccess() -> prospectTrees is empty");
                        }
                        else {
                            addChildren(currentModel, item, currentFolderMap, prospectTrees.get(0));
                        }
                        break;
                    default:
                        throw new IllegalStateException("unknown type " + currentType); // $NON-NLS$
                }
                callback.onSuccess(item.getChildren());
            }
        });
    }

    private void showFolderItems(Type type) {
        final BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> batFolderItems;
        batFolderItems = getBatFolderItems(type);
        requestFolderItems(batFolderItems, false);
    }

    private BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> getBatFolderItems(Type type) {
        final BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> batFolderItems;
        switch (type) {
            case INVESTOR:
                batFolderItems = this.batFolderItemsInvestor;
                break;
            case PORTFOLIO:
                batFolderItems = this.batFolderItemsPortfolio;
                break;
            case PROSPECT:
                batFolderItems = this.batFolderItemsProspect;
                break;
            default:
                throw new IllegalStateException("unknown type " + type); // $NON-NLS$
        }
        return batFolderItems;
    }

    private void requestFolderItems(BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> batFolderItems, boolean forRefresh) {
        final Model.Item item = this.currentModel.getSelectedItem();
        if (item == null) {
            return;
        }
        final AsyncCallback<ResponseType> callback = getFolderItemCallback(batFolderItems, item, forRefresh);
        if (batFolderItems.getDatabaseId().equals(item.getDatabaseId()) && !forRefresh) {
            callback.onSuccess(null);
            return;
        }
        batFolderItems.setDatabaseId(item.getDatabaseId());
        batFolderItems.getBlock().issueRequest(callback);
    }

    private AsyncCallback<ResponseType> getFolderItemCallback(final BlockAndTalker<FolderItem.Talker, List<FolderItem>, FolderItem> batFolderItems,
                                                              final Model.Item item, final boolean forRefresh) {
        return new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                setHeader(StringUtil.asHeader(ExplorerController.this.currentType.getDisplayText()));
                view.showFolderItems(null);
            }

            @Override
            public void onSuccess(ResponseType result) {
                final List<FolderItem> resultObject = batFolderItems.createResultObject();
                setHeader(StringUtil.asHeader(ExplorerController.this.currentType.getDisplayText(), item.getName()));
                view.showFolderItems(resultObject);
                if (!forRefresh) {
                    view.select(currentModel.getSelectedIds());
                }
            }
        };
    }

    private void showLayout(PlaceChangeEvent event) {
        final Model.Item item = this.currentModel.getSelectedItem();
        final LayoutDesc desc = PmWebSupport.getInstance().getReportByGuidId(this.currentLayoutGuid.getGuid());
        if(desc == null) {
            this.view.showNoLayout(this.currentType);
            return;
        }
        setHeader(StringUtil.asHeader(desc.getLayout().getLayoutName(), item.getName()));

        String handle = event != null ? event.getHistoryToken().get(AnalysisController.HKEY_HANDLE) : null;
        if(handle == null) {
            handle = this.historyToken.get(AnalysisController.HKEY_HANDLE);
        }
        if (StringUtil.hasText(handle)) {
            final String databaseId = item.getDatabaseId();
            final String layoutGuid = desc.getLayout() != null ? desc.getLayout().getGuid() : null;
            final Config sourceConfig = StringUtil.hasText(databaseId) && StringUtil.hasText(layoutGuid)
                    ? Config.createWithDatabaseId(this.historyToken, item.getDatabaseId(), desc.getLayout().getGuid(),
                    this.view.getOffsetHeight(), this.view.getOffsetWidth(), PrivacyMode.isActive())
                    : null;

            this.analysisController.fetch(Config.createWithHandle(this.historyToken, handle, desc.getLayout().getGuid(),
                    null, sourceConfig, PrivacyMode.isActive()));
        }
        else {
            this.analysisController.evaluate(Config.createWithDatabaseId(this.historyToken, item.getDatabaseId(),
                    desc.getLayout().getGuid(), this.view.getOffsetHeight(), this.view.getOffsetWidth(), PrivacyMode.isActive()
            ));
        }
        this.view.showLayout();
    }

    private void showNoData() {
        setHeader(StringUtil.asHeader(this.currentType.getDisplayText(), I18n.I.noDataAvailable()));
        this.view.showNoData(this.currentType);
    }

    public DmxmlContext getContext() {
        return this.context;
    }

    public void selectionChanged(Model.Item item) {
        MmwebServiceAsyncProxy.cancelPending();
        final String token = this.currentType.getToken();
        createHistoryToken(item.getId(), this.currentLayoutGuid, token).fire();
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(LAST_FOLDER_KEY + token, item.getId());
    }

    public HistoryToken createHistoryToken() {
        return createHistoryToken(this.currentFolderId, this.currentLayoutGuid, this.currentType != null
                ? this.currentType.getToken()
                : null);
    }


    public HistoryToken createHistoryToken(String folderId, LayoutNode layoutNode, String typeToken) {
        final HistoryToken.Builder builder = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_EXPLORER);
        if (StringUtil.hasText(typeToken)) {
            builder.with(TYPE_TOKEN_KEY, typeToken);
        }
        if (StringUtil.hasText(folderId)) {
            builder.with(FOLDER_TOKEN_KEY, folderId);
        }
        if (layoutNode != null) {
            builder.with(LAYOUT_TOKEN_KEY, layoutNode.toString());
        }
        return builder.build();
    }

    private void setHeader(final SafeHtml safeHtml) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                MainController.INSTANCE.getView().setContentHeader(safeHtml);
            }
        });
    }

    public Type getCurrentType() {
        return this.currentType;
    }

    @Override
    public boolean isPrintable() {
        return this.analysisController.isPrintable();
    }

    @Override
    public String getPrintHtml() {
        return this.analysisController.getPrintHtml();
    }

    @Override
    public void deactivate() {
        this.analysisController.deactivate();
    }

    @Override
    public void activate() {
        this.analysisController.activate();
    }

    @Override
    public void refresh() {
        if (this.currentLayoutGuid != null) {
            this.analysisController.evaluate(this.analysisController.getConfig(), true);
        }
        else if (this.currentModel != null && this.currentType != null) {
            requestFolderItems(getBatFolderItems(this.currentType), true);
        }
    }

    @Override
    public boolean hasMoreChildren(Model.Item item) {
        return item.hasMoreChildren();
    }

    @Override
    public void loadChildren(final Model.Item item, final AsyncCallback<List<? extends Item>> callback) {
        requestChildren(item, new AsyncCallback<List<Model.Item>>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("load children failed for " + item.getName(), caught);
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(List<Model.Item> result) {
                //assumption is that filters should be re-evaluated every time.
                //if this assumption is false, the request must be done only once (when getItems is null)
                // --> set hasMoreChildren to false in this case -> also remove style async from expandIcon in NavTree
                callback.onSuccess(result);
            }
        });
    }

}