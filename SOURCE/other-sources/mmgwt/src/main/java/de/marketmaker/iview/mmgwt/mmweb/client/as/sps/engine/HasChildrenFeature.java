package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

/**
 * Author: umaurer
 * Created: 29.01.14
 */
public interface HasChildrenFeature extends RequiresRelease {
    ChildrenFeature getChildrenFeature();
    boolean isFormContainer();
}