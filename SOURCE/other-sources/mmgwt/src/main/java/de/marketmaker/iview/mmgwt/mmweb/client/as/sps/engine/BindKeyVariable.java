package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

/**
 * Author: umaurer
 * Created: 27.03.14
 */
public class BindKeyVariable extends BindKey {
    private final String bindKeys;

    public BindKeyVariable(String bindKeys) {
        this.bindKeys = bindKeys;
    }

    public String getBindKeys() {
        return bindKeys;
    }

    public String toString() {
        return this.bindKeys;
    }
}
