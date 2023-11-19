/*
 * VwdPageController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.FlipChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TopFlopSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OverviewSTKController extends DelegatingPageController {
    private static final String DEF = "ov_stk"; // $NON-NLS-0$

    // News-Topics: topic=[3J,3X,3G,3U,4D,4E,4F,4M,4U,4V,8W,19Q]

    private FlipChartSnippet fc1;

    private FlipChartSnippet fc2;

    private TopFlopSnippet tf;

    public OverviewSTKController(ContentContainer contentContainer) {
        super(contentContainer);
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), DEF);

        this.fc1 = (FlipChartSnippet) this.delegate.getSnippet("fc1"); // $NON-NLS-0$
        this.fc2 = (FlipChartSnippet) this.delegate.getSnippet("fc2"); // $NON-NLS-0$
        this.tf = (TopFlopSnippet) this.delegate.getSnippet("tf"); // $NON-NLS-0$

        if (this.fc1 != null) {
            handleViewChange(this.fc1, this.fc2);
            this.fc1.getMultiViewSupport().addValueChangeHandler(new ValueChangeHandler<Integer>() {
                public void onValueChange(ValueChangeEvent<Integer> e) {
                    handleViewChange(fc1, fc2);
                }
            });
        }
        if (this.fc2 != null) {
            this.fc2.getMultiViewSupport().addValueChangeHandler(new ValueChangeHandler<Integer>() {
                public void onValueChange(ValueChangeEvent<Integer> e) {
                    handleViewChange(fc2, fc1);
                }
            });
        }
    }

    private void handleViewChange(FlipChartSnippet fc, FlipChartSnippet other) {
        final String name = fc.getMultiViewSupport().getSelectedViewName();
        this.tf.setTitleSuffix(name);
        this.tf.setListid(fc.getSymbol(), fc.getMarketStrategy());
        if (other != null) {
            other.getMultiViewSupport().getViewSelectionModel().setUnselected(true);
        }
    }
}
