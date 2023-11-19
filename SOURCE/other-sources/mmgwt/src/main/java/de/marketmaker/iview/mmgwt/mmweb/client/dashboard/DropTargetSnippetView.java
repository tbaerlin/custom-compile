package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;

/**
 * Author: umaurer
 * Created: 15.06.15
 */
public class DropTargetSnippetView extends SnippetView<DropTargetSnippet> {
    protected DropTargetSnippetView(final DropTargetSnippet snippet) {
        super(snippet);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        final Widget containerWidget = this.container.asWidget();
        containerWidget.addStyleName("mm-snippet-dropTarget");
        this.container.setContentWidget(new HTML("&nbsp;")); // $NON-NLS$
    }
}
