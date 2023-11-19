package de.marketmaker.iview.mmgwt.mmweb.client;

/**
 * Author: umaurer
 * Created: 09.09.15
 */
public interface ViewFactory<V extends AbstractMainView> {
    V createView();
}
