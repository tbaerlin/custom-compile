/*
* StructPieSnippetView.java
*
* Created on 18.07.2008 13:10:46
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;

/**
 * @author Michael Lösch
 */
public class StructPieSnippetView extends SnippetView<StructPieSnippet> {

    private Image image = new Image();

    public StructPieSnippetView(StructPieSnippet snippet) {
        super(snippet);
        this.image.setStyleName("mm-chart"); // $NON-NLS-0$
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(new SimplePanel(this.image));
    }

    public void update(String url) {
        reloadTitle();
        this.image.setUrl(ChartUrlFactory.getUrl(url));
        this.image.setVisible(true);
    }


}
