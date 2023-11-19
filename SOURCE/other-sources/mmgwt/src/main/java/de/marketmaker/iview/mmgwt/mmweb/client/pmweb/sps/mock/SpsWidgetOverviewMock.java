package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.BoundWidgetDesc;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMIndexedString;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TiDateKind;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Arrays;
import java.util.List;

/**
 * Author: umaurer
 * Created: 07.10.14
 */
@NonNLS
public class SpsWidgetOverviewMock extends PreProcessHook {
    public static final String MOCK_USER_PROPERTY = "fbSpsWidgetOverviewMock";
    private static final String[] LIST_VALUES = {"n/a", "Kaufen", "Verkaufen", "Zeichnen mit noch mehr Text, so dass der Platz nicht reicht"};

    @Override
    public void preProcess(SectionDesc formDescRoot, DataContainerCompositeNode dataDeclRoot, DataContainerCompositeNode dataRoot, List<ErrorMM> errors) {
        if (!SessionData.INSTANCE.isUserPropertyTrue(MOCK_USER_PROPERTY)) {
            return;
        }
        if (!"Einstiegsseite".equals(formDescRoot.getCaption())) {
            return;
        }
        if (dataDeclRoot.getChildren().isEmpty()) {
            Firebug.warn("no decl \"FirstPage\"");
            return;
        }
        final DataContainerNode declFirstPage = dataDeclRoot.getChildren().get(0);
        if (!"FirstPage".equals(declFirstPage.getNodeLevelName())) {
            Firebug.warn("first decl is not \"FirstPage\"");
            return;
        }
        if (!(declFirstPage instanceof DataContainerGroupNode)) {
            Firebug.warn("\"FirstPage\" is not of type DataContainerGroupNode");
            return;
        }
        formDescRoot.setDescription("Diese Seite enthält den Mock " + this.getClass().getSimpleName() + ".\n Er wird eingeschaltet mit der user property \"" + MOCK_USER_PROPERTY + "\". Siehe dazu das Zahnrad rechts unten.");
        final DataContainerGroupNode decl = (DataContainerGroupNode) declFirstPage;
        final DataContainerGroupNode data = (DataContainerGroupNode) dataRoot.getChildren().get(0);
        clearAllData(formDescRoot, decl, data);
        addSimpleForm(formDescRoot, decl, data);
        addOtherForm(formDescRoot, decl, data);
        addListForm(formDescRoot, decl, data, "ListForm_1sel_1col", true, 1, "Diese Section ist eine einfache sps-form. Sie enthält ListWidgetDescs mit SingleSelect und einer einzelnen Spalte");
        addListForm(formDescRoot, decl, data, "ListForm_1sel_3col", true, 3, "Diese Section ist eine einfache sps-form. Sie enthält ListWidgetDescs mit SingleSelect und 3 Spalten");
        addListForm(formDescRoot, decl, data, "ListForm_xsel_1col", false, 1, "Diese Section ist eine einfache sps-form. Sie enthält ListWidgetDescs mit MultiSelect und einer einzelnen Spalte");
        addListForm(formDescRoot, decl, data, "ListForm_xsel_3col", false, 3, "Diese Section ist eine einfache sps-form. Sie enthält ListWidgetDescs mit MultiSelect und 3 Spalten");
        addMiscellaneousForm(formDescRoot, decl, data);
        addSectionListForm(formDescRoot, decl, data);
    }

    private void addSectionListForm(SectionDesc formDescRoot, DataContainerGroupNode decl, DataContainerGroupNode data) {
        final SectionDesc sectionListForm = MockUtil.addSectionWidget(formDescRoot, "SectionListForm");

        final SectionDesc editSection = MockUtil.addSectionWidget(null, "editSection");

        MockUtil.addSectionListWidget(sectionListForm, "sectionList", editSection, "ListSection with mm-right styled cells", "tableLayout");
        MockUtil.addReadOnlyWidget(editSection, "number1", "Number1 right aligned", "mm-right");
        MockUtil.addReadOnlyWidget(editSection, "number2", "Number2");
        MockUtil.addEditWidget(editSection, "number3", "Number3 right aligned", "mm-right");
        MockUtil.addEditWidget(editSection, "number4", "Number4");

        for (Boolean isDecl : Arrays.asList(true, false)) {
            final DataContainerListNode sectionListNode = MockUtil.addList(isDecl ? decl : data, "sectionList");
            for (int i = 0, j = 1; i < 10; i++, j++) {
                final DataContainerGroupNode entryNode = MockUtil.addGroupToList(isDecl, sectionListNode);
                final double value = i / j;
                MockUtil.addDecl(isDecl, entryNode, "number1", MockUtil.pti(false, "10"), MockUtil.mmNumber(value));
                MockUtil.addDecl(isDecl, entryNode, "number2", MockUtil.pti(false, "10"), MockUtil.mmNumber(value));
                MockUtil.addDecl(isDecl, entryNode, "number3", MockUtil.pti(false, "10"), MockUtil.mmNumber(value));
                MockUtil.addDecl(isDecl, entryNode, "number4", MockUtil.pti(false, "10"), MockUtil.mmNumber(value));
            }
        }
    }

    private void clearAllData(SectionDesc formDescRoot, DataContainerGroupNode declFirstPage, DataContainerGroupNode dataFirstPage) {
        formDescRoot.getItems().clear();
        declFirstPage.getChildren().clear();
        dataFirstPage.getChildren().clear();
    }

    class Desc {
        final SectionDesc sd;
        final DataContainerGroupNode dcDecl;
        final DataContainerGroupNode dcData;
        WidgetDesc wd;
        TiType tiType;
        ParsedTypeInfo pti;

        Desc(SectionDesc sd, DataContainerGroupNode dcDecl, DataContainerGroupNode dcData) {
            this.sd = sd;
            this.dcDecl = dcDecl;
            this.dcData = dcData;
        }

        Desc add(WidgetDesc wd) {
            this.sd.getItems().add(wd);
            this.wd = wd;
            return this;
        }

        Desc dateKind(TiDateKind dk) {
            this.pti.setDateKind(dk);
            return this;
        }

        Desc seconds() {
            this.pti.setIsTimeSeconds(true);
            return this;
        }

        Desc style(String style) {
            this.wd.setStyle(style);
            return this;
        }

        Desc readonly() {
            if (this.wd instanceof BoundWidgetDesc) {
                ((BoundWidgetDesc) this.wd).setIsReadonly(true);
            }
            else {
                throw new RuntimeException("Cannot be readonly (not BoundWidgetDesc): " + this.wd.getClass().getSimpleName());
            }
            return this;
        }

        Desc tooltip(String tooltip) {
            if (this.wd instanceof BoundWidgetDesc) {
                ((BoundWidgetDesc) this.wd).setTooltip(tooltip);
            }
            else {
                throw new RuntimeException("Cannot be tool-tipped (not BoundWidgetDesc): " + this.wd.getClass().getSimpleName());
            }
            return this;
        }

        Desc mandatory() {
            this.pti.setDemanded(true);
            return this;
        }

        Desc noDescription() {
            if (this.pti == null) {
                return this;
            }
            this.pti.setDescription(null);
            return this;
        }
    }

    private void addSimpleForm(SectionDesc formDescRoot, DataContainerGroupNode declFirstPage, DataContainerGroupNode dataFirstPage) {
        final String bind = "simpleForm";
        final SectionDesc sd = new SectionDesc();
//        sd.setDescription("Diese Section ist eine einfache sps-form. Sie enthält etliche EditWidgetDescs, die in Typ und Parametern variieren.");
        sd.setCaption(bind);
        sd.setId(bind);
        sd.setBind(bind);
        sd.setStyle("sps-form spsWidgetOverviewMock mockSimpleForm");
        formDescRoot.getItems().add(sd);

        final DataContainerGroupNode dcDecl;
        final DataContainerGroupNode dcData;
        declFirstPage.getChildren().add(dcDecl = createGroupNode(bind));
        dataFirstPage.getChildren().add(dcData = createGroupNode(bind));

        final Desc desc = new Desc(sd, dcDecl, dcData);

        desc.tiType = TiType.TI_STRING;
        addEditWidgetDesc(desc, "Kaufen", "String", "editable, optional");
        addEditWidgetDesc(desc, "Kaufen", "String_mandatory", "editable, mandatory").mandatory();
        addEditWidgetDesc(desc, "Kaufen", "String", "readonly, optional").readonly();
        addEditWidgetDesc(desc, null, "String_null", "editable, optional");
        addEditWidgetDesc(desc, null, "String_null", "readonly, optional").readonly();

        desc.tiType = TiType.TI_BOOLEAN;
        addEditWidgetDesc(desc, "true", "Boolean_true", "editable, optional");
        addEditWidgetDesc(desc, "true", "Boolean_true", "readonly, optional, true").readonly();
        addEditWidgetDesc(desc, "false", "Boolean_false", "readonly, optional, false").readonly();
        addEditWidgetDesc(desc, null, "Boolean_null", "editable, optional, null");
        addEditWidgetDesc(desc, null, "Boolean_null_mandatory", "editable, mandatory, null").mandatory();

        desc.tiType = TiType.TI_NUMBER;
        addEditWidgetDesc(desc, "12", "Number_12", "editable, optional, 12");
        addEditWidgetDesc(desc, "12", "Number_12", "readonly, optional, 12").readonly();
        addEditWidgetDesc(desc, "12", "Number_12_mandatory", "editable, mandatory, 12").mandatory();
        addEditWidgetDesc(desc, null, "Number_null", "editable, optional, null");

        desc.tiType = TiType.TI_ENUMERATION;
        addEditWidgetDesc(desc, "Kaufen", "Enum_Kaufen", "editable, optional, Kaufen");
        addEditWidgetDesc(desc, "Verkaufen", "Enum_mandatory", "editable, mandatory, Kaufen").mandatory();
        addEditWidgetDesc(desc, "Kaufen", "Enum_Kaufen", "readonly, optional, Kaufen").readonly();
        addEditWidgetDesc(desc, null, "Enum_null", "editable, optional, null");

        addEditWidgetDesc(desc, "Kaufen", "Enum_Kaufen", "editable, optional, Kaufen\nstyle: combo").style("combo");
        addEditWidgetDesc(desc, "Verkaufen", "Enum_mandatory", "editable, mandatory, Kaufen\nstyle: combo").mandatory().style("combo");
        addEditWidgetDesc(desc, "Kaufen", "Enum_Kaufen", "readonly, optional, Kaufen\nstyle: combo").readonly().style("combo");
        addEditWidgetDesc(desc, null, "Enum_null", "editable, optional, null\nstyle: combo").style("combo").noDescription();

        desc.tiType = TiType.TI_DATE;
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "DateTime_2012", "editable, optional, 2012").dateKind(TiDateKind.DK_DATE_TIME);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "DateTime_2012_mandatory", "editable, mandatory, 2012").mandatory().dateKind(TiDateKind.DK_DATE_TIME);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "DateTime_2012", "readonly, optional, 2012").readonly().dateKind(TiDateKind.DK_DATE_TIME);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "DateTime_2012_sec", "editable, optional, 2012").dateKind(TiDateKind.DK_DATE_TIME).seconds();
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "DateTime_2012_sec", "readonly, optional, 2012").readonly().dateKind(TiDateKind.DK_DATE_TIME).seconds();
        addEditWidgetDesc(desc, null, "DateTime_null", "editable, optional, null").dateKind(TiDateKind.DK_DATE_TIME);

        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Date_2012", "editable, optional, 2012").dateKind(TiDateKind.DK_DATE);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Date_2012_mandatory", "editable, mandatory, 2012").mandatory().dateKind(TiDateKind.DK_DATE);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Date_2012", "readonly, optional, 2012").readonly().dateKind(TiDateKind.DK_DATE);
        addEditWidgetDesc(desc, null, "Date_null", "editable, optional, null").dateKind(TiDateKind.DK_DATE);

        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Time_2012", "editable, optional, 2012").dateKind(TiDateKind.DK_TIME);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Time_2012_mandatory", "editable, mandatory, 2012").dateKind(TiDateKind.DK_TIME);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Time_2012", "readonly, optional, 2012").dateKind(TiDateKind.DK_TIME);
        addEditWidgetDesc(desc, null, "Time_null", "editable, optional, null").dateKind(TiDateKind.DK_TIME);
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Time_2012_sec", "editable, optional, 2012").dateKind(TiDateKind.DK_TIME).seconds();
        addEditWidgetDesc(desc, "2012-07-28T05:12:23", "Time_2012_sec", "readonly, optional, 2012").dateKind(TiDateKind.DK_TIME).seconds();
        addEditWidgetDesc(desc, null, "Time_2012_null_sec", "editable, optional, 2012").dateKind(TiDateKind.DK_TIME).seconds();

        desc.tiType = TiType.TI_MEMO;
        addEditWidgetDesc(desc, "Kaufen\nVerkaufen\nund sonstige Späße", "Memo", "editable, optional");

        final ShellMMInfo security = MockUtil.shellMMInfo("0", ShellMMType.ST_AKTIE, "BASF11", "DE000BASF111", "Badische Anilin- & Soda-Fabrik Aktiengesellschaft", "90577300");
        addDeclLeaf(MockUtil.pti(TiType.TI_SHELL_MM), "security", dcDecl.getChildren());
        addDataLeaf("security", security, dcData.getChildren());

        desc.add(MockUtil.createEditWidgetDesc("security", "Description")).style("description").readonly().tooltip("A Description tooltip");
        desc.add(MockUtil.createEditWidgetDesc("security", "Description")).style("description").readonly();
        desc.add(MockUtil.createEditWidgetDesc("security", "Type")).style("type").readonly().tooltip("A Type tooltip");
        desc.add(MockUtil.createEditWidgetDesc("security", "WKN")).style("number").readonly().tooltip("A WKN tooltip");
        desc.add(MockUtil.createEditWidgetDesc("security", "ISIN")).style("isin").readonly().tooltip("An ISIN tooltip");
        desc.add(MockUtil.createEditWidgetDesc("security", "ISIN/WKN")).style("isin-number").readonly().tooltip("An ISIN or WKN tooltip");
    }

    private void addOtherForm(SectionDesc formDescRoot, DataContainerGroupNode declFirstPage, DataContainerGroupNode dataFirstPage) {
        final String bind = "otherForm";
        final SectionDesc sd = new SectionDesc();
//        sd.setDescription("Diese Section ist eine einfache sps-form. Sie enthält etliche EditWidgetDescs, die in Typ und Parametern variieren.");
        sd.setCaption(bind);
        sd.setId(bind);
        sd.setBind(bind);
        sd.setStyle("sps-form spsWidgetOverviewMock mockOtherForm");
        formDescRoot.getItems().add(sd);

        final DataContainerGroupNode dcDecl;
        final DataContainerGroupNode dcData;
        declFirstPage.getChildren().add(dcDecl = createGroupNode(bind));
        dataFirstPage.getChildren().add(dcData = createGroupNode(bind));

        final Desc desc = new Desc(sd, dcDecl, dcData);
        desc.tiType = TiType.TI_STRING;
        addLabelWidgetDesc(desc, null, "&nbsp;").noDescription();

        addLabelWidgetDesc(desc, "LabelWidgetDesc", "Kaufen").noDescription();

        addEditWidgetDesc(desc, "A long string in a readonly widget with styles width-100 and sps-nowrap. This is the value", "loremLong", "").noDescription();
        desc.style("width-100 sps-nowrap").readonly();
        addLabelWidgetDesc(desc, "Not Bound Label No-Wrap + Ellipsis + Completion", "NotBoundLabelNoWrap Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");
        desc.style("width-100 sps-nowrap");

        MockUtil.addLabelWidget(desc.sd, "this string has no effect", "boundLabelNoWrap", "Bound Label No-Wrap + Ellipsis + Completion", "Bound Label No-Wrap + Ellipsis + Completion", "width-100", "sps-nowrap");
        MockUtil.addDecl(true, dcDecl, "boundLabelNoWrap", MockUtil.pti(TiType.TI_STRING), MockUtil.mmString("BoundLabelNoWrap consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat"));
        MockUtil.addDecl(false, dcData, "boundLabelNoWrap", MockUtil.pti(TiType.TI_STRING), MockUtil.mmString("BoundLabelNoWrap consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat"));

        addLabelWidgetDesc(desc, "Emphasized Caption", "Ut labore et dolore magna aliquyam erat, sed diam voluptua");
        desc.style("sps-caption-emphasize");

        final LabelWidgetDesc labelWidgetDesc = MockUtil.addLabelWidget(desc.sd, "bound labelTextAsIconTooltip static text has no effect", "labelTextAsIconTooltip", "useLabelTextAsIconTooltip", "useLabelTextAsIconTooltip");
        labelWidgetDesc.setIconNameBind("labelIconName");
        MockUtil.addDecl(true, dcDecl, "labelIconName", MockUtil.pti(TiType.TI_STRING), MockUtil.mmString("PmIcon:CheckOk"));
        MockUtil.addDecl(false, dcData, "labelIconName", MockUtil.pti(TiType.TI_STRING), MockUtil.mmString("PmIcon:CheckOk"));
        MockUtil.addDecl(true, dcDecl, "labelTextAsIconTooltip", MockUtil.pti(TiType.TI_STRING), MockUtil.mmString("bound labelTextAsIconTooltip"));
        MockUtil.addDecl(false, dcData, "labelTextAsIconTooltip", MockUtil.pti(TiType.TI_STRING), MockUtil.mmString("bound labelTextAsIconTooltip"));

        final LabelWidgetDesc labelWidgetDesc2 = MockUtil.addLabelWidget(desc.sd, "not bound labelTextAsIconTooltip", "", "useLabelTextAsIconTooltip not bound", "useLabelTextAsIconTooltip");
        labelWidgetDesc2.setIconNameBind("labelIconName");
    }

    private void addMiscellaneousForm(SectionDesc formDescRoot, DataContainerGroupNode declFirstPage, DataContainerGroupNode dataFirstPage) {
        final String bind = "miscellaneous";
        final SectionDesc sd = new SectionDesc();
        sd.setDescription("This section contains miscellaneous widgets beyond standard text inputs, lists, check boxes, etc. The section itself is an example of a dividerSection styled section");
        sd.setCaption("Miscellaneous");
        sd.setId(bind);
        sd.setBind(bind);
        sd.setStyle("sps-form spsWidgetOverviewMock mockSimpleForm dividerSection");
        formDescRoot.getItems().add(sd);

        final DataContainerGroupNode dcDecl;
        final DataContainerGroupNode dcData;
        declFirstPage.getChildren().add(dcDecl = createGroupNode(bind));
        dataFirstPage.getChildren().add(dcData = createGroupNode(bind));

        final Desc desc = new Desc(sd, dcDecl, dcData);

        /* DIShellMMInfo */
        //stock BASF
        final ShellMMInfo security = MockUtil.shellMMInfo("0", ShellMMType.ST_AKTIE, "BASF11", "DE000BASF111", "Badische Anilin- & Soda-Fabrik Aktiengesellschaft", "90577300");
        addDeclLeaf(MockUtil.pti(TiType.TI_SHELL_MM), "security", dcDecl.getChildren());
        addDataLeaf("security", security, dcData.getChildren());

        //Depot Test HA (pmweb)
        final ShellMMInfo securityAccountTestHa = MockUtil.shellMMInfo("20473717", ShellMMType.ST_DEPOT, "1234567890", null, "Test HA (pmweb)", null);
        addDeclLeaf(MockUtil.pti(TiType.TI_FOLDER), "securityAccount", dcDecl.getChildren());
        addDataLeaf("securityAccount", securityAccountTestHa, dcData.getChildren());

        desc.add(MockUtil.createEditWidgetDesc("security", "Security", ""));
        desc.add(MockUtil.createEditWidgetWithObjectDesc("security", "securityAccount", "Pick from Depot"))
                .style("pickDepotSymbol")
                .tooltip("Pick a security that is already in stock of security account Test HA");

        desc.add(MockUtil.createEditWidgetDesc("security", "Description")).style("description").readonly();
        desc.add(MockUtil.createEditWidgetDesc("security", "Type")).style("type").readonly();
        desc.add(MockUtil.createEditWidgetDesc("security", "WKN")).style("number").readonly();
        desc.add(MockUtil.createEditWidgetDesc("security", "ISIN")).style("isin").readonly();
        desc.add(MockUtil.createEditWidgetDesc("security", "ISIN/WKN")).style("isin-number").readonly();

        /* PM DMS */
        addDmsGroup(desc, "dmsDocumentData");
        desc.add(MockUtil.createEditWidgetDesc("dmsDocumentData", "Archived document ID", "A link to a document in a DMS connected to PM"))
                .style("dmsDocumentLink").readonly();

        /* PM async file attachment upload */
        desc.add(MockUtil.createEditWidgetDesc(null, "Attachments", null)).style("fileAttachments");

        /* vwd document manager */
        //declare transaction type
        addDeclLeaf(MockUtil.pti(createEnumElement("Kaufen")), "transactionType", dcDecl.getChildren());
        addDataLeaf("transactionType", MockUtil.mmDbRef("Kaufen"), dcData.getChildren());

        //do archive widget
        addDeclLeaf(MockUtil.pti(TiType.TI_STRING), "archivedDocumentId", dcDecl.getChildren());
        addDataLeaf("archivedDocumentId", new DefaultMM(), dcData.getChildren());
        desc.add(MockUtil.createEditWidgetWithObjectDesc("archivedDocumentId", "security", "Archive PIB/KIID"))
                .style("pibKiidArchiveDownload");

        //security does not matter in this case, but must be bound!
        addDeclLeaf(MockUtil.pti(TiType.TI_STRING), "alreadyArchivedDocumentId", dcDecl.getChildren());
        addDataLeaf("alreadyArchivedDocumentId", MockUtil.mmString("NF1V.ON3E.1YN6.15DD"), dcData.getChildren());
        desc.add(MockUtil.createEditWidgetWithObjectDesc("alreadyArchivedDocumentId", "security", "Archived PIB/KIID"))
                .style("pibKiidArchiveDownload");

        //availability widget is usable as labelWidgetDesc as well as editWidgetWithObjectDesc!
        desc.add(MockUtil.createLabelWidgetDesc(null, "security", "PIB/KIID avail lbl"))
                .style("pibKiidAvailability");
        desc.add(MockUtil.createEditWidgetWithObjectDesc("security", "transactionType", "PIB/KIID avail edWObj"))
                .style("pibKiidAvailability");
        desc.add(MockUtil.createLabelWidgetDesc(null, "security", "PIB/KIID avail ico lbl"))
                .style("pibKiidAvailabilityIcon");
        desc.add(MockUtil.createEditWidgetWithObjectDesc("security", "transactionType", "PIB/KIID avail ico edWObj"))
                .style("pibKiidAvailabilityIcon");
    }

    private void addDmsGroup(Desc desc, String bind) {
        desc.dcDecl.getChildren().add(createDmsGroup(true, bind));
        desc.dcData.getChildren().add(createDmsGroup(false, bind));
    }

    private DataContainerGroupNode createDmsGroup(boolean decl, String nodeLevelName) {
        return MockUtil.createDmsGroupNode(decl, nodeLevelName, "", true, "A PDF file", "AN-IMAGINARY-DMS-HANDLE-THAT-DOES-NOT-WORK");
    }

    private Desc addEditWidgetDesc(Desc d, String value, String bind, String description) {
        final String caption = d.tiType.toString();
        bind = "EditWidgetDesc_" + bind;
        final EditWidgetDesc wd = new EditWidgetDesc();
        wd.setCaption(caption);
        wd.setBind(bind);
        d.add(wd);

        if (hasNode(d.dcDecl, bind)) {
            updatePti(d, bind);
            return d;
        }

        d.pti = addLeaf(d.tiType, bind, value, d.dcDecl.getChildren(), d.dcData.getChildren());
        d.pti.setDescription(description + "\nbind: " + bind + "\nEinfaches EditWidgetDesc mit " + d.tiType);
        if (d.tiType == TiType.TI_ENUMERATION) {
            for (String listValue : LIST_VALUES) {
                d.pti.getEnumElements().add(createEnumElement(listValue));
            }
        }
        return d;
    }

    private Desc addLabelWidgetDesc(Desc d, String caption, String text) {
        final LabelWidgetDesc wd = new LabelWidgetDesc();
        wd.setCaption(caption);
        wd.setText(text);
        d.add(wd);
        d.pti = null;
        return d;
    }

    private void updatePti(Desc d, String bind) {
        for (DataContainerNode node : d.dcDecl.getChildren()) {
            if (bind.equals(node.getNodeLevelName())) {
                if (node instanceof DataContainerLeafNodeDeclaration) {
                    d.pti = ((DataContainerLeafNodeDeclaration) node).getDescription();
                }
            }
        }
    }

    private MMIndexedString createEnumElement(String name) {
        final MMIndexedString e = new MMIndexedString();
        e.setCode(name);
        e.setValue(name);
        return e;
    }

    private boolean hasNode(DataContainerGroupNode node, String nodeLevelName) {
        for (DataContainerNode child : node.getChildren()) {
            if (nodeLevelName.equals(child.getNodeLevelName())) {
                return true;
            }
        }
        return false;
    }

    private DataContainerGroupNode createGroupNode(String nodeLevelName) {
        if (nodeLevelName == null) {
            return null;
        }
        final DataContainerGroupNode node = new DataContainerGroupNode();
        node.setNodeLevelName(nodeLevelName);
        return node;
    }

    private DataContainerListNode createListNode(String nodeLevelName) {
        final DataContainerListNode node = new DataContainerListNode();
        node.setNodeLevelName(nodeLevelName);
        return node;
    }

    private void addListForm(SectionDesc formDescRoot, DataContainerGroupNode declFirstPage, DataContainerGroupNode dataFirstPage, String bind, boolean singleSelect, int colCount, String description) {
        final SectionDesc sd = new SectionDesc();
        sd.setDescription(description);
        sd.setCaption("ListWidgetDesc " + (singleSelect ? " SingleSelect" : " MultiSelect") + " " + colCount + " columns");
        sd.setId(bind);
        sd.setBind(bind);
        sd.setStyle("sps-form spsWidgetOverviewMock mockListForm" + (singleSelect ? " mock1sel" : " mockXsel"));
        formDescRoot.getItems().add(sd);

        final DataContainerGroupNode dcDecl;
        final DataContainerGroupNode dcData;
        declFirstPage.getChildren().add(dcDecl = createGroupNode(bind));
        dataFirstPage.getChildren().add(dcData = createGroupNode(bind));

        final Desc desc = new Desc(sd, dcDecl, dcData);
        desc.tiType = TiType.TI_STRING;

        addListWidgetDesc(desc, "Kaufen", LIST_VALUES, "SingleSelect_SingleColumn", "LWD", singleSelect, colCount);
        addListWidgetDesc(desc, "Kaufen", LIST_VALUES, "SingleSelect_SingleColumn_mandatory", "LWD", singleSelect, colCount).mandatory();
        addListWidgetDesc(desc, "Kaufen", LIST_VALUES, "SingleSelect_SingleColumn", "LWD", singleSelect, colCount).readonly();
        addListWidgetDesc(desc, null, LIST_VALUES, "SingleSelect_SingleColumn_null", "LWD", singleSelect, colCount);
        addListWidgetDesc(desc, null, LIST_VALUES, "SingleSelect_SingleColumn_null", "LWD", singleSelect, colCount).readonly();
        addListWidgetDesc(desc, null, new String[0], "SingleSelect_SingleColumn_null_empty", "LWD", singleSelect, colCount);
        addListWidgetDesc(desc, null, new String[0], "SingleSelect_SingleColumn_mandatory_null_empty", "LWD", singleSelect, colCount).mandatory();

        //default icon
        addListWidgetDesc(desc, "Kaufen", LIST_VALUES, "SingleSelect_SingleColumn", "LWD", singleSelect, colCount).style("tablePicker");
        //16x16px portfolio icon
        addListWidgetDesc(desc, "Kaufen", LIST_VALUES, "SingleSelect_SingleColumn_mandatory", "LWD", singleSelect, colCount).style("tablePicker PmIcon:Portfolio16");
        addListWidgetDesc(desc, "Kaufen", LIST_VALUES, "SingleSelect_SingleColumn", "LWD", singleSelect, colCount).readonly().style("tablePicker PmIcon:Portfolio16");
    }

    private Desc addListWidgetDesc(Desc d, String value, String[] listValues, String bind, String description, boolean singleSelect, int colCount) {
        final String caption = "ListWidgetDesc";
        final String bindValue = "ListWidgetDesc_value_" + bind;
        final String bindList = "ListWidgetDesc_list_" + bind;

        final ListWidgetDesc wd = new ListWidgetDesc();
        wd.setCaption(caption);
        wd.setBind(bindValue);
        wd.setItemsBind(bindList);
        wd.setKeyField("col-0");
        for (int i = 0; i < colCount; i++) {
            final ListWidgetDescColumn col = new ListWidgetDescColumn();
            col.setColumnName("col-" + i);
            col.setFieldName("col-" + i);
            wd.getColumns().add(col);
        }
        d.add(wd);

        if (hasNode(d.dcDecl, bindValue)) {
            return d;
        }

        // value
        if (singleSelect) {
            d.pti = addLeaf(d.tiType, bindValue, value, d.dcDecl.getChildren(), d.dcData.getChildren());
            d.pti.setDescription(description + "\nbindValue: " + bindValue + "\nbindList: " + bindList + "\nListWidgetDesc mit SingleSelect\ncolCount: " + colCount);
        }
        else {
            final DataContainerListNode declValues = createListNode(bindValue);
            final DataContainerListNode dataValues = createListNode(bindValue);
            d.dcDecl.getChildren().add(declValues);
            d.dcData.getChildren().add(dataValues);
            d.pti = addLeaf(d.tiType, bindValue, value, declValues.getChildren(), dataValues.getChildren());
            d.pti.setDescription(description + "\nbindValue: " + bindValue + "\nbindList: " + bindList + "\nListWidgetDesc mit SingleSelect\ncolCount: " + colCount);
        }

        // list
        final DataContainerListNode declListNode = createListNode(bindList);
        final DataContainerListNode dataListNode = createListNode(bindList);
            addDeclListEntry(declListNode, d.pti, colCount);
            if (value != null && !contains(listValues, value)) {
                addDataListEntry(dataListNode, d.pti, createArray(colCount, value));
            }
            for (String listValue : listValues) {
                addDataListEntry(dataListNode, d.pti, createArray(colCount, listValue));
            }
        d.dcDecl.getChildren().add(declListNode);
        d.dcData.getChildren().add(dataListNode);

        return d;
    }

    private boolean contains(String[] array, String value) {
        for (String v : array) {
            if (value.equals(v)) {
                return true;
            }
        }
        return false;
    }

    private void addDeclListEntry(DataContainerListNode declListNode, ParsedTypeInfo pti, int colCount) {
        final DataContainerGroupNode declGroupNode = new DataContainerGroupNode();
        declListNode.getChildren().add(declGroupNode);
        addDeclListEntries(pti, declGroupNode, colCount);
    }

    private void addDeclListEntries(ParsedTypeInfo pti, DataContainerCompositeNode parentNode, int colCount) {
        for (int i = 0; i < colCount; i++) {
            addDeclLeaf(pti, "col-" + i, parentNode.getChildren());
        }
    }

    private void addDataListEntry(DataContainerListNode dataListNode, ParsedTypeInfo pti, String[] values) {
        final DataContainerGroupNode dataGroupNode = new DataContainerGroupNode();
        dataListNode.getChildren().add(dataGroupNode);
        for (int i = 0; i < values.length; i++) {
            addDataLeaf(pti.getTypeId(), "col-" + i, values[i], dataGroupNode.getChildren());
        }
    }

    private String[] createArray(int colCount, String... values) {
        final String[] array = new String[colCount];
        System.arraycopy(values, 0, array, 0, values.length);
        for (int i = values.length; i < array.length; i++) {
            array[i] = "col-" + i;
        }
        return array;
    }

    private ParsedTypeInfo addLeaf(TiType tiType, String bindValue, String value, List<DataContainerNode> declList, List<DataContainerNode> dataList) {
        final ParsedTypeInfo pti = MockUtil.pti(tiType, "pti.displayName", false, false, "0", "0", "0", "0", 0, null);

        addDeclLeaf(pti, bindValue, declList);

        if (value == null) {
            return pti;
        }
        addDataLeaf(tiType, bindValue, value, dataList);
        return pti;
    }

    private void addDeclLeaf(ParsedTypeInfo pti, String bindValue, List<DataContainerNode> declList) {
        final DataContainerLeafNodeDeclaration decl = new DataContainerLeafNodeDeclaration();
        decl.setNodeLevelName(bindValue);
        decl.setDescription(pti);
        declList.add(decl);
    }

    private void addDataLeaf(TiType tiType, String bindValue, String value, List<DataContainerNode> dataList) {
        addDataLeaf(bindValue, MmTalkHelper.asMMType(value, tiType), dataList);
    }

    private void addDataLeaf(String bindValue, MM dataItem, List<DataContainerNode> dataList) {
        final DataContainerLeafNodeDataItem data = new DataContainerLeafNodeDataItem();
        data.setNodeLevelName(bindValue);
        data.setDataItem(dataItem);
        dataList.add(data);
    }
}
