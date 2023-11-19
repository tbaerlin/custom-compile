/*
 * BlockDocumentation.java
 *
 * Created on 20.03.2012 14:20:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import de.marketmaker.iview.dmxmldocu.DmxmlBlockDocumentation;

/**
 * Simple wrapper around the sorted list of block names and the detailed docu accessible by name
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class BlocksDocumentation implements Serializable {

    private ArrayList<String> blockNames;

    private HashMap<String, DmxmlBlockDocumentation> blockDocu;
    
    private String defaultAuthentication;
    private String defaultAuthenticationType;


    public ArrayList<String> getBlockNames() {
        return blockNames;
    }

    public void setBlockNames(ArrayList<String> blockNames) {
        this.blockNames = blockNames;
    }

    public HashMap<String, DmxmlBlockDocumentation> getBlockDocu() {
        return blockDocu;
    }

    public void setBlockDocu(HashMap<String, DmxmlBlockDocumentation> blockDocu) {
        this.blockDocu = blockDocu;
    }
    
    public DmxmlBlockDocumentation getDocuFor(String blockName) {
        return this.blockDocu.get(blockName);
    }

    public String getDefaultAuthentication() {
        return defaultAuthentication;
    }

    public void setDefaultAuthentication(String defaultAuthentication) {
        this.defaultAuthentication = defaultAuthentication;
    }

    public String getDefaultAuthenticationType() {
        return defaultAuthenticationType;
    }

    public void setDefaultAuthenticationType(String defaultAuthenticationType) {
        this.defaultAuthenticationType = defaultAuthenticationType;
    }
}
