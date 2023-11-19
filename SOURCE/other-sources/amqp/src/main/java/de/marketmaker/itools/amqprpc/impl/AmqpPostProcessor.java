/*
 * AmqpAutomation.java
 *
 * Created on 19.03.15 09:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.common.spring.MmPropertyPlaceholderConfigurer;
import de.marketmaker.istar.common.spring.PrototypeManager;
import de.marketmaker.itools.amqprpc.AmqpProxyFactoryBean;
import de.marketmaker.itools.amqprpc.AmqpServiceExporter;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import de.marketmaker.itools.amqprpc.supervising.PeriodicSupervisor;

import static de.marketmaker.istar.common.Constants.DOMAIN_ID;
import static de.marketmaker.istar.common.Constants.MACHINE_NAME;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

/**
 * Adds beans to a BeanFactory that are used for remote method invocations over amqp.
 * The following beans will be added:
 * <dl>
 * <dt><b>amqpSupervisor</b> <code>de.marketmaker.itools.amqprpc.supervising.PeriodicSupervisor</code></dt>
 * <dd>checks for connection problems</dd>
 * <dt><b>amqpConnectionFactory</b> <code>com.rabbitmq.client.ConnectionFactory</code></dt>
 * <dd>the default amqp server host will be appropriate for the {@link de.marketmaker.istar.common.Constants#DOMAIN_ID}, but can be overridden using {@link #setHost(String)}</dd>
 * <dt><b>amqpConnectionManager</b> <code>de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager</code></dt>
 * <dd>&nbsp;</dd>
 * <dt><b>amqp&lt;Service&gt;Prototype</b> <code>de.marketmaker.itools.amqprpc.AmqpServiceExporter</code></dt>
 * <dd>for each key in {@link #exportedServices} and each service implemented by the bean with that name
 * <br><code>&lt;Service&gt;</code> is the simple name of an interface annotated with {@link AmqpAddress}
 * <em>unless</em> the configuration overrides the default queue. In that case, <code>&lt;Service&gt;</code>
 * is either the value of an explicit "name" property or the capitalized name of the bean that will
 * be exported.
 * <br>e.g.:<code>amqpNewsServerPrototype</code>
 * <br>instances of this bean will not be created automatically, they will be managed by the following bean:
 * </dd>
 * <dt><b>amqp&lt;Service&gt;Exporter</b> <code>de.marketmaker.istar.common.spring.PrototypeManager</code></dt>
 * <dd>manages instances of the corresponding <code>...Prototype</code> bean(s). Creates either
 * 1 or as many instances of the prototype as have been configured</dd>
 * <dt><b>amqp&lt;Service&gt;Queue</b> <code>de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl</code></dt>
 * <dd>queue used by the corresponding <code>...Prototype</code> bean(s) or the corresponding <code>...Proxy</code> bean</dd>
 * <dt><b>amqp&lt;Service&gt;Proxy</b> <code>de.marketmaker.itools.amqprpc.AmqpProxyFactoryBean</code></dt>
 * <dd>for each 'ref'-bean property used in the context that has a value starting with 'amqp'
 * <br>the service's class will be determined based on the setter method for the respective property
 * <br>properties of each proxy can be configured using {@link #setImportedServices(Map)}, but
 * this has no effect on which services are actually imported.
 * <br><code>&lt;Service&gt;</code> is the simple name of an interface annotated with {@link AmqpAddress}
 * <em>unless</em> the importedServices configuration overrides the default queue.
 * In that case, <code>&lt;Service&gt;</code>
 * is either the value of an explicit "name" property or the capitalized value of the "ref"-property
 * without the amqp prefix.
 * </dd>
 * </dl>
 * <p>Example
 * <pre>
 * &lt;bean class="de.marketmaker.itools.amqprpc.impl.AmqpPostProcessor">
 *   &lt;property name="exportedServicesNames" value="profileProvider,userMasterDataSource"/>
 * &lt;/bean>
 * </pre> or
 * <pre>
 * &lt;bean class="de.marketmaker.itools.amqprpc.impl.AmqpPostProcessor">
 *  &lt;property name="exportedServices">
 *   &lt;!-- key: name of bean to be exported, value: either property string or property map -->
 *   &lt;util:map>
 *    &lt;entry key="newsServer" value="numInstances=2,ttl=500"/>
 *    &lt;entry key="xyzServer">
 *     &lt;map>
 *      &lt;entry key="queue" value="istar.another.queue"/>
 *     &lt;/map>
 *    &lt;/entry>
 *   &lt;/util:map>
 *   &lt;/property>
 *  &lt;property name="importedServices">
 *   &lt;util:map>
 *   &lt;!-- key: name of proxy bean, value: either property string or property map -->
 *    &lt;entry key="amqpProfileProvider">
 *     &lt;map>
 *      &lt;entry key="local" value="true"/>
 *      &lt;entry key="rpcTimeout" value="10000"/>
 *     &lt;/map>
 *    &lt;/entry>
 *    &lt;entry key="amqpFeedConnectorEod" value="queue=istar.chicago3.eod"/>
 *   &lt;/util:map>
 *  &lt;/property>
 * &lt;/bean>
 * </pre>
 * The following system properties are evaluated:
 * <dl>
 * <dt><tt>-Damqp.local.queues=queue(,queue)*</tt></dt>
 * <dd>prepend <tt>${MACHINEID}.</tt> to the queue name of all services whose
 * {@link AmqpAddress#queue()} returns one of the specified queue names</dd>
 * <dt><tt>-Damqp.off=true</tt></dt>
 * <dd>prototype managers will not instantiate any prototypes when started so that no
 * services will be offered to other clients; amqp proxies for remote services will still work</dd>
 * </dl>
 * @author oflege
 */
public class AmqpPostProcessor implements BeanDefinitionRegistryPostProcessor, Ordered {

    private static final Set<String> LOCAL_QUEUES = StringUtils.commaDelimitedListToSet(
            System.getProperty("amqp.local.queues"));

    private static final boolean ALL_OFF = Boolean.getBoolean("amqp.off");

    private BeanFactory parentBeanFactory;

    /**
     * Since no {@link MmPropertyPlaceholderConfigurer} bean is available here we use magic numbers.
     */
    private static String getAmqpServerHost() {
        switch (DOMAIN_ID) {
            case "dev":
                return "te-dmxml-msgsrv.ffm.vwd.cloud";
            case "test":
                return "temsgsrv";
            case "teffm":
                return "testcore";
            case "cloud":
                return "pr-dmxml-msgsrv";
            default:
                return "msgsrv";
        }
    }

    protected final Log logger = LogFactory.getLog(getClass());

    private int slowInvocationThreshold = 2000;

    private Map<String, Object> exportedServices;

    private Map<String, Object> importedServices = emptyMap();

    private BeanDefinitionRegistry bdr;

    private int numAmqpBeans = 0;

    private int maxNumThreads = 0;

    private String host = getAmqpServerHost();

    private String virtualHost = "istar";

    private String username = "merger";

    private String password = "merger";

    private String port = "5672";

    private int prefetchCount = 1;

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public void setMaxNumThreads(int maxNumThreads) {
        this.maxNumThreads = maxNumThreads;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setSlowInvocationThreshold(int slowInvocationThreshold) {
        this.slowInvocationThreshold = slowInvocationThreshold;
    }

    public void setExportedServicesNames(String[] names) {
        this.exportedServices = Arrays.stream(names).collect(toMap(s -> s, s -> emptyMap()));
    }

    public void setExportedServices(Map<String, Object> exportedServices) {
        this.exportedServices = exportedServices;
    }

    public void setImportedServices(Map<String, Object> importedServices) {
        this.importedServices = importedServices;
    }

    private Class<?> getClass(BeanDefinition bd) {
        if (bd.getBeanClassName() != null) {
            return ClassUtils.resolveClassName(bd.getBeanClassName(), null);
        }
        if (bd.getParentName() != null) {
            return getClass(bdr.getBeanDefinition(bd.getParentName()));
        }
        throw new IllegalArgumentException(String.valueOf(bd));
    }

    public int getOrder() {
        return 1;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // empty
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry r) {
        this.bdr = r;
        if (r instanceof HierarchicalBeanFactory) {
            this.parentBeanFactory = ((HierarchicalBeanFactory) r).getParentBeanFactory();
        }

        if (this.exportedServices != null) {
            addServiceExporters();
        }
        addServiceProxies();

        if (this.numAmqpBeans > 0 && !isDefinedInParent("amqpSupervisor")) {
            GenericBeanDefinition ps = new GenericBeanDefinition();
            ps.setBeanClass(PeriodicSupervisor.class);
            ps.setPropertyValues(getPropertyValues("checkInterval", 60));
            registerBean("amqpSupervisor", ps);

            GenericBeanDefinition cm = new GenericBeanDefinition();
            cm.setBeanClass(AmqpRpcConnectionManager.class);
            cm.setPropertyValues(getPropertyValues("connectionFactory", ref("amqpConnectionFactory")));
            if (this.maxNumThreads > 0) {
                cm.getPropertyValues().add("maxNumThreads", this.maxNumThreads);
            }
            registerBean("amqpConnectionManager", cm);

            GenericBeanDefinition cf = new GenericBeanDefinition();
            cf.setBeanClass(ConnectionFactory.class);
            cf.setPropertyValues(new MutablePropertyValues());
            cf.getPropertyValues().add("host", this.host);
            cf.getPropertyValues().add("username", this.username);
            cf.getPropertyValues().add("password", this.password);
            cf.getPropertyValues().add("port", this.port);
            cf.getPropertyValues().add("virtualHost", this.virtualHost);
            cf.getPropertyValues().add("requestedHeartbeat", "15");
            registerBean("amqpConnectionFactory", cf);
        }

        this.bdr = null;
    }

    private boolean isDefinedInParent(String name) {
        return this.parentBeanFactory != null && this.parentBeanFactory.containsBean(name);
    }

    private boolean registerBean(String name, BeanDefinition bd) {
        this.numAmqpBeans++;
        this.bdr.registerBeanDefinition(name, bd);
        this.logger.info("<registerBean> " + name + ": " + bd + ", " + toString(bd.getPropertyValues()));
        return true;
    }

    private String toString(MutablePropertyValues values) {
        return Arrays.stream(values.getPropertyValues())
                .map(pv -> pv.getName() + "=" + pv.getValue())
                .collect(Collectors.joining(", ", "Properties[", "]"));
    }

    private void registerAlias(String name, String alias) {
        this.bdr.registerAlias(name, alias);
        this.logger.info("<registerAlias> '" + name + "' aka '" + alias + "'");
    }

    private Object ref(String name) {
        return new RuntimeBeanReference(name);
    }

    private void addServiceExporters() {
        for (Map.Entry<String, Object> e : this.exportedServices.entrySet()) {
            addServiceExporter(e.getKey(), toProperties(e.getKey(), e.getValue()));
        }
    }

    private void addServiceExporter(String name, Map<String, Object> properties) {
        BeanDefinition bd = bdr.getBeanDefinition(name);

        Map<Class<?>, AmqpAddress> addresses = findAddresses(getClass(bd));
        for (Map.Entry<Class<?>, AmqpAddress> e : addresses.entrySet()) {
            //noinspection unchecked
            Map<String, Object> serviceProperties = properties.containsKey(e.getKey().getName())
                    ? (Map<String, Object>) properties.get(e.getKey().getName())
                    : properties;
            addServiceExporter(name, e.getKey(), serviceProperties);
        }
    }

    private static Map<String, Object> toProperties(String key, Object value) {
        final Map<String, Object> result = toProperties(value);
        if (result.containsKey("queue") && !result.containsKey("name")) {
            result.put("name", StringUtils.capitalize(key.startsWith("amqp") ? key.substring(4) : key));
        }
        return result;
    }

    private static Map<String, Object> toProperties(Object value) {
        if (value instanceof Map) {
            //noinspection unchecked
            return (Map<String, Object>) value;
        }
        return Arrays.stream(String.valueOf(value).split(","))
                .map(s -> s.split("=")).collect(toMap(
                        (String[] t) -> t[0].trim(), // key
                        (String[] t) -> t.length > 1 ? t[1].trim() : "" // value
                ));
    }

    private void addServiceExporter(String service, Class<?> clazz, Map<String, Object> map) {
        AmqpAddress address = clazz.getAnnotation(AmqpAddress.class);
        String queueBeanName = registerAddress(clazz, true, map);

        String prototypeBeanName = beanName(clazz, map, "Prototype");

        GenericBeanDefinition e = new GenericBeanDefinition();
        e.setBeanClass(AmqpServiceExporter.class);
        e.setScope("prototype");
        e.setPropertyValues(new MutablePropertyValues());
        e.getPropertyValues().add("connectionManager", ref("amqpConnectionManager"));
        e.getPropertyValues().add("rpcServerSettings.numberOfMessagesToPrefetch", this.prefetchCount);
        e.getPropertyValues().add("rpcServerSettings.useServerId", address.dedicatedConsumer());
        e.getPropertyValues().add("supervisor", ref("amqpSupervisor"));
        e.getPropertyValues().add("service", ref(service));
        e.getPropertyValues().add("serviceInterface", clazz.getName());
        e.getPropertyValues().add("address", ref(queueBeanName));
        addRawByteArrayMethods(e.getPropertyValues(), clazz);
        registerBean(prototypeBeanName, e);

        GenericBeanDefinition m = new GenericBeanDefinition();
        m.setBeanClass(PrototypeManager.class);
        m.setPropertyValues(getPropertyValues("beanName", prototypeBeanName));
        m.getPropertyValues().add("numInstances", getNumInstances(map));

        registerBean(beanName(clazz, map, "Exporter"), m);
    }

    private Object getNumInstances(Map<String, Object> map) {
        return ALL_OFF ? "0" : map.getOrDefault("numInstances", "1");
    }

    private void addRawByteArrayMethods(MutablePropertyValues pvs, Class<?> clazz) {
        // TODO: limit names by regex in AmqpAddress?
        final Set<String> methodNames = Arrays.stream(clazz.getMethods())
                .filter(m -> m.getReturnType() == byte[].class)
                .map(Method::getName)
                .collect(Collectors.toSet());
        if (!methodNames.isEmpty()) {
            pvs.add("rawByteArrayReplyMethods", methodNames);
        }
    }

    private void addServiceProxies() {
        for (String name : bdr.getBeanDefinitionNames()) {
            BeanDefinition bd = bdr.getBeanDefinition(name);
            if (bd.getBeanClassName() == null) {
                continue;
            }
            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(getClass(bd));
            for (PropertyDescriptor pd : pds) {
                addProxy(name, bd, pd);
            }
        }
    }

    private void addProxy(String name, BeanDefinition bd, PropertyDescriptor pd) {
        if (pd.getWriteMethod() == null) {
            return;
        }
        String writeMethodName = pd.getWriteMethod().getName();

        Class<?> clazz = pd.getWriteMethod().getParameterTypes()[0];
        if (BeanUtils.isSimpleProperty(clazz)) {
            return;
        }
        PropertyValue pv = bd.getPropertyValues().getPropertyValue(pd.getName());
        if (pv == null || !(pv.getValue() instanceof RuntimeBeanReference)) {
            return;
        }
        RuntimeBeanReference rbr = (RuntimeBeanReference) pv.getValue();
        if (bdr.isBeanNameInUse(rbr.getBeanName()) || !rbr.getBeanName().startsWith("amqp")) {
            return;
        }
        Map<String, Object> properties = getImportProperties(rbr.getBeanName());

        clazz = resolveProxyClass(clazz, properties);

        String beanName = beanName(clazz, properties, "Proxy");
        if (!bdr.isBeanNameInUse(beanName)) {
            Map<Class<?>, AmqpAddress> addresses = findAddresses(clazz);
            if (addresses.size() > 1) {
                this.logger.warn("<addProxy> ignoring " + name + "." + writeMethodName
                        + " => " + addresses.toString());
                return;
            }
            if (addresses.isEmpty()) {
                return;
            }
            createServiceProxy(addresses.keySet().iterator().next(), beanName, properties);
            if (!rbr.getBeanName().equals(beanName)) {
                registerAlias(beanName, rbr.getBeanName());
            }
            registerAlias(beanName, properties);
        }
        this.logger.info("<addProxy> " + name + "." + writeMethodName + " -> ref:" + beanName);
        bd.getPropertyValues().add(pd.getName(), ref(beanName));
    }

    private Map<String, Object> getImportProperties(final String key) {
        return this.importedServices.containsKey(key)
                ? toProperties(key, this.importedServices.get(key))
                : this.importedServices;
    }

    /**
     * Given an @AmqpInterface C that extends interfaces A and B and clients with properties
     * a and b of types A and B, respectively, we would like to create a proxy for interface C, but
     * the post processor has no way of detecting that C even exists. To tell the post processor
     * which class it should actually use, we specify a "class" property for the imported service,
     * i.e. the bean reference looks like this
     * <pre>
     *     &lt;-- a is a property of type A -->
     *     &lt;property key="a" ref="amqpC"/>
     * </pre>
     * and for the post processor we configure
     * <pre>
     * &lt;property name="importedServices">
     *   &lt;util:map>
     *     &lt;entry key="amqpC" value="class=C"/>
     * </pre>
     */
    private Class<?> resolveProxyClass(Class<?> clazz, Map<String, Object> proxyProperties) {
        if (!proxyProperties.containsKey("class")) {
            return clazz;
        }
        final Class<?> c = ClassUtils.resolveClassName(String.valueOf(proxyProperties.get("class")),
                clazz.getClassLoader());
        if (!c.isAnnotationPresent(AmqpAddress.class)) {
            throw new IllegalArgumentException(c.getName() + " has no @AmqpAddress");
        }
        if (!clazz.isAssignableFrom(c)) {
            throw new IllegalArgumentException(clazz.getName() + " is not assignable from "
                    + c.getName());
        }
        this.logger.info("<resolveProxyClass> " + clazz.getName() + " => " + c.getName());
        return c;
    }

    private void registerAlias(String beanName, Map<String, Object> properties) {
        if (properties.containsKey("alias")) {
            registerAlias(beanName, String.valueOf(properties.get("alias")));
        }
    }

    private void createServiceProxy(Class<?> clazz, String beanName, Map<String, Object> map) {
        if (isDefinedInParent(beanName)) {
            this.logger.debug("<createServiceProxy> " + beanName + " is defined in parent factory");
            return;
        }
        AmqpAddress address = clazz.getAnnotation(AmqpAddress.class);
        String queueBeanName = registerAddress(clazz, false, map);

        GenericBeanDefinition p = new GenericBeanDefinition();
        p.setBeanClass(AmqpProxyFactoryBean.class);
        p.setPropertyValues(new MutablePropertyValues());
        p.getPropertyValues().add("connectionManager", ref("amqpConnectionManager"));
        p.getPropertyValues().add("rpcTimeout", map.getOrDefault("rpcTimeout", address.rpcTimeout()));
        p.getPropertyValues().add("slowInvocationThreshold", this.slowInvocationThreshold);
        p.getPropertyValues().add("supervisor", ref("amqpSupervisor"));
        p.getPropertyValues().add("serviceInterface", clazz.getName());
        p.getPropertyValues().add("address", ref(queueBeanName));
        addRawByteArrayMethods(p.getPropertyValues(), clazz);
        registerBean(beanName, p);
    }

    private String registerAddress(Class<?> clazz, boolean consumer, Map<String, Object> map) {
        String queueBeanName = beanName(clazz, map, "Queue");

        AmqpAddress address = clazz.getAnnotation(AmqpAddress.class);
        String requestQueue = getQueueName(clazz, address, consumer, map);

        GenericBeanDefinition a = new GenericBeanDefinition();
        a.setBeanClass(AmqpRpcAddressImpl.class);
        a.setPropertyValues(new MutablePropertyValues());
        a.getPropertyValues().add("exchange", address.exchange());
        a.getPropertyValues().add("settings.requestQueueMessageTTL", map.getOrDefault("ttl", address.ttl()));
        a.getPropertyValues().add("settings.queueAutoDelete", requestQueue.startsWith(MACHINE_NAME));
        a.getPropertyValues().add("requestQueue", requestQueue);
        registerBean(queueBeanName, a);

        if (consumer) {
            this.logger.info("<registerAddress> consume from <--|||" + requestQueue + "|||");
        }
        else {
            this.logger.info("<registerAddress> publish to -->|||" + requestQueue + "|||");
        }

        return queueBeanName;
    }

    private String resolveQueueName(Class<?> svc, AmqpAddress address, Map<String, Object> map) {
        final Object q = map.get("queue");
        if (q != null) {
            this.logger.info("<resolveQueueName> " + svc + " from properties: '" + q + "'");
            return String.valueOf(q);
        }
        return address.queue();
    }

    private String getQueueName(Class<?> svc, AmqpAddress address, boolean consumer,
            Map<String, Object> map) {
        final String queue = resolveQueueName(svc, address, map);
        if (isServiceExportedByDevMachine(consumer) || LOCAL_QUEUES.contains(queue)
                || Boolean.valueOf(String.valueOf(map.get("local")))) {
            return MACHINE_NAME + "." + queue;
        }
        return queue;
    }

    private boolean isServiceExportedByDevMachine(boolean consumer) {
        return consumer && "dev".equals(DOMAIN_ID);
    }

    private String beanName(Class<?> clazz, Map<String, Object> map, final String suffix) {
        final Object o = map.get("name");
        if (o == null) {
            return beanName(clazz, suffix);
        }
        String name = String.valueOf(o);
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("empty name");
        }
        if (name.equals(clazz.getSimpleName())) {
            throw new IllegalArgumentException("cannot use name " + name);
        }
        return beanName(name, suffix);
    }

    private String beanName(Class<?> clazz, final String suffix) {
        return beanName(clazz.getSimpleName(), suffix);
    }

    private String beanName(String name, final String suffix) {
        return "amqp" + name + suffix;
    }

    private MutablePropertyValues getPropertyValues(String name, Object value) {
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add(name, value);
        return pvs;
    }

    private Map<Class<?>, AmqpAddress> findAddresses(Class<?> clazz) {
        return addAddresses(clazz, new HashMap<>());
    }

    private Map<Class<?>, AmqpAddress> addAddresses(Class<?> clazz,
            Map<Class<?>, AmqpAddress> annotations) {
        if (clazz.isInterface()) {
            AmqpAddress annotation = clazz.getAnnotation(AmqpAddress.class);
            if (annotation != null) {
                annotations.put(clazz, annotation);
            }
        }
        AmqpAddress annotation = clazz.getAnnotation(AmqpAddress.class);
        if (annotation != null) {
            if (!StringUtils.hasText(annotation.queue())) {
                return annotations;
            }
            annotations.put(clazz, annotation);
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            addAddresses(ifc, annotations);
        }
        return annotations;
    }

    public static void main(String[] args) {
        ApplicationContext ac = new FileSystemXmlApplicationContext(args[0]);
    }
}
