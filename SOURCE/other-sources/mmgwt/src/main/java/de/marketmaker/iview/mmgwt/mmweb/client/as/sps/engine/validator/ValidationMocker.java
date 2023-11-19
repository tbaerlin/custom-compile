package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.pmxml.SectionDesc;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 02.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ValidationMocker {

    public static void addMocks(SectionDesc formDescRoot, Context context) {
        if (formDescRoot.getBind().equals("inhaber/fv")) { // $NON-NLS$
            addInhaberFV(context);
        }
    }

    private static void addInhaberFV(Context context) {
        final BindToken sourceToken = BindToken.create("/inhaber/fv/fvKeineAngaben"); // $NON-NLS$

        final List<BindToken> targetTokens = Arrays.asList(
                BindToken.create("/inhaber/fv/fvEinkuenfteGehaltRente") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvEinkuenfteKapital") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvEinkuenfteGewerblich") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvAusgabenLebenshaltung") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvAusgabenKredite") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvAusgabenSonstiges") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvEinkuenfteUeberschuss") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvVermoegenBankguthaben") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvVermoegenImmobilien") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvVermoegenWertpapiere") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvVerbindlichkeitHypotheken") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvVerbindlichkeitKredite") // $NON-NLS$
                , BindToken.create("/inhaber/fv/fvNettoVermoegen") // $NON-NLS$
        );


        final Validator disable = Validator.create(StringCompare.createEqual("true", sourceToken, context), new WidgetAction() {  // $NON-NLS$
            @Override
            public void doIt(SpsWidget widget, ValidationResponse response) {
                widget.setEnabled(false);
            }
        });
        final Validator enable = Validator.create(StringCompare.createUnEqual("true", sourceToken, context), new WidgetAction() {  // $NON-NLS$
            @Override
            public void doIt(SpsWidget widget, ValidationResponse response) {
                widget.setEnabled(true);
            }
        });

        for (BindToken targetToken : targetTokens) {
            context.putValidator(targetToken, disable);
            context.putValidator(targetToken, enable);
        }
    }
}