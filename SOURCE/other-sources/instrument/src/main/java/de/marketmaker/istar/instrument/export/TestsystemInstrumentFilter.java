/*
 * TestsystemInstrumentFilter.java
 *
 * Created on 22.07.15 13:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;

/**
 * If instruments/quotes are exported using vwhichsystem = 'pt', the same feed symbol may be
 * used for two quotes in two different instruments, once with negative iid/qid (from the
 * test system) and once with positive iid/qid (from the production system). The job of this
 * class is to act as a filter for those test instruments that contain quotes that use the
 * same feed symbol as any production quote.
 * <p>
 * Relies on the fact that instruments are processed with increasing iids, so that testsystem
 * instruments are read first. For each testsystem instrument, we map the feed symbol to the
 * respective iid. For each production instrument, we look for symbols in that map and, if
 * one is present, the mapped iid is marked as to be ignored.
 * </p>
 * @author oflege
 */
public class TestsystemInstrumentFilter implements InstrumentAdaptor, Predicate<Instrument> {

    private final Object2IntMap<String> testSymbols = new Object2IntOpenHashMap<>();

    private final IntSet iidsToIgnore = new IntOpenHashSet();

    private boolean disabled = false;

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public void adapt(InstrumentDp2 instrument) {
        if (this.disabled) {
            return;
        }
        if (instrument.getId() < 0) {
            addSymbols(instrument);
        }
        else {
            checkForDuplicateSymbol(instrument);
        }
    }

    @Override
    public boolean test(Instrument instrument) {
        return instrument.getId() < 0 && this.iidsToIgnore.contains((int) instrument.getId());
    }

    protected void checkForDuplicateSymbol(InstrumentDp2 instrument) {
        for (Quote quote : instrument.getQuotes()) {
            final String code = quote.getSymbolVwdcode();
            if (code != null) {
                final int i = this.testSymbols.removeInt(code);
                if (i != 0) {
                    this.iidsToIgnore.add(i);
                    return;
                }
            }
        }
    }

    protected void addSymbols(InstrumentDp2 instrument) {
        for (Quote quote : instrument.getQuotes()) {
            final String code = quote.getSymbolVwdcode();
            if (code != null) {
                this.testSymbols.put(code, (int) instrument.getId());
            }
        }
    }
}
