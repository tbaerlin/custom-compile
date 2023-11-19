/*
 * SpeedTestServiceImpl.java
 *
 * Created on 07.03.2011 10:51:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class SpeedTestServiceImpl implements SpeedTestService {
    public byte[] returnJunkOfSize(int size) {
        return new byte[size];
    }
}
