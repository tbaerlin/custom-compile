/*
 * DependencyFeatureTestMock.java
 *
 * Created on 08.10.2014 13:34
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;
import static de.marketmaker.iview.pmxml.TiType.*;
import static de.marketmaker.iview.pmxml.WidgetInputAction.*;

/**
 * @author mdick
 */
@NonNLS
public class DependencyFeatureTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "aBoolean1", "A Boolean 1 (initially false)");
        addDependency(addEditWidget(s, "aString1", "A string visible if Boolean 1 is true"), "aBoolean1", WIA_VISIBLE_IF_TRUE
        );

        addEditWidget(s, "aBoolean2", "A Boolean 2 (initially true)");
        addDependency(addEditWidget(s, "aString2", "A string visible if Boolean 2 is not true"), "aBoolean2", WIA_VISIBLE_IF_FALSE
        );

        addEditWidget(s, "aBoolean3", "A Boolean 3 (initially null)");
        addDependency(addEditWidget(s, "aString3", "A string visible if Boolean 3 is true"), "aBoolean3", WIA_VISIBLE_IF_TRUE
        );

        addEditWidget(s, "aBoolean4", "A Boolean 4 (initially null)");
        addDependency(addEditWidget(s, "aString4", "A string visible if Boolean 4 is not true"), "aBoolean4", WIA_VISIBLE_IF_FALSE
        );

        addEditWidget(s, "stringValueForRegExp", "A string value to be tested with RegExp");
        addDependency(addEditWidget(s, "aString5", "A string visible if RegExp matches"), "stringValueForRegExp", WIA_VISIBLE_IF_REG_EXP_MATCHES, "^Kaufen$");

        addEditWidget(s, "enumValueForRegExp", "An enum value to be tested with RegExp");
        addDependency(addEditWidget(s, "aString5", "A string visible if RegExp matches"), "enumValueForRegExp", WIA_VISIBLE_IF_REG_EXP_MATCHES, "^k$");

        addEditWidget(s, "enumValueForRegExpInverted", "An enum value to be tested with RegExp");
        addDependency(addEditWidget(s, "aString5", "A string visible if RegExp NOT matches"), "enumValueForRegExpInverted", WIA_VISIBLE_IF_REG_EXP_NOT_MATCHES, "^k$");

        // Angemessenheitsprüfung is only necessary for buy transactions.
        // The OK icon should be cleared if the transaction type value has changed.
        // Additionally, if the transaction type is buy or hold, the icon should be visible.
        // For sell, the icon should be invisible.
        addEditWidget(s, "enumValueForLabelWithIcon", "An enum value to be tested with undetermined icon on changed property");
        final LabelWidgetDesc labelWithIcon = addLabelWidget(s, "should show the undetermined icon if enumValueForLabelWithIcon changed");
        labelWithIcon.setIconNameBind("iconName");
        addDependency(labelWithIcon, "enumValueForLabelWithIcon", WIA_PM_ICON_CHECK_UNDETERMINED_IF_HAS_CHANGED);
        addDependency(labelWithIcon, "enumValueForLabelWithIcon", WIA_VISIBLE_IF_REG_EXP_MATCHES, "^k$|^h$");

        addEditWidget(s, "triggersStale", "triggersStale");
        addDependency(readonly(addEditWidget(s, "getsStale", "getsStale")), "triggersStale", WIA_INDICATE_STALE_VALUE_IF_CHANGED);

        final SectionDesc editSection = addSectionWidget(null, "$#");
        addEditWidget(editSection, "booleanChangesList", "booleanChangesList");
        addSectionListWidget(s, "listTriggersStale", editSection, "listTriggersStale");
        addDependency(readonly(addEditWidget(s, "getsStaleFromList", "getsStaleFromList")), "listTriggersStale", WIA_INDICATE_STALE_VALUE_IF_CHANGED);

        addEditWidget(s, "triggersReset", "triggersReset");
        addDependency(readonly(addEditWidget(s, "isReset", "isReset")), "triggersReset", WIA_SET_BOUND_VALUE_TO_NULL_IF_CHANGED);
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode root = prepareRootNode(dcn);

        addDecl(decl, root, "aBoolean1", pti(TI_BOOLEAN), mmBool(true));
        addDecl(decl, root, "aBoolean2", pti(TI_BOOLEAN), mmBool(true));
        addDecl(decl, root, "aBoolean3", pti(TI_BOOLEAN));
        addDecl(decl, root, "aBoolean4", pti(TI_BOOLEAN));
        addDecl(decl, root, "aString1", pti(TI_STRING), mmString("Hello World!"));
        addDecl(decl, root, "aString2", pti(TI_STRING), mmString("Hello Again!"));
        addDecl(decl, root, "aString3", pti(TI_STRING), mmString("Hello Again!"));
        addDecl(decl, root, "aString4", pti(TI_STRING), mmString("Hello Again!"));

        addDecl(decl, root, "aString5", pti(TI_STRING), mmString("Hello Again!"));

        addDecl(decl, root, "stringValueForRegExp", pti(TI_STRING), mmString("Kaufen"));
        addDecl(decl, root, "enumValueForRegExp", pti(e("Kaufen", "k"), e("Verkaufen", "v"), e("Halten", "h")), mmIndexedString("Verkaufen", "v"));
        addDecl(decl, root, "enumValueForRegExpInverted", pti(e("Kaufen", "k"), e("Verkaufen", "v"), e("Halten", "h")), mmIndexedString("Verkaufen", "v"));

        addDecl(decl, root, "enumValueForLabelWithIcon", pti(e("Kaufen", "k"), e("Verkaufen", "v"), e("Halten", "h")), mmIndexedString("Kaufen", "k"));
        addDecl(decl, root, "iconName", pti(TI_STRING), mmString("PmIcon:CheckOk"));

        addDecl(decl, root, "getsStale", pti(TI_NUMBER), mmNumber(42.23));
        addDecl(decl, root, "triggersStale", pti(TI_NUMBER), mmNumber(51));

        addDecl(decl, root, "getsStaleFromList", pti(TI_NUMBER), mmNumber(23.42));
        final DataContainerListNode listTriggersStale = addList(root, "listTriggersStale");
        final DataContainerGroupNode listEntry = addGroupToList(decl, listTriggersStale);
        addDecl(decl, listEntry, "booleanChangesList", pti(TI_BOOLEAN), mmBool(false));

        addDecl(decl, root, "isReset", pti(TI_NUMBER), mmNumber(42.23));
        addDecl(decl, root, "triggersReset", pti(TI_NUMBER), mmNumber(51));
    }
}
