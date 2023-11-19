/*
 * Supervisable.java
 *
 * Created on 07.03.2011 13:18:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.supervising;

/**
 * Implementations of this interface provide some sort of persistent service, that might
 * need monitoring. Implementations can be augmented by a {@link Supervisor}
 * 
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface SupervisableAndRepairable extends Supervisable {

    /**
     * Try some self-repair. This method can assume some kind of misbehavior indicated by
     * {@link #everythingOk()} being {@code false}.
     */
    void tryToRecover();

}
