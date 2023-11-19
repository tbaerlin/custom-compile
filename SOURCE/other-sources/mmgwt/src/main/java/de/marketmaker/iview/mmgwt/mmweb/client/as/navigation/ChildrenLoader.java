package de.marketmaker.iview.mmgwt.mmweb.client.as.navigation;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * User: umaurer
 * Date: 21.11.13
 * Time: 10:55
 */
public interface ChildrenLoader<I extends Item> {
    boolean hasMoreChildren(I item);
    void loadChildren(I item, AsyncCallback<List<? extends Item>> callback);
}
