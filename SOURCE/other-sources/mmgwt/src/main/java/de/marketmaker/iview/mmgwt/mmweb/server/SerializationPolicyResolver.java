/*
 * SerializationPolicyResolver.java
 *
 * Created on 19.12.14 11:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author oflege
 */
public class SerializationPolicyResolver implements ServletContextAware, InitializingBean {

    private static final String DEFAULT_PATH = "WEB-INF/gwt";

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Where serialization policy files can be found. If null,
     * {@link com.google.gwt.user.server.rpc.RemoteServiceServlet#doGetSerializationPolicy(javax.servlet.http.HttpServletRequest, String, String)}
     * will be used to get access to the serialization policy. Otherwise, the policy will be
     * loaded from a file in this directory. Helps to speed up hosted mode development as it is
     * not required to stop/start the server and rsync the policy files whenever the client is
     * reloaded.
     */
    private File[] rpcDirs;

    private boolean autoDetect = true;

    private ServletContext servletContext;

    private String serializationPolicyFilePath;

    @SuppressWarnings("unused")
    public void setAutoDetect(boolean autoDetect) {
        this.autoDetect = autoDetect;
    }

    public void setSerializationPolicyFilePath(String path) {
        this.serializationPolicyFilePath = path;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.rpcDirs = tryGetRcpDirs();

        if (this.rpcDirs == null) {
            if (this.autoDetect) {
                this.rpcDirs = autoDetectPaths();
            }
            if (this.rpcDirs == null) {
                this.logger.info("<afterPropertiesSet> no serializationPolicyFilePath");
                return;
            }
        }
        this.logger.info(Arrays.stream(this.rpcDirs)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(", ", "<afterPropertiesSet> rpcDirs = [", "]")));
    }

    private File[] autoDetectPaths() throws IOException {
        final File dir = resolveDir(DEFAULT_PATH);
        if (dir != null) {
            final File current = getLocalDir("dmxml-1/current/WEB-INF/gwt");
            if (current.isDirectory() && current.getCanonicalFile().equals(dir.getCanonicalFile())) {
                // in a dmxml-1 fab deployment, we want to reference both the current and the
                // previous WEB-INF/gwt directory:
                final File previous = getLocalDir("dmxml-1/previous/WEB-INF/gwt");
                if (previous.isDirectory()) {
                    return new File[]{dir, previous};
                }
            }
            return new File[]{dir};
        }
        return null;
    }

    static File getLocalDir(String path) {
        final File dir = new File(System.getProperty("user.home"), "produktion/local");
        return new File(dir, path);
    }

    /**
     * ServletContext is set after property setters but before afterPropertiesSet.
     * Hence, this must be called in afterPropertiesSet.
     * See {@link #resolveDir(File)}.
     */
    public File[] tryGetRcpDirs() {
        if (!StringUtils.hasText(this.serializationPolicyFilePath)) {
            return null;
        }
        final File[] dirs = Arrays.stream(this.serializationPolicyFilePath.split(","))
                .map(this::resolveDir)
                .filter(d -> d != null)
                .toArray(File[]::new);
        if (dirs.length == 0) {
            throw new IllegalArgumentException("no directory in: " + this.serializationPolicyFilePath);
        }
        return dirs;
    }

    private File resolveDir(String name) {
        final File d = resolveDir(new File(name));
        return (d.isDirectory()) ? d : null;
    }

    private File resolveDir(File in) {
        if (in.isAbsolute()) {
            return in;
        }
        final String path = this.servletContext.getRealPath("/" + in);
        return (path != null) ? new File(path) : in;
    }

    /**
     */
    protected SerializationPolicy getSerializationPolicy(String strongName) {
        if (this.rpcDirs == null) {
            return null;
        }

        String fileName = SerializationPolicyLoader.getSerializationPolicyFileName(strongName);
        for (File rpcDir : this.rpcDirs) {
            final File f = new File(rpcDir, fileName);
            if (f.isFile()) {
                return getSerializationPolicy(f);
            }
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getSerializationPolicy> no such file " + fileName);
        }
        return null;
    }

    private SerializationPolicy getSerializationPolicy(File f) {
        try (InputStream is = new FileInputStream(f)) {
            SerializationPolicy result = SerializationPolicyLoader.loadFromStream(is, null);
            this.logger.info("<getSerializationPolicy> from " + f.getAbsolutePath());
            return result;
        } catch (ParseException e) {
            this.logger.error("<getSerializationPolicy> Failed to parse the policy file '"
                    + f.getAbsolutePath() + "'", e);
        } catch (IOException e) {
            this.logger.error("<getSerializationPolicy> Could not read the policy file '"
                    + f.getAbsolutePath() + "'", e);
        }
        return null;
    }
}
