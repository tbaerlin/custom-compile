/*
 * MdpsExporterHelp.java
 *
 * Created on 01.09.2008 18:51:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.mdpsexport;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsExporterHelp implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File configDir;
    private File dataDir;
    private File outputDir;
    private InstrumentTypeEnum[] types;

    public void setConfigDir(File configDir) {
        this.configDir = configDir;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setTypes(String... types) {
        this.types = new InstrumentTypeEnum[types.length];
        for (int i = 0; i < types.length; i++) {
            this.types[i] = InstrumentTypeEnum.valueOf(types[i]);
        }
    }

    public void afterPropertiesSet() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        final MdpsExporterHelp help = new MdpsExporterHelp();
        help.setConfigDir(new File("d:/produktion/prog/istar-ratios-mdpsexport/conf"));
        help.setOutputDir(new File("d:/produktion/prog/istar-ratios-mdpsexport/out"));
        help.setTypes("STK", "FND", "BND", "CUR", "IND");
        help.afterPropertiesSet();

        help.getMarkets("STK".toLowerCase());
    }

    private void getMarkets(final String type) throws Exception {
        final File[] files = this.outputDir.listFiles((dir, name) -> name.indexOf(type) > 0);
        Arrays.sort(files, (o1, o2) -> (int) (o2.lastModified() - o1.lastModified()));

        final Set<String> markets =
                Files.lines(files[0].toPath())
                        .map(s -> {
                            final String vwdcode = s.substring(0, s.indexOf(';'));
                            final int marketStart = vwdcode.indexOf(".");
                            final int marketEnd = vwdcode.indexOf(".", marketStart + 1);

                            return vwdcode.substring(marketStart + 1, marketEnd < 0 ? vwdcode.length() : marketEnd);
                        })
                        .collect(Collectors.toCollection(TreeSet::new));

        markets.forEach(System.out::println);
    }
}
