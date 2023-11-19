/*
 * StringTemplateUrlResolver.java
 *
 * Created on 19.06.2006 15:50:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.stringtemplate;

import de.marketmaker.istar.merger.web.easytrade.MoleculeController;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STGroupString;

import static de.marketmaker.istar.common.util.LocalConfigProvider.getIstarSrcDir;

/**
 * Resolves viewnames to StringTemplateView objects. Each such view is supposed to belong to
 * a particular STGroup. The root dir is supposed to contain files named <em>zone</em>-group.stg.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class StringTemplateViewResolver extends AbstractTemplateViewResolver
        implements InitializingBean, DisposableBean {

    private static final STGroup NULL_GROUP = new STGroupString("");

    private static final String DEFAULT_GROUP = "iview";

    private static final String GROUP_SUFFIX = "-group.stg";

    private static final String TEMPLATE_HIERARCHY_SEPARATOR =
        Pattern.quote(MoleculeController.SEP_TEMPLATE_HIERARCHY);

    /**
     * Where templates can be found; if null, templates are assumed to be available on the
     * classpath (e.g., in istar-merger-x.y.z-templates.jar)
     */
    private File rootDir = null;

    private int refreshIntervalInSeconds = 0;

    private ConcurrentHashMap<String, Future<STGroup>> groupsByName
            = new ConcurrentHashMap<>();

    private Timer timer;

    public StringTemplateViewResolver() {
        setViewClass(requiredViewClass());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.refreshIntervalInSeconds > 0 && this.rootDir != null) {
            this.timer = new Timer(StringTemplateViewResolver.class.getSimpleName(), true);
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    refreshTemplates(null);
                }
            }, this.refreshIntervalInSeconds * 1000, this.refreshIntervalInSeconds * 1000);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "group", description = "groupname; empty=all groups")
    })
    public String refreshTemplates(String group) {
        if (StringUtils.hasText(group)) {
            if (this.groupsByName.remove(group) != null) {
                clearCache();
                return "refreshed " + group;
            }
            return "no such group: '" + group + "'";
        }
        else if (!this.groupsByName.isEmpty()) {
            this.groupsByName.clear();
            clearCache();
        }
        return "refreshed all groups";
    }

    /**
     * Requires StringTemplateView.
     * @see StringTemplateView
     */
    protected Class requiredViewClass() {
        return StringTemplateView.class;
    }

    /**
     * Specify where templates can be found. An empty string will be ignored.
     * @param path specifies template search path
     * @throws IOException if r is not a valid file
     */
    public void setRootDir(String path) throws IOException {
        if (StringUtils.hasText(path)) {
            this.rootDir = new File(path);
            if (!this.rootDir.exists()) {
                throw new IOException("cannot read " + this.rootDir.getAbsolutePath());
            }
        }
    }

    public void setRefreshIntervalInSeconds(int refreshIntervalInSeconds) {
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
    }

    private STGroup doGetGroup(final String name) {
        Future<STGroup> f = this.groupsByName.get(name);
        if (f == null) {
            FutureTask<STGroup> ft = new FutureTask<>(() -> loadGroup(name));
            f = this.groupsByName.putIfAbsent(name, ft);
            if (f == null) {
                f = ft;
                ft.run();
            }

        }
        try {
            return f.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URL asURL(File f) {
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private STGroup loadGroup(String name) {
        try {
            final STGroup result = doLoadGroup(name);
            if (result.getFileName() == null) {
                this.logger.error("<loadGroup> no such group: " + name);
                return NULL_GROUP;
            }
            result.setListener(new CommonsLoggingErrorListener(name));
            Renderer.registerDefaultRenderers(result);
            return result;
        } catch (IllegalArgumentException ignored) {
            // STGroupFile constructor will throw IllegalArgumentException for a missing file
            return NULL_GROUP;
        }
    }

    private STGroup doLoadGroup(String name) {
        if (this.rootDir != null) {
            return new STGroupFile(getUrlForGroup(name), "utf8", '$', '$');
        }
        else {
            return new STGroupFile(name + GROUP_SUFFIX, "utf8", '$', '$');
        }
    }

    private URL getUrlForGroup(String name) {
        if (this.rootDir.isDirectory()) {
            return asURL(new File(this.rootDir, name + GROUP_SUFFIX));
        }
        else {
            final URLClassLoader cl = new URLClassLoader(new URL[]{asURL(this.rootDir)});
            return cl.getResource(name + GROUP_SUFFIX);
        }
    }

    public STGroup getGroup(String name) {
        final STGroup group = doGetGroup(name);
        return (group != NULL_GROUP) ? group : null;
    }

    protected AbstractUrlBasedView buildView(String s) throws Exception {
        final int p = s.indexOf(MoleculeController.SEP_TEMPLATE_AND_VIEW);
        if (p > 0) {
            String[] names =  s.substring(0, p).split(TEMPLATE_HIERARCHY_SEPARATOR);
            for (String name : names) {
                if (isKnownGroup(name)) {
                    return buildView(name, s.substring(p + 1));
                }
            }
        }
        return buildView(DEFAULT_GROUP, s);
    }

    private boolean isKnownGroup(final String name) {
        return getGroup(name) != null;
    }

    private AbstractUrlBasedView buildView(String zone, String name) throws Exception {
        final StringTemplateView result = (StringTemplateView) super.buildView(name);
        final STGroup group = getGroup(zone);
        result.setGroup(group);
        return result;
    }

    public static void main(String[] args) throws Exception {
        final StringTemplateViewResolver s = new StringTemplateViewResolver();
        s.setRefreshIntervalInSeconds(0);
        s.setRootDir(new File(getIstarSrcDir(), "merger/src/conf/dmxml-1/WEB-INF/templates/st4").getAbsolutePath());
        final STGroup group = s.getGroup("iview");
        final ST st = group.getInstanceOf("sample-form");
//        final View view = s.loadView("lbbw/molecule", Locale.getDefault());
        System.out.println(st.render());
    }
}
