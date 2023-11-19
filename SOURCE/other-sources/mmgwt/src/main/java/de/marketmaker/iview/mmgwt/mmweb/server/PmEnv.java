package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.iview.pmxml.GetEnvironmentResponse;
import de.marketmaker.iview.pmxml.VoidRequest;
import org.springframework.remoting.RemoteLookupFailureException;

/**
 * Created on 03.07.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class PmEnv {
    private static final String FUNCTIONKEY_GET_ENV = "GetEnvironment";

    private final static Object mutex = new Object();
    private static PmEnv INSTANCE = null;

    private GetEnvironmentResponse response;

    public static GetEnvironmentResponse getEnvResponse(PmxmlHandler pmxmlHandler) throws RemoteLookupFailureException {
        synchronized (mutex) {
            if (INSTANCE == null) {
                INSTANCE = new PmEnv(pmxmlHandler);
            }
            return INSTANCE.response;
        }
    }

    private PmEnv(PmxmlHandler pmxmlHandler) throws RemoteLookupFailureException {
        this.response = loadEnvironmentInfos(pmxmlHandler);
    }

    private GetEnvironmentResponse loadEnvironmentInfos(PmxmlHandler pmxmlHandler) throws RemoteLookupFailureException {
        try {
            return pmxmlHandler.exchangeData(new VoidRequest(), FUNCTIONKEY_GET_ENV, GetEnvironmentResponse.class);
        }
        catch (RemoteLookupFailureException re) {
            throw re;
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
