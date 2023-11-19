/*
 * PermutationProvider.java
 *
 * Created on 01.07.2016 15:35
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.runtime;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * This is necessary to ease testing of AppNameProvider, because Permutation cannot be mocked!
 * @author mdick
 */
@SuppressWarnings("unused") //injected automatically via @Singleton therefore the class is not referenced!
@Singleton
public class PermutationProvider implements Provider<Permutation> {
    @Override
    public Permutation get() {
        return Permutation.get();
    }
}
