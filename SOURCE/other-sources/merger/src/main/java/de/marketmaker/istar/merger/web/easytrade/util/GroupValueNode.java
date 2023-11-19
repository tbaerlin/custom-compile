package de.marketmaker.istar.merger.web.easytrade.util;

import de.marketmaker.istar.merger.util.NaturalOrderComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * a simple node class that can hold child nodes or count values (in case the node is a leaf node)
 */
public class GroupValueNode {

    // a comparator that can sort string encoded numbers the human way
    private static final NaturalOrderComparator COMPARATOR = new NaturalOrderComparator();

    private Map<GroupValueKey, GroupValueNode> children;

    private TreeMap<String, Integer> counts;

    public GroupValueNode() {}

    /**
     * this method creates a child map
     * @return
     */
    public Map<GroupValueKey, GroupValueNode> getChildren() {
        if (children == null) {
            children = new HashMap<>();
        }
        return children;
    }

    /**
     * check with isLeaf() before calling this method,
     * this methods creates a count map
     * @return
     */
    public Map<String, Integer> getCounts() {
        if (counts == null) {
            counts = new TreeMap<>(COMPARATOR);
        }
        return counts;
    }

    public void addCount(String value) {
        assert this.children == null;
        if (this.counts == null) {
            this.counts = new TreeMap<>(COMPARATOR);
        }
        this.counts.put(value, getCount(value) + 1);
    }

    int getCount(String value) {
        assert this.counts != null;
        final Integer count = this.counts.get(value);
        return (count != null) ? count : 0;
    }

    // used in the string template
    public int getChildCount() {
        return isLeaf() ? counts.size() : (children != null ? children.size() : 0);
    }

    public boolean isLeaf() {
        return this.counts != null;
    }

}
