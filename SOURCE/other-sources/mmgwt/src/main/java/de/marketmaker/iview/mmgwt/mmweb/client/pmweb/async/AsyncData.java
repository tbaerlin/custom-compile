package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

import java.io.Serializable;

/**
 * User: umaurer
 * Date: 16.10.13
 * Time: 14:12
 */
public class AsyncData implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final AsyncData PING = new AsyncData(null, State.PING, -1);

    public enum State {
        STARTED, PROGRESS, PAUSED, FINISHED, ERROR, PING
    }

    private String handle;
    private State state;
    private int progress;
    private String message;
    private String finishedDate;

    public AsyncData() {
        this.handle = null;
        this.state = null;
        this.progress = -1;
    }

    public AsyncData(AsyncStateResult res) {
        this.handle = res.getHandle();
        try {
            this.progress = Integer.valueOf(res.getStateResponse().getProgress());
        } catch (NumberFormatException e) {
            Firebug.error("invalid progress (" + res.getStateResponse().getProgress() + ")", e);
            if (res.isFinished()) {
                this.progress = 100;
            } else {
                this.progress = 42;
            }
        }
        if (res.isFinished()) {
            this.state = State.FINISHED;
            this.finishedDate = res.getStateResponse().getFinished();
        } else {
            switch (res.getStateResponse().getState()) {
                case AS_PAUSED: this.state = State.PAUSED;
                    break;
               case AS_RUNNING: this.state = State.PROGRESS;
                   break;
               case AS_INITIALIZED: this.state = State.STARTED;
                   break;
               default: this.state = State.ERROR;
            }
        }
    }

    public AsyncData(String handle, State state, int progress) {
        this.handle = handle;
        this.state = state;
        this.progress = progress;
    }

    public boolean isEmpty() {
        return this.handle == null && this.state == null;
    }

    public String getHandle() {
        return handle;
    }

    public State getState() {
        return state;
    }

    public int getProgress() {
        return progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AsyncData withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getFinishedDate() {
        return this.finishedDate;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[').append(this.handle).append("] ").append(this.state);
        if (this.state == State.PROGRESS) {
            sb.append('(').append(this.progress).append(')');
        }
        if (this.message != null) {
            sb.append('"').append(this.message).append('"'); // $NON-NLS$
        }
        return sb.toString();
    }
}
