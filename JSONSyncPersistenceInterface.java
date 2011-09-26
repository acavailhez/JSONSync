package com.jsonsync;

/**
 *
 * @author Arnaud CAVAILHEZ
 */
public interface JSONSyncPersistenceInterface {

    public String get(String key);

    public void put(String key, String value);
}
