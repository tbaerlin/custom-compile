package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.Collection;
import java.util.List;

/**
 * Author: umaurer
 * Created: 23.01.14
 */
public interface HasChildren {
    void addChild(SpsWidget spsWidget);

    void addChildren(Collection<SpsWidget> children);

    int getChildCount();

    SpsWidget getChild(int index);

    List<SpsWidget> getChildren();

    String getBindPrefix();
}
