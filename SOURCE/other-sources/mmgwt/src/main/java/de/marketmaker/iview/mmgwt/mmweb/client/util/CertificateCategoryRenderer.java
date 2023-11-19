/*
 * CertificateCategoryRenderer.java
 *
 * Created on 20.08.2008 17:08:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CertificateCategoryRenderer implements Renderer<String> {

    private final String prefix = "CERT_"; // $NON-NLS-0$

    public String render(String category) {
        if (category == null) {
            return null;
        }
        try {
            return CertificateTypeEnum.valueOf(category).getDescription();
        } catch (Exception e) {
            DebugUtil.logToServer(getClass().getSimpleName() + " <render> no valid enum value: " + category);
        }
        if (category.startsWith(prefix)) {
            return category.substring(prefix.length());
        }
        return category;
    }
}
