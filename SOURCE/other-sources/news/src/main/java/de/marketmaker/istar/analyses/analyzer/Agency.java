package de.marketmaker.istar.analyses.analyzer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ComparisonChain;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * data object for a rating agency, contains references to the analyses
 */
public class Agency {

    // dpa-afx:NDB_Analyst_Institute_Name == source in protobuf must not be null
    // example data: JPMORGAN, GOLDMAN SACHS, KEPLER CHEUVREUX
    private final String id;

    // dpa-afx:NDB_Analyst_Institute_Symbol
    // this is might be null for some agencies (CLOSE BROTHERS SEYDLER, CANACCORD, WELLS FARGO)
    // values are:
    // BRR, BNP, CRS, JPM, SPE, STI, INR, NBA, SVR, OPP, RDB, RBC, DZ, EQI, BEB, COM,
    // DEB, BAA, BAC, NOE, UBS, NOL, JEF, HSX, JMP, BAL, MAC, CAM, MST, ATLE, GES, MEL,
    // CFTZ, SOT, KEU, MORN, HAU
    @Nullable
    private String symbol;

    // the analyses for this agency
    private final Set<Analysis> analyses = new HashSet<>();

    public static final Comparator<Agency> ID_COMPARATOR
            = (left, right) -> ComparisonChain.start().compare(left.id, right.id).result();

    Agency(String id) {
        assert id != null : "id for agency must not be null";
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void put(Analysis analysis) {
        analyses.add(analysis);
    }

    public Set<Analysis> getAnalyses() {
        return analyses;
    }

    @Override
    public String toString() {
        return "Agency [" + id + "/" + symbol
                + " has: " + analyses.size() + " analyses"
                + "]";
    }

}
