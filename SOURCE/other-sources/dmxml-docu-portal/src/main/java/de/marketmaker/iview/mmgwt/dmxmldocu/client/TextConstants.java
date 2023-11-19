/*
 * TextConstants.java
 *
 * Created on 18.09.2012 16:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * @author Markus Dick
 */
public interface TextConstants extends Constants {

    TextConstants I = GWT.create(TextConstants.class);

    String companyLogo();

    String companyLabel();

    String contactLabel();

    String contactPerson();

    String contactDetails();

    String contactEmail();

    String companyPhone();

    String copyrightText();

    String contactEmailLabel();
}
