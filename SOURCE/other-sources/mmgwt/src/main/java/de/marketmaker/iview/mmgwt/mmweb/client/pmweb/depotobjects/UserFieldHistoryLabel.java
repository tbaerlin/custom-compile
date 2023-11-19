/*
 * UserFieldHistoryLabel.java
 *
 * Created on 26.04.13 14:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.HistoryItem;
import de.marketmaker.iview.pmxml.UserFieldDeclarationDesc;

import java.util.List;

/**
 * @author Markus Dick
 */
public class UserFieldHistoryLabel extends HistoryLabel {
    public UserFieldHistoryLabel(SafeHtml safeHtml, List<HistoryItem> historyList, UserFieldDeclarationDesc userFieldDeclarationDesc) {
        super(historyList, userFieldDeclarationDesc);
        setHTML(safeHtml);
    }

    public UserFieldHistoryLabel(String label, List<HistoryItem> historyList, UserFieldDeclarationDesc userFieldDeclarationDesc) {
        super(historyList, userFieldDeclarationDesc);
        setText(label);
        setTitle(label);
    }
}
