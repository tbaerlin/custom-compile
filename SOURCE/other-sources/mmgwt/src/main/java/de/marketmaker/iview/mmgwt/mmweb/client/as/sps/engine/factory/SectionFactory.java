package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsColumnSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLayoutSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionDescColumn;
import de.marketmaker.iview.pmxml.SectionListDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;

import java.util.List;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public class SectionFactory extends Factory<SectionDesc> {
    public static final String[] FORM_COLUMN_STYLES = new String[]{"sps-form-caption", "sps-form-field"}; // $NON-NLS$

    public SectionFactory(String baseClass) {
        super(baseClass);
    }

    @Override
    SpsWidget doCreateSpsWidget(final SectionDesc sectionDesc, final Context context, BindToken parentToken) {
        final SpsWidget section;
        final Integer spsFormColumnCount = CssUtil.getStyleValueInt(sectionDesc.getStyle(), "sps-form"); // $NON-NLS$

        if (TextUtil.hasStyle(sectionDesc, "SingleAnalysis")) { // $NON-NLS$
            section = new SpsLayoutSection();
        }
        else if (sectionDesc.getColumns() != null && !sectionDesc.getColumns().isEmpty() && !SessionData.INSTANCE.isUserPropertyTrue("noColumns")) { // $NON-NLS$
            section = new SpsColumnSection(getColumnStyles(sectionDesc.getColumns()));
        }
        else if (TextUtil.hasStyle(sectionDesc, "sps-flow")) { // $NON-NLS$
            section = new SpsColumnSection(FORM_COLUMN_STYLES).withFormContainer(false);
        }
        else if (spsFormColumnCount != null) {
            final String[] columnStyles = new String[spsFormColumnCount * 2];
            for (int i = 0; i < spsFormColumnCount; i++) {
                columnStyles[i * 2] = FORM_COLUMN_STYLES[0];
                columnStyles[i * 2 + 1] = FORM_COLUMN_STYLES[1];
            }
            section = new SpsColumnSection(columnStyles).withFormContainer(true);
        }
        else if (isForm(sectionDesc)) {
            section = new SpsColumnSection(FORM_COLUMN_STYLES).withFormContainer(true);
        }
        else {
            final BindToken bindToken = StringUtil.hasText(sectionDesc.getBind())
                    ? parentToken.append(sectionDesc.getBind())
                    : null;

            if (bindToken != null && context.isList(bindToken)) {
                //TODO: this usage of SectionDesc deprecated. The preferred way is to use a SectionListDesc
                //TODO: remove this block if Anlageplanungsliste uses the new SectionListDesc
                section = SectionListFactory.createSpsListSection(sectionDesc, context, bindToken)
                        .withListEntryCaption(sectionDesc.getCaption());
                section.setDescription(sectionDesc.getDescription());
                section.setDescriptionIcon(sectionDesc.getDescriptionIcon());
                return section;
            } else {
                section = new SpsSection();
            }
        }

        section.setCaption(sectionDesc.getCaption());
        section.setDescription(sectionDesc.getDescription());
        section.setDescriptionIcon(sectionDesc.getDescriptionIcon());
        return section;
    }

    static boolean isForm(SectionDesc sectionDesc) {
        final String style = sectionDesc.getStyle();
        if (StringUtil.hasText(style)) {
            return TextUtil.hasStyle(sectionDesc, "sps-form") || TextUtil.hasStyle(sectionDesc, "sps-smallForm"); // $NON-NLS$
        }
        for (WidgetDesc widgetDesc : sectionDesc.getItems()) {
            if (triggersForm(widgetDesc)) {
                sectionDesc.setStyle("sps-form"); // $NON-NLS$
                return true;
            }
        }
        return false;
    }

    private static boolean triggersForm(WidgetDesc wd) {
        return !(wd instanceof SectionDesc)
                && !(wd instanceof LabelWidgetDesc)
                && !(wd instanceof SectionListDesc);
    }

    private String[] getColumnStyles(List<SectionDescColumn> columns) {
        final String[] colStyles = new String[columns.size()];
        for (int i = 0; i < colStyles.length; i++) {
            colStyles[i] = columns.get(i).getStyle();
        }
        return colStyles;
    }
}
