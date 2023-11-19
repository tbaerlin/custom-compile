/*
 * SpeedTestService.java
 *
 * Created on 07.03.2011 10:50:25
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
interface SpeedTestService {

    byte[] returnJunkOfSize(int size);

}
