/*
 * IllegalInstrumentTypeException.java
 *
 * Created on 29.07.2010 11:23:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger;

import java.util.Arrays;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author oflege
 */
public class IllegalInstrumentTypeException extends MergerException {

    public IllegalInstrumentTypeException(Instrument instrument, InstrumentTypeEnum... expected) {
        super(instrument.getId() + ".iid: type " + instrument.getInstrumentType()
            + " not in " + Arrays.toString(expected));
    }

    @Override
    public String getCode() {
        return "instrument.type.illegal";
    }
}
