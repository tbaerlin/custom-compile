/*
 * PageCopy.java
 *
 * Created on 27.07.2005 07:25:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.pages;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Use this class to copy pages from one database to another. Useful if you have restarted
 * a chicago instance and find out that it missed some pages while being down.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PageCopy {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length  < 2) {
            System.err.println("Usage: PageCopy <context.xml> all");
            System.err.println("Usage: PageCopy <context.xml> after yyyy-MM-dd'T'HH:mm:ss");
            System.err.println("Usage: PageCopy <context.xml> pageid( pageid)*");
            System.err.println("Usage: PageCopy <context.xml> <file-with-pageid-per-line>");
            System.err.println("-- <context.xml> is a spring application context file with beans:");
            System.err.println("-- . pageDao1 used to load pages");
            System.err.println("-- . pageDao2 used to store pages");
            System.exit(1);
        }

        FileSystemXmlApplicationContext ac = new FileSystemXmlApplicationContext(args[0]);

        PageDao pd1 = (PageDao) ac.getBean("pageDao1");
        PageDao pd2 = (PageDao) ac.getBean("pageDao2");

        List<String> pages = new ArrayList<>();

        if ("all".equals(args[1])) {
            final List<Integer> pagenumbers = pd1.getPagenumbers();
            for (Integer i : pagenumbers) {
                pages.add(i.toString());
            }
        }
        else if ("after".equals(args[1])) {
            final DateTime dt = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(args[2]);
            final List<Integer> pagenumbers = pd1.getPagenumbersChangedAfter(dt);
            for (Integer i : pagenumbers) {
                pages.add(i.toString());
            }
        }
        else if (args.length == 2 && (new File(args[1])).canRead()) {
            Scanner s = new Scanner(new File(args[1]));
            while (s.hasNextLine()) {
                pages.add(s.nextLine().trim());
            }
        }
        else {
            pages.addAll(Arrays.asList(args).subList(1, args.length));
        }

        if ("test".equals(args[args.length - 1])) {
            System.out.println(pages);
            return;
        }

        for (String s : pages) {
            int pagenumber = Integer.parseInt(s);
            PageData pageData = pd1.getPageData(pagenumber);
            if (pageData == null) {
                System.err.println("no such page: " + s);
            }

            pd2.store(pageData);

            System.out.println("Copied page " + s);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
