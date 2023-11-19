/*
 * MmwebServiceAsyncProxy.java
 *
 * Created on 03.03.2008 15:04:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockOrError;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.dmxml.RequestedBlockType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.BeforeRequestEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ResponseReceivedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.RequestStatistics;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LogWindow;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PhoneGapUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MmwebServiceAsyncProxy {
    private static MmwebServiceAsync createService() {
        final MmwebServiceAsync result = (MmwebServiceAsync) GWT.create(MmwebService.class);

        final String entryPoint;
        if (PhoneGapUtil.isPhoneGap()) {
            final String serverPrefix = UrlBuilder.getServerPrefix();
            if (serverPrefix != null) {
                String rpcUrl = serverPrefix + "/pmxml-1/vwd/mmweb.rpc"; // $NON-NLS$
                entryPoint = rpcUrl.replace("$moduleName", UrlBuilder.MODULE_NAME); // $NON-NLS$
            }
            else {
                final String contextPath = MainController.INSTANCE == null ? "/dmxml-1" : MainController.INSTANCE.contextPath; // $NON-NLS$
                entryPoint = contextPath + "/" + UrlBuilder.MODULE_NAME + "/mmweb.rpc"; // $NON-NLS$
            }
        }
        else {
            final String rpcUrl = JsUtil.getServerSetting("rpcUrl"); // $NON-NLS$
            if (rpcUrl != null) {
                // diese Variante wird in idoc verwendet
                entryPoint = rpcUrl.replace("$moduleName", UrlBuilder.MODULE_NAME); // $NON-NLS$
            }
            else {
                // main controller in idoc is null
                final String contextPath = MainController.INSTANCE == null ? "/dmxml-1" : MainController.INSTANCE.contextPath; // $NON-NLS$
                entryPoint = contextPath + "/" + UrlBuilder.MODULE_NAME + "/mmweb.rpc"; // $NON-NLS$
            }
        }

        ((ServiceDefTarget) result).setServiceEntryPoint(entryPoint);
        return result;
    }

    public static final MmwebServiceAsyncProxy INSTANCE = new MmwebServiceAsyncProxy(createService());

    private static final HashSet<String> PM_BLOCK_TYPES = new HashSet<String>(
            Arrays.asList(new String[]{"PM", "OE", "ACT"}) //$NON-NLS$
    );

    private int currentId = 0;

    private int numActive = 0;
    private int numPmActive = 0;
    private boolean preventCommunication = false;

    private final Map<AsyncCallbackProxy, AsyncCallbackProxy> pending = new IdentityHashMap<AsyncCallbackProxy, AsyncCallbackProxy>();

    private final MmwebServiceAsync mmwebServiceAsync;

    private void firePendingRequestsEvent() {
        EventBusRegistry.get().fireEvent(new PendingRequestsEvent(this.numActive, this.numPmActive));
    }

    public int getCurrentId() {
        return this.currentId;
    }

    public static void cancelPending() {
        INSTANCE.doCancelPending();
    }

    private MmwebServiceAsyncProxy(MmwebServiceAsync mmwebServiceAsync) {
        this.mmwebServiceAsync = mmwebServiceAsync;
    }

    public void getData(RequestType dmxmlRequest, AsyncCallback<ResponseType> async, boolean cancellable) {
        if (this.preventCommunication) {
            async.onFailure(new IllegalStateException("server communication prevented after serializationPolicy problem")); // $NON-NLS$
            return;
        }
        final RequestStatistics stats = RequestStatistics.create(dmxmlRequest, Duration.currentTimeMillis());

        final boolean containsPmRequest = checkContainsPmRequest(dmxmlRequest);
        incNumActive(1, containsPmRequest ? 1 : 0);

        final MmwebRequest mmwebRequest = new MmwebRequest(dmxmlRequest);
        LogWindow.prepareRequest(mmwebRequest);

        final AsyncCallbackProxy callback = new AsyncCallbackProxy(async, cancellable, stats, mmwebRequest, containsPmRequest);
        if (cancellable) {
            addPendingRequest(callback);
        }

        send(mmwebRequest, callback);
        callback.logSentRequest();
    }

    private void preventFurtherCommunication() {
        this.preventCommunication = true;
        if (Window.confirm("serializationPolicy Problem. Neu laden?")) { // $NON-NLS$
            Window.Location.reload();
        }
    }

    private void addPendingRequest(AsyncCallbackProxy newCallbackProxy) {
        if (!this.pending.isEmpty()) {
            final int pendingCount = this.pending.size();
            final String requestS = pendingCount == 1 ? " request" : " requests"; // $NON-NLS$
            final ArrayList<String> listXml = new ArrayList<String>(pendingCount);
            listXml.add("pending" + requestS + ":"); // $NON-NLS$
            final StringBuilder sb = new StringBuilder();
            sb.append(this.pending.size() + 1).append(" parallel requests"); // $NON-NLS$
            for (AsyncCallbackProxy callbackProxy : this.pending.values()) {
                listXml.add(LogWindow.toXml(callbackProxy.mmwebRequest.getDmxmlRequest()));
                sb.append("\n->  ").append(callbackProxy.getRequestedBlockList()); // $NON-NLS$
            }
            listXml.add("new request:"); // $NON-NLS$
            listXml.add(LogWindow.toXml(newCallbackProxy.mmwebRequest.getDmxmlRequest()));
            sb.append("\n->  ").append(newCallbackProxy.getRequestedBlockList()); // $NON-NLS$

            if (SessionData.INSTANCE.isUserPropertyTrue("fbPending")) { // $NON-NLS$
                Firebug.warn(pendingCount + requestS + " pending while issuing new request -> see following grouped log entries");
                Firebug.groupCollapsed(sb.toString(), listXml.toArray(new String[listXml.size()]));
            }
            else {
                Firebug.warn(pendingCount + requestS + " pending while issuing new request -> set user property fbPending=true to log requests");
                Firebug.debug(sb.toString());
            }
        }

        this.pending.put(newCallbackProxy, newCallbackProxy);
    }

    private void send(MmwebRequest request, AsyncCallbackProxy callback) {
        EventBusRegistry.get().fireEvent(new BeforeRequestEvent(request));

        @SuppressWarnings({"gwtRawAsyncCallback"})
        final RequestBuilder rb = this.mmwebServiceAsync.getData(request, callback);
        try {
            rb.setHeader("Grid", getGwtRequestId(callback)); // $NON-NLS-0$
            rb.setHeader(MmwebService.SESSION_VALIDATOR_REQUEST_HEADER, URL.encode(SessionData.INSTANCE.getUser().getUid()));
            rb.send();
        } catch (RequestException ex) {
            //noinspection ThrowableInstanceNeverThrown
            callback.onFailure(new InvocationException(
                    "Unable to initiate the asynchronous service invocation -- check the network connection", // $NON-NLS-0$
                    ex));
        }
    }

    private boolean checkContainsPmRequest(RequestType dmxmlRequest) {
        for (final RequestedBlockType blockType : dmxmlRequest.getBlock()) {
            if (isPmBlockType(blockType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPmBlockType(RequestedBlockType blockType) {
        final String blockKey = blockType.getKey();
        final int indexOfUnderscore = blockKey.indexOf('_');

        return indexOfUnderscore >= 0 && PM_BLOCK_TYPES.contains(blockKey.substring(0, indexOfUnderscore));
    }

    private String getGwtRequestId(AsyncCallbackProxy callback) {
        return SessionData.INSTANCE.getUser().getVwdId() + "_" + callback.id; // $NON-NLS-0$
    }

    public void doCancelPending() {
        final int size = this.pending.size();
        if (size > 0) {
            final String logMessage = "cancelling " + size + " pending request" + (size > 1 ? "s" : ""); // $NON-NLS$
            Firebug.warn(logMessage);
            LogWindow.add(logMessage, false);
            int countPmRequests = 0;
            for (AsyncCallbackProxy acp : pending.values()) {
                if (acp.isContainsPmRequest()) {
                    countPmRequests++;
                }
            }
            incNumActive(-size, -countPmRequests);
            this.pending.clear();
        }
    }

    public boolean hasPendingRequests() {
        return this.pending.size() > 0;
    }

    private void incNumActive(int allN, int pmN) {
        this.numActive += allN;
        this.numPmActive += pmN;
        firePendingRequestsEvent();
    }

    /**
     * An AsyncCallback wrapper that is able to detect response cancellation; on receiving a
     * response that has not been cancelled, it will extract the ResponseType part of the
     * result and forward it to its delegate (or the respective Throwable for a failed request).
     */
    class AsyncCallbackProxy implements AsyncCallback<MmwebResponse> {
        private final AsyncCallback<ResponseType> delegate;

        private final int id = ++currentId;

        private final RequestStatistics stats;
        private final MmwebRequest mmwebRequest;

        private final boolean cancellable;
        private final boolean containsPmRequest;

        private long sentMillis;

        AsyncCallbackProxy(AsyncCallback<ResponseType> delegate, boolean cancellable,
                           RequestStatistics stats, MmwebRequest mmwebRequest, boolean containsPmRequest) {
            this.delegate = delegate;
            this.cancellable = cancellable;
            this.stats = stats;
            this.mmwebRequest = mmwebRequest;
            this.containsPmRequest = containsPmRequest;
        }

        public void logSentRequest() {
            this.sentMillis = System.currentTimeMillis();
            if (!this.mmwebRequest.isWithXmlRequest()) {
                LogWindow.add(this.mmwebRequest.getDmxmlRequest());
                if (SessionData.INSTANCE.isUserPropertyTrue("fbRequest")) { // $NON-NLS$
                    Firebug.groupCollapsed("Request " + this.id + " -> " + getRequestedBlockList(), LogWindow.toXml(this.mmwebRequest.getDmxmlRequest()));
                }
            }
        }

        public String getRequestedBlockList() {
            final StringBuilder sb = new StringBuilder();
            final List<RequestedBlockType> listBlocks = this.mmwebRequest.getDmxmlRequest().getBlock();
            String komma = "";
            for (RequestedBlockType block : listBlocks) {
                sb.append(komma).append(block.getKey()).append("[").append(block.getId()).append("]");
                komma = ", ";
            }
            return sb.toString();
        }

        public void logXmlRequest(String xmlRequest) {
            if (xmlRequest == null) {
                return;
            }
            if (SessionData.INSTANCE.isUserPropertyTrue("fbRequest")) { // $NON-NLS$
                Firebug.groupCollapsed("Request " + this.id + " (from server) -> " + getRequestedBlockList(), xmlRequest);
            }
            LogWindow.addRequest(xmlRequest);
        }

        public void logXmlResponse(String xmlResponse) {
            if (xmlResponse == null) {
                return;
            }
            if (SessionData.INSTANCE.isUserPropertyTrue("fbResponse")) { // $NON-NLS$
                final long duration = System.currentTimeMillis() - this.sentMillis;
                Firebug.groupCollapsed("Response " + this.id + " (" + duration + " millis) -> " + getRequestedBlockList(), xmlResponse);
            }
            LogWindow.addResponse(xmlResponse);
        }

        @Override
        public void onFailure(Throwable caught) {
            this.stats.onFailure(Duration.currentTimeMillis());
            LogWindow.add(this.mmwebRequest.getDmxmlRequest());
            if (!wasCancelled()) {
                LogWindow.add(caught);
                incNumActive(-1, this.containsPmRequest ? -1 : 0);
                this.delegate.onFailure(caught);
            }
            if (caught.getMessage() != null && caught.getMessage().contains("serializationPolicyFile not found")) { // $NON-NLS$
                preventFurtherCommunication();
            }
        }

        @Override
        public void onSuccess(MmwebResponse mmwebResponse) {
            this.stats.onResponse(Duration.currentTimeMillis());

            if (wasCancelled()) {
                this.stats.onCancel();
                Firebug.log("received response for cancelled " + this + ": " + getCancelledBlockList(mmwebResponse)); // $NON-NLS$
                return;
            }
            if (mmwebResponse.getState() == MmwebResponse.State.SESSION_EXPIRED) {
                DebugUtil.logToServer("MmwebServiceAsyncProxy.AsyncCallbackProxy <onSuccess> session expired -> forceLogout");
                AbstractMainController.INSTANCE.logoutExpiredSession();
                return;
            }

            final ResponseType response = mmwebResponse.getResponseType();
            logXmlRequest(mmwebResponse.getXmlRequest());
            logXmlResponse(mmwebResponse.getXmlResponse());
            incNumActive(-1, this.containsPmRequest ? -1 : 0);

            if (mmwebResponse.getState() == MmwebResponse.State.OK) {
                EventBusRegistry.get().fireEvent(new ResponseReceivedEvent(mmwebResponse));
                this.delegate.onSuccess(response);
            }
            else {
                this.delegate.onFailure(null);
                AbstractMainController.INSTANCE.showError(I18n.I.dataNotAvailable());
            }

            this.stats.onCompletion(Duration.currentTimeMillis());
            EventBusRegistry.get().fireEvent(new RequestCompletedEvent(mmwebResponse, this.stats));
        }

        private boolean wasCancelled() {
            return this.cancellable && pending.remove(this) == null;
        }

        private List<String> getCancelledBlockList(MmwebResponse mmwebResponse) {
            final List<BlockOrError> listBlockOrError
                    = mmwebResponse.getResponseType().getData().getBlockOrError();
            final List<String> list = new ArrayList<String>(listBlockOrError.size());
            for (BlockOrError boe : listBlockOrError) {
                if (boe instanceof BlockType) {
                    final BlockType block = (BlockType) boe;
                    list.add(block.getKey() + "[" + block.getCorrelationId() + "]"); // $NON-NLS$
                }
                else if (boe instanceof ErrorType) {
                    final ErrorType error = (ErrorType) boe;
                    list.add(error.getKey() + "[" + error.getCorrelationId() + "]"); // $NON-NLS$
                }
            }
            return list;
        }

        boolean isContainsPmRequest() {
            return this.containsPmRequest;
        }

        public String toString() {
            return "AsyncCallbackProxy[" + this.id + "]"; // $NON-NLS$
        }
    }

}
