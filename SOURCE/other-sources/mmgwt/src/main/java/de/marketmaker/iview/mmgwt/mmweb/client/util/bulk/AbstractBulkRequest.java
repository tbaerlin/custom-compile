package de.marketmaker.iview.mmgwt.mmweb.client.util.bulk;

/**
 * @author umaurer
 */
public abstract class AbstractBulkRequest<R> implements BulkRequest<R> {
    private String errorMessage = null;
    private BulkRequest.State state = BulkRequest.State.PREPARED;

    protected void setState(BulkRequest.State state) {
        this.state = state;
    }

    public BulkRequest.State getState() {
        return this.state;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    protected void _onSuccess(BulkRequestHandler handler) {
        handler.onSuccess(this);
        onSuccess();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void onSuccess() {
        // can be overwritten from caller as callback
    }

    protected void _onError(BulkRequestHandler handler, String message, Throwable exception) {
        this.errorMessage = message;
        handler.onError(this, message, exception);
        onError(message, exception);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void onError(String message, Throwable exception) {
        // can be overwritten from caller as callback
    }

}
