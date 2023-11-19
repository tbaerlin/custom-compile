package de.marketmaker.iview.mmgwt.dmxmldocu.client.login;

import java.io.Serializable;

/**
 * @author umaurer
 */
public class LoginException extends RuntimeException implements Serializable {
    public LoginException() {
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginException(Throwable cause) {
        super(cause);
    }
}
