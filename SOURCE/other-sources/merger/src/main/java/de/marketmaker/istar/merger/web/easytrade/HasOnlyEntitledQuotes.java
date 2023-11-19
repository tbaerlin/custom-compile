package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Ulrich Maurer
 *         Date: 09.07.12
 */
public interface HasOnlyEntitledQuotes {
    /**
     * If <code>true</code> only quotes with access permission (according to the user profile) are returned,
     * if <code>false</code> all quotes of the list are returned. Default value: <code>false</code>.
     */
    boolean isOnlyEntitledQuotes();
}