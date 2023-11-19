package de.marketmaker.istar.merger.web.easytrade.util;

// used as key in GroupValueNode
public class GroupValueKey {
    private final String group;

    private final String value;

    public GroupValueKey(String groupName, String groupValue) {
        this.group = groupName;
        this.value = groupValue;
    }

    public String getGroup() {
        return group;
    }

    public String getTranslation() {
        return getGroup();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupValueKey that = (GroupValueKey) o;
        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public String toString() {
        return group + " [" + value + "]";
    }
}
