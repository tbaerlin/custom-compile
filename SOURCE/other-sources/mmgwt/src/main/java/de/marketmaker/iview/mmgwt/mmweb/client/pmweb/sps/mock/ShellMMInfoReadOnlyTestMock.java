/*
 * ShellMMInfoReadOnlyTestMock.java
 *
 * Created on 08.05.2014 10:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addReadOnlyWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addToParent;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;

/**
 * Replaces any task with decls and data to test the SpsReadonly widget specializations for ShellMMInfo types.
 * @author Markus Dick
 */
@NonNLS
public class ShellMMInfoReadOnlyTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        final EditWidgetDesc shellEdit = new EditWidgetDesc();
        shellEdit.setCaption("Instrument");
        shellEdit.setBind("/instrument");
        addToParent(shellEdit, s);

        addReadOnlyWidget(s, "/instrument", "Description", "description");
        addReadOnlyWidget(s, "/instrument", "Type", "type");
        addReadOnlyWidget(s, "/instrument", "WKN", "number");
        addReadOnlyWidget(s, "/instrument", "ISIN", "isin");
        addReadOnlyWidget(s, "/instrument", "ISIN / WKN", "isin-number");

        final EditWidgetDesc folderEdit = new EditWidgetDesc();
        folderEdit.setCaption("Folder");
        folderEdit.setBind("/folder");
        addToParent(folderEdit, s);
        addReadOnlyWidget(s, "/folder", "Description", "description");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final List<DataContainerNode> c = ((DataContainerCompositeNode) dcn).getChildren();
        c.clear();

        if(decl) {
            addShellDecl(c);
            addFolderDecl(c);
        }
        else {
            addShellData(c);
            addFolderData(c);
        }
    }

    private void addShellDecl(List<DataContainerNode> c) {
        final ParsedTypeInfo shellPti = new ParsedTypeInfo();
        shellPti.setTypeId(TiType.TI_SHELL_MM);
        shellPti.setDisplayName("Instrument");
        shellPti.getFolderTypes().addAll(ShellMMTypeUtil.getSecurityTypes());

        final DataContainerLeafNodeDeclaration shellDecl = new DataContainerLeafNodeDeclaration();
        shellDecl.setNodeLevelName("instrument");
        shellDecl.setDescription(shellPti);

        c.add(shellDecl);
    }

    private void addShellData(List<DataContainerNode> c) {
        final ShellMMInfo shell = new ShellMMInfo();
        shell.setBezeichnung("dreiundzwanzig");
        shell.setNumber("23");
        shell.setISIN("42");
        shell.setTyp(ShellMMType.ST_CERTIFICATE);

        final DataContainerLeafNodeDataItem shellLeaf = new DataContainerLeafNodeDataItem();
        shellLeaf.setNodeLevelName("instrument");
        shellLeaf.setDataItem(shell);
        c.add(shellLeaf);
    }

    private void addFolderDecl(List<DataContainerNode> c) {
        final ParsedTypeInfo folderPti = new ParsedTypeInfo();
        folderPti.setTypeId(TiType.TI_FOLDER);
        folderPti.setDisplayName("Folder");
        folderPti.getFolderTypes().addAll(ShellMMTypeUtil.getDepotObjectTypes());

        final DataContainerLeafNodeDeclaration folderDecl = new DataContainerLeafNodeDeclaration();
        folderDecl.setNodeLevelName("folder");
        folderDecl.setDescription(folderPti);

        c.add(folderDecl);
    }

    private void addFolderData(List<DataContainerNode> c) {
        final DataContainerLeafNodeDataItem folderLeaf = new DataContainerLeafNodeDataItem();
        folderLeaf.setNodeLevelName("folder");
        folderLeaf.setDataItem(new DefaultMM());

        c.add(folderLeaf);
    }
}
