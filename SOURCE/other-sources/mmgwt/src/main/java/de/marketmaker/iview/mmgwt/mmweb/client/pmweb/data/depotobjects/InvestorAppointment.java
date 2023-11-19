/*
 * InvestorAppointment.java
 *
 * Created on 26.04.13 14:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;

/**
 * @author Markus Dick
 */
public class InvestorAppointment {
    public static MmTalkWrapper<InvestorAppointment> createWrapper(String formula) {
        final MmTalkWrapper<InvestorAppointment> cols = MmTalkWrapper.create(formula, InvestorAppointment.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<InvestorAppointment>("Appointment") { // $NON-NLS$
            @Override
            public void setValue(InvestorAppointment ia, MM item) {
                ia.appointmentDateTime = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InvestorAppointment>("Description") { // $NON-NLS$
            @Override
            public void setValue(InvestorAppointment ia, MM item) {
                ia.description = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InvestorAppointment>("LastUpdateOn") { // $NON-NLS$
            @Override
            public void setValue(InvestorAppointment ia, MM item) {
                ia.lastUpdateOn = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InvestorAppointment>("RecordedOn") { // $NON-NLS$
            @Override
            public void setValue(InvestorAppointment ia, MM item) {
                ia.createdOn = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InvestorAppointment>("Reference") { // $NON-NLS$
            @Override
            public void setValue(InvestorAppointment ia, MM item) {
                ia.reference = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InvestorAppointment>("UserLastUpdate") { // $NON-NLS$
            @Override
            public void setValue(InvestorAppointment ia, MM item) {
                ia.userLastUpdate = asString(item);
            }
        });

        return cols;
    }

    private String appointmentDateTime;
    private String description;
    private String lastUpdateOn;
    private String createdOn;
    private String reference;
    private String userLastUpdate;

    public String getDescription() {
        return description;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getReference() {
        return reference;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvestorAppointment)) return false;

        final InvestorAppointment that = (InvestorAppointment) o;

        if (appointmentDateTime != null ? !appointmentDateTime.equals(that.appointmentDateTime) : that.appointmentDateTime != null)
            return false;
        if (createdOn != null ? !createdOn.equals(that.createdOn) : that.createdOn != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (lastUpdateOn != null ? !lastUpdateOn.equals(that.lastUpdateOn) : that.lastUpdateOn != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;
        if (userLastUpdate != null ? !userLastUpdate.equals(that.userLastUpdate) : that.userLastUpdate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appointmentDateTime != null ? appointmentDateTime.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (lastUpdateOn != null ? lastUpdateOn.hashCode() : 0);
        result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + (userLastUpdate != null ? userLastUpdate.hashCode() : 0);
        return result;
    }
}
