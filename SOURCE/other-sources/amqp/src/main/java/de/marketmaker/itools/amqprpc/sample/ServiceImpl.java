/*
 * ServiceImpl.java
 *
 * Created on 01.03.2011 16:54:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class ServiceImpl implements Service {

    private final Log logger = LogFactory.getLog(getClass());

    public Object pingpong(Object in) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.logger.info(Thread.currentThread().getName() + " sending back: " + in);
        return in;
    }
}
