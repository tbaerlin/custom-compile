/*
 * SelectorDefinitionProvider.java
 *
 * Created on 20.07.12 15:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

/**
 * @author Markus Dick
 */
public interface SelectorDefinitionProvider {
    SelectorDefinition getSelectorDefinition(int id);
}
