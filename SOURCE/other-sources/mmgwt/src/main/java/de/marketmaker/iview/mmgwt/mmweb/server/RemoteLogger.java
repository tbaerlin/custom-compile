/*
 * RemoteLogger.java
 *
 * Created on 12/17/14 10:20 AM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.gwt.core.server.StackTraceDeobfuscator;
import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.IoUtils;

import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

/**
 * Extends its superclass to provide custom locations for serialization policies and symbol maps.
 * Expects to find a {@link SerializationPolicyResolver} in its application context. For symbol
 * maps, we first look for a file named <tt>/WEB-INF/temp/iview-mmgwt-.*-conf\.zip</tt>, which is
 * supposed to contain the symbol map files in a <tt>symbolMaps/</tt> folder. This is useful for
 * deployment.<br>
 * If the zipfile is not found, we examine the servlet init parameter <tt>symbolMapsDirectory</tt>,
 * which is supposed to define the name of a directory that contains the symbol map files. If the
 * parameter is undefined, symbolMaps will be unavailable and stacktraces remain obfuscated.
 *
 * @author kmilyut
 */
public class RemoteLogger extends RemoteLoggingServiceImpl {

    private SerializationPolicyResolver serializationPolicyResolver;

    private ZipFile symbolMapsZip;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        setSymbolMapLocation();
        setSerializationPolicyResolver();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (this.symbolMapsZip != null) {
            IoUtils.close(this.symbolMapsZip);
            this.symbolMapsZip = null;
        }
    }

    private void setSerializationPolicyResolver() {
        ApplicationContext context = getWebApplicationContext(getServletContext());
        this.serializationPolicyResolver = context.getBean(SerializationPolicyResolver.class);
    }

    private void setSymbolMapLocation() throws ServletException {
        final File confZip = findConfZip();
        if (confZip != null) {
            setSymbolMapLocationFromZip(confZip);
            return;
        }

        String relativeSymbolMapPath = getServletConfig().getInitParameter("symbolMapsDirectory");
        if (!StringUtils.hasText(relativeSymbolMapPath)) {
            return;
        }
        String absoluteSymbolMapPath = resolvePath(relativeSymbolMapPath);
        if (absoluteSymbolMapPath == null) {
            throw new ServletException("no real path for " + relativeSymbolMapPath);
        }
        setSymbolMapsDirectory(absoluteSymbolMapPath);
    }

    private File findConfZip() {
        final File temp = new File(getServletContext().getRealPath("/WEB-INF"), "temp");
        final File[] confZipFiles = temp.listFiles(f -> {
            return f.getName().matches("iview-mmgwt-.*-conf\\.zip");
        });
        return (confZipFiles != null && confZipFiles.length == 1) ? confZipFiles[0] : null;
    }

    private void setSymbolMapLocationFromZip(File confZip) throws ServletException {
        try {
            this.symbolMapsZip = new ZipFile(confZip);
        } catch (IOException e) {
            throw new ServletException(e);
        }
        setDeobfuscator(new StackTraceDeobfuscator() {
            @Override
            protected InputStream openInputStream(String fileName) throws IOException {
                final ZipEntry entry = symbolMapsZip.getEntry("symbolMaps/" + fileName);
                if (entry != null) {
                    return symbolMapsZip.getInputStream(entry);
                }
                return new ByteArrayInputStream(new byte[0]);
            }
        });
        getServletContext().log("read symbol maps from " + confZip.getAbsolutePath());
    }

    private String resolvePath(String relativePath) throws ServletException {
        String realPath = getServletContext().getRealPath(relativePath);
        if (realPath != null) {
            return realPath;
        }
        // getRealPath won't resolve symbolic links, try to resolve it anyway for the
        // default location /WEB-INF/gwt/symbolMaps so that symbolMaps can be created as a link
        // to the gwt-extra symbolMaps directory for development
        if (relativePath.startsWith("/WEB-INF")) {
            File dir = new File(getServletContext().getRealPath("/WEB-INF"), relativePath.substring(9));
            if (dir.exists()) {
                return dir.getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public void setSymbolMapsDirectory(String symbolMapsDir) {
        super.setSymbolMapsDirectory(symbolMapsDir);
        setLazyLoadForDeobfuscator();
    }

    /**
     * reading all symbols from a symbolMap currently requires up to 50mb; we currently have
     * 26 permutations, so that more than 1gb would be required to keep all of them in memory.
     * To avoid all that, we set lazyLoad=true on the deobfuscator, so that it only caches symbols
     * for stacktraces that have actually been submitted for logging.
     */
    private void setLazyLoadForDeobfuscator() {
        // deobfuscator is private in the superclass, so use reflection to access it:
        Field f = ReflectionUtils.findField(RemoteLoggingServiceImpl.class, "deobfuscator");
        ReflectionUtils.makeAccessible(f);
        StackTraceDeobfuscator deobfuscator =
                (StackTraceDeobfuscator) ReflectionUtils.getField(f, this);
        deobfuscator.setLazyLoad(true);
    }

    private void setDeobfuscator(StackTraceDeobfuscator deobfuscator) {
        deobfuscator.setLazyLoad(true);
        // deobfuscator is private in the superclass, so use reflection to set it:
        Field f = ReflectionUtils.findField(RemoteLoggingServiceImpl.class, "deobfuscator");
        ReflectionUtils.makeAccessible(f);
        ReflectionUtils.setField(f, this, deobfuscator);
    }


    @Override
    protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request,
            String moduleBaseURL, String strongName) {
        return serializationPolicyResolver.getSerializationPolicy(strongName);
    }

    @Override
    protected void doUnexpectedFailure(Throwable e) {
        if (!GwtService.causedByIEBug(e, getThreadLocalResponse())) {
            super.doUnexpectedFailure(e);
        }
    }
}
