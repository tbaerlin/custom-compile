/*
 * Main.java
 *
 * Created on 21.07.2010 16:55:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.i18n;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;

/**
 * @author zzhao
 */
public class Main {

    private static final Pattern LITERAL_STRING =
            Pattern.compile("\"(([^\"]*?(\\\\\")*)+)\"");

    public static void main(String[] args) {
        String an = "D:\\iview\\trunk\\mmgwt\\src\\main\\java\\de\\marketmaker\\iview\\mmgwt\\mmweb\\client\\view";
        final String pkg = I18nMessagesGen.getPackageName(an, "de");
        System.out.println(pkg);

        String[] strArr = {"\"<br/><br/>\"",
                "\"<a href=\\\"\"",
                "\"\\\" target=\\\"dzcontent\\\" class=\\\"mm-simpleLink\\\">\""
        };

        for (int i = 0; i < strArr.length; i++) {
            System.out.println("----------------------------");
            System.out.println("String: " + strArr[i]);
            System.out.println("----------------------------");
            final Matcher matcher = LITERAL_STRING.matcher(strArr[i]);
            boolean found = false;
            while (matcher.find()) {
                System.out.format("Found ## '%s' (%d, %d)%n",
                        matcher.group(1), matcher.start(), matcher.end());
                found = true;
            }
            if (!found) {
                System.out.println("no match found");
            }
        }

        String percent = "99%";
        System.out.println(GWTAppInternationalizer.isToIgnore(percent));
        String nonPercent = "adf12%";
        System.out.println(GWTAppInternationalizer.isToIgnore(nonPercent));

        String pixel = "231px";
        System.out.println(GWTAppInternationalizer.isToIgnore(pixel));
        String nonPixel = "adfa123px";
        System.out.println(GWTAppInternationalizer.isToIgnore(nonPixel));

        String htmlStr = "<div class=\\\"mm-indent\\\"><a";
        System.out.println(GWTAppInternationalizer.isToIgnore(htmlStr));
        String specialStr = "]\\n";
        System.out.println(GWTAppInternationalizer.isToIgnore(specialStr));

        System.out.println(SystemUtils.getJavaIoTmpDir().isFile());
    }
}