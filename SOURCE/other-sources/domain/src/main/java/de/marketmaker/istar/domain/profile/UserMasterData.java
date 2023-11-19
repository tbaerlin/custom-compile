/*
 * UserMasterData.java
 *
 * Created on 14.07.2008 14:07:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.profile;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserMasterData {
    enum Gender { FEMALE, MALE, UNKNOWN }

    String getFirstName();

    String getLastName();

    String getVwdId();

    String getMandatorId();

    String getGisCustomerId();

    String getCentralBank();

    String getGenoId();

    String getVdbLogin();

    String getVdbPassword();

    String getAppTitle();

    String getCustomerName();

    Gender getGender();
    
    /**
     * Return the text of the node specified by the given xpath expression. Assumption is that the
     * contents of this object are specified by an xml document.
     * @param expr xpath expression
     * @return text of the Node selected by expr.
     * Returns null if expr is invalid or no node is selected
     */
    String nodeText(String expr);

    /**
     * Return the text of the node's attribute specified by the given xpath expression.
     * e.g. Mandator[@id='10']/Masterdata/Data[@type='111']/@value
     */
    String attributeText(String expr);

    Map<String, String> getStaticAccounts();

}
