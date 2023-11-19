/*
 * RmiServiceDescriptorEditor.java
 *
 * Created on 10.03.2005 11:59:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.rmi;

import java.util.Arrays;

import java.beans.PropertyEditorSupport;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id$
 */
public class RmiServiceDescriptorEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        final String[] configs = text.split(";");

        final RmiServiceDescriptor[] result = new RmiServiceDescriptor[configs.length];

        int count=0;
        for (String singleConfig : configs) {
            final String[] tokens = singleConfig.split(",");

            final int prio = Integer.parseInt(tokens[0]);
            final String url = tokens[1];

            result[count] = new RmiServiceDescriptor();
            result[count].setPriority(prio);
            result[count].setServiceUrl(url);
            
            count++;
        }
        Arrays.sort(result);

        setValue(result);
    }
}
