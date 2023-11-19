/*
 * MdpsMain.java
 *
 * Created on 17.06.2010 09:10:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.common.spring.Main;
import de.marketmaker.istar.common.util.NioUtils;

/**
 * @author oflege
 */
public class MdpsMain extends Main {
    private static final String MDPS_NAME = "mdpsName";

    static final Properties PROFILE = new Properties();

    private final Properties properties = new Properties();

    private final File cfgDir = new File(System.getProperty("cfgDir", "/MDPS/RUN/CFG/"));

    private final File admserverConfig = new File(this.cfgDir, "admserver.cfg");

    private final File profserverConfig = new File(this.cfgDir, "profserver.cfg");

    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);
        beanFactory.registerSingleton("__mdpsProperties", properties);
    }

    @Override
    protected PropertyPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
        final PropertyPlaceholderConfigurer result = getDefinedConfigurer();
        result.setLocalOverride(true);
        addAdmServerProperties();
        addProfServerProperties();
        addProfile();
        result.setProperties(this.properties);
        return result;
    }

    private void addProfile() {
        try {
            doAddProfile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doAddProfile() throws Exception {
        final String mdpsName = getMdpsName();
        final AdminProtocolSupport ps = new AdminProtocolSupport(mdpsName);
        try (final SocketChannel sc = NioUtils.createSocketChannel(getAddress(), 0)) {
            sendProfileRequest(ps, sc);
            readProfileResponse(ps, sc);
        }
        this.logger.info("<doAddProfile> " + PROFILE);
        this.properties.putAll(PROFILE);
    }

    public static String getMdpsName() {
        final String mdpsName = System.getProperty(MDPS_NAME);
        if (mdpsName == null) {
            throw new IllegalArgumentException("undefined property " + MdpsMain.MDPS_NAME);
        }
        return mdpsName;
    }

    public static boolean getProfileParameter(String name, boolean defaultValue) {
        final String value = PROFILE.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        final CustomBooleanEditor editor = new CustomBooleanEditor(false);
        editor.setAsText(value);
        return (Boolean) editor.getValue();
    }

    private void readProfileResponse(AdminProtocolSupport ps, SocketChannel sc) throws IOException {
        final ByteBuffer bb = ps.createBuffer(64 * 1024);
        do {
            sc.read(bb);
        } while (!ps.isMessageComplete(bb));
        bb.flip();
        PROFILE.putAll(ps.getProfileProperties(bb));
    }

    private void sendProfileRequest(AdminProtocolSupport ps, SocketChannel sc) throws IOException {
        final ByteBuffer bb = ps.createProfileRequest();
        while (bb.hasRemaining()) {
            sc.write(bb);
        }
    }

    private InetSocketAddress getAddress() {
        return new InetSocketAddress(properties.getProperty("primary.profile.host"),
                Integer.parseInt(properties.getProperty("primary.profile.port")));
    }

    private PropertyPlaceholderConfigurer getDefinedConfigurer() {
        final PropertyPlaceholderConfigurer ppc = super.getPropertyPlaceholderConfigurer();
        return (ppc == null) ? new PropertyPlaceholderConfigurer() : ppc;
    }

    private void addProfServerProperties() {
        addProperties(copyToString(this.profserverConfig), "profile");
    }

    private void addAdmServerProperties() {
        addProperties(copyToString(this.admserverConfig), "admin");
    }

    private void addProperties(String cfg, final String subkey) {
        this.properties.setProperty("primary." + subkey + ".host", findByPattern(cfg, "PrmSrvAddr ([\\d\\.]+)"));
        this.properties.setProperty("primary." + subkey + ".port", findByPattern(cfg, "PrmSrvPort (\\d+)"));
        this.properties.setProperty("secondary." + subkey + ".host", findByPattern(cfg, "SecSrvAddr ([\\d\\.]+)"));
        this.properties.setProperty("secondary." + subkey + ".port", findByPattern(cfg, "SecSrvPort (\\d+)"));
    }

    private String findByPattern(String cfg, String pattern) {
        return findFirstGroup(Pattern.compile(pattern, Pattern.MULTILINE), cfg);
    }

    private String findFirstGroup(Pattern p, String s) {
        final Matcher m = p.matcher(s);
        return (m.find()) ? m.group(1) : null;
    }

    private String copyToString(final File file) {
        try {
            return FileCopyUtils.copyToString(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        final MdpsMain m = new MdpsMain();
        PropertyPlaceholderConfigurer c = m.getPropertyPlaceholderConfigurer();
        System.out.println(m.properties);
    }
}
