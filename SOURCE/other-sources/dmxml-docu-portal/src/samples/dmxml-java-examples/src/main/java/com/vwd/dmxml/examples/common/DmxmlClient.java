/*
 * DmxmlClient.java
 *
 * Created on 19.09.2012 16:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package com.vwd.dmxml.examples.common;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * A simple HTTP client that is used to connect do data manager [xml].
 * <b>Do not use for production purposes.</b>
 * @author Markus Dick
 */
public class DmxmlClient {
    private int port = 80;
    private String host = null; //provided by vwd customer service
    private String zone = null; //provided by vwd customer service

    public DmxmlClient(String zone, String host) {
        if(zone == null || host == null) throw new IllegalArgumentException("zone and host required");
        this.zone = zone;
        this.host = host;
    }

    public DmxmlClient(String zone, String host, int port) {
        this(zone, host);
        this.port = port;
    }

    public Response request(String block) throws IOException {
        if(block == null || block.trim().isEmpty()) {
            throw new IllegalArgumentException("block must not be null or empty");
        }

        Response response = new Response();

        //build request URL
        final String url = "http://" + host + ":" + port + "/dmxml-1/" + zone + "/retrieve.xml";

        //build HTTP request body
        String httpRequestBody = "request=" + URLEncoder.encode(block, "UTF-8");

        //send request
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

            response.setHttpResponseCode(connection.getResponseCode());
            response.setHttpResponseMessage(connection.getResponseMessage());

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseBodyWriter = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder sb = new StringBuilder();
                while((line = responseBodyWriter.readLine()) != null) {
                   sb.append(line);
                }
                response.setContent(sb.toString());
            }
        }
        finally {
            disconnect(connection);
            close(requestBodyWriter);
            close(responseBodyWriter);
        }

        return response;
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

    public static class Response {
        private String content;
        private int httpResponseCode;
        private String httpResponseMessage;

        public Response() {
            super();
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getHttpResponseCode() {
            return httpResponseCode;
        }

        public void setHttpResponseCode(int httpResponseCode) {
            this.httpResponseCode = httpResponseCode;
        }

        public String getHttpResponseMessage() {
            return httpResponseMessage;
        }

        public void setHttpResponseMessage(String httpResponseMessage) {
            this.httpResponseMessage = httpResponseMessage;
        }
    }
}
