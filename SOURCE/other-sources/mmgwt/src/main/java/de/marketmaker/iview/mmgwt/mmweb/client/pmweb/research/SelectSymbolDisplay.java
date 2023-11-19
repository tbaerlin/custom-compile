/*
 * SelectSymbolDisplay.java
 *
 * Created on 18.07.2014 11:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.research;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisDisplay;
import de.marketmaker.iview.pmxml.ShellMMInfo;

/**
 * @author mdick
 */
public interface SelectSymbolDisplay<P extends SelectSymbolDisplay.Presenter> extends AnalysisDisplay<P> {
    void setTitle(String title);

    void show();
    void hide();

    interface Presenter extends AnalysisDisplay.Presenter {
        void onItemSelected(ShellMMInfo selectedItem);

        void onOkClicked();
        void onCancelClicked();
    }
}
