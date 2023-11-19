/*
 * PmRenderers.java
 *
 * Created on 20.03.13 13:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Address;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.RendererPipe;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.Familienstand;
import de.marketmaker.iview.pmxml.Geschlecht;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMBool;
import de.marketmaker.iview.pmxml.MMDBRef;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.MMDistribution;
import de.marketmaker.iview.pmxml.MMDistributionValueProcessed;
import de.marketmaker.iview.pmxml.MMNumber;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.MMTypRef;
import de.marketmaker.iview.pmxml.OwnerPersonLinkType;
import de.marketmaker.iview.pmxml.PmxmlConstants;
import de.marketmaker.iview.pmxml.ReportingFrequenz;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.SimpleMM;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.UserFieldDeclarationDesc;
import de.marketmaker.iview.pmxml.VerificationStatus;

import java.util.Date;
import java.util.List;

/**
 * Some strictly PM web (and order entry) specific renderers.
 *
 * @author Markus Dick
 */
public final class PmRenderers {
    public static final String PM_NA_ABBR = "n/a"; //$NON-NLS$

    public static final Renderer<String> NO_NA = new DoNotRenderIfContainsNA();
    public static final Renderer<Address> ADDRESS_FULL_NAME_WITH_SALUTATION = new AddressNameRenderer(false, true);
    public static final Renderer<Person> PERSON_FULL_NAME_WITH_SALUTATION = new PersonNameRenderer(false, true);
    public static final Renderer<Geschlecht> GESCHLECHT_SALUTATION_RENDERER = new GeschlechtSalutationRenderer();
    public static final Renderer<OwnerPersonLinkType> OWNER_PERSON_LINK_TYPE_RENDERER = new OwnerPersonLinkTypeRenderer();
    public static final Renderer<Familienstand> FAMILIENSTAND_RENDERER = new FamilienstandRenderer();
    public static final Renderer<String> DATE_TIME_STRING = new DateTimeStringRenderer(null, false, false);
    public static final Renderer<String> DATE_TIME_HHMM_STRING = new DateTimeStringRenderer(null, false, true);
    public static final Renderer<String> DATE_STRING = new DateTimeStringRenderer(null, true, false);
    public static final Renderer<ShellMMType> SHELL_MM_TYPE = new ShellMMTypeRenderer(false);
    public static final Renderer<ShellMMType> SHELL_MM_TYPE_PLURAL = new ShellMMTypeRenderer(true);
    public static final Renderer<VerificationStatus> DATA_STATUS = new VerificationStatusRenderer();
    public static final Renderer<ReportingFrequenz> REPORTING_FREQUENCY = new ReportingFrequenzRenderer();
    public static final DataStatusLabelRenderer DATA_STATUS_LABEL = new DataStatusLabelRenderer();
    public static final DINumberRenderer DI_NUMBER_PERCENT23 = new DINumberRenderer(Renderer.PERCENT23);
    public static final Renderer<String> PORTFOLIO_VERSION_DATE =
            new RendererPipe<>(PmRenderers.DATE_TIME_STRING, new StringRenderer(I18n.I.noVersionDate()));
    public static final Renderer<DocumentMetadata> DMS_POSTBOX_TIMESTAMP_RENDERER = new DmsPostboxTimestampRenderer();
    public static final Renderer<DocumentMetadata> DMS_POSTBOX_YES_NO_RENDERER = new DmsPostboxYesNoRenderer();

    private PmRenderers() {
        /* do nothing */
    }

    private static class DoNotRenderIfContainsNA implements Renderer<String> {
        @Override
        public String render(String s) {
            if (!StringUtil.hasText(s)) {
                return s;
            }

            if (s.contains(PM_NA_ABBR)) return ""; //$NON-NLS$

            return s;
        }
    }

    private static class DateTimeStringRenderer implements Renderer<String> {
        /**
         * @link https://developers.google.com/apps-script/reference/ui/date-time-format?hl=en
         */
        private final DateTimeFormat.PredefinedFormat format;

        private final boolean dateOnly;
        private final boolean timeHHMM;

        private DateTimeStringRenderer(DateTimeFormat.PredefinedFormat format, boolean dateOnly, boolean timeHHMM) {
            this.format = format;
            this.dateOnly = dateOnly;
            this.timeHHMM = timeHHMM;
        }

        @Override
        public String render(String s) {
            return renderDateTime(s);
        }

        private String renderDateTime(String dateString) {
            if (StringUtil.hasText(dateString)) {
                try {
                    final Date date = Formatter.PM_DATE_TIME_FORMAT_MMTALK.parse(dateString);

                    if (PmxmlConstants.ZERO_DATE.equals(date)) {
                        return "";
                    }

                    if (this.format == null) {
                        if (this.dateOnly) {
                            return Formatter.LF.formatDate(date);
                        }
                        if (this.timeHHMM) {
                            return Formatter.formatDateTimeHHMM(date);
                        }

                        return Formatter.formatDateTime(date);
                    }

                    final DateTimeFormat dtf = DateTimeFormat.getFormat(this.format);
                    return dtf.format(date);
                }
                catch (Exception e) {
                    Firebug.error("cannot parse date " + dateString, e);
                }
            }
            return ""; //$NON-NLS$
        }
    }

    private abstract static class NameRenderer<T> implements Renderer<T> {
        private final boolean abbreviateMiddlenames;
        private final boolean renderSalutation;

        private NameRenderer(boolean abbreviateMiddlenames, boolean renderSalutation) {
            this.abbreviateMiddlenames = abbreviateMiddlenames;
            this.renderSalutation = renderSalutation;
        }

        protected String renderFullNameOfPerson(String salutation, String title, String firstname, String middlename, String lastname) {
            final StringBuilder sb = new StringBuilder();

            if (this.renderSalutation) {
                appendPart(salutation, sb);
            }
            appendPart(title, sb);
            appendPart(firstname, sb);
            if (this.abbreviateMiddlenames) {
                appendPart(abbreviateNames(middlename), sb);
            }
            else {
                appendPart(middlename, sb);
            }
            appendPart(lastname, sb);

            return sb.toString();
        }

        private static void appendPart(String part, StringBuilder sb) {
            if (StringUtil.hasText(part)) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(part);
            }
        }

        private static String abbreviateNames(String namesSeparatedByWhitespace) {
            if (namesSeparatedByWhitespace == null || namesSeparatedByWhitespace.trim().length() == 0) {
                return "";
            }

            final StringBuilder sb = new StringBuilder();
            final String[] names = namesSeparatedByWhitespace.split("\\s"); //$NON-NLS$

            for (String name : names) {
                final String trimmedName = name.trim();
                if (trimmedName.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(trimmedName.charAt(0)).append(".");
                }
            }

            return sb.toString();
        }
    }

    private static class PersonNameRenderer extends NameRenderer<Person> {
        private PersonNameRenderer(boolean abbreviateMiddlenames, boolean renderSalutation) {
            super(abbreviateMiddlenames, renderSalutation);
        }

        @Override
        public String render(Person p) {
            if (p == null) {
                return null;
            }

            final String salutation = GESCHLECHT_SALUTATION_RENDERER.render(p.getGender());

            String title = "";
            if (StringUtil.hasText(p.getDoctorates())) {
                title += p.getDoctorates();
            }
            if (StringUtil.hasText(p.getTitle())) {
                if (!title.isEmpty()) {
                    title += " ";
                }
                title += p.getTitle();
            }

            String lastName = "";
            if (StringUtil.hasText(p.getLastNamePrefix())) {
                lastName += p.getLastNamePrefix();
            }
            if (StringUtil.hasText(p.getLastName())) {
                if (!lastName.isEmpty()) {
                    lastName += " ";
                }
                lastName += p.getLastName();
            }
            if (StringUtil.hasText(p.getLastNameSuffix())) {
                if (!lastName.isEmpty()) {
                    lastName += " ";
                }
                lastName += p.getLastNameSuffix();
            }

            return renderFullNameOfPerson(salutation, title, p.getFirstName(), p.getMiddleName(), lastName);  //TODO: add middlenames if avail.
        }
    }

    private static class AddressNameRenderer extends NameRenderer<Address> {
        private AddressNameRenderer(boolean abbreviateMiddlenames, boolean renderSalutation) {
            super(abbreviateMiddlenames, renderSalutation);
        }

        @Override
        public String render(Address a) {
            if (a == null) {
                return null;
            }

            return renderFullNameOfPerson(a.getSalutation(), a.getTitle(), a.getFirstname(), a.getMiddlename(), a.getLastname());
        }
    }

    public static class ShellMMTypeRenderer implements Renderer<ShellMMType> {
        public final boolean usePlural;

        public ShellMMTypeRenderer(boolean usePlural) {
            this.usePlural = usePlural;
        }

        @Override
        public String render(ShellMMType shellMMType) {
            if (shellMMType == null) return null;

            final boolean plural = this.usePlural;

            switch (shellMMType) {
                case ST_AKTIE:
                    return plural ? I18n.I.stock() : I18n.I.typeStock();
                case ST_ANLEIHE:
                    return plural ? I18n.I.bonds() : I18n.I.typeBond();
                case ST_CERTIFICATE:
                    return plural ? I18n.I.certificates() : I18n.I.typeCertificate();
                case ST_FOND:
                    return plural ? I18n.I.funds() : I18n.I.typeFund();
                case ST_FUTURE:
                    return plural ? I18n.I.futures() : I18n.I.typeFuture();
                case ST_GENUSS:
                    return plural ? I18n.I.bonusShares() : I18n.I.typeBonusShare();
                case ST_INDEX:
                    return plural ? I18n.I.indices() : I18n.I.typeIndex();
                case ST_OPTION:
                    return plural ? I18n.I.options() : I18n.I.typeOption();
                case ST_OS:
                    return plural ? I18n.I.warrants() : I18n.I.typeWarrant();
                case ST_KONJUNKTURDATEN:
                    return plural ? I18n.I.economyCycleDate() : I18n.I.typeEconomyCycleDate();
                case ST_DEVISE:
                    return plural ? I18n.I.currencies1() : I18n.I.typeCurrency();
                case ST_BEZUGSRECHT:
                    return plural ? I18n.I.subscriptionRights() : I18n.I.typeSubscriptionRight();
                case ST_WP:
                    return plural ? I18n.I.instruments() : I18n.I.instrument();
                case ST_INHABER:
                    return I18n.I.pmInvestor();
                case ST_PORTFOLIO:
                    return plural ? I18n.I.pmPortfolios() : I18n.I.pmPortfolio();
                case ST_DEPOT:
                    return plural ? I18n.I.pmDepots() : I18n.I.pmDepot();
                case ST_KONTO:
                    return plural ? I18n.I.pmAccounts() : I18n.I.pmAccount();
                case ST_INTERESSENT:
                    return plural ? I18n.I.prospects() : I18n.I.prospect();
                case ST_GRUPPE:
                    return I18n.I.pmGroup();
                case ST_FILTER:
                    return I18n.I.pmFilter();
                case ST_FILTER_RESULT_FOLDER:
                    return I18n.I.pmFilterResultFolder();
                case ST_UNBEKANNT:
                    return I18n.I.typeUnknown();
                default:
                    final String value = shellMMType.value();
                    if (value != null) return value.substring(2); //return value without prefix "st"
                    return null;
            }
        }
    }

    public abstract static class AbstractDataItemRenderer<V> implements Renderer<MM> {
        private Renderer<V> renderer;

        public AbstractDataItemRenderer(Renderer<V> renderer) {
            this.renderer = renderer;
        }

        @Override
        public String render(MM di) {
            if (di == null || di instanceof DefaultMM) {
                return "";
            }
            else if (di instanceof ErrorMM) {
                return ((ErrorMM) di).getErrorString();
            }
            else {
                return renderer.render(getValue(di));
            }
        }

        protected abstract V getValue(MM di);
    }

    public static class DINumberRenderer extends AbstractDataItemRenderer<String> {
        public DINumberRenderer(Renderer<String> renderer) {
            super(renderer);
        }

        @Override
        protected String getValue(MM di) {
            return MmTalkHelper.asString(di);
        }
    }

    public static class UserFieldDataItemRenderer implements Renderer<MM> {
        private final boolean renderDiNumberInPercent;

        private static final UserFieldDataItemRenderer DEFAULT = new UserFieldDataItemRenderer(false);
        private static final UserFieldDataItemRenderer DI_NUMBER_IN_PERCENT = new UserFieldDataItemRenderer(true);

        /**
         * @param userFieldDeclarationDesc the user field declaration
         * @return a renderer, but never null!
         */
        public static Renderer<MM> get(UserFieldDeclarationDesc userFieldDeclarationDesc) {
            if (userFieldDeclarationDesc != null &&
                    userFieldDeclarationDesc.getDecl().getParsedTypeInfo().isNumberProcent()) {
                return DI_NUMBER_IN_PERCENT;
            }
            return DEFAULT;
        }

        private UserFieldDataItemRenderer(boolean renderDiNumberInPercent) {
            this.renderDiNumberInPercent = renderDiNumberInPercent;
        }

        @Override
        public String render(MM mm) {
            if (mm == null || mm instanceof DefaultMM) {
                return "";
            }
            else if (mm instanceof MMBool) {
                if (((MMBool) mm).getValue() == ThreeValueBoolean.TV_NULL) {
                    return "";
                }
                return Renderer.BOOLEAN_YES_NO_RENDERER.render(((MMBool) mm).getValue() == ThreeValueBoolean.TV_TRUE);
            }
            else if (mm instanceof MMNumber) {
                if (this.renderDiNumberInPercent) {
                    return Renderer.PERCENT23.render(((MMNumber) mm).getValue());
                }
                return Renderer.PRICE23.render(((MMNumber) mm).getValue());
            }
            else if (mm instanceof MMDBRef) {
                return ((MMDBRef) mm).getValue();
            }
            else if (mm instanceof MMTypRef) {
                return ((MMTypRef) mm).getValue();
            }
            else if (mm instanceof MMDistribution) {
                final MMDistribution dist = (MMDistribution) mm;
                final List<MMDistributionValueProcessed> elementList = dist.getContent();

                if (elementList == null || elementList.isEmpty()) {
                    return null;
                }

                final StringBuilder sb = new StringBuilder();
                for (MMDistributionValueProcessed e : elementList) {
                    if (e == null) {
                        Firebug.warn("<PmRenderers.UserFieldDataItemRenderer.render> MMDistributionValueProcessed is NULL!");
                        continue;
                    }
                    if (e.getValue() == null) {
                        Firebug.warn("<PmRenderers.UserFieldDataItemRenderer.render> MMDistributionValue is NULL!");
                        continue;
                    }
                    if (sb.length() > 0) {
                        sb.append(", "); //$NON-NLS$
                    }
                    final String weightInPercent = Renderer.PERCENT23.render(e.getNormalizedWeight());
                    sb.append(e.getValue().getDisplayName()).append("=").append(weightInPercent); //$NON-NLS$
                    sb.append(" (").append(Renderer.PRICE23.render(e.getValue().getWeight())).append(")");  // $NON-NLS$
                }
                return sb.toString();
            }
            else if (mm instanceof MMDateTime) {
                return PmRenderers.DATE_TIME_STRING.render(((MMDateTime) mm).getValue());
            }
            else if (mm instanceof SimpleMM) {
                return MmTalkHelper.asString(mm);
            }
            else if (mm instanceof ErrorMM) {
                return ((ErrorMM) mm).getErrorString();
            }

            return I18n.I.noSuitableRendererFor(mm.getClass().getName());
        }
    }

    public static class DataItemTableCellRenderer implements TableCellRenderer {
        private final TiType tiType;
        private final Renderer<MM> plainRenderer;
        private final String contentClass;

        public DataItemTableCellRenderer(UserFieldDeclarationDesc userFieldDeclarationDesc) {
            this.tiType = userFieldDeclarationDesc.getDecl().getFieldType();
            this.plainRenderer = UserFieldDataItemRenderer.get(userFieldDeclarationDesc);

            if (TiType.TI_BOOLEAN.equals(this.tiType)) {
                this.contentClass = "mm-center"; //$NON-NLS$
            }
            else if (TiType.TI_NUMBER.equals(this.tiType)) {
                this.contentClass = "mm-right"; //$NON-NLS$
            }
            else {
                this.contentClass = null;
            }
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            final String rawString = this.plainRenderer.render((MM) data);
            if (rawString != null) {
                sb.append(SafeHtmlUtils.htmlEscape(rawString));
            }
        }

        @Override
        public String getContentClass() {
            return this.contentClass;
        }

        @Override
        public boolean isPushRenderer() {
            return false;
        }
    }

    public static class VerificationStatusRenderer implements Renderer<VerificationStatus> {
        @Override
        public String render(VerificationStatus verificationStatus) {
            if (verificationStatus == null) return "";
            switch (verificationStatus) {
                case VS_VALID:
                    return I18n.I.verificationStateValid();
                case VS_INVALID:
                    return I18n.I.verificationStateInvalid();
                case VS_NA:
                    return "";
                default:
                    return verificationStatus.name();
            }
        }
    }

    public static class ReportingFrequenzRenderer implements Renderer<ReportingFrequenz> {
        @Override
        public String render(ReportingFrequenz reportingFrequenz) {
            if (reportingFrequenz == null) return "";
            switch (reportingFrequenz) {
                case RF_JAEHRLICH:
                    return I18n.I.annual();
                case RF_HALB_JAEHRLICH:
                    return I18n.I.semiannual();
                case RF_QUARTALSWEISE:
                    return I18n.I.quarterly();
                case RF_MONATLICH:
                    return I18n.I.monthly();
                case RF_NA:
                    return "";
                default:
                    return reportingFrequenz.name();
            }
        }
    }

    public static class DataStatusLabelRenderer {
        public String render(String label, String dateString) {
            final String dt = PmRenderers.DATE_TIME_STRING.render(dateString);
            if (!StringUtil.hasText(dt)) return label;
            return label + " (" + dt + ")";
        }
    }

    public static class GeschlechtSalutationRenderer implements Renderer<Geschlecht> {
        @Override
        public String render(Geschlecht g) {
            if (g == null) {
                return null;
            }

            switch (g) {
                case G_WEIBLICH:
                    return I18n.I.salutationFemale();
                case G_MAENNLICH:
                    return I18n.I.salutationMale();
                case G_NA:
                default:
                    return "";
            }
        }
    }

    public static class OwnerPersonLinkTypeRenderer implements Renderer<OwnerPersonLinkType> {
        @Override
        public String render(OwnerPersonLinkType personLinkType) {
            if (personLinkType == null) {
                return null;
            }

            switch (personLinkType) {
                case IPLT_BESITZ:
                    return I18n.I.inhaberPersonLinkTypeBESITZ();
                case IPLT_VOLLMACHT:
                    return I18n.I.inhaberPersonLinkTypeVOLLMACHT();
                case IPLT_GESETZLICHER_VERTRETER:
                    return I18n.I.inhaberPersonLinkTypeGESETZLICHER_VERTRETER();
                case IPLT_SOME_LINK_EXISTS:
                    return I18n.I.linked();
                case IPLT_NA:
                default:
                    return I18n.I.unknown();
            }
        }
    }

    public static class FamilienstandRenderer implements Renderer<Familienstand> {
        @Override
        public String render(Familienstand familienstand) {
            if (familienstand == null) {
                return null;
            }

            switch (familienstand) {
                case FS_LEDIG:
                    return I18n.I.maritalStatusLEDIG();
                case FS_VERHEIRATET:
                    return I18n.I.maritalStatusVERHEIRATET();
                case FS_GESCHIEDEN:
                    return I18n.I.maritalStatusGESCHIEDEN();
                case FS_GETRENNT_LEBEND:
                    return I18n.I.maritalStatusGETRENNT_LEBEND();
                case FS_VERWITWET:
                    return I18n.I.maritalStatusVERWITWET();
                case FS_LEBENSPARTNERSCHAFT:
                    return I18n.I.maritalStatusLEBENSPARTNERSCHAFT();
                case FS_LEBENSPARTNERSCHAFT_AUFGEHOBEN:
                    return I18n.I.maritalStatusLEBENSPARTNERSCHAFT_AUFGEHOBEN();
                case FS_LEBENSPARTNER_VERSTORBEN:
                    return I18n.I.maritalStatusLEBENSPARTNER_VERSTORBEN();
                case FS_NA:
                default:
                    return I18n.I.unknown();
            }
        }
    }

    public static abstract class AbstractDmsRenderer implements Renderer<DocumentMetadata> {
        public boolean hasUploadedPostboxDocuments(DocumentMetadata dm) {
            if(dm == null) {
                return false;
            }

            final List<MMString> list = dm.getPostboxUUIDList();
            return list != null && list.size() > 0;
        }
    }

    public static class DmsPostboxTimestampRenderer extends AbstractDmsRenderer {

        @Override
        public String render(DocumentMetadata dm) {
            if(!hasUploadedPostboxDocuments(dm)) {
                return "";
            }
            final String timestamp = PmRenderers.DATE_TIME_STRING.render(dm.getPostboxReadTimestamp());
            if(!StringUtil.hasText(timestamp)) {
                return I18n.I.postboxStatusUnread();
            }

            return timestamp;
        }
    }

    public static class DmsPostboxYesNoRenderer extends AbstractDmsRenderer {

        @Override
        public String render(DocumentMetadata dm) {
            return Renderer.BOOLEAN_YES_NO_RENDERER.render(hasUploadedPostboxDocuments(dm));
        }
    }
}
