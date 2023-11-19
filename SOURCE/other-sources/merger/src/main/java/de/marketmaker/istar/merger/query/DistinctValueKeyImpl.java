package de.marketmaker.istar.merger.query;


import java.io.Serializable;

public class DistinctValueKeyImpl<P extends Serializable>
        implements Serializable, Comparable<DistinctValueKeyImpl<P>>, DistinctValueCounter.PropertyValueKey<P> {

    private P value;
    private String name;

    public DistinctValueKeyImpl(P value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public P getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DistinctValueKeyImpl that = (DistinctValueKeyImpl) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(DistinctValueKeyImpl<P> that) {
        if (this.name == null ^ that.name == null) {
            return (this.name == null) ? -1 : 1;
        }
        if (that.name == null) {
            return 0;
        }
        if (this.equals(that)) {
            return 0;
        }

        final int nameCompare = this.name.compareTo(that.name);
        if (nameCompare != 0) {
            return nameCompare;
        }

        // we must not return 0 to be consistent with equals
        if (this.value == null ^ that.value == null) {
            return (this.value == null) ? -1 : 1;
        }
        return this.value.hashCode() - that.value.hashCode();
    }
}
