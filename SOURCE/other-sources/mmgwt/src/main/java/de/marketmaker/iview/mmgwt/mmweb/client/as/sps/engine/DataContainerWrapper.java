package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;

/**
 * Created on 21.03.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DataContainerWrapper<T extends DataContainerLeafNode> {
    private final DataContainerCompositeNode root;
    private final boolean declaration;

    public DataContainerWrapper(DataContainerCompositeNode root, boolean declaration) {
        this.root = root;
        this.declaration = declaration;
    }

    public DataContainerNode get(BindToken bindToken) {
        final DataContainerNode node = traverse(this.root, bindToken.getHead());
        if (node == null) {
            throw new IllegalStateException("could not find node for " + bindToken); // $NON-NLS$
        }
        return node;
    }

    private DataContainerNode traverse(DataContainerNode parent, BindKey bindKey) {
        if (bindKey == null) {
            return parent;
        }
        if (!(parent instanceof DataContainerCompositeNode)) {
            if (bindKey.getNext() != null) {
                return null;
            }
            return parent;
        }
        if (bindKey instanceof BindKeyIndexed) {
            if (!(parent instanceof DataContainerListNode)) {
                Firebug.warn("DataContainerWrapper.traverse - expected DataContainerListNode, but found " + parent.getClass().getSimpleName()+ "; bindKey=" + bindKey);
                return null;
            }
            final DataContainerListNode lnParent = (DataContainerListNode) parent;
            final int index = this.declaration ? 0 : ((BindKeyIndexed) bindKey).getIndex();
            return traverse(lnParent.getChildren().get(index), bindKey.getNext());
        }
        else if (bindKey instanceof BindKeyNamed) {
            if (!(parent instanceof DataContainerGroupNode)) {
                Firebug.warn("DataContainerWrapper.traverse - expected DataContainerGroupNode, but found " + parent.getClass().getSimpleName() + "; bindKey=" + bindKey);
                return null;
            }
            final DataContainerGroupNode gnParent = (DataContainerGroupNode) parent;
            final String name = ((BindKeyNamed) bindKey).getName();
            return traverse(getNamedChild(gnParent, name), bindKey.getNext());
        }
        else {
            throw new IllegalArgumentException("unhandled BindKey type: " + bindKey.getClass().getName()); // $NON-NLS$
        }
    }

    private DataContainerNode getNamedChild(DataContainerGroupNode parent, String name) {
        for (DataContainerNode child : parent.getChildren()) {
            if (name.equals(child.getNodeLevelName())) {
                return child;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public T getLeaf(BindToken bindToken) {
        final DataContainerNode node = get(bindToken);
        if (node instanceof DataContainerLeafNode) {
            return (T) node;
        }
        throw new IllegalArgumentException("Node (" + bindToken + ") is not a leaf!"); // $NON-NLS$
    }
}
