package de.marketmaker.istar.ratios.frontend;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NotSelector implements Selector {

    private final Selector delegate;

    public NotSelector(Selector delegate) {
        this.delegate = delegate;
    }

    public boolean select(Selectable s) {
        return !this.delegate.select(s);
    }

    @Override
    public int getCost() {
        return 1 + this.delegate.getCost();
    }

    public String toString() {
        return "!" + this.delegate.toString();
    }
}
