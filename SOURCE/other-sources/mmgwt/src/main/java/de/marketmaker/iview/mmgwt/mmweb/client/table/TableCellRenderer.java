/*
 * TableCellRenderer.java
 *
 * Created on 20.03.2008 13:24:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TableCellRenderer {
    public interface Context {
        /**
         * To enable automagic link handling by a table context, a renderer has to call this method
         * to create a link is unique for the given context.
         * @param lc context that will be handed to {@link de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener}
         * @param content The content that is displayed within the link
         * @param tooltip The tooltip text that is displayed, when the mouse moves over the link.
         * @param sb The link is added to this StringBuffer.
         */
        void appendLink(LinkContext lc, String content, String tooltip, StringBuffer sb);
        /**
         * To enable automagic link handling by a table context, a renderer has to call this method
         * to create a link is unique for the given context.
         * @param token The token that is displayed as link destination
         * @param content The content that is displayed within the link
         * @param tooltip The tooltip text that is displayed, when the mouse moves over the link.
         * @param sb The link is added to this StringBuffer.
         */
        void appendLink(String token, String content, String tooltip, StringBuffer sb);

        int getPriceGeneration();

        void setStyle(String s);

        String getStyle();

        boolean isPush();
    }

    void render(Object data, StringBuffer sb, Context context);
    boolean isPushRenderer();
    String getContentClass();
}
