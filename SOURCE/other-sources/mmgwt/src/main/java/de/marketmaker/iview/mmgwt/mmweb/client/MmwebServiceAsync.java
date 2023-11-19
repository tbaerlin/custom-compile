package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MmwebServiceAsync {
    RequestBuilder getData(MmwebRequest request, AsyncCallback<MmwebResponse> async);
}
