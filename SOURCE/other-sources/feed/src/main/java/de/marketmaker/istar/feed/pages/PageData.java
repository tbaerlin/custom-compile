/*
 * PageData.java
 *
 * Created on 13.06.2005 11:36:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.pages;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.EntitlementsVwd;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PageData implements Serializable {
    static final long serialVersionUID = 261735698745215981L;

    private int id;

    private String text;

    private String textg;

    private String nextLink;

    private boolean dynamic;

    private long timestamp;

    private List<String> keys = null;

    private Set<String> selectors;

    public PageData(int id, String text, String textg, boolean dynamic, long timestamp, Set<String> selectors) {
        this.id = id;
        this.text = text;
        this.textg = textg;
        this.dynamic = dynamic;
        this.timestamp = timestamp;
        this.selectors = selectors;
    }

    public String toString() {
        final String s = getDefinedText();
        StringBuilder sb = new StringBuilder("PageData[").append(this.id)
                .append(this.textg != null ? ".G" : ".P")
                .append(", dynamic=").append(this.dynamic);
        String textStart = (s.length() <= 23) ? s : (s.substring(0, 20) + "...");
        sb.append(", '").append(textStart.replaceAll("[\\t\\r\\n]", "§")).append("'");
        if (this.keys != null) {
            sb.append(", keys=").append(this.keys);
        }
        if (this.selectors != null) {
            sb.append(", selectors=").append(this.selectors);
        }
        sb.append("]");
        return sb.toString();
    }

    private String getDefinedText() {
        return this.text != null ? this.text : this.textg;
    }

    public int getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public String getTextg() {
        return textg;
    }

    public void appendText(String s) {
        this.text = this.text.concat(s);
    }

    public boolean isDynamic() {
        return this.dynamic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getNextLink() {
        return nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public Set<String> getSelectors() {
        return this.selectors;
    }

    /**
     * Returns the vendorkeys used on this page
     * @return keys, empty collection if no keys are defined.
     */
    public synchronized List<String> getKeys() {
        if (this.keys == null) {
            this.keys = parseKeys();
        }
        return keys;
    }

    /**
     * Set the keys to be returned by {@link #getKeys()}. If this method has not been called before
     * with a non-null argument, the getKeys method will parse the page's text to determine the keys.
     */
    public synchronized void setKeys(List<String> keys) {
        this.keys = keys;
    }

    private List<String> parseKeys() {
        if (!this.isDynamic()) {
            return Collections.emptyList();
        }

        final Set<String> keys = new HashSet<>();

        int n = 0;
        // using indexOf is way faster then evaluating a regex pattern.
        final String s = getDefinedText();
        while ((n = s.indexOf("<OB O=D ", n)) != -1) {
            int from = s.indexOf('>', n + 8) + 1;
            int to = s.indexOf(' ', from);
            keys.add(s.substring(from, to));
            n = to;
        }

        if (keys.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(keys);
    }

    public void writeTo(ByteBuffer bb) {
        bb.putInt(this.id);
        bb.putLong(this.timestamp);
        bb.put((byte) (this.isDynamic() ? 1 : 0));
        writeSelectorsTo(bb);
        if (this.text != null) {
            bb.put((byte) 0);
            bb.put(ByteUtil.toBytes(this.text));
        }
        else {
            bb.put((byte) 1);
            bb.put(ByteUtil.toBytes(this.textg));
        }
    }

    private void writeSelectorsTo(ByteBuffer bb) {
        if (this.selectors == null) {
            bb.put((byte) 0);
        }
        else {
            bb.put((byte) this.selectors.size());
            for (String selector : selectors) {
                bb.putInt(EntitlementsVwd.toValue(selector));
            }
        }
    }

    public static PageData readFrom(ByteBuffer bb) {
        final int id = bb.getInt();
        final long timestamp = bb.getLong();
        final boolean dynamic = bb.get() != 0;
        final Set<String> selectors = readSelectors(bb);
        final boolean withText = (bb.get() == 0);
        final byte[] tmp = new byte[bb.remaining()];
        bb.get(tmp);
        if (withText) {
            return new PageData(id, ByteUtil.toString(tmp), null, dynamic, timestamp, selectors);
        }
        else {
            return new PageData(id, null, ByteUtil.toString(tmp), dynamic, timestamp, selectors);
        }
    }

    private static Set<String> readSelectors(ByteBuffer bb) {
        int numSelectors = bb.get();
        if (numSelectors == 0) {
            return null;
        }
        final Set<String> result = new HashSet<>();
        for (int i = 0; i < numSelectors; i++) {
            result.add(EntitlementsVwd.toNumericEntitlement(bb.getInt()));
        }
        return result;
    }
}
