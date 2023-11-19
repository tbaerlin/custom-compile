package de.marketmaker.iview.mmgwt.mmweb.client.as.sps;

import de.marketmaker.iview.pmxml.SimpleMM;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: umaurer
 * Created: 27.01.14
 */
public class SpsServerData implements Serializable {
    private HashMap<String,SimpleMM> map;

    public SpsServerData() {
        this.map = new HashMap<>();
    }

    public SpsServerData(int initialSize) {
        this.map = new HashMap<>(initialSize);
    }

    public SimpleMM get(String key) {
        return this.map.get(key);
    }

    public SimpleMM put(String key, SimpleMM value) {
        return this.map.put(key, value);
    }

    public boolean containsKey(String bindKey) {
        return this.map.containsKey(bindKey);
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    public Map<String, SimpleMM> asMap() {
        return this.map;
    }
}
