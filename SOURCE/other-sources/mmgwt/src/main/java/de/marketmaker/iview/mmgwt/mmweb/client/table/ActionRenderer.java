/*
 * ActionRenderer.java
 *
 * Created on 07.01.2009 15:49:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import com.google.gwt.dom.client.Element;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * Renders a number of actions in a table cell that can be applied to the data shown in a
 * particular row.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ActionRenderer<D> extends TableCellRendererAdapter implements
        LinkListener<ActionRenderer.ActionContext<D>> {

    /**
     * Simple helper class that combines an Action and some arbitrary data object.
     * @param <D>
     */
    public static class ActionContext<D> {
        private final Action action;
        private final D data;

        private ActionContext(D data, Action action) {
            this.data = data;
            this.action = action;
        }
    }

    /**
     * Delegate that knows which actions exist, which are applicable to certain objects, and
     * how to perform those actions when the user selects them.
     */
    private final ActionHandler<D> handler;

    public ActionRenderer(ActionHandler<D> handler) {
        this.handler = handler;
    }

    public void render(Object data, StringBuffer sb, Context context) {
        int num = 0;
        sb.append("<div class=\"mm-nobreak\">"); // $NON-NLS-0$
        @SuppressWarnings({"unchecked"})
        final D d = (D) data;
        for (Action a : this.handler.getActions()) {
            if (!this.handler.isActionApplicableTo(a, d)) {
                continue;
            }
            if (num++ > 0) {
                sb.append("&nbsp;"); // $NON-NLS$
            }
            if (a.getIcon() != null) {
                context.appendLink(new LinkContext<>(this, new ActionContext<>(d, a)), IconImage.getHtml(a.getIcon(), a.getTooltip()), a.getTooltip(), sb);
            }
            else if (a.getText() != null) {
                context.appendLink(new LinkContext<>(this, new ActionContext<>(d, a)), a.getText(), a.getTooltip(), sb);
            }
        }
        sb.append("</div>"); // $NON-NLS$
    }

    public void onClick(LinkContext<ActionContext<D>> linkContext, Element e) {
        final ActionContext<D> ac = linkContext.getData();
        this.handler.doAction(ac.action, ac.data);
    }
}
