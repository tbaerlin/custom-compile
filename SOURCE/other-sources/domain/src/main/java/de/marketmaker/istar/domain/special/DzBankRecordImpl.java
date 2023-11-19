package de.marketmaker.istar.domain.special;

import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created on 19.10.12 10:08
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class DzBankRecordImpl implements Serializable, DzBankRecord {
    protected static final long serialVersionUID = 5L;

    private final Long iid;
    private final String wkn;
    private final Integer sort;
    private final String pibUrl;
    private final Integer risikoklasse;
    private final Integer bonifikation;
    private final BigDecimal bonibrief;
    private final String hinweise;
    private final String sonderheit;
    private final String topArgument;
    private final LocalDate referenceDate;
    private final String bonifikationstyp;
    private final boolean topProdukt;
    private final boolean kapitalmarktFavorit;
    private final BigDecimal rendite;
    private final String bezeichnung;
    private final String offertenkategorie;
    private final BigDecimal coupon;
    private final String issuerName;
    private final String typeKey;
    private final String type;

    private Integer indexPosition;
    private Instrument instrument;
    private boolean tradable;

    public DzBankRecordImpl(Long iid, String wkn, Integer sort, String pibUrl, Integer risikoklasse, Integer bonifikation, BigDecimal bonibrief, String hinweise,
                            String topArgument, String sonderheit, LocalDate referenceDate,
                            String bonifikationstyp, boolean topProdukt, boolean kapitalmarktFavorit, BigDecimal rendite,
                            String bezeichnung, String offertenkategorie, BigDecimal coupon,
                            String issuerName, String typeKey, String type) {
        this.iid = iid;
        this.wkn = wkn;
        this.sort = sort;
        this.pibUrl = pibUrl;
        this.risikoklasse = risikoklasse;
        this.bonifikation = bonifikation;
        this.bonibrief = bonibrief;
        this.hinweise = hinweise;
        this.sonderheit = sonderheit;
        this.topArgument = topArgument;
        this.referenceDate = referenceDate;
        this.bonifikationstyp = bonifikationstyp;
        this.topProdukt = topProdukt;
        this.kapitalmarktFavorit = kapitalmarktFavorit;
        this.rendite = rendite;
        this.bezeichnung = bezeichnung;
        this.offertenkategorie = offertenkategorie;
        this.coupon = coupon;
        this.issuerName = issuerName;
        this.typeKey = typeKey;
        this.type = type;
    }

    public Long getIid() {
        return iid;
    }

    public String getWkn() {
        return wkn;
    }

    public Integer getSort() {
        return sort;
    }

    public String getPibUrl() {
        return pibUrl;
    }

    public Integer getRisikoklasse() {
        return risikoklasse;
    }

    public Integer getBonifikation() {
        return bonifikation;
    }

    public BigDecimal getBonibrief() {
        return bonibrief;
    }

    public String getHinweise() {
        return hinweise;
    }

    public String getSonderheit() {
        return sonderheit;
    }

    @Override
    public String getTopArgument() {
        return this.topArgument;
    }

    @Override
    public LocalDate getReferenceDate() {
        return this.referenceDate;
    }

    @Override
    public String getBonifikationstyp() {
        return bonifikationstyp;
    }

    @Override
    public boolean getTopProdukt() {
        return topProdukt;
    }

    @Override
    public boolean getKapitalmarktFavorit() {
        return kapitalmarktFavorit;
    }

    @Override
    public BigDecimal getRendite() {
        return rendite;
    }

    @Override
    public String getBezeichnung() {
        return bezeichnung;
    }

    @Override
    public String getOffertenkategorie() {
        return offertenkategorie;
    }

    @Override
    public BigDecimal getCoupon() {
        return coupon;
    }

    @Override
    public String getIssuerName() {
        return this.issuerName;
    }

    @Override
    public String getTypeKey() {
        return this.typeKey;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public Integer getIndexPosition() {
        return this.indexPosition;
    }

    public void setIndexPosition(Integer indexPosition) {
        this.indexPosition = indexPosition;
    }

    @Override
    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    @Override
    public boolean getTradable() {
        return tradable;
    }

    public void setTradable(boolean tradable) {
        this.tradable = tradable;
    }

    @Override
    public Long getUnderlyingIid() {
        if (instrument instanceof Derivative) {
            return ((Derivative) instrument).getUnderlyingId();
        }
        return null;
    }

    @Override
    public DateTime getExpiration() {
        if (instrument != null) {
            return instrument.getExpiration();
        }
        return null;
    }

    @Override
    public String toString() {
        return "DzBankRecordImpl{" +
                "iid=" + iid +
                ", wkn=" + wkn +
                ", pibUrl=" + pibUrl +
                ", sort=" + sort +
                ", bonibrief=" + bonibrief +
                ", bonifikation=" + bonifikation +
                ", hinweis='" + hinweise + '\'' +
                ", risikoklasse=" + risikoklasse +
                ", sonderheit='" + sonderheit + '\'' +
                ", topArgument='" + topArgument + '\'' +
                ", referenceDate='" + referenceDate + '\'' +
                ", expiration='" + getExpiration() + '\'' +
                ", bonifikationsTyp='" + bonifikationstyp + '\'' +
                ", tradable='" + tradable + '\'' +
                ", topProdukt='" + topProdukt + '\'' +
                ", kapitalmarktFavorit='" + kapitalmarktFavorit + '\'' +
                ", rendite='" + rendite + '\'' +
                ", bezeichnung='" + bezeichnung + '\'' +
                ", offertenkategorie='" + offertenkategorie + '\'' +
                ", coupon='" + coupon + '\'' +
                ", indexPosition='" + indexPosition + '\'' +
                ", underlyingIid='" + getUnderlyingIid() + '\'' +
                ", issuerName='" + issuerName + '\'' +
                ", typeKey='" + typeKey + '\'' +
                ", type='" + type + '\'' +
                ", instrument='" + instrument + '\'' +
                "}";
    }

}