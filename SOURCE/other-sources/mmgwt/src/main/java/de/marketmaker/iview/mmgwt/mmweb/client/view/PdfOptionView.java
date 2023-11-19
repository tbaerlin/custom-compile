package de.marketmaker.iview.mmgwt.mmweb.client.view;

/**
 * @author umaurer
 */
public interface PdfOptionView {
    void addOption(final String id, String title, boolean checked, String style);
    void addOption(final String id, final String title, final String[] values, final boolean checked, final String style);
    void addLink(String link, String style, boolean httpPost);
    void updateLink(String link, boolean httpPost);
}
