package de.marketmaker.istar.merger.web.easytrade.multiplex;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Iterator;
import java.util.List;

/**
 * Created on 06.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
class TargetContext {
    private final MoleculeRequest mr;
    private List<ModelAndView> mav;
    private Iterator<ModelAndView> it;

    TargetContext(MoleculeRequest mr) {
        this.mr = new MoleculeRequest(mr);
    }

    void setMav(List<ModelAndView> mav) {
        this.mav = mav;
    }

    void addAtom(MoleculeRequest.AtomRequest atom) {
        this.mr.addAtom(atom.getId(), atom.getName(), atom.getParameterMap(), atom.getDependsOnId());
    }

    MoleculeRequest getRequest() {
        return this.mr;
    }

    ModelAndView next() {
        if (this.it == null) {
            this.it = this.mav.iterator();
        }
        return this.it.next();
    }
}

