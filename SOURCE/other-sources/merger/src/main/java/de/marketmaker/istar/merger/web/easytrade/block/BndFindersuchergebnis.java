/*
 * BndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;
import de.marketmaker.istar.common.util.ArraysUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BndFindersuchergebnis extends AbstractFindersuchergebnis {
    public static class Command extends ListCommand {
        private String[] searchstring;
        private String anleihetyp;
        private String land;
        private String waehrung;
        private String kupontyp;
        private String nominalzins;
        private String rendite;
        private String laufzeit;
        private String zinsfrequenz;
        private Boolean postbankAnleihe;

        public String[] getSearchstring() {
            return ArraysUtil.copyOf(searchstring);
        }

        public void setSearchstring(String[] searchstring) {
            this.searchstring = ArraysUtil.copyOf(searchstring);
        }

        public String getAnleihetyp() {
            return anleihetyp;
        }

        public void setAnleihetyp(String anleihetyp) {
            this.anleihetyp = anleihetyp;
        }

        public String getLand() {
            return land;
        }

        public void setLand(String land) {
            this.land = land;
        }

        public String getWaehrung() {
            return waehrung;
        }

        public void setWaehrung(String waehrung) {
            this.waehrung = waehrung;
        }

        public String getKupontyp() {
            return kupontyp;
        }

        public void setKupontyp(String kupontyp) {
            this.kupontyp = kupontyp;
        }

        public String getNominalzins() {
            return nominalzins;
        }

        public void setNominalzins(String nominalzins) {
            this.nominalzins = nominalzins;
        }

        public String getRendite() {
            return rendite;
        }

        public void setRendite(String rendite) {
            this.rendite = rendite;
        }

        public String getLaufzeit() {
            return laufzeit;
        }

        public void setLaufzeit(String laufzeit) {
            this.laufzeit = laufzeit;
        }

        public String getZinsfrequenz() {
            return zinsfrequenz;
        }

        public void setZinsfrequenz(String zinsfrequenz) {
            this.zinsfrequenz = zinsfrequenz;
        }

        public Boolean getPostbankAnleihe() {
            return postbankAnleihe;
        }

        public void setPostbankAnleihe(Boolean postbankAnleihe) {
            this.postbankAnleihe = postbankAnleihe;
        }
    }

    private static final Map<String, String> SORTFIELDS = new HashMap<>();
    static {
        SORTFIELDS.put("name", "postbankname");
        SORTFIELDS.put("kupon", "interest");
        SORTFIELDS.put("rendite", "yieldrelative_mdps");
        SORTFIELDS.put("volumen", "totalvolume");
    }

    public BndFindersuchergebnis() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.BND, cmd.getSearchstring(), null);

        addParameter(rsr, "wmbondtype", cmd.getAnleihetyp());
        addParameter(rsr, "country", cmd.getLand());
        addParameter(rsr, "currency", cmd.getWaehrung());
        addParameter(rsr, "wmcoupontype", cmd.getKupontyp());

        addParameter(rsr, "wmnominalinterest:R", cmd.getNominalzins());
        addParameter(rsr, "yieldrelative_mdps:R", cmd.getRendite());

        if (addParameter(rsr, "expires:R", cmd.getLaufzeit())) {
            rsr.addParameter("expires:ISR", "true");
        }

        addParameter(rsr, "wminterestperiod", cmd.getZinsfrequenz());

        if (cmd.getPostbankAnleihe() != null) {
            rsr.addParameter("postbankbond", cmd.getPostbankAnleihe().toString());
        }

        final ListResult listResult = ListResult.create(cmd, new ArrayList<>(SORTFIELDS.keySet()), "name", 0);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));
        rsr.addParameter("sort1", SORTFIELDS.get(listResult.getSortedBy()));
        rsr.addParameter("sort1:D", Boolean.toString(!listResult.isAscending()));

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(InstrumentTypeEnum.BND, sr, listResult);
        return new ModelAndView("bndfindersuchergebnis", model);
    }
}
