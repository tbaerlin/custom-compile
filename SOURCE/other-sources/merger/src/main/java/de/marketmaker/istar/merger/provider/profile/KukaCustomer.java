/*
 * KukaCustomer.java
 *
 * Created on 15.07.11 10:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.util.HashSet;
import java.util.Set;

/**
* @author oflege
*/
class KukaCustomer {
    static class Builder {
        private final String kennung;

        private final String kennwort;

        private Set<String> abos = new HashSet<>();

        Builder(String kennung, String kennwort) {
            this.kennung = kennung;
            this.kennwort = kennwort;
        }

        public void setAbos(Set<String> abos) {
            this.abos = abos;
        }

        KukaCustomer build() {
            return new KukaCustomer(kennung, kennwort, this.abos);
        }
    }

    private final String kennung;

    private final String kennwort;

    private final Set<String> abos;

    private KukaCustomer(String kennung, String kennwort, Set<String> abos) {
        this.kennung = kennung;
        this.kennwort = kennwort;
        this.abos = abos;
    }

    public String getKennung() {
        return this.kennung;
    }

    public String getKennwort() {
        return this.kennwort;
    }

    public Set<String> getAbos() {
        return this.abos;
    }

    public boolean hasAbo(String x) {
        return x != null && this.abos != null && this.abos.contains(x);
    }

    @Override
    public String toString() {
        return "KukaCustomer{" +
                "kennung='" + kennung + '\'' +
                ", kennwort='" + kennwort + '\'' +
                ", abos=" + abos +
                '}';
    }
}
