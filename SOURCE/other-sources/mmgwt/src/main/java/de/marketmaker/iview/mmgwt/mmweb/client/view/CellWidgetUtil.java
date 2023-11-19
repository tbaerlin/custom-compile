package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import de.marketmaker.itools.gwtutil.client.widgets.tree.HasParent;
import de.marketmaker.itools.gwtutil.client.widgets.tree.SimpleCellTreeResources;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

/**
 * @author Ulrich Maurer
 *         Date: 04.03.13
 */
public class CellWidgetUtil {
    public static final SimpleCellTreeResources RESOURCES = new SimpleCellTreeResources(
            IconImage.getImageResource("celltree-closed-item"), // $NON-NLS$
            IconImage.getImageResource("celltree-open-item"), // $NON-NLS$
            IconImage.getImageResource("celltree-loading"), // $NON-NLS$
            IconImage.getImageResource("celltree-selected-background") // $NON-NLS$
    );

    public static TreeNode openTreePath(CellTree tree, HasParent item) {
        final HasParent parent = item.getParent();
        if (parent == null) {
            return tree.getRootTreeNode();
        }
        final TreeNode parentNode = openTreePath(tree, parent);
        for (int i = 0, childCount = parentNode.getChildCount(); i < childCount; i++) {
            if (parentNode.getChildValue(i) == item) {
                return parentNode.setChildOpen(i, true);
            }
        }
        throw new IllegalStateException("MenuTree.openTreePath(" + item.toString() + "): cannot find tree item"); // $NON-NLS$
    }

}
