/*
 * AbstractShowStats.java
 *
 * Created on 04.03.2010 13:52:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import org.springframework.beans.BeansException;

/**
 * @author oflege
 */
abstract class AbstractShowStats extends ApplicationObjectSupport implements ShowStats {

    private volatile int mdpsPid = -1;

    public void setMdpsProcessId(int pid) {
        this.mdpsPid = pid;
    }

    @Override
    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();
    }

    protected <T> T getBean(Class<? extends T> clazz) {
        final String[] names = getApplicationContext().getBeanNamesForType(clazz);
        if (names.length == 0) {
            return null;
        }
        if (names.length > 1) {
            this.logger.warn("<getBean> >1 instances of " + clazz.getName()
                    + ", returning bean with name '" + names[0] + "'");
        }
        //noinspection unchecked
        return (T) getApplicationContext().getBean(names[0]);
    }

    protected OutputBuilder createBuilder() {
        return new OutputBuilder().printHeader(this.mdpsPid);
    }
}
