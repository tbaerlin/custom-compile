package de.marketmaker.iview.mmgwt.mmweb.client.util.bulk;

/**
 * @author umaurer
 */
public interface BulkRequest<R> {
    enum State {
        PREPARED, REQUESTED, REQUEST_ERROR, RESULT_ERROR, RESULT_OK
    }
    void sendRequest(BulkRequestHandler handler);
    R getResult();
    String getErrorMessage();
    State getState();
}
