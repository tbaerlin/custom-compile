package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget;

import de.marketmaker.itools.gwtutil.client.widgets.input.Radio;
import de.marketmaker.itools.gwtutil.client.widgets.input.RadioGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 22.01.14
 */
public class RadioButtonGroup {
    private static final Map<String, RadioGroup<String>> mapGroups = new HashMap<String, RadioGroup<String>>();

    public static Radio<String> create(String group, String value) {
        RadioGroup<String> rbg = mapGroups.get(group);
        if (rbg == null) {
            rbg = new RadioGroup<String>();
            mapGroups.put(group, rbg);
        }
        return rbg.add(value, false);
    }

    private RadioButtonGroup() {
    }
}
