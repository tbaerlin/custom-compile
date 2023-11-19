/*
 * SymbolCommand.java
 *
 * Created on 01.08.2006 13:06:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SymbolCommand extends HasSymbolCommand {

    /**
     * @return Identifier for an instrument or a quote that will be interpreted according to
     * the specified <tt>symbolStrategy</tt>.
     * @sample 25548.qid
     */
    String getSymbol();

}
