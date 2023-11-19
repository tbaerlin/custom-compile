package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSpec;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.tree.HasParent;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.Item;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContextProducer;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.IdentityHistoryContextProducer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 04.11.11
 */
public class NavItemSpec implements HasParent, Item {
    private static final HistoryContextProducer NULL_CONTEXT_PRODUCER = new IdentityHistoryContextProducer(null);

    public static final String SUBCONTROLLER_KEY = "sc"; //$NON-NLS$
    public static final HistoryToken NO_NAV_TOKEN = HistoryToken.builder("NO_NAV_TOKEN").build(); //$NON-NLS$

    private final String id;
    private String name;
    private String nameSuffix;
    private final PageController controller;
    private final HistoryToken token;
    private NavItemSpec parent = null;
    private ArrayList<NavItemSpec> listChildren = null;
    private final HistoryContextProducer contextProducer;
    private boolean enabled = true;
    private boolean visible = true;
    private GoToDelegate goToDelegate = null;
    private ImageSpec icon = null;
    private SafeHtml iconTooltip = null;
    private Widget endIcon = null;
    private String endIconCellClass = null;
    private ImageSpec leftIcon = null;
    private SafeHtml leftIconTooltip = null;
    private boolean selectFirstChildOnOpen = false;

    private boolean alwaysOpen = false;
    private boolean openByDefault = false;
    private boolean openWithParent = false;
    private boolean openWithSelection = false;
    private boolean closingSiblings = false;
    private boolean hasDelegate = false;
    private boolean transientItem = false;
    private boolean updateContentHeader = true;

    public interface GoToDelegate {
        void goTo(String value);
    }

    public NavItemSpec(String id, String name) {
        this(id, name, null, null, null, null);
    }

    public NavItemSpec(String id, String name, HistoryToken token, HistoryContext context) {
        this(id, name, token, null, new IdentityHistoryContextProducer(context), null);
    }

    public NavItemSpec(String id, String name, HistoryToken token, HistoryContextProducer contextProducer) {
        this(id, name, token, null, contextProducer, null);
    }

    public NavItemSpec(String id, String name, HistoryToken token) {
        this(id, name, token, null, null, null);
    }

    public NavItemSpec(String id, String name, HistoryToken token, PageController controller) {
        this(id, name, token, controller, null, null);
    }

    public NavItemSpec(String id, String name, GoToDelegate goToDelegate) {
        this(id, name, NO_NAV_TOKEN, null, null, goToDelegate);
    }

    public NavItemSpec(String id, String name, HistoryToken token, PageController controller, HistoryContextProducer contextProducer, GoToDelegate goToDelegate) {
        if (!StringUtil.hasText(id)) {
            throw new IllegalStateException("id must not be null or empty!"); // $NON-NLS$
        }
        this.id = id;
        this.name = name;
        this.token = token;
        this.controller = controller;
        this.contextProducer = contextProducer != null ? contextProducer : NULL_CONTEXT_PRODUCER;

        if (goToDelegate != null) {
            this.goToDelegate = goToDelegate;
        }
        else if (token != null) {
            this.goToDelegate = new GoToDelegate() {
                @Override
                public void goTo(String value) {
                    defaultGoTo();
                }
            };
        }
    }

    private void defaultGoTo() {
        assert token != null;
        if (this.controller == null) {
            this.token.fire(this.contextProducer.produce());
        }
        else {
            this.token.with(SUBCONTROLLER_KEY, this.id).fire(this.contextProducer.produce());
        }
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        if (StringUtil.hasText(this.nameSuffix)) {
            return this.name + " " + this.nameSuffix;
        }
        return this.name;
    }

    public HistoryToken getHistoryToken() {
        return this.token;
    }

    public void setParent(NavItemSpec parent) {
        this.parent = parent;
    }

    public NavItemSpec getParent() {
        return parent;
    }

    public void goTo(String value) {
        if (this.goToDelegate == null || !isEnabled()) {
            return;
        }
        this.goToDelegate.goTo(value);
    }

    public PageController getController() {
        return this.controller;
    }

    public NavItemSpec addChildren(List<NavItemSpec> navItemSpecChildren) {
        for (NavItemSpec navItemSpecChild : navItemSpecChildren) {
            addChild(navItemSpecChild);
        }
        return this;
    }

    public NavItemSpec addChildren(NavItemSpec... navItemSpecChildren) {
        for (NavItemSpec navItemSpecChild : navItemSpecChildren) {
            addChild(navItemSpecChild);
        }
        return this;
    }

    public NavItemSpec addChild(NavItemSpec navItemSpecChild) {
        navItemSpecChild.setParent(this);
        if (this.listChildren == null) {
            this.listChildren = new ArrayList<>();
        }
        this.listChildren.add(navItemSpecChild);
        return navItemSpecChild;
    }

    @Override
    public boolean isLeaf() {
        return this.listChildren == null || this.listChildren.isEmpty();
    }

    @Override
    public List<NavItemSpec> getChildren() {
        return this.listChildren;
    }

    public boolean hasChildren() {
        return this.listChildren != null;
    }

    public void setBaseName(String name) {
        this.name = name;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public NavItemSpec withGoToDelegate(GoToDelegate goToDelegate) {
        this.goToDelegate = goToDelegate;
        return this;
    }

    public NavItemSpec withAlwaysOpen() {
        this.alwaysOpen = true;
        return this;
    }

    @Override
    public boolean isAlwaysOpen() {
        return this.alwaysOpen;
    }

    public NavItemSpec withOpenByDefault() {
        this.openByDefault = true;
        return this;
    }

    @Override
    public boolean isOpenByDefault() {
        return this.openByDefault;
    }

    public NavItemSpec withOpenWithParent() {
        this.openWithParent = true;
        return this;
    }

    @Override
    public boolean isOpenWithParent() {
        return this.openWithParent;
    }

    public NavItemSpec withOpenWithSelection() {
        this.openWithSelection = true;
        return this;
    }

    @Override
    public boolean isOpenWithSelection() {
        return this.openWithSelection;
    }

    public NavItemSpec withClosingSiblings() {
        this.closingSiblings = true;
        return this;
    }

    public NavItemSpec withHasDelegate() {
        this.hasDelegate = true;
        return this;
    }

    public boolean isHasDelegate() {
        return hasDelegate;
    }

    public NavItemSpec withDoNotUpdateContentHeader() {
        this.updateContentHeader = false;
        return this;
    }

    public NavItemSpec withSelectFirstChildOnOpen() {
        this.selectFirstChildOnOpen = true;
        return this;
    }

    @Override
    public boolean isSelectFirstChildOnOpen() {
        return selectFirstChildOnOpen;
    }

    public boolean isUpdateContentHeader() {
        return this.updateContentHeader;
    }

    @Override
    public boolean isClosingSiblings() {
        return this.closingSiblings;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NavItemSpec)) return false;

        final NavItemSpec that = (NavItemSpec) o;

        if (!id.equals(that.id)) return false;
        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

    @Override
    public boolean hasSelectionHandler() {
        return this.goToDelegate != null && isEnabled();
    }

    @SuppressWarnings("unused")
    public boolean isPredecessor(NavItemSpec predecessor) {
        return this.parent != null
                && (this.parent.equals(predecessor) || this.parent.isPredecessor(predecessor));
    }

    public NavItemSpec findChildById(NavItemSpec spec, String id) {
        if (spec.getId().equals(id)) {
            return spec;
        }
        if (!spec.hasChildren()) {
            return null;
        }
        final List<NavItemSpec> childs = spec.getChildren();
        for (NavItemSpec child : childs) {
            final NavItemSpec result = findChildById(child, id);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public NavItemSpec findChildById(String id) {
        return findChildById(this, id);
    }

    /**
     * call this method if this NavItemSpec is transient and should not be saved e.g. as a default NavItemSpec
     *
     * @return itself
     */
    public NavItemSpec withIsTransient() {
        this.transientItem = true;
        return this;
    }

    public boolean isTransientItem() {
        return this.transientItem;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setIcon(String icon) {
        setIcon(icon, null);
    }

    public void setIcon(ImageSpec icon) {
        setIcon(icon, null);
    }

    public void setIcon(String icon, SafeHtml iconTooltip) {
        setIcon(IconImage.getImageResource(icon), iconTooltip);
    }

    public void setIcon(ImageSpec icon, SafeHtml iconTooltip) {
        this.icon = icon;
        this.iconTooltip = iconTooltip;
    }

    public NavItemSpec withIcon(String icon) {
        setIcon(icon, null);
        return this;
    }

    public NavItemSpec withIcon(ImageSpec icon) {
        setIcon(icon, null);
        return this;
    }

    public NavItemSpec withIcon(String icon, SafeHtml iconTooltip) {
        setIcon(icon, iconTooltip);
        return this;
    }

    public NavItemSpec withIcon(ImageSpec icon, SafeHtml iconTooltip) {
        setIcon(icon, iconTooltip);
        return this;
    }

    @Override
    public ImageSpec getIcon() {
        return this.icon;
    }

    @Override
    public SafeHtml getIconTooltip() {
        return this.iconTooltip;
    }

    @SuppressWarnings("unused")
    public NavItemSpec withEndIcon(String endIcon, SafeHtml endIconTooltip) {
        this.endIcon = IconImage.getImageResource(endIcon).asPrototype().createImage();
        Tooltip.addQtip(this.endIcon, endIconTooltip);
        return this;
    }

    public NavItemSpec withEndIcon(Widget endIcon) {
        this.endIcon = endIcon;
        return this;
    }

    @Override
    public Widget getEndIcon() {
        return this.endIcon;
    }

    public NavItemSpec withEndIconCellClass(String styleName) {
        this.endIconCellClass = styleName;
        return this;
    }

    @Override
    public String getEndIconCellClass() {
        return this.endIconCellClass;
    }

    public NavItemSpec withLeftIcon(String leftIcon, SafeHtml leftIconTooltip) {
        this.leftIcon = IconImage.getImageResource(leftIcon);
        this.leftIconTooltip = leftIconTooltip;
        return this;
    }

    @Override
    public ImageSpec getLeftIcon() {
        return this.leftIcon;
    }

    @Override
    public SafeHtml getLeftIconTooltip() {
        return this.leftIconTooltip;
    }
}