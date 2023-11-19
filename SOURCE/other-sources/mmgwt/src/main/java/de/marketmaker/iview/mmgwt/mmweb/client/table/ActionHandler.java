/*
 * ActionHandler.java
 *
 * Created on 07.01.2009 15:56:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.List;

/**
 * Object that knows which actions exist, which are applicable to certain objects, and
 * how to perform those actions when the user selects them.
 * An ActionHandler is used as a delegate of an {@link de.marketmaker.iview.mmgwt.mmweb.client.table.ActionRenderer}
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ActionHandler<D> {
    /**
     * all actions that this handler can process
     * @return list of actions
     */
    List<Action> getActions();

    boolean isActionApplicableTo(Action action, D data);

    void doAction(Action action, D data);
}
