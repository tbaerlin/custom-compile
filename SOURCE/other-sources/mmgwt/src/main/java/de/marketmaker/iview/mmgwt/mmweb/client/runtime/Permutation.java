package de.marketmaker.iview.mmgwt.mmweb.client.runtime;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

import java.util.function.Predicate;

/**
 * Specifies predicates that define an application permutation at runtime.
 * It's hard to track every "runtime permutation" of the iview codebase (AS, mmf[web], GIS, ...).
 * This class should be used wherever a decission about what application runs has to be made.
 * The fundamental idea is to handle app-specific behaviour using a strategy pattern, like it's done in {@link PermStr}.
 * When applied consistently, the app-specific if-else-blocks should disappear extensively.
 * <p>
 * DON'T use this class to determine different GUI modes, permissioning or stuff like that.
 * The one and only use case is to differentiate between the applications based upon this codebase.
 * <p>
 * Created on 10.03.16
 *
 * @author mloesch
 */
public enum Permutation {
    // TODO refactor according to FeatureFlags to ease testing!

    //position defines eval order
    GIS((v) -> Selector.DZ_BANK_USER.isAllowed() || Selector.WGZ_BANK_USER.isAllowed()),
    AS((v) -> SessionData.isAsDesign() && SessionData.isWithPmBackend()),
    MMF((v) -> true);

    private final Predicate<Void> p;

    Permutation(Predicate<Void> p) {
        this.p = p;
    }

    public boolean isActive() {
        return this.p.test(null);
    }

    /**
     * @return the currently running permutation
     */
    public static Permutation get() {
        final Permutation[] perms = Permutation.values();
        for (Permutation perm : perms) {
            if (perm.isActive()) {
                return perm;
            }
        }
        return null;
/*
 //the science-fictionish way:
        return Arrays.stream(Permutation.values())
                .filter((c) -> c.isActive())
                .findFirst()
                .get();
*/
    }
}