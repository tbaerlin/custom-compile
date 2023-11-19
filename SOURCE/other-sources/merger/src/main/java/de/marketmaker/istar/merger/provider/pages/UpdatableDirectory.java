/*
 * UpdatableDirectory.java
 *
 * Created on 19.07.2010 17:05:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import org.apache.lucene.store.Directory;

/**
 * Interface used in {@link de.marketmaker.istar.merger.provider.pages.IndexDirectoryController}
 * to notify targets about a change of directory.
 * @author Sebastian Wild
 */
public interface UpdatableDirectory {

    /**
     * This method is called, whenever the used index directory has to be changed.
     * Implementations should make sure, not to use the old directory any longer, once they
     * return from this method; if needed, execution should be blocked. 
     * @param indexDirectory the new index directory
     */
    void setDirectory(Directory indexDirectory);
}
