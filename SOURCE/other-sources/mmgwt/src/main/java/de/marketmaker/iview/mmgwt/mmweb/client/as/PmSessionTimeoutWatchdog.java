/*
 * PmSessionTimeoutWatchdog.java
 *
 * Created on 09.03.2015 10:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.EchoDataRequest;
import de.marketmaker.iview.pmxml.IsSessionAliveResponse;
import de.marketmaker.iview.pmxml.VoidRequest;

/**
 * @author Markus Dick
 */
public class PmSessionTimeoutWatchdog implements AsyncCallback<ResponseType>, HasValueChangeHandlers<Long>{
    private enum State { INIT, RUN, STOP }

    private final DmxmlContext context = new DmxmlContext();
    private final DmxmlContext.Block<IsSessionAliveResponse> block = this.context.addBlock("PM_UM_IsSessionAlive");  // $NON-NLS$
    private State state = State.STOP;

    private boolean showingCallbackDialog = false;
    private boolean showedCallbackDialog = false;

    private final Scheduler scheduler = Scheduler.get();
    private final Scheduler.RepeatingCommand repeatingCommand;

    // The whole countdown stuff is only useful to be viewed in the developers tool panel for debugging
    // purposes, because it only shows the time when the remaining session time is queried again. It
    // shows not the the real remaining session time due to the fact that nearly all PM requests reset
    // PM's internal session timeout timer.
    private final Scheduler countdownScheduler = Scheduler.get();
    private final Scheduler.RepeatingCommand countdownCommand;
    private long nextTimeoutMillis;
    private boolean startDebugCountdown = false;
    private HandlerManager handlerManager = new HandlerManager(this);

    public PmSessionTimeoutWatchdog() {
        this.context.setCancellable(false);

        this.block.setParameter(new VoidRequest());

        this.repeatingCommand = new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if (PmSessionTimeoutWatchdog.this.state == State.STOP) {
                    return false;
                }
                sendRequest();
                return false;
            }
        };

        this.countdownCommand = new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if (!startDebugCountdown || state == State.STOP) {
                    return false;
                }

                final long approxRemainingMillis = nextTimeoutMillis - System.currentTimeMillis();

                if(approxRemainingMillis >= 0) {
                    ValueChangeEvent.fire(PmSessionTimeoutWatchdog.this, approxRemainingMillis);
                }
                return true;
            }
        };
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Long> countdownValueChange) {
        return this.handlerManager.addHandler(ValueChangeEvent.getType(), countdownValueChange);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    public void start() {
        if(this.state != State.STOP) {
            throw new IllegalStateException("PmSessionTimeoutWatchdog has already been started");  // $NON-NLS$
        }
        this.state = State.INIT;
        sendRequest();
    }

    protected void sendRequest() {
        this.block.isToBeRequested();
        this.context.issueRequest(this);
    }

    public void stop() {
        this.state = State.STOP;
    }

    @Override
    public void onFailure(Throwable throwable) {
        AbstractMainController.INSTANCE.fireShutdownEvent(false, false);
    }

    @Override
    public void onSuccess(ResponseType responseType) {
        if(this.block.isResponseOk()) {
            handleResult(this.block.getResult());
        }
        else {
            // What should be done in this case? May be we should attempt to save the settings.
            // If we have luck, the PM server still works even if the session check block
            // returns a response that is not ok.
            AbstractMainController.INSTANCE.fireShutdownEvent(true, false);
        }
    }

    public void handleResult(IsSessionAliveResponse response) {
        final long now = System.currentTimeMillis();

        Firebug.debug("<PmSessionTimeoutWatchdog.handleResult> pm session ok: state=" + this.state + ", remainingLifetime=" + response.getRemainingLifetime());
        switch (this.state) {
            case INIT:
            case RUN:
                final int originalMillis = millis(response.getRemainingLifetime());
                this.nextTimeoutMillis = now + originalMillis; // only for debugging purposes
                int newMillis = originalMillis - 30000; //schedule with a fixed delta of 30 seconds
                Firebug.debug("<PmSessionTimeoutWatchdog.handleResult> originalMillis=" + originalMillis + ", newMillis=" + newMillis);
                if(originalMillis <= 500) {
                    // a half second is probably not enough time to save the settings to pm
                    Firebug.debug("<PmSessionTimeoutWatchdog.handleResult> originalMillis <= 500");
                    AbstractMainController.INSTANCE.fireShutdownEvent(false, false);
                    break;
                }
                else if(originalMillis <= 5000) {
                    // 5 seconds should be enough to save the settings
                    Firebug.debug("<PmSessionTimeoutWatchdog.handleResult> originalMillis <= 5000");
                    AbstractMainController.INSTANCE.fireShutdownEvent(true, false);
                    break;
                }
                else if(originalMillis <= 40000) {
                    // schedule with a fixed but more adaptive delta
                    newMillis = originalMillis / 2;
                    Firebug.debug("<PmSessionTimeoutWatchdog.handleResult> originalMillis <= 40000, newMillis: " + newMillis);
                    if(!this.showingCallbackDialog) {
                        showCallbackDialog(originalMillis);
                        this.showedCallbackDialog = true;
                    }
                }
                else {
                    // The callback dialog is only shown once in the 40 seconds time frame.
                    // So if the pm timeout was updated after the dialog was shown, we reset the the variable
                    // so that the dialog can be shown the next time the timeout nearly reached.
                    this.showedCallbackDialog = false;
                }

                Firebug.debug("<PmSessionTimeoutWatchdog.handleResult> schedule with fixed delay: " + newMillis);
                this.scheduler.scheduleFixedDelay(this.repeatingCommand, newMillis);
                this.state = State.RUN;
                break;
            case STOP:
            default:
        }
    }

    private void showCallbackDialog(int millis) {
        if(this.showedCallbackDialog) {
            return;
        }
        this.showingCallbackDialog = true;

        Dialog.confirm(SafeHtmlUtils.fromTrustedString(I18n.I.sessionWillExpirePM(seconds(millis))), new Command() {
            @Override
            public void execute() {
                final DmxmlContext echoContext = new DmxmlContext();
                final DmxmlContext.Block<BlockType> echoBlock = echoContext.addBlock("PM_Echo"); // $NON-NLS$
                echoBlock.setParameter(new EchoDataRequest());
                echoBlock.setToBeRequested();

                echoContext.issueRequest(new AsyncCallback<ResponseType>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        AbstractMainController.INSTANCE.showMessage(I18n.I.sessionRefreshFailedPM());
                    }

                    @Override
                    public void onSuccess(ResponseType responseType) {
                        //no message if ok.
                    }
                });

            }
        }).withCloseCommand(new Command() {
            @Override
            public void execute() {
                showingCallbackDialog = false;
            }
        });
    }

    public void startDebugCountdown() {
        if(this.startDebugCountdown) {
            return;
        }
        this.startDebugCountdown = true;
        this.countdownScheduler.scheduleFixedDelay(this.countdownCommand, 1000);
    }

    public void stopDebugCountdown() {
        this.startDebugCountdown = false;
    }

    private static int seconds(int millis) {
        return millis / 1000;
    }

    private static int millis(String secondsString) {
        return Integer.parseInt(secondsString) * 1000;
    }

    public boolean isStarted() {
        return this.state == State.INIT || this.state == State.RUN;
    }

    public boolean isDebugCountdownStarted() {
        return this.startDebugCountdown;
    }
}
