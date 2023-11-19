/*
 * VerySimpleClient.java
 *
 * Created on 19.09.2012 14:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package com.vwd.dmxml.examples.simple;

import com.vwd.dmxml.examples.common.DmxmlExample;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * A very simple example that demonstrates how to connect to vwd data manager [XML]
 */
public class VerySimpleExample extends DmxmlExample {
    static {
        exampleImplementation = VerySimpleExample.class;
    }

    @Override
    public int execute() {
        final String symbol = "DE0005204705"; //e.g. ISIN of vwd AG

        //build request URL
        final String url = "http://" + host + ":" + port + "/dmxml-1/" + zone + "/retrieve.xml";

        //build xml block
        final StringBuilder block = new StringBuilder();
        block.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        block.append("<request>\n");
        block.append("<authentication>");
        block.append(auth);
        block.append("</authentication>\n");
        block.append("<authenticationType>");
        block.append(authType);
        block.append("</authenticationType>\n");
        block.append("<locale>");
        block.append(Locale.getDefault().getLanguage());
        block.append("</locale>\n");
        block.append("<block key=\"MSC_StaticData\">\n");
        block.append("<parameter key=\"symbol\" value=\"");
        block.append(symbol);
        block.append("\"/>\n");
        block.append("</block>\n");
        block.append("</request>");

        //build HTTP request body
        final String httpRequestBody;
        try {
            httpRequestBody = "request=" + URLEncoder.encode(block.toString(), "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        //send request
        out.println("Sending dm[xml] request to: " + url);
        out.println();
        out.println(block);
        out.println("\n");

        HttpURLConnection connection = null;
        PrintWriter requestBodyWriter = null;
        BufferedReader responseBodyWriter = null;

        try {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            requestBodyWriter = new PrintWriter(connection.getOutputStream());
            requestBodyWriter.print(httpRequestBody);
            requestBodyWriter.flush();

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                out.println("dm[xml] response received:\n");
                responseBodyWriter = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = responseBodyWriter.readLine()) != null) {
                   out.println(line);
                }
                responseBodyWriter.close();
            }
            else {
               out.print("Error: ");
               out.println(connection.getResponseCode());
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            disconnect(connection);
            close(requestBodyWriter);
            close(responseBodyWriter);
        }

        return 0;
    }

    private void disconnect(HttpURLConnection connection) {
        try {
            connection.disconnect();
        }
        catch(Exception e) {
            /* do nothing */
        }
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        }
        catch(Exception e) {
            /* do nothing */
        }
    }
}
