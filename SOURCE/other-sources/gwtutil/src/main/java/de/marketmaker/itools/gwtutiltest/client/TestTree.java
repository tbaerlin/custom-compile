package de.marketmaker.itools.gwtutiltest.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;
import de.marketmaker.itools.gwtutil.client.widgets.tree.SimpleCellTreeResources;

/**
 * @author Ulrich Maurer
 *         Date: 27.02.13
 */
public class TestTree implements IsWidget {
    private final Widget tree;

    public TestTree(boolean withResources) {
        this.tree = createTree(withResources);
    }

    @Override
    public Widget asWidget() {
        return this.tree;
    }

    class TreeNode {
        private Object value;
        private List<TreeNode> listChildren = null;

        public TreeNode(Object value) {
            this.value = value;
        }

        private List<TreeNode> getListChildren() {
            if (this.listChildren == null) {
                this.listChildren = new ArrayList<TreeNode>();
            }
            return this.listChildren;
        }

        public void add(TreeNode child) {
            getListChildren().add(child);
        }

        public List<TreeNode> getChildren() {
            return this.listChildren;
        }

        public TreeNode withChildren(TreeNode... children) {
            Collections.addAll(getListChildren(), children);
            return this;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    class MyCell extends AbstractCell<TreeNode> {
        @Override
        public void render(Context context, TreeNode treeNode, SafeHtmlBuilder sb) {
            sb.appendEscaped(treeNode.toString());
        }
    }

    class MyTreeViewModel implements TreeViewModel {
        @Override
        public <T> NodeInfo<?> getNodeInfo(T value) {
            return new DefaultNodeInfo(new ListDataProvider(((TreeNode) value).getChildren()), new MyCell());

        }

        @Override
        public boolean isLeaf(Object value) {
            return ((TreeNode) value).getChildren() == null;
        }
    }

    private Widget createTree(boolean withResources) {

        final TreeNode rootNode = new TreeNode("root").withChildren(
                new TreeNode("child1").withChildren(
                        new TreeNode("child1-1").withChildren(
                                new TreeNode("child1-1-1"),
                                new TreeNode("child1-1-2"),
                                new TreeNode("child1-1-3")
                        ),
                        new TreeNode("child1-2"),
                        new TreeNode("child1-3")
                ),
                new TreeNode("child2").withChildren(
                        new TreeNode("child2-1"),
                        new TreeNode("child2-2"),
                        new TreeNode("child2-3")
                ),
                new TreeNode("child3").withChildren(
                        new TreeNode("child3-1"),
                        new TreeNode("child3-2"),
                        new TreeNode("child3-3")
                )
        );

        final CellTree cellTree = withResources
                ? new CellTree(new MyTreeViewModel(), rootNode, new SimpleCellTreeResources())
                : new CellTree(new MyTreeViewModel(), rootNode);
        return cellTree;
    }
}
