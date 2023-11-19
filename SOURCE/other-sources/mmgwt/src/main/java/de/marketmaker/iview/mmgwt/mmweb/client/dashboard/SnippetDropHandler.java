package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

/**
 * Author: umaurer
 * Created: 17.07.15
 */
public interface SnippetDropHandler {
    boolean isDropAllowed(String transferData, int row, int column);
    boolean onDndEnter(String transferData, int row, int column);
    void onDndLeave();
    void onDrop(String transferData, int row, int column);
}
