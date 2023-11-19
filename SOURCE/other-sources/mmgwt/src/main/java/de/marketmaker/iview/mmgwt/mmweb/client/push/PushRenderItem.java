/*
 * PushRenderItem.java
 *
 * Created on 26.02.2010 15:54:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.util.Arrays;
import java.util.HashSet;

import com.google.gwt.dom.client.Element;

import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Pushable;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author oflege
 */
public class PushRenderItem {
    private static class Context implements TableCellRenderer.Context {
        private String style;

        private final StringBuffer sb = new StringBuffer();

        public void appendLink(LinkContext lc, String content, String tooltip, StringBuffer sb) {
            throw new UnsupportedOperationException();
        }

        public void appendLink(String token, String content, String tooltip, StringBuffer sb) {
            throw new UnsupportedOperationException();
        }

        public int getPriceGeneration() {
            return PriceStore.INSTANCE.getPriceGeneration();
        }

        public void setStyle(String s) {
            this.style = s;
        }

        public String getStyle() {
            return style;
        }

        public boolean isPush() {
            return true;
        }

        void reset() {
            this.sb.setLength(0);
            this.style = null;
        }
    }

    private final static Context CTX = new Context();

    private final static HashSet<String> STYLES = new HashSet<String>(Arrays.asList(
            "n", "e", "p", // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
            "nup", "eup", "pup", // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
            "neq", "eeq", "peq", // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
            "ndn", "edn", "pdn")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

    private final Element element;

    private final Pushable data;

    private final TableCellRenderer renderer;

    private String style;

    public PushRenderItem(Element element, Pushable data, TableCellRenderer renderer) {
        this.element = element;
        this.data = data;
        this.renderer = renderer;
        for (String s : this.element.getClassName().split(" ")) { // $NON-NLS-0$
            if (STYLES.contains(s)) {
                this.style = s;
                break;
            }
        }
    }

    public Element getElement() {
        return element;
    }

    public Pushable getPushable() {
        return this.data;
    }

    public void render() {
        CTX.reset();
        this.renderer.render(this.data, CTX.sb, CTX);
        if (CTX.sb.length() > 0) {
            this.element.setInnerHTML(CTX.sb.toString());
            if (!StringUtil.equals(this.style, CTX.getStyle())) {
                if (this.style != null) {
                    this.element.removeClassName(this.style);
                }
                this.style = CTX.getStyle();
                if (this.style != null) {
                    this.element.addClassName(this.style);
                }
            }
        }
        else {
            clear();
        }
    }

    public boolean needsToBeCleared() {
        return this.style != null && this.style.length() > 1;
    }

    public void clear() {
        if (needsToBeCleared()) {
            this.element.removeClassName(this.style);
            if (this.style.length() > 2) {
                this.style = this.style.substring(0, 1);
                this.element.addClassName(this.style);
            }
            else {
                this.style = null;
            }
        }
    }
}
