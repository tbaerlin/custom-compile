package de.marketmaker.istar.ratios.frontend;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EnumFlyweightFactory {
    /**
     * If a selector is supposed to be created <em>before</em> the data that contains the enum
     * values is actually read, we cannot use {@link EnumFlyweightFactory#get(int, String)} to
     * figure out whether the selector would match anything at all. Instead, we have to add the
     * search expression as an interned enum. Mainly useful for batch programs such as
     * {@link de.marketmaker.istar.ratios.mdpsexport.MdpsExporter} which can avoid to collect
     * all RatioData objects in memory before exporting them.
     */
    public static final boolean WITH_LAZY_ENUMS = Boolean.getBoolean("withLazyEnums");

    @SuppressWarnings({"unchecked"})
    private final static Map<String, String>[] MAPS
            = (Map<String, String>[]) new Map[RatioFieldDescription.getMaxFieldId() + 1];

    static {
        final BitSet bs = RatioFieldDescription.getEnums();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            MAPS[i] = new HashMap<>();
        }
    }

    public static String[] intern(int fieldid, String[] str) {
        final String[] result = new String[str.length];

        for (int i = 0; i < str.length; i++) {
            result[i] = intern(fieldid, str[i]);
        }

        return result;
    }

    public static String get(int fieldid, String str) {
        final Map<String, String> map = MAPS[fieldid];
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (map) {
            return map.get(str);
        }
    }

    public static String intern(int fieldid, String str) {
        final Map<String, String> map = MAPS[fieldid];
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (map) {
            // todo: remove obsolete map entries
            return map.computeIfAbsent(str, k -> str);
        }
    }

    public static List<String> values(int fieldid) {
        final Map<String, String> map = MAPS[fieldid];
        if (map == null) {
            return null;
        }
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (map) {
            return new ArrayList<>(map.keySet());
        }
    }

    public static String asString() {
        final StringBuilder sb = new StringBuilder();
        final BitSet bs = RatioFieldDescription.getEnums();
        int n = 0;
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            n += MAPS[i].size();
            sb.append(RatioFieldDescription.getFieldById(i))
                    .append(" #").append(MAPS[i].size());
            if (!MAPS[i].isEmpty()) {
                long nb = 0;
                for (String s : MAPS[i].keySet()) {
                    nb += s.length();
                }
                sb.append(" (").append(nb / MAPS[i].size()).append(")");
            }
        }
        sb.append(", total: ").append(n);
        return sb.toString();
    }
}
