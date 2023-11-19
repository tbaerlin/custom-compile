/*
 * JaxbExample.java
 *
 * Created on 20.09.2012 13:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package com.vwd.dmxml.examples.jaxb;

import com.vwd.dmxml.examples.common.DmxmlClient;
import com.vwd.dmxml.examples.common.DmxmlExample;
import com.vwd.dmxml.examples.jaxb.generated.BlockOrError;
import com.vwd.dmxml.examples.jaxb.generated.InstrumentData;
import com.vwd.dmxml.examples.jaxb.generated.MSCStaticData;
import com.vwd.dmxml.examples.jaxb.generated.ObjectFactory;
import com.vwd.dmxml.examples.jaxb.generated.RequestType;
import com.vwd.dmxml.examples.jaxb.generated.RequestedBlockType;
import com.vwd.dmxml.examples.jaxb.generated.ResponseType;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This client demonstrates, how data manager [xml] can be accessed with JAXB.
 * Generating the necessary JAXB classes is a precondition before you can use this client.
 * The supplied <code>dmxml-example-xsd.xml</code> is only a demo and might not work correctly with
 * future releases or with your zone. Hence, please contact our sales/support office to get information
 * about where to get a XSD file appropriate for you.
 *
 * @author Markus Dick
 */
public class JaxbExample extends DmxmlExample {
    private final ObjectFactory objectFactory = new ObjectFactory();

    static {
        exampleImplementation = JaxbExample.class;
    }

    @Override
    public int execute() {
        String symbol1 = "DE0005204705"; //e.g. ISIN of vwd AG
        String correlationId1 = "1";
        String symbol2 = "DE0007100000"; //e.g. ISIN of Daimler AG
        String correlationId2 = "a-second-id";

        //Use this parameter to experiment with other locales/languages.
        //Currently de and en are fully supported.
        locale = Locale.getDefault();

        out.printf("Requesting MSC_StaticData for %s with correlationId '%s' and %s with correlationId '%s':\n\n",
                symbol1, correlationId1, symbol2, correlationId2);

        //build xml request
        JAXBElement<RequestType> request = createDmxmlRequest(auth, authType, locale);
        addMscStaticData(request, symbol1, correlationId1);
        addMscStaticData(request, symbol2, correlationId2);

        //store correlation ids to be used in a loop
        Set<String>correlationIds = new HashSet<String>();
        correlationIds.add(correlationId1);
        correlationIds.add(correlationId2);

        out.println(toXmlString(request));

        //send xml request
        DmxmlClient client = new DmxmlClient(zone, host, port);
        DmxmlClient.Response response;
        try {
            response = client.request(toXmlString(request));
        }
        catch(IOException e) {
            e.printStackTrace();
            return 1;
        }

        //process response
        if(response.getHttpResponseCode() == HttpURLConnection.HTTP_OK) {
            final ResponseType responseType = fromXmlString(response.getContent());

            out.println("Response:");
            List<BlockOrError> blocks = responseType.getData().getBlockOrError();
            for(BlockOrError block : blocks) {
                if(block instanceof MSCStaticData) {
                    final MSCStaticData mscStaticData = (MSCStaticData)block;

                    if(correlationIds.contains(mscStaticData.getCorrelationId())) {
                        printMscStaticData(mscStaticData);
                        out.println();
                    }
                }
            }
        }
        else {
            out.printf("Error: %s (%s)\n",
                    response.getHttpResponseCode(),
                    response.getHttpResponseMessage());
        }

        return 0;
    }

    private void printMscStaticData(MSCStaticData data) {
        if(data == null) return;

        printInstrumentData(data.getInstrumentdata());
        printValue("Type", data.getTypename());
        printValue("Sector", data.getSector());
        printValue("Country", data.getCountry());
        printValue("Ticker", data.getTickersymbol());
    }

    private void printInstrumentData(InstrumentData data) {
        if(data == null) return;

        printValue("ISIN", data.getIsin());
        printValue("name", data.getName());
        printValue("Type Enum", data.getType() + " (" + instrumentTypes.get(data.getType()) + ")");
    }

    private JAXBElement<RequestType>  createDmxmlRequest(String auth, String authType, Locale language) {
        if(auth == null || authType == null || language == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }

        final RequestType requestType = objectFactory.createRequestType();
        requestType.setAuthentication(auth);
        requestType.setAuthenticationType(authType);
        requestType.setLocale(language.getLanguage());

        return objectFactory.createRequest(requestType);
    }

    private void addMscStaticData(JAXBElement<RequestType> request, String symbol, String correlationId) {
        if(request == null || symbol == null) throw new IllegalArgumentException("request and symbol must not be null");

        final RequestedBlockType block = objectFactory.createRequestedBlockType();
        block.setKey("MSC_StaticData");
        if(correlationId != null && !correlationId.trim().isEmpty()) {
            block.setId(correlationId);
        }

        final RequestedBlockType.Parameter p = new RequestedBlockType.Parameter();
        p.setKey("symbol");
        p.setValue(symbol);
        block.getParameter().add(p);

        request.getValue().getBlock().add(block);
    }

    private String toXmlString(JAXBElement<RequestType> request) {
        final StringWriter writer = new StringWriter() ;
        JAXB.marshal(request, writer);
        return writer.toString();
    }

    private ResponseType fromXmlString(String content) {
        return JAXB.unmarshal(new StringReader(content), ResponseType.class);
    }
}
