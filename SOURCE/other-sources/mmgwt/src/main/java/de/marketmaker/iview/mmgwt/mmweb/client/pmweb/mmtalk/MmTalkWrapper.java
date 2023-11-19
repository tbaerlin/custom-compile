/*
 * MmTalkWrapper.java
 *
 * Created on 28.02.13 10:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import com.google.gwt.core.shared.GWT;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.ObjectMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PrivacyModeAllowedObjectId;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Instrument;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.InstrumentMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Market;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.SecurityAccountBalanceItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Address;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Advisor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Bank;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.CommissionScale;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Investor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.InvestorAppointment;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.MMTalkStringListEntryNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.ObjectName;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.OwnerPersonLink;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PortfolioProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PortfolioVersion;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.PortfolioVersionListItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Prospect;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer.FolderItem;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTableRow;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ParameterSimple;
import de.marketmaker.iview.pmxml.QueryResponseState;
import de.marketmaker.iview.pmxml.SimpleMM;
import de.marketmaker.iview.pmxml.TableTreeTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Lösch
 */
public class MmTalkWrapper<O> {
    private final List<MmTalkColumnMapper<O>> columnMappers = new ArrayList<>();
    private final List<MmTalkNodeMapper> nodeMappers = new ArrayList<>();
    private final Formula formula;
    private final Class<O> clazz;
    private final TableTreeTable node;


    public static <T> MmTalkWrapper<T> create(Formula formula, Class<T> clazz) {
        return new MmTalkWrapper<>(formula, clazz);
    }

    public static <T> MmTalkWrapper<T> create(String formula, Class<T> clazz) {
        return new MmTalkWrapper<>(Formula.create(formula), clazz);
    }

    private MmTalkWrapper(Formula formula, Class<O> clazz) {
        if (formula != null && (formula.getFormula().endsWith(".") || formula.getFormula().startsWith("."))) {
            throw new IllegalArgumentException("formula must not start or end with a dot '.'."); // $NON-NLS$
        }
        this.formula = formula;
        this.clazz = clazz;
        this.node = new TableTreeTable();
        this.node.setFormula(this.formula != null
                ? this.formula.getFormula()
                : null);
    }

    public MmTalkWrapper<O> appendColumnMapper(MmTalkColumnMapper<O> mapper) {
        this.columnMappers.add(mapper);
        return this;
    }

    public List<O> createResultObjectList(MMTalkResponse response) {
        if(response == null || response.getQueryResponseState() != QueryResponseState.QRS_OK) {
            return Collections.emptyList();
        }
        return createResultObjectList(response.getData());
    }

    public O createResultObject(MMTable table) {
        final List<O> result = createResultObjectList(table, true);
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public List<O> createResultObjectList(MMTable table) {
        return createResultObjectList(table, false);
    }

    private List<O> createResultObjectList(MMTable table, boolean onlyFirstOne) {
        final List<O> result = new ArrayList<>();
        final List<MMTableRow> rows = table.getRow();
        for (MMTableRow row : rows) {
            result.add(createResultObject(row.getCell()));
            if (onlyFirstOne && !result.isEmpty()) {
                return result;
            }
        }
        return result;
    }

    public O createResultObject(List<MM> cells) {
        final O o = createObject();
        int i = 0;

        final boolean given = MmTalkHelper.isTrue(cells.get(i));
        if (!given) {
            return null;
        }
        i++;

        for (MmTalkColumnMapper<O> columnMapper : this.columnMappers) {
            columnMapper.setValue(o, cells.get(i));
            i++;
        }

        for (MmTalkNodeMapper nodeMapper : nodeMappers) {
            final MM dataItem = cells.get(i);
            if (!(dataItem instanceof MMTable)) {
                throw new IllegalStateException("dataItem is not an instance of MMTable at position " + i); // $NON-NLS$
            }
            //noinspection unchecked
            nodeMapper.setValue(o, nodeMapper.getWrapper(), (MMTable) dataItem);
            i++;
        }
        return o;
    }

    private O createObject() {
        final O o = MmTalkHelper.getClientServerProxy().getClassInstance(this.clazz);
        if (o != null) {
            return o;
        }
        // unfortunately, gwt is not able to do something like GWT.create(this.clazz), so...
        if (this.clazz == Address.class) {
            return GWT.create(Address.class);
        }
        if (this.clazz == Account.class) {
            return GWT.create(Account.class);
        }
        if (this.clazz == Bank.class) {
            return GWT.create(Bank.class);
        }
        if (this.clazz == CommissionScale.class) {
            return GWT.create(CommissionScale.class);
        }
        if (this.clazz == Depot.class) {
            return GWT.create(Depot.class);
        }
        if (this.clazz == DmsMetadata.class) {
            return GWT.create(DmsMetadata.class);
        }
        if (this.clazz == FolderItem.class) {
            return GWT.create(FolderItem.class);
        }
        if (this.clazz == Instrument.class) {
            return GWT.create(Instrument.class);
        }
        if (this.clazz == InstrumentMetadata.class) {
            return GWT.create(InstrumentMetadata.class);
        }
        if (this.clazz == Investor.class) {
            return GWT.create(Investor.class);
        }
        if (this.clazz == InvestorAppointment.class) {
            return GWT.create(InvestorAppointment.class);
        }
        if(this.clazz == Prospect.class) {
            return GWT.create(Prospect.class);
        }
        if (this.clazz == ObjectName.class) {
            return GWT.create(ObjectName.class);
        }
        if (this.clazz == ObjectMetadata.class) {
            return GWT.create(ObjectMetadata.class);
        }
        if (this.clazz == Portfolio.class) {
            return GWT.create(Portfolio.class);
        }
        if (this.clazz == PortfolioVersion.class) {
            return GWT.create(PortfolioVersion.class);
        }
        if (this.clazz == PortfolioProfile.class) {
            return GWT.create(PortfolioProfile.class);
        }
        if (this.clazz == Advisor.class) {
            return GWT.create(Advisor.class);
        }
        if (this.clazz == Market.class) {
            return GWT.create(Market.class);
        }
        if (this.clazz == MMTalkStringListEntryNode.class) {
            return GWT.create(MMTalkStringListEntryNode.class);
        }
        if (this.clazz == PortfolioVersionListItem.class) {
            return GWT.create(PortfolioVersionListItem.class);
        }
        if(this.clazz == UserDefinedFields.class) {
            return GWT.create(UserDefinedFields.class);
        }
        if(this.clazz == HistoryItem.class) {
            return GWT.create(HistoryItem.class);
        }
        if(this.clazz == Person.class) {
            return GWT.create(Person.class);
        }
        if(this.clazz == OwnerPersonLink.class) {
            return GWT.create(OwnerPersonLink.class);
        }
        if(this.clazz == SecurityAccountBalanceItem.class) {
            return GWT.create(SecurityAccountBalanceItem.class);
        }
        if(this.clazz == PrivacyModeAllowedObjectId.class) {
            return GWT.create(PrivacyModeAllowedObjectId.class);
        }

        throw new IllegalArgumentException(this.clazz.getName() + " is not known by " + getClass() + // $NON-NLS$
                " as a valid class. You need to handle it in " + getClass().getName() + "."); // $NON-NLS$
    }

    private String[] getColumnFormulas() {
        final String[] result = new String[getColumFormulaCount()];
        int i = 0;

        result[i] = "Given"; // $NON-NLS$
        i++;

        for (MmTalkColumnMapper<O> columnMapper : this.columnMappers) {
            result[i] = columnMapper.getFormula();
            i++;
        }

        return result;
    }


    private int getColumFormulaCount() {
        return this.columnMappers.size() + 1; // +1 for the given check
    }

    public <T> MmTalkWrapper<O> appendNodeMapper(MmTalkNodeMapper<O, T> nodeMapper) {
        this.nodeMappers.add(nodeMapper);
        return this;
    }

    public TableTreeTable getNode() {
        MmTalkHelper.addColumnFormulas(this.node, getColumnFormulas());
        for (MmTalkNodeMapper nodeMapper : this.nodeMappers) {
            MmTalkHelper.addNode(this.node, nodeMapper.getWrapper().getNode());
        }
        return this.node;
    }

    private void addParameters(List<ParameterSimple> params) {
        if (this.formula != null) {
            params.addAll(this.formula.getMmTalkParams());
        }
        for (MmTalkNodeMapper nodeMapper : this.nodeMappers) {
            nodeMapper.getWrapper().addParameters(params);
        }
    }

    public List<ParameterSimple> getParameters() {
        final ArrayList<ParameterSimple> result = new ArrayList<>();
        addParameters(result);
        return result;
    }

    public void updateParameter(String name, SimpleMM value) {
        if (this.formula != null) {
            this.formula.updateParameter(name, value);
        }
        for (MmTalkNodeMapper nodeMapper : this.nodeMappers) {
            nodeMapper.getWrapper().updateParameter(name, value);
        }
    }

    public Formula getFormula() {
        return this.formula;
    }
}