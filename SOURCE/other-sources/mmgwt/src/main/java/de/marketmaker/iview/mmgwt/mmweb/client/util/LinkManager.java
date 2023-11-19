/*
 * LinkManager.java
 *
 * Created on 17.07.2008 13:25:05
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.Token;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil.appendAttribute;

/**
 * @author Ulrich Maurer
 */
public class LinkManager implements EventListener {
    public static final String LID_ATTR = "mm:lid"; // $NON-NLS-0$

    private List<LinkContext> linkContext = new ArrayList<>();

    /**
     * Handles a click
     * @param event click event
     * @return true iff event was handled, false otherwise
     */
    public boolean onClick(Event event) {
        if (this.linkContext.isEmpty()) {
            return false;
        }
        final Element target = getEventTargetWithLinkAttribute(event);
        if (target == null) {
            return false;
        }
        handleLinkClick(target, event);
        event.preventDefault();
        return true;
    }
    
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCLICK) {
            onClick(event);
        }
    }

    private String getLinkId(Element target) {
        // we have to check for "a" as IE6/7 throws an exception if getElementAttribute
        // is called on a table element!
        if ("a".equalsIgnoreCase(target.getTagName())) { // $NON-NLS-0$
            return target.getAttribute(LID_ATTR);
        }
        return null;
    }


    private Element getEventTargetWithLinkAttribute(Event event) {
        final Element currentTarget = DOM.eventGetCurrentTarget(event);
        Element target = DOM.eventGetTarget(event);
        while (!StringUtil.hasText(getLinkId(target))) {
            if (target == currentTarget) {
                return null;
            }
            target = DOM.getParent(target);
        }
        return target;
    }

    public String createLinkId(LinkContext linkContext) {
        final String lid = String.valueOf(this.linkContext.size());
        this.linkContext.add(linkContext);
        return lid;
    }

    public void appendLink(LinkContext lc, String content, String tooltip, StringBuffer sb) {
        final String lid = createLinkId(lc);
        sb.append("<a"); // $NON-NLS-0$
        appendAttribute(sb, LID_ATTR, lid);
        appendAttribute(sb, "href", "#" + (lc.getToken() != null ? lc.getToken() : "")); // $NON-NLS$
        //noinspection StringEquality
        if (tooltip != content && tooltip != null) {
            appendAttribute(sb, Tooltip.ATT_QTIP, tooltip);
            appendAttribute(sb, Tooltip.ATT_COMPLETION, Tooltip.ATT_QTIP);
            appendAttribute(sb, Tooltip.ATT_STYLE, "mm-linkHover"); // $NON-NLS$
        }
        final String style;
        if (lc.getData() instanceof Link) {
            style = ((Link) lc.getData()).getStyle();
        }
        else {
            style = null;
        }
        if (style != null) {
            appendAttribute(sb, "class", style); // $NON-NLS-0$
        }
        sb.append(">").append(content).append("</a>"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void appendLink(String token, String content, String tooltip, StringBuffer sb) {
        final String lid = createLinkId(LinkContext.historyTokenLink(new Token(token)));
        sb.append("<a"); // $NON-NLS-0$
        appendAttribute(sb, "class", "gwt-Hyperlink"); // $NON-NLS$
        appendAttribute(sb, LID_ATTR, lid);
        appendAttribute(sb, "href", "#" + token); // $NON-NLS$
        //noinspection StringEquality
        if (tooltip != content && tooltip != null) {
            appendAttribute(sb, Tooltip.ATT_QTIP, tooltip);
            appendAttribute(sb, Tooltip.ATT_COMPLETION, Tooltip.ATT_QTIP);
            appendAttribute(sb, Tooltip.ATT_STYLE, "mm-linkHover"); // $NON-NLS$
        }
        sb.append(">").append(content).append("</a>"); // $NON-NLS$
    }

    public EventListener getEventListener() {
        if (this.linkContext.isEmpty()) {
            return null;
        }
        return this;
    }

    private void handleLinkClick(Element e, Event event) {
        final String lid = getLinkId(e);
        final int n = Integer.parseInt(lid);
        assert n < this.linkContext.size();
        this.linkContext.get(n).onClick(e, event);
    }

    public void clear() {
        this.linkContext.clear();
    }
}
