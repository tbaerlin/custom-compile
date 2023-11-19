package de.marketmaker.iview.mmgwt.mmweb.client.util.bulk;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * @author umaurer
 */
public class BulkRequestJson extends AbstractBulkRequest<JSONValue> {
    private JSONValue jsonValue = null;
    private final String url;

    public BulkRequestJson(String url) {
        this.url = url;
    }

    public void sendRequest(final BulkRequestHandler handler) {
        final RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, this.url);
        rb.setHeader("Pragma", "no-cache"); // $NON-NLS$
        rb.setHeader("Cache-Control", "no-cache"); // $NON-NLS$
        try {
            rb.sendRequest("", new RequestCallback(){
                public void onResponseReceived(Request request, Response response) {
                    final int statusCode = response.getStatusCode();
                    if (statusCode == 200) {
                        jsonValue = JSONParser.parseLenient(response.getText());
                        setState(State.RESULT_OK);
                        _onSuccess(handler);
                    }
                    else {
                        setState(State.RESULT_ERROR);
                        _onError(handler, "BulkRequestJson(" + rb.getUrl() + ") returned with status code " + statusCode + "\n" + response.getHeadersAsString(), null); // $NON-NLS$
                    }
                }

                public void onError(Request request, Throwable exc) {
                    setState(State.RESULT_ERROR);
                    _onError(handler, "BulkRequestJson(" + rb.getUrl() + ") returned with error", exc); // $NON-NLS$
                }
            });
            setState(State.REQUESTED);
        }
        catch (RequestException exc) {
            setState(State.REQUEST_ERROR);
            _onError(handler, "could not send BulkRequestJson(" + rb.getUrl() + ")", exc); // $NON-NLS$
        }
    }

    public JSONValue getResult() {
        return this.jsonValue;
    }
}
