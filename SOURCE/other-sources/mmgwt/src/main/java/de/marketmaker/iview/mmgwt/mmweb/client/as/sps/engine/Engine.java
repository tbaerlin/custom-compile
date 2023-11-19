package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.AnalysisFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.EditFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.EditWithObjectFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.Factory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.LabelFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.ListFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.ProgressControlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.SectionFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.SectionListFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AnalysisControlDesc;
import de.marketmaker.iview.pmxml.BoundWidgetDesc;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.EditWidgetWithObjectDesc;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.ProgressControlDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionListDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public class Engine {
    private final Map<Class<? extends WidgetDesc>, Factory> descFac = new HashMap<>();
    private Context context;

    public Engine(Context context) {
        this.context = context;
        this.descFac.put(SectionDesc.class, new SectionFactory("section")); // $NON-NLS$
        this.descFac.put(SectionListDesc.class, new SectionListFactory("sectionList")); // $NON-NLS$
        this.descFac.put(EditWidgetDesc.class, new EditFactory("edit")); // $NON-NLS$
        this.descFac.put(EditWidgetWithObjectDesc.class, new EditWithObjectFactory("editWithObject")); // $NON-NLS$
        this.descFac.put(ListWidgetDesc.class, new ListFactory("list")); // $NON-NLS$
        this.descFac.put(AnalysisControlDesc.class, new AnalysisFactory("analysis")); // $NON-NLS$
        this.descFac.put(LabelWidgetDesc.class, new LabelFactory("label")); // $NON-NLS$
        this.descFac.put(ProgressControlDesc.class, new ProgressControlFactory("progressControl")); // $NON-NLS$
    }

    public SpsWidget createSpsWidget(WidgetDesc widgetDesc) {
        return createSpsWidget(widgetDesc, BindToken.EMPTY_ROOT_TOKEN, 0, false);
    }

    private SpsWidget createSpsWidget(WidgetDesc widgetDesc, BindToken parentToken, int level, boolean parentIsForm) {
        final Factory factory = this.descFac.get(widgetDesc.getClass());
        if (factory == null) {
            return null;
        }
        final SpsWidget spsWidget = factory.createSpsWidget(widgetDesc, this.context, parentToken);
        spsWidget.setLevel(level);
        if (parentIsForm && spsWidget.getColSpan() == 1) {
            spsWidget.forceCaptionWidget();
        }

        if (!(widgetDesc instanceof SectionDesc)
                || ((SectionDesc) widgetDesc).getItems().isEmpty()
                || (!(spsWidget instanceof HasChildrenFeature))) {
            spsWidget.onWidgetConfigured();
            return spsWidget;
        }

        // widget is a section
        BindToken token = parentToken;
        if (isBound(widgetDesc) && (spsWidget instanceof HasBindFeature)) {
            token = ((HasBindFeature) spsWidget).getBindFeature().getSpsProperty().getBindToken();
        }

        final SectionDesc sectionDesc = (SectionDesc)widgetDesc;
        final SectionDesc fixedNorthSectionDesc = findFixedSectionDesc(sectionDesc, level);
        if(fixedNorthSectionDesc != null) {
            final SpsWidget spsFixedSection = createSpsWidgets(Collections.singletonList((WidgetDesc) fixedNorthSectionDesc), token, false, level + 1).get(0);
            ((HasFixedNorthWidget)spsWidget).setFixedNorthWidget(spsFixedSection);
            sectionDesc.getItems().remove(fixedNorthSectionDesc);
        }

        if(!sectionDesc.getItems().isEmpty()) {
            ((HasChildrenFeature) spsWidget).getChildrenFeature().addChildren(
                    createSpsWidgets(sectionDesc.getItems(), token, ((HasChildrenFeature) spsWidget).isFormContainer(), level + 1)
            );
        }
        spsWidget.onWidgetConfigured();
        return spsWidget;
    }

    private SectionDesc findFixedSectionDesc(SectionDesc sectionDesc, int level) {
        if(level > 0) {
            // as negotiated with PM Core, only the root section may contain a section that should act as the fixed
            // section
            return null;
        }

        for (WidgetDesc childWidgetDesc : (sectionDesc).getItems()) {
            if(childWidgetDesc instanceof SectionDesc && ((SectionDesc)childWidgetDesc).isIsFixedSection()) {
                return (SectionDesc)childWidgetDesc;
            }
        }

        return null;
    }

    public static boolean isBound(WidgetDesc widgetDesc) {
        return (widgetDesc instanceof BoundWidgetDesc && StringUtil.hasText(((BoundWidgetDesc) widgetDesc).getBind()));
    }

    public List<SpsWidget> createSpsWidgets(List<WidgetDesc> items, BindToken parentToken, boolean parentIsForm, int level) {
        final List<SpsWidget> widgets = new ArrayList<>();
        for (WidgetDesc item : items) {
            final SpsWidget spsWidget = createSpsWidget(item, parentToken, level, parentIsForm);
            if (spsWidget != null) {
                widgets.add(spsWidget);
            }
        }
        return widgets;
    }

}