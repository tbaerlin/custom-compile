package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.itools.pmxml.frontend.PmxmlCryptUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryServiceException;
import org.apache.commons.codec.binary.Base64;

public abstract class AbstractOrderEntryService extends GwtService {

    private PmxmlHandler pmxmlHandler;
    /**
     * The default value is true for production purposes/releases, so there is no need to expose the existence of the property to customer's outsourced admins in XML config files.
     */
    private boolean encryptPassword = true;

    //8B7AE67D
    private static final byte[] iv = {0x38, 0x42, 0x37, 0x41, 0x45, 0x36, 0x37, 0x44};

    @SuppressWarnings("RedundantThrows")
    protected String doCrypt(String key, String password) throws Exception {
        try {
            final byte[] encrypted = PmxmlCryptUtil.encrypt(key, password + "#", iv);

            if (this.logger.isDebugEnabled()) {
                final StringBuilder sb = new StringBuilder("encrypted=");
                for (byte b : encrypted) {
                    sb.append(b).append("|");
                }
                this.logger.debug(sb.toString());
            }

            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            throw new OrderEntryServiceException("encrypt_password_failed", "Encrypting password failed", e);
        }
    }

    /**
     * The default value is true for production purposes/releases, so there is no need to expose the existence of the property to customer's outsourced admins in XML config files. For debugging you
     * may use this property to turn the encryption off.
     */
    @SuppressWarnings("unused")
    public void setEncryptPassword(boolean encryptPassword) {
        this.encryptPassword = encryptPassword;
    }

    @SuppressWarnings("unused")
    public void setPmxmlHandler(PmxmlHandler pmxmlHandler) {
        this.pmxmlHandler = pmxmlHandler;
    }

    public boolean isEncryptPassword() {
        return encryptPassword;
    }

    protected PmxmlHandler getPmxmlHandler() {
        return pmxmlHandler;
    }
}
