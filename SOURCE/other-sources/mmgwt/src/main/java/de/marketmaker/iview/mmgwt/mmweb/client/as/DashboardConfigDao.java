package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidationFeature;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.ConfigDao;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardController;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardSavedCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.JsonUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PrivFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.CreateLayoutRequest;
import de.marketmaker.iview.pmxml.CreateLayoutResponse;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.LayoutErrorResponse;
import de.marketmaker.iview.pmxml.LayoutGuidRequest;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.MMLayout;
import de.marketmaker.iview.pmxml.MMLayoutError;
import de.marketmaker.iview.pmxml.MMLayoutType;
import de.marketmaker.iview.pmxml.SaveLayoutRequest;
import de.marketmaker.iview.pmxml.SaveLayoutResponse;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 21.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DashboardConfigDao extends ConfigDao {
    public static final int NAME_MAX_CHAR_COUNT = 50;
    public static final ValidationFeature.Validator NAME_VALIDATOR = new ValidationFeature.Validator() {
        @Override
        public boolean isValid(String value, List<String> messages) {
            if (value == null || value.isEmpty() || value.length() > NAME_MAX_CHAR_COUNT) {
                messages.add(I18n.I.from1ToNChars(NAME_MAX_CHAR_COUNT));
                return false;
            }
            return true;
        }
    };

    private static abstract class NameErrorPromptCallback extends Dialog.PromptCallback {
        @Override
        protected boolean isValid(String value) {
            return NAME_VALIDATOR.isValid(value, new ArrayList<String>());
        }
    }

    private final Map<String, DashboardConfig> dashboardsById = new HashMap<>();
    private final Map<String, LayoutDesc> layoutsById = new HashMap<>();
    private final Map<String, List<DashboardConfig>> dashboardsByRole = new HashMap<>();

    public static void initialize(List<LayoutDesc> layouts, String privateZoneId, Command privsResultCommand) {
        ConfigDao.setInstance(new DashboardConfigDao(layouts, privateZoneId, privsResultCommand));
    }

    private DashboardConfigDao(List<LayoutDesc> layoutDescs, String privateZoneId, Command privsResultCommand) {
        final DmxmlContext privContext = new DmxmlContext();
        privContext.setCancellable(false);
        final HashMap<String, PrivFeature> privFeatures = new HashMap<>();

        for (LayoutDesc layoutDesc : layoutDescs) {
            final MMLayout layout = layoutDesc.getLayout();
            final String id = layout.getGuid();
            final DashboardConfig config = layoutDesc.getBlob() == null || layoutDesc.getBlob().length == 0
                    ? new DashboardConfig()
                    : JsonUtil.fromJson(new String(layoutDesc.getBlob()));
            //noinspection ConstantConditions
            config.setId(id);
            initializeRole(config, layoutDesc);
            config.setName(layout.getLayoutName());
            config.setAccess(layoutDesc.getLayout().getPrimaryZoneId().equals(privateZoneId) ? DashboardConfig.Access.PRIVATE : DashboardConfig.Access.PUBLIC);

            this.layoutsById.put(id, layoutDesc);
            this.dashboardsById.put(id, config);

            final PrivFeature priv = new PrivFeature(privContext, MMClassIndex.CI_TMM_LAYOUT, UMRightBody.UMRB_EDIT_MASTER_FILE_DATA, UMRightBody.UMRB_DELETE);
            priv.setId(layout.getID());
            privFeatures.put(id, priv);

            addDashboardByRole(config);
        }
        handlePrivs(privContext, privFeatures, privsResultCommand);
    }

    private void handlePrivs(DmxmlContext privContext, final HashMap<String, PrivFeature> privFeatures, final Command privsResultCommand) {
        privContext.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                Notifications.add(I18n.I.serverError(), PermStr.DASHBOARD_ERROR_GET_PRIV_FAILED.value());
                Firebug.error("could not get privileges", throwable); // $NON-NLS$
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                for (String id : privFeatures.keySet()) {
                    mapPrivEditById.put(id, privFeatures.get(id).allowed(UMRightBody.UMRB_EDIT_MASTER_FILE_DATA));
                    mapPrivDeleteById.put(id, privFeatures.get(id).allowed(UMRightBody.UMRB_DELETE));
                }
                privsResultCommand.execute();
            }
        });
    }

    private void initializeRole(DashboardConfig config, LayoutDesc layoutDesc) {
        final List<ShellMMType> shellMMTypes = layoutDesc.getShellMMTypes();
        if(shellMMTypes == null || shellMMTypes.isEmpty()) {
            config.setRoles(Collections.singletonList(ConfigDao.DASHBOARD_ROLE_GLOBAL));
            return;
        }
        if(shellMMTypes.size() == 1) {
            config.setRoles(Collections.singletonList(shellMMTypes.get(0).value()));
            return;
        }
        final ArrayList<String> values = new ArrayList<>(shellMMTypes.size());
        for (ShellMMType shellMMType : shellMMTypes) {
            values.add(shellMMType.value());
        }
        config.setRoles(values);
    }

    private List<ShellMMType> getShellMMTypes(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        if(roles.size() == 1) {
            return Collections.singletonList(toShellMMType(roles.get(0)));
        }

        final ArrayList<ShellMMType> shellMMTypes = new ArrayList<>();
        for (String role : roles) {
            shellMMTypes.add(toShellMMType(role));
        }
        return shellMMTypes;
    }

    private ShellMMType toShellMMType(String role) {
        for (ShellMMType shellMMType : ShellMMType.values()) {
            if (role.equals(shellMMType.value())) {
                return shellMMType;
            }
        }
        return null;
    }

    private void addDashboardByRole(DashboardConfig config) {
        for (String role : config.getRoles()) {
            List<DashboardConfig> list = this.dashboardsByRole.get(role);
            if (list == null) {
                list = new ArrayList<>();
                this.dashboardsByRole.put(role, list);
            }
            list.add(config);
        }
    }

    @Override
    public void save(final DashboardConfig config, final DashboardSavedCallback callback) {
        final LayoutDesc layoutDesc = this.layoutsById.get(config.getId());
        if (layoutDesc == null) {
            throw new NullPointerException("DashboardConfigDao <save> no layoutDesc found for id: " + config.getId()); // $NON-NLS$
        }
        Firebug.info("DashboardConfigDao <saveConfig> guid: " + layoutDesc.getLayout().getGuid());
        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(false);
        final DmxmlContext.Block<SaveLayoutResponse> block = dmxmlContext.addBlock("PM_SaveLayout"); // $NON-NLS$
        final SaveLayoutRequest request = new SaveLayoutRequest();
        layoutDesc.getLayout().setLayoutName(config.getName());
        layoutDesc.setBlobGiven(true);
        layoutDesc.setBlob(JsonUtil.toJson(config).getBytes());
        request.setLayout(layoutDesc);
        block.setParameter(request);
        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                Notifications.add(I18n.I.error(), PermStr.DASHBOARD_ERROR_ANY.value(throwable.toString()));
                Firebug.error("couldn't save layout (failure)", throwable);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                if (!block.isResponseOk()) {
                    final String error = block.getError().getCode() + ": " + block.getError().getDescription(); // $NON-NLS$
                    Notifications.add(I18n.I.error(), PermStr.DASHBOARD_ERROR_ANY.value(error));
                    Firebug.error("couldn't save layout (response not ok): " + error);
                } else {
                    final SaveLayoutResponse result = block.getResult();
                    switch (result.getCode()) {
                        case MMLE_NONE:
                            callback.onDashboardSaved(config.getId());
                            break;
                        case MMLE_NAMEERROR:
                            handleNameError(result, config, new NameErrorPromptCallback() {
                                @Override
                                public void execute(String value) {
                                    config.setName(value);
                                    save(config, callback);
                                }
                            });
                            return;
                        default:
                            handleErrorsDefault(result);
                    }
                }
            }
        });
        updateDashboardMaps(config);
    }

    @Override
    public void saveNew(final DashboardConfig config, final DashboardSavedCallback callback) {
        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(false);
        final DmxmlContext.Block<CreateLayoutResponse> block = dmxmlContext.addBlock("PM_CreateLayout"); // $NON-NLS$
        final CreateLayoutRequest request = new CreateLayoutRequest();
        request.setLayoutName(config.getName());
        request.setBlob(JsonUtil.toJson(config).getBytes());
        final List<ShellMMType> shellMMTypes = getShellMMTypes(config.getRoles());
        if (shellMMTypes != null && !shellMMTypes.isEmpty()) {
            request.setNoInput(false);
            request.getShellMMTypes().addAll(shellMMTypes);
        }
        else {
            request.setNoInput(true);
        }
        final String copySourceDashboardId = DashboardController.getCopySourceDashboardId(config.getId());
        if (copySourceDashboardId != null) {
            request.setSourceLayoutGuid(copySourceDashboardId);
        }
        request.setLayoutType(MMLayoutType.LTP_WEBGUI_DASHBOARD);
        request.setToPrivate(true);
        block.setParameter(request);
        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                Notifications.add(I18n.I.error(), PermStr.DASHBOARD_ERROR_ANY.value(throwable.toString()));
                Firebug.error("couldn't create layout (config)", throwable);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                if (!block.isResponseOk()) {
                    final String error = block.getError().getCode() + ": " + block.getError().getDescription(); // $NON-NLS$
                    Notifications.add(I18n.I.error(), I18n.I.dashboardErrorAny(error));
                    Firebug.error("couldn't create layout (config): " + error);
                }
                else {
                    final CreateLayoutResponse result = block.getResult();
                    switch(result.getCode()) {
                        case MMLE_NONE:
                            onDashboardSaved(config, result.getLayout(), callback);
                            break;
                        case MMLE_NAMEERROR:
                            handleNameError(result, config, new NameErrorPromptCallback() {
                                @Override
                                public void execute(String value) {
                                    config.setName(value);
                                    saveNew(config, callback);
                                }
                            });
                            return;
                        default:
                            handleErrorsDefault(result);
                    }
                }
            }
        });
    }

    private void handleNameError(SaveLayoutResponse result, final DashboardConfig config, NameErrorPromptCallback callback1) {
        Dialog.prompt(I18n.I.saveDashboard(), PermStr.DASHBOARD_ERROR_DUPLICATE_NAME.value(config.getName()), I18n.I.name(), result.getNameRecommendation(), null, NAME_VALIDATOR, callback1);
    }

    private String handleErrorsDefault(LayoutErrorResponse result) {
        final String msg = result.getMsg();
        final MMLayoutError code = result.getCode();
        final String message = StringUtil.hasText(msg) ? msg : I18n.I.code() + " [" + code.value() + "]"; // $NON-NLS$
        Notifications.add(I18n.I.serverError(), PermStr.DASHBOARD_ERROR_ANY.value(message));
        return message;
    }

    private void onDashboardSaved(DashboardConfig config, LayoutDesc layoutDesc, DashboardSavedCallback callback) {
        final String id = layoutDesc.getLayout().getGuid();
        config.setId(id);
        this.layoutsById.put(id, layoutDesc);
        this.dashboardsById.put(id, config);
        this.mapPrivEditById.put(id, true);
        this.mapPrivDeleteById.put(id, true);
        addDashboardByRole(config);
        callback.onDashboardSaved(id);
    }

    private void updateDashboardMaps(DashboardConfig config) {
        this.dashboardsById.put(config.getId(), config);

        for (String role : config.getRoles()) {
            List<DashboardConfig> list = this.dashboardsByRole.get(role);
            if (list == null) {
                list = new ArrayList<>();
                list.add(config);
                this.dashboardsByRole.put(role, list);
                return;
            }

            for (int i = 0; i < list.size(); i++) {
                final DashboardConfig dc = list.get(i);
                if (dc == config) {
                    return;
                }
                if (dc.getId().equals(config.getId())) {
                    list.set(i, config);
                    return;
                }
            }
            list.add(config);
        }
    }

    @Override
    public DashboardConfig getConfigById(final String id) {
        return this.dashboardsById.get(id);
    }

    @Override
    public List<DashboardConfig> getConfigsByRole(String role) {
        List<DashboardConfig> list = this.dashboardsByRole.get(role);
        if (PrivacyMode.isActive()) {
            list = onlyCustomerDesktopAllowed(list);
        }
        return list == null
                ? new ArrayList<DashboardConfig>()
                : list;
    }

    private List<DashboardConfig> onlyCustomerDesktopAllowed(List<DashboardConfig> list) {
        if (list == null) {
            return null;
        }
        final ArrayList<DashboardConfig> result = new ArrayList<>();
        for (DashboardConfig dc : list) {
            final LayoutDesc layoutDesc = this.layoutsById.get(dc.getId());
            if (layoutDesc == null) {
                continue;
            }
            if (layoutDesc.getLayout().isCustomerDesktopAllowed()) {
                result.add(dc);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        Firebug.info("DashboardConfigDao <delete> guid: " + id);
        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(false);
        final DmxmlContext.Block<LayoutErrorResponse> block = dmxmlContext.addBlock("PM_DeleteLayout"); // $NON-NLS$
        final LayoutGuidRequest request = new LayoutGuidRequest();
        request.setLayoutGuid(id);
        block.setParameter(request);
        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                Notifications.add(I18n.I.error(), PermStr.DASHBOARD_ERROR_ANY.value(throwable.toString()));
                Firebug.error("couldn't delete layout (failure)", throwable);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                if (!block.isResponseOk()) {
                    final String error = block.getError().getCode() + ": " + block.getError().getDescription(); // $NON-NLS$
                    Notifications.add(I18n.I.error(), I18n.I.dashboardErrorAny(error));
                    Firebug.error("couldn't delete layout (response not ok): " + error);
                }
                else if (block.getResult().getCode() != MMLayoutError.MMLE_NONE) {
                    final String message = handleErrorsDefault(block.getResult());
                    Firebug.error("couldn't delete layout (pm error): " + message);
                }
            }
        });

        // maintain dashboardsById and dashboardsByRole
        final DashboardConfig removed = this.dashboardsById.remove(id);
        if (removed != null && removed.getRoles() != null && !removed.getRoles().isEmpty()) {
            for (String role : removed.getRoles()) {
                final List<DashboardConfig> list = this.dashboardsByRole.get(role);
                if (list != null) {
                    list.remove(removed);
                }
            }
        }
    }
}