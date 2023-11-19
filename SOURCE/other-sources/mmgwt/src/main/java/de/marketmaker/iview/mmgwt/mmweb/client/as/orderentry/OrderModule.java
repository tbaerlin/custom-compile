/*
 * OrderModule.java
 *
 * Created on 30.10.12 08:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSessionContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.IsBrokingAllowedFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.IsPageControllerIsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.OrderResultCode;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class OrderModule implements PresenterDisposedHandler {
    private static final int ALLOCATE_CONTAINER_RETRIES = 3;

    private static OrderModule instance = null;
    private OrderSessionContainer orderSessionContainer;
    private boolean windowCloseHandlerRegistered = false;
    private boolean showOrderDialogLocked = false;

    private OrderModule(AsyncCallback<ResponseType> callback) {
        createOrderSessionContainer(callback);
    }

    @Override
    public void onPresenterDisposed(PresenterDisposedEvent event) {
        unlockShowOrderDialog();
    }

    private void createOrderSessionContainer(final AsyncCallback<ResponseType> callback) {
        OrderMethods.INSTANCE.allocateOrderSessionContainer(new AsyncCallback<OrderSessionContainer>() {
            @Override
            public void onSuccess(OrderSessionContainer result) {
                Firebug.debug("<OrderModule.createOrderSessionContainer.onSuccess> handle=" + result.getHandle());
                OrderModule.this.orderSessionContainer = result;
                registerOrderModuleShutdownHandler();
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("<OrderModule.createOrderSessionContainer> onFailure", caught);
                callback.onFailure(caught);
            }
        });
    }

    public static boolean createInstance(AsyncCallback<ResponseType> callback) {
        if(!Selector.AS_ORDERING.isAllowed()) {
            callback.onFailure(new RuntimeException(I18n.I.noAccessToFeature()));
        }
        if (instance == null || instance.orderSessionContainer == null) {
            instance = new OrderModule(callback);
            return true;
        }
        callback.onSuccess(null);
        return false;
    }

    private static void checkModuleInitialized() {
        if (instance == null || instance.orderSessionContainer == null) {
            throw new IllegalStateException("OrderModule instance has not yet been initialized!"); //$NON-NLS$
        }
    }

    public static void showByInvestorId(OrderEntryContext context) {
        checkModuleInitialized();
        if(instance.isShowOrderDialogLocked()) {
            return;
        }
        instance.createAndShowOrderDialogByInvestorId(context);
    }

    public static void showByDepotId(OrderEntryContext context) {
        checkModuleInitialized();
        if(instance.isShowOrderDialogLocked()) {
            return;
        }
        instance.createAndShowOrderDialogByDepotId(context);
    }

    public static void showByIsin(OrderEntryContext context) {
        checkModuleInitialized();
        if(instance.isShowOrderDialogLocked()) {
            return;
        }
        instance.lockShowOrderDialog();

        final OrderFromSecurityPresenter ocp = new OrderFromSecurityPresenter();
        instance.addPresenterDisposedHandler(ocp);
        ocp.show(context);
    }

    public static void requestOrderBookByDepotId(final String depotId, final AsyncCallback<IsPageControllerIsWidget> callback) {
        checkModuleInitialized();

        Firebug.debug("<OrderModule.requestOrderBookByDepotId> depotId=" + depotId);

        OrderMethods.INSTANCE.allocateOrderSession(instance.orderSessionContainer, depotId, new AsyncCallback<OrderSession>() {
            @Override
            public void onSuccess(final OrderSession session) {
                instance.handleBrokerLogin(null, session, new HandleBrokerLoginCommands() {
                    @Override
                    public void onAuthenticated() {
                        Firebug.debug("<requestOrderBookByDepotId.onAuthenticated>");
                        instance.createOrderBook(session, callback);
                    }

                    @Override
                    public void onLoginSuccessful() {
                        Firebug.debug("<requestOrderBookByDepotId.onLoginSuccessful>");
                        requestOrderBookByDepotId(depotId, callback);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (throwable instanceof OrderMethods.SessionContainerNotFoundException) {
                    instance.orderSessionContainer = null;

                    instance.createOrderSessionContainer(new AsyncCallback<ResponseType>() {
                        @Override
                        public void onSuccess(ResponseType result) {
                            requestOrderBookByDepotId(depotId, callback);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            OrderMethods.INSTANCE.showFailureMessage(caught);
                        }
                    });
                } else {
                    OrderMethods.INSTANCE.showFailureMessage(throwable);
                }
            }
        });
    }

    private void createOrderBook(OrderSession session, AsyncCallback<IsPageControllerIsWidget> callback) {
        try {
            callback.onSuccess(OrderPresenterFactory.createOrderBookPresenter(session));
        }
        catch(Exception e) {
             callback.onFailure(e);
        }
    }

    private void createAndShowOrderDialogByDepotId(final OrderEntryContext context) {
        createAndShowOrderDialogByDepotId(context, 0);
    }

    private void createAndShowOrderDialogByDepotId(final OrderEntryContext context, final int allocContainerRetries) {
        Firebug.debug("<OrderModule.createAndShowOrderDialogByDepotId> " + context.getParameterMap());

        lockShowOrderDialog();

        OrderMethods.INSTANCE.allocateOrderSession(this.orderSessionContainer, context.getParameterMap().getDepotId(), new AsyncCallback<OrderSession>() {
            @Override
            public void onSuccess(OrderSession session) {
                handleBrokerLogin(context, session, new HandleBrokerLoginOrderDialogCommands(OrderModule.this, session, context));
            }

            @Override
            public void onFailure(Throwable throwable) {
                unlockShowOrderDialog();
                if (allocContainerRetries < ALLOCATE_CONTAINER_RETRIES && throwable instanceof OrderMethods.SessionContainerNotFoundException) {
                    OrderModule.this.orderSessionContainer = null;

                    createOrderSessionContainer(new AsyncCallback<ResponseType>() {
                        @Override
                        public void onSuccess(ResponseType result) {
                            createAndShowOrderDialogByDepotId(context, allocContainerRetries + 1);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            OrderMethods.INSTANCE.showFailureMessage(caught);
                        }
                    });
                }
                else {
                    if(throwable instanceof OrderMethods.SessionContainerNotFoundException) {
                        Firebug.error("<OrderModule.createAndShowOrderDialogByDepotId> failed to allocate an order session container even after " + ALLOCATE_CONTAINER_RETRIES + " retries", throwable);
                    }

                    OrderMethods.INSTANCE.showFailureMessage(throwable);
                }
            }
        });
    }

    private void handleBrokerLogin(final OrderEntryContext context, final OrderSession session, HandleBrokerLoginCommands commands) {
        try {
            switch (session.getSessionState()) {
                case SS_AUTHENTICATED:
                    commands.onAuthenticated();
                    break;
                case SS_INITIALISED:
                    final String user = SessionData.INSTANCE.getUser().getLogin();
                    showBrokerLogin(user, session, context, null, commands);
                    break;
                case SS_VALIDATED:
                case SS_NONE:
                default:
                    throw new IllegalStateException("SessionState not expected: " + session.getSessionState()); //$NON-NLS$
            }
        }
        catch(Exception e) {
            unlockShowOrderDialog();
            final String message = StringUtil.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
            OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryError(SafeHtmlUtils.htmlEscapeAllowEntities(message))));
        }
    }

    protected void showBrokerLogin(String user, final OrderSession session, OrderEntryContext context, SafeHtml retryMessage, final HandleBrokerLoginCommands command) {
        final BrokerLoginPresenter presenter = new BrokerLoginPresenter(new BrokerLoginView());
        presenter.setCallback(new BrokerLoginCallback(this, session, context, command));

        if(session instanceof OrderSession.OrderSessionBHLKGS) {
            final String appName = ((OrderSession.OrderSessionBHLKGS)session).getFeatures().getApplicationName();

            final String title;
            if(StringUtil.hasText(appName)) {
                title = I18n.I.orderEntryBrokerLoginTitleBHLKGS() + " (" + appName + ")"; //$NON-NLS$
            }
            else {
                Firebug.debug("<OrderModule.showBrokerLogin> KGS session features do not contain an applicationName");
                title = I18n.I.orderEntryBrokerLoginTitleBHLKGS();
            }
            presenter.withHeading(title);
        }

        presenter.show(user, retryMessage);
    }

    private void createAndShowOrderDialogByInvestorId(final OrderEntryContext orderEntryContext) {
        Firebug.debug("<OrderModule.createAndShowOrderDialogByInvestorId> " + orderEntryContext.getParameterMap());

        final String investorId = orderEntryContext.getParameterMap().getInvestorId();

        if(StringUtil.hasText(investorId)) {
            lockShowOrderDialog();
            final String activityInstanceId = orderEntryContext.getParameterMap().getActivityInstanceId();

            final DmxmlContext context = new DmxmlContext();
            context.setCancellable(false);

            final IsBrokingAllowedFeature brokingAllowedFeature = new IsBrokingAllowedFeature(context);
            brokingAllowedFeature.setId(investorId);

            context.issueRequest(new AsyncCallback<ResponseType>() {
                @Override
                public void onSuccess(ResponseType result) {
                    if(!brokingAllowedFeature.isResponseOk()) {
                        unlockShowOrderDialog();
                        OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(I18n.I.orderEntryLoadingDepotsOfInvestorFailed()));
                        return;
                    }

                    if (StringUtil.hasText(activityInstanceId) && !brokingAllowedFeature.isActivityBrokingAllowed()
                            || !StringUtil.hasText(activityInstanceId) && !brokingAllowedFeature.isStandaloneBrokingAllowed()) {
                        unlockShowOrderDialog();
                        OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(I18n.I.orderEntryOrcSecurityAccountNoRightForOrderEntry()));
                    }
                    else if (!StringUtil.hasText(brokingAllowedFeature.getDepotId())) {
                        unlockShowOrderDialog();
                        OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(I18n.I.orderEntryNoDepotsForInvestorFound()));
                    }
                    else {
                        orderEntryContext.getParameterMap().setDepotId(brokingAllowedFeature.getDepotId());
                        createAndShowOrderDialogByDepotId(orderEntryContext);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    unlockShowOrderDialog();
                    OrderMethods.INSTANCE.showFailureMessage(caught, I18n.I.orderEntryLoadingDepotsOfInvestorFailed());
                }
            });
        }
    }

    private void registerOrderModuleShutdownHandler() {
        Firebug.debug("Registering ShutdownHandler that closes OrderSessionContainers and OrderSessions on logout or closing browser window");

        if(this.windowCloseHandlerRegistered) return;

        EventBusRegistry.get().addHandler(ShutdownEvent.getType(), new ShutdownHandler() {
            @Override
            public void onShutdown(ShutdownEvent event) {
                try {
                    OrderMethods.INSTANCE.closeAllOrderSessions(OrderModule.instance.orderSessionContainer);
                } catch (Exception e) {
                    Firebug.warn("Exception while closing all order sessions", e);
                }

                try {
                    OrderMethods.INSTANCE.closeOrderSessionContainer(OrderModule.instance.orderSessionContainer, new AsyncCallback<OrderSessionContainer>() {
                        @Override
                        public void onSuccess(OrderSessionContainer result) {
                            Firebug.warn("OrderSessionContainer successfully closed");
                            OrderModule.instance.orderSessionContainer = null;
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Firebug.warn("Failed to close OrderSessionContainer", caught);
                        }
                    });
                } catch (Exception e) {
                    Firebug.warn("Exception while closing all order session containers", e);
                }
            }
        });

        this.windowCloseHandlerRegistered = true;
    }

    private void lockShowOrderDialog() {
        this.showOrderDialogLocked = true;
    }

    private void unlockShowOrderDialog() {
        this.showOrderDialogLocked = false;
    }

    public boolean isShowOrderDialogLocked() {
        return showOrderDialogLocked;
    }

    private Display.Presenter createBrokingPresenter(OrderSession session) {
        final Display.Presenter presenter = OrderPresenterFactory.createBrokingPresenter(session);
        addPresenterDisposedHandler(presenter);
        return presenter;
    }

    private void addPresenterDisposedHandler(HasPresenterDisposedHandlers handlers) {
        handlers.addPresenterDisposedHandler(this);
    }

    public interface HandleBrokerLoginCommands {
        void onAuthenticated();
        void onLoginSuccessful();
    }

    public static class HandleBrokerLoginOrderDialogCommands implements HandleBrokerLoginCommands {
        private final OrderModule orderModule;
        private final OrderSession orderSession;
        private final OrderEntryContext orderEntryContext;

        public HandleBrokerLoginOrderDialogCommands(OrderModule orderModule, OrderSession orderSession,
                                                    OrderEntryContext context) {
            this.orderModule = orderModule;
            this.orderSession = orderSession;
            this.orderEntryContext = context;
        }

        @Override
        public void onAuthenticated() {
            final Display.Presenter presenter = this.orderModule.createBrokingPresenter(this.orderSession);
            presenter.show(this.orderEntryContext);
        }

        @Override
        public void onLoginSuccessful() {
            final String depotId = this.orderSession.getAccountInfo().getSecurityAccount().getId();
            this.orderEntryContext.getParameterMap().setDepotId(depotId);
            this.orderModule.createAndShowOrderDialogByDepotId(this.orderEntryContext);
        }
    }

    private static class BrokerLoginCallback implements BrokerLoginDisplay.Callback {
        private final OrderModule orderModule;
        private final OrderSession orderSession;
        private final HandleBrokerLoginCommands command;
        private final OrderEntryContext orderEntryContext;

        public BrokerLoginCallback(OrderModule orderModule, OrderSession orderSession, OrderEntryContext context, HandleBrokerLoginCommands command) {
            this.orderModule = orderModule;
            this.orderSession = orderSession;
            this.orderEntryContext = context;
            this.command = command;
        }

        @Override
        public void onPassword(final String user, final String password) {
            OrderMethods.INSTANCE.loginBroker(this.orderSession, user, password, new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    BrokerLoginCallback.this.command.onLoginSuccessful();
                }

                @Override
                public void onFailure(Throwable caught) {
                    if (caught instanceof OrderMethods.PmxmlOrderEntryResponseException) {
                        final OrderMethods.PmxmlOrderEntryResponseException pmoere =
                                (OrderMethods.PmxmlOrderEntryResponseException) caught;

                        if(OrderResultCode.ORC_BROKER_LOGIN_FAILED == pmoere.getOrderResultCode()) {
                            BrokerLoginCallback.this.orderModule.showBrokerLogin(
                                    user,
                                    BrokerLoginCallback.this.orderSession,
                                    BrokerLoginCallback.this.orderEntryContext,
                                    OrderMethods.INSTANCE.getFormattedMessageForException(pmoere, null),
                                    BrokerLoginCallback.this.command);
                            return;
                        }
                    }

                    BrokerLoginCallback.this.orderModule.unlockShowOrderDialog();
                    OrderMethods.INSTANCE.showFailureMessage(caught);
                }
            });
        }

        @Override
        public void onCancel() {
            BrokerLoginCallback.this.orderModule.unlockShowOrderDialog();
            OrderMethods.INSTANCE.closeOrderSession(BrokerLoginCallback.this.orderSession, new AsyncCallback<OrderSession>() {
                @Override
                public void onSuccess(OrderSession result) {
                    Firebug.info("<BrokerLoginView.onCancel> OrderSession closed");
                }

                @Override
                public void onFailure(Throwable caught) {
                    Firebug.info("<BrokerLoginView.onCancel> Closing order session failed");
                }
            });
        }
    }
}