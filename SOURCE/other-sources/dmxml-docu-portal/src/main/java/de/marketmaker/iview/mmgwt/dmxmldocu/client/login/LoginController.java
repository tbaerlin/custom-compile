package de.marketmaker.iview.mmgwt.dmxmldocu.client.login;

import java.util.Date;
import java.util.Iterator;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.data.User;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.LoggedInEvent;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.LogoutEvent;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.LogoutHandler;

/**
 * @author umaurer
 */
public class LoginController implements LogoutHandler {

    public interface Display {

        Widget asWidget();

        HasClickHandlers getSubmitButton();

        HasText getUsernameText();

        HasText getPasswordText();

        HasText getMessageText();

        HasValue<Boolean> isStoreSession();

        Focusable getUsernameFocus();

        Focusable getPasswordFocus();

        HasKeyDownHandlers[] getHasKeyDownHandlers();
    }

    private class Handler implements KeyDownHandler, ClickHandler {
        public void onClick(ClickEvent clickEvent) {
            onSubmit();
        }

        public void onKeyDown(KeyDownEvent kpe) {
            if (kpe.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                onSubmit();
            }
            else {
                display.getMessageText().setText(" ");
            }
        }
    }

    //TODO: should be parameterizable
    private static final String COOKIE_LAST_USERNAME = "dmxml-last-username"; //$NON-NLS$

    private final HandlerManager eventBus;

    private HasWidgets container;

    private Display display;

    private LoginServiceAsync loginService;

    public LoginController(HandlerManager eventBus, LoginServiceAsync loginService,
            Display display) {
        this.eventBus = eventBus;
        this.loginService = loginService;
        this.display = display;
        bind();
    }

    private void bind() {
        final Handler h = new Handler();
        for (HasKeyDownHandlers handlers : this.display.getHasKeyDownHandlers()) {
            handlers.addKeyDownHandler(h);
        }
        this.display.getSubmitButton().addClickHandler(h);
        this.eventBus.addHandler(LogoutEvent.TYPE, this);
    }

    private void onSubmit() {
        String username = display.getUsernameText().getText();
        boolean storeSession = this.display.isStoreSession() == null ? false : this.display.isStoreSession().getValue();
        if (username.endsWith(",x")) { //$NON-NLS$
            username = username.substring(0, username.length() - 2);
            storeSession = true;
        }
        login(username, display.getPasswordText().getText(), storeSession);
    }

    public void login() {
        this.loginService.getUserFromSession(new AsyncCallback<User>() {
            public void onFailure(Throwable e) {
                Firebug.error("getUserFromSession() failed", e);
                showLoginForm();
            }

            public void onSuccess(User user) {
                if (user == null) {
                    Firebug.log("login() showLoginForm");
                    showLoginForm();
                }
                else {
                    Firebug.log("login() got user from server");
                    onLoggedIn(user);
                }
            }
        });
    }

    public void login(String username, String password, boolean storeSession) {
        this.loginService.login(username, password, storeSession, new AsyncCallback<User>() {
            public void onFailure(Throwable e) {
                Firebug.error("login failed", e);
                display.getMessageText().setText(e.getMessage());
            }

            public void onSuccess(User user) {
                Firebug.log("login onSuccess");
                onLoggedIn(user);
            }
        });
    }

    private void showLoginForm() {
        final String username = Cookies.getCookie(COOKIE_LAST_USERNAME);
        final Focusable[] focusable = new Focusable[] { this.display.getUsernameFocus() };
        if (username != null) {
            this.display.getUsernameText().setText(username);
            focusable[0] = this.display.getPasswordFocus();
        }
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                focusable[0].setFocus(true);
            }
        });
        this.container.add(this.display.asWidget());
    }

    private void onLoggedIn(User user) {
        Cookies.setCookie(COOKIE_LAST_USERNAME, user.getUsername(), new Date(Long.MAX_VALUE));
        this.eventBus.fireEvent(new LoggedInEvent(user));
    }

    public void go(HasWidgets container) {
        this.container = container;
        login();
    }

    public void onLogout() {
        final Iterator iterator = this.container.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        this.container.add(new HTML("Restarting the application ...")); //$NON-NLS$
        this.loginService.logout(new AsyncCallback<Void>(){
            public void onFailure(Throwable e) {
                Firebug.error("logout failed", e);
            }

            public void onSuccess(Void result) {
                Firebug.log("logged out");
                Window.Location.reload();
            }
        });
    }
}
