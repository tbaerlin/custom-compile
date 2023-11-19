/*
 * ViewIntradayController.java
 *
 * Created on 28.04.2005 11:09:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickServer;
import de.marketmaker.istar.feed.tick.TickServerMBean;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMP3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMPZ;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class ViewTicksController extends AdminController {

    private final static String REQUIRED_BEAN_CLASSNAME =
            ClassUtils.getShortName(TickServer.class);

    protected boolean isRequiredType(String type) {
        return REQUIRED_BEAN_CLASSNAME.equals(type);
    }

    @RequestMapping("/TickServer.html")
    protected ModelAndView doHandle(HttpServletRequest request, ViewTicksCommand cmd) throws Exception {
        final ModelAndView mav = prepareCommand(request, cmd);
        if (cmd.getInfo() == null || cmd.getObjectName() == null) {
            return mav;
        }

        final Map<String, Object> m = mav.getModel();
        if (!StringUtils.hasText(cmd.getKey())) {
            return new ModelAndView("main", m);
        }

        final TickServerMBean bean =
                (TickServerMBean) getService(TickServerMBean.class, cmd);


        final AbstractTickRecord.TickItem ticks;
        try {
            final TimeTaker tt = new TimeTaker();
            ticks = getTicks(cmd, bean);
            m.put("took", tt.toString());

            if (ticks != null) {
                if (ticks.getEncoding() == DUMP3 || ticks.getEncoding() == DUMPZ) {
                    m.put("feeddump", new ViewableRecords(ticks, cmd));
                }
                else {
                    m.put("result", new ViewableTicks(ticks, cmd));
                }
            }
        } catch (Exception e) {
            this.logger.warn("<processFormSubmission> failed", e);
        }

        return new ModelAndView("main", m);
    }
}
