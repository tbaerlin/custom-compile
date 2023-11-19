/*
 * SpsListSingleTablePickerTestMock.java
 *
 * Created on 22.05.2015 11:44
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SelectionMode;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;
import static de.marketmaker.iview.pmxml.TiType.*;

/**
 * @author mdick
 */
@NonNLS
public class SpsListSingleTablePickerTestMock extends PreProcessHook {

    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "instrument", "Instrument");
        final ListWidgetDesc list = addListWidget(s, "instrument", "holdings", "shell", SelectionMode.SM_SINGLE_SELECT, "Holdings", "tablePicker", "PmIcon:Portfolio16");
        addListWidgetColumn(list, "label", "Bezeichnung");
        addListWidgetColumn(list, "isin", "ISIN");
        addListWidgetColumn(list, "nominal", "Nominal");
        addListWidgetColumn(list, "amount", "Betrag");

        final ListWidgetDesc listNoColumnNames = addListWidget(s, "instrument", "holdings", "shell", SelectionMode.SM_SINGLE_SELECT, "Holdings no column names (null)", "tablePicker", "PmIcon:Portfolio16");
        addListWidgetColumn(listNoColumnNames, "label", null);
        addListWidgetColumn(listNoColumnNames, "isin", null);
        addListWidgetColumn(listNoColumnNames, "nominal", null);
        addListWidgetColumn(listNoColumnNames, "amount", null);

        final ListWidgetDesc listNoColumnNamesWhitespace = addListWidget(s, "instrument", "holdings", "shell", SelectionMode.SM_SINGLE_SELECT, "Holdings no column names (whitespaces)", "tablePicker", "PmIcon:Portfolio16");
        addListWidgetColumn(listNoColumnNamesWhitespace, "label", "   ");
        addListWidgetColumn(listNoColumnNamesWhitespace, "isin", "\t");
        addListWidgetColumn(listNoColumnNamesWhitespace, "nominal", "\n");
        addListWidgetColumn(listNoColumnNamesWhitespace, "amount", " \r\n\n\r\t  ");

        final ListWidgetDesc listCustomColumnStyle = addListWidget(s, "instrument", "holdings", "shell", SelectionMode.SM_SINGLE_SELECT, "Holdings custom column style", "tablePicker", "PmIcon:Portfolio16");
        addListWidgetColumn(listCustomColumnStyle, "label", "Bezeichnung", "mm-right");
        addListWidgetColumn(listCustomColumnStyle, "isin", "ISIN", "mm-right");
        addListWidgetColumn(listCustomColumnStyle, "nominal", "Nominal", "mm-left");
        addListWidgetColumn(listCustomColumnStyle, "amount", "Betrag", "mm-center");

        final ListWidgetDesc listReadonly = addListWidget(s, "instrument", "holdings", "shell", SelectionMode.SM_SINGLE_SELECT, "Holdings", "tablePicker", "PmIcon:Portfolio16");
        listReadonly.setIsReadonly(true);
        addListWidgetColumn(listReadonly, "label", "Bezeichnung");
        addListWidgetColumn(listReadonly, "isin", "ISIN");
        addListWidgetColumn(listReadonly, "nominal", "Nominal");
        addListWidgetColumn(listReadonly, "amount", "Betrag");

        final ListWidgetDesc listComp = addListWidget(s, "instrument", "holdings", "shell", SelectionMode.SM_SINGLE_SELECT, "Holdings");
        addListWidgetColumn(listComp, "label", "Bezeichnung");
        addListWidgetColumn(listComp, "isin", "ISIN");
        addListWidgetColumn(listComp, "nominal", "Nominal");
        addListWidgetColumn(listComp, "amount", "Betrag");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode p = (DataContainerCompositeNode) dcn;
        final List<DataContainerNode> c = p.getChildren();
        c.clear();

        addDecl(decl, p, "instrument", mandatory(pti(TI_SHELL_MM)), new DefaultMM());

        final DataContainerListNode holdings = addList(p, "holdings");

        double nominal = 1000000;
        double price = 42;
        double amount1 = nominal * price++;
        addTableEntry(decl, addGroupToList(decl, holdings), BASF11, nominal++, amount1);
        addTableEntry(decl, addGroupToList(decl, holdings), BAY001, nominal++/100, price++);  //make resulting strings shorter for testing the custom cell styling
        final double amount2 = nominal * price;
        addTableEntry(decl, addGroupToList(decl, holdings), LED400, nominal, amount2);
    }

    private void addTableEntry(boolean decl, DataContainerGroupNode groupNode, ShellMMInfo shellMMInfo, double nominal, double amount) {
        if(groupNode == null) {
            return;
        }

        addDecl(decl, groupNode, "shell", pti(TI_SHELL_MM), shellMMInfo);
        addDecl(decl, groupNode, "label", pti(TI_STRING), mmString(shellMMInfo.getBezeichnung()));
        addDecl(decl, groupNode, "isin", pti(TI_STRING), mmString(shellMMInfo.getISIN()));
        addDecl(decl, groupNode, "nominal", pti(false, "100"), mmNumber(nominal));
        addDecl(decl, groupNode, "amount", pti(false, "100"), mmNumber(amount));
    }
}
