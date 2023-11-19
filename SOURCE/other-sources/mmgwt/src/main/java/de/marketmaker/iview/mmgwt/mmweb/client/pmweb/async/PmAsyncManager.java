package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmInvalidSessionException;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.DocumentOrigin;
import de.marketmaker.iview.pmxml.EvalLayoutRequest;
import de.marketmaker.iview.pmxml.GetStateResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * User: umaurer
 * User: mloesch
 * Date: 14.10.13
 * Time: 11:13
 * <p/>
 * PmAsyncManager provides methods to start evaluations and get their current state.
 * Usually, one would request a evaluation or a status and register for process and status updates
 * of these evaluations. Updates are distributed from pm-backend via AMQP-Events to the tomcat-webapp.
 * From the tomcat-webapp, events are transmitted to the browser by the gwt-comet framework,
 * which is encapsulated by {@link AsyncCometHandler}.
 * Besides, there is a periodic monitoring that pulls the current state of an evaluation, if a defined time of
 * missing updates passed (regardless of whether the reason is a problem with comet or pm-backend and tomcat).
 * The pull-code is encapsulated by {@link PullHandlerFallback}.
 */

public class PmAsyncManager implements PmAsyncHandler, ShutdownHandler {
    private static PmAsyncManager I = null;
    private final Map<String, AsyncCallback<AsyncStateResult>> pendingHandles = new HashMap<>();
    private final Map<EvalLayoutRequest, AsyncCallback<AsyncHandleResult>> pendingEvalReqs = new HashMap<>();
    private final ArrayList<String> runningHandles = new ArrayList<>();
    private boolean keepSessionAlive = false;
    private final AsyncHandler asyncHandler;
    private final PullHandler pullHandler;
    private final String userId;
    private String sessionId;
    private boolean sessionCreationPending = false;
    private final HashSet<String> evaluateCallsInProgressHandles = new HashSet<>();

    private static long evaluatePseudoHandle = 0;

    private PmAsyncManager(final String userId) {
        this.asyncHandler = AsyncHandler.Factory.create(new AsyncHandler.TestMessageCallback() {
            @Override
            public void onTestMessageReceived() {
                Firebug.debug("PmAsyncManager <onTestMessageReceived> calling startMonitoring");
                pullHandler.startMonitoring();
                DebugUtil.logToServer("PmAsyncManager <onTestMessageReceived> monitoring started for " + userId);
            }
        }, new AsyncHandler.ErrorCallback() {
            @Override
            public void onError(String sessionId) {
                final String errMsg = "PmAsyncManager <onError> switch to fallback mode for session " + sessionId + "!"; // $NON-NLS$
                Firebug.error(errMsg);
                DebugUtil.logToServer(errMsg);
                pullHandler.startAsFallback();
            }
        });
        this.pullHandler = new PullHandlerFallback(this);
        this.userId = userId;
        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(false);
        EventBusRegistry.get().addHandler(PmAsyncEvent.getType(), this);
        EventBusRegistry.get().addHandler(ShutdownEvent.getType(), this);
    }

    private void createSession() {
        this.sessionCreationPending = true;
        AsyncServiceAsync.App.getInstance().createSession(this.userId, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                PmAsyncManager.this.sessionCreationPending = false;
                Firebug.error("cannot get async session", caught);
            }

            @Override
            public void onSuccess(String sid) {
                PmAsyncManager.this.sessionCreationPending = false;
                Firebug.info("PmAsyncManager <createSession> done for sessionId: " + sid); // $NON-NLS-0$
                onSessionCreated(sid);
            }
        });
    }

    private void onSessionCreated(String sessionId) {
        this.sessionId = sessionId;
        final Command onConnectedCallback = new Command() {
            @Override
            public void execute() {
                processPending();
            }
        };
        this.pullHandler.startAsFallback();
        this.asyncHandler.start(this.sessionId, onConnectedCallback);
    }

    public void closeSession(boolean cancelHandles, final String callee) {
        if (this.sessionId == null) {
            Firebug.debug("<PmAsyncManager.closeSession> sessionId==null callee: " + callee);
            if(this.pullHandler.isActive()) {
                Firebug.debug("<PmAsyncManager.closeSession> sessionId==null but pullHandler still active. stopping pull handler. callee: " + callee);
                this.pullHandler.stop();
            }
            return;
        }
        final String sid = this.sessionId;
        this.sessionId = null;

        stopAsync();
        stopPullHandler();

        Firebug.info("<PmAsyncManager.closeSession> async stopped, pull handler stopped, local sessionId=" + sid + " instance sessionId " + this.sessionId + "  callee: " + callee);

        AsyncServiceAsync.App.getInstance().closeSession(sid, cancelHandles, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.error("Failed to close async service session " + sid + "!" + " callee: " + callee, caught);
            }

            @Override
            public void onSuccess(Void result) {
                Firebug.debug("Async service session " + sid + " closed" + " callee " + callee);
            }
        });
    }

    private void processPending() {
        for (Map.Entry<String, AsyncCallback<AsyncStateResult>> e : this.pendingHandles.entrySet()) {
            registerHandle(e.getKey(), e.getValue());
        }
        this.pendingHandles.clear();
        for (Map.Entry<EvalLayoutRequest, AsyncCallback<AsyncHandleResult>> e : this.pendingEvalReqs.entrySet()) {
            evaluateStep2EnsureSession(e.getKey(), e.getValue());
        }
        this.pendingEvalReqs.clear();
    }

    public void evaluate(final EvalLayoutRequest request, final AsyncCallback<AsyncHandleResult> handleResultCallback) {
        // adding a pseudo handle avoids that the ws session is closed as long as the uuid is being generated.
        final String pseudoHandle = getNextPseudoHandle();
        addEvaluateCallInProgress(pseudoHandle);

        Firebug.debug("<PmAsyncManager.evaluate> creating handle for layout " + request.getLayoutGuid() + " pseudoHandle: " + pseudoHandle);

        AsyncServiceAsync.App.getInstance().createUuid(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                removeEvaluateCallInProgress(pseudoHandle);
                Firebug.debug("<PmAsyncManager.evaluate..onFailure> creating handle for layout " + request.getLayoutGuid() + " removed pseudoHandle: " + pseudoHandle);
                handleResultCallback.onFailure(caught);
            }

            @Override
            public void onSuccess(String handle) {
                removeEvaluateCallInProgress(pseudoHandle);
                Firebug.debug("<PmAsyncManager.evaluate..onSuccess> created handle " + handle + " for layout " + request.getLayoutGuid() + " removed pseudoHandle: " + pseudoHandle + "; calling service evaluateStep2EnsureSession DO_ASYNC");
                request.setAsyncHandle(handle);
                request.setAsyncOrigin(DocumentOrigin.DO_ASYNC);

                evaluateStep2EnsureSession(request, handleResultCallback);
            }
        });
    }

    private String getNextPseudoHandle() {
        return "EPH_" + evaluatePseudoHandle++; // $NON-NLS$
    }

    private void evaluateStep2EnsureSession(final EvalLayoutRequest request, final AsyncCallback<AsyncHandleResult> handleResultCallback) {
        addEvaluateCallInProgress(request.getAsyncHandle());

        if (this.sessionCreationPending) {
            Firebug.debug("<PmAsyncManager.evaluateStep2EnsureSession> addPending 1 -- " + request.getLayoutGuid() + " handle " + request.getAsyncHandle());
            addPending(request, handleResultCallback);
            return;
        }
        if (this.sessionId == null) {
            Firebug.debug("<PmAsyncManager.evaluateStep2EnsureSession> addPending 2 -- " + request.getLayoutGuid() + " handle " + request.getAsyncHandle());
            addPending(request, handleResultCallback);
            createSession();
            return;
        }
        if (!handlerIsActive()) {
            Firebug.debug("<PmAsyncManager.evaluateStep2EnsureSession> not handlerIsActive -- " + request.getLayoutGuid() + " handle " + request.getAsyncHandle());

            final String sid = this.sessionId;
            this.asyncHandler.start(this.sessionId, new Command() {
                @Override
                public void execute() {
                    evaluateStep3EvalLayout(sid, request, handleResultCallback);
                }
            });
        }
        else {
            Firebug.debug("<PmAsyncManager.evaluateStep2EnsureSession> handlerIsActive -- " + request.getLayoutGuid() + " handle " + request.getAsyncHandle());
            evaluateStep3EvalLayout(this.sessionId, request, handleResultCallback);
        }
    }

    private void evaluateStep3EvalLayout(final String sid, final EvalLayoutRequest request, final AsyncCallback<AsyncHandleResult> callback) {
        Firebug.debug("<PmAsyncHandler.evaluateStep3EvalLayout> handle " + request.getAsyncHandle() + " for layout " + request.getLayoutGuid());
        removeEvaluateCallInProgress(request.getAsyncHandle());
        addRunning(request.getAsyncHandle());
        AsyncServiceAsync.App.getInstance().evalLayout(sid, request, callback);
    }

    private void addEvaluateCallInProgress(String asyncHandle) {
        Firebug.debug("<PmAsyncManager.addEvaluateCallInProgress> handle " + asyncHandle);
        this.evaluateCallsInProgressHandles.add(asyncHandle);
    }

    private void removeEvaluateCallInProgress(String asyncHandle) {
        Firebug.debug("<PmAsyncManager.removeEvaluateCallInProgress> handle " + asyncHandle);
        this.evaluateCallsInProgressHandles.remove(asyncHandle);
    }

    private boolean hasEvaluateCallsInProgress() {
        return !this.evaluateCallsInProgressHandles.isEmpty();
    }

    private void addRunning(String handle) {
        if (this.runningHandles.contains(handle)) {
            return;
        }
        this.runningHandles.add(handle);
        this.keepSessionAlive = false;
    }

    public void registerHandle(final String handle, final AsyncCallback<AsyncStateResult> stateResultCallback) {
        if (this.sessionId == null) {
            addPending(handle, stateResultCallback);
            createSession();
            return;
        }
        if (!handlerIsActive()) {
            final String sid = this.sessionId;
            this.asyncHandler.start(this.sessionId, new Command() {
                @Override
                public void execute() {
                    requestStateAndRegisterForPush(sid, handle, stateResultCallback);
                }
            });
        }
        else {
            requestStateAndRegisterForPush(this.sessionId, handle, stateResultCallback);
        }
    }

    private boolean handlerIsActive() {
        return this.asyncHandler.isActive() || this.pullHandler.isActive();
    }

    private void requestStateAndRegisterForPush(String sid, String handle, AsyncCallback<AsyncStateResult> stateResultCallback) {
        addRunning(handle);
        AsyncServiceAsync.App.getInstance().getStateResponse(sid, handle, true, wrapStateCallback(stateResultCallback, handle));
    }

    private AsyncCallback<GetStateResponse> wrapStateCallback(final AsyncCallback<AsyncStateResult> callback, final String handle) {
        return new AsyncCallback<GetStateResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                runningHandles.remove(handle);
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(GetStateResponse result) {
                final AsyncStateResult asr = new AsyncStateResult(handle, "unknown", result); // $NON-NLS$
                callback.onSuccess(asr);
                PmAsyncEvent.fire(new AsyncData(asr));
            }
        };
    }

    public static void createInstance(String uid) {
        if (I != null) {
            throw new IllegalStateException("instance already initialized"); // $NON-NLS$
        }
        I = new PmAsyncManager(uid);
    }

    public static PmAsyncManager getInstance() {
        return I;
    }

    public void pullStatus() {
        if (!hasRunningHandles()) {
            Firebug.debug("PmAsyncManager <pullStatus> No running/pending handles known.");
        }
        if (hasPendingHandles()) {
            processPending();
        }
        final ArrayList<String> handles = new ArrayList<>(this.runningHandles);
        for (final String handle : handles) {
            pullHandleStatus(handle);
        }
    }

    public void pullHandleStatus(final String handle) {
        AsyncServiceAsync.App.getInstance().getStateResponse(this.sessionId, handle, false, new AsyncCallback<GetStateResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("error while getting state of handle " + handle, caught);
                if (caught instanceof PmInvalidSessionException) {
                    Firebug.warn("invalid pm session exception. logging out.");
                    DebugUtil.logToServer("PmAsyncManager <pullHandleStatus> PmInvalidSessionException -> forceLogout");
                    AbstractMainController.INSTANCE.logoutExpiredSession();
                }
                if (caught instanceof InvalidJobStateException) {
                    final String state = ((InvalidJobStateException) caught).getState();
                    Firebug.warn("requested handle (" + handle + ") has an invalid state (" + state + ")");
                }
                runningHandles.remove(handle);
            }

            @Override
            public void onSuccess(GetStateResponse result) {
                Firebug.debug("PmAsyncManager <pullHandleStatus> state of handle " + handle + ": " + result.getState());
                PmAsyncEvent.fire(new AsyncData(new AsyncStateResult(handle, "unknown", result))); // $NON-NLS$
            }
        });
    }

    @Override
    public void onAsync(final PmAsyncEvent event) {
        this.pullHandler.setMillis();

        final AsyncData asyncData = event.getAsyncData();
        final AsyncData.State state = asyncData.getState();

        if (state == AsyncData.State.FINISHED || state == AsyncData.State.ERROR) {
            this.runningHandles.remove(asyncData.getHandle());
            Firebug.debug("PmAsyncManager <onAsync> removing handle " + asyncData.getHandle() + " from 'runningHandles' because of its state "  // $NON-NLS$
                    + state);
        }
        else {
            Firebug.debug("PmAsyncManager <onAsync> still running (" + asyncData.getHandle() + ") state: " // $NON-NLS$
                    + state + (state == AsyncData.State.PROGRESS ?  " progress: " + asyncData.getProgress() : ""));   // $NON-NLS$
        }
        // call closeSessionIfPossible deferred, so it's guaranteed that every onAsync was called before session is closed
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                closeSessionIfPossible("PmAsyncManager.onAsync handle " + asyncData.getHandle());  // $NON-NLS$
            }
        });
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        closeSession(true, "PmAsyncManaget.onShutdown");  // $NON-NLS$
    }

    public void unregisterHandle(final String handle, final boolean keepSessionAlive, final String callee) {
        Firebug.debug("<PmAsyncManager.unregisterHandle> handle " + handle + " keepSessionAlive " + keepSessionAlive + " callee " + callee);

        this.keepSessionAlive = keepSessionAlive;
        AsyncServiceAsync.App.getInstance().unregisterHandle(handle, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.warn("cannot unregister handle " + handle, caught);
            }

            @Override
            public void onSuccess(Void result) {
                runningHandles.remove(handle);
                closeSessionIfPossible(callee + " --> PmAsyncManager.unregisterHandle " + handle);  // $NON-NLS$
            }
        });
    }

    public void closeSessionIfPossible(String callee) {
        if (hasRunningHandles() || this.keepSessionAlive) {
            Firebug.debug("PmAsyncManager <closeSessionIfPossible> can't close session because of " // $NON-NLS$
                    + (hasRunningHandles() ? "running handles" : "keepSessionAlive=true") + " callee " + callee); // $NON-NLS$
            return;
        }
        Firebug.debug("PmAsyncManager <closeSessionIfPossible> no jobs running. closing session, stopping asyncHandler" + " callee " + callee);

        closeSession(false, callee + " --> PmAsyncManager.closeSessionIfPossible");  // $NON-NLS$
    }

    void addPending(EvalLayoutRequest request, AsyncCallback<AsyncHandleResult> callback) {
        this.pendingEvalReqs.put(request, callback);
    }

    void addPending(String handle, AsyncCallback<AsyncStateResult> callback) {
        Firebug.debug("PmAsyncManager <addPending> -- request handle -- handle=" + handle);
        this.pendingHandles.put(handle, callback);
    }

    public boolean hasRunningHandles() {
        return !this.runningHandles.isEmpty() || hasEvaluateCallsInProgress() || hasPendingHandles();
    }

    public boolean hasPendingHandles() {
        return !this.pendingHandles.isEmpty() || !this.pendingEvalReqs.isEmpty();
    }

    public boolean isRunning(String handle) {
        return this.runningHandles.contains(handle);
    }

    public void stopPullHandler() {
        this.pullHandler.stop();
    }

    public void stopAsync() {
        this.asyncHandler.stop();
    }
}