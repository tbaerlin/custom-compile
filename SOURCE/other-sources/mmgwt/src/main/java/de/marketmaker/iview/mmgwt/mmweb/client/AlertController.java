/*
* AlertController.java
*
* Created on 24.09.2008 13:57:39
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ALTGetAlerts;
import de.marketmaker.iview.dmxml.ALTGetUser;
import de.marketmaker.iview.dmxml.ALTOK;
import de.marketmaker.iview.dmxml.Alert;
import de.marketmaker.iview.dmxml.AlertExecution;
import de.marketmaker.iview.dmxml.AlertNotification;
import de.marketmaker.iview.dmxml.AlertUser;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.Action;
import de.marketmaker.iview.mmgwt.mmweb.client.table.ActionHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AlertUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.LimitsUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author Oliver Flege
 */
public class AlertController extends AbstractPageController implements PageController, Command {

    private final AlertView view;

    private DmxmlContext.Block<ALTGetUser> blockUser;

    private DmxmlContext.Block<ALTGetAlerts> blockAlerts;

    private DmxmlContext.Block<ALTOK> ackBlock;

    private DmxmlContext.Block<ALTOK> deleteBlock;

    private DmxmlContext.Block<ALTOK> updateBlock;

    private DmxmlContext.Block<ALTOK> updateUserBlock;

    public static final ActionHandler<Alert> ALERT_ACTION_HANDLER = new ActionHandler<Alert>() { // must be placed before INSTANCE = new AlertController()
        @Override
        public List<Action> getActions() {
            return Arrays.asList(Action.EDIT, Action.DELETE, Action.ACKNOWLEDGE);
        }

        @Override
        public boolean isActionApplicableTo(Action action, Alert data) {
            return action != Action.ACKNOWLEDGE || isWithUnacknowledgedNotification(data);
        }

        @Override
        public void doAction(Action action, Alert data) {
            if (action == Action.EDIT) {
                INSTANCE.edit(data);
            }
            else if (action == Action.DELETE) {
                INSTANCE.delete(data);
            }
            else if (action == Action.ACKNOWLEDGE) {
                INSTANCE.acknowledge(data);
            }
        }
    };

    public static final AlertController INSTANCE = new AlertController(); // must be placed after ALERT_ACTION_HANDLER

    private AlertUser alertUser = new AlertUser();

    private AlertController() {
        this.context.setCancellable(false);

        this.blockUser = createBlock(this.context, "ALT_GetUser"); // $NON-NLS-0$
        this.blockAlerts = createBlock(this.context, "ALT_GetAlerts"); // $NON-NLS-0$

        this.ackBlock = createBlock(new DmxmlContext(), "ALT_AckNotifications"); // $NON-NLS-0$
        this.deleteBlock = createBlock(new DmxmlContext(), "ALT_DeleteAlert"); // $NON-NLS-0$

        this.updateBlock = createBlock(new DmxmlContext(), "ALT_UpdateAlert"); // $NON-NLS-0$
        // update is an atomic sequence of delete and insert
        this.updateBlock.setParameter("deleteExisting", "true"); // $NON-NLS-0$ $NON-NLS-1$

        this.updateUserBlock = createBlock(new DmxmlContext(), "ALT_UpdateUser"); // $NON-NLS-0$

        this.view = new AlertView();
    }

    private static boolean isWithUnacknowledgedNotification(Alert data) {
        if (data.getExecution() == null || data.getExecution().isEmpty()) {
            return false;
        }
        for (AlertExecution execution : data.getExecution()) {
            if (execution.getNotification() == null || execution.getNotification().isEmpty()) {
                continue;
            }
            for (AlertNotification notification : execution.getNotification()) {
                if (!notification.isAcknowledged()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> getUnacknowledgedNotificationIds(Alert data) {
        final List<String> result = new ArrayList<>();
        if (data.getExecution() == null || data.getExecution().isEmpty()) {
            return result;
        }
        for (AlertExecution execution : data.getExecution()) {
            if (execution.getNotification() == null || execution.getNotification().isEmpty()) {
                continue;
            }
            for (AlertNotification notification : execution.getNotification()) {
                if (!notification.isAcknowledged()) {
                    result.add(notification.getId());
                }
            }
        }
        return result;
    }

    @Override
    public ContentContainer getContentContainer() {
        return AbstractMainController.INSTANCE.getView();
    }

    private static <V extends BlockType> DmxmlContext.Block<V> createBlock(
            final DmxmlContext context, String key) {
        final DmxmlContext.Block<V> result = context.addBlock(key);
        result.setParameter("vwdUserId", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        return result;
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        getContentContainer().setContent(this.view);
        refresh();
    }

    @Override
    protected void onResult() {
        Firebug.warn(getContentContainer().getContent() != null ? getContentContainer().getContent().getClass().getSimpleName() : "null");
        Firebug.warn(this.view != null ? this.view.getClass().getSimpleName() : "null");

        if (getContentContainer().isShowing(this.view)) {
            updateView();
        }
        if (this.blockAlerts.isResponseOk()) {
            EventBusRegistry.get().fireEvent(new LimitsUpdatedEvent());
        }
    }

    private void updateView() {
        final List<Alert> alerts = getAlertInformation();
        if (alerts == null) {
            this.view.show(DefaultTableDataModel.NULL);
            return;
        }
        final DefaultTableDataModel model = DefaultTableDataModel.create(alerts,
                new AbstractRowMapper<Alert>() {
                    private String iid = null;
                    private int n;

                    @Override
                    public Object[] mapRow(Alert alert) {
                        if (!alert.getInstrumentdata().getIid().equals(this.iid)) {
                            this.n = 0;
                        }
                        this.iid = alert.getInstrumentdata().getIid();
                        return new Object[]{
                                alert,
                                Integer.toString(++this.n),
                                alert.getName(),
                                new QuoteWithInstrument(alert.getInstrumentdata(), alert.getQuotedata()),
                                alert.getQuotedata().getMarketName(),
                                AlertUtil.getLimitFieldName(alert),
                                alert.getLowerBoundary(),
                                alert.getLowerBoundaryPercent(),
                                alert.getUpperBoundary(),
                                alert.getUpperBoundaryPercent(),
                                alert.getReferenceValue(),
                                alert.getCreated(),
                                alert
                        };
                    }
                });
        this.view.show(model);
    }

    private void onLoadInitialAlertInformation() {
        this.context.removeBlock(this.blockUser);
        if (this.blockUser.isResponseOk()) {
            this.alertUser = this.blockUser.getResult().getUser();
        }
    }

    public String getEmailAddress() {
        return this.alertUser.getEmailAddress1();
    }

    @Override
    public void execute() {
        AbstractMainController.INSTANCE.updateProgress(I18n.I.loginAtLimitServer());

        this.context.setCancellable(false);
        this.context.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (throwable != null) {
                    Firebug.error("loadInitialAlertInformation failed", throwable); // $NON-NLS-0$
                }
                onLoadInitialAlertInformation();
                AbstractMainController.INSTANCE.runInitSequence();
            }
        });
    }

    public List<Alert> getAlertInformation(String iid) {
        if (!this.blockAlerts.isResponseOk()) {
            return null;
        }
        ArrayList<Alert> result = null;
        for (Alert alert : this.blockAlerts.getResult().getAlert()) {
            if (alert.getQuotedata() != null
                    && (iid == null || alert.getInstrumentdata().getIid().equals(iid))) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(alert);
            }
        }
        if (result != null && result.size() > 1) {
            Collections.sort(result, AlertUtil.COMPARE_BY_INSTRUMENTNAME_AND_DATE);
        }
        return result;
    }

    public List<Alert> getAlertInformation() {
        return getAlertInformation(null);
    }

    public List<Alert> getUnacknowledgedAlertInformation() {
        final List<Alert> alertInformation = getAlertInformation();
        if(alertInformation == null) {
            return null;
        }
        if(alertInformation.isEmpty()) {
            return Collections.emptyList();
        }

        List<Alert> filtered = new ArrayList<>();
        for (Alert alert : alertInformation) {
            if(isWithUnacknowledgedNotification(alert)) {
                filtered.add(alert);
            }
        }
        return filtered;
    }

    public void edit(Alert a) {
        AlertEditForm.INSTANCE.show(a, new QuoteWithInstrument(a.getInstrumentdata(), a.getQuotedata()));
    }

    public void delete(final Alert a) {
        Dialog.confirm(I18n.I.deleteLimitConfirmation(a.getName()), () -> doDelete(a));
    }

    private void doDelete(Alert a) {
        this.deleteBlock.setParameter("alertId", a.getId()); // $NON-NLS-0$
        this.deleteBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (deleteBlock.isResponseOk()) {
                    ActionPerformedEvent.fire("X_ALT_DEL"); // $NON-NLS-0$
                    AbstractMainController.INSTANCE.showMessage(I18n.I.limitDeleted());
                    refresh();
                }
                else {
                    AbstractMainController.INSTANCE.showError(I18n.I.error());
                }
            }
        });
    }

    public void acknowledge(Alert a) {
        final List<String> ids = getUnacknowledgedNotificationIds(a);
        if (ids.isEmpty()) {
            Firebug.log("Found no unacknowledeg alerts?!"); // $NON-NLS-0$
            return;
        }
        this.ackBlock.setParameters("notificationId", ids.toArray(new String[ids.size()])); // $NON-NLS-0$
        this.ackBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (ackBlock.isResponseOk()) {
                    ActionPerformedEvent.fire("X_ALT_ACK");                             // $NON-NLS-0$
                    AbstractMainController.INSTANCE.showMessage(I18n.I.receptionConfirmed());
                    refresh();
                }
                else {
                    AbstractMainController.INSTANCE.showError(I18n.I.error());
                }
            }
        });
    }

    public void update(final Alert updated) {
        if (updated == null) { // update was cancelled
            return;
        }
        this.updateBlock.setParameter("alertId", updated.getId()); // $NON-NLS-0$
        this.updateBlock.setParameter("fieldId", updated.getFieldId()); // $NON-NLS-0$
        this.updateBlock.setParameter("name", updated.getName()); // $NON-NLS-0$
        this.updateBlock.setParameter("referenceValue", updated.getReferenceValue()); // $NON-NLS-0$
        this.updateBlock.setParameter("infoText", updated.getInfoText()); // $NON-NLS-0$
        this.updateBlock.setParameter("lowerBoundary", updated.getLowerBoundary()); // $NON-NLS-0$
        this.updateBlock.setParameter("lowerBoundaryPercent", updated.getLowerBoundaryPercent()); // $NON-NLS-0$
        this.updateBlock.setParameter("upperBoundary", updated.getUpperBoundary()); // $NON-NLS-0$
        this.updateBlock.setParameter("upperBoundaryPercent", updated.getUpperBoundaryPercent()); // $NON-NLS-0$
        this.updateBlock.setParameter("email", updated.isEmail()); // $NON-NLS-0$
        this.updateBlock.setParameter("sms", updated.isSms()); // $NON-NLS-0$
        this.updateBlock.setParameter("validUntil", updated.getValidUntil()); // $NON-NLS-0$
        this.updateBlock.setParameter("symbol", updated.getQuotedata().getQid()); // $NON-NLS-0$
        this.updateBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (updateBlock.isResponseOk()) {
                    ActionPerformedEvent.fire(updated.getId() != null ? "X_ALT_UPD" : "X_ALT_INS"); // $NON-NLS-0$ $NON-NLS-1$
                    AbstractMainController.INSTANCE.showMessage(I18n.I.limitChanged());
                    refresh();
                }
                else {
                    AbstractMainController.INSTANCE.showError(I18n.I.error());
                }
            }
        });
    }

    public void saveEmailAddress(final String s) {
        if (s.equals(getEmailAddress())) {
            return;
        }
        this.updateUserBlock.setParameter("emailAddress1", s); // $NON-NLS-0$
        this.updateUserBlock.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                AbstractMainController.INSTANCE.showError(I18n.I.emailAddressNotSaved());
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                AbstractMainController.INSTANCE.showMessage(I18n.I.emailAddressSaved());
                alertUser.setEmailAddress1(s);
            }
        });
    }
}
