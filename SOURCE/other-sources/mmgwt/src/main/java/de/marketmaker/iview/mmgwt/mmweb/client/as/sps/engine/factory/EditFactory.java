package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.AbstractSpsAsyncLinkWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsAttachmentsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCheckBox;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCombo;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDateTimeEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDecimalEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDmsLinkWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsMultilineEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsOrderEntryWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsRadios;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsReadonlyField;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSecurityDocumentsLinkWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSelectResearchSymbolPicker;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsShellMMInfoPicker;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TiType;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.ShellMMInfoPicker.*;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public class EditFactory extends Factory<EditWidgetDesc> {
    public EditFactory(String baseClass) {
        super(baseClass);
    }

    @Override
    SpsWidget doCreateSpsWidget(EditWidgetDesc editDesc, Context context, BindToken parentToken) {
        final String bindKey = editDesc.getBind();
        final SpsWidget spsWidget;
        if(TextUtil.hasStyle(editDesc, "fileAttachments")) {  // $NON-NLS$
            spsWidget = new SpsAttachmentsWidget().withActivityInstanceId(context.getActivityInstanceId())
                    .withTaskId(context.getTaskId())
                    .withActivityInstanceGuid(context.getActivityInstanceGuid());
        }
        else if (bindKey != null) {
            final BindToken bindToken = BindToken.create(parentToken, bindKey);

            if(context.isGroup(bindToken)) {
                spsWidget = createGroupBoundWidgets(editDesc, context);
            }
            else if (editDesc.isIsReadonly() || context.isForceReadonly()) {
                spsWidget = createReadonlyField(editDesc, context, bindToken);
            }
            else {
                spsWidget = createWidgetForType(editDesc, context, bindToken);
            }
        }
        else {
            if (editDesc.isIsReadonly() || context.isForceReadonly()) {
                spsWidget = new SpsReadonlyField(SpsReadonlyField.ValueType.TEXT);
            }
            else {
                spsWidget = new SpsEdit();
            }
        }
        spsWidget.setReadonly(editDesc.isIsReadonly() || context.isForceReadonly());
        spsWidget.setCaption(editDesc.getCaption());
        return spsWidget;
    }

    private SpsWidget createGroupBoundWidgets(EditWidgetDesc editDesc, final Context context) {
        if(TextUtil.hasStyle(editDesc, "dmsDocumentLink", "secDocumentLink")) {  // $NON-NLS$
            final AbstractSpsAsyncLinkWidget spsAsyncLinkWidget =
                TextUtil.hasStyle(editDesc, "dmsDocumentLink")  // $NON-NLS$
                    ? new SpsDmsLinkWidget()
                    : new SpsSecurityDocumentsLinkWidget();
            if((editDesc.isIsReadonly() || context.isForceReadonly()) && context.isForceDisabledLinksIfReadonly()) {
                spsAsyncLinkWidget.withDisabledLink();
            }
            return spsAsyncLinkWidget;
        }
        else if(TextUtil.hasStyle(editDesc, "orderEntryButton")) {  // $NON-NLS$
            return new SpsOrderEntryWidget()
                    .withMainInput(context.getMainInput())
                    .withActivityInstanceId(context.getActivityInstanceId())
                    .withInternalRefresh(context.getRefreshInternalCommand());
        }
        else {
            throw new RuntimeException("Binding an EditWidgetDesc to a group requires one of the following styles: \"dmsDocumentLink\", \"secDocumentLink\", \"orderEntryButton\""); // $NON-NLS$
        }
    }

    private SpsWidget createWidgetForType(EditWidgetDesc editDesc, final Context context, BindToken bindToken) {
        final TiType tiType = context.getTiType(bindToken);
        if (tiType != null) {
            final ParsedTypeInfo pti = context.getLeafDeclaration(bindToken).getDescription();
            switch (tiType) {
                case TI_STRING:
                    if(CssUtil.hasStyle(editDesc.getStyle(), "password")) {  // $NON-NLS$
                        return new SpsEdit().withMaxLength(pti.getMemoCharacterLimit());
                    }
                    else {
                        return new SpsEdit().withMaxLength(pti.getMemoCharacterLimit());
                    }
                case TI_NUMBER:
                    return new SpsDecimalEdit().withPercent(pti.isNumberProcent())
                            .withSpin(pti.getNumberSpin())
                            .withVUnit(pti.getVUnit())
                            .withMin(pti.getMin())
                            .withMax(pti.getMax())
                            .withDemanded(pti.isDemanded())
                            .withRenderTrailingZeros(TextUtil.hasStyle(editDesc, "mm-right"));  // $NON-NLS$
                case TI_ENUMERATION:
                    if (TextUtil.hasStyle(editDesc, "combo")) { // $NON-NLS$
                        return new SpsCombo(context.getEnum(bindToken), pti.getEnumerationNullValue());
                    }
                    else {
                        return new SpsRadios(context.getEnum(bindToken), pti.getEnumerationNullValue());
                    }
                case TI_BOOLEAN:
                    return new SpsCheckBox().withThreeValueBoolean(!pti.isBooleanIsKindOption());
                case TI_DATE:
                    return new SpsDateTimeEdit().withDateKind(pti.getDateKind()).withSeconds(pti.isIsTimeSeconds());
                case TI_MEMO:
                    return new SpsMultilineEdit().withMaxLength(pti.getMemoCharacterLimit());
                case TI_FOLDER: {
                    return createFolder(context, pti.getFolderTypes());
                }
                case TI_SHELL_MM: {
                    return createShellMMInfo(context, editDesc, pti.getFolderTypes());
                }
            }
        }
        return new SpsEdit();
    }

    private SpsWidget createReadonlyField(EditWidgetDesc editDesc, Context context, BindToken bindToken) {
        final TiType tiType = context.getTiType(bindToken);
        if (tiType != null) {
            switch (tiType) {
                case TI_NUMBER:
                    final ParsedTypeInfo ptiNumber = context.getLeafDeclaration(bindToken).getDescription();
                    final SpsReadonlyField.ValueType valueType = ptiNumber.isNumberProcent()
                            ? SpsReadonlyField.ValueType.PERCENT
                            : SpsReadonlyField.ValueType.DECIMAL;
                    return new SpsReadonlyField(valueType)
                            .withVUnit(ptiNumber.getVUnit())
                            .withRenderTrailingZeros(TextUtil.hasStyle(editDesc, "mm-right"));  // $NON-NLS$
                case TI_ENUMERATION:
                    final ParsedTypeInfo ptiEnum = context.getLeafDeclaration(bindToken).getDescription();
                    return new SpsReadonlyField(SpsReadonlyField.ValueType.ENUM).withEnumDescriptions(ptiEnum.getEnumerationNullValue(), context.getEnum(bindToken));
                case TI_BOOLEAN:
                    return new SpsReadonlyField(SpsReadonlyField.ValueType.CHECK);
                case TI_DATE:
                    return new SpsReadonlyField(SpsReadonlyField.ValueType.DATE);
                case TI_MEMO:
                    return new SpsReadonlyField(SpsReadonlyField.ValueType.PRE);
                case TI_SHELL_MM:
                case TI_FOLDER:
                    final SpsWidget researchSymbolPicker = createResearchSymbolPicker(editDesc, true, context.getActivityInstanceId());
                    if(researchSymbolPicker != null) {
                        return researchSymbolPicker;
                    }
                    final SpsReadonlyField field = new SpsReadonlyField(SpsReadonlyField.ValueType.SHELL)
                            .withShellMMInfoAttribute(getShellMMInfoAttribute(editDesc));
                    if(!context.isForceDisabledLinksIfReadonly()) {
                        field.withShellMMInfoLink(context.getHistoryContextName())
                                .withMainInput(context.getMainInput());
                    }
                    return field;
            }
        }
        return new SpsReadonlyField(SpsReadonlyField.ValueType.TEXT);
    }

    private SpsReadonlyField.ShellMMInfoAttribute getShellMMInfoAttribute(EditWidgetDesc editDesc) {
        for(SpsReadonlyField.ShellMMInfoAttribute attribute : SpsReadonlyField.ShellMMInfoAttribute.values()) {
            if(TextUtil.hasStyle(editDesc, attribute.getStyle())) {
                return attribute;
            }
        }
        return SpsReadonlyField.ShellMMInfoAttribute.DESCRIPTION;
    }

    private SpsWidget createFolder(Context context, List<ShellMMType> folderTypes) {
        final SpsShellMMInfoPicker p = new SpsShellMMInfoPicker();
        p.setSelectSymbolFormStyle(SelectSymbolFormStyle.FOLDER);
        p.setShellMMTypes(folderTypes);
        p.withShellMMInfoLink(context.getHistoryContextName());
        return p;
    }

    private SpsWidget createShellMMInfo(Context context, EditWidgetDesc editDesc, List<ShellMMType> folderTypes) {
        final SpsWidget researchSymbolPicker = createResearchSymbolPicker(editDesc, false, context.getActivityInstanceId());
        if(researchSymbolPicker != null) {
            return researchSymbolPicker;
        }

        final SpsShellMMInfoPicker p = new SpsShellMMInfoPicker();
        p.setSelectSymbolFormStyle(SelectSymbolFormStyle.SYMBOL);
        p.setShellMMTypes(folderTypes);
        p.withShellMMInfoLink(context.getHistoryContextName());

        if(TextUtil.hasStyle(editDesc, "ORDER_ENTRY")) { // $NON-NLS$
            p.setSelectSymbolFormStyle(SelectSymbolFormStyle.SYMBOL_WITH_ORDER_ENTRY_AVAILABILITY);
        }

        return p;
    }

    private SpsWidget createResearchSymbolPicker(EditWidgetDesc editDesc, boolean readonly, String activityInstanceId) {
        if(TextUtil.hasStyle(editDesc, "pickResearchSymbol")) {   // $NON-NLS$
            final String layoutGuid = editDesc.getLayoutGUID();
            if(!StringUtil.hasText(layoutGuid)) {
                throw new IllegalArgumentException("Parameter LayoutGUID is mandatory for EditWidgetDesc of style \"pickResearchSymbol\"");  // $NON-NLS$
            }
            return new SpsSelectResearchSymbolPicker(layoutGuid).withVisible(!readonly)
                    .withActivityInstanceId(activityInstanceId);
        }
        return null;
    }
}
