/*
 * RatingSystemProvider.java
 *
 * Created on 08.11.2005 16:45:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.rating;

import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.domain.rating.RatingSystem;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface RatingSystemProvider {
    RatingSystem getRatingSystem(String name);
}
