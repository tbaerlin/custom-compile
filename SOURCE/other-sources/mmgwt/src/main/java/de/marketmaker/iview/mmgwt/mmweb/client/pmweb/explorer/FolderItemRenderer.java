package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer;

/**
 * Created on 23.04.13 14:16
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Renderer is not located in {@link de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers}
 * because of its needed reference on {@link FolderItem}, which is located in the pm-web module.
 */

public class FolderItemRenderer implements TableCellRenderer {
    private static HashSet<ShellMMType> VALID_TYPES = new HashSet<>(
            Arrays.asList(ShellMMType.ST_INHABER, ShellMMType.ST_PORTFOLIO, ShellMMType.ST_INTERESSENT)
    );

    protected final String contentClass;
    protected final String nullText;
    private FolderLinkListener listener;

    public FolderItemRenderer(String nullText) {
        this(nullText, null);
    }

    public FolderItemRenderer(String nullText, String contentClass) {
        this.nullText = nullText;
        this.contentClass = contentClass;
        this.listener = new FolderLinkListener();
    }

    public String getContentClass() {
        return this.contentClass;
    }

    public void render(Object data, StringBuffer sb, Context context) {
        final FolderItem item = getItem(data);
        if (item == null) {
            sb.append(this.nullText);
            return;
        }
        if (!valid(item)) {
            sb.append(item.getName());
            return;
        }
        final String linkContent = "<div " + // $NON-NLS$
                (this.contentClass != null
                        ? "class=" + this.contentClass + "-content" // $NON-NLS$
                        : "")
                + ">" + item.getName() + "</div>"; // $NON-NLS$
        context.appendLink(new LinkContext<FolderItem>(this.listener, item), linkContent, null, sb);
    }

    private boolean valid(FolderItem item) {
        return item != null && VALID_TYPES.contains(item.getType());
    }

    protected FolderItem getItem(final Object data) {
        return data != null ? (FolderItem) data : null;
    }

    public boolean isPushRenderer() {
        return false;
    }
}