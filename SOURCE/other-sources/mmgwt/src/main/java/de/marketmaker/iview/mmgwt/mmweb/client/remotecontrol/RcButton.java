package de.marketmaker.iview.mmgwt.mmweb.client.remotecontrol;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.tools.i18n.NonNLS;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MessageEvent;
import elemental.html.WebSocket;

/**
 * Author: umaurer
 * Created: 19.02.15
 */
@NonNLS
public class RcButton implements IsWidget {
    private final Label label = new Label("Ich bin aus");
    private WebSocket webSocket;

    public RcButton() {
        // TODO: können wir überhaupt WebSockets???
        this.label.setStyleName("mm-link");
        this.label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                toggleRemoteControl();
            }
        });
    }

    private void toggleRemoteControl() {
        if (this.webSocket == null) {
            startRemoteControl();
        }
        else {
            stopRemoteControl();
        }
    }

    private void stopRemoteControl() {
        this.webSocket.close();
        this.webSocket = null;
        this.label.setText("Ich bin aus");
    }

    private void startRemoteControl() {
        this.label.setText("Ich verbinde mich");
        final String url = UrlBuilder.forDmxml("rcController.frm?action=createSession").toString();
        final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);
        requestBuilder.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                final int statusCode = response.getStatusCode();
                if (statusCode != Response.SC_OK) {
                    Firebug.error("can not connect " + statusCode + ": " + response.getStatusText());
                    label.setText("Fehler: " + statusCode);
                    return;
                }
                final String sessionId = response.getText();
                label.setText("Ich weiß eine sessionId");
                openWebSocket(sessionId);
            }

            @Override
            public void onError(Request request, Throwable throwable) {
                Firebug.warn("RcButton RequestBuilder error", throwable);
                label.setText("jeht nedd");
            }
        });
        try {
            requestBuilder.send();
        } catch (RequestException e) {
            Firebug.warn("cannot request sessionId", e);
        }
    }

    private void openWebSocket(final String sessionId) {
        this.webSocket = Browser.getWindow().newWebSocket(UrlBuilder.getWebSocketUrl("/rcEndpoint/" + sessionId));
        this.webSocket.setOnerror(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                Firebug.warn("WebSocket.onError " + event.getClass().getName());
            }
        });
        this.webSocket.setOnopen(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                Firebug.warn("RcButton.onOpen");
                label.setText("Ich bin verbunden");
            }
        });
        this.webSocket.setOnmessage(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                final MessageEvent me = (MessageEvent) event;
                final String token = (String) me.getData();
                Firebug.warn("RcButton.onMessage token: " + token);
                PlaceUtil.goTo(token);
            }
        });
        this.webSocket.setOnclose(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                Firebug.warn("RcButton.onClose");
            }
        });
    }

    @Override
    public Widget asWidget() {
        return this.label;
    }
}
