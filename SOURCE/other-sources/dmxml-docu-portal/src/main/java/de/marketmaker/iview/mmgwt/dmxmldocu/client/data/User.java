/*
 * User.java
 *
 * Created on 07.03.2007 12:15:46
 *
 * Copyright (c) market maker Software AG - part of the vwd group. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.data;

import java.io.Serializable;
import java.util.Set;

/**
 * @author umaurer
 */
public class User implements Serializable, Comparable<User> {
    private int id;
    private int clientId;
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> groups;
    private String password;
    private String sessionId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return this.firstName + " " + this.lastName; //$NON-NLS$
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String toString() {
        return getName();
    }

    public int compareTo(User user) {
        int result = getName().compareTo(user.getName());
        if (result != 0) {
            return result;
        }
        return getUsername().compareTo(user.getUsername());
    }
}
