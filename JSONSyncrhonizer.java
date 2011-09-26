package com.jsonsync;

import java.util.Date;
import com.jsonsync.utils.DateUtils;
import com.jsonsync.utils.json.JSONException;
import com.jsonsync.utils.json.JSONObject;

/**
 *
 * @author Arnaud CAVAILHEZ
 */
public class JSONSyncrhonizer {

    private static JSONSyncPersistenceInterface persistenceInteface;

    public static void startPersistence(JSONSyncPersistenceInterface persistenceIntefaceCandidate) {
        persistenceInteface = persistenceIntefaceCandidate;
    }

    public static void startLocalPersistence() {
        persistenceInteface = new JSONSyncHashMapPersistence();
    }

    public static JSONObject update(String syncId, String clientId, JSONObject newClientJSON) {
        try {
            //sanity null checks
            if (syncId == null) {
                throw new JSONSyncRuntimeException("JSONSyncrhonizer update called with null sync Id ");
            }
            if (clientId == null) {
                throw new JSONSyncRuntimeException("JSONSyncrhonizer update called with null client Id ");
            }
            if (newClientJSON == null) {
                throw new JSONSyncRuntimeException("JSONSyncrhonizer update called with null client son, please call subscribe instead ");
            }
            JSONObject syncJSON = getSyncJSON(syncId);
            if (syncJSON == null) {
                throw new JSONSyncRuntimeException("No sync data found with sync_id:" + syncId + " -- cannot update, please call subscribe first");
            }
            if (!syncJSON.has(clientId)) {
                throw new JSONSyncRuntimeException("No client JSON found with sync_id:" + syncId + " and client_id:" + clientId + " -- cannot update, please call subscribe if it's a new client_id");
            }
            JSONObject oldClientSyncJSON = syncJSON.getJSONObject(clientId);
            String oldClientJSONString = oldClientSyncJSON.getString("data");
            JSONObject oldClientJSON = new JSONObject(oldClientJSONString);
            //extract master data
            String masterJSONString = syncJSON.getString("master_data");
            JSONObject masterJSON = new JSONObject(masterJSONString);
            //synchronize
            JSONSyncUtils.updateMaster(masterJSON, oldClientJSON, newClientJSON);
            saveMasterJSON(syncId, clientId, masterJSON, syncJSON);
            return masterJSON;
        } catch (JSONException ex) {
            throw new JSONSyncRuntimeException("Could not sync", ex);
        }
    }

    public static JSONObject subscribe(String syncId, String clientId) {
        //sanity null checks
        if (syncId == null) {
            throw new JSONSyncRuntimeException("JSONSyncrhonizer subscribe called with null sync Id ");
        }
        if (clientId == null) {
            throw new JSONSyncRuntimeException("JSONSyncrhonizer subscribe called with null client Id ");
        }

        //create new sync entry (if first client) or subscribe client id
        JSONObject syncJSON = getSyncJSON(syncId);
        if (syncJSON == null) {
            //new sync id, create master entry
            JSONObject masterJSON = new JSONObject();
            try {
                saveMasterJSON(syncId, clientId, masterJSON, new JSONObject());
            } catch (Exception ex) {
                throw new JSONSyncRuntimeException("Could not create new sync entry with sync_id:" + syncId, ex);
            }
            return masterJSON;
        } else {
            try {
                //extract master data
                String masterJSONString = syncJSON.getString("master_data");
                JSONObject masterJSON = new JSONObject(masterJSONString);
                //save update
                saveMasterJSON(syncId, clientId, masterJSON, syncJSON);
                return masterJSON;
            } catch (JSONException ex) {
                throw new JSONSyncRuntimeException("Could not subscribe", ex);
            }
        }

    }

    public static JSONObject get(String syncId) {
        try {
            //sanity null checks
            if (syncId == null) {
                throw new JSONSyncRuntimeException("JSONSyncrhonizer subscribe called with null sync Id ");
            }
            JSONObject syncJSON = getSyncJSON(syncId);
            if (syncJSON == null) {
                throw new JSONSyncRuntimeException("No sync data found with sync_id:" + syncId + " -- cannot extract, please call subscribe first");
            }
            String masterJSONString = syncJSON.getString("master_data");
            JSONObject masterJSON = new JSONObject(masterJSONString);
            return masterJSON;
        } catch (JSONException ex) {
            throw new JSONSyncRuntimeException("Could not extract master data for sync_id:" + syncId, ex);
        }
    }

    private static void saveMasterJSON(String syncId, String clientId, JSONObject masterJSON, JSONObject syncJSON) throws JSONException {

        //store
        String now = DateUtils.encode(new Date());
        //store client sync data
        JSONObject newClientSyncJSON = new JSONObject();
        newClientSyncJSON.put("last_sync", now);
        newClientSyncJSON.put("data", masterJSON.toString());
        syncJSON.remove(clientId);
        syncJSON.put(clientId, newClientSyncJSON);
        //store master data
        syncJSON.remove("master_data");
        syncJSON.put("master_data", masterJSON.toString());
        syncJSON.remove("last_sync");
        syncJSON.put("last_sync", now);

        //store new master JSON to client
        try {
            persistenceInteface.put(syncId, syncJSON.toString());
        } catch (Exception ex) {
            throw new JSONSyncRuntimeException("Cannot save sync data with sync_id:" + syncId + " and client_id:" + clientId + " -- cannot update or subscribe, no data lost", ex);
        }
    }

    private static JSONObject getSyncJSON(String syncId) {
        String syncData = persistenceInteface.get(syncId);
        if (syncData == null) {
            return null;
        }

        try {
            JSONObject syncJSON = new JSONObject(syncData);
            return syncJSON;
        } catch (JSONException ex) {
            throw new JSONSyncRuntimeException("Cannot read sync data with sync_id:" + syncId + " -- cannot update or subscribe, serious problem", ex);
        }
    }
}
