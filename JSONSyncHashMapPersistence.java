package com.jsonsync;

import com.jsonsync.JSONSyncPersistenceInterface;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Arnaud CAVAILHEZ
 */
class JSONSyncHashMapPersistence implements JSONSyncPersistenceInterface {

    private HashMap<String, String> hash;

    public String toString() {
        String out = "";
        for (Map.Entry<String, String> entry : hash.entrySet()) {
            out += entry.getKey() + "===" + entry.getValue() + "\n";
        }
        return out;
    }

    public JSONSyncHashMapPersistence() {
        hash = new HashMap<String, String>();
    }

    public String get(String key) {
        return hash.get(key);
    }

    public void put(String key, String value) {
        hash.put(key, value);
    }
}
