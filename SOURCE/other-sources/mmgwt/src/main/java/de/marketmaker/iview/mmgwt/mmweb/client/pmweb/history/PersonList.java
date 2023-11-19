/*
 * PersonList.java
 *
 * Created on 17.03.14 14:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;

import java.util.List;

/**
 * @author Markus Dick
 */
public class PersonList extends ContextList<Person> {
    public PersonList(List<Person> list) {
        super(list);
    }

    @Override
    protected String getIdOf(Person item) {
        return item.getId();
    }
}
