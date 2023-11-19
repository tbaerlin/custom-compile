package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.widget.ContentPanel;

/**
 * Created on 04.08.11 11:23
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class LiveFinderCategory extends ContentPanel {

    public LiveFinderCategory(String heading) {
        setHeading(heading);
        setAnimCollapse(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        addStyleName("mm-workspace-item"); // $NON-NLS$

//        addLeaf(new LiveFinderCategoryItem("CatItem1")); // $NON-NLS$
//        addLeaf(new LiveFinderCategoryItem("CatItem2")); // $NON-NLS$



    }

}
