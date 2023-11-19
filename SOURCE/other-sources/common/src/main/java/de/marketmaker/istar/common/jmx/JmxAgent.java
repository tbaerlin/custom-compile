/*
 * JmxAgent.java
 *
 * Created on 09.02.2007 10:56:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.jmx;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * A jmx agent that can be used to create an RMI-based jmx connector that is compatible with
 * a firewall (i.e., no random export ports). In order to run this agent before any other
 * application code, put this class in a separate jar file (e.g., jmxagent.jar) together with
 * a manifest file that specifies a single line as follows:<pre>
 * Premain-Class: de.marketmaker.istar.common.jmx.JmxAgent
 * </pre>
 * and then start your application with: <tt>java -javaagent:jmxagent.jar ...</tt>
 * <p>
 * The agent will print its configuration and the jmx service url that has to be used
 * to connect to the connector to System.out.
 * <p>
 * Properties
 * <dl>
 * <dt><em>jmxremote.registry.port</em>, <b>mandatory</b>
 * <dd>rmi registry port
 * <dt><em>jmxremote.export.port</em>
 * <dd>port for exported rmi objects, default <code>jmxremote.registry.port + 1</code>
 * <dt><em>jmxremote.ssl</em>
 * <dd>whether to use ssl sockets (requires certificate etc.), default false
 * <dt><em>jmxremote.auth</em>
 * <dd>whether username/password is required to connect, default false
 * <dt><em>jmx.remote.x.password.file</em>
 * <dd>file with username/password data, default is <tt>${user.home}/.jmx_password.properties</tt>
 * <dt><em>jmx.remote.x.access.file</em>
 * <dd>file with access rights for each user, default is <tt>${user.home}/.jmx_access.properties</tt>
 * </dl>
 *
 * @see <a href="http://java.sun.com/javase/6/docs/technotes/guides/management/agent.html">
 * http://java.sun.com/javase/6/docs/technotes/guides/management/agent.html</a>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class JmxAgent {

    public static void premain(String args, Instrumentation i) throws Exception {
        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");

        final int registryPort = Integer.getInteger("jmxremote.registry.port");
        System.out.println("JmxAgent -- registry.port " + registryPort);

        final int exportPort = Integer.getInteger("jmxremote.export.port", registryPort + 1);
        System.out.println("JmxAgent -- export.port " + exportPort);

        LocateRegistry.createRegistry(registryPort);

        // Environment map.
        //
        final Map<String,Object> env = new HashMap<>();

        // Provide SSL-based RMI socket factories.
        //
        // The protocol and cipher suites to be enabled will be the ones
        // defined by the default JSSE implementation and only server
        // authentication will be required.
        //
        if (Boolean.getBoolean("jmxremote.ssl")) {
            System.out.println("JmxAgent -- with SSL");
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
            env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
        }

        if (Boolean.getBoolean("jmxremote.auth")) {
            System.out.println("JmxAgent -- with Authorization");

            final File homeDir = new File(System.getProperty("user.home"));

            // Provide the password file used by the connector server to
            // perform user authentication. The password file is a properties
            // based text file specifying username/password pairs.
            //
            env.put("jmx.remote.x.password.file",
                    System.getProperty("jmx.remote.x.password.file" ,
                            new File(homeDir, ".jmx_password.properties").getAbsolutePath()));

            // Provide the access level file used by the connector server to
            // perform user authorization. The access level file is a properties
            // based text file specifying username/access level pairs where
            // access level is either "readonly" or "readwrite" access to the
            // MBeanServer operations.
            //
            env.put("jmx.remote.x.access.file",
                    System.getProperty("jmx.remote.x.access.file" ,
                            new File(homeDir, ".jmx_access.properties").getAbsolutePath()));
        }

        final String hostname = System.getProperty("java.rmi.server.hostname", "localhost");

        // Create an RMI connector server.
        //
        // As specified in the JMXServiceURL the RMIServer stub will be
        // registered in the RMI registry running in the local host on
        // port 3000 with the name "jmxrmi". This is the same name the
        // out-of-the-box management agent uses to register the RMIServer
        // stub too.
        //
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:" +
            exportPort  + "/jndi/rmi://" + hostname + ":" + registryPort + "/jmxrmi");

        System.out.println("JmxAgent -- " + url.toString());

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final JMXConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        cs.start();
    }
}
