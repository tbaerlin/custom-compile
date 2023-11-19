/*
 * DmxmlContextFacade.java
 *
 * Created on 15.01.2015 09:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.Activatable;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * A thin wrapper around a DmxmlContext. Enables us to transparently use a DmxmlContext for only one
 * interested party but also for more than ony interested party. Its function is similar
 * to SnippetContextController. However, it relies plainly on AsyncCallback and not on Snippets.
 *
 * @author mdick
 */
public interface DmxmlContextFacade extends Activatable {
    DmxmlContext createOrGetContext(int numberOfBlocks);
    void reload();
    boolean subscribe(DmxmlContext reference, AsyncCallback<ResponseType> callback);
    boolean unsubscribe(DmxmlContext reference, AsyncCallback<ResponseType> callback);
}
