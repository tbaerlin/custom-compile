package de.marketmaker.istar.domain.data;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ConvensysRawdata {
    String getContent();
    
    String getContent(String xsdSource);

    Map<String, Object> getAdditionalInformation();
}
