package de.marketmaker.iview.mmgwt.mmweb.client.data;

/**
 * @author Ulrich Maurer
 *         Date: 17.11.11
 */
public class BestOfCell {
    private final String key;

    private final String title;

    public BestOfCell(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }
}
