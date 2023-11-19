package de.marketmaker.istar.merger.provider.history.eod;
/**
 * Provides classes for creation and retrieval end-of-day price history.
 * <p>
 *     EoD price history is divided in three types:
 *     <dl>
 *         <dt>Months</dt><dd>contains EoD price history for the past <tt>n</tt> months</dd>
 *         <dt>Patch</dt><dd>contains EoD prices that are corrected in the past, can be older than
 *         <tt>n</tt> months</dd>
 *         <dt>Rest</dt><dd>contains EoD prices from beginning till the recent <tt>n</tt> months</dd>
 *     </dl>
 * </p>
 * <p>
 *     EoD price history files are encoded in two kinds of schemas:
 *     <dl>
 *         <dt>Months,Rest</dt><dd>
 *             contains three logical parts(continuously) in order:
 *             <ol>
 *                 <li>price data(class <tt>EodPrices</tt>), organized in fields, more detail below</li>
 *                 <li>dictionary data, organized in quote and (offset, length)-pair into price data</li>
 *                 <li>index, organized in B* tree(level-one) for dictionary data</li>
 *             </ol>
 *             Price data for each quote are stored as following:
 *             <ul>
 *                 <li>field count: 1 byte. For each field the following:(class <tt>EodFields</tt>)</li>
 *                 <li>length of this field's price data and field id: 1 int (encoded as 24 bits for length
 *                 8 bits for field id). For each year of this field's data the following:(class
 *                 <tt>EodField</tt>)</li>
 *                 <li>year: 1 short. For this year's data the following:(class <tt>EodYear</tt>)</li>
 *                 <li>length of this year's data and its months: 1 int (encoded as 20 bits for length
 *                 12 bits for months, for each month contained in this year's data, one bit is set,
 *                 therefore 12 bits are needed). For each month the following:(class <tt>EodMonth</tt>)</li>
 *                 <li>days for this month: 1 int (for each day in this month where there is a price, one
 *                 bit is set in this integer)</li>
 *                 <li>BCD(class <tt>BCD</tt>) encoded prices. Each price is self-ending and can be
 *                 iterated using the count of set bits in the above integer (days)</li>
 *             </ul>
 *         Dictionary data are stored as following:
 *         <ul>
 *             <li>quote id: 1 long</li>
 *             <li>offset and length: 1 long (encoded as 40 bits as offset, 24 bits as length)</li>
 *         </ul>
 *         Index is stored as one level B* tree to accelerate searching. (class <tt>OneLevelBsTree</tt>)
 *         </dd>
 *         <dt>Patch</dt><dd>
 *             contains three logical parts(continuously) in order:
 *             <ol>
 *                 <li>price data(class <tt>EodPrices</tt>), organized in dates, more detail below</li>
 *                 <li>dictionary data, organized in quote and (offset, length)-pair into price data</li>
 *                 <li>index, organized in B* tree(level-one) for dictionary data</li>
 *             </ol>
 *             Price data for each quote are stored as following:
 *             <ul>
 *                 <li>date: 1 int (encoded as <tt>yyyyMMdd</tt>)</li>
 *                 <li>field count: 1 byte, for each field</li>
 *                 <li>field id: 1 byte and its price encoded in BCD</li>
 *             </ul>
 *             Other parts are stored the same as for "Months and Rest".
 *         </dd>
 *     </dl>
 * </p>
 * <p>
 *     According to this storage structure, operations like "merge sort" are applied during EoD price
 *     updates. Such operations are implemented along the hierarchy.
 * </p>
 * <p>
 *     Because of this structure, EoD prices for one quote from its IPO till today can be scattered
 *     across all three of kinds history units. Class <tt>EodPriceHistoryGatherer</tt> is provided
 *     to gather those prices for one specific quote.
 * </p>
 * <p>
 *     The Months history unit contains prices data for the recent <tt>n</tt> months. This parameter
 *     can be customized. As in case of such months' boundaries, the Months' and Patch's data will
 *     be merged onto Rest's data. Afterwards no patch data are in effect and all of Months' data
 *     are stored in Rest's data too.<br/>
 *     EoD price history provider is supposed to keep all Months' and Patch data in memory if those
 *     data are not too large to keep(class <tt>EodData</tt>).
 * </p>
 */