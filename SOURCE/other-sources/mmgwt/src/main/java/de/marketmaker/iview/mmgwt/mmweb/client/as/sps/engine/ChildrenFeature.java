package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: umaurer
 * Created: 23.01.14
 */
public class ChildrenFeature implements HasChildren {
    private String bindPrefix = "";
    private final List<SpsWidget> children = new ArrayList<>();

    @Override
    public void addChild(SpsWidget spsWidget) {
        this.children.add(spsWidget);
    }

    @Override
    public void addChildren(Collection<SpsWidget> children) {
        this.children.addAll(children);
    }

    @Override
    public int getChildCount() {
        return this.children.size();
    }

    @Override
    public SpsWidget getChild(int index) {
        return this.children.get(index);
    }

    @Override
    public List<SpsWidget> getChildren() {
        return this.children;
    }

    public void setBindPrefix(String bindPrefix) {
        this.bindPrefix = bindPrefix;
    }

    @Override
    public String getBindPrefix() {
        return this.bindPrefix;
    }

    public void releaseChildren() {
        for (SpsWidget spsWidget : this.children) {
            if (spsWidget instanceof RequiresRelease) {
                ((RequiresRelease) spsWidget).release();
            }
        }
    }
}
