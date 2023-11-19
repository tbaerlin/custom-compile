package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRendererAdapter;

/**
 * RadioButtonRenderer.java
 * Created on 07.07.2009 10:38:01
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class RadioButtonRenderer extends TableCellRendererAdapter {
    final private String name;
    private Element e = null;

    public RadioButtonRenderer(String name) {
        this.name = name;
    }

    @Override
    public void render(Object data, StringBuffer sb, Context context) {
        sb.append("<input type=\"radio\" name=\"") // $NON-NLS$
                .append(this.name)
                .append("\"")
                .append(data!=null&&Boolean.valueOf(data.toString())
                        ? " checked"  // $NON-NLS$
                        : "")
                .append(">");  // $NON-NLS$
    }

    private static native int getSelectedJs(Element e) /*-{
         var radios = e.getElementsByTagName("input"); // $NON-NLS-0$
          for (var i = 0; i < radios.length; i++) {
             if (radios[i].checked == true) return i;
         }
         return -1;
   }-*/;

    public int getSelected() {
        if (this.e == null) {
            return -1;
        }
        return getSelectedJs(this.e);
    }

    public void setElement(Element e) {
        this.e = e;
    }

}
