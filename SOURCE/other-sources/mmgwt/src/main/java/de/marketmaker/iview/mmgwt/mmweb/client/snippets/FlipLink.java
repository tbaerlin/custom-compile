/*
 * FlipLink.java
 *
 * Created on 29.02.12 11:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author oflege
 */
public class FlipLink extends Link implements LinkListener<Link> {
    private int row = -1;

    private int column = -1;

    private boolean visible = false;

    private final SnippetTableWidget tw;

    private final String flipId;

    public FlipLink(SnippetTableWidget tw, String flipId) {
        super(null, I18n.I.more(), null);
        this.tw = tw;
        this.flipId = flipId;
        withStyle("mm-snippetTable-expand"); // $NON-NLS-0$
    }

    public void setRow(int row) {
        setLocation(row, 1);
    }

    public void setLocation(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public LinkListener<Link> getListener() {
        return this;
    }

    public void onClick(LinkContext linkContext, Element e) {
        if (this.row >= 0) {
            this.visible = !this.visible;
            this.tw.setVisible(this.flipId, this.visible);
            this.tw.setLinkText(this.row, this.column, this.visible ? I18n.I.less() : I18n.I.more(), false);
        }
    }
}
