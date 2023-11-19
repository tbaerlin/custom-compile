/*
 * ReleaseDetail.java
 *
 * Created on 15.03.12 16:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public class ReleaseDetail extends AbstractRelease implements Serializable {

    private final int uid;

    private String highlights;

    private String consensusNotes;

    private Image chart1;

    private Image chart2;

    private Image grid1;

    public ReleaseDetail(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    public Image getChart1() {
        return chart1;
    }

    public Image getChart2() {
        return chart2;
    }

    public String getConsensusNotes() {
        return consensusNotes;
    }

    public Image getGrid1() {
        return grid1;
    }

    public String getHighlights() {
        return highlights;
    }

    void setChart1(Image chart1) {
        this.chart1 = chart1;
    }

    void setChart2(Image chart2) {
        this.chart2 = chart2;
    }

    void setConsensusNotes(String consensusNotes) {
        this.consensusNotes = consensusNotes;
    }

    void setGrid1(Image grid1) {
        this.grid1 = grid1;
    }

    void setHighlights(String highlights) {
        this.highlights = highlights;
    }
}
