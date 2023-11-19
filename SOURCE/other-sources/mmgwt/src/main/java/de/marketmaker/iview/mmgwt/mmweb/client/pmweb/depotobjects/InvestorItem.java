package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author umaurer
 */
public class InvestorItem extends BaseTreeModel implements Comparable<InvestorItem> {
    public static final String ZONE_PMWEB_LOCAL = "pm local"; // $NON-NLS$
    private final boolean editable;
    private static long keyCounter = 0;
    private static final Map<String, InvestorItem> INVESTOR_BY_KEY = new HashMap<String, InvestorItem>();
    private final String key;
    private final boolean cached;

    public static final InvestorItem ROOT_GROUP = new InvestorItem("12345", InvestorItem.Type.Gruppe, "ALL", "PUBLIC", HasChildren.YES, false, true); // $NON-NLS$
    // public static final InvestorItem ROOT_GROUP = new InvestorItem("0006070", InvestorItem.Type.Gruppe, "WEM", "HVB Luxembourg", HasChildren.YES, false); // $NON-NLS$

    public static enum Type {
        ROOT("", "", ShellMMType.ST_GRUPPE),  // $NON-NLS$
        Gruppe(I18n.I.group(), "pm-investor-group", ShellMMType.ST_GRUPPE),  // $NON-NLS$
        Inhaber(I18n.I.pmInvestor(), "pm-investor", ShellMMType.ST_INHABER),  // $NON-NLS$
        Holder(I18n.I.pmInvestor(), "pm-investor", ShellMMType.ST_INHABER),  // $NON-NLS$
        Portfolio(I18n.I.portfolio(), "pm-investor-portfolio", ShellMMType.ST_PORTFOLIO),  // $NON-NLS$
        Depot(I18n.I.depot(), "pm-investor-depot", ShellMMType.ST_DEPOT), // $NON-NLS$
        Konto(I18n.I.account(), "pm-investor-account", ShellMMType.ST_KONTO);  // $NON-NLS$

        private final String desciption;
        private final String iconStyle;
        private final ShellMMType shellMMType;

        Type(String desciption, String iconStyle, ShellMMType shellMMType) {
            this.desciption = desciption;
            this.iconStyle = iconStyle;
            this.shellMMType = shellMMType;
        }

        public ShellMMType getShellMMType() {
            return this.shellMMType;
        }

        public String getDesciption() {
            return desciption;
        }

        public String getIconStyle() {
            return iconStyle;
        }

        public AbstractImagePrototype getIcon() {
            return IconImage.get(this.iconStyle);
        }

        public AbstractImagePrototype getIconLarge() {
            return IconImage.get(this.iconStyle + "-large"); // $NON-NLS-0$
        }

        public static List<Type> fromShellMMType(ShellMMType shellMMType) {
            final ArrayList<Type> types = new ArrayList<Type>();
            final Type[] values = Type.values();
            for (Type value : values) {
                if (value.shellMMType.value().equals(shellMMType.value())) {
                    types.add(value);
                }
            }
            return types;
        }
    }

    public static enum HasChildren {
        UNKNOWN, YES, NO
    }

    public InvestorItem copy() {
        return new InvestorItem(this.getId(), this.getType(), this.getName(), this.getZone(), this.editable);
    }

    public InvestorItem deepCopy() {
        InvestorItem item = this.copy();
        final List<ModelData> children = this.getChildren();
        for (ModelData child : children) {
            item.add(((InvestorItem) child).deepCopy());
        }
        return item;
    }

    public InvestorItem(String id, Type type, String name, String zone, boolean editable) {
        this(id, type, name, zone, HasChildren.UNKNOWN, editable, true);
    }

    public InvestorItem(String id, Type type, String name, String zone, HasChildren hasChildren, boolean editable, boolean cached) {
        this.key = String.valueOf(keyCounter++);
        this.cached = cached;
        if (cached) {
            INVESTOR_BY_KEY.put(key, this);
        }
        set("id", id); // $NON-NLS$
        set("type", type); // $NON-NLS$
        setName(name);
        setZone(zone);
        setHasChildren(hasChildren);
        this.editable = editable;
    }

    public String getId() {
        return get("id"); // $NON-NLS-0$
    }

    public Type getType() {
        return get("type"); // $NON-NLS-0$
    }

    public String getKey() {
        return this.key;
    }

    public static InvestorItem getByKey(String key) {
        return INVESTOR_BY_KEY.get(key);
    }

/*
    public String getKey() {
        final InvestorItem parent = (InvestorItem) getParent();
        if (parent == null) {
            return getId();
        }
        final String parentKey = parent.getKey();
        return parentKey == null ? getId() : (parentKey + "-" + getId());
    }
*/

    public boolean isCached() {
        return cached;
    }

    public boolean isType(Type type) {
        return getType() == type;
    }

    public String getName() {
        return get("name"); // $NON-NLS-0$
    }

    public void setName(String name) {
        set("name", name); // $NON-NLS-0$
        setLabel(getKey(), getType(), name);
    }

    private void setLabel(String key, Type type, String name) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<span"); // $NON-NLS$
        if (type != Type.ROOT) {
            sb.append(" class=\"pm-investorItem\""); // $NON-NLS$
        }
        sb.append(" onclick=\"mmInvestorTreeItemClick('"); // $NON-NLS$
        sb.append(key);
        sb.append("')\"");
        sb.append(" title=\""); // $NON-NLS$
        sb.append(type);
        sb.append(": ");
        sb.append(name);
        sb.append("\"");
        sb.append(">");

        sb.append(type.getIcon().getHTML());
        sb.append("&nbsp;"); // $NON-NLS$

        sb.append(name);
        sb.append("</span>"); // $NON-NLS$
        set("label", sb.toString()); // $NON-NLS$
    }

    public String getZone() {
        return get("zone"); // $NON-NLS$
    }

    public void setZone(String zone) {
        set("zone", zone); // $NON-NLS$
    }

    public void setHasChildren(HasChildren hasChildren) {
        set("hasChildren", hasChildren); // $NON-NLS$
    }

    public HasChildren getHasChildren() {
        return get("hasChildren"); // $NON-NLS$
    }

    public boolean isEditable() {
        return editable;
    }

    @Override
    public String toString() {
        return "InvestorItem{" + // $NON-NLS$
                "editable=" + editable + // $NON-NLS$
                ", key='" + key + '\'' + // $NON-NLS$
                ", cached=" + cached + // $NON-NLS$
                ", id=" + get("id") + // $NON-NLS$
                ", type=" + get("type") + // $NON-NLS$
                ", name=" + getName() + // $NON-NLS$
                ", zone=" + getZone() + // $NON-NLS$
                '}';
    }

    public List<InvestorItem> asList() {
        final List<InvestorItem> list = new ArrayList<InvestorItem>(1);
        list.add(this);
        return list;
    }


    public boolean isAncestorOf(InvestorItem item) {
        final InvestorItem parent = (InvestorItem) item.getParent();
        return parent != null && (parent.equals(this) || this.isAncestorOf(parent));
    }


    @Override
    public int hashCode() {
        return getId().hashCode() + getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InvestorItem && ((InvestorItem) obj).getId().equals(getId()) && ((InvestorItem) obj).getName().equals(getName());
    }

    public int compareTo(InvestorItem o) {
        int result = getName().compareTo(o.getName());
        if (result != 0) {
            return result;
        }
        return getType().compareTo(o.getType());
    }



}
