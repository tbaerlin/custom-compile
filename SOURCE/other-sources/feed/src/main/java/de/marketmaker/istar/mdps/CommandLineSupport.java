/*
 * CommandLineCommandSupport.java
 *
 * Created on 15.04.2008 09:25:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.support.JmxUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CommandLineSupport implements InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<CommandLineOperation> ops = new ArrayList<>();

    private MBeanServer mBeanServer;

    public void setOperations(List<CommandLineOperation> operations) {
        this.ops.addAll(operations);
    }

    public void appendCommands(Map<String, String> commands) {
        for (CommandLineOperation operation : this.ops) {
            commands.put(operation.getSignature(), operation.getDescription());
        }
    }

    public void appendCommands(StringBuilder sb) {
        for (CommandLineOperation operation : this.ops) {
            sb.append(operation.getSignature()).append(" - ").append(operation.getDescription())
                    .append("\n");
        }
    }

    public void afterPropertiesSet() throws Exception {
        this.mBeanServer = JmxUtils.locateMBeanServer();
    }

    public String execute(String cmd, String[] params) {
        final CommandLineOperation operation = getOperation(cmd, params);
        if (operation == null) {
            return "Kommando unbekannt: '" + cmd + "'";
        }

        final ObjectName objectName = getObjectName(operation.getObjectNameStr());
        if (objectName == null) {
            return "Name Zielobjekt fehlerhaft: '" + operation.getObjectNameStr() + "'";
        }

        final MBeanInfo beanInfo = getMBeanInfo(objectName);
        if (beanInfo == null) {
            return "Unbekanntes Objekt: '" + objectName.getCanonicalName() + "'";
        }

        for (MBeanOperationInfo operationInfo : beanInfo.getOperations()) {
            if (!operation.getMethod().equals(operationInfo.getName())) {
                continue;
            }
            final MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
            if (parameterInfos.length != params.length) {
                continue;
            }

            final String[] types = new String[parameterInfos.length];
            for (int i = 0; i < parameterInfos.length; i++) {
                types[i] = parameterInfos[i].getType();
            }

            try {
                final Object result
                        = this.mBeanServer.invoke(objectName, operationInfo.getName(), params, types);
                return toString(result, "Kommando erfolgreich aufgerufen");
            } catch (Exception e) {
                this.logger.error("<execute> failed to invoke " + operationInfo.getName()
                        + " on " + objectName.getCanonicalName(), e);
                return "Fehler beim Kommandoaufruf (siehe Logger-Datei)";
            }
        }

        for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
            if (!operation.getMethod().equals(attributeInfo.getName())) {
                continue;
            }

            if (params.length == 1) {
                try {
                    this.mBeanServer.setAttribute(objectName, new Attribute(attributeInfo.getName(), params[0]));
                } catch (Exception e) {
                    this.logger.error("<execute> failed to set attribte " + attributeInfo.getName()
                            + " on " + objectName.getCanonicalName(), e);
                    return "Wert konnte nicht gesetzt werden";
                }
                return "Wert wurde gesetzt";
            }
            else {
                try {
                    final AttributeList attributeList
                            = this.mBeanServer.getAttributes(objectName, new String[]{attributeInfo.getName()});
                    return toString(attributeList.get(0), "--");
                } catch (Exception e) {
                    this.logger.error("<execute> failed to get attribute " + attributeInfo.getName()
                            + " on " + objectName.getCanonicalName(), e);
                    return "Wert konnte nicht ermittelt werden (siehe Logger-Datei)";
                }
            }
        }

        return "Operation unbekannt: '" + operation.getMethod() + "'";
    }

    private String toString(Object o, String defaultValue) {
        if (o == null) {
            return defaultValue;
        }
        return String.valueOf(o);
    }

    private MBeanInfo getMBeanInfo(ObjectName objectName) {
        try {
            return this.mBeanServer.getMBeanInfo(objectName);
        } catch (Exception e) {
            return null;
        }
    }

    private ObjectName getObjectName(String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            return null;
        }
    }

    public boolean isValidCommand(String cmd, String[] params) {
        return getOperation(cmd, params) != null;
    }

    private CommandLineOperation getOperation(String cmd, String[] params) {
        for (CommandLineOperation operation : this.ops) {
            if (cmd.equalsIgnoreCase(operation.getCommand())
                    && params.length == operation.getNumParams()) {
                return operation;
            }
        }
        return null;
    }
}
