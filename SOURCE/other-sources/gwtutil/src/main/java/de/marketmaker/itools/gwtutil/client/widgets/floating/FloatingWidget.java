package de.marketmaker.itools.gwtutil.client.widgets.floating;

/**
 * @author Ulrich Maurer
 *         Date: 22.10.12
 */
public interface FloatingWidget {
    int getMinimumPosition();
    int getMaximumPosition();
    int getPosition();
    int setPosition(int position, boolean withTransition);
    void updateButtonVisibility();
}
