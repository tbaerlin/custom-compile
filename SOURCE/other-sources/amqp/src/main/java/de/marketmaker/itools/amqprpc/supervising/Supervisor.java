/*
 * Supervisor.java
 *
 * Created on 07.03.2011 15:57:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.supervising;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementations of this interface are active components that monitor correct functioning
 * of their {@link SupervisableAndRepairable} object. This should mean calling
 * {@link SupervisableAndRepairable#everythingOk()} from "time to time" and
 * {@link SupervisableAndRepairable#tryToRecover()}
 * in case the former indicates an error condition.
 * <p/>
 * <p/>
 * Implementations are expected to start their monitoring task in
 * {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()}
 * and stop them in {@link org.springframework.beans.factory.DisposableBean#destroy()}
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface Supervisor {

    /**
     * Property, if set the supervisor will use a daemon thread for his task
     *
     * @param usingDaemonThread
     */
    void setUsingDaemonThread(boolean usingDaemonThread);

    void addSupervisedObject(SupervisableAndRepairable supervisedObject);

    void removeSupervisedObject(SupervisableAndRepairable supervisedObject);
}
