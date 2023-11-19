/*
 * SpsOrderEntryWidgetTestMock.java
 *
 * Created on 08.05.2014 11:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SelectionMode;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * Replaces any task with decls and data to test the SpsOrderEntryWidget.
 *
 * @author Markus Dick
 */
@NonNLS
public class SpsOrderEntryWidgetTestMock extends PreProcessHook {
    public static final String PERSON_CUSTOMER_NUMBER_KORDOBA_KGS_TEST_SYSTEM = "1202006"; //"Gräfin Egon HB-Berechtig"
    public static final String PERSON_CUSTOMER_NUMBER_PMS_QA_MOCK = "0000183385"; //"Bernath,Benjamin"

    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        final EditWidgetDesc edit = new EditWidgetDesc();
        edit.setCaption("Order Entry");
        edit.setBind("/order1");
        edit.setStyle("orderEntryButton");
        addToParent(edit, s);

        addEditWidget(s, "/order1/ListEntryID", "ID des Anlageplanungslisteneintrags");
        final ListWidgetDesc persons = addListWidget(s, "/order1/PersonCustomerNumber", "/persons", "number", SelectionMode.SM_SINGLE_SELECT, "Gesprächspartner/Auftraggeber Kundennummer");
        addListWidgetColumn(persons, "number", "Kundennummer");
        addListWidgetColumn(persons, "name", "Gesprächspartner");
        addEditWidget(s, "/order1/SecurityAccount", "Depot");
        addEditWidget(s, "/order1/SecurityAccountID", "Depot-ID used if Depot is empty");
        addEditWidget(s, "/order1/TransactionType", "Transaktion");
        addEditWidget(s, "/order1/Security", "Instrument", "ORDER_ENTRY");
        addReadOnlyWidget(s, "/order1/Security", "Description", "description");
        addReadOnlyWidget(s, "/order1/Security", "Type", "type");
        addReadOnlyWidget(s, "/order1/Security", "WKN", "number");
        addReadOnlyWidget(s, "/order1/Security", "ISIN", "isin");

        addEditWidget(s, "/order1/SettlementAccount", "Abrechnungskonto");
        addEditWidget(s, "/order1/SettlementAccountID", "Abrechnungskonto-ID used if Abrechnungskonto is empty");
        addEditWidget(s, "/order1/Quantity", "Stück/Nominal");
        addEditWidget(s, "/order1/Limit", "Limit");
        addEditWidget(s, "/order1/LimitCurrency", "Limitwährung");
        addEditWidget(s, "/order1/CommunicationChannel", "Kommunikationsweg/Auftragserteilung über (BhL K-GS)");
        addEditWidget(s, "/order1/BusinessSegment", "Geschäftssegment (BhL K-GS)");
        addEditWidget(s, "/order1/Comment", "Freitext");
        addEditWidget(s, "/order1/Enabled", "Enabled");
        addEditWidget(s, "/order1/ProcessedQuantity", "ProcessedQuantity");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode root = prepareRootNode(dcn);

        final DataContainerListNode persons = addList(root, "persons");
        addPerson(decl, persons, PERSON_CUSTOMER_NUMBER_PMS_QA_MOCK, "Benjamin,Bernath");
        addPerson(decl, persons, PERSON_CUSTOMER_NUMBER_KORDOBA_KGS_TEST_SYSTEM, "Gräfin Egon HB-Berechtig");

        addOrder(decl, addOrderButtonGroup(root.getChildren(), "order1"));
    }

    public DataContainerGroupNode addPerson(boolean decl, DataContainerListNode persons,
            String number, String name) {
        final DataContainerGroupNode person1 = addGroupToList(decl, persons);
        addDecl(decl, person1, "number", pti(TiType.TI_STRING), mmString(number));
        addDecl(decl, person1, "name", pti(TiType.TI_STRING), mmString(name));
        return person1;
    }

    private DataContainerGroupNode addOrderButtonGroup(List<DataContainerNode> c, String nodeLevelName) {
        final DataContainerGroupNode order = new DataContainerGroupNode();
        order.setNodeLevelName(nodeLevelName);
        c.add(order);
        return order;
    }

    private void addOrder(boolean decl, DataContainerGroupNode order) {
        addDecl(decl, order, "ListEntryID", TiType.TI_STRING);
        addDecl(decl, order, "PersonCustomerNumber", pti(TiType.TI_STRING), mmString(PERSON_CUSTOMER_NUMBER_PMS_QA_MOCK));
        addShellMMInfo(decl, "SecurityAccount", TiType.TI_FOLDER, new ShellMMType[]{ShellMMType.ST_DEPOT}, order);
        addDecl(decl, order, "SecurityAccountID", TiType.TI_NUMBER);
        addEnum(decl, order, "TransactionType", e("Kaufen", "Kaufen"), e("Verkaufen", "Verkaufen"), e("Zeichnen", "Zeichnen"), e("Halten", "Halten"), e("Nicht kaufen", "Nicht kaufen"));
        addShellMMInfo(decl, "Security", TiType.TI_SHELL_MM, new ShellMMType[]{ShellMMType.ST_AKTIE, ShellMMType.ST_FOND, ShellMMType.ST_ANLEIHE}, order);
        addShellMMInfo(decl, "SettlementAccount", TiType.TI_FOLDER, new ShellMMType[]{ShellMMType.ST_KONTO}, order);
        addDecl(decl, order, "SettlementAccountID", TiType.TI_NUMBER);
        addDecl(decl, order, "Quantity", TiType.TI_NUMBER);
        addDecl(decl, order, "Limit", TiType.TI_NUMBER);
        addEnum(decl, order, "LimitCurrency", e("US Amerikanischer Euro", "USD"), e("Europäischer Euro", "EUR"), e("Australischer Euro", "AUD"));
//        addEnum(decl, order, "CommunicationChannel", e("Filiale", "FI"), e("Hausbesuch", "HB"), e("Telefon", "TE"), e("E-Mail", "unknown-1"), e("Fax", "FA"), e("Schriftl. Auftrag", "unknown-2"));
        addDecl(decl, order, "CommunicationChannel", pti(TiType.TI_STRING), mmString("FA"));
//        addEnum(decl, order, "BusinessSegment", e("Anlageberatung/VV", "1"), e("Beratungsfr. Geschäft/UI VV", "2"), e("Handelsgeschäft GGP", "3"));
        addDecl(decl, order, "BusinessSegment", pti(TiType.TI_STRING), mmString("1"));
        addDecl(decl, order, "Comment", TiType.TI_MEMO);
        addDecl(decl, order, "Enabled", TiType.TI_BOOLEAN);
        addDecl(decl, order, "ProcessedQuantity", TiType.TI_NUMBER);
    }
}
