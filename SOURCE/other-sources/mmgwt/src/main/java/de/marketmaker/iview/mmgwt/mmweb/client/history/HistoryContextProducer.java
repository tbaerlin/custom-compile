/*
 * HistoryContextProducer.java
 *
 * Created on 08.07.2014 11:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.history;

/**
 * This producer solves the following problem that occurs when navigating from a parent object to a child object with
 * a context that contains a list:
 *
 * 1. in parent object view click on linked child1 (has history context hc1 with child list cl1, selected item is child1),
 * 2. click context down button to switch to child2 (selected item of instance cl1 is now child2)
 * 3. click on context back to navigate to parent object
 * 4. click again on linked child1 (history context is still hc1 with child list cl1. Selected item is still child2)
 * Result: data of child1 is shown but child2 is selected in the context list, which is wrong!
 * Therefore: provide a possibility to create new history contexts every time the user triggers a navigation to child
 * views whose contexts have lists, i.e. when the defaultGoTo delegate of the NavItemSpec is called.
 *
 * @author Markus Dick
 */
public interface HistoryContextProducer {
    HistoryContext produce();
}
