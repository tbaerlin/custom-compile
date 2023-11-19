package de.marketmaker.istar.merger.qos;

import java.io.IOException;
import java.io.Serializable;

public interface QosDao {
    void store(String key, Serializable value) throws Exception;

    Serializable retrieve(String key) throws ClassNotFoundException, IOException;
}
