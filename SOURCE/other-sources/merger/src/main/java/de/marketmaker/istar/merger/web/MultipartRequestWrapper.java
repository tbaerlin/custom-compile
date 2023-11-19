/*
 * MultipartRequestWrapper.java
 *
 * Created on 01.02.13 10:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author oflege
 */
public class MultipartRequestWrapper extends RequestWrapper<MultipartHttpServletRequest>
        implements MultipartHttpServletRequest {

    MultipartRequestWrapper(MultipartHttpServletRequest delegate,
            Map<String, String[]> parameters, String requestUri,
            Map<String, Object> attributes) {
        super(delegate, parameters, requestUri, attributes);
    }

    public Iterator getFileNames() {
        return this.delegate.getFileNames();
    }

    public MultipartFile getFile(String s) {
        return delegate.getFile(s);
    }

    public Map getFileMap() {
        return delegate.getFileMap();
    }

    @Override
    public HttpMethod getRequestMethod() {
        return delegate.getRequestMethod();
    }

    @Override
    public HttpHeaders getRequestHeaders() {
        return delegate.getRequestHeaders();
    }

    @Override
    public HttpHeaders getMultipartHeaders(String paramOrFileName) {
        return delegate.getMultipartHeaders(paramOrFileName);
    }

    @Override
    public List<MultipartFile> getFiles(String name) {
        return delegate.getFiles(name);
    }

    @Override
    public MultiValueMap<String, MultipartFile> getMultiFileMap() {
        return delegate.getMultiFileMap();
    }

    @Override
    public String getMultipartContentType(String paramOrFileName) {
        return delegate.getMultipartContentType(paramOrFileName);
    }

}
