package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SimpleHtmlSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * Created on 10.10.11 11:37
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class EniteoProductInFocusController extends DelegatingPageController {

    private PriceTeaserSnippet pt;
    private final static String PAGE_URL = "https://www.eniteo.de/dvt2/produktimfokus.htn?gisberater=1"; // $NON-NLS$
    private final static String WKN_URL = "eniteoProduct.xml"; // $NON-NLS$

    public EniteoProductInFocusController(ContentContainer contentContainer) {
        super(contentContainer);
    }

    @Override
    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), "eniteo_product"); // $NON-NLS$
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        super.onPlaceChange(event);
        this.pt = (PriceTeaserSnippet) getSnippet("pt"); // $NON-NLS$
        if (this.pt == null) {
            throw new IllegalStateException("could not get PriceTeaserSnippet by id 'pt'. Check guidef"); // $NON-NLS$
        }
        final SimpleHtmlSnippet html = (SimpleHtmlSnippet) getSnippet("html"); // $NON-NLS$
        if (html == null) {
            throw new IllegalStateException("could not get SimpleHtmlSnippet by id 'html'. Check guidef"); // $NON-NLS$
        }
        html.setHtml("<iframe id=\"external-content\" name=\"external-content\" src=\"" // $NON-NLS-0$
                + PAGE_URL + "\" width=\"1000px\" height=\"740px\" frameborder=\"0\"></iframe>"); // $NON-NLS$
        requestProductWkn();
    }

    private void requestProductWkn() {
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, WKN_URL);
        try {
            request.sendRequest("", new RequestCallback() { // $NON-NLS$

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        parseWkn(response.getText());
                    }
                }

                public void onError(Request request, Throwable exception) {
                    Firebug.log("RequestError: " + exception.getMessage() + "\n" + WKN_URL); // $NON-NLS$
                }
            });

        } catch (Exception ex) {
            Firebug.log("RequestException: " + ex.getMessage() + "\n" + WKN_URL); // $NON-NLS$
        }
    }

    private void parseWkn(String xml) {
        final Document document = XMLParser.parse(xml);
        final Node wknNode = document.getElementsByTagName("wkn").item(0).getChildNodes().item(0); // $NON-NLS$
        setPriceTeaserSymbol(wknNode.getNodeValue());
    }

    private void setPriceTeaserSymbol(String symbol) {
        this.pt.setSymbol(null, symbol, null);
        this.refresh();
    }

    @Override
    public String getPrintHtml() {
        Window.open(PAGE_URL, "_blank", "menubar=yes,resizable=yes,location=no,scrollbars=yes,status=no,toolbar=no"); // $NON-NLS$
        return null;
    }
}