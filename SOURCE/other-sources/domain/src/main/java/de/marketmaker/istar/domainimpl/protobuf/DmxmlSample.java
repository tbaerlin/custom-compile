/*
 * Sample.java
 *
 * Created on 10.05.11 11:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import com.google.protobuf.CodedInputStream;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.codec.binary.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DmxmlSample {
    private String authentication;

    private String authenticationType;

    private String httpLogin;

    private String httpPassword;

    private String dmxmlUrl;

    private String symbol;

    private DmxmlSample() {
    }

    public DmxmlSample(String... params) {
        this.httpLogin = params[0];
        this.httpPassword = params[1];
        this.dmxmlUrl = params[2];
        this.authentication = params[3];
        this.authenticationType = params[4];
        this.symbol = params.length > 5 ? params[5] : "710000.ETR";
    }

    private Document doGet(URL url, String postRequest) throws Exception {
        final String request = URLEncoder.encode("request", "UTF-8") + "=" + URLEncoder.encode(postRequest, "UTF-8");

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        final OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
        wr.write(request);
        wr.flush();

        if (urlConnection.getResponseCode() != 200) {
            System.err.println(url.toExternalForm() + " returned "
                    + urlConnection.getResponseCode() + " : " + urlConnection.getResponseMessage());
            return null;
        }

        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = builder.parse(urlConnection.getInputStream());
        urlConnection.disconnect();
        return doc;
    }

    private int getNumItems(Document doc) {
        final Node node = doc.getDocumentElement().getElementsByTagName("protobufNumItems").item(0);
        return Integer.parseInt(node.getTextContent());
    }

    private byte[] getRawData(Document doc) throws Exception {
        final Node node = doc.getDocumentElement().getElementsByTagName("protobuf").item(0);
        final String s = node.getTextContent();
        return new Base64().decode(s);
    }

    private GZIPInputStream getInputStream(byte[] data) throws IOException {
        return new GZIPInputStream(new ByteArrayInputStream(data));
    }

    private BigDecimal toBigDecimal(long unscaled, int scale) {
        return new BigDecimal(BigInteger.valueOf(unscaled), scale);
    }

    private static String getRequest(String authentication, String authenticationType,
            Map<String, String> params) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<request>\n" +
                "  <authentication>" + authentication + "</authentication>\n" +
                "  <authenticationType>" + authenticationType + "</authenticationType>\n" +
                "   <block key=\"MSC_TickData\">\n");
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("    <parameter key=\"").append(entry.getKey()).append("\" value=\"").append(entry.getValue()).append("\"/>\n");

        }
        sb.append("  </block>\n" +
                "</request>");

        return sb.toString();
    }

    private void getTicks() throws Exception {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(httpLogin, httpPassword.toCharArray());
            }
        });

        final String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        final Map<String, String> params = new HashMap<>();
        params.put("atoms", "MSC_TickData");
        params.put("format", "PROTOBUF");
        params.put("symbol", this.symbol);
        params.put("start",  "2015-03-19T09:04:00");
        params.put("end", "2015-03-23T09:15:00");
        params.put("aggregation", "PT0S");
        params.put("type", "TICK");
//        params.put("numTrades", "100");
        params.put("tickType", "BID_ASK_TRADE");

        final String postRequest = getRequest(this.authentication, this.authenticationType, params);

        final URL url = new URL(this.dmxmlUrl);
        final Document doc = doGet(url,postRequest);
        if (doc == null) {
            return;
        }

        // the following steps are necessary for all deserializations of protobuf objects,
        // the only variations are for the concrete types of the objects to be deserialized

        // 1: extract number of serialized items
        final int numItems = getNumItems(doc);

        // 2: extract base64-encoded data
        final byte[] rawdata = getRawData(doc);
        System.out.println(rawdata.length);

        // 3: create deserializer using the appropriate type
        ProtobufDeserializer<TimeseriesProtos.TickEvent> des =
                new ProtobufDeserializer<>(getInputStream(rawdata), numItems,
                        new ProtobufDeserializer.ObjectFactory<TimeseriesProtos.TickEvent>() {
                            public TimeseriesProtos.TickEvent parseFrom(
                                    CodedInputStream cis) throws IOException {
                                return TimeseriesProtos.TickEvent.parseFrom(cis);
                            }
                        });

        // 4: create state object of appropriate type to hold current deserialization state
        TickEventState state = new TickEventState();
        int n = 0;
        while (des.hasNext()) {
            // 5: read update
            final TimeseriesProtos.TickEvent update = des.next();
            if (++n > 100) {
//                continue;
            }
            // 6: merge with state to obtain "full" data object
            final TimeseriesProtos.TickEvent tickEvent = state.merge(update);

            // 7: use tick for whatever purpose
            //    query availability of fields with hasXyz() methods before accessing Xyz
            StringBuilder sb = new StringBuilder(100);
            sb.append(new DateTime(tickEvent.getTime() * 1000L));
            if (tickEvent.hasBidPrice()) {
                sb.append(", bid=").append(toBigDecimal(tickEvent.getBidPrice(), tickEvent.getExponentBidPrice()).toPlainString());
            }
            if (tickEvent.hasAskPrice()) {
                sb.append(", ask=").append(toBigDecimal(tickEvent.getAskPrice(), tickEvent.getExponentAskPrice()).toPlainString());
            }
            if (tickEvent.hasPrice()) {
                sb.append(", price=").append(toBigDecimal(tickEvent.getPrice(), tickEvent.getExponentPrice()).toPlainString());
            }
            if (tickEvent.hasBidVolume()) {
                sb.append(", #b").append(tickEvent.getBidVolume());
            }
            if (tickEvent.hasAskVolume()) {
                sb.append(", #a").append(tickEvent.getAskVolume());
            }
            if (tickEvent.hasVolume()) {
                sb.append(", #").append(tickEvent.getVolume());
            }
            if (tickEvent.hasSupplement()) {
                sb.append(", #").append(tickEvent.getSupplement());
            }
            sb.append(", #f=" + tickEvent.getFieldsCount());
            System.out.println(sb.toString());
        }
        System.out.println(n);
    }

    private void decodeTicks(int numItems, byte[] rawdata) throws IOException {
        ProtobufDeserializer<TimeseriesProtos.Tick> des =
                new ProtobufDeserializer<>(getInputStream(rawdata), numItems,
                        new ProtobufDeserializer.ObjectFactory<TimeseriesProtos.Tick>() {
                            public TimeseriesProtos.Tick parseFrom(
                                    CodedInputStream cis) throws IOException {
                                return TimeseriesProtos.Tick.parseFrom(cis);
                            }
                        });

        // 4: create state object of appropriate type to hold current deserialization state
        TickState state = new TickState();
        while (des.hasNext()) {
            // 5: read update
            final TimeseriesProtos.Tick update = des.next();
            // 6: merge with state to obtain "full" data object
            final TimeseriesProtos.Tick t = state.merge(update);

            // 7: use tick for whatever purpose
            //    query availability of fields with hasXyz() methods before accessing Xyz
            StringBuilder sb = new StringBuilder(100);
            sb.append(new DateTime(t.getTime() * 1000L));
            if (t.hasPrice()) {
                sb.append(", p=").append(toBigDecimal(t.getPrice(), t.getExponentPrice()).toPlainString());
            }
            if (t.hasVolume()) {
                sb.append(", #").append(t.getVolume());
            }
            if (t.hasSupplement()) {
                sb.append(", s=").append(t.getSupplement());
            }
            if (t.getFieldsCount() > 0) {
                for (TimeseriesProtos.Field field : t.getFieldsList()) {
                    sb.append(", f(").append(field.getFieldId()).append(")=");
                    if (field.hasStringValue()) {
                        sb.append(field.getStringValue());
                    }
                    else if (field.hasIntValue()) {
                        sb.append(field.getIntValue());
                    }
                    else if (field.hasPriceValue()) {
                        sb.append(toBigDecimal(field.getPriceValue(), t.getExponentPrice()).toPlainString());
                    }
                }
            }
            System.out.println(sb.toString());
        }
    }


    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            byte[] b = Files.readAllBytes(Paths.get(args[1]));
            new DmxmlSample().decodeTicks(Integer.parseInt(args[0]), (new Base64()).decode(b));
            return;
        }
        if (args.length < 5) {
            System.err.println("Usage: Sample <http.login> <http.password> <dmxml.host> <dmxml.authentication> <dmxml.authenticationType> [<symbol>]");
            return;
        }
        final DmxmlSample sample = new DmxmlSample(args);
        sample.getTicks();
    }
}
