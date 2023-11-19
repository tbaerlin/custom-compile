package de.marketmaker.itools.i18n;

/**
 * @author umaurer
 */
public class KeyValueWithDistance implements Comparable<KeyValueWithDistance> {
    private final String key;
    private final String value;
    private final int distance;

    public KeyValueWithDistance(String key, String value, int distance) {
        this.key = key;
        this.value = value;
        this.distance = distance;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return this.key + "->" + this.value + " (" + this.distance + ")";
    }

    public int compareTo(KeyValueWithDistance o) {
        int result = this.distance - o.distance;
        if (result != 0) {
            return result;
        }
        result = this.key.compareTo(o.key);
        if (result != 0) {
            return result;
        }
        return this.value.compareTo(o.value);
    }
}
