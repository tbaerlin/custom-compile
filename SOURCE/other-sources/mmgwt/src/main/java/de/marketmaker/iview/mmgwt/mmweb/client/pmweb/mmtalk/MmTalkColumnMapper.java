package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import de.marketmaker.iview.pmxml.MM;

/**
 * Created on 28.02.13 10:34
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class MmTalkColumnMapper<O> {
    private final String formular;

    public MmTalkColumnMapper(String formular) {
        if (formular.endsWith(".")) {
            throw new IllegalArgumentException("formula must not end with a '.'! Use MmTalkComplexColumnMapper instead."); // $NON-NLS$
        }
        this.formular = formular;
    }

    public String getFormula() {
        return this.formular;
    }

    public abstract void setValue(O object, MM item);
}