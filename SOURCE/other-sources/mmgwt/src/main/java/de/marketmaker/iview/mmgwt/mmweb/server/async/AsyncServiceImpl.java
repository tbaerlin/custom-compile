package de.marketmaker.iview.mmgwt.mmweb.server.async;

import de.marketmaker.itools.pmxml.frontend.InvalidSessionException;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmInvalidSessionException;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncHandleResult;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncService;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.InvalidJobStateException;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.ObjectName;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.server.GwtService;
import de.marketmaker.iview.mmgwt.mmweb.server.PmxmlHandler;
import de.marketmaker.iview.pmxml.AsyncState;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.EvalLayoutRequest;
import de.marketmaker.iview.pmxml.GetStateResponse;
import de.marketmaker.iview.pmxml.HandleRequest;
import de.marketmaker.iview.pmxml.MMTalkRequest;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ObjectQuery;
import de.marketmaker.iview.pmxml.QueryStandardSelection;
import de.marketmaker.iview.pmxml.SEAsyncStateChange;
import de.marketmaker.iview.pmxml.SEMMJobProgress;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TaskFramworkPrio;
import de.marketmaker.iview.pmxml.VoidResponse;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData.State.*;

/**
 * User: umaurer
 * Date: 15.10.13
 * Time: 14:31
 */
@SuppressWarnings("GwtServiceNotRegistered")
public class AsyncServiceImpl extends GwtService
        implements AsyncService, InitializingBean, JobProgressListener, ServerStateChangeListener {

    private final ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();

    private final Map<String, Progress> handleToProgress = new HashMap<>();

    private PmxmlHandler pmxmlHandler;

    private PmxmlHandler pmxmlHandlerAsync;

    private final Object mutex = new Object();

    static {
        MmTalkHelper.setDateFormatProxy(new GwtClientServerProxyServer());
    }

    private class Progress {
        private final String handle;
        private final String sessionId;
        private final String pmAuthToken;
        private int progress;
        private TaskFramworkPrio prio;

        public Progress(String handle, String sessionId, String pmAuthToken) {
            this.handle = handle;
            this.sessionId = sessionId;
            this.pmAuthToken = pmAuthToken;
            this.progress = 0;
        }

        public String getHandle() {
            return this.handle;
        }

        private String getPmAuthToken() {
            return pmAuthToken;
        }

        public boolean isFinished() {
            return this.progress == -1;
        }

        public void setAndSendProgress(int progress, TaskFramworkPrio prio) {
            if (progress < this.progress && this.progress < 70 || compare(this.prio, prio) > 0) {
                return;
            }
            this.progress = progress;
            this.prio = prio;
            write(this.sessionId, new AsyncData(this.handle, AsyncData.State.PROGRESS, this.progress));
        }

        private int compare(TaskFramworkPrio p1, TaskFramworkPrio p2) {
            if (p1 == null) {
                return p2 == null ? 0 : -1;
            }
            else {
                return p2 == null ? 1 : p1.compareTo(p2);
            }
        }

        public void sendFinished(String message) {
            this.progress = -1;
            doWrite(FINISHED, message);
        }

        public void sendPaused() {
            doWrite(PAUSED, null);
        }

        public void sendError(String error) {
            this.progress = -1;
            doWrite(ERROR, error);
        }

        private void doWrite(final AsyncData.State state, String msg) {
            logger.debug("[" + this.handle + "] " + state + " -> " + this.sessionId);
            write(this.sessionId, new AsyncData(this.handle, state, this.progress).withMessage(msg));
        }
    }

    public void setPmxmlHandler(PmxmlHandler pmxmlHandler) {
        this.pmxmlHandler = pmxmlHandler;
    }

    public void setPmxmlHandlerAsync(PmxmlHandler pmxmlHandlerAsync) {
        this.pmxmlHandlerAsync = pmxmlHandlerAsync;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.pmxmlHandlerAsync.addListener((JobProgressListener) this);
        this.pmxmlHandlerAsync.addListener((ServerStateChangeListener) this);
    }

    @Override
    public String createSession(String uid) {
        final String sessionId = uid + "_" + Long.toHexString(System.currentTimeMillis());
        this.logger.debug("createSession() -> " + sessionId);
        synchronized (this.mutex) {
            this.clients.put(sessionId, Client.EMPTY);
        }
        return sessionId;
    }

    public void connect(String sessionId, Client client) {
        this.logger.trace("<connect> " + sessionId);
        synchronized (this.mutex) {
            if (!this.clients.containsKey(sessionId)) {
                throw new IllegalArgumentException("no such client " + sessionId);
            }
            this.clients.put(sessionId, client);
            client.sendPing(sessionId);
        }
    }

    private void write(String sessionId, AsyncData data) {
        synchronized (this.mutex) {
            final Client client = this.clients.get(sessionId);
            if (client == null) {
                this.logger.error("<write> no client found for sessionId: " + sessionId);
                return;
            }
            client.write(sessionId, data);
        }
    }

    public void disconnect(String sessionId) {
        this.logger.trace("<disconnect> " + sessionId);
        synchronized (this.mutex) {
            if (!this.clients.containsKey(sessionId)) {
                this.logger.debug("<disconnect> no client found for sessionId: " + sessionId);
                return;
            }
            final Client client = this.clients.put(sessionId, Client.EMPTY);
            if (client != Client.EMPTY) {
                this.logger.trace("nulling session of client " + sessionId);
            }
        }
    }

    @Override
    public GetStateResponse getStateResponse(String sessionId, String handle, boolean registerForPush) throws PmInvalidSessionException, InvalidJobStateException {
        this.logger.info("<getStateResponse> pulling state of handle " + handle + " and session " + sessionId + ", registerForPush: " + registerForPush);
        final HandleRequest req = new HandleRequest();
        req.setHandle(handle);
        final GetStateResponse res;
        try {
            res = this.pmxmlHandlerAsync.exchangeData(req, "Async_GetState", GetStateResponse.class);
        }
        catch (Exception e) {
            checkSessionValidity(e);
            logger.error("cannot get res of handle", e);
            throw new RuntimeException("AsyncServiceImpl.registerHandle -> " + e.getMessage());
        }
        if (res == null) {
            logger.error("res of handle (" + handle + ") is null!");
            throw new InvalidJobStateException(handle, null);
        }
        if (invalidState(res.getState())) {
            logger.error("res of handle (" + handle + ") is invalid (" + res.getState() + ")");
            throw new InvalidJobStateException(handle, res.getState().value());
        }
        synchronized (this.handleToProgress) {
            if (isRunning(res.getState()) && registerForPush && !this.handleToProgress.containsKey(handle)) {
                this.handleToProgress.put(handle, new Progress(handle, sessionId, PmxmlHandler.getAuthTokenFromSession()));
            }
        }
        this.logger.debug("<getStateResponse> state of handle " + handle + " and session " + sessionId + ": " + res.getState());
        onProgress(res.getPrio(), Integer.valueOf(res.getProgress()), handle);
        onStateChange(res.getState(), handle);
        return res;
    }

    private boolean invalidState(AsyncState state) {
        return state == AsyncState.AS_CANCELED || state == AsyncState.AS_CRASHED || state == AsyncState.AS_FAILED
                || state == AsyncState.AS_TIMEOUT || state == AsyncState.AS_UNKNOWN;
    }

    @Override
    public String createUuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    @Override
    public AsyncHandleResult evalLayout(String sessionId, EvalLayoutRequest request) throws PmInvalidSessionException {
        try {
            // might throw exception, so evaluate before invoking exchangeData
            final String objectName = toObjectName(request);
            synchronized (this.handleToProgress) {
                this.handleToProgress.put(request.getAsyncHandle(), new Progress(request.getAsyncHandle(), sessionId, PmxmlHandler.getAuthTokenFromSession()));
            }
            this.pmxmlHandlerAsync.exchangeData(request, "Async_EvalLayout_Evaluate", VoidResponse.class);
            this.logger.info("<evalLayout> asyncHandle=" + request.getAsyncHandle() + " layoutGuid=" + request.getLayoutGuid());
            return new AsyncHandleResult(request.getAsyncHandle(), objectName);
        }
        catch (Exception e) {
            checkSessionValidity(e);
            logger.error("cannot start evaluation", e);
            throw new RuntimeException("AsyncServiceImpl.evalLayout", e);
        }
    }

    private void checkSessionValidity(Throwable exception) throws PmInvalidSessionException {
        if (exception != null && exception instanceof InvalidSessionException) {
            logger.warn("catched a " + exception.getClass() + "! invalidating tomcat session");
            throw new PmInvalidSessionException();
        }
    }

    private String toObjectName(EvalLayoutRequest request) throws PmxmlException {
        final ObjectQuery query = request.getQuery();

        if (query == null) {
            return "";
        }
        else if (query instanceof DatabaseIdQuery) {
            return getObjectName(((DatabaseIdQuery) query));
        }
        else if (query instanceof QueryStandardSelection) {
            return getObjectName(((QueryStandardSelection) query));
        }

        return "ERROR: request of unknown type";
    }

    public String getObjectName(DatabaseIdQuery query) throws PmxmlException {
        final ObjectName.DbIdTalker talker = new ObjectName.DbIdTalker();
        final MMTalkRequest objectNameRequest = talker.createRequest();
        final DatabaseIdQuery talkersQuery = talker.getQuery();
        talkersQuery.getIds().add(query.getIds().get(0));
        return requestName(talker, objectNameRequest);
    }

    public String getObjectName(QueryStandardSelection query) throws PmxmlException {
        final ObjectName.SecIdTalker talker = new ObjectName.SecIdTalker();
        final MMTalkRequest objectNameRequest = talker.createRequest();
        final QueryStandardSelection talkersQuery = talker.getQuery();
        talkersQuery.setDataItemType(ShellMMType.ST_WP);
        talkersQuery.getKeys().add(query.getKeys().get(0));
        return requestName(talker, objectNameRequest);
    }

    private String requestName(AbstractMmTalker<? extends ObjectQuery, ObjectName, ObjectName> talker, MMTalkRequest objectNameRequest) throws PmxmlException {
        final MMTalkResponse mmTalkResponse = this.pmxmlHandler.exchangeData(objectNameRequest, "MMTalk_Execute", MMTalkResponse.class);
        final ObjectName resultObject = talker.createResultObject(mmTalkResponse);
        if(resultObject == null) {
            return "";
        }
        return resultObject.getName();
    }

    @Override
    public void unregisterHandle(String handle) {
        if (removeProgress(handle) == null) {
            this.logger.warn("<unregisterHandle> no entry for " + handle);
            return;
        }
        cancel(handle);
    }

    private void cancel(String handle) {
        final HandleRequest handleRequest = new HandleRequest();
        handleRequest.setHandle(handle);
        try {
            this.pmxmlHandlerAsync.exchangeData(handleRequest, "Async_Cancel");
            logger.debug("[" + handle + "] cancelJob");
        }
        catch (Exception e) {
            logger.warn("cannot cancel async handle: " + handle, e);
        }
    }

    @Override
    public void closeSession(String sessionId, boolean cancelHandles) {
        this.logger.debug("closeSession(" + sessionId + ")");
        if (cancelHandles) {
            final Collection<Progress> values;
            synchronized (this.handleToProgress) {
                values = new ArrayList<>(this.handleToProgress.values());
            }
            for (Progress progress : values) {
                if (sessionId.equals(progress.sessionId)) {
                    this.logger.debug("Cancelling async handle: " + progress.getHandle());
                    cancel(progress.getHandle());
                    removeProgress(progress.getHandle());
                }
            }
        }
        synchronized (this.mutex) {
            this.clients.remove(sessionId);
        }
    }


    @Override
    public void onProgress(SEMMJobProgress event) {
        final TaskFramworkPrio prio = event.getPrio();
        final int p = Integer.parseInt(event.getProgress());
        final String handle = event.getHandle();
        onProgress(prio, p, handle);
    }

    private void onProgress(TaskFramworkPrio prio, int p, String handle) {
        final Progress progress = getProgress(handle);
        if (progress != null) {
            progress.setAndSendProgress(p, prio);
        }
    }

    @Override
    public void onStateChange(SEAsyncStateChange event) {
        final AsyncState asyncState = event.getState();
        if (asyncState == AsyncState.AS_UNKNOWN) {
            logger.debug("state change - started: " + event.getStarted());
            return;
        }
        onStateChange(asyncState, event.getHandle());
    }

    private void onStateChange(AsyncState asyncState, String handle) {
        final Progress progress = getProgress(asyncState, handle);
        if (progress != null && asyncState != AsyncState.AS_RUNNING && asyncState != AsyncState.AS_INITIALIZED) {
            if (asyncState == AsyncState.AS_COMPLETED || asyncState == AsyncState.AS_COMPLETED_WITH_NOTES) {
                progress.sendFinished(null);
            }
            else if (asyncState == AsyncState.AS_PAUSED) {
                progress.sendPaused();
            }
            else if (invalidState(asyncState)) {
                this.logger.error("evaluation NOT completed. state is " + asyncState);
                progress.sendError(getMessage(asyncState));
            }
            else {
                throw new IllegalStateException("unhandled asyncState " + asyncState);
            }
        }
    }

    private Progress getProgress(AsyncState state, String handle) {
        if (isRunning(state)) {
            return getProgress(handle);
        }
        return removeProgress(handle);
    }

    private boolean isRunning(AsyncState state) {
        return state == AsyncState.AS_PAUSED || state == AsyncState.AS_RUNNING || state == AsyncState.AS_INITIALIZED;
    }

    private Progress getProgress(String handle) {
        synchronized (this.handleToProgress) {
            return this.handleToProgress.get(handle);
        }
    }

    private Progress removeProgress(String handle) {
        synchronized (this.handleToProgress) {
            return this.handleToProgress.remove(handle);
        }
    }

    private String getMessage(AsyncState asyncState) {
        switch (asyncState) {
            case AS_FAILED:
                return "Fehler";
            case AS_CRASHED:
                return "Kritischer Fehler";
            case AS_TIMEOUT:
                return "Zeitüberschreitung";
            case AS_CANCELED:
                return "Abgebrochen";
            default:
                return null;
        }
    }

    @Override
    public AsyncData getAsyncData() {
        return new AsyncData("0", AsyncData.State.STARTED, 0);
    }
}