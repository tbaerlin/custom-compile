/*
 * RatioDataStore.java
 *
 * Created on 28.10.2005 08:38:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.IOException;
import java.util.function.Consumer;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface RatioDataStore {

    /**
     * Store the ratio data in the default ratio file
     */
    void store(TypeData value) throws IOException;


    /**
     * Add stored content to TypeData (if any)
     */
    void restore(InstrumentTypeEnum type, Consumer<RatioData> consumer) throws Exception;

}
