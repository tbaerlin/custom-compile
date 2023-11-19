package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.itools.amqprpc.impl.RemoteAccessTimeoutException;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryActivityService;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryServiceException;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsRequest;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsResponse;
import de.marketmaker.iview.pmxml.block.PmExchangeData;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

@SuppressWarnings("GwtServiceNotRegistered")
public class OrderEntryActivityServiceImpl extends AbstractOrderEntryService implements OrderEntryActivityService {

    private final AtomicReference<BrokerageModuleID> brokerageModuleID = new AtomicReference<>(BrokerageModuleID.BM_NONE);

    @SuppressWarnings("unused")
    public void setBrokerageModuleID(String value) {
        try {
            this.brokerageModuleID.set(BrokerageModuleID.fromValue(value));
        } catch (Exception e) {
            this.logger.error(String.format("<setBrokerageModuleID> failed to parse '%s' as brokerage module ID", value));
        }
    }

    @Override
    public CheckAndSetBackendCredentialsResponse login(CheckAndSetBackendCredentialsRequest request) throws OrderEntryServiceException {
        final String pmSessionId = (String) WebUtils.getSessionAttribute(ServletRequestHolder.getHttpServletRequest(), ProfileResolver.PM_AUTHENTICATION_KEY);
        if (pmSessionId == null) {
            throw new IllegalArgumentException("<login> no active PM server session in HTTP session");
        }
        if (request == null) {
            throw new IllegalArgumentException("<login> request is null!");
        }

        if (request.getBrokerageModuleId() == null) {
            final BrokerageModuleID brokerageModuleID = this.brokerageModuleID.get();
            request.setBrokerageModuleId(brokerageModuleID);
            this.logger.trace(String.format("<login> using brokerage module %s", brokerageModuleID.value()));
        }
        else {
            this.logger.trace(String.format("<login> using brokerage module defined in request from Web UI %s", request.getBrokerageModuleId().value()));
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new OrderEntryServiceException("password_empty", "password must not be empty");
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<login> " + request.getBrokerageModuleId() + " for pm session " + pmSessionId);
        }

        try {
            request.setPasswordCrypted(isEncryptPassword());
            if (isEncryptPassword()) {
                request.setPassword(doCrypt(pmSessionId, request.getPassword()));
            }
        } catch (Exception e) {
            this.logger.error("<login> encrypt_password_failed failed for PM server session " + pmSessionId, e);
            throw new OrderEntryServiceException("encrypt_password_failed", e.getMessage());
        }

        try {
            PmExchangeData.bindToServer(ServletRequestHolder.getHttpServletRequest().getSession(false));
            @SuppressWarnings("UnnecessaryLocalVariable") final CheckAndSetBackendCredentialsResponse response = getPmxmlHandler()
                .exchangeData(request, "CheckAndSetBackendCredentials", CheckAndSetBackendCredentialsResponse.class);
            return response;
        } catch (Exception e) {
            this.logger.error("<login> failed for PM server session " + pmSessionId + " and brokerage module " + request.getBrokerageModuleId(), e);
            if (e.getCause() instanceof RemoteAccessTimeoutException) {
                throw new OrderEntryServiceException("remote_timeout", e.getCause().getMessage());
            }
            throw new OrderEntryServiceException("unspecified.error", e.getMessage(), e);
        }
    }
}
