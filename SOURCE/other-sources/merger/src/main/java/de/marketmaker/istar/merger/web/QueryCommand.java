/*
 * QueryCommand.java
 *
 * Created on 06.06.12 08:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

/**
 * @author zzhao
 */
public interface QueryCommand {

    /**
     * A query string composed of one or more terms combined with <code>and</code>
     * <p>
     * example:<pre>
     * &#39;Term1 AND Term2&#39; or &#39;Term1 &amp;&amp; Term2&#39;</pre>
     * </p>
     *
     * <p>
     * A term is a predicate on a certain field and can be formulated as:<pre>
     * field operator value</pre>
     * whereby:
     * <ul>
     * <li><b>field</b> is application specific, usually a string like 'vwdCode', 'wkn' or 'rating'<br/><br/></li>
     * <li>allowed <b>operators</b> are:
     * <table border="1">
     * <tr><th>Operator</th><th>Explanation</th></tr>
     * <tr><td>==</td><td>equals to</td></tr>
     * <tr><td>=</td><td>equals to</td></tr>
     * <tr><td>!=</td><td>unequal to</td></tr>
     * <tr><td>&gt;</td><td>greater than</td></tr>
     * <tr><td>&gt;=</td><td>no less than</td></tr>
     * <tr><td>&lt;</td><td>less than</td></tr>
     * <tr><td>&lt;=</td><td>no greater than</td></tr>
     * </table>
     * <br/></li>
     * <li><b>value</b> can be of following type:
     * <table border="1">
     * <tr><th>Type</th><th>Format</th><th>Example</th></tr>
     * <tr><td>number</td><td>integer or decimal format</td><td>4711 or 3.1415926</td></tr>
     * <tr><td>date time</td><td>application specific, normally yyyy-MM-dd</td><td>2012-01-01</td></tr>
     * <tr><td>boolean</td><td>true or false</td><td>true or false</td></tr>
     * <tr><td>string</td><td>bare or in quotation</td><td>DE0007 or &#39;GmbH&#39;</td></tr>
     * </table>
     * <br/></li>
     * </ul>
     * </p>
     *
     * <p>
     * multiple values can be concatenated with &#64;, which is automatically interpreted as
     * <code>or</code>-Terms,<br/>some query examples:
     * <table border="1">
     * <tr><th>Example</th><th>Explanation</th></tr>
     * <tr><td>askPrice&gt;'4'</td><td>asked price greater than 4</td></tr>
     * <tr><td>wkn=='710&#64;820' AND yieldPerYear &gt;'5'</td><td>WKN starts with 710 or 820 and annual yield greater than 5%</td></tr>
     * <tr><td>issuername=&#39;~^.*Banken.*$&#39;</td><td>issuer name matching given regular expression</td></tr>
     * </table>
     * </p>
     *
     * <p>
     * regular expression follow the Java convention, some examples:
     * <table border="1">
     * <tr><th>Example</th><th>Explanation</th></tr>
     * <tr> <td>&#39;~^.*Banken.*$&#39;</td><td>matches the string 'Banken' somewhere in a string field</td></tr>
     * <tr><td>&#39;~^.*[24680]{2}$&#39;</td><td>matches a string ending in at least 2 even digits</td></tr>
     * <tr><td>&#39;~^\d*$&#39;</td><td>matches an integer</td></tr>
     * <tr><td>&#39;~^[foo|BAR]$&#39;</td><td>matches 'foo' or 'BAR'</td></tr>
     * </table>
     * <br/>
     *
     * @return a query string.
     */
    String getQuery();
}
