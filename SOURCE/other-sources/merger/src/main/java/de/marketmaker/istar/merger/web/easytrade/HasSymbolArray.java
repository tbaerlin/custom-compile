package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Ulrich Maurer
 *         Date: 09.07.12
 */
public interface HasSymbolArray {
    /**
     * @return Identifiers for instruments or quotes that will be interpreted according to
     * the specified <tt>symbolStrategy</tt>
     * @sample 25548.qid
     */
    String[] getSymbol();
}
