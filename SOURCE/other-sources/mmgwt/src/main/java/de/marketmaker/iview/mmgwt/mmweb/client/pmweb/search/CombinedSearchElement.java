/*
 * CombinedSearchElement.java
 *
 * Created on 22.02.13 14:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import de.marketmaker.iview.dmxml.MSCBasicSearchElement;
import de.marketmaker.iview.pmxml.ShellMMInfo;

/**
 * @author Markus Dick
 */
class CombinedSearchElement {
    static enum State { EMPTY_ISIN, AVAILABLE, AMBIGUOUS_ISIN, NOT_AVAILABLE, NO_ORDER_ENTRY_DUE_TO_BUSINESS_RULES }

    private ShellMMInfo shellMMInfo;
    private MSCBasicSearchElement mscBasicSearchElement;
    private State shellMmInfoState;

    CombinedSearchElement(MSCBasicSearchElement mscBasicSearchElement) {
        this.mscBasicSearchElement = mscBasicSearchElement;
    }

    CombinedSearchElement(ShellMMInfo shellMMInfo) {
        this.shellMMInfo = shellMMInfo;
    }

    public MSCBasicSearchElement getMscBasicSearchElement() {
        return mscBasicSearchElement;
    }

    public void setMscBasicSearchElement(MSCBasicSearchElement mscBasicSearchElement) {
        this.mscBasicSearchElement = mscBasicSearchElement;
    }

    public ShellMMInfo getShellMMInfo() {
        return shellMMInfo;
    }

    public void setShellMMInfo(ShellMMInfo shellMMInfo) {
        this.shellMMInfo = shellMMInfo;
    }

    public State getShellMmInfoState() {
        return this.shellMmInfoState;
    }

    public void setShellMmInfoState(State shellMmInfoState) {
        this.shellMmInfoState = shellMmInfoState;
    }

    public String getIsin() {
        if(this.mscBasicSearchElement != null) {
            return this.mscBasicSearchElement.getInstrumentdata().getIsin();
        }
        if(this.shellMMInfo != null) {
            return this.shellMMInfo.getISIN();
        }
        return null;
    }
}
