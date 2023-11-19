package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.merger.web.NoProfileException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Felix Hoffmann
 */
public class NoProfileExceptionResolver implements HandlerExceptionResolver {

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex instanceof NoProfileException) {
            final String redirectOnNoProfile = request.getParameter("redirectOnNoProfile");
            if (redirectOnNoProfile != null) {
                return new ModelAndView("redirect:"+redirectOnNoProfile);
            }
        }
        return null;
    }
}
