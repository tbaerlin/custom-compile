package de.marketmaker.iview.mmgwt.mmweb.client.runtime;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation.*;

/**
 *
 * Defines app-specific strings using {@link Permutation} and a strategy for every permutation
 *
 * Created on 10.03.16
 *
 * @author mloesch
 */

public enum PermStr {
    // TODO refactor according to FeatureFlags to ease testing!
    DASHBOARD(reg(AS, I18n.I::dashboard), reg(MMF, I18n.I::dashboard), reg(GIS, I18n.I::workspace)),
    DASHBOARDS(reg(AS, I18n.I::dashboards), reg(MMF, I18n.I::dashboards), reg(GIS, I18n.I::workspaces)),
    DASHBOARD_ADD(reg(AS, I18n.I::dashboardAdd), reg(MMF, I18n.I::dashboardAdd), reg(GIS, I18n.I::workspaceAdd)),
    DASHBOARD_COPY(reg(AS, I18n.I::dashboardCopy), reg(MMF, I18n.I::dashboardCopy), reg(GIS, I18n.I::workspaceCopy)),
    DASHBOARD_DEFAULT_NAME_ADD(reg(AS, I18n.I::dashboardDefaultNameAdd), reg(MMF, I18n.I::dashboardDefaultNameAdd), reg(GIS, I18n.I::workspaceDefaultNameAdd)),
    DASHBOARD_DELETE(reg(AS, I18n.I::dashboardDelete), reg(MMF, I18n.I::dashboardDelete), reg(GIS, I18n.I::workspaceDelete)),
    DASHBOARD_DELETE_CONFIRM(reg(AS, I18n.I::dashboardDeleteConfirm), reg(MMF, I18n.I::dashboardDeleteConfirm), reg(GIS, I18n.I::workspaceDeleteConfirm)),
    DASHBOARD_EDIT(reg(AS, I18n.I::dashboardEdit), reg(MMF, I18n.I::dashboardEdit), reg(GIS, I18n.I::workspaceEdit)),
    DASHBOARD_ERROR_ANY(reg(AS, I18n.I::dashboardErrorAny), reg(MMF, I18n.I::dashboardErrorAny), reg(GIS, I18n.I::workspaceErrorAny)),
    DASHBOARD_ERROR_DUPLICATE_NAME(reg(AS, I18n.I::dashboardErrorDuplicateName), reg(MMF, I18n.I::dashboardErrorDuplicateName), reg(GIS, I18n.I::workspaceErrorDuplicateName)),
    DASHBOARD_ERROR_GET_PRIV_FAILED(reg(AS, I18n.I::dashboardErrorGetPrivFailed), reg(MMF, I18n.I::dashboardErrorGetPrivFailed), reg(GIS, I18n.I::workspaceErrorGetPrivFailed)),
    LINKED_OBJECTS(reg(AS, I18n.I::linkedObjects), reg(MMF, I18n.I::associates), reg(GIS, I18n.I::associates)),
    LIVE_FINDER(reg(AS, I18n.I::advancedSearch), reg(MMF, I18n.I::detailSearch), reg(GIS, I18n.I::detailSearch));

    private final Map<Permutation, Function<String, String>> map;

    @SafeVarargs
    PermStr(Map.Entry<Permutation, Function<String, String>>... entrys) {
        this.map = new IdentityHashMap<>();
        for (Map.Entry<Permutation, Function<String, String>> entry : entrys) {
            this.map.put(entry.getKey(), entry.getValue());
        }
    }

    static Map.Entry<Permutation, Function<String, String>> reg(Permutation perm, Supplier<String> supplier) {
        return new HashMap.SimpleEntry<>(perm, (Function<String, String>) text -> supplier.get());
    }

    static Map.Entry<Permutation, Function<String, String>> reg(Permutation perm, Function<String, String> function) {
        return new HashMap.SimpleEntry<>(perm, function);
    }

    public String value() {
        return value(null);
    }

    public String value(String param) {
        final Permutation perm = Permutation.get();
        if (this.map.isEmpty() || !this.map.containsKey(perm)) {
            throw new RuntimeException("No value found for permutation " + perm);  // $NON-NLS$
        }
        return this.map.get(perm).apply(param);
    }
}