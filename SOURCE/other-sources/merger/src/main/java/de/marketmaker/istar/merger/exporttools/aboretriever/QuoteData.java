/*
 * SubscriptionQuote.java
 *
 * Created on 21.04.2010 16:53:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.exporttools.aboretriever;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class QuoteData {
    private final static Map<String, String> names = new HashMap<>();

    private final int iid;

    private final int qid;

    private final byte type;

    private final ByteString vwdsymbol;

    private final String name;

    private int[] entitlements;

    QuoteData(String line) {
        final String[] tokens = line.split("\t");
        this.iid = Integer.parseInt(tokens[0]);
        this.qid = Integer.parseInt(tokens[1]);
        this.vwdsymbol = "-".equals(tokens[2]) ? null : new ByteString(tokens[2]);
        this.type = Byte.parseByte(tokens[3]);

        String s = names.get(tokens[4]);
        if (s == null) {
            //noinspection RedundantStringConstructorCall
            s = new String(tokens[4]);
            names.put(s, s);
        }
        this.name = s;
    }

    public int getIid() {
        return iid;
    }

    public int getQid() {
        return qid;
    }

    public InstrumentTypeEnum getType() {
        return InstrumentTypeEnum.valueOf(type);
    }

    public String getVwdsymbol() {
        return vwdsymbol == null ? null : vwdsymbol.toString();
    }

    public String getName() {
        return name;
    }

    public int[] getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(int[] entitlements) {
        this.entitlements = entitlements;
    }

    static void resetNames() {
        System.out.println("#names: " + names.size());
        names.clear();
    }
}