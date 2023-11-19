package de.marketmaker.iview.pmxml.block;

import de.marketmaker.iview.pmxml.QueryResponseState;
import org.omg.CORBA.DynAnyPackage.Invalid;

/**
 * Created on 22.07.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class InvalidResponseException extends Exception {
    private QueryResponseState state;

    public QueryResponseState getState() {
        return this.state;
    }

    public InvalidResponseException(QueryResponseState state) {
        this.state = state;
    }

    public static void check(QueryResponseState queryResponseState) throws InvalidResponseException {
        if (queryResponseState != QueryResponseState.QRS_OK) {
            throw new InvalidResponseException(queryResponseState);
        }
    }
}