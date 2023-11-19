package de.marketmaker.istar.merger.provider;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.instrument.ContentFlags;

/**
 * implement this to push content flags into MDP
 */
@FunctionalInterface
public interface ContentFlagsWriter {

    Logger logger = LoggerFactory.getLogger(ContentFlagsWriter.class);

    ContentFlagsWriter NULL = map -> {
        logger.info("ContentFlags:");
        for (Map.Entry<String, Set<ContentFlags.Flag>> e : map.entrySet()) {
            logger.info(" " + e.getKey() + " => " + e.getValue());
        }
    };

    // mapping iids (without '.iid'-postfix) to content flags
    void writeContentFlags(Map<String, Set<ContentFlags.Flag>> map);

}
