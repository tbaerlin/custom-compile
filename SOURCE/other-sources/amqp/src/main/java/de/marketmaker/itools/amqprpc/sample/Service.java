/*
 * Service.java
 *
 * Created on 01.03.2011 16:48:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface Service {
    Object pingpong(Object in);
}
