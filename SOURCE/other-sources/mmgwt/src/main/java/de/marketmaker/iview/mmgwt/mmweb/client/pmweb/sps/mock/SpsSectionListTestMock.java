/*
 * SpsSectionListTestMock.java
 *
 * Created on 28.07.2014 11:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionListDesc;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.SortMode;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Arrays;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;
import static de.marketmaker.iview.pmxml.TiType.*;

/**
 * Contains mocks of all SpsSectionList view variants.
 * @author mdick
 */
@NonNLS
public class SpsSectionListTestMock extends PreProcessHook {
    private static final String DEPOT_1 = "20473717";
    private static final String DEPOT_2 = "21159905";

    protected SectionListDesc listSection = null;

    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc root = prepareRootSectionWidget(wd);

        //SectionList table layout list based without footer
        addSectionListWidget(root, "/listSectionT", createEntrySection(), "table layout list based without footer", "tableLayout");

        //SectionList table layout list based with footer
        final SectionListDesc tableLayoutListBasedWithFooter = addSectionListWidget(root, "/listSectionTF", createEntrySection(), "table layout list based with footer", "tableLayout");
        final SectionDesc tableLayoutListBasedWithFooterFooter = addSectionWidget(null, "Footer Section");
        tableLayoutListBasedWithFooter.setFooterSection(tableLayoutListBasedWithFooterFooter);
        addLabelWidget(tableLayoutListBasedWithFooterFooter, "Total");
        addReadOnlyWidget(tableLayoutListBasedWithFooterFooter, "/total", "Total");

        //SectionList table layout group based without footer
        addGroupOrder(addSectionListWidget(root, "/groupSectionT", createEntrySection(), "table layout group based without footer", "tableLayout"));

        //SectionList table layout group based with footer
        final SectionListDesc tableLayoutGroupBasedWithFooter = addGroupOrder(addSectionListWidget(root, "/groupSectionTF", createEntrySection(), "table layout group based with footer", "tableLayout"));
        final SectionDesc tableLayoutGroupBasedWithFooterFooter = addSectionWidget(null, "Footer Section Table");
        tableLayoutGroupBasedWithFooter.setFooterSection(tableLayoutGroupBasedWithFooterFooter);
        addLabelWidget(tableLayoutGroupBasedWithFooterFooter, "Total");
        addReadOnlyWidget(tableLayoutGroupBasedWithFooterFooter, "/total", "Total");

        //SectionList panel layout list based without footer
        this.listSection = addSectionListWidget(root, "/listSectionP", createEntrySection(), "panel layout list based without footer");
        this.listSection.setTooltipAdd("The add tool tip");
        this.listSection.setTooltipDelete("The delete tool tip");

        //SectionList panel layout list based with footer
        final SectionListDesc panelLayoutListBased = addSectionListWidget(root, "/listSectionPF", createEntrySection(), "panel layout list based with footer");
        panelLayoutListBased.setTooltipAdd("The add tool tip");
        panelLayoutListBased.setTooltipDelete("The delete tool tip");

        final SectionDesc panelLayoutListBasedFooter = addSectionWidget(null, "Footer Section Panel");
        panelLayoutListBased.setFooterSection(panelLayoutListBasedFooter);
        addReadOnlyWidget(panelLayoutListBasedFooter, "/total", "Total");

        //SectionList panel layout group based without footer
        addGroupOrder(addSectionListWidget(root, "/groupSectionP", createEntrySection(), "panel layout group based without footer"));

        //SectionList panel layout group based with footer
        final SectionListDesc panelLayoutGroupBasedWithFooter = addGroupOrder(addSectionListWidget(root, "/groupSectionPF", createEntrySection(), "panel layout group based with footer"));
        final SectionDesc panelLayoutGroupBasedFooter = addSectionWidget(null, "Footer Section Panel");
        panelLayoutGroupBasedWithFooter.setFooterSection(panelLayoutGroupBasedFooter);
        addReadOnlyWidget(panelLayoutGroupBasedFooter, "/total", "Total");
    }

    private SectionListDesc addGroupOrder(SectionListDesc sectionListDesc) {
        sectionListDesc.getGroupOrder().addAll(Arrays.asList(mmString("groupEntry3"), mmString("groupEntry2"), mmString("groupEntry1")));
        return sectionListDesc;
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = prepareRootNode(dcn);
        addListWithEntries(decl, parent, "listSectionT");
        addListWithEntries(decl, parent, "listSectionTF");
        addGroupWithEntries(decl, parent, "groupSectionT");
        addGroupWithEntries(decl, parent, "groupSectionTF");
        addListWithEntries(decl, parent, "listSectionP");
        addListWithEntries(decl, parent, "listSectionPF");
        addGroupWithEntries(decl, parent, "groupSectionP");
        addGroupWithEntries(decl, parent, "groupSectionPF");

        addDecl(decl, parent, "total", pti(TI_NUMBER), mmNumber(42.23));
        addDepotList(decl, parent);
        addTransactionTypesForDepots(decl, parent);
    }

    private SectionDesc createEntrySection() {
        final SectionDesc entrySection = addSectionWidget(null, "$#. List Entry");
        addReadOnlyWidget(entrySection, "entryId", "Entry ID");

        final ListWidgetDesc listWidgetDepotId = addListWidget(entrySection, "depotId", "/depotList", "key", "Depot");
        addListWidgetColumn(listWidgetDepotId, "value", "0", SortMode.SM_NONE, null);

        final ListWidgetDesc listWidgetTransactionType = addListWidget(entrySection, "transactionType", "/transactionTypes/{depotId}", "key", "Transaction");
        addListWidgetColumn(listWidgetTransactionType, "value", "0", SortMode.SM_NONE, null);

        addEditWidgetWithObject(entrySection, "instrument", "depotId", "Instrument", "pickDepotSymbol");
        addReadOnlyWidget(entrySection, "transactionType", "Transaction", "description");
        addReadOnlyWidget(entrySection, "instrument", "Description", "description");
        addReadOnlyWidget(entrySection, "instrument", "Type", "type");
        addReadOnlyWidget(entrySection, "instrument", "ISIN/WKN", "isin-number");
        return entrySection;
    }

    private void addTransactionTypesForDepots(boolean decl, DataContainerCompositeNode parent) {
        final DataContainerGroupNode group = addGroup(parent, "transactionTypes");

        final DataContainerListNode list1 = addList(group, DEPOT_1);
        addTransactionTypeEntry(decl, list1, "list11", "Buy Depot 1");
        addTransactionTypeEntry(decl, list1, "list12", "Sell Depot 1");
        addTransactionTypeEntry(decl, list1, "list13", "Subscribe Depot 1");

        final DataContainerListNode list2 = addList(group, DEPOT_2);
        addTransactionTypeEntry(decl, list2, "list21", "Buy Depot 2");
        addTransactionTypeEntry(decl, list2, "list22", "Sell Depot 2");
    }

    private void addTransactionTypeEntry(boolean decl, DataContainerListNode list, String key, String value) {
        final DataContainerGroupNode entry = addGroupToList(decl, list);
        addDecl(decl, entry, "key", pti(TI_STRING), mmString(key));
        addDecl(decl, entry, "value", pti(TI_STRING), mmString(value));
    }

    private void addListWithEntries(boolean decl, DataContainerCompositeNode parent, String bindKey) {
        final DataContainerListNode listNode = addList(parent, bindKey);
        for(int i = 0; i < 3; i++) {
            final DataContainerGroupNode entry = addGroupToList(decl, listNode);
            addEntry(decl, entry, i);
        }
    }

    private void addGroupWithEntries(boolean decl, DataContainerCompositeNode parent, String bindKey) {
        final DataContainerGroupNode groupNode = addGroup(parent, bindKey);
        for(int i = 0; i < 3; i++) {
            final DataContainerGroupNode entry = addGroup(groupNode, "groupEntry" + (i + 1));
            addEntry(decl, entry, i + 1);
        }
    }

    private void addEntry(boolean decl, DataContainerGroupNode entry, int entryId) {
        addDecl(decl, entry, "entryId", pti(TI_NUMBER), mmNumber(entryId));
        addDecl(decl, entry, "depotId", pti(TI_NUMBER), mmNumber(DEPOT_1));
        addDecl(decl, entry, "transactionType", pti(TI_STRING), new DefaultMM());
        final Set<ShellMMType> securityTypes = ShellMMTypeUtil.getSecurityTypes();
        addShellMMInfo(decl, "instrument", TI_SHELL_MM, securityTypes.toArray(new ShellMMType[securityTypes.size()]), entry, new DefaultMM());
    }

    private void addDepotList(boolean decl, DataContainerCompositeNode parent) {
        final DataContainerListNode depotList = addList(parent, "depotList");
        addDepotListEntry(decl, depotList, DEPOT_1, "Test HA");
        addDepotListEntry(decl, depotList, DEPOT_2, "Test HA Fuchsbriefe");
    }

    private void addDepotListEntry(boolean decl, DataContainerListNode parent, String depotId, String value) {
        final DataContainerGroupNode entry = addGroupToList(decl, parent);
        addDecl(decl, entry, "key", pti(TI_NUMBER), mmNumber(depotId));
        addDecl(decl, entry, "value", pti(TI_STRING), mmString(value));
    }
}
