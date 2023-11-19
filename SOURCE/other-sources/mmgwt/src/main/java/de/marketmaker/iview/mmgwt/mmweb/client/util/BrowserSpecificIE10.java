/*
 * BrowserSpecificIE10.java
 *
 * Created on 27.03.2014 09:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Markus Dick
 */
public class BrowserSpecificIE10 extends BrowserSpecificIE {
    static void doFixDivBehindPdfObjectBugPart2(final HTML divContainingTheObjectTag) {
        divContainingTheObjectTag.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                if (attachEvent.isAttached()) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                                private boolean firstCall = true;

                                @Override
                                public boolean execute() {
                                    if (this.firstCall) {
                                        this.firstCall = false;

                                        final Element e = divContainingTheObjectTag.getElement();
                                        if (e != null) {
                                            final Element o = e.getFirstChildElement();
                                            if (o != null) {
                                                o.getStyle().setWidth(99.9d, Style.Unit.PCT);
                                            }
                                        }
                                        return true;
                                    }
                                    else {
                                        final Element e = divContainingTheObjectTag.getElement();
                                        if (e != null) {
                                            final Element o = e.getFirstChildElement();
                                            if (o != null) {
                                                o.getStyle().clearWidth();
                                            }
                                        }
                                        return false;
                                    }
                                }
                            }, 500);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void fixDivBehindPdfObjectBugPart2(final HTML divContainingTheObjectTag) {
        doFixDivBehindPdfObjectBugPart2(divContainingTheObjectTag);
    }
}
