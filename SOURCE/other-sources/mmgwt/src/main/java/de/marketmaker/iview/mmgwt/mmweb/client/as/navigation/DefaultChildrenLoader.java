package de.marketmaker.iview.mmgwt.mmweb.client.as.navigation;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * User: umaurer
 * Date: 21.11.13
 * Time: 11:34
 */
public class DefaultChildrenLoader<I extends Item> implements ChildrenLoader<I> {
    @Override
    public boolean hasMoreChildren(I item) {
        return false;
    }

    @Override
    public void loadChildren(I item, AsyncCallback<List<? extends Item>> callback) {
        // nothing to do
    }
}
