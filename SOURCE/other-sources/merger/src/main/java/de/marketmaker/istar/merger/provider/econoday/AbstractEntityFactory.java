/*
 * AbstractEntityFactory.java
 *
 * Created on 25.05.11 09:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author zzhao
 */
public abstract class AbstractEntityFactory<K, V extends Version> implements InitializingBean {

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String table;

    private final String column;

    private JdbcTemplate jt;

    private ActiveMonitor monitor;

    private Executor executor;

    private long lastRevision = Long.MIN_VALUE;

    protected AbstractEntityFactory(String table) {
        this(table, null);
    }

    protected void initRevision(long val) {
        this.lastRevision = val;
    }

    protected AbstractEntityFactory(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public String getTable() {
        return table;
    }

    public void setDataSource(DataSource dataSource) {
        this.jt = new JdbcTemplate(dataSource);
    }

    protected JdbcTemplate getJdbcTemplate() {
        return this.jt;
    }

    public void setMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.jt, "data source required");
        Assert.isTrue((null == this.monitor) || (null != this.executor),
                "executor required, refreshing entities in monitor thread not allowed");
        // initial load of resources before registering by monitor
        doRefresh(Long.MAX_VALUE);
        if (null != this.monitor) {
            final DBResource res = (null == this.column)
                    ? new DBResource(this.jt, this.table)
                    : new DBResource(this.jt, this.table, this.column);
            res.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    refresh((Long) evt.getNewValue());
                }
            });
            this.monitor.addResource(res);
        }
    }

    private void refresh(final long newVersion) {
        this.executor.execute(new Runnable() {
            public void run() {
                doRefresh(newVersion);
            }
        });
    }

    private void doRefresh(long newVersion) {
        if (newVersion <= this.lastRevision) {
            this.logger.warn("<doRefresh> refresh for [" + newVersion + "] ignored" +
                    " since last revision is [" + this.lastRevision + "]");
            return;
        }

        try {
            final TimeTaker tt = new TimeTaker();
            final Map<K, V> newValues = query(this.lastRevision);
            for (Map.Entry<K, V> entry : newValues.entrySet()) {
                final V value = entry.getValue();
                this.cache.put(entry.getKey(), value);
                this.lastRevision = Math.max(this.lastRevision, value.getRevision());
            }
            this.logger.info("<doRefresh> refreshed [" + newValues.size() + "] entities from: "
                    + this.table + " in: " + tt);
        } catch (Exception e) {
            this.logger.error("<doRefresh> cannot refreshing entities", e);
        }
    }

    protected abstract Map<K, V> query(long fromRevision);

    public V get(K key) {
        return this.cache.get(key);
    }

    public List<V> values() {
        return new ArrayList<>(this.cache.values());
    }
}
