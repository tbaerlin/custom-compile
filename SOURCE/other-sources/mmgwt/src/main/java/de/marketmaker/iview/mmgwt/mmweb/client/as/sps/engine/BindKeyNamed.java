package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

/**
 * Author: umaurer
 * Created: 27.03.14
 */
public class BindKeyNamed extends BindKey {
    private final String name;

    public BindKeyNamed(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return this.name;
    }
}
