package de.marketmaker.istar.merger.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerInterceptor;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.util.ClassUtil;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategy;
import de.marketmaker.istar.domain.instrument.MarketNameStrategy;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.instrument.TickerStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;

/**
 * Responsible for loading and configuring zone specifications as Zone-objects and
 * acting as a {@link ZoneProvider}. Primary input is {@link #zoneSpec}, which defines
 * the zones and where their properties can be found.
 * Zone properties are ordinary java property files. The following types of properties are supported
 * (all properties are optional) in a zone property file:
 * <h4>Global Zone Parameters</h4>
 * <dl>
 * <dt>extends=<em>parentZoneName</em>
 * <dd>this zone extends parentZone, that is parentZone's properties are copied
 * into this zone before any other processing takes place.
 * <dt>templateBase=<em>path</em>
 * <dd>the path that will be used as a prefix for all view names; default is the zone's name
 * <dt>interceptors=<em>beanName(,beanName)*</em>
 * <dd>defines default interceptors for handlers in this zone. The names have to be names of beans
 * that implement {@link org.springframework.web.servlet.HandlerInterceptor}.
 * <dt>marketStrategy=<em>beanname</em> | [<em>key1</em>:<em>beanname1</em>, <em>key2</em>:<em>beanname2</em> ...]
 * <dd>defines the default market strategy and possibly other market strategies that should
 * be available in the context of this zone. The names have to be names
 * of beans that implement {@link de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy}.
 * If multiple strategies are defined, the one with the key "default" will be used as the default
 * market strategy (if no such entry exists, the default is defined by
 * {@link de.marketmaker.istar.merger.context.RequestContext#DEFAULT_MARKET_STRATEGY}). To reference
 * one of the other strategies, use their name as follows: If the strategy's key is "foo", it can
 * be referenced using <tt>marketStrategy=foo</tt> or, if it should be used as default for a custom
 * strategy, try s.th. like <tt>marketStrategy=market,default=foo:ETR,FFM</tt>
 * <dt>quoteNameStrategy=<em>beanName|className|className#member</em></dt>
 * <dd>defined how quote names will be resolved</dd>
 * <dt>instrumentNameStrategy=<em>beanName|className|className#member</em></dt>
 * <dd>defined how instrument names will be resolved</dd>
 * <dt>context.<em>key</em>=<em>value</em>
 * <dd>used to create a context map for the zone, which will be available in the view model.
 * The map will contain all keys and the respective
 * mapped value and is of type Map&lt;String, Object&gt;. The values can be specified as
 * <ul>
 * <li><tt>[s1,s2,...]</tt>: yields a <tt>List&lt;String&gt;</tt> (groovy list syntax)
 * <li><tt>[k1:v1,k2:v2,...]</tt>: yields a <tt>Map&lt;String, String&gt;</tt> (groovy map syntax)
 * <li><tt>true|false</tt>: yields the corresponding <tt>Boolean</tt> object
 * <li>If the key matches <tt>error-page.<em>nnn</em></tt>: yields an
 * {@link de.marketmaker.istar.merger.web.ErrorPage} object that can be used to redirect to certain
 * static html pages depending on the error code <em>nnn</em>. The value is supposed to be defined as
 * <em>msg-or-resource[,status-code[,content-type]]</em><dl>
 *     <dt>msg-or-resource</dt>
 *     <dd>if this string starts with a '/', a resource with that name will be loaded and its
 *     content used as the error message; otherwise, the string is used as is</dd>
 *     <dt>status-code <em>(optional)</em></dt>
 *     <dd>defines the response's status code, default value is <em>nnn</em></dd>
 *     <dt>content-type <em>(optional)</em></dt>
 *     <dd>defines the response's Content-Type header</dd>
 * </dl>
 * <li>all other values are treated as String objects.
 * </ul>
 * </dl>
 * <h4>Handlers</h4>
 * Zones contain definitions for handlers. A handler is a name for a particular action; it may
 * be the name of an atom executed by the MolculeController, the name of a chart etc. To identify
 * a handler, the name part of a URI is examined (i.e., for URL "/foo/bar.png?a=b", the handler's name
 * is "bar.png"). Supported properties for handlers:
 * <dl>
 * <dt><em>handlername</em>.extends=<em>parentHandlerName1(,parentHandlerName2)*</em>
 * <dd>the parameters and context map of the parent handlers will be added to the handler,
 * but only if no parameter or context entry with the same name has already
 * been specified for the handler. Parameters will be added from each parent in the order
 * the parent names appear in the extends property.
 * <dt><em>handlername</em>.interceptors=<em>beanName(,beanName)*</em>
 * <dd>defines interceptors for a specific handler. The names have to be names of beans
 * that implement {@link org.springframework.web.servlet.HandlerInterceptor}, this defition overrides
 * a zone wide interceptors definition.
 * <dt><em>handlername</em>.context.<em>key</em>=<em>value</em>
 * <dd>defines a handler specific context map in the same way as the zone's context map (see above).
 * When {@link Zone#getContextMap(java.lang.String)} is invoked, the entries in the
 * handlers context map override those entries in the zone's context map that have the same key. The
 * context map will be available during request processing as an HttpServletRequest attribute
 * with the key {@link Zone#ATOM_CONTEXT_ATTRIBUTE}.
 * </dl>
 * <h4>Handler Parameters</h4>
 * Parameters resemble HttpServletRequest parameters: for a given key, there may be n parameter
 * values (Strings) that will be stored and made available as a <tt>String[]</tt>. To define multiple
 * values, use comma-separated values in brackets (e.g.: <tt>[<em>value1</em>, <em>value2</em>, ...]</tt>).
 * Parameters defined in a zone will be mixed
 * with actual request parameters. Zone parameters are either <tt>default</tt> or
 * <tt>fixed</tt>. The former can be overridden by actual request parameteres, the latter cannot.
 * <dl>
 * <dt>default.<em>key</em>=<em>value</em>
 * <dd>A default parameter that applies to all handlers
 * <dt>fixed.<em>key</em>=<em>value</em>
 * <dd>A fixed parameter that applies to all handlers; fixed means that it cannot be changed by an
 * actual request parameter
 * <dt><em>handlername</em>.default.<em>key</em>=<em>value</em>
 * <dd>Handler-specific default parameter; overrides global default parameters.
 * <dt><em>handlername</em>.fixed.<em>key</em>=<em>value</em>
 * <dd>Handler-specific fixed parameter; overrides global fixed parameters.
 * </dl>
 * Defining the key <tt>controllerName</tt> for a handler allows to specify the name of the controller
 * bean that should be used. This allows it, for example, to define different chart names
 * (i.e., handlers) all with their own parameters, and handle all those charts using a single
 * controller.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class DefaultZoneProvider extends WebApplicationObjectSupport implements ZoneProvider {
    // a string may be specified as path.to.class#STATIC_STRING_FIELD
    private static final Pattern STRING_CONSTANT = Pattern.compile("\\w+(\\.\\w+)+#\\w+");

    protected final AtomicReference<Map<String, Zone>> zonesByName = new AtomicReference<>();

    /**
     * Refers to a Resource describing where to find zone property files; is itself a
     * property file, each non-comment line has to be specified as:
     * <pre>
     * <em>zoneName</em>=/path/to/zone.prop
     * </pre>
     */
    Resource zoneSpec;

    final Pattern pattern = Pattern.compile("(if (\\w+) )?(.*?)\\.?(default|fixed|context)\\.(.*)");

    final Pattern interceptorsPattern = Pattern.compile("(.*?)\\.interceptors$");

    final Pattern extendsPattern = Pattern.compile("(.*?)\\.extends$");

    final Map<String, String> resourcesByName = new HashMap<>();

    public String toString() {
        return "DefaultZoneProvider[" + this.zoneSpec
                + ", zones=" + this.zonesByName.get().keySet() + "]";
    }

    public void setZoneSpec(Resource zoneSpec) {
        this.zoneSpec = zoneSpec;
    }

    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        if (!this.zoneSpec.exists()) {
            throw new IllegalArgumentException("zoneSpec resource does not exist: " + zoneSpec);
        }

        loadZones();
        if (this.zonesByName.get() == null) {
            throw new IllegalStateException("failed to load zones");
        }
    }

    @ManagedOperation
    public void loadZones() {
        try {
            doLoadZones();
        } finally {
            this.resourcesByName.clear();
        }
    }

    void doLoadZones() {

        ZonePropertiesReader zonePropertiesReader = new ZonePropertiesReader(getApplicationContext(), this.zoneSpec);
        /*
        final Properties zones = loadProperties(this.zoneSpec);
        if (zones == null) {
            return;
        }

        final Map<String, Properties> input = loadZoneProperties(zones);
         */

        final Map<String, Properties> input = zonePropertiesReader.loadZones();
        if (input == null) {
            this.logger.error("<loadZones> failed to load all properties from " + this.zoneSpec.getFilename());
            return;
        }

        // This was set before loading the actual zones.
        boolean ok = true;

        final Map<String, Zone> result = new HashMap<>();
        for (Map.Entry<String, Properties> entry : input.entrySet()) {
            final String key = entry.getKey();
            final Properties p = entry.getValue();

            try {
                loadZone(key, p, result);
            } catch (Exception e) {
                this.logger.error("<loadZones> failed for " + key, e);
                ok = false;
            }
        }

        if (ok) {
            this.zonesByName.set(result);
            this.logger.info("<loadZones> (re)loaded " + result.size() + " zone(s)");
        }
    }

    /*

    Map<String, Properties> loadZoneProperties(Properties zones) {
        final HashMap<String, Properties> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : zones.entrySet()) {
            final String key = (String) entry.getKey();
            final String res = (String) entry.getValue();

            final Resource r = getResource(res);
            if (!r.exists()) {
                this.logger.error("<loadZoneProperties> invalid resource for zone " + key + ": " + r);
                continue;
            }
            final Properties p = loadProperties(r);
            if (p == null) {
                continue;
            }
            result.put(key, p);
        }
        return result.size() == zones.size() ? result : null;
    }

     */

    /*
    Resource getResource(String res) {
        if (getApplicationContext() instanceof WebApplicationContext) {
            final WebApplicationContext context = (WebApplicationContext) getApplicationContext();
            return context.getResource(res);
        }
        return new DefaultResourceLoader().getResource(res);
    }
    */

    /*
    protected Properties loadProperties(Resource resource) {
        try {
            return PropertiesLoader.load(resource);
        } catch (IOException e) {
            this.logger.error("<loadProperties> failed", e);
            return null;
        }
    }
    */

    protected void loadZone(String name, Properties p, Map<String, Zone> result) throws Exception {
        final ZoneImpl z = new ZoneImpl(name);

        initTemplateBase(p, z);
        initMarketStrategy(p, z);
        initQuoteComparator(p, z);
        initQuoteNameStrategy(p, z);
        initMarketNameStrategy(p, z);
        initInstrumentNameStrategy(p, z);
        initTickerStrategy(p, z);
        initBaseQuoteFilter(p, z);
        initInterceptors(p, z);

        final Matcher matcher = this.pattern.matcher("");
        final Matcher extendsMatcher = this.extendsPattern.matcher("");
        final Matcher interceptorsMatcher = this.interceptorsPattern.matcher("");

        final Map<String, String> extensions = new HashMap<>();

        for (String key : p.stringPropertyNames()) {
            final String value = resolveValue(getProperty(p, key));

            matcher.reset(key);

            if (matcher.matches()) {
                final String flagName = matcher.group(2);
                if (flagName == null || FeatureFlags.Flag.valueOf(flagName).isEnabled()) {
                    if ("default".equals(matcher.group(4))) {
                        z.addDefaultParameter(matcher.group(3), matcher.group(5), resolveStringValue(value));
                    }
                    else if ("fixed".equals(matcher.group(4))) {
                        z.addFixedParameter(matcher.group(3), matcher.group(5), resolveStringValue(value));
                    }
                    else if ("context".equals(matcher.group(4))) {
                        z.addContextObject(matcher.group(3), matcher.group(5),
                                toObject(matcher.group(5), value.trim()));
                    }
                }
                continue;
            }

            extendsMatcher.reset(key);
            if (extendsMatcher.matches()) {
                extensions.put(extendsMatcher.group(1), value);
                continue;
            }

            interceptorsMatcher.reset(key);
            if (interceptorsMatcher.matches()) {
                z.setInterceptors(interceptorsMatcher.group(1), toInterceptors(value));
                continue;
            }

            this.logger.warn("<loadZone> ignoring invalid property " + key + "=" + value);
        }

        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            final String key = entry.getKey();
            final String[] parents = StringUtils.commaDelimitedListToStringArray(entry.getValue());
            for (String parent : parents) {
                if (!z.containsKey(parent)) {
                    this.logger.warn("<loadZone> unknown parent '" + parent + "' for " + key);
                    continue;
                }
                final Map<String, String[]> dp = z.getDefaultParameters(parent);
                for (Map.Entry<String, String[]> e : dp.entrySet()) {
                    z.addDefaultParametersIfAbsent(key, e.getKey(), e.getValue());
                }

                final Map<String, String[]> fp = z.getFixedParameters(parent);
                for (Map.Entry<String, String[]> e : fp.entrySet()) {
                    z.addFixedParametersIfAbsent(key, e.getKey(), e.getValue());
                }

                final Map<String, Object> cm = z.getContextObjects(parent);
                for (Map.Entry<String, Object> e : cm.entrySet()) {
                    z.addContextObjectIfAbsent(key, e.getKey(), e.getValue());
                }
            }
        }

        this.logger.info("<loadZone> succeeded for " + name + "...");
        result.put(name, z);
    }

    String removeProperty(Properties p, String key) {
        final String result = (String) p.remove(key);
        return (result != null) ? resolveValue(result) : null;
    }

    String getProperty(Properties p, String key) {
        return getProperty(p, key, null);
    }

    String getProperty(Properties p, String key, String defaultValue) {
        final String result = p.getProperty(key);
        return (result != null) ? resolveValue(result) : defaultValue;
    }

    String resolveValue(final String value) {
        try {
            if (getApplicationContext() instanceof ConfigurableApplicationContext) {
                return ((ConfigurableApplicationContext) getApplicationContext()).getBeanFactory().resolveEmbeddedValue(value);
            }
        } catch (Exception e) {
            this.logger.warn(e.getMessage());
        }
        return value;
    }

    void initInterceptors(Properties p, ZoneImpl z) throws Exception {
        final String interceptors = removeProperty(p, "interceptors");
        if (interceptors != null) {
            z.setInterceptors("", toInterceptors(interceptors));
        }
    }

    void initTemplateBase(Properties p, ZoneImpl z) throws Exception {
        final String templateBase = removeProperty(p, "templateBase");
        if (templateBase != null) {
            z.setTemplateBase(templateBase);
        }
    }

    void initMarketStrategy(Properties p, ZoneImpl z) throws Exception {
        final String name = removeProperty(p, "marketStrategy");
        if (name == null) {
            return;
        }
        final Object value = toObject("marketStrategy", name);
        if (value instanceof String) {
            final MarketStrategy ms = getMarketStrategy((String) value);
            z.setMarketStrategy(ms);
        }
        else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, String> map = (Map<String, String>) value;
            z.setMarketStrategies(getObjects(map, MarketStrategy.class));
        }
        else {
            throw new IllegalArgumentException("invalid marketStrategy type: " + value.getClass().getName());
        }
    }

    private MarketStrategy getMarketStrategy(String value) {
        if (FeatureFlags.isEnabled(FeatureFlags.Flag.TEST_MARKET_STRATEGIES_IF_AVAILABLE)) {
            try {
                final MarketStrategy testMs = getObject(value + "_TEST", MarketStrategy.class);
                if (testMs != null) {
                    return testMs;
                }
            } catch (IllegalArgumentException ignore) {
                // ignore silently
            }
        }
        return getObject(value, MarketStrategy.class);
    }

    void initQuoteComparator(Properties p, ZoneImpl z) throws Exception {
        final String name = removeProperty(p, "quoteComparator");
        if (name != null) {
            //noinspection unchecked
            z.setQuoteComparator(getObject(name, Comparator.class));
        }
    }

    void initQuoteNameStrategy(Properties p, ZoneImpl z) throws Exception {
        final String name = removeProperty(p, "quoteNameStrategy");
        if (name != null) {
            z.setQuoteNameStrategy(getObject(name, QuoteNameStrategy.class));
        }
    }

    void initMarketNameStrategy(Properties p, ZoneImpl z) throws Exception {
        final String name = removeProperty(p, "marketNameStrategy");
        if (name != null) {
            z.setMarketNameStrategy(getObject(name, MarketNameStrategy.class));
        }
    }

    void initInstrumentNameStrategy(Properties p, ZoneImpl z) throws Exception {
        final String name = removeProperty(p, "instrumentNameStrategy");
        if (name != null) {
            z.setInstrumentNameStrategy(getObject(name, InstrumentNameStrategy.class));
        }
    }

    void initTickerStrategy(Properties p, ZoneImpl z) throws Exception {
        final String name = removeProperty(p, "tickerStrategy");
        if (name != null) {
            z.setTickerStrategy(getObject(name, TickerStrategy.class));
        }
    }

    void initBaseQuoteFilter(Properties p, ZoneImpl z) throws Exception {
        final String name = removeProperty(p, "baseQuoteFilter");
        if (name != null) {
            z.setBaseQuoteFilter(getObject(name, QuoteFilter.class));
        }
    }

    private <V> V getObject(String name, Class<V> clazz) {
        if (isBeanName(name)) {
            //noinspection unchecked
            return getApplicationContext().getBean(name, clazz);
        }
        return ClassUtil.getObject(name);
    }

    private boolean isBeanName(String name) {
        return getApplicationContext().containsBean(name);
    }

    private <V> Map<String, V> getObjects(Map<String, String> map, final Class<V> clazz) {
        final Map<String, V> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.put(entry.getKey(), getObject(entry.getValue(), clazz));
        }
        return result;
    }

    ErrorPage toErrorPage(int defaultErrorCode, String value) throws IOException {
        final String[] t = value.split(",", 3);
        final String errorMessage = (t[0].startsWith("/")) ? loadResource(t[0]) : t[0];
        final int errorCode = t.length > 1 ? Integer.parseInt(t[1]) : defaultErrorCode;
        final String contentType = t.length > 2 ? t[2] : guessContentType(t[0]);
        return new ErrorPage(errorCode, errorMessage, contentType);
    }

    String guessContentType(String s) {
        if (s.startsWith("/")) {
            if (s.endsWith(".html") || s.endsWith(".htm")) {
                return "text/html;charset=utf-8";
            }
            if (s.endsWith(".xml")) {
                return "text/xml";
            }
            return "text/plain;charset=utf-8";
        }
        return "text/plain";
    }

    String loadResource(String name) throws IOException {
        if (this.resourcesByName.containsKey(name)) {
            return this.resourcesByName.get(name);
        }
        final InputStream stream = getServletContext().getResourceAsStream(name);
        final String result = FileCopyUtils.copyToString(new InputStreamReader(stream, "UTF-8"));
        this.resourcesByName.put(name, result);
        return result;
    }

    HandlerInterceptor[] toInterceptors(String value) throws Exception {
        final String[] beanNames = StringUtils.commaDelimitedListToStringArray(value);
        final List<HandlerInterceptor> tmp = new ArrayList<>(beanNames.length);
        for (String name : beanNames) {
            tmp.add(getApplicationContext().getBean(name, HandlerInterceptor.class));
        }
        return tmp.toArray(new HandlerInterceptor[tmp.size()]);
    }

    private String resolveStringValue(String value) throws Exception {
        if (STRING_CONSTANT.matcher(value).matches()) {
            return getObject(value, String.class);
        }
        return value;
    }

    Object toObject(String key, String value) throws Exception {
        if (key.startsWith("error-page.")) {
            return toErrorPage(Integer.parseInt(key.substring(key.indexOf('.') + 1)), value);
        }

        final Object o = toScalarObject(value);

        if (key.equals("beans")) {
            final Map<String, Object> beans = new HashMap<>();
            if (o instanceof String) {
                beans.put((String) o, getApplicationContext().getBean((String) o));
            }
            if (o instanceof List) {
                final List<String> list = (List<String>) o;
                for (final String s : list) {
                    beans.put(s, getApplicationContext().getBean(s));
                }
            }
            if (o instanceof Map) {
                final Map<String, String> map = (Map<String, String>) o;
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    beans.put(entry.getKey(), getApplicationContext().getBean(entry.getValue()));
                }
            }
            return beans;
        }

        return o;
    }

    private Object toScalarObject(String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            // support groovy like syntax for lists and maps; all keys/values are strings
            if ("[]".equals(value)) return Collections.emptyList();
            if ("[:]".equals(value)) return Collections.emptyMap();
            final String[] elements = StringUtils.commaDelimitedListToStringArray(value.substring(1, value.length() - 1));
            if (isMap(elements)) {
                return Collections.unmodifiableMap(StringUtils.splitArrayElementsIntoProperties(elements, ":"));
            }
            else {
                return Arrays.asList(elements);
            }
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.valueOf(value);
        }
        return value;
    }

    boolean isMap(String[] elements) {
        for (String element : elements) {
            if (element.indexOf(':') < 1) {
                return false;
            }
        }
        return true;
    }

    public Zone getZone(String id) {
        final Map<String, Zone> m = this.zonesByName.get();
        return m.get(id);
    }

    public static void main(String[] args) {
        System.out.println("de.marketmaker.istar.merger.web.ProfileResolver#ROOT_AUTHENTICATION_TYPE".matches("de\\.marketmaker(\\.\\w+)+#\\w+"));
    }
}