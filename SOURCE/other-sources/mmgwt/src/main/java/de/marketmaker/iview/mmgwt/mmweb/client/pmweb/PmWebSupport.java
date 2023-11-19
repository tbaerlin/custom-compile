/*
 * PmWebSupport.java
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.DashboardConfigDao;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.myspace.SnippetMenuConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.LayoutMenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.DashboardSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.PmInstrumentSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.PmPriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.PmReportSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.PmSecuritySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.workspace.ExplorerWorkspace;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.AbstractPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.bulk.BulkRequestBlock;
import de.marketmaker.iview.mmgwt.mmweb.client.util.bulk.BulkRequestHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.SearchWorkspace;
import de.marketmaker.iview.pmxml.AvailableLayoutsRequest;
import de.marketmaker.iview.pmxml.AvailableLayoutsResponse;
import de.marketmaker.iview.pmxml.AvailableLayoutsSortMode;
import de.marketmaker.iview.pmxml.GetUserfieldsDeclsRequest;
import de.marketmaker.iview.pmxml.GetUserfieldsDeclsResponse;
import de.marketmaker.iview.pmxml.GetWorkspaceRequest;
import de.marketmaker.iview.pmxml.GetWorkspaceResponse;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.LayoutDocumentType;
import de.marketmaker.iview.pmxml.LayoutGlobalMetaDataResponse;
import de.marketmaker.iview.pmxml.MMLayoutType;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UserField;
import de.marketmaker.iview.pmxml.UserFieldDeclarationDesc;
import de.marketmaker.iview.pmxml.UserFieldDeclarationDescTypeGroup;
import de.marketmaker.iview.pmxml.UserFieldName;
import de.marketmaker.iview.pmxml.VoidRequest;
import de.marketmaker.iview.pmxml.WorksheetDefaultMode;
import de.marketmaker.iview.pmxml.WorkspaceDefaultSheetDesc;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author umaurer
 */
public class PmWebSupport {
    private static PmWebSupport instance = null;

    private static final UserFieldDeclarationDescComparator USER_FIELD_DECLARATION_DESC_COMPARATOR = new UserFieldDeclarationDescComparator();

    private static final UserFieldCategoryComparator USER_FIELD_CATEGORY_COMPARATOR = new UserFieldCategoryComparator();

    private final AsyncCallback<ResponseType> callback;

    private Map<String, LayoutDesc> mapReportsByGuidId = null;

    private Map<InvestorItem.Type, LayoutMenuItem> mapReportMenuRootItem = null;

    private Map<InvestorItem.Type, LayoutDesc> mapReportDefaults = null;

    private Map<ShellMMType, List<UserFieldCategory>> mapUserFieldDecls = null;

    private LayoutGlobalMetaDataResponse globalLayoutMetadata;

    private final HashSet<String> portraitsWithPmWorkspace = new HashSet<>();

    private final AbstractMainController.Dependency<PageController> pmWorkspaceDependency =
            this::injectPmWorkspaceDependency;

    public static PmWebSupport getInstance() {
        return instance;
    }

    private PmWebSupport(final AsyncCallback<ResponseType> callback) {
        this.callback = callback;
        initializeNativeCallback();

        final BulkRequestBlock brb = BulkRequestBlock.forPmxml();
        final DmxmlContext.Block<AvailableLayoutsResponse> blockAvailableReports = brb.addBlock("PM_GetAvailableLayouts"); // $NON-NLS$
        final AvailableLayoutsRequest availLayoutsParam = new AvailableLayoutsRequest();
        availLayoutsParam.setSortMode(AvailableLayoutsSortMode.ALSM_DEFAULT);
        blockAvailableReports.setParameter(availLayoutsParam);

        final DmxmlContext.Block<AvailableLayoutsResponse> blockPmDashboardLayouts = brb.addBlock("PM_GetAvailableLayouts"); // $NON-NLS$
        final AvailableLayoutsRequest dashLayoutsParam = new AvailableLayoutsRequest();
        dashLayoutsParam.setSortMode(AvailableLayoutsSortMode.ALSM_ZONE_AND_NAME);
        dashLayoutsParam.getLayoutTypes().add(MMLayoutType.LTP_WEBGUI_DASHBOARD);
        dashLayoutsParam.setLoadBlob(true);
        blockPmDashboardLayouts.setParameter(dashLayoutsParam);

        final DmxmlContext.Block<AvailableLayoutsResponse> blockPmSnippetLayouts = brb.addBlock("PM_GetAvailableLayouts"); // $NON-NLS$
        final AvailableLayoutsRequest snippetLayoutsParam = new AvailableLayoutsRequest();
        snippetLayoutsParam.setSortMode(AvailableLayoutsSortMode.ALSM_DEFAULT);
        snippetLayoutsParam.getLayoutTypes().add(MMLayoutType.LTP_WEBGUI_FORMULA_GADGET);
        snippetLayoutsParam.setInitParams(true);
        blockPmSnippetLayouts.setParameter(snippetLayoutsParam);

        final DmxmlContext.Block<LayoutGlobalMetaDataResponse> globalLayoutMetadata = brb.addBlock("PM_GlobalLayoutMetadata"); // $NON-NLS$
        globalLayoutMetadata.setParameter(new VoidRequest());

        final Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMap = createTypeMap(false, brb);
        final Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMapPrivacyMode = createTypeMap(true, brb);

        final DmxmlContext.Block<GetUserfieldsDeclsResponse> blockUserFieldDefs = brb.addBlock("PM_UserFieldDecls"); // $NON-NLS$
        final GetUserfieldsDeclsRequest req = new GetUserfieldsDeclsRequest();
        req.getShellMMTypes().addAll(MmTalkHelper.toShellMmTypeDescList(ShellMMType.values()));
        blockUserFieldDefs.setParameter(req);

        final BulkRequestHandler bulkRequestHandler = new BulkRequestHandler();
        bulkRequestHandler.add(brb);
        bulkRequestHandler.sendRequests(o -> {
            if (bulkRequestHandler.isFinishedSuccessfully()) {
                onAllRequestsSuccessfull(blockAvailableReports, blockPmDashboardLayouts, blockPmSnippetLayouts, typeMap, typeMapPrivacyMode, blockUserFieldDefs, globalLayoutMetadata);
            }
            else {
                callback.onFailure(null);
            }
        });
    }

    private Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> createTypeMap(
            boolean privacyModeActive, BulkRequestBlock brb) {
        final Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMap = new HashMap<>();
        typeMap.put(ShellMMType.ST_INHABER, createWorkspaceBlock(privacyModeActive, brb, ShellMMType.ST_INHABER));
        typeMap.put(ShellMMType.ST_INTERESSENT, createWorkspaceBlock(privacyModeActive, brb, ShellMMType.ST_INTERESSENT));
        typeMap.put(ShellMMType.ST_DEPOT, createWorkspaceBlock(privacyModeActive, brb, ShellMMType.ST_DEPOT));
        typeMap.put(ShellMMType.ST_PORTFOLIO, createWorkspaceBlock(privacyModeActive, brb, ShellMMType.ST_PORTFOLIO));
        typeMap.put(ShellMMType.ST_KONTO, createWorkspaceBlock(privacyModeActive, brb, ShellMMType.ST_KONTO));
        typeMap.put(ShellMMType.ST_PERSON, createWorkspaceBlock(privacyModeActive, brb, ShellMMType.ST_PERSON));
        typeMap.put(ShellMMType.ST_UNBEKANNT, createWorkspaceBlock(privacyModeActive, brb, ShellMMType.ST_UNBEKANNT));
        return typeMap;
    }

    private DmxmlContext.Block<GetWorkspaceResponse> createWorkspaceBlock(boolean privacyModeActive,
            BulkRequestBlock brb, ShellMMType shellMMType) {
        final DmxmlContext.Block<GetWorkspaceResponse> result = brb.addBlock("PM_GetWorkspace"); // $NON-NLS$
        final GetWorkspaceRequest req = new GetWorkspaceRequest();
        req.setCustomerDesktopActive(privacyModeActive);
        req.setShellMMType(shellMMType);
        result.setParameter(req);
        return result;
    }

    private native void initializeNativeCallback() /*-{
        $wnd.pmIsinLink = @de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport::pmIsinLink(Ljava/lang/String;);
    }-*/;

    @SuppressWarnings({"UnusedDeclaration"})
    private static void pmIsinLink(String isin) {
        Firebug.log("pmIsinLink(" + isin + ")"); // $NON-NLS$
        PlaceUtil.goTo("M_S/s=" + isin); // $NON-NLS$
    }

    private void onAllRequestsSuccessfull(
            DmxmlContext.Block<AvailableLayoutsResponse> blockAvailableReports,
            DmxmlContext.Block<AvailableLayoutsResponse> blockDashboardLayouts,
            DmxmlContext.Block<AvailableLayoutsResponse> blockPmSnippetLayouts,
            Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMap,
            Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMapPrivacyMode,
            DmxmlContext.Block<GetUserfieldsDeclsResponse> blockUserFieldDefs,
            DmxmlContext.Block<LayoutGlobalMetaDataResponse> globalLayoutMetadata) {
        Firebug.log("PmWebSupport <onAllRequestsSuccessfull>");
        DashboardConfigDao.initialize(blockDashboardLayouts.getResult().getLayouts(),
                blockDashboardLayouts.getResult().getPrivZoneId(),
                () -> Scheduler.get().scheduleDeferred(
                        () -> {
                            callback.onSuccess(null);
                            Firebug.log("portfolio module initialized successfully"); // $NON-NLS$
                        }));
        PmWorkspaceHandler.getInstance().update(false, typeMap);
        PmWorkspaceHandler.getInstance().update(true, typeMapPrivacyMode);
        buildReportMenus(blockAvailableReports.getResult().getLayouts(), typeMap);
        registerPmSnippetLayouts(blockPmSnippetLayouts.getResult().getLayouts());
        buildUserFieldDeclsMap(blockUserFieldDefs.getResult());

        this.globalLayoutMetadata = globalLayoutMetadata.getResult();
        if (this.globalLayoutMetadata != null) {
            Collections.sort(this.globalLayoutMetadata.getDocumentTypes(), new LayoutDocumentTypeComparator());
        }

        loadPortraitsWithPmWorkspace();
        AbstractMainController.INSTANCE.addPageControllerDependency(this.pmWorkspaceDependency);

        AbstractMainController.INSTANCE.getView().addWorkspaceItem(ExplorerWorkspace.getInstance());
        AbstractMainController.INSTANCE.getView().addWorkspaceItem(SearchWorkspace.getInstance());

        configurePmSnippets();
    }

    private void registerPmSnippetLayouts(List<LayoutDesc> layouts) {
        for (LayoutDesc layoutDesc : layouts) {
            final DashboardSnippet.Class layoutSnippetClass = new DashboardSnippet.Class(layoutDesc);
            SnippetClass.addClass(layoutSnippetClass);
            SnippetMenuConfig.INSTANCE.add(layoutSnippetClass);
        }
    }

    private void buildUserFieldDeclsMap(GetUserfieldsDeclsResponse response) {
        this.mapUserFieldDecls = new HashMap<>();

        final HashMap<String, UserFieldDeclarationDesc> nameToDescription = new HashMap<>();

        for (UserFieldDeclarationDesc declaration : response.getDeclarations()) {
            nameToDescription.put(declaration.getName(), declaration);
        }

        for (UserFieldDeclarationDescTypeGroup typeGroup : response.getTypeGroups()) {
            final Map<String, UserFieldCategory> categoryNameToCategory = new HashMap<>();

            for (UserFieldName name : typeGroup.getNames()) {
                final UserFieldDeclarationDesc userFieldDeclarationDesc = nameToDescription.get(name.getName());
                if (userFieldDeclarationDesc == null) {
                    throw new IllegalStateException("UserFieldName '" + name.getName() + "' not listed in GetUserfieldsDeclsResponses declaration list"); // $NON-NLS$
                }

                String fieldCategoryName = userFieldDeclarationDesc.getDecl().getFieldCategory();
                if (!StringUtil.hasText(fieldCategoryName)) {
                    fieldCategoryName = UserFieldCategory.DEFAULT_CATEGORY;
                }

                UserFieldCategory userFieldCategory = categoryNameToCategory.get(fieldCategoryName);
                if (userFieldCategory == null) {
                    userFieldCategory = new UserFieldCategory(fieldCategoryName);
                    categoryNameToCategory.put(fieldCategoryName, userFieldCategory);
                }

                userFieldCategory.getUserFieldDeclarationDescs().add(userFieldDeclarationDesc);
            }

            final List<UserFieldCategory> userFieldCategories = new ArrayList<>(categoryNameToCategory.values());
            this.mapUserFieldDecls.put(typeGroup.getTyp(), userFieldCategories);

            Collections.sort(userFieldCategories, USER_FIELD_CATEGORY_COMPARATOR);

            for (UserFieldCategory ufc : userFieldCategories) {
                Collections.sort(ufc.getUserFieldDeclarationDescs(), USER_FIELD_DECLARATION_DESC_COMPARATOR);
            }
        }
    }

    private static void configurePmSnippets() {
        // residual of early PM Web prototype
        /*final PmSignalsSnippet.Class pmSignalsSnippetClazz = new PmSignalsSnippet.Class();
        SnippetClass.addClass(pmSignalsSnippetClazz);
        SnippetMenuConfig.INSTANCE.add(pmSignalsSnippetClazz);*/

        // just for early dashboard demo purposes
        /*final PmActivitySnippet.Class activitySnippet = new PmActivitySnippet.Class();
        SnippetClass.addClass(activitySnippet);
        SnippetMenuConfig.INSTANCE.add(activitySnippet);*/

        SnippetClass.addClass(new PmReportSnippet.Class()); //necessary for showing pm analysis/reports in market data views
        SnippetClass.addClass(new PmPriceTeaserSnippet.Class());
        SnippetClass.addClass(new PmInstrumentSnippet.Class());
        SnippetClass.addClass(new PmSecuritySnippet.Class());
    }

    private void buildReportMenus(List<LayoutDesc> listReports,
            Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMap) {
        Firebug.log("PmWebSupport <buildReportMenus>");
        this.mapReportsByGuidId = createReportGuidMap(listReports);
        Firebug.log("PmWebSupport <buildReportMenus> done. registered " + this.mapReportsByGuidId.size() + " layouts");
        createReportMenuTrees(typeMap);
    }

    public LayoutMenuItem getReportMenuRootItem(InvestorItem.Type type) {
        return this.mapReportMenuRootItem.get(type);
    }

    private void createReportMenuTrees(
            Map<ShellMMType, DmxmlContext.Block<GetWorkspaceResponse>> typeMap) {
        Firebug.log("PmWebSupport <createReportMenuTrees>");
        this.mapReportMenuRootItem = new HashMap<>();
        this.mapReportDefaults = new HashMap<>();

        for (ShellMMType shellMMType : typeMap.keySet()) {
            final List<InvestorItem.Type> types = InvestorItem.Type.fromShellMMType(shellMMType);
            for (InvestorItem.Type type : types) {
                if (!this.mapReportMenuRootItem.containsKey(type)) {
                    final GetWorkspaceResponse workspaceResponse = typeMap.get(shellMMType).getResult();
                    final LayoutMenuItem rootItem = createReportMenuTree(type, type.name(),
                            workspaceResponse.getSheets(), toDefaultSheetsMap(workspaceResponse));
                    if (rootItem == null) {
                        throw new IllegalStateException("could not create rootItem for type " + type.getShellMMType().name() + // $NON-NLS$
                                "! check for layoutguid and/or existing children"); // $NON-NLS$
                    }
                    this.mapReportMenuRootItem.put(type, rootItem);
                }
            }
        }
        checkDefaultLayouts();
    }

    public static Map<String, Set<WorksheetDefaultMode>> toDefaultSheetsMap(
            GetWorkspaceResponse workspaceResponse) {
        final Map<String, Set<WorksheetDefaultMode>> defaultSheets = new HashMap<>();
        for (WorkspaceDefaultSheetDesc defaultSheetDesc : workspaceResponse.getDefaultSheets()) {
            final String nodeId = defaultSheetDesc.getNodeId();
            Set<WorksheetDefaultMode> modeSet = defaultSheets.get(nodeId);
            if (modeSet == null) {
                modeSet = new HashSet<>();
                defaultSheets.put(nodeId, modeSet);
            }
            modeSet.add(defaultSheetDesc.getDefaultMode());
        }
        return defaultSheets;
    }

    public static boolean isMainDefaultSheet(WorkspaceSheetDesc sheetDesc,
            Map<String, Set<WorksheetDefaultMode>> defaultSheetMap) {
        return isDefaultSheet(sheetDesc, defaultSheetMap, WorksheetDefaultMode.WDM_MAIN);
    }

    public static boolean isDefaultSheet(WorkspaceSheetDesc sheetDesc,
            Map<String, Set<WorksheetDefaultMode>> defaultSheetMap,
            WorksheetDefaultMode defaultMode) {
        assert sheetDesc != null : "parameter sheetDesc must not be null!";
        assert defaultSheetMap != null : "parameter defaultSheetMap must not be null!";
        assert defaultMode != null : "parameter defaultMode must not be null!";

        final Set<WorksheetDefaultMode> worksheetDefaultModes = defaultSheetMap.get(sheetDesc.getNodeId());
        final boolean defaultSheet = worksheetDefaultModes != null && worksheetDefaultModes.contains(defaultMode);
        if (defaultSheet) {
            Firebug.log("<PmWebSupport.isDefaultSheet> " + defaultMode + " " + sheetDesc.getNodeId() + " " + sheetDesc.getCaption());
        }
        return defaultSheet;
    }

    private void checkDefaultLayouts() {
        for (InvestorItem.Type type : InvestorItem.Type.values()) {
            if (type == InvestorItem.Type.ROOT) {
                continue;
            }
            if (this.mapReportDefaults.get(type) == null) {
                final LayoutMenuItem layoutMenuItem = this.mapReportMenuRootItem.get(type);
                if (layoutMenuItem != null && layoutMenuItem.getChildren() != null && !layoutMenuItem.getChildren().isEmpty()) {
                    Firebug.log("PmWebSupport <checkDefaultLayouts> setting a default layout for type " + type);
                    setDefault(type, layoutMenuItem.getChildren());
                }
                else {
                    Firebug.log("PmWebSupport <checkDefaultLayouts> no layouts defined for type " + type);
                }
            }
        }
    }

    private void setDefault(InvestorItem.Type type, List<LayoutMenuItem> layoutMenuItems) {
        if (layoutMenuItems == null || layoutMenuItems.isEmpty()) {
            throw new IllegalStateException("root item for type " + type + " has no child elements!"); // $NON-NLS$
        }
        for (LayoutMenuItem layoutMenuItem : layoutMenuItems) {
            if (layoutMenuItem.getChildren() != null && !layoutMenuItem.getChildren().isEmpty()) {
                setDefault(type, layoutMenuItem.getChildren());
                if (this.mapReportDefaults.containsKey(type)) {
                    return;
                }
            }
            else if (layoutMenuItem.getLayoutDesc() != null && StringUtil.hasText(layoutMenuItem.getLayoutDesc().getLayout().getGuid())) {
                this.mapReportDefaults.put(type, layoutMenuItem.getLayoutDesc());
                return;
            }
            else {
                throw new IllegalStateException("could not find a default layout for " + type); // $NON-NLS$
            }
        }
    }

    private LayoutMenuItem createReportMenuTree(InvestorItem.Type type, WorkspaceSheetDesc sheet,
            Map<String, Set<WorksheetDefaultMode>> defaultSheets) {
        if (sheet.getSheets().isEmpty()) {
            if (sheet.getLayoutType() != MMLayoutType.LTP_NONE && !StringUtil.hasText(sheet.getLayoutGuid())) {
                throw new IllegalStateException("sheet of layouttype " + sheet.getLayoutType().name() + " doesn't have a layoutguid " + sheet.getCaption()); // $NON-NLS$
            }
            final LayoutDesc layoutDesc = this.mapReportsByGuidId.get(sheet.getLayoutGuid());
            if (isMainDefaultSheet(sheet, defaultSheets)) {
                final LayoutDesc previousDefault = this.mapReportDefaults.put(type, layoutDesc);
                if (previousDefault != null) {
                    final String message = "multiple layoutDesc defaults for type " + type.toString() // $NON-NLS$
                            + ": " + previousDefault.getLayout().getLayoutName() + " - " + layoutDesc.getLayout().getLayoutName();
                    Firebug.log(message);
                    DebugUtil.logToServer(message);
                    throw new RuntimeException(message);
                }
            }
            return new LayoutMenuItem(sheet.getCaption(), layoutDesc);
        }
        return createReportMenuTree(type, sheet.getCaption(), sheet.getSheets(), defaultSheets);
    }

    private LayoutMenuItem createReportMenuTree(InvestorItem.Type type, String itemName,
            List<WorkspaceSheetDesc> sheets, Map<String, Set<WorksheetDefaultMode>> defaultSheets) {
        final LayoutMenuItem item = new LayoutMenuItem(itemName);
        for (WorkspaceSheetDesc sheet : sheets) {
            item.addChild(createReportMenuTree(type, sheet, defaultSheets));
        }
        return item.hasReports() ? item : new LayoutMenuItem("No workspace defined"); // $NON-NLS$
    }


    private Map<String, LayoutDesc> createReportGuidMap(List<LayoutDesc> list) {
        final Map<String, LayoutDesc> map = new HashMap<>(list.size());
        for (LayoutDesc layoutDesc : list) {
            map.put(layoutDesc.getLayout().getGuid(), layoutDesc);
        }
        return map;
    }

    public static boolean createInstance(AsyncCallback<ResponseType> callback) {
        if (instance == null) {
            instance = new PmWebSupport(callback);
            return true;
        }
        return false;
    }

    public LayoutDesc getReportByGuidId(String guid) {
        return this.mapReportsByGuidId.get(guid);
    }

    public LayoutDesc getReportDefault(InvestorItem.Type type) {
        return this.mapReportDefaults.get(type);
    }

    public LayoutGlobalMetaDataResponse getGlobalLayoutMetadata() {
        if (this.globalLayoutMetadata == null) {
            throw new IllegalStateException("this.globalLayoutMetadata == null!"); // $NON-NLS$
        }
        return this.globalLayoutMetadata;
    }

    public List<UserFieldCategory> getUserFieldDecls(ShellMMType shellMMType) {
        if (this.mapUserFieldDecls == null) {
            throw new IllegalStateException("this.mapUserFieldDecls == null!"); // $NON-NLS$
        }
        return this.mapUserFieldDecls.get(shellMMType);
    }

    private void injectPmWorkspaceDependency(String key, PageController pageController) {
        if (this.portraitsWithPmWorkspace.contains(key) && pageController instanceof AbstractPortraitController) {
            Firebug.log("<PmWebSupport.injectPmWorkspaceDependency> injecting WorkspaceSupportSymbolAdapter into " + key);
            ((AbstractPortraitController) pageController).addNavItemExtension(new WorkspaceSupportMetadataAware());
        }
    }

    private void loadPortraitsWithPmWorkspace() {
        Firebug.log("<PmWebSupport.loadPortraitsWithPmWorkspace>");
        final HashSet<String> targets = this.portraitsWithPmWorkspace;
        final JSONValue jsonValue = SessionData.INSTANCE.getGuiDef("pm-workspace-nav-targets").getValue(); //$NON-NLS$

        if (jsonValue != null) {
            final JSONArray jsonArray = jsonValue.isArray();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    final JSONString jsonString = jsonArray.get(i).isString();
                    if (jsonString != null) {
                        Firebug.log("<PmWebSupport.loadPortraitsWithPmWorkspace> adding " + jsonString.stringValue());
                        targets.add(jsonString.stringValue());
                    }
                }
            }
        }
    }

    public static class UserFieldCategory {
        public static final String DEFAULT_CATEGORY = I18n.I.none();

        private String name;

        private List<UserFieldDeclarationDesc> userFieldDeclarationDescs;

        private UserFieldCategory(String name) {
            this.name = name;
            this.userFieldDeclarationDescs = null;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<UserFieldDeclarationDesc> getUserFieldDeclarationDescs() {
            if (this.userFieldDeclarationDescs == null) {
                this.userFieldDeclarationDescs = new ArrayList<>();
            }
            return this.userFieldDeclarationDescs;
        }

        public boolean isDefaultCategory() {
            return DEFAULT_CATEGORY.equals(this.name);
        }
    }

    public static class UserFieldCategoryWithUserFields extends UserFieldCategory {
        private ArrayList<UserField> userFields;

        public UserFieldCategoryWithUserFields(String name) {
            super(name);
        }

        public List<UserField> getUserFields() {
            if (this.userFields == null) {
                this.userFields = new ArrayList<>();
            }
            return this.userFields;
        }
    }

    private static class UserFieldDeclarationDescComparator implements
            Comparator<UserFieldDeclarationDesc> {
        @Override
        public int compare(UserFieldDeclarationDesc o1, UserFieldDeclarationDesc o2) {
            if (o1 == o2) return 0;
            if (o1 != null && o2 == null) return 1;
            if (o1 == null) return -1;

            final int i1 = parseInt(o1.getDecl().getFieldCategorySortIndex());
            final int i2 = parseInt(o2.getDecl().getFieldCategorySortIndex());
            return (i1 < i2) ? -1 : ((i1 == i2) ? 0 : 1);
        }

        private int parseInt(String intString) {
            try {
                return Integer.parseInt(intString);
            } catch (NumberFormatException e1) {
                return Integer.MIN_VALUE;
            }
        }
    }

    private static class UserFieldCategoryComparator implements Comparator<UserFieldCategory> {
        @Override
        public int compare(UserFieldCategory o1, UserFieldCategory o2) {
            if (o1 == o2) return 0;
            if (o1 != null && o2 == null) return 1;
            if (o1 == null) return -1;

            if (UserFieldCategory.DEFAULT_CATEGORY.equals(o1.getName())) return -1;
            if (UserFieldCategory.DEFAULT_CATEGORY.equals(o2.getName())) return 1;

            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static class LayoutDocumentTypeComparator implements Comparator<LayoutDocumentType> {
        @Override
        public int compare(LayoutDocumentType o1, LayoutDocumentType o2) {
            if (o1 == o2) {
                return 0;
            }
            final String name1 = o1 != null ? o1.getName() != null ? o1.getName().toLowerCase() : null : null;
            final String name2 = o2 != null ? o2.getName() != null ? o2.getName().toLowerCase() : null : null;
            return CompareUtil.compare(name1, name2);
        }
    }

    public List<Language> getLanguages(){
        return getGlobalLayoutMetadata().getLanguages();
    }

    public String getDefaultLanguage(){
        return getGlobalLayoutMetadata().getDefaultLanguage();
    }
}