/*
 * SectionListFactory.java
 *
 * Created on 30.07.2014 14:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCompositeProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListActions;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SpsSectionList;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.panel.SectionListPanelView;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.table.SectionListTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionListDesc;

import java.util.Collection;
import java.util.List;

/**
 * @author mdick
 */
public class SectionListFactory extends Factory<SectionListDesc> {
    public SectionListFactory(String baseClass) {
        super(baseClass);
    }

    //TODO: remove this block if Anlageplanungsliste uses the new SectionListDesc
    static SpsSectionList createSpsListSection(final SectionDesc sectionDesc, final Context context, final BindToken bindToken) {
        return createSpsListSection(sectionDesc, null, context, bindToken, false, false, null);
    }

    private static SpsSectionList createSpsListSection(SectionDesc editSection, SectionDesc footerSection, final Context context, final BindToken bindToken, final boolean useTableLayout, boolean collapsible, BindToken footerBindToken) {
        final boolean readonly = editSection.isIsReadonly() || context.isForceReadonly();

        final SpsProperty spsProperty = context.getRootProp().get(bindToken.getHead());
        final SpsListActions actions;

        if(spsProperty instanceof SpsListProperty) {
            actions = createListActionsForList(editSection, footerSection, context, bindToken, readonly, (SpsListProperty)spsProperty, footerBindToken);
        }
        else if(spsProperty instanceof SpsGroupProperty) {
            actions = createListActionsForGroup(editSection, footerSection, context, footerBindToken);
        }
        else {
            throw new IllegalArgumentException("Bound property is not of type SpsListProperty or SpsGroupProperty");  // $NON-NLS$
        }

        final SpsSectionList w = useTableLayout
            ? new SpsSectionList(actions, readonly, new SectionListTableView())
            : new SpsSectionList(actions, readonly, new SectionListPanelView());
        w.setCollapsible(collapsible);
        if(footerSection != null) {
            w.setFooterCaption(footerSection.getCaption());
        }

        return w;
    }

    private static SpsListActions createListActionsForList(final SectionDesc editSectionDesc, final SectionDesc footerSectionDesc, final Context context, final BindToken bindToken, final boolean readonly, final SpsListProperty spsListProperty, final BindToken footerBindToken) {
        return new SpsListActions() {
            @Override
            public void deleteProperty(SpsProperty p) {
                if (!readonly) {
                    removeValidators(context, p);
                    spsListProperty.remove(p, true, true);
                }
            }

            @Override
            public void addProperty(boolean indicateChanged) {
                if (!readonly) {
                    final DataContainerListNode sectionDecl = (DataContainerListNode) context.getDeclaration(bindToken);
                    final DataContainerNode childDecl = sectionDecl.getChildren().get(0);
                    final SpsProperty p = Context.createProperties(childDecl, childDecl, spsListProperty);
                    spsListProperty.add(p, indicateChanged, true);
                }
            }

            @Override
            public void deleteAllAndAddProperty() {
                if (!readonly) {
                    for (SpsProperty child : spsListProperty.getChildren()) {
                        removeValidators(context, child);
                    }
                    final DataContainerListNode sectionDecl = (DataContainerListNode) context.getDeclaration(bindToken);
                    final DataContainerNode childDecl = sectionDecl.getChildren().get(0);
                    final SpsProperty p = Context.createProperties(childDecl, childDecl, spsListProperty);
                    spsListProperty.clearAndAdd(p, true);
                }
            }

            @Override
            public List<SpsWidget> createWidgets(SpsProperty p, int level) {
                final BindToken listElementToken = p.getBindToken();
                return context.getEngine().createSpsWidgets(editSectionDesc.getItems(), listElementToken, SectionFactory.isForm(editSectionDesc), level);
            }

            @Override
            public List<SpsWidget> createFooterWidgets(int level) {
                if(footerSectionDesc == null) {
                    return null;
                }
                return context.getEngine().createSpsWidgets(footerSectionDesc.getItems(), footerBindToken, SectionFactory.isForm(footerSectionDesc), level);
            }

            @Override
            public SpsProperty createDefaultCompareProperty() {
                if(readonly) {
                    return null;
                }

                final DataContainerListNode sectionDecl = (DataContainerListNode) context.getDeclaration(bindToken);
                final DataContainerNode childDecl = sectionDecl.getChildren().get(0);
                return Context.createProperties(childDecl, childDecl, null); //parent is not necessary
            }
        };
    }

    private static SpsListActions createListActionsForGroup(final SectionDesc editSectionDesc, final SectionDesc footerSectionDesc, final Context context, final BindToken footerBindToken) {
        return new SpsListActions() {
            @Override
            public void deleteProperty(SpsProperty p) {
                throw new IllegalArgumentException("deleteProperty called, but bound property is of type SpsGroupProperty");  // $NON-NLS$
            }

            @Override
            public void addProperty(boolean indicateChanged) {
                throw new IllegalArgumentException("addProperty called, but bound property is of type SpsGroupProperty");  // $NON-NLS$
            }

            @Override
            public void deleteAllAndAddProperty() {
                throw new IllegalArgumentException("deleteAllAndAddProperty called, but bound property is of type SpsGroupProperty");  // $NON-NLS$
            }

            @Override
            public List<SpsWidget> createWidgets(SpsProperty p, int level) {
                final BindToken groupElementToken = p.getBindToken();
                return context.getEngine().createSpsWidgets(editSectionDesc.getItems(), groupElementToken, SectionFactory.isForm(editSectionDesc), level);
            }

            @Override
            public List<SpsWidget> createFooterWidgets(int level) {
                if(footerSectionDesc == null) {
                    return null;
                }
                return context.getEngine().createSpsWidgets(footerSectionDesc.getItems(), footerBindToken, SectionFactory.isForm(footerSectionDesc), level);
            }

            @Override
            public SpsProperty createDefaultCompareProperty() {
//                throw new IllegalArgumentException("createDefaultCompareProperty called, but bound property is of type SpsGroupProperty");  // $NON-NLS$
                return null;
            }
        };
    }

    private static void removeValidators(Context context, SpsProperty p) {
        if (p instanceof SpsCompositeProperty) {
            final Collection<SpsProperty> children = ((SpsCompositeProperty) p).getChildren();
            for (SpsProperty child : children) {
                removeValidators(context, child);
            }
        }
        context.removeValidator(p.getBindToken());
    }

    @Override
    SpsWidget doCreateSpsWidget(SectionListDesc widgetDesc, Context context, BindToken parentToken) {
        final SpsSectionList spsWidget;

        final BindToken bindToken = StringUtil.hasText(widgetDesc.getBind())
                ? parentToken.append(widgetDesc.getBind())
                : null;

        if (bindToken != null && (context.isList(bindToken) || context.isGroup(bindToken))) {
            final SectionDesc editSection = widgetDesc.getEditSection();
            final SectionDesc footerSection = widgetDesc.getFooterSection();
            if (widgetDesc.isIsReadonly() || context.isForceReadonly() || context.isGroup(bindToken)) {
                editSection.setIsReadonly(true);
                if(footerSection != null) {
                    footerSection.setIsReadonly(true);
                }
            }

            final BindToken footerBindToken = footerSection != null && StringUtil.hasText(footerSection.getBind())
                    ? parentToken.append(footerSection.getBind())
                    : null;

            spsWidget = createSpsListSection(editSection, footerSection, context, bindToken, TextUtil.hasStyle(widgetDesc, "tableLayout"), TextUtil.hasStyle(widgetDesc, "sps-collapsible"), footerBindToken)  // $NON-NLS$
                    .withAddButtonTooltip(widgetDesc.getTooltipAdd())
                    .withDeleteButtonTooltip(widgetDesc.getTooltipDelete())
                    .withListEntryCaption(editSection.getCaption())
                    .withGroupOrder(MmTalkHelper.toStringList(widgetDesc.getGroupOrder()));

            if(widgetDesc.isRemoveDefaultListEntries()) {
                spsWidget.withRemoveDefaultListEntries();
            }
        }
        else {
            throw new IllegalArgumentException("SectionListFactory: bind token not set or not bound property is neither a list nor a group");  // $NON-NLS$
        }

        spsWidget.setCaption(widgetDesc.getCaption());
        spsWidget.setDescription(widgetDesc.getDescription());
        spsWidget.setDescriptionIcon(widgetDesc.getDescriptionIcon());
        return spsWidget;
    }
}
