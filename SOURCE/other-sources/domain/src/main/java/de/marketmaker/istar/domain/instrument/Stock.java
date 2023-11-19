/*
 * Bond.java
 *
 * Created on 17.12.2004 11:48:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;



/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: Stock.java,v 1.1 2004/12/17 17:51:57 tkiesgen Exp $
 */
public interface Stock extends Instrument {
    int getGeneralMeetingDate();
}
