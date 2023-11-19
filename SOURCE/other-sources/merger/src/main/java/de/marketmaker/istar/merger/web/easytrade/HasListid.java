package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Ulrich Maurer
 *         Date: 09.07.12
 */
public interface HasListid {
    /**
     * The list name or index symbol of the requested list.
     * <p>
     *   Sample values are:
     *   <ul>
     *       <li>106547.qid</li>
     *       <li>846900 (together with symbolStrategy=WKN)</li>
     *       <li>euribor (named list as defined in MDP)</li>
     *   </ul>
     * </p>
     * @sample 106547.qid
     */
    String getListid();
}
