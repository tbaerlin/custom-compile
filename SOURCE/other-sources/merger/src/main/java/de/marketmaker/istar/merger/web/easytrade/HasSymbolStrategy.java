package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Ulrich Maurer
 *         Date: 05.07.12
 */
public interface HasSymbolStrategy {
    /**
     * Specifies the <tt>symbol</tt>'s key system, default is <tt>AUTO</tt>. For certain symbols,
     * the key system can be detected automatically by inspecting the key. The following rules are
     * evaluated in order to determine a strategy if <tt>AUTO</tt> is specified:
     * <table border=1>
     *     <tr><th>symbol</th><th>symbol strategy</th><th>description</th><th>example</th></tr>
     *     <tr><td><em>symbol</em>.iid</td><td>IID</td><td>vwd specific instrument id</td><td>448.iid</td></tr>
     *     <tr><td><em>symbol</em>.qid</td><td>QID</td><td>vwd specific quote id</td><td>22.qid</td></tr>
     *     <tr><td><em>isin</em></td><td>ISIN</td><td>International Securities Identification Number</td><td>de0007100000</td></tr>
     *     <tr><td><tt>abc(.xyz)+*</tt></td></td><td>VWDCODE_PREFIX</td><td>vwd code prefix (only available in some contexts)</td><td>710000.DTB.*</td></tr>
     *     <tr><td><tt>abc(.xyz)+</tt></td></td><td>VWDCODE</td><td>vwd code</td><td>846900.ETR</td></tr>
     *     <tr><td>[0-9]+(_[0-9]){3}</td></td><td>BIS_KEY</td><td>bis key (vwd internal)</td><td>5_1000_2000_43</td></tr>
     *     <tr><td><em>symbol</em></td></td><td>WKN</td><td>Wertpapierkennnummer</td><td>710000</td></tr>
     * </table>
     * <tt>QID</tt>, <tt>VWDCODE_PREFIX</tt>, <tt>VWDCODE</tt> and <tt>BIS_KEY</tt>
     * specify a specific quote whereas the other symbol strategies refer to instrument symbols.
     */
    SymbolStrategyEnum getSymbolStrategy();
}
