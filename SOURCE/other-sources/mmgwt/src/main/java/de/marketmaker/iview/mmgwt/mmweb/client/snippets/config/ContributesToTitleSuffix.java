/*
 * AbstractTextBoxForm.java
 *
 * Created on 27.06.2012 13:57:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

/**
 * @author Markus Dick
 */
public interface ContributesToTitleSuffix {
    public static final String TITLE_SUFFIX_BASE = "titleSuffix"; //$NON-NLS$
    public String getTitleSuffixContribution();
    public String getTitleSuffixContributionParameterName();
}
