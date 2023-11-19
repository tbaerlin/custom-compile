/*
 * ProviderMethod.java
 *
 * Created on 23.10.13 11:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;

/**
 * @author zzhao
 */
public interface ProviderMethod<T> {
    T invoke() throws IOException;
}
