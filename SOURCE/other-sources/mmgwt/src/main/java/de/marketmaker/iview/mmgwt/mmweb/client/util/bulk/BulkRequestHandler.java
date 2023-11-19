package de.marketmaker.iview.mmgwt.mmweb.client.util.bulk;

import com.google.gwt.core.client.Scheduler;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CompletionHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class BulkRequestHandler {
    private List<BulkRequest> listPending = new ArrayList<>();
    private CompletionHandler completionHandler;
    private int errorCount = 0;
    private boolean completeOnError = true;
    private boolean completed = false;

    public BulkRequestHandler() {
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setCompleteOnError(boolean completeOnError) {
        this.completeOnError = completeOnError;
    }

    public void add(BulkRequest request) {
        this.listPending.add(request);
    }

    public void sendRequests(CompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
        for (BulkRequest request : this.listPending) {
            request.sendRequest(this);
        }
    }

    public void onSuccess(BulkRequest request) {
        if (this.completed) {
            return;
        }
        this.listPending.remove(request);
        if (this.listPending.isEmpty()) {
            onComplete();
        }
    }

    public void onError(BulkRequest request, String message, Throwable exception) {
        Firebug.log("BulkRequestHandler.onError: " + request.getClass().getName());
        if (this.completed) {
            return;
        }
        if (exception == null) {
            Firebug.log(message);
            DebugUtil.logToServer(message);
        }
        else {
            Firebug.error(message, exception);
            DebugUtil.logToServer(message, exception);
        }
        this.errorCount++;
        this.listPending.remove(request);
        if (this.completeOnError) {
            onComplete();
        }
    }

    private void onComplete() {
        this.completed = true;
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                completionHandler.onComplete(BulkRequestHandler.this);
            }
        });
    }

    public boolean isFinishedSuccessfully() {
        return this.errorCount == 0 && this.listPending.isEmpty();
    }
}
