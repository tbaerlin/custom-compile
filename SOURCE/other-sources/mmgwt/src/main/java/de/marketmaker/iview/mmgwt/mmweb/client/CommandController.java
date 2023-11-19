/*
 * CommandController.java
 *
 * Created on 23.01.2012 13:51:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Michael Lösch
 */
public class CommandController extends AbstractPageController {

    private final Command command;

    public CommandController(ContentContainer container, Command command) {
        super(container);
        this.command = command;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.command.execute();
    }
}
