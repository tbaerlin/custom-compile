/*
 * OrderConfirmationMock.java
 *
 * Created on 05.02.13 17:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import com.google.gwt.user.client.Random;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderConfirmationDisplay.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Dick
 */
@NonNLS
public class OrderConfirmationMock {
    public static ArrayList<Section> getMockSections() {
        ArrayList<Section> sections = new ArrayList<Section>();

        addSectionWithSpan(sections, 3);
        addSections(sections);
        addSectionWithSpan(sections, 2);
        addSections(sections);

        return sections;
    }

    private static void addSections(ArrayList<Section> sections) {
        for(int i = 0; i < 3; i++) {
            final Section s = new Section("Headline " + i);
            List<SimpleEntry> es = s.getEntries();
            int count = 1 + 2 * Random.nextInt(4);
            for(int k = 0; k < count; k++) {
                int labelLen = Random.nextInt(3);
                int valueLen = 1 + 2 * Random.nextInt(3);

                StringBuilder sbl = new StringBuilder();
                sbl.append(s.getHeadline()).append(" Label ");
                for(int l = 0; l < labelLen; l++) {
                    sbl.append("la");
                }

                StringBuilder sbv = new StringBuilder();
                sbv.append(s.getHeadline()).append("Value ");
                for(int l = 0; l < valueLen; l++) {
                    sbv.append("va");
                }

                Entry e = new Entry(sbl.toString() + k, sbv.toString() + k);
                es.add(e);
            }
            sections.add(s);
        }
    }

    private static void addSectionWithSpan(ArrayList<Section> sections, int span) {
        final StringBuilder sb = new StringBuilder();
        final int headlineLen = 1 + 2 * Random.nextInt(38);

        sb.append("Headline Colspan ").append(span).append(" ");
        for(int l = 0; l < headlineLen; l++) {
            sb.append("he");
        }

        final Section s = new Section(sb.toString(), span);

        final List<SimpleEntry> es = s.getEntries();

        for(int i = 0; i < 4; i++) {
            final int entryLen = 1 + 2 * Random.nextInt(16);
            final StringBuilder esb = new StringBuilder();

            esb.append("Headline Colspan ").append(span).append(" ");

            for(int j = 0; j < entryLen; j++) {
                esb.append("se");
            }
            esb.append(i);
            SimpleEntry e = new SimpleEntry(esb.toString());
            es.add(e);
        }

        sections.add(s);
    }
}
