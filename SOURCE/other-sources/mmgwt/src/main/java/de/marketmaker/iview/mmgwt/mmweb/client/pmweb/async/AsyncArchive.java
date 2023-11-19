package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import java.util.HashMap;
import java.util.Map;

/**
 * User: umaurer
 * Date: 29.11.13
 * Time: 11:59
 */
public class AsyncArchive {
    public static final AsyncArchive I = new AsyncArchive();
    private final Map<String, ArchiveData> mapByHandle = new HashMap<>();

    private AsyncArchive() {
    }

    public void add(ArchiveData archiveData) {
        this.mapByHandle.put(archiveData.getHandle(), archiveData);
    }

    public ArchiveData getByHandle(String handle) {
        return mapByHandle.get(handle);
    }
}
