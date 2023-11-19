package de.marketmaker.itools.gwtcomet.comet.client.impl;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.StatusCodeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 31.01.13 16:54
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * <p/>
 * SOURCE: https://groups.google.com/forum/?fromgroups=#!topic/gwt-comet/NkfGh6-nREM
 *
 * @author Michael Lösch
 */

public class IEXDRCometTransport extends RawDataCometTransport {

    private static final String SEPARATOR = "\n";

    private int read;
    private XDomainRequest transportRequest;
    private XDomainRequestListener xDomainRequestListener = new
            XDomainRequestListener() {

                @Override
                public void onError(XDomainRequest request) {
                    if (isCurrent(request)) {
                        expectingDisconnection = true;
                        listener.onError(new StatusCodeException(Response.SC_INTERNAL_SERVER_ERROR, ""), true);
                        transportRequest = null;
                    }
                }

                @Override
                public void onLoad(XDomainRequest request, String
                        responseText) {
                    request.clearListener();
                    if (isCurrent(request)) {
                        transportRequest = null;
                        if (!disconnecting) {
                            onReceiving(Response.SC_OK, responseText, false);
                        }
                    }
                }

                @Override
                public void onProgress(XDomainRequest request, String
                        responseText) {
                    if (!disconnecting && isCurrent(request)) {
                        onReceiving(Response.SC_OK, responseText, true);
                    }
                    else {
                        request.clearListener();
                        request.abort();
                        if (isCurrent(request)) {
                            transportRequest = null;
                        }
                    }
                }

                @Override
                public void onTimeout(XDomainRequest request) {
                    if (isCurrent(request)) {
                        if (!expectingDisconnection) {
                            listener.onError(new RequestException("Unexpected connection timeout " + request.getTimeout()),
                                    false);
                        }
                    }
                }

                public boolean isCurrent(XDomainRequest request) {
                    return request == transportRequest;
                }
            };

    @Override
    public void connect(int connectionCount) {
        super.connect(connectionCount);
        read = 0;
        transportRequest = XDomainRequest.create();
        try {
            transportRequest.setListener(xDomainRequestListener);
            final String url = getUrl(connectionCount);
            transportRequest.openGET(url);
            transportRequest.send();

        } catch (JavaScriptException ex) {
            if (transportRequest != null) {
                transportRequest.abort();
                transportRequest = null;
            }
            listener.onError(new RequestException(ex.getMessage()), false);
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if (transportRequest != null) {
            transportRequest.clearListener();
            transportRequest.abort();
            transportRequest = null;
        }
        listener.onDisconnected();
    }

    private void onReceiving(int statusCode, String responseText,
                             boolean connected) {
        if (statusCode != Response.SC_OK) {
            if (!connected) {
                super.disconnect();
                listener.onError(new StatusCodeException(statusCode, responseText), connected);
            }
        }
        else {
            int index = responseText.lastIndexOf(SEPARATOR);
            if (index > read) {
                List<Serializable> messages = new ArrayList<Serializable>();

                JsArrayString data = split(responseText.substring(read, index), SEPARATOR);
                int length = data.length();
                for (int i = 0; i < length; i++) {
                    if (disconnecting) {
                        return;
                    }

                    String message = data.get(i);
                    if (!message.isEmpty()) {
                        parse(message, messages);
                    }
                }
                read = index + 1;
                if (!messages.isEmpty()) {
                    listener.onMessage(messages);
                }
            }

            if (!connected) {
                super.disconnected();
            }
        }
    }

    native static JsArrayString split(String string, String
            separator) /*-{
        return string.split(separator);
    }-*/;

    static String unescape(String string) {
        return string.replace("\\n", "\n").replace("\\\\", "\\");
    }

    @Override
    public String getUrl(int connectionCount) {
        String url = super.getUrl(connectionCount);
        // Detect if we have a session in a cookie and pass it on the url, because XDomainRequest does not
        // send cookies
        if (!url.toLowerCase().contains(";jsessionid")) {
            String sessionid = Cookies.getCookie("JSESSIONID");
            if (sessionid != null) {
                String parm = ";jsessionid=" + sessionid;
                int p = url.indexOf('?');
                if (p > 0) {
                    return url.substring(0, p) + parm + url.substring(p);
                }
                else {
                    return url + parm;
                }
            }
        }
        if (!url.toUpperCase().contains("PHPSESSID")) {
            String sessionid = Cookies.getCookie("PHPSESSID");
            if (sessionid != null) {
                int p = url.indexOf('?');
                String param = "PHPSESSID=" + sessionid;
                if (p > 0) {
                    return url.substring(0, p + 1) + param + "&" + url.substring(p + 1);
                }
                else {
                    return url + "?" + param;
                }
            }
        }

        return url;
    }
}