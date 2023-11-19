/*
 * MultiAnlageplanungslisteMock.java
 *
 * Created on 17.06.2015 14:41
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
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * @author mdick
 */
@NonNLS
public class MultiAnlageplanungslisteMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc root = MockUtil.prepareRootSectionWidget(wd);
        root.setStyle("sps-dummy");
        root.setDescription("Halten Sie hier die beabsichtigten Transaktionen zu den einzelnen Finanzinstrumenten fest.");
        addBaustein(root, "apl1", true);
        addBaustein(root, "apl2", false);
    }

    private void addBaustein(SectionDesc root, final String aplBindName, boolean odd) {
        final SectionDesc widgetDesc = odd
                ? addSectionWidget(root, "", "backgroundColor-eee")
                : addSectionWidget(root, "");

        final SectionDesc sect1 = bind("/MAPL_Anlageplanungsliste/" + aplBindName, widgetDesc);

        final SectionDesc header1 = addSectionWidget(sect1, "", "sps-form-6", "sps-plainForm", "paddingBottom-0", "paddingTop-5");
//alternative
        addLabelWidget(header1, "Test HA (pmweb)Test HA (pmweb)Test HA (pmweb)Test HA (pmweb)", "", "Bankberatung", "width-160", "paddingLeft-0", "marginRight-5", "sps-nowrap", "sps-caption-emphasize");
        //Parameter LabelWidgetDesc: parent, text, bind, caption, styles...
// original
//        readonly(addLabelWidget(header1, "Bankberatung"));
//        readonly(addLabelWidget(header1, "Test HA (pmweb)"));
        readonly(addEditWidget(header1, "VermoegenSollwert", "Sollwert Vermögen", "marginRight-5"));
        readonly(addEditWidget(header1, "VermoegenIstwert", "Istwert Vermögen", "marginRight-5"));
        readonly(addEditWidget(header1, "LiquiditaetIstwert", "Istwert Liquidität", "marginRight-5"));
        final LabelWidgetDesc gP = readonly(addLabelWidget(header1, "hallo", "GeePErgebnisBeschreibung", "Geeignetheitsprüfung", "marginRight-5"));
        gP.setIconNameBind("GeepErfolgreichIconName");

        final SectionDesc editSection = addSectionWidget(null, "Eintrag $#");
        editSection.setId(aplBindName + "-editSection");
        addEditWidget(editSection, "TransactionType", "Transaktionstyp", "combo", "width-140");
        final SectionDesc trailing = addSectionWidget(editSection, "", "trailing");
        readonly(addEditWidget(trailing, "Security", "Wertpapier", "ORDER_ENTRY"));
        final EditWidgetDesc pickResearchSymbol = addEditWidget(trailing, "Security", null, "pickResearchSymbol");
        pickResearchSymbol.setLayoutGUID("A99F9E9B56994CF8B143A10D4886B794");
        readonly(addEditWidget(editSection, "Security", "WKN", "number", "width-70"));
        addEditWidget(editSection, "Quantity", "Nennwert");
        readonly(addEditWidget(editSection, "Betrag", "Betrag (EUR)"));
        final LabelWidgetDesc angP = readonly(addLabelWidget(editSection, "", "AngpErgebnisBeschreibung", "Ang."));
        angP.setIconNameBind("AngpErfolgreichIconname");
        readonly(addEditWidgetWithObject(editSection, "Security", "TransactionType", "PIB/KIID", "pibKiidAvailabilityIcon"));

        addSectionListWidget(sect1, "Entries", editSection, "", "tableLayout", "sps-collapsible", "paddingTop-2", "paddingBottom-5");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode p = prepareRootNode(dcn);
        final DataContainerGroupNode mapl = addGroup(p, "MAPL_Anlageplanungsliste");
        addBausteinData(decl, mapl, "apl1", BASF11);
        addBausteinData(decl, mapl, "apl2", LED400);
    }

    private void addBausteinData(boolean decl, DataContainerGroupNode mapl, String aplName, ShellMMInfo security) {
        final DataContainerGroupNode apl = addGroup(mapl, aplName);
        addDecl(decl, apl, "PortfolioName", pti(TiType.TI_STRING), mmString("Test HA (pmweb) Test HA (pmweb) Test HA (pmweb) Test HA (pmweb)"));
        addDecl(decl, apl, "VermoegenSollwert", pti(TiType.TI_NUMBER), mmNumber(203527.5917));
        addDecl(decl, apl, "VermoegenIstwert", pti(TiType.TI_NUMBER),  mmNumber(203527.5917));
        addDecl(decl, apl, "LiquiditaetIstwert", pti(TiType.TI_NUMBER),  mmNumber(203527.5917));
        addDecl(decl, apl, "GeePErgebnisBeschreibung", pti(TiType.TI_STRING), new DefaultMM() /*mmString("Prüfung nicht möglich. Risikoklasse auf Kunden- oder Wertpapierebene nicht vollständig.")*/ );
        addDecl(decl, apl, "GeepErfolgreichIconName", pti(TiType.TI_STRING), mmString("PmIcon:CheckNotOk"));
        final DataContainerListNode entries = addList(apl, "Entries");
        final DataContainerGroupNode entryNode = addGroupToList(decl, entries);

        addDecl(decl, entryNode, "TransactionType", mandatory(pti("n/a", e("n/a", "n/a"), e("Verkaufen", "Verkaufen"), e("Kaufen", "Kaufen"), e("Halten", "Halten"))), e("Kaufen", "Kaufen"));
        addDecl(decl, entryNode, "Security", pti(TiType.TI_SHELL_MM), security);
        addDecl(decl, entryNode, "Quantity", mandatory(pti(TiType.TI_NUMBER)), mmNumber(1000));
        addDecl(decl, entryNode, "Betrag", pti(TiType.TI_NUMBER), mmNumber(81000));
        addDecl(decl, entryNode, "AngpErgebnisBeschreibung", pti(TiType.TI_STRING), new DefaultMM());
        addDecl(decl, entryNode, "AngpErfolgreichIconname", pti(TiType.TI_STRING), mmString("PmIcon:CheckUndetermined"));
    }
}
