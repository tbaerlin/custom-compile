package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DragDropSupport;

/**
 * Author: umaurer
 * Created: 09.06.15
 */
public class SnippetTemplate extends Composite {
    private final FlowPanel panel = new FlowPanel();

    public SnippetTemplate(final SnippetClass clazz) {
        this.panel.setStyleName("mm-snippetTemplate");
        this.panel.add(new Label(clazz.getTitle()));
        initWidget(this.panel);

        DragDropSupport.makeDraggable(this, null, "snippet:new:" + clazz.getSnippetClassName(), clazz.getTitle()); // $NON-NLS$
    }
}
