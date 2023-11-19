/*
 * MerItem.java
 *
 * Created on 26.09.2008 08:40:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.mer;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.EntitlementQuote;

import java.io.*;

/**
 * @author Oliver Flege
* @author Thomas Kiesgen
*/
public class MerItem implements Serializable {
    protected static final long serialVersionUID = 1L;

    private final long qid;
    private transient final Quote quote;
    private final String country;
    private final String type;

    public MerItem(long qid, Quote quote, String country, String type) {
        this.qid = qid;
        this.quote = quote;
        this.country = country;
        this.type = type;
    }

    public long getQid() {
        return qid;
    }

    public Quote getQuote() {
        return quote;
    }

    public String getCountry() {
        return country;
    }

    public String getType() {
        return type;
    }

    public static void main(String[] args) throws Exception {
        final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("d:/temp/test.obj"));
        final MerItem item = new MerItem(1L, EntitlementQuote.create(null, null, "7C"), "a", "b");
        oos.writeObject(item);
        oos.close();

        final ObjectInputStream ois = new ObjectInputStream(new FileInputStream("d:/temp/test.obj"));
        final MerItem mi  = (MerItem) ois.readObject();
        ois.close();

        System.out.println(mi.getQid() + " " + mi.getQuote());
    }
}
