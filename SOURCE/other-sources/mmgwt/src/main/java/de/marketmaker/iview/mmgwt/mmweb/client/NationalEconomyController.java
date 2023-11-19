package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * NationalEconomyController.java
 * 
 * Created on Oct 2, 2008 12:31:30 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class NationalEconomyController extends DelegatingPageController {

    private static final String DEF_GERMANY = "nec_germany"; // $NON-NLS-0$



    private static final String DEF_EASTEUROPE = "nec_easteur"; // $NON-NLS-0$

    private static final String DEF_AMERICA = "nec_america"; // $NON-NLS-0$

    private static final String DEF_ASIA = "nec_asia"; // $NON-NLS-0$

    private static final String DEF_G7CHEU = "nec_g7cheu"; // $NON-NLS-0$

    public static NationalEconomyController createG7CHEU(ContentContainer contentContainer) {
        return new NationalEconomyController(contentContainer, DEF_G7CHEU);
    }

    public static NationalEconomyController createGER(ContentContainer contentContainer) {
        return new NationalEconomyController(contentContainer, DEF_GERMANY);
    }

    public static NationalEconomyController createAMERICA(ContentContainer contentContainer) {
        return new NationalEconomyController(contentContainer, DEF_AMERICA);
    }

    public static NationalEconomyController createEASTEUROPE(ContentContainer contentContainer) {
        return new NationalEconomyController(contentContainer, DEF_EASTEUROPE);
    }

    public static NationalEconomyController createASIA(ContentContainer contentContainer) {
        return new NationalEconomyController(contentContainer, DEF_ASIA);
    }


    final private String def;

    private NationalEconomyController(ContentContainer contentContainer, String def) {
        super(contentContainer);
        this.def = def;
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), this.def);
    }

}
