/*
 * IssuerRatingInterface.java
 *
 * Created on 08.05.12 18:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.io.Serializable;

import org.joda.time.LocalDate;

/**
 * @author zzhao
 */
public interface IssuerRating extends Serializable {

    RatingSource getSource();

    String getIssuerName();

    String getLei();

    String getVwdSymbol();

    String getCountryIso();

    String getCurrencyIso();

    String getRatingFitchIssuerLongTerm();

    Entry.Action getRatingFitchIssuerLongTermAction();

    LocalDate getRatingFitchIssuerLongTermDate();

    String getRatingFitchIssuerShortTerm();

    Entry.Action getRatingFitchIssuerShortTermAction();

    LocalDate getRatingFitchIssuerShortTermDate();

    String getRatingFitchIssuerIFS();

    Entry.Action getRatingFitchIssuerIFSAction();

    LocalDate getRatingFitchIssuerIFSDate();

    // Counterparty Rating (CTP)

    String getRatingMoodysIssuerLongTerm();

    Entry.Action getRatingMoodysIssuerLongTermAction();

    LocalDate getRatingMoodysIssuerLongTermDate();

    String getRatingMoodysIssuerShortTerm();

    Entry.Action getRatingMoodysIssuerShortTermAction();

    LocalDate getRatingMoodysIssuerShortTermDate();

    // Counterparty Rating (CTP) backed

    String getRatingMoodysIssuerLongTermBacked();

    Entry.Action getRatingMoodysIssuerLongTermActionBacked();

    LocalDate getRatingMoodysIssuerLongTermDateBacked();

    String getRatingMoodysIssuerShortTermBacked();

    Entry.Action getRatingMoodysIssuerShortTermActionBacked();

    LocalDate getRatingMoodysIssuerShortTermDateBacked();

    // Senior Unsecured Rating (SU)

    String getRatingMoodysIssuerLongTermSu();

    Entry.Action getRatingMoodysIssuerLongTermActionSu();

    LocalDate getRatingMoodysIssuerLongTermDateSu();

    String getRatingMoodysIssuerShortTermSu();

    Entry.Action getRatingMoodysIssuerShortTermActionSu();

    LocalDate getRatingMoodysIssuerShortTermDateSu();

    // Senior Unsecured Rating (SU) backed

    String getRatingMoodysIssuerLongTermSuBacked();

    Entry.Action getRatingMoodysIssuerLongTermActionSuBacked();

    LocalDate getRatingMoodysIssuerLongTermDateSuBacked();

    String getRatingMoodysIssuerShortTermSuBacked();

    Entry.Action getRatingMoodysIssuerShortTermActionSuBacked();

    LocalDate getRatingMoodysIssuerShortTermDateSuBacked();

    // Bank Deposit Rating (BDR)

    String getRatingMoodysIssuerLongTermBdr();

    Entry.Action getRatingMoodysIssuerLongTermActionBdr();

    LocalDate getRatingMoodysIssuerLongTermDateBdr();

    String getRatingMoodysIssuerShortTermBdr();

    Entry.Action getRatingMoodysIssuerShortTermActionBdr();

    LocalDate getRatingMoodysIssuerShortTermDateBdr();

    // Bank Deposit Rating (BDR) backed

    String getRatingMoodysIssuerLongTermBdrBacked();

    Entry.Action getRatingMoodysIssuerLongTermActionBdrBacked();

    LocalDate getRatingMoodysIssuerLongTermDateBdrBacked();

    String getRatingMoodysIssuerShortTermBdrBacked();

    Entry.Action getRatingMoodysIssuerShortTermActionBdrBacked();

    LocalDate getRatingMoodysIssuerShortTermDateBdrBacked();

    // Insurance Financial Strength Rating (IFS)

    String getRatingMoodysIssuerLongTermIfsr();

    Entry.Action getRatingMoodysIssuerLongTermActionIfsr();

    LocalDate getRatingMoodysIssuerLongTermDateIfsr();

    String getRatingMoodysIssuerShortTermIfsr();

    Entry.Action getRatingMoodysIssuerShortTermActionIfsr();

    LocalDate getRatingMoodysIssuerShortTermDateIfsr();

    // Insurance Financial Strength Rating (IFS) backed

    String getRatingMoodysIssuerLongTermIfsrBacked();

    Entry.Action getRatingMoodysIssuerLongTermActionIfsrBacked();

    LocalDate getRatingMoodysIssuerLongTermDateIfsrBacked();

    String getRatingMoodysIssuerShortTermIfsrBacked();

    Entry.Action getRatingMoodysIssuerShortTermActionIfsrBacked();

    LocalDate getRatingMoodysIssuerShortTermDateIfsrBacked();

    // ---

    String getRatingStandardAndPoorsIssuerLongTerm();

    Entry.Action getRatingStandardAndPoorsIssuerLongTermAction();

    LocalDate getRatingStandardAndPoorsIssuerLongTermDate();

    Entry.RegulatoryId getRatingStandardAndPoorsIssuerLongTermRegulatoryId();

    String getRatingStandardAndPoorsIssuerShortTerm();

    Entry.Action getRatingStandardAndPoorsIssuerShortTermAction();

    LocalDate getRatingStandardAndPoorsIssuerShortTermDate();

    String getRatingStandardAndPoorsIssuerLongTermFSR();

    Entry.Action getRatingStandardAndPoorsIssuerLongTermActionFSR();

    LocalDate getRatingStandardAndPoorsIssuerLongTermDateFSR();

    String getRatingStandardAndPoorsIssuerShortTermFSR();

    Entry.Action getRatingStandardAndPoorsIssuerShortTermActionFSR();

    LocalDate getRatingStandardAndPoorsIssuerShortTermDateFSR();

    String getRatingStandardAndPoorsIssuerLongTermFER();

    Entry.Action getRatingStandardAndPoorsIssuerLongTermActionFER();

    LocalDate getRatingStandardAndPoorsIssuerLongTermDateFER();

    String getRatingStandardAndPoorsIssuerShortTermFER();

    Entry.Action getRatingStandardAndPoorsIssuerShortTermActionFER();

    LocalDate getRatingStandardAndPoorsIssuerShortTermDateFER();
}
