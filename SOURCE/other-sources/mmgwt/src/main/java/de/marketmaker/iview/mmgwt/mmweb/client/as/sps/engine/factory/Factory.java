package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasViewStateFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.ViewStateFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.DependencyCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.DependencyFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Engine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasBindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.IndicateStaleDataModificationCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.SetBoundDataToNullModificationCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.SpsLabelWidgetIconNameModificationCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.SpsWidgetModificationCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency.VisibilityModificationCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.BoundWidgetDesc;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.pmxml.WidgetDescDependency;
import de.marketmaker.iview.pmxml.WidgetInputAction;

import java.util.Map;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public abstract class Factory<WD extends WidgetDesc> {
    public static final String STYLE_PREFIX = "sps-"; // $NON-NLS$
    private final String baseClass;

    protected Factory(String baseClass) {
        this.baseClass = baseClass;
    }

    public <WW extends WD> SpsWidget createSpsWidget(WW widgetDesc, Context context, BindToken parentToken) {
        final SpsWidget spsWidget = doCreateSpsWidget(widgetDesc, context, parentToken);
        final String descId = widgetDesc.getId();
        spsWidget.setDescId(descId);
        spsWidget.setBaseStyle(STYLE_PREFIX + this.baseClass);
        //TODO spsWidget.setCellClass(widgetDesc.getCellStyle());
        spsWidget.setStyle(widgetDesc.getStyle());
        //TODO spsWidget.setTooltip(widgetDesc.getToolTip());

        applyDependencies(widgetDesc, context, spsWidget, parentToken); //does not depend on BoundWidgetDesc

        if(StringUtil.hasText(descId) && spsWidget instanceof HasViewStateFeature) {
            final ViewStateFeature viewStateFeature = ((HasViewStateFeature) spsWidget).getViewStateFeature();
            viewStateFeature.setStateKey(descId);
            context.registerViewStateFeature(viewStateFeature);

            final Map<String, String> viewStateForSpsWidget = context.getViewStateForSpsWidget(viewStateFeature.getStateKey());
            if(viewStateForSpsWidget != null) {
                viewStateFeature.loadState(viewStateForSpsWidget);
            }
        }

        if (!Engine.isBound(widgetDesc)) {
            return spsWidget;
        }
        final BindToken bindToken = BindToken.create(parentToken, ((BoundWidgetDesc) widgetDesc).getBind());
        applyMandatory(context, spsWidget, bindToken);
        applyTooltip((BoundWidgetDesc) widgetDesc, context, spsWidget, bindToken);
        ((HasBindFeature) spsWidget).getBindFeature().setContextAndTokens(context, parentToken, bindToken);
        return spsWidget;
    }

    private void applyTooltip(BoundWidgetDesc widgetDesc, Context context, SpsWidget spsWidget, BindToken bindToken) {
        final String tooltip = widgetDesc.getTooltip();
        if (StringUtil.hasText(tooltip)) {
            spsWidget.setTooltip(tooltip);
        }
        else {
            final DataContainerNode node = context.getDeclaration(bindToken);
            if (node instanceof DataContainerLeafNodeDeclaration) {
                final ParsedTypeInfo pti = ((DataContainerLeafNodeDeclaration) node).getDescription();
                spsWidget.setTooltip(pti.getDescription());
            }
        }
    }

    private void applyMandatory(Context context, SpsWidget spsWidget, BindToken bindToken) {
        final DataContainerNode node = context.getDeclaration(bindToken);
        if (node instanceof DataContainerLeafNodeDeclaration) {
            final ParsedTypeInfo pti = ((DataContainerLeafNodeDeclaration) node).getDescription();
            spsWidget.setMandatory(pti.isDemanded());
/*
            if (!pti.isDemanded()) {
                return;
            }
            final Validator error = Validator.create(
                    StringCompare.createEqual(null, bindToken, context), I18n.I.spsValidationMandatory(), true, new WidgetAction() {
                @Override
                public void doIt(SpsWidget widget, ValidationResponse response) {
                    widget.visualizeError(SpsWidget.ErrorType.MANDATORY, true);
                }
            });
            final Validator ok = Validator.create(StringCompare.createUnEqual(null, bindToken, context), new WidgetAction() {
                @Override
                public void doIt(SpsWidget widget, ValidationResponse response) {
                    widget.visualizeError(SpsWidget.ErrorType.MANDATORY, false);
                }
            });
            context.putValidator(bindToken, error);
            context.putValidator(bindToken, ok);
*/
        }
    }

    private void applyDependencies(WidgetDesc widgetDesc, Context context, SpsWidget spsWidget, BindToken parentToken) {
        if(widgetDesc == null || spsWidget == null) {
            return;
        }
        final DependencyFeature dependencyFeature = spsWidget.getDependencyFeature();

        for (WidgetDescDependency dependency : widgetDesc.getDependencies()) {
            if(dependency == null) {
                DebugUtil.showDeveloperNotification("WidgetDescDependency is null for " + widgetDesc.getId());
                continue;
            }

            final WidgetInputAction action = dependency.getAction();
            if(action == null) {
                DebugUtil.showDeveloperNotification("WidgetInputAction is null for " + widgetDesc.getId());
                continue;
            }

            final DependencyCommand dependencyCommand;
            switch(action) {
                case WIA_VISIBLE_IF_TRUE:
                    dependencyCommand = new VisibilityModificationCommand();
                    break;
                case WIA_VISIBLE_IF_FALSE:
                    dependencyCommand = new VisibilityModificationCommand().withInverted();
                    break;
                case WIA_VISIBLE_IF_REG_EXP_MATCHES:
                case WIA_VISIBLE_IF_REG_EXP_NOT_MATCHES:
                    try {
                        final VisibilityModificationCommand vmc = new VisibilityModificationCommand().withRegExp(dependency.getCondition());
                        if(dependency.getAction() == WidgetInputAction.WIA_VISIBLE_IF_REG_EXP_NOT_MATCHES) {
                            vmc.withInverted();
                        }
                        dependencyCommand = vmc;
                    }
                    catch(Exception e) {
                        DebugUtil.showDeveloperNotification("WidgetDescDependency " + action + " with bind '" + dependency.getBind() + "' condition '" + dependency.getCondition() + "' is not a valid RegExp", e);
                        continue;
                    }
                    break;
                case WIA_PM_ICON_CHECK_UNDETERMINED_IF_HAS_CHANGED:
                    dependencyCommand = new SpsLabelWidgetIconNameModificationCommand("PmIcon:CheckUndetermined"); // $NON-NLS$
                    break;
                case WIA_INDICATE_STALE_VALUE_IF_CHANGED:
                    dependencyCommand = new IndicateStaleDataModificationCommand();
                    break;
                case WIA_SET_BOUND_VALUE_TO_NULL_IF_CHANGED:
                    dependencyCommand = new SetBoundDataToNullModificationCommand();
                    break;
                default:
                    DebugUtil.showDeveloperNotification("Unrecognized WidgetInputAction: " + action);
                    continue;
            }

            /*if(dependencyCommand instanceof SpsWidgetModificationCommand) {*/  //currently this is not necessary
                ((SpsWidgetModificationCommand) dependencyCommand).setWidget(spsWidget);
            /*}*/
            final BindToken bindToken = BindToken.create(parentToken, dependency.getBind());
//            Firebug.debug("<Factory.applyDependencies> added: " + dependencyCommand.getClass().getSimpleName() + " parent: " + parentToken + " dependencyBind: " + dependency.getBind() + " result: " + bindToken);
            dependencyCommand.getBindFeature().setContextAndTokens(context, parentToken, bindToken);

            dependencyFeature.addDependencyCommand(dependencyCommand);
        }
    }

    abstract SpsWidget doCreateSpsWidget(WD widgetDesc, Context context, BindToken parentToken);
}