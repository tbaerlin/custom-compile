package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

/**
 * @author Ulrich Maurer
 *         Date: 08.01.13
 */
public interface StateSupport {
    public static final String PM_CONTAINER = "C"; // $NON-NLS$
    public static final String INSTRUMENT = "I"; // $NON-NLS$
    public static final String INVESTORS = "IV"; // $NON-NLS$
    public static final String MARKETS = "M"; // $NON-NLS$
    public static final String PORTFOLIO = "O"; // $NON-NLS$
    public static final String PAGES = "P"; // $NON-NLS$
    public static final String USER = "U"; // $NON-NLS$
    public static final String WATCHLIST = "W"; // $NON-NLS$
    public static final String SEARCH = "S"; // $NON-NLS$

    String getStateKey();
}
