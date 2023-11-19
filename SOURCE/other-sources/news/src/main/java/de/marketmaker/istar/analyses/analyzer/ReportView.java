package de.marketmaker.istar.analyses.analyzer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class ReportView implements Serializable {

    private final List<String> columnTitles = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

    public void setTitles(Collection<String> titles) {
        columnTitles.addAll(titles);
    }

    public void addRow(Collection<String> elements) {
        List<String> row = new ArrayList<>(elements);
        rows.add(row);
    }

    public List<String> getColumnTitles() {
        return columnTitles;
    }

    public Collection<List<String>> getRows() {
        return rows;
    }

}
