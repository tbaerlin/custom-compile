/*
 * AnalysisUtil.java
 *
 * Created on 22.10.2015 16:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.ArchiveData;
import de.marketmaker.iview.pmxml.LayoutDesc;

/**
 * @author mdick
 */
public final class AnalysisUtil {
    private AnalysisUtil() {
        //do not instantiate
    }

    public static String getLayoutName(ArchiveData archiveData) {
        try {
            return archiveData.getLayoutDesc().getLayout().getLayoutName();
        }
        catch(NullPointerException e) {
            return null;
        }
    }


    public static String getLayoutName(LayoutDesc layoutDesc) {
        try {
            return layoutDesc.getLayout().getLayoutName();
        }
        catch(NullPointerException e) {
            return null;
        }
    }

    public static String getLayoutGuid(ArchiveData result) {
        try {
            return result.getLayoutDesc().getLayout().getGuid();
        }
        catch (NullPointerException e) {
            return null;
        }
    }

    public static String getLayoutGuid(LayoutDesc layoutDesc) {
        try {
            return layoutDesc.getLayout().getGuid();
        }
        catch (NullPointerException e) {
            return null;
        }
    }
}
