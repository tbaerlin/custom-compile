/*
 * FinderFormElement.java
 *
 * Created on 10.06.2008 13:31:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.Map;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;

import de.marketmaker.iview.dmxml.FinderMetaList;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FinderFormElement extends HasHandlers {
    /**
     * add this element to the row of the given Grid
     * @param flexTable the FlexTable
     * @param sectionId the id of the section
     */
    public void addTo(FlexTable flexTable, int sectionId);

    /**
     * returns a (partial) query expression based on the values of the contained fields
     * @return query or null if not enabled or no fields configured
     */
    public String getQuery();

    /**
     * add user readable query to panel
     * @param panel The panel to which the explanation is added
     */
    public void addExplanation(FlowPanel panel);



    /**
     * add state to config so that invoking {@link #apply(FinderFormConfig)} with the config
     * sets all contained fields to their current values
     * @param config configuration store
     */
    void addConfigTo(FinderFormConfig config);

    /**
     * set all form fields according to the given config
     * @param config defines form
     */
    void apply(FinderFormConfig config);

    /**
     * reset all form fields
     */
    void reset();

    void initialize(Map<String,FinderMetaList> map);

    void addClickHandler(ClickHandler ch);

    boolean getValue();
    void setValue(boolean checked);

    boolean isActive();
    void setActive(boolean active);
    Integer getDefaultOrder();
    String getLabel();
    String getId();
    boolean isConfigurable();

    void addChangeHandler(ChangeHandler ch);
}