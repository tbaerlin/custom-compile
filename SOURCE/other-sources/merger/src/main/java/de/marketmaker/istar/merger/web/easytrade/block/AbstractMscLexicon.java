/*
 * AbstractMscLexicon.java
 *
 * Created on 19.10.2009 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.web.easytrade.provider.LexiconProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractMscLexicon extends EasytradeCommandController {
    protected LexiconProvider lexiconProvider;

    protected AbstractMscLexicon(Class aClass) {
        super(aClass);
    }

    public void setLexiconProvider(LexiconProvider lexiconProvider) {
        this.lexiconProvider = lexiconProvider;
    }
}
