package de.marketmaker.itools.gwtutil.client.util;

/**
 * Author: umaurer
 * Created: 13.08.14
 */
public interface FocusKeyHandler {
    boolean onFocusKeyClick();
    boolean onFocusKeyEscape();
    boolean onFocusKeyHome();
    boolean onFocusKeyPageUp();
    boolean onFocusKeyUp();
    boolean onFocusKeyDown();
    boolean onFocusKeyPageDown();
    boolean onFocusKeyEnd();
    boolean onFocusKey(char c);
    boolean onFocusDelete();
    boolean onFocusAdd();
}
