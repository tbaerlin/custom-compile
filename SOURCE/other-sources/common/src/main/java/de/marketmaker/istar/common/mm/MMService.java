/*
 * MMService.java
 *
 * Created on 17.03.2005 07:58:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import org.joda.time.YearMonthDay;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.pm5.mmservice")
public interface MMService {
    /**
     * Returns the handles for the given handle string which is supposed to
     * represent some item of the type defined by the keytype.
     * @param handleStr lookup key
     * @param keytype type of lookup key
     * @return array of handles that match the handleStr
     * @throws MMTalkException
     * @deprecated since calling this method returns instance specific handles, it cannot be used
     * by clients that do not know which instance answers a subsequest getMMTalkTable (e.g., because
     * MMServices are connected by JMS as competing consumers)
     */
    public int[] getHandles(String handleStr, MMKeyType keytype) throws MMTalkException;

    /** @deprecated use {@link #getMMTalkTable(MMTalkTableRequest)} */
    public Object[] getMMTalkTable(int[] handles, String contextHandle, String[] formulas,
            String preFormula)
            throws MMTalkException;

    /**
     * @deprecated use {@link #getMMTalkTable(MMTalkTableRequest)}
     */
    public Object[] getMMTalkTable(String[] keys, MMKeyType keytype, String contextHandle, String[] formulas,
            String preFormula)
            throws MMTalkException;

    /**
     * Evaluates the request that describes an MMTalk expression
     * @param request MMTalk container
     * @return A response with the following data in {@link MMServiceResponse#getData()}:<p>
     * If keys.length == 1, the result array contains as many elements as there are formulas
     * and each element represents the corresponding formula's result (it may be a simple type
     * or an array type). If keys.length = n (n &gt; 1), the result contains n * formulas.length
     * elements. The first n elements are the results of applying the first formula to the n
     * keys respectively, the second n elements are results of applying the second formula and so on.
     * For each key that cannot be identified, the corresponding evaluation results will be null.
     * 
     * @throws MMTalkException
     */
    public MMServiceResponse getMMTalkTable(MMTalkTableRequest request) throws MMTalkException;

    /** @deprecated use {@link #getTimeseries(MMTimeseriesRequest)} */
    public Object[] getTimeseries(String[] keys, MMKeyType keytype, String[] formulas,
            YearMonthDay from, YearMonthDay to) throws MMTalkException;

    /**
     * Evaluates the request that describes an MMTalk expression returning a set of timeserieses
     * @param request MMTalk container
     * @return A response with the following data in {@link MMServiceResponse#getData()}:<p>
     * The result array contains one Object array for each given key. Those arrays,
     * in turn, contain double arrays for each formula. Each double array represents a timeseries, and
     * the element at index 0 is the value on startDay, at index 1 the value on the day after startDay
     * and so on. For days on which the requested timeseries did not contain a value, the
     * corresponding value in the float array will be Float.NaN. For each key that cannot be identified,
     * the double arrays will be null.
     *
     * @throws MMTalkException
     */
    public MMServiceResponse getTimeseries(MMTimeseriesRequest request) throws MMTalkException;

}
