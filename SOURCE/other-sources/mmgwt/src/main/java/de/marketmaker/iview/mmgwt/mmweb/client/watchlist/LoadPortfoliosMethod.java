/*
 * LoadPortfoliosMethod.java
 *
 * Created on 18.02.2016 08:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.PFEvaluation;

/**
 * @author mdick
 */
public class LoadPortfoliosMethod extends AbstractLoadElementsMethod<LoadPortfoliosMethod, PFEvaluation> {
    public LoadPortfoliosMethod(AsyncCallback<PFEvaluation> callback) {
        super("PF_Evaluation", callback); // $NON-NLS$
    }
}
