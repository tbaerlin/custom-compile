/*
 * Investor.java
 *
 * Created on 18.12.12 08:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.ReportingFrequenz;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class Investor extends AbstractOwner implements ContextItem, HasUserDefinedFields {
    public static class InvestorTalker extends AbstractMmTalker<DatabaseIdQuery, Investor, Investor> {

        public InvestorTalker() {
            super("Inhaber"); // $NON-NLS$
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<Investor> createWrapper(Formula formula) {
            return AbstractOwner.withLinkedPersons(Investor.createWrapper(formula));
        }

        @Override
        public Investor createResultObject(MMTalkResponse response) {
            final List<Investor> resultObject = this.wrapper.createResultObjectList(response);
            if (!resultObject.isEmpty()) {
                return resultObject.get(0);
            }
            return null;
        }
    }

    public static MmTalkWrapper<Investor> createWrapper(Formula formula) {
        final MmTalkWrapper<Investor> cols = MmTalkWrapper.create(formula, Investor.class);
        AbstractOwner.appendMappers(cols, "Inhaber_Steuersatz");  // $NON-NLS$

        cols.appendNodeMapper(new MmTalkNodeMapper<Investor, Address>(Address.createWrapper("Referenzadresse1")) { // $NON-NLS$
            @Override
            public void setValue(Investor o, MmTalkWrapper<Address> wrapper, MMTable table) {
                o.referenceAddress1 = wrapper.createResultObject(table);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Investor, Address>(Address.createWrapper("Referenzadresse2")) { // $NON-NLS$
            @Override
            public void setValue(Investor o, MmTalkWrapper<Address> wrapper, MMTable table) {
                o.referenceAddress2 = wrapper.createResultObject(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Investor>("ReportingFrequenz") { // $NON-NLS$
            @Override
            public void setValue(Investor o, MM item) {
                o.reportingFrequency = asEnum(MMTypRefType.TRT_REPORTING_FREQUENZ, ReportingFrequenz.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Investor>("Scheduledreporting_Aktiv") { // $NON-NLS$
            @Override
            public void setValue(Investor o, MM item) {
                o.scheduledReportingActive = asBoolean(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Investor>("Scheduledreporting_ProfileName") { // $NON-NLS$
            @Override
            public void setValue(Investor o, MM item) {
                o.reportingProfile = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Investor>("Inhaber_Letzte_ScheduledReportingBenachrichtigung") { // $NON-NLS$
            @Override
            public void setValue(Investor o, MM item) {
                o.scheduledReportingLastNotification = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Investor>("PerformanceStartDate") { // $NON-NLS$
            @Override
            public void setValue(Investor o, MM item) {
                o.performanceCalculationFromDate = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Investor, InvestorAppointment>(InvestorAppointment.createWrapper("HolderAppointments")) {  //$NON-NLS$
            @Override
            public void setValue(Investor o, MmTalkWrapper<InvestorAppointment> wrapper, MMTable table) {
                o.investorAppointments = wrapper.createResultObjectList(table);
            }
        });
        return cols;
    }

    private Address referenceAddress1;
    private Address referenceAddress2;
    private ReportingFrequenz reportingFrequency;
    private Boolean scheduledReportingActive;
    private String reportingProfile;
    private String scheduledReportingLastNotification;
    private String performanceCalculationFromDate;
    private List<InvestorAppointment> investorAppointments;

    private UserDefinedFields userDefinedFields;

    private Investor() {
    }

    public Address getReferenceAddress1() {
        return referenceAddress1;
    }

    public Address getReferenceAddress2() {
        return referenceAddress2;
    }

    public ReportingFrequenz getReportingFrequency() {
        return reportingFrequency;
    }

    public Boolean isScheduledReportingActive() {
        return scheduledReportingActive;
    }

    public String getReportingProfile() {
        return reportingProfile;
    }

    public String getScheduledReportingLastNotification() {
        return scheduledReportingLastNotification;
    }

    public UserDefinedFields getUserDefinedFields() {
        return userDefinedFields;
    }

    public void setUserDefinedFields(UserDefinedFields userDefinedFields) {
        this.userDefinedFields = userDefinedFields;
    }

    @Override
    public ShellMMType getShellMMType() {
        return ShellMMType.ST_INHABER;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Investor)) return false;

        final Investor investor = (Investor) o;

        if (investorAppointments != null ? !investorAppointments.equals(investor.investorAppointments) : investor.investorAppointments != null)
            return false;
        if (performanceCalculationFromDate != null ? !performanceCalculationFromDate.equals(investor.performanceCalculationFromDate) : investor.performanceCalculationFromDate != null)
            return false;
        if (referenceAddress1 != null ? !referenceAddress1.equals(investor.referenceAddress1) : investor.referenceAddress1 != null)
            return false;
        if (referenceAddress2 != null ? !referenceAddress2.equals(investor.referenceAddress2) : investor.referenceAddress2 != null)
            return false;
        if (reportingFrequency != investor.reportingFrequency) return false;
        if (reportingProfile != null ? !reportingProfile.equals(investor.reportingProfile) : investor.reportingProfile != null)
            return false;
        if (scheduledReportingActive != null ? !scheduledReportingActive.equals(investor.scheduledReportingActive) : investor.scheduledReportingActive != null)
            return false;
        if (scheduledReportingLastNotification != null ? !scheduledReportingLastNotification.equals(investor.scheduledReportingLastNotification) : investor.scheduledReportingLastNotification != null)
            return false;
        if (userDefinedFields != null ? !userDefinedFields.equals(investor.userDefinedFields) : investor.userDefinedFields != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = referenceAddress1 != null ? referenceAddress1.hashCode() : 0;
        result = 31 * result + (referenceAddress2 != null ? referenceAddress2.hashCode() : 0);
        result = 31 * result + (reportingFrequency != null ? reportingFrequency.hashCode() : 0);
        result = 31 * result + (scheduledReportingActive != null ? scheduledReportingActive.hashCode() : 0);
        result = 31 * result + (reportingProfile != null ? reportingProfile.hashCode() : 0);
        result = 31 * result + (scheduledReportingLastNotification != null ? scheduledReportingLastNotification.hashCode() : 0);
        result = 31 * result + (performanceCalculationFromDate != null ? performanceCalculationFromDate.hashCode() : 0);
        result = 31 * result + (investorAppointments != null ? investorAppointments.hashCode() : 0);
        result = 31 * result + (userDefinedFields != null ? userDefinedFields.hashCode() : 0);
        return result;
    }
}