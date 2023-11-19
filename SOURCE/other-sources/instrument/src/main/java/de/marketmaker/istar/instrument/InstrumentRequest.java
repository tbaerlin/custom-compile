/*
 * InstrumentRequest.java
 *
 * Created on 22.12.2004 14:05:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.instrument.search.SearchRequest;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;

/**
 * A request for a list of specific instruments. The instruments can be requested using a number
 * of different ids or symbols.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 213123L;

    // anything that can be used to uniquely identify an instrument

    public enum KeyType {
        IID(IndexConstants.FIELDNAME_IID),
        QID(IndexConstants.FIELDNAME_QID),
        WKN(KeysystemEnum.WKN.name().toLowerCase()),
        ISIN(KeysystemEnum.ISIN.name().toLowerCase()),
        VALOR(KeysystemEnum.VALOR.name().toLowerCase()),
        VALORSYMBOL(KeysystemEnum.VALORSYMBOL.name().toLowerCase()),
        SEDOL(KeysystemEnum.SEDOL.name().toLowerCase()),
        CUSIP(KeysystemEnum.CUSIP.name().toLowerCase()),
        VWDCODE(KeysystemEnum.VWDCODE.name().toLowerCase()),
        VWDSYMBOL(KeysystemEnum.VWDSYMBOL.name().toLowerCase()),
        WM_TICKER(KeysystemEnum.WM_TICKER.name().toLowerCase()),
        EUREXTICKER(KeysystemEnum.EUREXTICKER.name().toLowerCase());

        private final String field;

        KeyType(String field) {
            this.field = field;
        }
    }

    public class Item implements Serializable {
        static final long serialVersionUID = 2L;

        private final String key;

        private final KeyType keyType;

        private Item(String key, KeyType keyType) {
            this.key = key;
            this.keyType = keyType;
        }

        public String getKey() {
            return key;
        }

        public KeyType getKeyType() {
            return keyType;
        }

        @Override
        public String toString() {
            return this.keyType + ":" + this.key;
        }

        SearchRequest toSearchRequest() {
            final SearchRequestStringBased sr
                    = new SearchRequestStringBased(InstrumentRequest.this.getClientInfo());
            final String s = this.key.trim().contains(" ") ? "(" + this.key + ")" : this.key;
            sr.setSearchExpression(this.keyType.field + ":" + s);
            sr.setCountInstrumentResults(true);
            sr.setMaxNumResults(1);
            sr.setUsePaging(true);
            sr.setPagingCount(1);
            return sr;
        }
    }

    private final List<Item> items = new ArrayList<>();

    public void addItem(String key, KeyType type) {
        this.items.add(new Item(key, type));
    }

    public void addItems(Collection<String> keys, KeyType type) {
        for (String key : keys) {
            addItem(key, type);
        }
    }

    public List<Item> getItems() {
        return this.items;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(this.items);
    }
}
