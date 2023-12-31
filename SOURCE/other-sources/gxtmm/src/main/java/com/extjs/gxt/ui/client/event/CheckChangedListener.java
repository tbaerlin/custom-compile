/*
 * Ext GWT 2.2.4 - Ext for GWT
 * Copyright(c) 2007-2010, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package com.extjs.gxt.ui.client.event;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Check changed listener.
 */
public class CheckChangedListener<M extends ModelData> implements Listener<CheckChangedEvent<M>> {

  public void handleEvent(CheckChangedEvent<M> ce) {
    if (ce.getType() == Events.CheckChanged) {
      checkChanged(ce);
    }
  }

  /**
   * Fires when the check state has changed.
   * 
   * @param event the check changed event
   */
  public void checkChanged(CheckChangedEvent<M> event) {

  }

}
