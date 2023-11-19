/*
 * GuiEventHandler.java
 *
 * Created on 28.03.12 16:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.event;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface GuiEventHandler {
    
    void blockSelected(String blockName);
    
    void sendRequestClicked();
    
    void logoutClicked();
    
}
