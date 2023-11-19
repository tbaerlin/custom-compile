/*
 * ResetStatistics.java
 *
 * Created on 29.06.2010 11:56:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.statistics;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Detects all implementations of {@link de.marketmaker.istar.common.statistics.HasStatistics}
 * in its application context and provides a JMX method to invoke resetStatistics() for all of them.
 * @author oflege
 */
@ManagedResource
public class ResetStatistics extends ApplicationObjectSupport implements InitializingBean {
    private List<HasStatistics> delegates = null;

    private Set<String> excluded = Collections.emptySet();

    private volatile DateTime resetAt = null;

    public void setExcluded(String[] excluded) {
        this.excluded = new HashSet<>(Arrays.asList(excluded));
        this.logger.info("<setExcluded> " + this.excluded);
    }

    @ManagedOperation
    public void resetStatistics() {
        for (HasStatistics delegate : delegates) {
            delegate.resetStatistics();
        }
        this.resetAt = new DateTime();
    }

    public DateTime getResetAt() {
        return this.resetAt;
    }

    public void setHasStatisticss(List<HasStatistics> delegates) {
        this.delegates = delegates;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.delegates == null) {
            this.delegates = detectHasStatistics();
        }
    }

    private List<HasStatistics> detectHasStatistics() {
        final List<HasStatistics> result = new ArrayList<>();

        final Map<String, HasStatistics> beansByName
                = getApplicationContext().getBeansOfType(HasStatistics.class, false, false);

        for (String s : this.excluded) {
            beansByName.remove(s);
        }
        result.addAll(beansByName.values());

        this.logger.info("<detectHasStatistics> found " + beansByName.keySet());
        return result;
    }
    
}
