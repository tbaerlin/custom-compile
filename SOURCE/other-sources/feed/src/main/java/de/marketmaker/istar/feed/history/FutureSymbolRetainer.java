/*
 * FutureSymbolRetainer.java
 *
 * Created on 17.03.14 16:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author zzhao
 */
public class FutureSymbolRetainer implements SymbolRetainer<ByteString> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Set<ByteString> marketsWithFutures = new HashSet<>();

    public void setMarketsWithFutures(String path) throws IOException {
        List<String> lines = FileUtils.readLines(Paths.get(path).toFile(), "UTF-8");
        for (String line : lines) {
            if (StringUtils.isNotBlank(line)) {
                this.marketsWithFutures.add(new ByteString(line.trim()));
            }
        }

        this.logger.info("<setMarketsWithFutures> {}", this.marketsWithFutures);
    }

    @Override
    public boolean shouldRetain(ByteString symbol) {
        return shouldRetain(this.marketsWithFutures, symbol);
    }

    static boolean shouldRetain(Set<ByteString> marketsWithFutures, ByteString symbol) {
        ByteString market = getMarket(symbol);
        return marketsWithFutures.contains(market) && quasiFutureSymbol(symbol);

    }

    static boolean quasiFutureSymbol(ByteString symbol) {
        int idx = symbol.lastIndexOf('.') + 1;
        if (symbol.length() - idx != 3) {
            return false;
        }
        for (int i = idx; i < symbol.length(); i++) {
            if (!symbol.isDigit(i)) {
                return false;
            }
        }

        return true;
    }

    private static ByteString getMarket(ByteString symbol) {
        return symbol.substring(0, symbol.indexOf('.'));
    }
}
