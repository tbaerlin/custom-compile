/*
 * Supervisable.java
 *
 * Created on 09.03.2011 17:49:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.supervising;

/**
 * This interface provides methods to check sanity of an object. Subtype {@link SupervisableAndRepairable}
 * also provides self-repair methods.
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface Supervisable {
    /**
     * @return true iff object is working properly
     */
    boolean everythingOk();

    /**
     * @return the message used in log messages, when {@link #everythingOk()} returns false.
     */
    String logMessageInCaseOfError();
}
