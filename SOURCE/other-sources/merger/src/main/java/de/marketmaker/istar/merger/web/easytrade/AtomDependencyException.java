package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.merger.MergerException;

/**
 * Thrown if an atom cannot access the model of the atom it depends on.
 * @author oflege
 *
 *
 */
public class AtomDependencyException extends MergerException {
    protected AtomDependencyException(final String message) {
        super(message);
    }

    public AtomDependencyException(Exception e) {
        super("dependent atom failed", e);
    }

    @Override
    public String getCode() {
        return "atom.dependency";
    }
}
