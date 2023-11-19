/*
 * FTACommentary.java
 *
 * Created on 6/7/13 2:48 PM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;

/**
 * @author Stefan Willenbrock
 */
public interface FTACommentary {

    long getInstrumentid();

    String getPrevisioni();

    String getSituazione();

    String getAnalisiTecnica();

    String getSegnaliOperativi();

    String getAnalisiStatistica();
}
