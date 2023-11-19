/*
 * ItemWithSymbolDp2.java
 *
 * Created on 19.12.2004 17:07:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.KeysystemEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ItemWithSymbolsDp2 implements ItemWithSymbols, Serializable {
    static final long serialVersionUID = -10L;

    private long id;

    private final Map<KeysystemEnum, String> symbols = new HashMap<>();

    public ItemWithSymbolsDp2() {
        this(-1);
    }

    public ItemWithSymbolsDp2(long id) {
        this(id, Collections.emptyMap());
    }

    public ItemWithSymbolsDp2(long id, Map<KeysystemEnum, String> symbols) {
        this.id = id;
        symbols.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> this.symbols.put(entry.getKey(), entry.getValue()));
    }

    public Iterable<Map.Entry<KeysystemEnum, String>> getSymbols() {
        return getSymbolMap().entrySet();
    }

    /**
     * Use this method when a map of all symbols is need by a client; in contrast to
     * <tt>this.symbols</tt>, the returned map's iterator has a well defined order.
     * @return map with all symbols
     */
    private Map<KeysystemEnum, String> getSymbolMap() {
        if (this.symbols.isEmpty()) {
            return Collections.emptyMap();
        }
        return new EnumMap<>(this.symbols);
    }

    public String getSymbol(KeysystemEnum id) {
        return (this.symbols != null) ? this.symbols.get(id) : null;
    }

    void remove(KeysystemEnum id) {
        if (this.symbols != null) {
            this.symbols.remove(id);
        }
    }

    public String getSymbol(String id) {
        final KeysystemEnum kse = KeysystemEnum.valueOf(id);
        return (this.symbols != null) ? this.symbols.get(kse) : null;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setSymbol(KeysystemEnum anEnum, String symbol) {
        if (symbol != null) {
            this.symbols.put(anEnum, symbol);
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        final ItemWithSymbolsDp2 item = (ItemWithSymbolsDp2) o;

        return this.id == item.id && this.symbols.equals(item.symbols);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + symbols.hashCode();
        return result;
    }

    protected static boolean equalsById(ItemWithSymbols i1, ItemWithSymbols i2) {
        if (i1 != null) {
            return i2 != null && i1.getId() == i2.getId();
        }
        return i2 == null;
    }

    public String toString() {
        return getClass().getSimpleName() + "/" + this.id + ", symbols = " + getSymbolMap();
    }
}
