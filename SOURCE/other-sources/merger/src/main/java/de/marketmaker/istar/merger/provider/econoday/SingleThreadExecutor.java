/*
 * VendorDataListener.java
 *
 * Created on 15.04.11 11:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.util.concurrent.ExecutorServiceUtil;

/**
 * @author zzhao
 */
public class SingleThreadExecutor implements InitializingBean, DisposableBean, Executor {

    private static final Set<String> NAME_SET = new HashSet<>(4);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    private int timeOutSeconds = 60;

    private ExecutorService es;

    public void setName(String name) {
        this.name = name;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.name, "Thread name required");
        Assert.isTrue(!NAME_SET.contains(this.name), "Duplicated thread name: '" + this.name + "'");
        NAME_SET.add(this.name);
        this.es = Executors.newSingleThreadExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, name);
            }
        });
    }

    public void setTimeOutSeconds(int timeOutSeconds) {
        this.timeOutSeconds = timeOutSeconds;
    }

    public void execute(Runnable r) {
        this.es.execute(r);
    }

    public void destroy() throws Exception {
        ExecutorServiceUtil.shutdownAndAwaitTermination(this.es, this.timeOutSeconds);
        this.logger.info("<destroy> executor '" + this.name + "' stopped");
    }
}
