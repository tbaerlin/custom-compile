/*
 * RmiProxyFactoryBean.java
 *
 * Created on 02.03.2005 10:17:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.rmi;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.rmi.RmiClientInterceptorUtils;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RmiProxyFactory extends RmiProxyFactoryBean implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TreeSet<RmiServiceDescriptor> usables = new TreeSet<>();
    private final Set<RmiServiceDescriptor> deads = new HashSet<>();

    private final Queue<RmiServiceDescriptor> queue = new LinkedList<>();
    private final Thread thread = new Thread(this, "service-reconnect");

    private static final Map<String, RmiProxyFactory> INSTANCES =
            new HashMap<>();

    /**
     * It may be better to use static getInstance, which returns the same instance for identical
     * serviceDescriptors. Since each instance starts a background thread to check service
     * availability, getInstance avoids creating many threads for identical proxies.
     */
    public RmiProxyFactory() {
    }

    /**
     * Returns an RmiProxyFactory instance for the given serviceDescriptor; if no such object
     * already exists, it will be created.
     * @param serviceDescriptor
     * @param serviceClass class of the proxied service
     * @return RmiProxyFactory
     * @throws Exception if serviceDescriptor is not valid.
     */
    public static synchronized RmiProxyFactory getInstance(String serviceDescriptor, Class serviceClass) throws Exception {
        RmiProxyFactory existing = INSTANCES.get(serviceDescriptor);
        if (existing != null) {
            return existing;
        }

        final RmiServiceDescriptorEditor editor = new RmiServiceDescriptorEditor();
        editor.setAsText(serviceDescriptor);
        final RmiProxyFactory result = new RmiProxyFactory();
        result.setRmiServices((RmiServiceDescriptor[]) editor.getValue());
        result.setServiceInterface(serviceClass);
        result.afterPropertiesSet();

        INSTANCES.put(serviceDescriptor, result);

        return result;
    }

    /**
     * @param descriptors
     */
    public void setRmiServices(RmiServiceDescriptor[] descriptors) {
        this.usables.addAll(Arrays.asList(descriptors));
    }

    private void initialConnect() {
        final StringBuilder stb = new StringBuilder();

        for (Iterator it = this.usables.iterator(); it.hasNext();) {
            final RmiServiceDescriptor descriptor = (RmiServiceDescriptor) it.next();

            stb.append(";");
            stb.append(descriptor.getPriority());
            stb.append(",");
            stb.append(descriptor.getServiceUrl());

            if (!connect(descriptor)) {
                it.remove();
                this.deads.add(descriptor);
            }
        }

        setServiceUrl(stb.substring(1));
    }

    private boolean connect(final RmiServiceDescriptor descriptor) {
        final RmiProxyFactoryBean proxy = new RmiProxyFactoryBean();
        proxy.setServiceUrl(descriptor.getServiceUrl());
        proxy.setServiceInterface(getServiceInterface());
        proxy.setRefreshStubOnConnectFailure(false);
        proxy.setLookupStubOnStartup(true);
        try {
            proxy.afterPropertiesSet();
            final Remote server = (Remote) proxy.getObject();
            descriptor.setService(server);
        }
        catch (Exception e) {
            descriptor.setService(null);
            this.logger.warn("<connect> service not connected: " + descriptor, e);
            return false;
        }

        return true;
    }

    protected synchronized Remote lookupStub() throws RemoteLookupFailureException {
        if (this.queue.isEmpty()) {
            fillQueue();

            if (this.queue.isEmpty()) {
                return null;
            }
        }

        return this.queue.remove().getService();
    }

    private void fillQueue() {
        if (this.usables.isEmpty()) {
            return;
        }

        final int priority = this.usables.first().getPriority();
        for (final RmiServiceDescriptor descriptor : this.usables) {
            if (descriptor.getPriority() == priority) {
                this.queue.add(descriptor);
            }
            else {
                break;
            }
        }
    }

    public void afterPropertiesSet() {
        this.thread.setDaemon(true);
        this.thread.start();

        setCacheStub(false);
        setLookupStubOnStartup(false);
        initialConnect();
        super.afterPropertiesSet();
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Remote stub = getStub();

        if (stub == null) {
            throw new RemoteLookupFailureException("RMI lookup for service [" + getServiceUrl() + "] failed", null);
        }

        try {
            return doInvoke(invocation, stub);
        }
        catch (RemoteConnectFailureException ex) {
            addToDead(stub);
            return invoke(invocation);
        }
        catch (RemoteLookupFailureException ex) {
            addToDead(stub);
            return invoke(invocation);
        }
        catch (RemoteException ex) {
            if (RmiClientInterceptorUtils.isConnectFailure(ex)) {
                addToDead(stub);
                return invoke(invocation);
            }
            else {
                throw ex;
            }
        }
    }

    public void run() {
        while (true) {
            final Set<RmiServiceDescriptor> toDo;
            synchronized (this) {
                toDo = new HashSet<>(this.deads);
            }

            for (final RmiServiceDescriptor descriptor : toDo) {
                if (connect(descriptor)) {
                    synchronized (this.deads) {
                        this.deads.remove(descriptor);
                        this.usables.add(descriptor);
                    }
                }
            }

            synchronized (this) {
                try {
                    final long waitTime = hasAnyDeads()?10*1000:Long.MAX_VALUE;
                    wait(waitTime);
                }
                catch (InterruptedException ignore) {
                }
            }
        }
    }

    private synchronized boolean hasAnyDeads() {
        return !this.deads.isEmpty();
    }

    private synchronized void addToDead(Remote stub) {
        for (Iterator it = this.usables.iterator(); it.hasNext();) {
            final RmiServiceDescriptor descriptor = (RmiServiceDescriptor) it.next();

            if (descriptor.getService() == stub) {
                it.remove();
                this.deads.add(descriptor);
                break;
            }
        }

        notify();
    }
}