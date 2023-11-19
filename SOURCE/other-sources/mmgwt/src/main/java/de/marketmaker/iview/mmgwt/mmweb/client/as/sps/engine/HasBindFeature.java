package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

/**
 * Author: umaurer
 * Created: 27.01.14
 */
public interface HasBindFeature {
    BindFeature getBindFeature();
    void onPropertyChange();
}
