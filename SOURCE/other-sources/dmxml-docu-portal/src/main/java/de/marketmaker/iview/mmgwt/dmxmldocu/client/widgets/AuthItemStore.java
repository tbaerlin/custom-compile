package de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets;

import java.util.List;

/**
 * @author Markus Dick
 */
public interface AuthItemStore {
    public List<AuthItem> getIAuthItems();
    public void setIAuthItems(List<AuthItem> authItems);
}
