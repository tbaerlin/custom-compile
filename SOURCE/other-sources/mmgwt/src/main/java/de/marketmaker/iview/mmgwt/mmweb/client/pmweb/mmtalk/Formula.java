package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.pmxml.ParameterSimple;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.SimpleMM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 25.03.13 13:41
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class Formula {
    // mmtalk stuff
    private final String formula;
    private final List<ParameterSimple> mmTalkParams;

    // non-mmtalk parameters
    private List<PmWebSupport.UserFieldCategory> userFieldCategories;
    private ShellMMType shellMmType;


    public static Formula create(String formula) {
        return new Formula(formula);
    }

    public static Formula create() {
        return Formula.create("");
    }

    private Formula(String formula) {
        if (formula == null) {
            this.formula = "";
        } else {
            this.formula = formula;
        }
        this.mmTalkParams = new ArrayList<ParameterSimple>();
    }

    public Formula withMmTalkParam(String name, SimpleMM dataItem) {
        if (!this.formula.contains(name)) {
            throw new IllegalArgumentException("formula '" + this.formula + "' doesn't contain parameters name '" + name + "'!"); // $NON-NLS$
        }
        final ParameterSimple p = new ParameterSimple();
        p.setName(name);
        p.setValue(dataItem);
        this.mmTalkParams.add(p);
        return this;
    }

    public boolean updateParameter(String name, SimpleMM value) {
        for (ParameterSimple parameter : this.mmTalkParams) {
            if (parameter.getName().equals(name)) {
                parameter.setValue(value);
                return true;
            }
        }
        return false;
    }

    public String getFormula() {
        return this.formula;
    }

    public List<ParameterSimple> getMmTalkParams() {
        return this.mmTalkParams;
    }

    public Formula withUserFieldCategories(List<PmWebSupport.UserFieldCategory> categories) {
        this.userFieldCategories = categories;
        return this;
    }

    public List<PmWebSupport.UserFieldCategory> getUserFieldCategories() {
        return this.userFieldCategories;
    }

    public Formula withShellMmType(ShellMMType shellMmType) {
        this.shellMmType = shellMmType;
        return this;
    }

    public ShellMMType getShellMmType() {
        return this.shellMmType;
    }

    @Override
    public String toString() {
        return this.formula;
    }
}