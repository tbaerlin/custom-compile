/*
 * ViewDpfilesController.java
 *
 * Created on 25.04.2005 08:54:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.feed.dp.DpFileInfo;
import de.marketmaker.istar.feed.dp.DpManager;
import de.marketmaker.istar.feed.dp.DpManagerMBean;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class ViewDpfilesController extends AdminController {

    private final static String REQUIRED_BEAN_CLASSNAME =
            ClassUtils.getShortName(DpManager.class);

    private static final Comparator<DpFileInfo> COMPARE_BY_NAME = (i1, i2)
            -> i1.getInputFile().compareToIgnoreCase(i2.getInputFile());

    protected boolean isRequiredType(String type) {
        return REQUIRED_BEAN_CLASSNAME.equals(type);
    }

    @RequestMapping("/DpManager.html")
    protected ModelAndView doHandle(HttpServletRequest request, ViewDpfilesCommand cmd) throws Exception {
        final ModelAndView mav = prepareCommand(request, cmd);
        if (cmd.getInfo() == null || cmd.getObjectName() == null) {
            return mav;
        }

        final DpManagerMBean bean = (DpManagerMBean) getService(DpManagerMBean.class, cmd);

        if (cmd.getTrigger() != null) {
            String triggerResult = bean.triggerWrite(cmd.getTrigger());
            mav.addObject("triggerResult", triggerResult);
            // wait some time so that dpFileInfo contains write
            TimeUnit.SECONDS.sleep(2);
        }

        final DpFileInfo[] result = bean.getDpFileInfo();
        Arrays.sort(result, COMPARE_BY_NAME);

        mav.addObject("result", result);
        return mav;
    }
}
