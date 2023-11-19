package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

/**
 * Author: umaurer
 * Created: 27.03.14
 */
public class BindKeyIndexed extends BindKey {
    private final int index;

    public BindKeyIndexed(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return String.valueOf(this.index);
    }
}
