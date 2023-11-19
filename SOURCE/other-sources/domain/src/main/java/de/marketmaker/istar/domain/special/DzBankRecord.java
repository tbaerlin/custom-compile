package de.marketmaker.istar.domain.special;

import de.marketmaker.istar.domain.instrument.Instrument;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

/**
 * Created on 19.10.12 10:08
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface DzBankRecord {

    Long getIid();

    String getWkn();

    Integer getSort();

    String getPibUrl();

    Integer getRisikoklasse();

    Integer getBonifikation();

    BigDecimal getBonibrief();

    String getHinweise();

    String getTopArgument();

    String getSonderheit();

    LocalDate getReferenceDate();

    boolean getTradable();

    boolean getTopProdukt();

    boolean getKapitalmarktFavorit();

    BigDecimal getRendite();

    String getBezeichnung();

    String getOffertenkategorie();

    String getBonifikationstyp();

    BigDecimal getCoupon();

    Integer getIndexPosition();

    String getIssuerName();

    String getTypeKey();

    String getType();

    Instrument getInstrument();

    Long getUnderlyingIid();

    DateTime getExpiration();

}
