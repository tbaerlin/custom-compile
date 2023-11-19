/*
 * ShellMMInfoMock.java
 *
 * Created on 15.05.2014 11:17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * Replaces any task with decls and data to test the SpsShellMMInfoPicker widget.
 *
 * @author Markus Dick
 */
@NonNLS
public class ShellMMInfoTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = (SectionDesc) wd;
        s.setBind(null);
        final List<WidgetDesc> i = s.getItems();
        i.clear();

        addWidget(i, "WPReferenz", null, "Symbol");
        addWidget(i, "WPReferenzOE", "ORDER_ENTRY", "Symbol (OE)");
        addWidget(i, "FolderReferenz", null, "Folder");
        addWidget(i, "DepotObjektReferenz", "DEPOT_OBJECT", "Depot-Objekt");
    }

    private void addWidget(List<WidgetDesc> i, String bind, String style, String caption) {
        final EditWidgetDesc w = new EditWidgetDesc();
        w.setIsReadonly(false);
        w.setBind(bind);
        w.setCaption(caption);
        if(style != null) {
            w.setStyle(style);
        }
        i.add(w);
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final List<DataContainerNode> c = ((DataContainerCompositeNode) dcn).getChildren();
        c.clear();

        final ParsedTypeInfo securityType = createParsedTypeInfo(TiType.TI_SHELL_MM, ShellMMType.ST_AKTIE, ShellMMType.ST_ANLEIHE, ShellMMType.ST_FOND, ShellMMType.ST_CERTIFICATE);
        final ParsedTypeInfo folderType = createParsedTypeInfo(TiType.TI_FOLDER, ShellMMType.ST_ORDNER, ShellMMType.ST_GRUPPE, ShellMMType.ST_FILTER, ShellMMType.ST_LIMIT_ORDNER, ShellMMType.ST_FORMEL_SIGNAL_ORDNER, ShellMMType.ST_FILTER_RESULT_FOLDER);
        final ParsedTypeInfo depotObjectType = createParsedTypeInfo(TiType.TI_FOLDER, ShellMMType.ST_INHABER, ShellMMType.ST_PORTFOLIO, ShellMMType.ST_DEPOT, ShellMMType.ST_KONTO);

        addLeaf(decl, c, securityType, "WPReferenz");
        addLeaf(decl, c, securityType, "WPReferenzOE");
        addLeaf(decl, c, folderType, "FolderReferenz");
        addLeaf(decl, c, depotObjectType, "DepotObjektReferenz");
    }

    protected ParsedTypeInfo createParsedTypeInfo(TiType tiType, ShellMMType... folderTypes) {
        final ParsedTypeInfo p = new ParsedTypeInfo();
        p.setTypeId(tiType);
        for (ShellMMType type : folderTypes) {
            p.getFolderTypes().add(type);
        }
        return p;
    }

    protected void addLeaf(boolean decl, List<DataContainerNode> c, ParsedTypeInfo p, String nodeLevelName) {
        if(decl) {
            final DataContainerLeafNodeDeclaration d = new DataContainerLeafNodeDeclaration();
            d.setNodeLevelName(nodeLevelName);
            d.setDescription(p);

            c.add(d);
        }
        else {
            final DataContainerLeafNodeDataItem d = new DataContainerLeafNodeDataItem();
            d.setNodeLevelName(nodeLevelName);
            d.setDataItem(new DefaultMM());
            c.add(d);
        }
    }
}
