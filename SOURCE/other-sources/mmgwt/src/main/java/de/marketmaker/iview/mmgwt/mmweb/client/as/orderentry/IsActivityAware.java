/*
 * IsActivityAware.java
 *
 * Created on 14.08.2014 07:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

/**
 * @author mdick
 */
public interface IsActivityAware {
    void setActivityInstanceId(String id);
    String getActivityInstanceId();

    void setActivityListEntryId(String id);
    String getActivityListEntryId();
}
