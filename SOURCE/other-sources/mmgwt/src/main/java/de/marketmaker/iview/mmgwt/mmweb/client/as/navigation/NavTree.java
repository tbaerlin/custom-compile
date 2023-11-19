package de.marketmaker.iview.mmgwt.mmweb.client.as.navigation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.event.EventUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSpec;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.dom.client.Style.Overflow.HIDDEN;
import static com.google.gwt.dom.client.Style.TableLayout.FIXED;
import static com.google.gwt.dom.client.Style.TextOverflow.ELLIPSIS;
import static com.google.gwt.dom.client.Style.Unit.PCT;
import static com.google.gwt.dom.client.Style.WhiteSpace.NOWRAP;

/**
 * Created with IntelliJ IDEA.
 * User: umaurer
 * Date: 29.04.13
 * Time: 09:12
 */
public class NavTree<I extends Item> extends Composite implements HasSelectionHandlers<I>, HasResizeHandlers {
    private final FlowPanel panel;
    private final FlowPanel panelLabel;
    private final Widget itemWidget;
    private final int level;
    private FlowPanel panelChildren;
    private final Image expandIcon;

    private final NavTree<I> root;
    private final NavTree<I> parent;
    private List<NavTree<I>> children;
    private final ChildrenLoader<I> childrenLoader;
    private final I item;
    private boolean expanded = true;
    private NavTree<I> selected = null;

    private long lastSelectionMillis = -1;

    public NavTree(I root) {
        this(root, 0, new DefaultChildrenLoader<>());
    }

    public NavTree(I root, int level) {
        this(root, level, new DefaultChildrenLoader<>());
    }

    public NavTree(I root, int level, ChildrenLoader<I> childrenLoader) {
        this.panel = null;
        this.panelLabel = null;
        this.itemWidget = null;
        this.expandIcon = null;
        this.root = this;
        this.parent = null;
        this.level = level;

        this.item = root;
        this.panelChildren = new FlowPanel();
        this.panelChildren.setStyleName("as-navTree");
        this.panelChildren.addStyleName("as-navTree-level-" + level);
        if (!hasTopHandle(root)) {
            this.panelChildren.addStyleName("as-navTree-no-topHandle");
        }

        this.childrenLoader = childrenLoader;

        if (root.isLeaf() && !childrenLoader.hasMoreChildren(root)) {
            this.children = null;
        }
        else {
            final List<? extends Item> children = root.getChildren();
            this.children = new ArrayList<>(children.size());
            for (Item child : children) {
                @SuppressWarnings("unchecked") final NavTree<I> childView = new NavTree(this, child, level);
                this.children.add(childView);
                this.panelChildren.add(childView);
            }
            setExpanded(true, false, true, true);
        }
        initWidget(this.panelChildren);
        setVisible(this.item.isVisible());
    }

    private boolean hasTopHandle(I root) {
        if (root.getChildren() == null) {
            return false;
        }
        for (Item child : root.getChildren()) {
            if (child.isLeaf()) {
                continue;
            }
            if (child.isAlwaysOpen()) {
                continue;
            }
            return true;
        }
        return false;
    }

    private NavTree(NavTree<I> parent, final I item, int level) {
        this.root = parent.root;
        this.childrenLoader = parent.childrenLoader;
        this.level = level;
        this.parent = parent;
        this.item = item;
        this.panel = new FlowPanel();
        this.panel.setStyleName("item");
        this.panel.addStyleName("level-" + level);
        this.panelLabel = new FlowPanel();
        this.panelLabel.setStyleName("labelPanel");
        this.itemWidget = createItemWidget(item, item.hasSelectionHandler());
        this.itemWidget.setStyleName("label");
        if (item.hasSelectionHandler()) {
            this.itemWidget.addStyleName("action");
            final ClickHandler clickHandler = event -> fireSelectionEvent(item);
            EventUtil.addClickHandler(this.itemWidget, clickHandler);
        }
        this.panelLabel.add(this.itemWidget);
        this.panel.add(this.panelLabel);
        if (item.isLeaf() && !this.childrenLoader.hasMoreChildren(item)) {
            if (!item.hasSelectionHandler()) {
                this.itemWidget.addStyleName("no-action");
            }
            this.children = null;
            this.panelChildren = null;
            this.expandIcon = null;
        }
        else {
            this.panelChildren = new FlowPanel();
            this.panelChildren.setStyleName("children");
            this.panel.add(this.panelChildren);
            if (item.isLeaf()) {
                this.children = null;
            }
            else {
                onChildrenAvailable(item.getChildren());
            }
            if (item.isAlwaysOpen()) {
                this.expandIcon = null;
            }
            else {
                this.expandIcon = new Image("clear.cache.gif"); // $NON-NLS$
                this.expandIcon.setStyleName("expandIcon");
                if (item.isLeaf()) {
                    this.expandIcon.addStyleName("async");
                }
                this.expandIcon.addClickHandler(event -> setExpanded(!expanded));
                if (!item.hasSelectionHandler() || item.isOpenWithSelection()) {
                    EventUtil.addClickHandler(this.itemWidget, event -> {
                        final boolean expand = !NavTree.this.expanded || item.isOpenWithSelection();
                        if (expand) {
                            selectOrSubSelect(item);
                        }
                        setExpanded(expand);
                    });
                }
                this.panelLabel.add(this.expandIcon);
            }
            final boolean expanded = item.isAlwaysOpen() || item.isOpenByDefault();
            setExpanded(expanded, false, true, true);
            changeExpandStyle(null, expanded);
        }
        if (item.getLeftIcon() != null) {
            final SimplePanel sp = new SimplePanel(item.getLeftIcon().asPrototype().createImage());
            sp.setStyleName("leftIconPanel");
            if (item.getLeftIconTooltip() != null) {
                Tooltip.addQtip(sp, item.getLeftIconTooltip());
            }
            this.panel.add(sp);
        }
        initWidget(this.panel);
        setVisible(item.isVisible());
    }

    @SuppressWarnings("unchecked")
    private void selectOrSubSelect(I item) {
        if (item == null || !item.isSelectFirstChildOnOpen()) {
            return;
        }
        final boolean goDeeper = item.isHasDelegate() && item.getChildren() != null && !item.getChildren().isEmpty();
        if (goDeeper) {
            final I firstChild = (I) item.getChildren().get(0);
            selectOrSubSelect(firstChild);
        }
        else if (!anyChildSelected(item)) {
            select(NavTree.this, item);
            fireSelectionEvent(item);
        }
    }

    private boolean anyChildSelected(I item) {
        if (!(item instanceof NavItemSpec)) {
            return false;
        }
        final NavItemSpec parent = ((NavItemSpec) item).getParent();
        NavTree<I> parentsTree = getNavTreeOfParent(parent);
        if (parentsTree == null) {
            return false;
        }
        final List<NavTree<I>> children = parentsTree.getChildren();
        for (NavTree<I> child : children) {
            if (child == root.selected) {
                return true;
            }
        }
        return false;
    }

    private NavTree<I> getNavTreeOfParent(NavItemSpec parent) {
        return findNavTreeById(this, parent.getId(), (i, id) -> {
            NavItemSpec nis = (NavItemSpec) i;
            return nis.getId().equals(id);
        });
    }

    public void onChildrenAvailable(List<? extends Item> children) {
        if (this.children == null) {
            this.children = new ArrayList<>(children.size());
        }
        else {
            this.children.clear();
        }
        this.panelChildren.clear();
        int childLevel = this.level + 1;
        for (Item child : children) {
            //noinspection unchecked
            final NavTree<I> childView = new NavTree(this, child, childLevel);
            this.children.add(childView);
            this.panelChildren.add(childView);
        }
    }

    private Widget createItemWidget(I item, boolean active) {
        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        final Style tableStyle = table.getElement().getStyle();
        tableStyle.setTableLayout(FIXED);
        tableStyle.setWidth(100, PCT);

        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();

        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        final ImageSpec icon = item.getIcon();
        if (icon != null) {
            sb.appendHtmlConstant("<span");
            appendQtip(sb, item.getIconTooltip());
            sb.appendHtmlConstant(">").append(icon.asPrototype().getSafeHtml()).appendHtmlConstant("</span>");
        }
        sb.appendHtmlConstant("&nbsp;").appendEscaped(item.getName());
        table.setHTML(0, 0, sb.toSafeHtml());
        final Element cellElement = formatter.getElement(0, 0);
        cellElement.setAttribute(Tooltip.ATT_COMPLETION, "auto"); // $NON-NLS$
        cellElement.setAttribute(Tooltip.ATT_STYLE, "as-navTree-qtip" + (active ? " active" : "")); // $NON-NLS$
        final Style cellStyle = cellElement.getStyle();
        cellStyle.setWhiteSpace(NOWRAP);
        cellStyle.setOverflow(HIDDEN);
        cellStyle.setTextOverflow(ELLIPSIS);

        if (item.getEndIcon() != null) {
            table.setWidget(0, 1, item.getEndIcon());
            formatter.setWidth(0, 1, getWidth(item.getEndIcon()) + "px"); // $NON-NLS$
            if (item.getEndIconCellClass() != null) {
                formatter.setStyleName(0, 1, item.getEndIconCellClass());
            }
        }
        return table;
    }

    private int getWidth(Widget widget) {
        if (widget instanceof Image) {
            return ((Image) widget).getWidth();
        }
        if (widget instanceof IconImageIcon) {
            return ((IconImageIcon) widget).getWidth();
        }
        throw new IllegalArgumentException("NavTree.getWidth() unsupported widget type: " + widget.getClass().getSimpleName()); // $NON-NLS$
    }

    private void appendQtip(SafeHtmlBuilder sb, SafeHtml qtip) {
        if (qtip != null) {
            sb.appendHtmlConstant(" qtip=\"").appendEscaped(qtip.asString()).appendHtmlConstant("\"");
        }
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<I> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    private void fireSelectionEvent(I item) {
        final long time = System.currentTimeMillis();
        if (time - this.lastSelectionMillis < 600) {
//            Notifications.add(I18n.I.hint(), I18n.I.secondClickIgnored()).requestStateDelayed(NotificationMessage.State.DELETED, 5);
            Firebug.warn("NavTree.fireSelectionEvent(" + item.getName() + "): doubleclick within 600 millis");
            return;
        }
        this.lastSelectionMillis = time;
        Firebug.debug("NavTree.fireSelectionEvent(" + item.getName() + ")");
        SelectionEvent.fire(this.root, item);
    }

    public void fireSelectionEvent() {
        fireSelectionEvent(this.item);
    }

    public NavTree<I> getParentNavTree() {
        return parent;
    }

    public void setExpanded(boolean expanded) {
        setExpanded(expanded, true, true, true);
    }

    private void setExpanded(final boolean expanded, final boolean closeSiblings, final boolean fireResize, final boolean allowLoadChildren) {
        final boolean moreChildren = this.childrenLoader.hasMoreChildren(getItem());
        if (this.expanded == expanded) {
            return;
        }
        if (expanded && allowLoadChildren && moreChildren) {
            if (this.expandIcon != null) {
                this.expandIcon.addStyleName("loading");
            }
            this.childrenLoader.loadChildren(getItem(), new AsyncCallback<List<? extends Item>>() {
                @Override
                public void onFailure(Throwable caught) {
                    // nothing to do
                }

                @Override
                public void onSuccess(List<? extends Item> children) {
                    NavTree.this.onChildrenAvailable(children);
                    if (expandIcon != null) {
                        expandIcon.removeStyleName("loading");
                    }
                    setExpanded(true, closeSiblings, fireResize, false);
                }
            });
            return;
        }

        if (this.children == null && !moreChildren) {
            return;
        }

        if (expanded) {
            if (this.item.isClosingSiblings() && closeSiblings) {
                this.parent.collapseAllChildren();
            }
            if (this.children != null) {
                for (NavTree<I> child : this.children) {
                    if (child.getItem().isOpenWithParent()) {
                        child.setExpanded(true, false, false, true);
                    }
                }
            }
        }
        else {
            if (this.item.isAlwaysOpen()) {
                throw new IllegalStateException("cannot close always open NavTree"); // $NON-NLS$
            }
        }
        this.panelChildren.setVisible(expanded);
        changeExpandStyle(this.expanded, expanded);
        this.expanded = expanded;
        if (fireResize) {
            fireResizeEvent();
        }
    }

    private void changeExpandStyle(Boolean removedState, boolean addedState) {
        if (this.expandIcon == null) {
            return;
        }
        if (removedState != null) {
            this.expandIcon.removeStyleName(removedState ? "expanded" : "collapsed");
        }
        this.expandIcon.addStyleName(addedState ? "expanded" : "collapsed");
    }

    private void collapseAllChildren() {
        for (NavTree childView : this.children) {
            childView.setExpanded(false);
        }
    }

    public List<NavTree<I>> getChildren() {
        return children;
    }

    @SuppressWarnings("unchecked")
    public NavTree<I> getChildById(String id, NavTreeItemIdentifier<I> identifier) {
        if (this.children == null) {
            return null;
        }
        for (NavTree<I> child : this.children) {
            if (identifier.hasId(child.getItem(), id)) {
                return child;
            }
            final NavTree found = child.getChildById(id, identifier);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public I getItem() {
        return item;
    }

    public void setSelected(boolean selected, boolean fireEvent) {
        if (this.panelLabel == null) {
            return;
        }
        if (selected) {
            unselect();
            this.root.selected = this;
            this.panelLabel.addStyleName("selected");
            NavTree navTree = this.item.isOpenWithSelection() ? this : getParentNavTree();
            while (navTree != null) {
                navTree.setExpanded(true);
                navTree = navTree.getParentNavTree();
            }
            if (fireEvent) {
                fireSelectionEvent();
            }
        }
        else {
            if (this.root.selected == this) {
                this.root.selected = null;
            }
            this.panelLabel.removeStyleName("selected");
        }
    }

    public void unselect() {
        if (this.root.selected != null) {
            this.root.selected.setSelected(false, false);
        }
    }

    public void setSelectedByIds(String[] ids, NavTreeItemIdentifier<I> identifier, boolean fireEvent) {
        unselect();
        setSelectedByIds(ids, 0, identifier, fireEvent);
    }

    private boolean setSelectedByIds(String[] ids, int index, NavTreeItemIdentifier<I> identifier, boolean fireEvent) {
        if (index >= ids.length) {
            return true;
        }
        final NavTree<I> child = getChildById(ids[index], identifier);
        if (child == null) {
            return false;
        }
        child.setExpanded(true, true, true, true);
        if (child.setSelectedByIds(ids, index + 1, identifier, fireEvent)) {
            child.setSelected(true, fireEvent);
        }
        return false;
    }

    @Override
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {
        return addHandler(handler, ResizeEvent.getType());
    }

    private void fireResizeEvent() {
        ResizeEvent.fire(this.root, this.root.panelChildren.getOffsetWidth(), this.root.panelChildren.getOffsetHeight());
    }

    public static <I extends Item> NavTree<I> select(NavTree<I> tree, I selectedItem) {
        return select(tree, selectedItem, false);
    }

    public static <I extends Item> NavTree<I> select(NavTree<I> tree, I selectedItem, boolean fireEvent) {
        final Item treeitem = tree.getItem();
        if (treeitem == null) {
            throw new IllegalStateException("treeitem == null"); // $NON-NLS$
        }
        NavTree<I> selectedNavTree = null;
        final boolean selected = tree.getItem().equals(selectedItem);
        tree.setSelected(selected, fireEvent);
        if (selected) {
            selectedNavTree = tree;
        }
        if (tree.getChildren() != null) {
            final List<NavTree<I>> children = tree.getChildren();
            for (NavTree<I> child : children) {
                final NavTree<I> subTree = select(child, selectedItem);
                if (subTree != null) {
                    selectedNavTree = subTree;
                }
            }
        }
        return selectedNavTree;
    }

    public NavTree<I> findNavTreeById(String id, NavTreeItemIdentifier<I> identifier) {
        return findNavTreeById(this, id, identifier);
    }

    public static <I extends Item> NavTree<I> findNavTreeById(NavTree<I> tree, String id, NavTreeItemIdentifier<I> identifier) {
        if (identifier.hasId(tree.getItem(), id)) {
            return tree;
        }
        if (tree.getChildren() == null) {
            return null;
        }
        for (NavTree<I> branch : tree.getChildren()) {
            final NavTree<I> found = findNavTreeById(branch, id, identifier);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public NavTree<I> removeById(String id, NavTreeItemIdentifier<I> identifier) {
        final NavTree<I> navTree = findNavTreeById(id, identifier);
        if (navTree == null) {
            Firebug.warn("NavTree <deleteById> cannot delete item, id not found: " + id);
            return null;
        }

        final NavTree parentNavTree = navTree.getParentNavTree();
        parentNavTree.children.remove(navTree);
        parentNavTree.panelChildren.remove(navTree);
        fireResizeEvent();
        return navTree;
    }
}