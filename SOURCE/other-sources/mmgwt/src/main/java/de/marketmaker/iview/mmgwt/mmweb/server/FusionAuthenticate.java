package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.IOException;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Felix Hoffmann
 */
@Controller
public class FusionAuthenticate {
    private final Log logger = LogFactory.getLog(this.getClass());

    private String redirectPath = "index.html";
    private String requiredSelector = "METALS_MOBILE";

    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public void setRequiredSelector(String requiredSelector) {
        this.requiredSelector = requiredSelector;
    }

    public static class Command {
        private String login;
        private String password;
        private String zone;
        private String locale;

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }
    }

    private UserServiceIfc userService;

    @Required
    public void setUserService(UserServiceIfc userService) {
        this.userService = userService;
    }

    @RequestMapping("/fusionapp/mobile/login.html")
    protected ModelAndView login(HttpServletRequest servletRequest, HttpServletResponse servletResponse, Command c)
            throws Exception {
        ServletRequestHolder.setHttpServletRequest(servletRequest);
        try {
            return doLogin(servletRequest, servletResponse, c);
        } finally {
            ServletRequestHolder.setHttpServletRequest(null);
        }
    }

    private ModelAndView doLogin(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, Command c) throws IOException {
        final String action = servletRequest.getParameter("action");
        final UserRequest userRequest = new UserRequest(false, c.getLogin(), c.getPassword(), c.getZone(), null, c.getLocale());
        final UserResponse userResponse = this.userService.getUser(userRequest);

        final UserResponse.State state = userResponse.getState();

        final String module = userRequest.getModule();
        final ClientConfig clientConfig = this.userService.getConfig(module);


        if ("logout".equals(action)) {
            this.logger.info("<action> logout");
            this.userService.logout();
        }

        servletResponse.setContentType("text/html;charset=UTF-8");
        final ModelAndView mav = new ModelAndView("mmgwt/mobile/login");

        if (state == UserResponse.State.OK) {
            if (c.getLogin() != null && clientConfig != null) {
                Profile userProfile = this.userService.getProfileByLogin(c.getLogin(), clientConfig);
                if (userProfile.isAllowed(Selector.valueOf(this.requiredSelector))) {
                    servletResponse.sendRedirect(this.redirectPath);
                } else {
                    mav.addObject("state", "Funktionalität nicht verfügbar.");
                    return mav;
                }
            }

        } else {
            switch (state) {
                case WRONG_PASSWORD:
                    mav.addObject("state", "Falsches Passwort.");
                    break;
                case WRONG_INITIAL_PASSWORD:
                    mav.addObject("state", "Falsches Initialpasswort.");
                    break;
                case UNKNOWN_USER:
                    if (c.getLogin() != null) {
                        if (c.getLogin().equals("")) {
                            mav.addObject("state", "Username muss angegeben werden.");
                        } else {
                            mav.addObject("state", "Unbekannter username: " + c.getLogin());
                        }
                    }
                    break;
                case INACTIVE_USER:
                    mav.addObject("state", "Benutzer inaktiv: " + c.getLogin());
                    break;
                case INVALID_PRODUCT:
                    mav.addObject("state", "Ungültiges Produkt.");
                    break;
                default:
                    mav.addObject("state", "Interner Fehler.");
                    break;
            }
            return mav;
        }
        return null;
    }
}
