/*
 * FieldNameConverter.java
 *
 * Created on 03.11.2008 09:54:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.io.File;
import java.util.Scanner;

/**
 * Replaces the names in a file with fieldnames (one name per line), to take care of name changes
 * due to fieldmap updates.
 *  
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FieldNameConverter {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: FieldNameConverter filename");
            return;
        }
        final Scanner s = new Scanner(new File(args[0]));
        while (s.hasNextLine()) {
            final String line = s.nextLine();
            if (line.startsWith("#")) {
                System.out.println(line);
            }
            final VwdFieldDescription.Field old = VwdFieldDescriptionOld.getFieldByName(line);
            if (old == null) {
                System.err.println("No such field: " + line);
                continue;
            }
            VwdFieldDescription.Field newField = null;
            if (VwdFieldDescription.length() >= old.id()) {
                newField = VwdFieldDescription.getField(old.id());
            }
            if (newField == null) {
                System.err.println("Field no longer present: " + line);
                continue;
            }
            System.out.println(newField.name());
        }
        s.close();
    }
}
