/*
 * SpsListDataTableTestMock.java
 *
 * Created on 15.07.2015 09:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListDataTable;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;
import static de.marketmaker.iview.pmxml.TiType.TI_NUMBER;
import static de.marketmaker.iview.pmxml.TiType.TI_SHELL_MM;
import static de.marketmaker.iview.pmxml.TiType.TI_STRING;

/**
 * Contains mocks of all SpsListDataTable view variants.
 * @see SpsListDataTable#onItemsChange()
 * @author mdick
 */
@NonNLS
public class SpsListDataTableTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc root = prepareRootSectionWidget(wd);

        //normal ones
        final ListWidgetDesc col0Null = addListWidget(root, "dummy", "col0Null", null, "col0Null", "table");
        addListWidgetColumn(col0Null, "col0", "Null");
        addListWidgetColumn(col0Null, "col1", "Something");

        final ListWidgetDesc col0String = addListWidget(root, "dummy", "col0String", null, "col0String", "table");
        addListWidgetColumn(col0String, "col0", "String");
        addListWidgetColumn(col0String, "col1", "Something");

        final ListWidgetDesc col0Shell = addListWidget(root, "dummy", "col0Shell", null, "col0Shell", "table");
        addListWidgetColumn(col0Shell, "col0", "Shell");
        addListWidgetColumn(col0Shell, "col1", "Something");

        final ListWidgetDesc col0Number = addListWidget(root, "dummy", "col0Number", null, "col0Number", "table");
        addListWidgetColumn(col0Number, "col0", "Number miniBar", "miniBar");
        addListWidgetColumn(col0Number, "col1", "Something");

        // entryColor styled ones
        final ListWidgetDesc col0NullEntryColor = addListWidget(root, "dummy", "col0Null", null, "col0Null", "table", "entryColor");
        addListWidgetColumn(col0NullEntryColor, "col0", "Null");
        addListWidgetColumn(col0NullEntryColor, "col1", "Something");

        final ListWidgetDesc col0StringEntryColor = addListWidget(root, "dummy", "col0String", null, "col0String", "table", "entryColor");
        addListWidgetColumn(col0StringEntryColor, "col0", "String");
        addListWidgetColumn(col0StringEntryColor, "col1", "Something");

        final ListWidgetDesc col0ShellEntryColor = addListWidget(root, "dummy", "col0Shell", null, "col0Shell", "table", "entryColor");
        addListWidgetColumn(col0ShellEntryColor, "col0", "Shell");
        addListWidgetColumn(col0ShellEntryColor, "col1", "Something");

        //note: entryColor and miniBar cannot be combined there should be developer notification about the misconfiguration
        final ListWidgetDesc col0NumberEntryColor = addListWidget(root, "dummy", "col0Number", null, "col0Number", "table", "entryColor");
        addListWidgetColumn(col0NumberEntryColor, "col0", "Number miniBar", "miniBar");
        addListWidgetColumn(col0NumberEntryColor, "col1", "Something");

        final ListWidgetDesc col0ShellNoLink = addListWidget(root, "dummy", "col0ShellNoLink", null, "col0Shell", "table");
        addListWidgetColumn(col0ShellNoLink, "col0", "Iff MainInput then no Link");
        addListWidgetColumn(col0ShellNoLink, "col1", "Something");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = prepareRootNode(dcn);
        addCol0ShellMMInfoList(decl, parent, "col0Shell");
        addCol0NullList(decl, parent, "col0Null");
        addCol0StringList(decl, parent, "col0String");
        addCol0NumberList(decl, parent, "col0Number");
        addCol0ShellMMInfoListNoLink(decl, parent, "col0ShellNoLink");
        addDecl(decl, parent, "dummy", pti(TI_NUMBER), new DefaultMM());
    }

    private void addCol0ShellMMInfoList(boolean decl, DataContainerCompositeNode parent, String bindKey) {
        final DataContainerListNode listNode = addList(parent, bindKey);
        addEntryWithShellMMInfo(decl, addGroupToList(decl, listNode), BASF11, "a string row 0");
        addEntryWithShellMMInfo(decl, addGroupToList(decl, listNode), LED400, "a string row 1");
    }

    private void addEntryWithShellMMInfo(boolean decl, DataContainerGroupNode entry, ShellMMInfo shellMMInfo, String aString) {
        addDecl(decl, entry, "col0", pti(TI_SHELL_MM), shellMMInfo);
        addDecl(decl, entry, "col1", pti(TI_STRING), mmString(aString));
    }

    private void addCol0ShellMMInfoListNoLink(boolean decl, DataContainerCompositeNode parent, String bindKey) {
        final DataContainerListNode listNode = addList(parent, bindKey);
        addEntryWithShellMMInfoNoLink(decl, addGroupToList(decl, listNode), MockUtil.INVESTOR_TEST_HA, "iff main object this investor then no link");
        addEntryWithShellMMInfoNoLink(decl, addGroupToList(decl, listNode), MockUtil.PORTFOLIO_TEST_HA, "iff main object this portfolio then no link");
    }

    private void addEntryWithShellMMInfoNoLink(boolean decl, DataContainerGroupNode entry, ShellMMInfo shellMMInfo, String aString) {
        addDecl(decl, entry, "col0", pti(TI_SHELL_MM), shellMMInfo);
        addDecl(decl, entry, "col1", pti(TI_STRING), mmString(aString));
    }

    private void addCol0NullList(boolean decl, DataContainerCompositeNode parent, String bindKey) {
        final DataContainerListNode listNode = addList(parent, bindKey);
        addEntryWithNull(decl, addGroupToList(decl, listNode), "a string row 0");
        addEntryWithNull(decl, addGroupToList(decl, listNode), "a string row 1");
    }

    private void addEntryWithNull(boolean decl, DataContainerGroupNode entry, String aString) {
        addDecl(decl, entry, "col0", pti(TI_NUMBER), new DefaultMM());
        addDecl(decl, entry, "col1", pti(TI_STRING), mmString(aString));
    }

    private void addCol0StringList(boolean decl, DataContainerCompositeNode parent, String bindKey) {
        final DataContainerListNode listNode = addList(parent, bindKey);
        addEntryWithString(decl, addGroupToList(decl, listNode), "a string col 0 row 0", "a string col 1 row 0");
        addEntryWithString(decl, addGroupToList(decl, listNode), "a string col 0 row 1", "a string col 1 row 1");
    }

    private void addEntryWithString(boolean decl, DataContainerGroupNode entry, String col0, String col1) {
        addDecl(decl, entry, "col0", pti(TI_STRING), mmString(col0));
        addDecl(decl, entry, "col1", pti(TI_STRING), mmString(col1));
    }

    private void addCol0NumberList(boolean decl, DataContainerCompositeNode parent, String bindKey) {
        final DataContainerListNode listNode = addList(parent, bindKey);
        addEntryWithNumber(decl, addGroupToList(decl, listNode), 23.42, "a string col 1 row 0");
        addEntryWithNumber(decl, addGroupToList(decl, listNode), 42.23, "a string col 1 row 1");
    }

    private void addEntryWithNumber(boolean decl, DataContainerGroupNode entry, double number, String col1) {
        addDecl(decl, entry, "col0", pti(TI_NUMBER, null, false, false, "0", "100", "100", "10", 0, ""), mmNumber(number));
        addDecl(decl, entry, "col1", pti(TI_STRING), mmString(col1));
    }
}
