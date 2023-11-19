/*
 * DependencyCommand.java
 *
 * Created on 08.10.2014 14:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency;

import com.google.gwt.user.client.Command;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasBindFeature;

/**
* @author mdick
*/
public interface DependencyCommand extends Command, HasBindFeature {
    void release();
}
