package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Author: umaurer
 * Created: 21.02.14
 */
public class MmwebServiceException extends RuntimeException implements IsSerializable {
    public MmwebServiceException() {
    }

    public MmwebServiceException(String message) {
        super(message);
    }

    public MmwebServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MmwebServiceException(Throwable cause) {
        super(cause);
    }
}
