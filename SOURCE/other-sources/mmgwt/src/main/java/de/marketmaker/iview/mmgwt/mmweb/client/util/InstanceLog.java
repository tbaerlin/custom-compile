package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * User: umaurer
 * Date: 28.05.13
 * Time: 13:52
 */
public class InstanceLog {
    private static int counter = 0;
    private final String className;
    private int instanceId = counter++;
    private static Type type =
            "Firebug".equals(SessionData.INSTANCE.getUserProperty("instanceLog")) ? Type.Firebug  // $NON-NLS$
                    : ("LogWindow".equals(SessionData.INSTANCE.getUserProperty("instanceLog")) ? Type.LogWindow : Type.Silent); // $NON-NLS$
    private Type instanceType = null;
    private long lastTime = System.currentTimeMillis();

    public enum Type {
        Silent, Firebug, LogWindow
    }

    public InstanceLog(Object object) {
        this(object.getClass().getSimpleName());
    }

    public InstanceLog(String className) {
        this.className = className;
        log("new instance"); // $NON-NLS$
    }

    public void log(String text) {
        final Type type = this.instanceType != null ? this.instanceType : InstanceLog.type;
        final long time = System.currentTimeMillis();
        final long timeDiff = time - this.lastTime;
        this.lastTime = time;
        switch (type) {
            case Firebug:
                Firebug.log("~~~~~~~~~~~~~~~~~~~~~~ " + timeDiff + "ms " + className + " " + instanceId + " " + text);
                break;
            case LogWindow:
                LogWindow.addPre(className + " " + instanceId + "\n" + text); // $NON-NLS$
                break;
        }
    }

    public InstanceLog withType(Type type) {
        this.instanceType = type;
        return this;
    }

    public static void setType(Type type) {
        InstanceLog.type = type;
    }
}
