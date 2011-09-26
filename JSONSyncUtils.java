package com.jsonsync;

import com.jsonsync.JSONSyncRuntimeException;
import java.util.Iterator;
import com.jsonsync.utils.json.JSONArray;
import com.jsonsync.utils.json.JSONException;
import com.jsonsync.utils.json.JSONObject;

/**
 *
 * @author Arnaud CAVAILHEZ
 */
class JSONSyncUtils {

    public static void updateMaster(JSONObject oldMasterJSON, JSONObject oldClientJSON, JSONObject newClientJSON) {

        JSONObject syncJSON = getSyncJSON(oldClientJSON, newClientJSON);
        //copy json
        applySyncToJSON(oldMasterJSON, syncJSON);
    }

    private static void applySyncToJSON(JSONObject json, JSONObject syncJSON) {
        try {
            JSONObject addJSON = syncJSON.getJSONObject("_add");
            JSONObject deleteJSON = syncJSON.getJSONObject("_delete");
            applySyncDeleteToJSON(json, deleteJSON);
            applySyncAddToJSON(json, addJSON);
        } catch (JSONException ex) {
            throw new JSONSyncRuntimeException("#SYNCHRONIZE_JSON failed ", ex);
        }
    }

    private static void applySyncDeleteToJSON(JSONObject json, JSONObject deleteJSON) {
        try {
            if (deleteJSON.has("_keys")) {
                JSONArray keys = deleteJSON.getJSONArray("_keys");
                for (int i = 0; i < keys.length(); i++) {
                    json.remove(keys.getString(i));
                }
            }
            for (Iterator<String> keys = deleteJSON.keys(); keys.hasNext();) {
                String key = keys.next();
                if (!key.equals("_keys")) {
                    Object deleteObject = deleteJSON.get(key);
                    if (deleteObject instanceof JSONObject) {
                        if (!json.has(key) || !(json.get(key) instanceof JSONObject)) {
                            //?
                            //log.warn("#SYNCHRONIZE_JSON asked to delete something that does not exist key = [" + key + "], doing nothing");
                        } else {
                            JSONObject deleteObjectJSON = (JSONObject) deleteObject;
                            JSONObject subJSON = json.getJSONObject(key);
                            applySyncDeleteToJSON(subJSON, deleteObjectJSON);
                        }
                    } else if (deleteObject instanceof JSONArray) {
                        if (!json.has(key) || !(json.get(key) instanceof JSONArray)) {
                            //?
                            //log.warn("#SYNCHRONIZE_JSON asked to delete something that does not exist key = [" + key + "], doing nothing");
                        } else {
                            JSONArray deleteObjectArray = (JSONArray) deleteObject;
                            JSONArray subArray = json.getJSONArray(key);
                            for (int i = 0; i < deleteObjectArray.length(); i++) {
                                Object objectToDelete = deleteObjectArray.get(i);
                                int index = JSONUtils.indexOfObjectInJSONArray(objectToDelete, subArray);
                                if (index != -1) {
                                    subArray.remove(index);
                                }
                            }
                            int t = 0;
                        }
                    }
                }

            }
        } catch (JSONException ex) {

            throw new JSONSyncRuntimeException("#SYNCHRONIZE_JSON failed ", ex);
        }
    }

    private static void applySyncAddToJSON(JSONObject json, JSONObject addJSON) {
        try {
            for (Iterator<String> keys = addJSON.keys(); keys.hasNext();) {
                String key = keys.next();

                Object addObject = addJSON.get(key);
                if (addObject instanceof JSONObject) {


                    if (!json.has(key) || !(json.get(key) instanceof JSONObject)) {
                        json.put(key, addObject);
                    } else {

                        JSONObject addObjectJSON = (JSONObject) addObject;
                        JSONObject subJSON = json.getJSONObject(key);
                        applySyncAddToJSON(subJSON, addObjectJSON);
                    }


                } else if (addObject instanceof JSONArray) {
                    if (!json.has(key) || !(json.get(key) instanceof JSONArray)) {
                        //add the entire array
                        json.put(key, addObject);
                    } else {
                        JSONArray addObjectArray = (JSONArray) addObject;
                        JSONArray subArray = json.getJSONArray(key);
                        for (int i = 0; i < addObjectArray.length(); i++) {
                            Object objectToAdd = addObjectArray.get(i);
                            int index = JSONUtils.indexOfObjectInJSONArray(objectToAdd, subArray);
                            if (index == -1) {
                                subArray.put(objectToAdd);
                            }
                        }
                        int t = 0;
                    }
                } else {
                    json.put(key, addObject);
                }
            }
        } catch (JSONException ex) {

            throw new JSONSyncRuntimeException("#SYNCHRONIZE_JSON failed ", ex);
        }
    }

    private static JSONObject getSyncJSON(JSONObject oldJSON, JSONObject newJSON) {
        try {
            JSONObject addJSON = new JSONObject();
            JSONObject deleteJSON = new JSONObject();
            JSONArray deleteKeysArray = new JSONArray();
            for (Iterator<String> keys = newJSON.keys(); keys.hasNext();) {

                String key = keys.next();
                Object newObject = newJSON.get(key);
                if (oldJSON.has(key)) {
                    Object oldObject = oldJSON.get(key);
                    if (!newObject.getClass().equals(oldObject.getClass())) {
                        addJSON.put(key, newObject);
                        //deleteKeysArray.put(key);
                    } else {
                        //if both are JSON
                        if (newObject instanceof JSONObject) {
                            JSONObject newObjectJSON = (JSONObject) newObject;
                            JSONObject oldObjectJSON = (JSONObject) oldObject;
                            JSONObject syncJSON = getSyncJSON(oldObjectJSON, newObjectJSON);
                            JSONObject subAddJSON = syncJSON.getJSONObject("_add");
                            if (subAddJSON.length() > 0) {
                                addJSON.put(key, subAddJSON);
                            }
                            JSONObject subDeleteJSON = syncJSON.getJSONObject("_delete");
                            if (subDeleteJSON.length() > 0) {
                                deleteJSON.put(key, subDeleteJSON);
                            }

                        } else if (newObject instanceof JSONArray) {
                            JSONArray newObjectArray = (JSONArray) newObject;
                            JSONArray oldObjectArray = (JSONArray) oldObject;
                            JSONObject syncArrayJSON = getDifferenceBetweenArray(oldObjectArray, newObjectArray);
                            JSONArray addArray = syncArrayJSON.getJSONArray("_add");
                            JSONArray deleteArray = syncArrayJSON.getJSONArray("_delete");
                            addJSON.put(key, addArray);
                            deleteJSON.put(key, deleteArray);
                        } else {
                            if (!newObject.equals(oldObject)) {
                                addJSON.put(key, newObject);
                                //deleteJSON.put(key, oldObject);
                            }
                        }
                    }

                } else {
                    addJSON.put(key, newObject);
                }

            }
            for (Iterator<String> keys = oldJSON.keys(); keys.hasNext();) {
                String key = keys.next();

                if (!newJSON.has(key)) {
                    deleteKeysArray.put(key);
                }

            }
            if (deleteKeysArray.length() > 0) {
                deleteJSON.put("_keys", deleteKeysArray);
            }
            JSONObject syncJSON = new JSONObject();
            syncJSON.put("_add", addJSON);
            syncJSON.put("_delete", deleteJSON);
            return syncJSON;
        } catch (JSONException ex) {

            throw new JSONSyncRuntimeException("#SYNCHRONIZE_JSON failed ", ex);
        }
    }

    private static JSONObject getDifferenceBetweenArray(JSONArray oldArray, JSONArray newArray) {
        /*try {
        JSONArray deleteArray = new JSONArray();
        JSONArray addArray = new JSONArray();
        //parse newArray to know what to add
        for (int i = 0; i < newArray.length(); i++) {
        Object o = newArray.get(i);
        int index = JSONUtils.indexOfObjectInJSONArray(o, oldArray);
        if (index != i) {
        addArray.put(o);
        }
        }

        //parse oldArray to know what to add
        for (int i = 0; i < oldArray.length(); i++) {
        Object o = oldArray.get(i);
        int index = JSONUtils.indexOfObjectInJSONArray(o, newArray);
        if (index != i) {
        deleteArray.put(o);
        }
        }

        JSONObject syncJSON = new JSONObject();
        syncJSON.put("_add", addArray);
        syncJSON.put("_delete", deleteArray);
        return syncJSON;
        } catch (JSONException ex) {
        throw new JSONSyncRuntimeException("#SYNCHRONIZE_JSON failed ", ex);
        }*/
        //tmp hack : hardcore all-copy method
        try {
            if (!oldArray.equals(newArray)) {
                JSONObject syncJSON = new JSONObject();
                syncJSON.put("_add", newArray);
                syncJSON.put("_delete", oldArray);
                return syncJSON;
            } else {
                JSONObject syncJSON = new JSONObject();
                syncJSON.put("_add", new JSONArray());
                syncJSON.put("_delete", new JSONArray());
                return syncJSON;
            }
        } catch (JSONException ex) {
            throw new JSONSyncRuntimeException("#SYNCHRONIZE_JSON failed ", ex);
        }
    }
}
