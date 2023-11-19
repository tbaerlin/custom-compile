package de.marketmaker.iview.mmgwt.mmweb.server;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

/**
 * Author: umaurer
 * Created: 09.02.15
 */
public class MySerializationPolicyProvider implements SerializationPolicyProvider {
    @Override
    public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
        return new SimpleSerializationPolicy();
    }
}
