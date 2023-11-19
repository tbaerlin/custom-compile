/*
 * PriceRecordWithFactor.java
 *
 * Created on 07.05.2010 14:53:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.aop.support.AopUtils;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.Price;

/**
 * Allows to create PriceRecord objects that are based on a delegate PriceRecord
 * and multiply all prices and data derived from prices (e.g., changeNet) with a given factor.
 * E.g., multiplication with a currency cross rate allows to display a PriceRecord's values
 * in another currency.
 * @author oflege
 */
public class PriceRecordWithFactor implements InvocationHandler {
    private static final HashSet<String> METHODS_WITH_RESULTS_TO_BE_FACTORED
            = new HashSet<>(Arrays.asList("getSpreadNet", "getChangeNet", "getTurnoverDay",
            "getPreviousTurnoverDay", "getOfficialClose", "getMarketCapitalization"));

    private final PriceRecord delegate;

    private final BigDecimal factor;

    public static PriceRecord create(PriceRecord pr, BigDecimal factor) {
        if (pr == null || pr == NullPriceRecord.INSTANCE || pr == ZeroPriceRecord.INSTANCE) {
            return pr;
        }
        return (PriceRecord) Proxy.newProxyInstance(PriceRecord.class.getClassLoader(),
                new Class<?>[]{PriceRecord.class}, new PriceRecordWithFactor(pr, factor));
    }

    private PriceRecordWithFactor(PriceRecord delegate, BigDecimal factor) {
        this.delegate = delegate;
        this.factor = factor;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (AopUtils.isToStringMethod(method)) {
            return "[" + this.delegate.toString() + " * " + this.factor.toPlainString() + "]";
        }
        final Object o = method.invoke(this.delegate, args);
        if (o instanceof Price) {
            return PriceUtil.multiply((Price) o, this.factor, 2, RoundingMode.HALF_UP);
        }
        if (o != null && METHODS_WITH_RESULTS_TO_BE_FACTORED.contains(method.getName())) {
            return ((BigDecimal) o).multiply(this.factor).setScale(2, RoundingMode.HALF_UP);
        }
        return o;
    }

}
