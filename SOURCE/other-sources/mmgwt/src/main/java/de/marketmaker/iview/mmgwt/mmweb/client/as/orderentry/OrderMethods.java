/*
 * OrderMethods.java
 *
 * Created on 30.10.12 10:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryServiceAsync;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryServiceException;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSessionContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ImplementedOrderModules;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AllocateOrderSessionContainerDataRequest;
import de.marketmaker.iview.pmxml.AllocateOrderSessionContainerDataResponse;
import de.marketmaker.iview.pmxml.AllocateOrderSessionDataRequest;
import de.marketmaker.iview.pmxml.AllocateOrderSessionDataResponse;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.CancelOrderRequest;
import de.marketmaker.iview.pmxml.CancelOrderResponse;
import de.marketmaker.iview.pmxml.ChangeOrderDataRequest;
import de.marketmaker.iview.pmxml.ChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.CloseOrderSessionContainerDataRequest;
import de.marketmaker.iview.pmxml.CloseOrderSessionContainerDataResponse;
import de.marketmaker.iview.pmxml.CloseOrderSessionDataRequest;
import de.marketmaker.iview.pmxml.CloseOrderSessionDataResponse;
import de.marketmaker.iview.pmxml.DataResponse;
import de.marketmaker.iview.pmxml.GetIPOListRequest;
import de.marketmaker.iview.pmxml.GetIPOListResponse;
import de.marketmaker.iview.pmxml.GetOpenOrderSessionsDataRequest;
import de.marketmaker.iview.pmxml.GetOpenOrderSessionsDataResponse;
import de.marketmaker.iview.pmxml.GetOrderDetailsRequest;
import de.marketmaker.iview.pmxml.GetOrderDetailsResponse;
import de.marketmaker.iview.pmxml.GetOrderbookRequest;
import de.marketmaker.iview.pmxml.GetOrderbookResponse;
import de.marketmaker.iview.pmxml.LoginBrokerRequest;
import de.marketmaker.iview.pmxml.LoginBrokerResponse;
import de.marketmaker.iview.pmxml.LookupSecurityDataRequest;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderResultCode;
import de.marketmaker.iview.pmxml.OrderResultMSGType;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.OrderValidationServerityType;
import de.marketmaker.iview.pmxml.OrderValidationType;
import de.marketmaker.iview.pmxml.SendOrderDataRequest;
import de.marketmaker.iview.pmxml.SendOrderDataResponse;
import de.marketmaker.iview.pmxml.SessionHandle;
import de.marketmaker.iview.pmxml.ValidateChangeOrderDataRequest;
import de.marketmaker.iview.pmxml.ValidateChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidateOrderDataRequest;
import de.marketmaker.iview.pmxml.ValidateOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidationMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class OrderMethods {
    public static final OrderMethods INSTANCE = GWT.create(OrderMethods.class);

    private DmxmlContext createDmxmlContext() {
        return createDmxmlContext(false);
    }

    private DmxmlContext createDmxmlContext(boolean cancellable) {
        final DmxmlContext context = new DmxmlContext();
        context.setCancellable(cancellable);
        return context;
    }

    private DmxmlContext.Block<AllocateOrderSessionContainerDataResponse> createAllocateOrderSessionContainerBlock() {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<AllocateOrderSessionContainerDataResponse> result =
                context.addBlock("OE_AllocSessionContainer"); // $NON-NLS$
        final AllocateOrderSessionContainerDataRequest containerRequest = new AllocateOrderSessionContainerDataRequest();
        result.setParameter(containerRequest);
        return result;
    }

    private DmxmlContext.Block<AllocateOrderSessionDataResponse> createAllocateOrderSession(final String containerHandle, final String depotId) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<AllocateOrderSessionDataResponse> block = context.addBlock("OE_AllocSession"); // $NON-NLS$
        final AllocateOrderSessionDataRequest sessionDataRequest = new AllocateOrderSessionDataRequest();

        sessionDataRequest.setContainerHandle(containerHandle);
        sessionDataRequest.setDepotId(depotId);

        block.setParameter(sessionDataRequest);

        return block;
    }

    private DmxmlContext.Block<GetOpenOrderSessionsDataResponse>
    createGetOpenOrderSessions(final String containerHandle) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<GetOpenOrderSessionsDataResponse> block = context.addBlock("OE_GetOpenSessions"); //$NON-NLS$
        final GetOpenOrderSessionsDataRequest request = new GetOpenOrderSessionsDataRequest();

        request.setHandle(containerHandle);
        block.setParameter(request);

        return block;
    }

    private DmxmlContext.Block<CloseOrderSessionDataResponse> createCloseOrderSession(final String sessionHandle) {
        return addCloseOrderSessionBlock(createDmxmlContext(), sessionHandle);
    }

    private DmxmlContext.Block<CloseOrderSessionDataResponse> addCloseOrderSessionBlock(DmxmlContext context, String sessionHandle) {
        final DmxmlContext.Block<CloseOrderSessionDataResponse> block = context.addBlock("OE_CloseSession"); //$NON-NLS$
        final CloseOrderSessionDataRequest closeSessionRequest = new CloseOrderSessionDataRequest();

        closeSessionRequest.setSessionHandle(sessionHandle);
        block.setParameter(closeSessionRequest);

        return block;
    }

    private DmxmlContext.Block<CloseOrderSessionContainerDataResponse>
    createCloseOrderSessionContainerBlock(final String containerHandle) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<CloseOrderSessionContainerDataResponse> block =
                context.addBlock("OE_CloseSessionContainer"); //$NON-NLS$
        final CloseOrderSessionContainerDataRequest closeSessionContainerRequest =
                new CloseOrderSessionContainerDataRequest();
        closeSessionContainerRequest.setHandle(containerHandle);
        block.setParameter(closeSessionContainerRequest);

        return block;
    }

    private DmxmlContext.Block<LookupSecurityDataResponse>
    createLookupSecurityBlock(final String sessionHandle, final OrderTransaktionType type, final String isin) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<LookupSecurityDataResponse> block = context.addBlock("OE_LookupSecurity"); //$NON-NLS$
        final LookupSecurityDataRequest lookupSecurityDataRequest = new LookupSecurityDataRequest();
        lookupSecurityDataRequest.setSessionHandle(sessionHandle);
        lookupSecurityDataRequest.setOrderTyp(type);
        lookupSecurityDataRequest.setISIN(isin);
        block.setParameter(lookupSecurityDataRequest);

        return block;
    }

    private DmxmlContext.Block<ValidateOrderDataResponse>
    createValidateOrderBlock(String handle, OrderDataType order, List<ValidationMessage> validationMessages) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<ValidateOrderDataResponse> block = context.addBlock("OE_ValidateOrder"); //$NON-NLS$
        final ValidateOrderDataRequest request = new ValidateOrderDataRequest();
        request.setHandle(handle);
        request.setOrder(order);

        final List<ValidationMessage> requestValidationMessages = request.getValidationMsgList();
        if(validationMessages != null) {
            requestValidationMessages.addAll(validationMessages);
        }

        block.setParameter(request);

        return block;
    }

    private DmxmlContext.Block<ValidateChangeOrderDataResponse>
    createValidateChangeOrderBlock(String handle, OrderDataType order, List<ValidationMessage> validationMessages) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<ValidateChangeOrderDataResponse> block = context.addBlock("OE_ValidateChangeOrder"); //$NON-NLS$
        final ValidateChangeOrderDataRequest request = new ValidateChangeOrderDataRequest();
        request.setHandle(handle);
        request.setOrder(order);

        final List<ValidationMessage> requestValidationMessages = request.getValidationMsgList();
        if(validationMessages != null) {
            requestValidationMessages.addAll(validationMessages);
        }

        block.setParameter(request);

        return block;
    }

    private DmxmlContext.Block<SendOrderDataResponse>
    createSendOrderBlock(String handle, OrderDataType order, List<ValidationMessage> validationMessages) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<SendOrderDataResponse> block = context.addBlock("OE_SendOrder"); //$NON-NLS$
        final SendOrderDataRequest request = new SendOrderDataRequest();
        request.setHandle(handle);
        request.setOrder(order);

        final List<ValidationMessage> requestValidationMessages = request.getValidationMsgList();
        if(validationMessages != null) {
            requestValidationMessages.addAll(validationMessages);
        }

        block.setParameter(request);

        return block;
    }

    private DmxmlContext.Block<ChangeOrderDataResponse>
    createChangeOrderBlock(String handle, OrderDataType order, List<ValidationMessage> validationMessages) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<ChangeOrderDataResponse> block = context.addBlock("OE_ChangeOrder"); //$NON-NLS$
        final ChangeOrderDataRequest request = new ChangeOrderDataRequest();
        request.setHandle(handle);
        request.setOrder(order);

        final List<ValidationMessage> requestValidationMessages = request.getValidationMsgList();
        if(validationMessages != null) {
            requestValidationMessages.addAll(validationMessages);
        }

        block.setParameter(request);

        return block;
    }

    private DmxmlContext.Block<CancelOrderResponse>
    createCancelOrderBlock(String handle, OrderDataType order, List<ValidationMessage> validationMessages) {
        final DmxmlContext context = createDmxmlContext();
        final DmxmlContext.Block<CancelOrderResponse> block = context.addBlock("OE_CancelOrder"); //$NON-NLS$
        final CancelOrderRequest request = new CancelOrderRequest();
        request.setHandle(handle);
        request.setOrder(order);

        final List<ValidationMessage> requestValidationMessages = request.getValidationMsgList();
        if(validationMessages != null) {
            requestValidationMessages.addAll(validationMessages);
        }

        block.setParameter(request);

        return block;
    }

    public void allocateOrderSessionContainer(final AsyncCallback<OrderSessionContainer> callback) {
        sendRequest(createAllocateOrderSessionContainerBlock(), new AsyncCallback<AllocateOrderSessionContainerDataResponse>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(AllocateOrderSessionContainerDataResponse allocateOrderSessionContainerDataResponse) {
                callback.onSuccess(new OrderSessionContainer(allocateOrderSessionContainerDataResponse));
            }
        }, I18n.I.orderEntryOrcTimeout());
    }

    public void allocateOrderSession(final OrderSessionContainer containerHandle, final String depotId, final AsyncCallback<OrderSession> callback) {
        sendRequest(createAllocateOrderSession(containerHandle.getHandle(), depotId),
                new AsyncCallback<AllocateOrderSessionDataResponse>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(AllocateOrderSessionDataResponse allocateOrderSessionDataResponse) {
                callback.onSuccess(OrderSession.createOrderSession(allocateOrderSessionDataResponse));
            }
        }, I18n.I.orderEntryOrcTimeout());
    }

    public void getOpenOrderSessions(final OrderSessionContainer container, final AsyncCallback<GetOpenOrderSessionsDataResponse> callback) {
        sendRequest(createGetOpenOrderSessions(container.getHandle()), callback, I18n.I.orderEntryOrcTimeout());
    }

    public void closeOrderSession(final OrderSession orderSession, final AsyncCallback<OrderSession> callback) {
        sendRequest(createCloseOrderSession(orderSession.getHandle()), new AsyncCallback<CloseOrderSessionDataResponse>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(CloseOrderSessionDataResponse closeOrderSessionDataResponse) {
                callback.onSuccess(orderSession);
            }
        }, orderSession.getMessages().orderEntryOrcTimeout());
    }

    public void closeOrderSessionContainer(final OrderSessionContainer orderSessionContainer,
                                    final AsyncCallback<OrderSessionContainer> callback) {
        sendRequest(createCloseOrderSessionContainerBlock(orderSessionContainer.getHandle()),
                new AsyncCallback<CloseOrderSessionContainerDataResponse>() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(CloseOrderSessionContainerDataResponse closeOrderSessionContainerDataResponse) {
                callback.onSuccess(orderSessionContainer);
            }
        }, I18n.I.orderEntryOrcTimeout());
    }

    void closeAllOrderSessions(final OrderSessionContainer orderSessionContainer) {
        if(orderSessionContainer != null) {
            getOpenOrderSessions(orderSessionContainer, new AsyncCallback<GetOpenOrderSessionsDataResponse>() {
                @Override
                public void onSuccess(GetOpenOrderSessionsDataResponse result) {
                    closeAllOrderSessions(result.getSessions());
                }

                @Override
                public void onFailure(Throwable caught) {
                    Firebug.warn("Getting open order sessions of container '" + orderSessionContainer.getHandle() + "' failed!", caught);
                }
            });
        }
    }

    private void closeAllOrderSessions(List<SessionHandle> sessions) {
        if(sessions == null || sessions.isEmpty()) {
            Firebug.debug("<OrderMethods.closeAllOrderSessions> Nothing to do: session list is null or empty");
            return;
        }

        Firebug.debug("<OrderMethods.closeAllOrderSessions> sessions.size=" + sessions.size());
        final DmxmlContext context = createDmxmlContext();

        final List<DmxmlContext.Block<CloseOrderSessionDataResponse>>blocks =
                new ArrayList<>(sessions.size());

        for(SessionHandle session : sessions) {
            Firebug.debug("Adding block OE_CloseOrderSession for session " + session.getHandle());
            blocks.add(addCloseOrderSessionBlock(context, session.getHandle()));
        }

        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onSuccess(ResponseType result) {
                for(DmxmlContext.Block<CloseOrderSessionDataResponse> block : blocks) {
                    if (block.isResponseOk() && OrderResultCode.ORC_OK  ==  block.getResult().getCode()) {
                        Firebug.debug("Session closed.");
                    }
                    else if (block.isResponseOk()){
                        Firebug.debug("Closing session failed. OrderResultCode is not Ok.");
                    }
                    else {
                        Firebug.debug("Closing session failed. Response is not Ok.");
                    }
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("<OrderMethods.closeAllOrderSessions> issue request failed", caught);
            }
        });
    }

    public void lookupSecurity(final OrderSession orderSession, final OrderTransaktionType type, final String isin, final AsyncCallback<LookupSecurityDataResponse> callback) {
        sendRequest(createLookupSecurityBlock(orderSession.getHandle(), type, isin), callback, orderSession.getMessages().orderEntryOrcTimeout());
    }

    public void loginBroker(final OrderSession orderSession, final String user, final String password, final AsyncCallback<Void> callback) {
        final LoginBrokerRequest request = new LoginBrokerRequest();
        request.setSessionHandle(orderSession.getHandle());
        request.setUser(user);
        request.setPassword(password);

        OrderEntryServiceAsync.App.getInstance().login(request, new AsyncCallback<LoginBrokerResponse>() {
            @Override
            public void onSuccess(LoginBrokerResponse result) {
                final OrderResultCode code = result.getCode();
                if (code == OrderResultCode.ORC_OK) {
                    callback.onSuccess(null);
                }
                else if(code == OrderResultCode.ORC_BACKEND_TIME_OUT) {
                    callback.onFailure(new PmxmlTimeoutResponseException(orderSession.getMessages().orderEntryOrcTimeout()));
                }
                else {
                    //not necessary to handle RPC timeouts here, because in case of a timeout,
                    //the server side throws an OrderEntryServiceException, which is handled
                    //via onFailure.
                    callback.onFailure(new PmxmlOrderEntryResponseException(result));
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    public void validateOrder(final OrderSession orderSession,
                       final OrderDataType order,
                       final List<ValidationMessage> validationMessages,
                       final AsyncCallback<ValidateOrderDataResponse> callback) {

        sendRequest(createValidateOrderBlock(orderSession.getHandle(), order, validationMessages), order, callback,
                orderSession.getMessages().orderEntryOrcTimeout());
    }

    public void sendOrder(final OrderSession orderSession,
                       final OrderDataType order,
                       final List<ValidationMessage> validationMessages,
                       final AsyncCallback<SendOrderDataResponse> callback) {

        sendRequest(createSendOrderBlock(orderSession.getHandle(), order, validationMessages), order, callback,
                orderSession.getMessages().orderEntryOrcTimeoutSendOrder());
    }

    public void changeOrder(final OrderSession orderSession,
                          final OrderDataType order,
                          final List<ValidationMessage> validationMessages,
                          final AsyncCallback<ChangeOrderDataResponse> callback) {

        sendRequest(createChangeOrderBlock(orderSession.getHandle(), order, validationMessages), order, callback,
                orderSession.getMessages().orderEntryOrcTimeoutChangeOrder());
    }

    public void validateChangeOrder(final OrderSession orderSession,
                              final OrderDataType order,
                              final List<ValidationMessage> validationMessages,
                              final AsyncCallback<ValidateChangeOrderDataResponse> callback) {

        sendRequest(createValidateChangeOrderBlock(orderSession.getHandle(), order, validationMessages), order, callback,
                orderSession.getMessages().orderEntryOrcTimeout());
    }

    public void cancelOrder(OrderSession orderSession,
                               OrderDataType order,
                               List<ValidationMessage> validationMessages,
                               AsyncCallback<CancelOrderResponse> callback) {

        sendRequest(createCancelOrderBlock(orderSession.getHandle(), order, validationMessages), order, callback,
                orderSession.getMessages().orderEntryOrcTimeoutCancelOrder());
    }

    protected <T extends DataResponse> void sendRequest(final DmxmlContext.Block<T> block,
                                                        final OrderDataType order,
                                                        final AsyncCallback<T> callback,
                                                        final String timeoutMessage) { //may be a map in the future

        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ResponseType result) {
                Firebug.debug("block.responseOk " + block.isResponseOk());
                Firebug.debug("block.state " + block.getState());

                if (block.isResponseOk()) {
                    final OrderResultCode orderResultCode = block.getResult().getCode();
                    Firebug.debug("block.result.code " + orderResultCode);

                    switch (orderResultCode) {
                        case ORC_OK:
                            callback.onSuccess(block.getResult());
                            break;
                        case ORC_VALIDATE_ORDER_FAILED:
                            callback.onFailure(PmxmlValidateOrderResponseException.create(block.getResult(), order));
                            break;
                        case ORC_SESSION_CONTAINER_NOT_FOUND:
                            callback.onFailure(new SessionContainerNotFoundException(block.getResult()));
                            break;
                        case ORC_BACKEND_TIME_OUT:
                            callback.onFailure(new PmxmlTimeoutResponseException(timeoutMessage));
                            break;
                        default:
                            callback.onFailure(new PmxmlOrderEntryResponseException(block.getResult()));
                    }
                }
                else {
                    callback.onFailure(createThrowable(block, timeoutMessage));
                }
            }
        });
    }

    private <T extends DataResponse> Throwable createThrowable(DmxmlContext.Block<T> block, String timeoutMessage) {
        final ErrorType error = block.getError();
        if(error != null && error.getCode() != null) {
            switch(error.getCode().trim()) {
                case "timeout":         // $NON-NLS$
                case "remote_timeout":  // $NON-NLS$
                    return new PmxmlTimeoutResponseException(timeoutMessage);
            }
        }
        return new PmxmlErrorResponseException(block);
    }

    public void queryOrderBook(final OrderSession orderSession, final GetOrderbookRequest request, final AsyncCallback<GetOrderbookResponse> callback) {
        request.setHandle(orderSession.getHandle());

        final DmxmlContext context = createDmxmlContext(true);
        final DmxmlContext.Block<GetOrderbookResponse> block = context.addBlock("OE_QueryOrderBook"); //$NON-NLS$
        block.setParameter(request);

        sendRequest(block, callback, orderSession.getMessages().orderEntryOrcTimeout());
    }

    public void getOrderDetails(final OrderSession orderSession, String orderNumber, final AsyncCallback<GetOrderDetailsResponse> callback) {
        Firebug.debug("<OrderMethods.getOrderDetails>");
        final DmxmlContext context = createDmxmlContext(true);
        final DmxmlContext.Block<GetOrderDetailsResponse> block = context.addBlock("OE_GetOrderDetails"); //$NON-NLS$

        final GetOrderDetailsRequest request = new GetOrderDetailsRequest();
        request.setHandle(orderSession.getHandle());
        request.setOrderNumber(orderNumber);

        block.setParameter(request);
        sendRequest(block, callback, orderSession.getMessages().orderEntryOrcTimeout());
    }

    public void getIPOList(final OrderSession orderSession, final AsyncCallback<GetIPOListResponse> callback) {
        Firebug.debug("<OrderMethods.getIPOList>");
        final DmxmlContext context = createDmxmlContext(true);
        final DmxmlContext.Block<GetIPOListResponse> block = context.addBlock("OE_GetIPOList"); //$NON-NLS$

        final GetIPOListRequest request = new GetIPOListRequest();
        request.setHandle(orderSession.getHandle());

        block.setParameter(request);
        sendRequest(block, callback, orderSession.getMessages().orderEntryOrcTimeout());
    }

    protected <T extends DataResponse> void sendRequest(final DmxmlContext.Block<T> block,
                                                        final AsyncCallback<T> callback,
                                                        final String timeoutMessage) {

        Firebug.debug("<OrderMethods.sendRequest>");

        block.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ResponseType result) {
                Firebug.debug("block.responseOk " + block.isResponseOk());
                Firebug.debug("block.state " + block.getState());

                if (block.isResponseOk()) {
                    final OrderResultCode orderResultCode = block.getResult().getCode();
                    Firebug.debug("block.result.code " + orderResultCode);

                    switch(orderResultCode) {
                        case ORC_OK:
                            callback.onSuccess(block.getResult());
                            break;
                        case ORC_SESSION_CONTAINER_NOT_FOUND:
                            callback.onFailure(new SessionContainerNotFoundException(block.getResult()));
                            break;
                        case ORC_BACKEND_TIME_OUT:
                            callback.onFailure(new PmxmlTimeoutResponseException(timeoutMessage));
                            break;
                        default:
                            callback.onFailure(new PmxmlOrderEntryResponseException(block.getResult()));
                    }
                }
                else {
                    callback.onFailure(createThrowable(block, timeoutMessage));
                }
            }
        });
    }

    public void showValidationErrorMessage(SafeHtml message) {
        Dialog.warning(message);
    }

    public void showFailureMessage(SafeHtml message) {
        logToServer(message.asString(), null);
        Dialog.error(message);
    }

    public void showFailureMessage(Throwable caught) {
        showFailureMessage(caught, "--"); //$NON-NLS$
    }

    public void showFailureMessage(Throwable caught, String payload) {
        final SafeHtml message = getFormattedMessageForException(caught, payload);

        logToServer(message.asString(), caught);
        Dialog.error(message);
    }

    public SafeHtml getFormattedMessageForException(Throwable caught, String payload) {
        //TODO: Should we use the original messages provided by the Order Entry backend,
        //TODO: when these are internationalized?
        //TODO: decide how we present the external message.
        final SafeHtml message;
        if(caught == null) {
            message = SafeHtmlUtils.fromString(payload);
        }
        else if (caught instanceof PmxmlOrderEntryResponseException) {
            final PmxmlOrderEntryResponseException pmoere = (PmxmlOrderEntryResponseException)caught;
            final OrderResultCode orc = pmoere.getOrderResultCode();

            final String internalMessage;
            switch(orc) {
                case ORC_SESSION_CONTAINER_NOT_FOUND:
                case ORC_SESSION_NOT_FOUND:
                    internalMessage = I18n.I.orderEntryOrcSessionNotFound();
                    break;
                case ORC_SECURITY_ACCOUNT_NOT_FOUND:
                    internalMessage = I18n.I.orderEntryOrcSecurityAccountNotFound();
                    break;
                case ORC_SECURITY_ACCOUNT_HAS_NO_BANK:
                    internalMessage = I18n.I.orderEntryOrcSecurityAccountHasNoBank();
                    break;
                case ORC_BROKER_LOGIN_FAILED:
                    //This case is currently not used, because the message is directly displayed by the BrokerLogin dialog.
                    internalMessage = I18n.I.orderEntryOrcBrokerLoginFailed();
                    break;
                case ORC_SECURITY_NOT_FOUND:
                    internalMessage = I18n.I.orderEntryOrcSecurityNotFound(SafeHtmlUtils.htmlEscape(payload));
                    break;
                case ORC_VALIDATE_ORDER_FAILED:
                    internalMessage = I18n.I.orderEntryOrcValidateOrderFailed();
                    break;
                case ORC_SEND_ORDER_FAILED:
                    internalMessage = I18n.I.orderEntryOrcSendOrderFailed();
                    break;
                case ORC_SECURITY_ACCOUNT_NO_RIGHT_FOR_ORDER_ENTRY:
                    internalMessage = I18n.I.orderEntryOrcSecurityAccountNoRightForOrderEntry();
                    break;
                case ORC_SECURITY_TYPE_NOT_PERMITTED:
                    internalMessage = I18n.I.orderEntryOrcSecurityTypeNotPermitted(SafeHtmlUtils.htmlEscape(payload));
                    break;
                case ORC_GET_SESSION_FEATURES_FAILED:
                    internalMessage = I18n.I.orderEntryOrcGetSessionFeaturesFailed();
                    break;
                case ORC_GET_ORDERBOOK_FAILED:
                    internalMessage = I18n.I.orderEntryOrcGetOrderBookFailed();
                    break;
                case ORC_GET_ORDER_DETAIL_FAILED:
                    internalMessage = I18n.I.orderEntryOrcGetOrderDetailFailed();
                    break;
                case ORC_CANCEL_ORDER_FAILED:
                    internalMessage = I18n.I.orderEntryOrcCancelOrderFailed();
                    break;
                case ORC_SESSION_NOT_AUTHENTICATED:
                    internalMessage = I18n.I.orderEntryOrcSessionNotAuthenticated();
                    break;
                case ORC_NO_CACHED_ORDERBOOK_ENTRY_FOUND:
                    internalMessage = I18n.I.orderEntryOrcNoCachedOrderbookEntryFound();
                    break;
                case ORC_GET_SECURITY_INFOS_FAILED:
                    internalMessage = I18n.I.orderEntryOrcGetSecurityInfosFailed();
                    break;
                case ORC_SECURITY_ACCOUNT_DEACTIVATED:
                    internalMessage = I18n.I.orderEntryOrcSecurityAccountDeactivated();
                    break;
                case ORC_NO_STOCK:
                    internalMessage = I18n.I.orderEntryOrcNoStock(SafeHtmlUtils.htmlEscape(payload));
                    break;
                case ORC_BACKEND_TIME_OUT:
                    //Only given for the sake of completeness, because timeouts should be signaled with a special exception type.
                    internalMessage = I18n.I.orderEntryOrcTimeout();
                    break;
                default:
                    internalMessage = I18n.I.orderEntryOrcAnyError(SafeHtmlUtils.htmlEscape(pmoere.getDescription()));
            }

            if(!StringUtil.hasText(pmoere.getExternalMessage())) {
                message = SafeHtmlUtils.fromTrustedString(internalMessage);
            }
            else {
                final String externalMessage = pmoere.getExternalMessage();
                final String escaped = SafeHtmlUtils.htmlEscape(externalMessage);
                final String composed = I18n.I.orderEntryOrcAnyErrorWithExternalMessage(internalMessage, escaped);
                message = SafeHtmlUtils.fromTrustedString(composed);
            }
        }
        else if (caught instanceof PmxmlTimeoutResponseException) {
            message = SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryOrcAnyError(caught.getMessage()));
        }
        else {
            // special timeout handling only for #loginBroker
            if (caught instanceof OrderEntryServiceException && "remote_timeout".equals(((OrderEntryServiceException) caught).getCode())) { // $NON-NLS$
                message = SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryOrcTimeout());
            }
            else {
                message = SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryError(SafeHtmlUtils.htmlEscape(caught.getMessage())));
            }
        }
        return message;
    }

    @SuppressWarnings("unchecked")
    public <T extends OrderDataType> void handleBackendValidationFailed(OrderValidationMessagePresenter presenter, final Throwable caught , final OrderValidationCallback<T> callback) {
        if (caught instanceof OrderMethods.PmxmlValidateOrderResponseException) {
            final OrderMethods.PmxmlValidateOrderResponseException pmvore = (OrderMethods.PmxmlValidateOrderResponseException) caught;

            final List<ValidationMessage>validationMessages = pmvore.getValidationMessages();

            if (!validationMessages.isEmpty()) {
                final OrderValidationMessagePresenter.Callback adapter = new OrderValidationMessagePresenter.Callback() {
                    @Override
                    public void onProceed(List<ValidationMessage> editedValidationMessages) {
                        callback.onProceedAfterValidateOrderFailed((T)pmvore.getReceivedDataType(), editedValidationMessages);
                    }

                    @Override
                    public void onCancel() {
                        callback.onCancelAfterValidateOrderFailed(validationMessages);
                    }
                };

                presenter.show(validationMessages, adapter);
            }
            else {
                callback.onAnyExceptionAfterValidateOrderFailed(caught);
            }
        }
        else {
            callback.onAnyExceptionAfterValidateOrderFailed(caught);
        }
    }

    private void logToServer(String message, Throwable caught) {
        DebugUtil.logToServer(new LogBuilder().add("failureMessage", message).toString(), caught);
    }

    public static class PmxmlOrderEntryResponseException extends RuntimeException {
        private final OrderResultCode orderResultCode;
        private final OrderResultMSGType orderResultMSGType;
        private final String externalMessage;
        private String message;

        public PmxmlOrderEntryResponseException(String description, OrderResultCode orderResultCode, OrderResultMSGType orderResultMSGType) {
            this(description, orderResultCode, orderResultMSGType, null);
        }

        public PmxmlOrderEntryResponseException(String description, OrderResultCode orderResultCode, OrderResultMSGType orderResultMSGType, String externalMessage) {
            super(description);

            this.orderResultCode = orderResultCode;
            this.orderResultMSGType = orderResultMSGType;
            this.externalMessage = externalMessage;
        }

        public PmxmlOrderEntryResponseException(DataResponse dataResponse) {
            this(dataResponse.getDescription(), dataResponse.getCode(), dataResponse.getMSGType(), dataResponse.getExternalMessage());
        }

        @Override
        public String getMessage() {
            if(!StringUtil.hasText(this.message)) {
                final String description = super.getMessage();
                StringBuilder msg = new StringBuilder();

                if(this.orderResultMSGType != null) {
                    msg.append(orderResultMSGType.name()).append(": "); //$NON-NLS$
                }

                if(this.orderResultCode != null) {
                    msg.append("Result: ").append(this.orderResultCode.name()); //$NON-NLS$
                }

                if(StringUtil.hasText(description)) {
                    if(this.orderResultCode != null) {
                        msg.append(", "); //$NON-NLS$
                    }
                    msg.append(description);
                }

                if(StringUtil.hasText(this.externalMessage)) {
                    if(StringUtil.hasText(description)) {
                        msg.append(" "); //$NON-NLS$
                    }
                    msg.append("external message: ").append(this.externalMessage); //$NON-NLS$
                }
                this.message = msg.toString();
            }

            return this.message;
        }

        public String getDescription() {
            return super.getMessage();
        }

        public String getExternalMessage() {
            return this.externalMessage;
        }

        public OrderResultCode getOrderResultCode() {
            return orderResultCode;
        }

        public OrderResultMSGType getOrderResultMSGType() {
            return orderResultMSGType;
        }
    }

    public static class PmxmlTimeoutResponseException extends RuntimeException {
        public PmxmlTimeoutResponseException(String message) {
            super(message);
        }
    }

    public static class PmxmlErrorResponseException extends RuntimeException {
        private final String level;
        private final String code;
        private final String key;
        private final String correlationId;
        private String message;

        public PmxmlErrorResponseException(DmxmlContext.Block block) {
            super(block.getError() != null ? block.getError().getDescription() : "" + block.getState());

            final ErrorType error = block.getError();
            if(error != null) {
                this.level = error.getLevel();
                this.code = error.getCode();
                this.key = error.getKey();
                this.correlationId = error.getCorrelationId();
            }
            else {
                this.level = "";
                this.code = "";
                this.key = "";
                this.correlationId = "";
            }
        }

        @Override
        public String getMessage() {
            if(!StringUtil.hasText(this.message)) {
                final String description = super.getMessage();
                final StringBuilder msg = new StringBuilder();

                if(StringUtil.hasText(this.level)) {
                    msg.append(this.level).append(' ');
                }

                if(StringUtil.hasText(this.code)) {
                    msg.append("Code: ") //$NON-NLS$
                            .append(this.code)
                            .append(' ');
                }
                if(StringUtil.hasText(this.key)) {
                    msg.append("Key: ") //$NON-NLS$
                            .append(this.code)
                            .append(' ');
                }
                if(StringUtil.hasText(this.correlationId)) {
                    msg.append("Correlation ID: ") //$NON-NLS$
                            .append(this.correlationId)
                            .append(' ');
                }

                if(StringUtil.hasText(description)) {
                    msg.append(", ").append(description); //$NON-NLS$
                }

                this.message = msg.toString();
            }
            return this.message;
        }

        public String getDescription() {
            return super.getMessage();
        }

        public String getLevel() {
            return level;
        }

        public String getCode() {
            return code;
        }

        public String getKey() {
            return key;
        }

        public String getCorrelationId() {
            return correlationId;
        }
    }

    public static class PmxmlValidateOrderResponseException extends PmxmlOrderEntryResponseException {
        private final OrderDataType sentOrderDataType;
        private final OrderDataType receivedDataType;
        private final List<ValidationMessage>validationMessages;

        private static PmxmlValidateOrderResponseException create(DataResponse response, OrderDataType sentOrderDataType) {
            if(response instanceof ValidateOrderDataResponse) {
                return new PmxmlValidateOrderResponseException((ValidateOrderDataResponse)response, sentOrderDataType);
            }
            else if(response instanceof SendOrderDataResponse) {
                return new PmxmlValidateOrderResponseException((SendOrderDataResponse)response, sentOrderDataType);
            }
            else if(response instanceof CancelOrderResponse) {
                return new PmxmlValidateOrderResponseException((CancelOrderResponse)response, sentOrderDataType);
            }
            else if(response instanceof ChangeOrderDataResponse) {
                return new PmxmlValidateOrderResponseException((ChangeOrderDataResponse)response, sentOrderDataType);
            }
            else if(response instanceof ValidateChangeOrderDataResponse) {
                return new PmxmlValidateOrderResponseException((ValidateChangeOrderDataResponse)response, sentOrderDataType);
            }
            else {
                final String responseType = response != null ? response.getClass().getName() : "null"; //$NON-NLS$
                final ValidationMessage vm = new ValidationMessage();
                vm.setServerity(OrderValidationServerityType.VST_ERROR);
                vm.setTyp(OrderValidationType.VT_INFO);
                vm.setMsg("Internal client error: response type " + responseType + "is not handled by PmxmlValidateOrderResponseException!"); //$NON-NLS$
                return new PmxmlValidateOrderResponseException(response, sentOrderDataType, null, Collections.singletonList(vm));
            }
        }

        private PmxmlValidateOrderResponseException(DataResponse response,
                                                    OrderDataType sentOrderDataType,
                                                    OrderDataType receivedOrderDataType,
                                                    List<ValidationMessage> messages) {
            super(response);
            this.sentOrderDataType = sentOrderDataType;
            this.receivedDataType = receivedOrderDataType;
            this.validationMessages = messages;

            if(!OrderResultCode.ORC_VALIDATE_ORDER_FAILED.equals(response.getCode())) {
                throw new IllegalArgumentException("result code must be of type ORC_VALIDATE_ORDER_FAILED"); //$NON-NLS$
            }
        }

        public PmxmlValidateOrderResponseException(ValidateChangeOrderDataResponse response, OrderDataType sentOrderDataType) {
            this(response, sentOrderDataType, response.getOrder(), response.getValidationMsgList());
        }

        public PmxmlValidateOrderResponseException(ValidateOrderDataResponse response, OrderDataType sentOrderDataType) {
            this(response, sentOrderDataType, response.getOrder(), response.getValidationMsgList());
        }

        public PmxmlValidateOrderResponseException(SendOrderDataResponse response, OrderDataType sentOrderDataType) {
            this(response, sentOrderDataType, response.getOrder(), response.getValidationMsgList());
        }

        public PmxmlValidateOrderResponseException(CancelOrderResponse response, OrderDataType sentOrderDataType) {
            this(response, sentOrderDataType, response.getOrder(), response.getValidationMsgList());
        }

        public PmxmlValidateOrderResponseException(ChangeOrderDataResponse response, OrderDataType sentOrderDataType) {
            this(response, sentOrderDataType, response.getOrder(), response.getValidationMsgList());
        }

        public List<ValidationMessage> getValidationMessages() {
            return this.validationMessages;
        }

        public OrderDataType getReceivedDataType() {
            return this.receivedDataType;
        }

        public OrderDataType getSentOrderDataType() {
            return sentOrderDataType;
        }
    }

    public static class SessionContainerNotFoundException extends PmxmlOrderEntryResponseException {
        public SessionContainerNotFoundException(DataResponse response) {
            super(response);

            if(!OrderResultCode.ORC_SESSION_CONTAINER_NOT_FOUND.equals(response.getCode())) {
                throw new IllegalArgumentException("result code must be of type ORC_SESSION_CONTAINER_NOT_FOUND"); //$NON-NLS$
            }
        }
    }

    public interface OrderValidationCallback<T extends OrderDataType> {
        void onProceedAfterValidateOrderFailed(T receivedOrderDataType, List<ValidationMessage> editedValidationMessages);
        void onCancelAfterValidateOrderFailed(List<ValidationMessage> validationMessages);
        void onAnyExceptionAfterValidateOrderFailed(Throwable caught);
    }

    protected boolean isBrokerageModuleSupported(BrokerageModuleID id) {
        try {
            if(!ImplementedOrderModules.isImplemented(id)) {
                return false;
            }

            final OrderPresenterFactory.OrderEntry oe = OrderPresenterFactory.OrderEntry.fromModule(id);
            return true;
        }
        catch(IllegalStateException e) {
            return false;
        }
    }
}
