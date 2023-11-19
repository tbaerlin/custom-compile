/*
 * MarketMappingIDMS.java
 *
 * Created on 25.01.2008 14:12:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.PropertiesLoader;

/**
 * Bean that reads parameter mappings from a resource. Can be used to map arbitrary input parameters
 * to arbitrary output parameters. The input file has the following format:
 * <pre>
 * [aName]
 * inName1=outName1
 * inName2=outName2-1,outName2-2
 * [anotherName]
 * inName1=...
 * </pre>
 * The file can contain an arbitrary number of named mappings, the name has to appear in square
 * brackets before the mappings are listed. A mapping can be requested by invoking the
 * {@link #translate(String, String[])} method. The first parameter specifies the mapping's name,
 * the second the values in the original request.<br>
 * Sometimes, it is necessary to substrings of a given parameter. In that case, a pattern can
 * be defined that will be used to find mapping keys as follows
 * <pre>
 * [aName;find=(\w+)]
 * </pre>
 * The regular expression defined by the find-parameter will be applied to the input and for all
 * matching groups the mapping will be performed.<p>
 * To be really useful, an instance of this class should be used with a
 * {@link de.marketmaker.istar.merger.web.ParameterMappingHandlerInterceptor}
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class ResourceParameterMapping implements InitializingBean, EmbeddedValueResolverAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Resource resource;

    private ActiveMonitor activeMonitor;

    private volatile Map<String, Mapper> mappers = Collections.emptyMap();

    private StringValueResolver resolver;

    private static class Mapper {
        private final Map<String, List<String>> mappings = new HashMap<>();

        private final String name;

        private final Pattern pattern;

        private final String prefix;

        private final FileResource properties;

        private Mapper(String name, Map<String, String> params, FileResource f) {
            this.name = name;
            final String regex = params.get("find");
            this.pattern = (regex != null) ? Pattern.compile(regex) : null;
            this.prefix = params.get("prefix");
            this.properties = f;
        }

        public String getName() {
            return name;
        }

        public String[] translate(String[] input) {
            final List<String> result = new ArrayList<>();
            for (String s : input) {
                if (this.pattern == null) {
                    result.addAll(getMappings(s));
                }
                else {
                    final Matcher m = this.pattern.matcher(s);
                    while (m.find()) {
                        result.addAll(getMappings(m.group(1)));
                    }
                }
            }
            return result.toArray(new String[result.size()]);
        }

        private List<String> getMappings(String s) {
            final List<String> result = getMapping(s);
            if (result != null) {
                return result;
            }
            if (this.prefix != null) {
                return Collections.singletonList(this.prefix + s);
            }
            return Collections.emptyList();
        }

        private List<String> getMapping(String s) {
            synchronized (this.mappings) {
                return this.mappings.get(s);
            }
        }

        void addMapping(String s, List<String> l) {
            synchronized (this.mappings) {
                this.mappings.put(s, l);
            }
        }

        void replaceMappings(Map<String, List<String>> newMappings) {
            if (newMappings == null) {
                return;
            }
            synchronized (this.mappings) {
                this.mappings.clear();
                this.mappings.putAll(newMappings);
            }
        }
    }


    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    public String[] translate(String key, String[] input) {
        final Mapper mapper = this.mappers.get(key);
        if (mapper == null) {
            return null;
        }
        return mapper.translate(input);
    }

    public void afterPropertiesSet() throws Exception {
        init();
    }

    @ManagedOperation(description = "(re)load mappings")
    public void init() throws Exception {
        final Map<String, Mapper> tmp = new HashMap<>();

        Mapper current = null;

        Scanner sc = null;
        String lastS = null;
        try {
            final InputStream is = resource.getInputStream();
            sc = new Scanner(is);
            while (sc.hasNextLine()) {
                String s = sc.nextLine().trim();
                lastS=s;
                if (!StringUtils.hasText(s) || s.startsWith("#")) {
                    continue;
                }
                if (s.startsWith("[") && s.endsWith("]")) {
                    current = createMapper(s.substring(1, s.length() - 1));
                    tmp.put(current.getName(), current);
                }
                else if (current != null) {
                    final int n = s.indexOf('=');
                    current.addMapping(s.substring(0, n),
                            Arrays.asList(StringUtils.commaDelimitedListToStringArray(s.substring(n + 1))));
                }
            }
            this.mappers = Collections.synchronizedMap(tmp);
            this.logger.info("<init> reloaded mappings");
        } catch (Exception e) {
            this.logger.error("<init> failed for " +lastS, e);
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }

    private Mapper createMapper(String s) {
        final String[] tokens = s.split(";");
        final String name = tokens[0];
        final Map<String, String> params = new HashMap<>();
        for (String token : tokens) {
            final int n = token.indexOf('=');
            if (n != -1) {
                params.put(token.substring(0, n), token.substring(n + 1));
            }
        }

        FileResource fr = null;
        final String fileName = params.remove("file");
        if (fileName != null) {
            fr = new FileResource(resolveStringValue(fileName));
        }

        final Mapper result = new Mapper(name, params, fr);

        if (result.properties != null) {
            result.replaceMappings(readMappingsFromFile(result.properties));
            if (this.activeMonitor != null) {
                initResourceMonitoring(result);
            }
        }

        return result;
    }

    private String resolveStringValue(String fileName) {
        if (this.resolver == null) {
            return fileName;
        }
        return this.resolver.resolveStringValue(fileName);
    }

    private void initResourceMonitoring(final Mapper mapper) {
        this.activeMonitor.addResource(mapper.properties);
        mapper.properties.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                mapper.replaceMappings(readMappingsFromFile(mapper.properties));
            }
        });
    }


    private Map<String, List<String>> readMappingsFromFile(final FileResource fr) {
        final Properties props;
        try {
            props = PropertiesLoader.load(fr.getResourceAsStream());
        } catch (IOException e) {
            this.logger.error("<addMappingsFromFile> failed for " + fr, e);
            return null;
        }

        final HashMap<String, List<String>> result = new HashMap<>();
        for (String s : props.stringPropertyNames()) {
            result.put(s, Arrays.asList(props.getProperty(s).split(",")));
        }
        this.logger.info("<addMappingsFromFile> " + fr);
        return result;
    }
}
